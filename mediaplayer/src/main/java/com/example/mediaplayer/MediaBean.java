package com.example.mediaplayer;

public class MediaBean {

    public String url;

    public long pausePosition;

    public boolean isPause;

    public boolean isCompleted;

    public boolean isStop;

    public MediaPlayerImp.MediaStateListener stateListener;

    public MediaBean(String url) {
        this.url = url;
    }
}
