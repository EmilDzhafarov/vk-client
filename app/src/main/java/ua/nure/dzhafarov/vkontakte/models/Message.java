package ua.nure.dzhafarov.vkontakte.models;

import java.io.Serializable;

public class Message implements Serializable {
    
    private int id;
    private String body;
    private int userId;
    private int fromId;
    private long time;
    private boolean readed;
    
    public Message() {}

    @Override
    public String toString() {
        return "Message{" +
                "body='" + body + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getFromId() {
        return fromId;
    }

    public void setFromId(int fromId) {
        this.fromId = fromId;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isReaded() {
        return readed;
    }

    public void setReaded(boolean readed) {
        this.readed = readed;
    }
}
