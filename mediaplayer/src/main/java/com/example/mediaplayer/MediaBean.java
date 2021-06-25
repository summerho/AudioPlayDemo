package com.example.mediaplayer;

public class MediaBean {

    public String url;

    public long pausePosition = 0;

    public int state = -1;

    public MediaPlayerImp.MediaStateListener stateListener;

    public MediaBean(String url) {
        this.url = url;
    }
}
