package io.github.toilal.nsf4j.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration class of nfs4j daemon.
 */
public class Config {
    private int port = 2049;
    private boolean udp = false;
    private boolean portmapDisabled = false;
    private ApiConfig api = null;
    private PermissionsConfig permissions = new PermissionsConfig();
    private Boolean noShare = null;
    private List<ShareConfig> shares = new ArrayList<>(Arrays.asList(new ShareConfig(Paths.get("."))));
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

    public boolean isPortmapDisabled() {
        return portmapDisabled;
    }

    public void setPortmapDisabled(boolean portmapDisabled) {
        this.portmapDisabled = portmapDisabled;
    }

    public ApiConfig getApi() {
        return api;
    }

    public void setApi(ApiConfig api) {
        this.api = api;
    }

    public PermissionsConfig getPermissions() {
        return permissions;
    }

    public void setPermissions(PermissionsConfig permissions) {
        this.permissions = permissions;
    }

    public Path getExportFile() {
        return exportFile;
    }

    public void setExportFile(Path exportFile) {
        this.exportFile = exportFile;
    }

    public Boolean getNoShare() {
        return noShare;
    }

    public void setNoShare(Boolean noShare) {
        this.noShare = noShare;
    }

    public List<ShareConfig> getShares() {
        return shares;
    }

    public void setShares(List<ShareConfig> shares) {
        this.shares = shares;
    }
}
