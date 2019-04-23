package world.gfi.nfs4j;

import org.yaml.snakeyaml.Yaml;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import world.gfi.nfs4j.config.ApiConfig;
import world.gfi.nfs4j.config.Config;
import world.gfi.nfs4j.config.CustomConstructor;
import world.gfi.nfs4j.config.CustomRepresenter;
import world.gfi.nfs4j.config.ShareConfig;
import world.gfi.nfs4j.fs.permission.PermissionsMapperType;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Main entrypoint for nfs4j daemon.
 */
@Command(sortOptions = false)
public class Main implements Callable<Void> {
    @Option(names = {"-c", "--config"}, description = "Path to configuration file", defaultValue = "nfs4j.yml")
    private Path config;

    @Parameters(description = "Directories to share")
    private List<String> shares;

    @Option(names = {"-u", "--uid"}, description = "Default user id to use for exported files")
    private Integer uid;

    @Option(names = {"-g", "--gid"}, description = "Default group id to use for exported files")
    private Integer gid;

    @Option(names = {"-m", "--mask"}, description = "Default mask to use for exported files")
    private Integer mask;
    
    @Option(names = {"-t", "--permission-type"}, 
            description = "Permission type to use (DISABLED, EMULATED, UNIX)", defaultValue = "DISABLED", showDefaultValue = CommandLine.Help.Visibility.ALWAYS)
    private PermissionsMapperType permissionType = null;

    @Option(names = {"-p", "--port"}, description = "Port to use")
    private Integer port;

    @Option(names = {"--api"}, description = "Enable HTTP API")
    private Boolean api;

    @Option(names = {"--api-port"}, description = "Port to use for API")
    private Integer apiPort;

    @Option(names = {"--api-ip"}, description = "Ip to use for API")
    private String apiIp;

    @Option(names = {"--api-bearer"}, description = "Bearer to use for API authentication")
    private String apiBearer;

    @Option(names = {"--no-share"}, description = "Disable default share and configured shares")
    private Boolean noShare;

    @Option(names = {"--udp"}, description = "Use UDP instead of TCP")
    private Boolean udp;

    @Option(names = {"--portmap-disabled"}, description = "Disable embedded portmap service")
    private Boolean portmapDisabled;

    @Option(names = {"-e", "--exports"}, description = "Path to exports file (nsf4j advanced configuration)")
    private Path exports;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message")
    boolean help;

    private Daemon daemon;

    public static void main(String[] args) {
        CommandLine.call(new Main(), args);
    }

    @Override
    public Void call() throws Exception {
        if (this.help) {
            CommandLine.usage(this, System.out);
            return null;
        }

        Yaml yaml = new Yaml(new CustomConstructor(), new CustomRepresenter());
        Config config = null;

        File configFile = this.config.toFile();
        if (configFile != null && configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(this.config.toFile())) {
                config = yaml.loadAs(fis, Config.class);
            }
        }

        if (config == null) {
            config = new Config();
        }

        if (this.port != null) {
            config.setPort(this.port);
        }

        if (this.udp != null) {
            config.setUdp(this.udp);
        }

        if (this.portmapDisabled != null) {
            config.setPortmapDisabled(this.portmapDisabled);
        }

        if (this.uid != null) {
            config.getPermissions().setUid(uid);
        }

        if (this.gid != null) {
            config.getPermissions().setGid(gid);
        }

        if (this.mask != null) {
            config.getPermissions().setMask(mask);
        }

        if (this.permissionType != null) {
            config.getPermissions().setType(permissionType);
        }

        if (this.api != null || this.apiPort != null || this.apiIp != null || this.apiBearer != null) {
            ApiConfig apiConfig = new ApiConfig();

            if (this.apiPort != null) {
                apiConfig.setPort(this.apiPort);
            }

            if (this.apiIp != null) {
                apiConfig.setIp(this.apiIp);
            }

            if (this.apiBearer != null) {
                apiConfig.setBearer(this.apiBearer);
            }

            config.setApi(apiConfig);
        }

        if (this.noShare != null) {
            config.setNoShare(this.noShare);
        }

        if (config.getNoShare() == null || !config.getNoShare()) {
            if (this.shares != null) {
                List<ShareConfig> configShares = config.getShares();
                configShares.clear();
                for (String share : this.shares) {
                    configShares.add(ShareConfig.fromString(share));
                }
            }

            if (config.getShares().size() <= 0) {
                throw new IllegalArgumentException("At least one share should be defined.");
            }

            for (ShareConfig share : config.getShares()) {
                if (share.getAlias() == null) {
                    if (config.getShares().size() > 1) {
                        String defaultAlias = share.buildDefaultAlias();
                        share.setAlias(defaultAlias);
                    } else {
                        share.setAlias("/");
                    }
                }
            }
        } else {
            config.getShares().clear();
        }

        if (this.exports != null) {
            config.setExportFile(this.exports);
        }

        this.daemon = new Daemon(config);
        this.daemon.start();

        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e) {
                break;
            }
        }

        return null;
    }
}
