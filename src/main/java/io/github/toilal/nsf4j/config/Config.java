package io.github.toilal.nsf4j.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Config {
    private int port = 2049;
    private boolean udp = false;
    private Path root = Paths.get(".");
    private Path exportFile;

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

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        this.root = root;
    }

    public Path getExportFile() {
        return exportFile;
    }

    public void setExportFile(Path exportFile) {
        this.exportFile = exportFile;
    }
}
