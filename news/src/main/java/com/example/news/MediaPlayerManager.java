package com.example.news;

import android.content.Context;

import com.example.mediaplayer.MediaBean;
import com.example.mediaplayer.MediaPlayerHelper;
import com.example.mediaplayer.MediaPlayerImp;

import java.util.ArrayList;
import java.util.List;

public class MediaPlayerManager {

    private volatile static MediaPlayerManager mInstance;

    public static MediaPlayerManager getInstance() {
        if (mInstance == null) {
            synchronized (MediaPlayerManager.class) {
                if (mInstance == null) {
                    mInstance = new MediaPlayerManager();
                }
            }
        }
        return mInstance;
    }

    List<MediaPlayerImp.MediaStateListener> mMediaStateListeners = new ArrayList<>();

    public void registerObserver(MediaPlayerImp.MediaStateListener listener) {
        if (!mMediaStateListeners.contains(listener)) {
            mMediaStateListeners.add(listener);
        }
    }

    public void unregisterObserver(MediaPlayerImp.MediaStateListener listener) {
        mMediaStateListeners.remove(listener);
    }

    private void notifyStateChanged(MediaBean bean) {
        for (MediaPlayerImp.MediaStateListener mediaStateListener : mMediaStateListeners) {
            mediaStateListener.onStateChanged(bean);
        }
    }

    private final MediaPlayerImp.MediaStateListener mStateListener = new MediaPlayerImp.MediaStateListener() {
        @Override
        public void onStateChanged(MediaBean bean) {
            notifyStateChanged(bean);
        }
    };

    public void play(Context context, String url) {
        MediaPlayerHelper.getInstance(context).setMediaStateListener(mStateListener);
        MediaPlayerHelper.getInstance(context).play(url);
    }

    public void start(Context context, String url) {
        MediaPlayerHelper.getInstance(context).setMediaStateListener(mStateListener);
        MediaPlayerHelper.getInstance(context).start(url);
    }

    public void seekTo(Context context, String url, long mesc) {
        MediaPlayerHelper.getInstance(context).setMediaStateListener(mStateListener);
        MediaPlayerHelper.getInstance(context).seekTo(url, mesc);
    }

    public void pause(Context context, String url) {
        MediaPlayerHelper.getInstance(context).pause(url);
    }

    public void stop(Context context, String url) {
        MediaPlayerHelper.getInstance(context).stop(url);
    }
}
