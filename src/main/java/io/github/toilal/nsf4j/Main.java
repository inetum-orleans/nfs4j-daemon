package io.github.toilal.nsf4j;

import io.github.toilal.nsf4j.config.Config;
import io.github.toilal.nsf4j.config.CustomConstructor;
import io.github.toilal.nsf4j.config.CustomRepresenter;
import io.github.toilal.nsf4j.config.Share;
import org.yaml.snakeyaml.Yaml;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    @Option(names = {"-p", "--port"}, description = "Port to use")
    private Integer port;

    @Option(names = {"--udp"}, description = "Use UDP instead of TCP")
    private Boolean udp;

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

        if (this.uid != null) {
            config.getPermissions().setUid(uid);
        }

        if (this.gid != null) {
            config.getPermissions().setGid(gid);
        }

        if (this.mask != null) {
            config.getPermissions().setMask(mask);
        }

        if (this.shares != null) {
            List<Share> configShares = config.getShares();
            configShares.clear();
            for (String share : this.shares) {
                configShares.add(Share.fromString(share));
            }
        }

        if (config.getShares().size() <= 0) {
            throw new IllegalArgumentException("At least one share should be defined.");
        }

        for (Share share : config.getShares()) {
            if (share.getAlias() == null) {
                if (config.getShares().size() > 1) {
                    String defaultAlias = share.getPath().toAbsolutePath().normalize().toString().replace(":", "").replace(File.separator, "/");
                    if (!defaultAlias.startsWith("/")) {
                        defaultAlias = "/" + defaultAlias;
                    }
                    share.setAlias(defaultAlias);
                } else {
                    share.setAlias("/");
                }
            }
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
