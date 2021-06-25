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

    public void setMediaStateListener(@NonNull MediaPlayerImp.MediaStateListener listener) {
        mMediaPlayerImp.setMediaStateListener(listener);
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
     * 是否处于暂停状态
     */
    public boolean isPause(String url) {
        return mMediaPlayerImp.isPause(url);
    }

    /**
     * 是否处于播放结束状态
     */
    public boolean isCompleted(String url) {
        return mMediaPlayerImp.isCompleted(url);
    }

    /**
     * 是否处于播放停止状态
     */
    public boolean isStop(String url) {
        return mMediaPlayerImp.isStop(url);
    }

    /**
     * 开始播放
     * @param url 音频链接
     */
    public void play(String url) {
        stop(mPlayUrl);
        // 更新被暂停音频页面的UI
        if (mMediaPlayerImp.getMediaBean(mPlayUrl) != null && mMediaPlayerImp.getMediaBean(mPlayUrl).stateListener != null) {
            mMediaPlayerImp.getMediaBean(mPlayUrl).stateListener.onStateChanged(MediaPlayerImp.STATE_PAUSE);
        }
        if (TextUtils.isEmpty(mPlayUrl) || !mPlayUrl.equals(url)) {
            // 记录被暂停音频的状态及播放进度，用于下次继续播放
            mMediaPlayerImp.updateMediaStatus(mPlayUrl, true, false, false);
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
            MediaPlayerImp.MediaStateListener stateListener = mMediaPlayerImp.getMediaBean(url).stateListener;
            mMediaPlayerImp.setMediaStateListener(stateListener);
            long pausePosition = mMediaPlayerImp.getMediaBean(url) == null ? 0 : mMediaPlayerImp.getMediaBean(url).pausePosition;
            mMediaPlayerImp.setSeekToPosition(pausePosition);
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
