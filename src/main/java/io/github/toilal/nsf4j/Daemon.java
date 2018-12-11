package io.github.toilal.nsf4j;

import io.github.toilal.nsf4j.config.Config;
import io.github.toilal.nsf4j.fs.PosixFileSystem;
import io.github.toilal.nsf4j.fs.WindowsFileSystem;
import org.dcache.nfs.ExportFile;
import org.dcache.nfs.v3.MountServer;
import org.dcache.nfs.v3.NfsServerV3;
import org.dcache.nfs.v3.xdr.mount_prot;
import org.dcache.nfs.v3.xdr.nfs3_prot;
import org.dcache.nfs.v4.MDSOperationFactory;
import org.dcache.nfs.v4.NFSServerV41;
import org.dcache.nfs.v4.xdr.nfs4_prot;
import org.dcache.nfs.vfs.VirtualFileSystem;
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

public class Daemon implements Closeable {
    private final OncRpcSvc nfsSvc;

    public Daemon(Config config) {
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

        String name = "nfs4j@" + config.getPort();

        VirtualFileSystem vfs;
        if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
            vfs = new WindowsFileSystem(config.getRoot());
        } else {
            vfs = new PosixFileSystem(config.getRoot());
        }

        OncRpcSvcBuilder rpcBuilder = new OncRpcSvcBuilder()
                .withPort(config.getPort())
                .withAutoPublish()
                .withIoStrategy(IoStrategy.LEADER_FOLLOWER)
                .withServiceName(name);

        if (config.isUdp()) {
            rpcBuilder.withUDP();
        } else {
            rpcBuilder.withTCP();
        }

        nfsSvc = rpcBuilder.build();

        NFSServerV41 nfs4 = new NFSServerV41.Builder()
                .withVfs(vfs)
                .withOperationFactory(new MDSOperationFactory())
                .withExportFile(exportFile)
                .build();

        NfsServerV3 nfs3 = new NfsServerV3(exportFile, vfs);
        MountServer mountd = new MountServer(exportFile, vfs);

        nfsSvc.register(new OncRpcProgram(mount_prot.MOUNT_PROGRAM, mount_prot.MOUNT_V3), mountd);
        nfsSvc.register(new OncRpcProgram(nfs3_prot.NFS_PROGRAM, nfs3_prot.NFS_V3), nfs3);
        nfsSvc.register(new OncRpcProgram(nfs4_prot.NFS4_PROGRAM, nfs4_prot.NFS_V4), nfs4);
    }

    public void start() throws IOException {
        nfsSvc.start();
    }

    @Override
    public void close() throws IOException {
        nfsSvc.stop();
    }
}
