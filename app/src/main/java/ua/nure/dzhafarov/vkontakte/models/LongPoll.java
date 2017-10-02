package ua.nure.dzhafarov.vkontakte.models;

import java.io.Serializable;

public class LongPoll implements Serializable {
    
    private String key;
    private String server;
    private long ts;

    public LongPoll(){}
    
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public long getTs() {
        return ts;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }
}
