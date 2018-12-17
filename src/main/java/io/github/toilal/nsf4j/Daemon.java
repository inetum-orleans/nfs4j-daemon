package io.github.toilal.nsf4j;

import io.github.toilal.nsf4j.api.Api;
import io.github.toilal.nsf4j.config.Config;
import io.github.toilal.nsf4j.config.ShareConfig;
import io.github.toilal.nsf4j.fs.AttachableFileSystem;
import io.github.toilal.nsf4j.fs.DefaultFileSystemFactory;
import io.github.toilal.nsf4j.fs.RootFileSystem;
import io.github.toilal.nsf4j.fs.handle.UniqueAtomicLongGenerator;
import io.github.toilal.nsf4j.fs.permission.DefaultPermissionsMapperFactory;
import io.github.toilal.nsf4j.fs.permission.PermissionsMapper;
import io.github.toilal.nsf4j.status.Status;
import io.github.toilal.nsf4j.status.StatusShare;
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
    private final OncRpcSvc nfsSvc;
    private final PermissionsMapper defaultPermissionsMapper;
    private final DefaultPermissionsMapperFactory permissionsMapperFactory;
    private final DefaultFileSystemFactory fsFactory;
    private final UniqueAtomicLongGenerator uniqueHandleGenerator;
    private final Config config;
    private RootFileSystem vfs;
    private Api api;
    private OncRpcEmbeddedPortmap portmapSvc = null;

    public Daemon(Config config) {
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
        defaultPermissionsMapper = permissionsMapperFactory.newPermissionsMapper(config.getPermissions());
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

    public AttachableFileSystem attach(ShareConfig share) {
        PermissionsMapper permissionsMapper = defaultPermissionsMapper;
        if (share.getPermissions() != null) {
            permissionsMapper = permissionsMapperFactory.newPermissionsMapper(share.getPermissions());
        }
        AttachableFileSystem shareVfs = fsFactory.newFileSystem(share.getPath(), permissionsMapper, uniqueHandleGenerator);
        vfs.attachFileSystem(shareVfs, share.getAlias());
        return shareVfs;
    }

    public AttachableFileSystem detach(ShareConfig share) {
        return vfs.detachFileSystem(share.getAlias());
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
        status.setConfig(this.config);
        List<StatusShare> shares = new ArrayList<>();
        for (Map.Entry<String, AttachableFileSystem> vfsEntry : this.vfs.getFileSystems().entrySet()) {
            shares.add(new StatusShare(vfsEntry.getKey(), vfsEntry.getValue().getRoot().toString()));
        }
        status.setShares(shares);
        return status;
    }
}