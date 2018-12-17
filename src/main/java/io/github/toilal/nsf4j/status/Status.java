package io.github.toilal.nsf4j.status;

import io.github.toilal.nsf4j.config.Config;

import java.util.List;

public class Status {
    private Config config;
    private List<StatusShare> shares;

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public List<StatusShare> getShares() {
        return shares;
    }

    public void setShares(List<StatusShare> shares) {
        this.shares = shares;
    }
}
