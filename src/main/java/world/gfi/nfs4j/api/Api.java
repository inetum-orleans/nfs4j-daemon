package world.gfi.nfs4j.api;

import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import world.gfi.nfs4j.Daemon;
import world.gfi.nfs4j.config.ApiConfig;
import world.gfi.nfs4j.config.ShareConfig;
import world.gfi.nfs4j.exceptions.AliasAlreadyExistsException;
import world.gfi.nfs4j.exceptions.AlreadyAttachedException;
import world.gfi.nfs4j.exceptions.AttachException;
import world.gfi.nfs4j.exceptions.NoSuchAliasException;
import world.gfi.nfs4j.fs.AttachableFileSystem;

import static spark.Spark.before;
import static spark.Spark.exception;
import static spark.Spark.get;
import static spark.Spark.halt;
import static spark.Spark.ipAddress;
import static spark.Spark.port;
import static spark.Spark.post;

public class Api {
    private final Daemon daemon;
    private final ApiConfig config;

    private static final Logger logger = LoggerFactory.getLogger(Api.class);

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

        exception(Exception.class, (exception, req, res) -> {
            res.status(500);
            res.type("application/json");
            logger.error(exception.getMessage(), exception);
            Error error = new Error(exception.getMessage());
            res.body(new JsonTransformer().render(error));
        });

        exception(JsonSyntaxException.class, (exception, req, res) -> {
            res.status(400);
            res.type("application/json");
            Error error = new Error(exception.getMessage());
            res.body(new JsonTransformer().render(error));
        });

        exception(AliasAlreadyExistsException.class, (exception, req, res) -> {
            res.status(409);
            res.type("application/json");
            Error error = new Error(exception.getMessage());
            AttachableFileSystem fs = exception.getExistingFs();
            ShareConfig shareConfig = new ShareConfig();
            shareConfig.setPath(fs.getRoot());
            shareConfig.setAlias(fs.getAlias());
            error.setData(shareConfig);
            res.body(new JsonTransformer().render(error));
        });

        exception(AlreadyAttachedException.class, (exception, req, res) -> {
            res.status(409);
            res.type("application/json");
            Error error = new Error(exception.getMessage());
            AttachableFileSystem fs = exception.getFs();
            ShareConfig shareConfig = new ShareConfig();
            shareConfig.setPath(fs.getRoot());
            shareConfig.setAlias(fs.getAlias());
            error.setData(shareConfig);
            res.body(new JsonTransformer().render(error));
        });

        exception(NoSuchAliasException.class, (exception, req, res) -> {
            res.status(404);
            res.type("application/json");
            Error error = new Error(exception.getMessage());
            error.setData(exception.getAlias());
            res.body(new JsonTransformer().render(error));
        });

        get("/ping", (req, res) -> {
            res.type("application/json");
            return "pong";
        }, new JsonTransformer());

        get("/status", (req, res) -> {
            res.type("application/json");
            return this.getStatus();
        }, new JsonTransformer());

        post("/attach", "application/json", (req, res) -> {
            ShareConfig shareConfig = JsonTransformer.gson.fromJson(req.body(), ShareConfig.class);
            this.attach(shareConfig);
            res.type("application/json");
            return shareConfig;
        }, new JsonTransformer());

        post("/detach", "application/json", (req, res) -> {
            ShareConfig shareConfig = JsonTransformer.gson.fromJson(req.body(), ShareConfig.class);
            this.detach(shareConfig);
            res.type("application/json");
            return shareConfig;
        }, new JsonTransformer());

        post("/stop", (req, res) -> {
            this.stop();
            return null;
        });
    }

    private Object getStatus() {
        return this.daemon.getStatus();
    }

    private void detach(ShareConfig share) throws AttachException {
        this.daemon.detach(share);
    }

    private void attach(ShareConfig share) throws AttachException {
        this.daemon.attach(share);
    }

    public void stop() {
        stop();
    }
}
