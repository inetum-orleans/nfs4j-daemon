package io.github.toilal.nsf4j.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration class of nfs4j daemon.
 */
public class Config {
    private int port = 2049;
    private boolean udp = false;
    private List<Share> shares = Arrays.asList(new Share(Paths.get(".")));
    private Path exportFile;

    public Config() {
        this.shares = shares;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isUdp() {
        return udp;
    }

    public void setUdp(boolean udp) {
        this.udp = udp;
    }

    public Path getExportFile() {
        return exportFile;
    }

    public void setExportFile(Path exportFile) {
        this.exportFile = exportFile;
    }

    public List<Share> getShares() {
        return shares;
    }

    public void setShares(List<Share> shares) {
        this.shares = shares;
    }
}
