package ua.nure.dzhafarov.vkontakte.models;


import java.io.Serializable;

public class Photo implements Serializable {
    
    private int id;
    private String photoLowResolution;
    private String photoHighResolution;
    private int ownerId;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getPhotoLowResolution() {
        return photoLowResolution;
    }

    public void setPhotoLowResolution(String photoLowResolution) {
        this.photoLowResolution = photoLowResolution;
    }

    public String getPhotoHighResolution() {
        return photoHighResolution;
    }

    public void setPhotoHighResolution(String photoURL2560) {
        this.photoHighResolution = photoURL2560;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }
}

