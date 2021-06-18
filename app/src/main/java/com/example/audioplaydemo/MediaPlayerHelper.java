package com.example.audioplaydemo;

import android.content.Context;

import androidx.annotation.NonNull;

public class MediaPlayerHelper {

    private volatile static MediaPlayerHelper mInstance;

    private final MediaPlayerImp mMediaPlayerImp;

    public static MediaPlayerHelper getInstance(Context context) {
        if (mInstance == null) {
            synchronized (MediaPlayerHelper.class) {
                if (mInstance == null) {
                    mInstance = new MediaPlayerHelper(context);
                }
            }
        }
        return mInstance;
    }

    private MediaPlayerHelper(Context context) {
        mMediaPlayerImp = new MediaPlayerImp(context, true);
    }

    /**
     * 设置状态监听
     */
    public void setMediaListener(@NonNull MediaPlayerImp.MediaListener listener) {
        mMediaPlayerImp.setMediaListener(listener);
    }

    /**
     * 设置得到或失去音频焦点时的监听
     */
    public void setAudioFocusChangeListener(@NonNull MediaPlayerImp.AudioFocusChangeListener listener) {
        mMediaPlayerImp.setAudioFocusChangeListener(listener);
    }

    /**
     * 重置
     */
    public void reset() {
        mMediaPlayerImp.reset();
    }

    /**
     * 是否正在播放
     */
    public boolean isPlaying() {
        return mMediaPlayerImp.isPlaying();
    }

    /**
     * 开始播放
     * @param url 音频链接
     */
    public void play(String url) {
//        reset();
        stop();
        mMediaPlayerImp.playAsync(url);
    }

    /**
     * 继续播放
     */
    public void start() {
        mMediaPlayerImp.start();
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (isPlaying()) {
            mMediaPlayerImp.pause();
        }
    }

    /**
     * 结束播放
     */
    public void stop() {
        mMediaPlayerImp.stop();
    }

    /**
     * 释放
     */
    public void release() {
        mMediaPlayerImp.release();
    }

    /**
     * 指定时间位置播放
     * @param msec 毫秒
     */
    public void seekTo(int msec) {
        mMediaPlayerImp.seekTo(msec);
    }

    /**
     * 获取总时长
     */
    public long getDuration() {
        return mMediaPlayerImp.getDuration();
    }

    /**
     * 获取当前播放位置
     */
    public long getCurrentPosition() {
        return mMediaPlayerImp.getCurrentPosition();
    }
}
