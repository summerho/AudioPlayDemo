package com.example.mediaplayer;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

public class MediaPlayerHelper {

    private volatile static MediaPlayerHelper mInstance;

    /**
     * 播放器具体实现类
     */
    private final MediaPlayerImp mMediaPlayerImp;

    /**
     * 当前播放音频的链接
     */
    private String mPlayUrl;

    /**
     * 当前音频监听器
     */
    private MediaPlayerImp.MediaListener mCurrentMediaListener;

    /**
     * 前一个音频监听器
     */
    private MediaPlayerImp.MediaListener mPreMediaListener;

    /**
     * 被暂停时的播放进度，用来从此位置继续播放
     */
    private long mPreMediaPlayPosition;

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
        mMediaPlayerImp = new MediaPlayerImp(context);
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
        if (mPreMediaListener != null) {
            mPreMediaListener.onPause(); // 更新上一个音频页面的UI
        }
        if (TextUtils.isEmpty(mPlayUrl) || !mPlayUrl.equals(url)) {
            mPreMediaPlayPosition = getCurrentPosition(); // 记录上一个音频被暂停时的播放进度
        }
        mMediaPlayerImp.playAsync(url);
        mPlayUrl = url;
    }

    /**
     * 继续播放
     */
    public void start(String url) {
        if (!TextUtils.isEmpty(mPlayUrl) && mPlayUrl.equals(url)) { // 同一个音频，继续播放
            mMediaPlayerImp.start();
        } else { // 不同音频，继续播放
            mMediaPlayerImp.setSeekToPosition(mPreMediaPlayPosition);
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
    public void seekTo(long msec) {
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
