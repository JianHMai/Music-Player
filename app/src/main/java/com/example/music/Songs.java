package com.example.music;

public class Songs {
    private String title;
    private String artist;
    private String path;

    public String getPath(){
        return path;
    }
    public void setPath(String path){
        this.path=path;
    }
    public String getTitle(){
        return title;
    }
    public void setTitle(String title){
        this.title=title;
    }
    public String getArtist(){
        return artist;
    }
    public void setArtist(String artist){
        this.artist=artist;
    }
}