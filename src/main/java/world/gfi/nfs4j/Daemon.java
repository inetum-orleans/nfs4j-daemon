package world.gfi.nfs4j;

import org.dcache.nfs.ExportFile;
import org.dcache.nfs.v3.MountServer;
import org.dcache.nfs.v3.NfsServerV3;
import org.dcache.nfs.v3.xdr.mount_prot;
import org.dcache.nfs.v3.xdr.nfs3_prot;
import org.dcache.nfs.v4.MDSOperationFactory;
import org.dcache.nfs.v4.NFSServerV41;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.oncrpc4j.portmap.OncRpcEmbeddedPortmap;
import org.dcache.oncrpc4j.rpc.IoStrategy;
import org.dcache.oncrpc4j.rpc.OncRpcProgram;
import org.dcache.oncrpc4j.rpc.OncRpcSvc;
import org.dcache.oncrpc4j.rpc.OncRpcSvcBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.gfi.nfs4j.api.Api;
import world.gfi.nfs4j.api.JsonTransformer;
import world.gfi.nfs4j.config.Config;
import world.gfi.nfs4j.config.PermissionsConfig;
import world.gfi.nfs4j.config.ShareConfig;
import world.gfi.nfs4j.exceptions.AttachException;
import world.gfi.nfs4j.fs.AttachableFileSystem;
import world.gfi.nfs4j.fs.DefaultFileSystemFactory;
import world.gfi.nfs4j.fs.RootFileSystem;
import world.gfi.nfs4j.fs.handle.UniqueAtomicLongGenerator;
import world.gfi.nfs4j.fs.permission.DefaultPermissionsMapperFactory;
import world.gfi.nfs4j.fs.permission.PermissionsMapper;
import world.gfi.nfs4j.status.Status;
import world.gfi.nfs4j.status.StatusShare;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * nfs4j Daemon.
 */
public class Daemon implements Closeable {
    private static final Logger LOG = LoggerFactory.getLogger(Daemon.class);

    private final OncRpcSvc nfsSvc;
    private final DefaultPermissionsMapperFactory permissionsMapperFactory;
    private final DefaultFileSystemFactory fsFactory;
    private final UniqueAtomicLongGenerator uniqueHandleGenerator;
    private final Config config;
    private RootFileSystem vfs;
    private Api api;
    private OncRpcEmbeddedPortmap portmapSvc = null;

    public Daemon(Config config) throws AttachException {
        this.config = config;

        ExportFile exportFile;
        if (config.getExportFile() == null) {
            try (InputStream stream = this.getClass().getClassLoader().getResourceAsStream("exports")) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                exportFile = new ExportFile(reader);
            } catch (IOException e) {
                throw new IOError(e);
            }
        } else {
            try (InputStream stream = new FileInputStream(config.getExportFile().toFile())) {
                Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
                exportFile = new ExportFile(reader);
            } catch (IOException e) {
                throw new IOError(e);
            }
        }

        OncRpcSvcBuilder rpcBuilder = new OncRpcSvcBuilder()
                .withPort(config.getPort())
                .withAutoPublish()
                .withIoStrategy(IoStrategy.LEADER_FOLLOWER)
                .withServiceName("nfs4j@" + config.getPort());

        if (config.isUdp()) {
            rpcBuilder.withUDP();
        } else {
            rpcBuilder.withTCP();
        }

        nfsSvc = rpcBuilder.build();

        uniqueHandleGenerator = new UniqueAtomicLongGenerator();

        fsFactory = new DefaultFileSystemFactory();
        permissionsMapperFactory = new DefaultPermissionsMapperFactory();

        vfs = new RootFileSystem(config.getPermissions(), uniqueHandleGenerator);
        for (ShareConfig share : config.getShares()) {
            attach(share);
        }

        NFSServerV41 nfs4 = new NFSServerV41.Builder()
                .withVfs(vfs)
                .withOperationFactory(new MDSOperationFactory())
                .withExportFile(exportFile)
                .build();

        NfsServerV3 nfs3 = new NfsServerV3(exportFile, vfs);
        MountServer mountd = new MountServer(exportFile, vfs);

        if (!config.isPortmapDisabled()) {
            portmapSvc = new OncRpcEmbeddedPortmap();
        }

        nfsSvc.register(new OncRpcProgram(mount_prot.MOUNT_PROGRAM, mount_prot.MOUNT_V3), mountd);
        nfsSvc.register(new OncRpcProgram(nfs3_prot.NFS_PROGRAM, nfs3_prot.NFS_V3), nfs3);
        nfsSvc.register(new OncRpcProgram(nfs4_prot.NFS4_PROGRAM, nfs4_prot.NFS_V4), nfs4);

        if (config.getApi() != null) {
            api = new Api(config.getApi(), this);
        }
    }

    public AttachableFileSystem attach(ShareConfig share) throws AttachException {
        String alias = share.getAlias();
        if (alias == null) {
            alias = share.buildDefaultAlias();
        } else if (share.isAppendDefaultAlias()) {
            alias = alias + share.buildDefaultAlias();
        }

        PermissionsConfig permissions = share.getPermissions() == null ? this.config.getPermissions() : share.getPermissions();
        PermissionsMapper permissionsMapper = null;

        try {
            permissionsMapper = permissionsMapperFactory.newPermissionsMapper(permissions, share, alias);
        } catch (IOException e) {
            throw new AttachException("Can't create permissions mapper", e);
        }

        AttachableFileSystem shareVfs = fsFactory.newFileSystem(share.getPath(), permissionsMapper, uniqueHandleGenerator);
        vfs.attachFileSystem(shareVfs, alias);
        share.setPath(shareVfs.getRoot());
        share.setAlias(shareVfs.getAlias());
        LOG.info("Share has been attached: " + share);
        return shareVfs;
    }

    public AttachableFileSystem detach(ShareConfig share) throws AttachException {
        AttachableFileSystem detached = vfs.detachFileSystem(share.getAlias());
        share.setPath(detached.getRoot());
        share.setAlias(detached.getAlias());
        LOG.info("Share has been detached: " + share);
        return detached;
    }

    public void start() throws IOException {
        nfsSvc.start();
        if (api != null) {
            api.start();
        }
    }

    @Override
    public void close() throws IOException {
        if (api != null) {
            api.stop();
        }
        if (portmapSvc != null) {
            portmapSvc.shutdown();
        }
        nfsSvc.stop();
    }

    public Object getStatus() {
        Status status = new Status();
        Config configCopy = JsonTransformer.gson.fromJson(JsonTransformer.gson.toJson(this.config), Config.class);
        configCopy.setShares(null);
        status.setConfig(configCopy);
        List<StatusShare> shares = new ArrayList<>();
        for (Map.Entry<String, AttachableFileSystem> vfsEntry : this.vfs.getFileSystems().entrySet()) {
            shares.add(new StatusShare(vfsEntry.getKey(), vfsEntry.getValue().getRoot().toString()));
        }
        status.setShares(shares);
        return status;
    }
}