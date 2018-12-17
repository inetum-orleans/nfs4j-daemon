package io.github.toilal.nsf4j.api;

import io.github.toilal.nsf4j.Daemon;
import io.github.toilal.nsf4j.config.ApiConfig;
import io.github.toilal.nsf4j.config.ShareConfig;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.ipAddress;
import static spark.Spark.port;
import static spark.Spark.post;

public class Api {
    private final Daemon daemon;
    private final ApiConfig config;

    public Api(ApiConfig config, Daemon daemon) {
        this.config = config;
        this.daemon = daemon;
    }

    public void start() {
        if (config.getPort() != null) {
            port(this.config.getPort());
        }

        if (config.getIp() != null) {
            ipAddress(this.config.getIp());
        }

        if (config.getBearer() != null) {
            before((req, res) -> {
                String authorization = req.headers("Authorization");
                if (authorization == null) {
                    res.header("WWW-Authenticate", "Bearer");
                    halt(401);
                } else if (!authorization.equals("Bearer " + config.getBearer())) {
                    halt(403);
                }
            });
        }

        get("/ping", (req, res) -> {
            res.header("content-type", "application/json");
            return "pong";
        }, new JsonTransformer());

        get("status", (req, res) -> {
            res.header("content-type", "application/json");
            return this.getStatus();
        }, new JsonTransformer());

        post("/attach", "application/json", (req, res) -> {
            ShareConfig shareConfig = JsonTransformer.gson.fromJson(req.body(), ShareConfig.class);
            this.attach(shareConfig);
            return null;
        });

        post("/detach", "application/json", (req, res) -> {
            ShareConfig shareConfig = JsonTransformer.gson.fromJson(req.body(), ShareConfig.class);
            this.detach(shareConfig);
            this.stop();
            return null;
        });

        post("/stop", (req, res) -> {
            this.stop();
            return null;
        });

        post("/restart", (req, res) -> "");
    }

    private Object getStatus() {
        return this.daemon.getStatus();
    }

    private void detach(ShareConfig share) {
        this.daemon.detach(share);
    }

    private void attach(ShareConfig share) {
        this.daemon.attach(share);
    }

    public void stop() {
        stop();
    }
}
