package io.github.toilal.nsf4j;

import io.github.toilal.nsf4j.config.Config;
import io.github.toilal.nsf4j.config.CustomConstructor;
import io.github.toilal.nsf4j.config.CustomRepresenter;
import io.github.toilal.nsf4j.config.Share;
import org.yaml.snakeyaml.Yaml;
import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.Callable;


/**
 * Main entrypoint for nfs4j daemon.
 */
public class Main implements Callable<Void> {
    @Option(names = {"-c", "--config"}, description = "Path to configuration file", defaultValue = "nfs4j.yml")
    private Path config;

    @Option(names = {"-s", "--share"}, description = "Root directory to serve")
    private Path share;

    @Option(names = {"-p", "--port"}, description = "RPC port to use")
    private Integer port;

    @Option(names = {"-u", "--udp"}, description = "Use UDP instead of TCP")
    private Boolean udp;

    @Option(names = {"-e", "--exports"}, description = "Path to exports file")
    private Path exports;

    private Daemon daemon;

    public static void main(String[] args) {
        CommandLine.call(new Main(), args);
    }

    @Override
    public Void call() throws Exception {
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

        if (this.share != null) {
            config.setShares(Arrays.asList(new Share(this.share, "/")));
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
