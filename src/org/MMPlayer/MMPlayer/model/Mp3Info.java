package org.MMPlayer.MMPlayer.model;

import java.io.Serializable;

public class Mp3Info implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int id = 0;
    private String mp3Path = null;
    private String mp3Name = null;
    private String mp3Size = null;
    private String lrcName = null;
    private String singer=null;
    private String album=null;
    private String title=null;
    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private Long mp3Duration;
    public Long getMp3Duration() {
        return mp3Duration;
    }

    public String getSinger() {
        return singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public void setMp3Duration(Long i) {
        this.mp3Duration = i;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMp3Path() {
        return mp3Path;
    }

    public void setMp3Path(String mp3Path) {
        this.mp3Path = mp3Path;
    }

    public String getMp3Name() {
        return mp3Name;
    }

    public void setMp3Name(String mp3Name) {
        this.mp3Name = mp3Name;
    }

    public String getMp3Size() {
        return mp3Size;
    }

    public void setMp3Size(String mp3Size) {
        this.mp3Size = mp3Size;
    }

    public String getLrcName() {
        return lrcName;
    }

    public void setLrcName(String lrcName) {
        this.lrcName = lrcName;
    }



    public Mp3Info(int id, String mp3Path, String mp3Name, String mp3Size,
                   String lrcName, String lrcSize, String singer, String album,
                   String title, Long mp3Duration) {
        super();
        this.id = id;
        this.mp3Path = mp3Path;
        this.mp3Name = mp3Name;
        this.mp3Size = mp3Size;
        this.lrcName = lrcName;
        this.singer = singer;
        this.album = album;
        this.title = title;
        this.mp3Duration = mp3Duration;
    }

    public Mp3Info() {
        super();
    }

    @Override
    public String toString() {
        return "Mp3Info [id=" + id + ", mp3Path=" + mp3Path + ",mp3Name="
                + mp3Name + ", mp3Size=" + mp3Size + ", lrcName=" + lrcName
                + ", singer=" +singer  +"album"+album +"]";
    }
}
