package ua.nure.dzhafarov.vkontakte.models;

import java.io.Serializable;

public class Community implements Serializable {
    
    private int id;
    private String name;
    private String type;
    private String photoUrl;
    
    public Community() {}

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}
