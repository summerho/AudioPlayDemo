package com.example.mediaplayer;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

public class MediaPlayerHelper {

    private volatile static MediaPlayerHelper mInstance;

    private final MediaPlayerImp mMediaPlayerImp;

    private String mPlayUrl;

    private MediaPlayerImp.MediaListener mCurrentMediaListener;

    private MediaPlayerImp.MediaListener mPreMediaListener;

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
        if (listener != mCurrentMediaListener) {
            mPreMediaListener = mCurrentMediaListener;
        }
        mCurrentMediaListener = listener;
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
    public boolean isPlaying(String url) {
        return !TextUtils.isEmpty(mPlayUrl) && mPlayUrl.equals(url) && mMediaPlayerImp.isPlaying();
    }

    /**
     * 开始播放
     * @param url 音频链接
     */
    public void play(String url) {
        stop(mPlayUrl);
        mMediaPlayerImp.playAsync(url);
        mPlayUrl = url;
        if (mPreMediaListener != null) {
            mPreMediaListener.onPause();
        }
    }

    /**
     * 继续播放
     */
    public void start(String url) {
        if (!TextUtils.isEmpty(mPlayUrl) && mPlayUrl.equals(url)) {
            mMediaPlayerImp.start();
        } else {
            play(url);
        }
    }

    /**
     * 暂停播放
     */
    public void pause(String url) {
        if (isPlaying(url)) {
            mMediaPlayerImp.pause();
        }
    }

    /**
     * 结束播放
     */
    public void stop(String url) {
        if (isPlaying(url)) {
            mMediaPlayerImp.stop();
        }
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
