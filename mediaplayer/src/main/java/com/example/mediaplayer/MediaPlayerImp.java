package com.example.mediaplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static android.content.Context.AUDIO_SERVICE;

public class MediaPlayerImp {

    /**
     * 就绪
     */
    public static final int STATE_PREPARED = 0;

    /**
     * 暂停
     */
    public static final int STATE_PAUSE = 1;

    /**
     * 结束
     */
    public static final int STATE_COMPLETED = 2;

    /**
     * 停止
     */
    public static final int STATE_STOP = 3;

    /**
     * 错误
     */
    public static final int STATE_ERROR = 4;

    /**
     * 获得音频焦点
     */
    public static final int STATE_GAIN_AUDIO_FOCUS = 5;

    /**
     * 失去音频焦点
     */
    public static final int STATE_LOSS_AUDIO_FOCUS = 6;

    private final AudioManager mAm;

    private MediaStateListener mStateListener;

    private final Context mContext;

    /**
     * 是否是谷歌开源的ExoPlayer播放器
     */
    private final boolean isExoPLayer;

    private final Object mMediaPlayer;

    private Player.EventListener mExoPlayerListener;

    private int mPlaybackState = -1;

    /**
     * 继续播放的位置
     */
    private long mSeekToPosition;

    /**
     * 当前播放的链接
     */
    private String mPlayUrl;

    private final Map<String, MediaBean> mMediaMap = new HashMap<>();

    public interface MediaStateListener {
        void onStateChanged(int state);
    }

    public MediaPlayerImp(Context context) {
        this(context, false);
    }

    public MediaPlayerImp(Context context, boolean isExoPLayer) {
        this.isExoPLayer = isExoPLayer;
        this.mContext = context;
        mMediaPlayer = createMediaPlayer(context);
        mAm = (AudioManager) context.getSystemService(AUDIO_SERVICE);
    }

    /**
     * 获得/失去音频焦点时的回调
     */
    private final AudioManager.OnAudioFocusChangeListener mAfChangeListener = focusChange -> {
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            start();
            mStateListener.onStateChanged(STATE_GAIN_AUDIO_FOCUS);
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            pause();
            mStateListener.onStateChanged(STATE_LOSS_AUDIO_FOCUS);
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            pause();
            mStateListener.onStateChanged(STATE_LOSS_AUDIO_FOCUS);
        }
    };

    /**
     * 获取音频焦点
     */
    private boolean requestFocus() {
        int result = mAm.requestAudioFocus(mAfChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    protected void reset() {
        if (isExoPLayer) {
            //no reset do nothing
        } else {
            ((MediaPlayer) mMediaPlayer).reset();
        }
    }
    /**
     * 是否正在播放
     */
    protected boolean isPlaying() {
        if (isExoPLayer) {
            return ((ExoPlayer) mMediaPlayer).getPlayWhenReady();
        } else {
            return ((MediaPlayer) mMediaPlayer).isPlaying();
        }
    }

    /**
     * 是否处于暂停状态
     */
    protected boolean isPause(String url) {
        MediaBean bean = mMediaMap.get(url);
        return bean != null && bean.state == STATE_PAUSE || bean != null && bean.state == STATE_ERROR && bean.pausePosition > 0;
    }

    /**
     * 是否处于播放结束状态
     */
    protected boolean isCompleted(String url) {
        MediaBean bean = mMediaMap.get(url);
        return bean != null && bean.state == STATE_COMPLETED;
    }

    /**
     * 是否处于播放停止状态
     */
    protected boolean isStop(String url) {
        MediaBean bean = mMediaMap.get(url);
        return bean != null && bean.state == STATE_STOP;
    }

    /**
     * 是否处于播放错误状态
     */
    protected boolean isError(String url) {
        MediaBean bean = mMediaMap.get(url);
        return bean != null && bean.state == STATE_ERROR;
    }

    protected MediaBean getMediaBean(String url) {
        return mMediaMap.get(url);
    }

    protected void start() {
        if (!requestFocus()) {
            return;
        }
        if (isExoPLayer) {
            ((ExoPlayer) mMediaPlayer).setPlayWhenReady(true);
        } else {
            ((MediaPlayer) mMediaPlayer).start();
        }
    }

    protected void pause() {
        if (isExoPLayer) {
            ((ExoPlayer) mMediaPlayer).setPlayWhenReady(false);
        } else {
            ((MediaPlayer) mMediaPlayer).pause();
        }
        updateMediaState(STATE_PAUSE);
    }

    protected void stop() {
        if (isExoPLayer) {
            ((ExoPlayer) mMediaPlayer).setPlayWhenReady(false);
            ((ExoPlayer) mMediaPlayer).stop();
        } else {
            ((MediaPlayer) mMediaPlayer).stop();
        }
        updateMediaState(STATE_STOP);
    }

    protected void release() {
        if (isExoPLayer) {
            ((ExoPlayer) mMediaPlayer).removeListener(mExoPlayerListener);
            ((ExoPlayer) mMediaPlayer).release();
        } else {
            ((MediaPlayer) mMediaPlayer).release();
        }
    }

    protected void seekTo(long msec) {
        if (isExoPLayer) {
            ((ExoPlayer) mMediaPlayer).seekTo(msec);
        } else {
            ((MediaPlayer) mMediaPlayer).seekTo((int) msec);
        }
    }

    protected void playAsync(String strUrl) {
        if (!requestFocus()) {
            return;
        }
        mPlayUrl = strUrl;
        if (isExoPLayer) {
            DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(mContext, "MediaPlayerImp");
            if (strUrl.endsWith("m3u8")) {
                MediaSource hlsMediaSource =
                        new HlsMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(strUrl));
                ((ExoPlayer) mMediaPlayer).prepare(hlsMediaSource, true, true);
            } else {
                MediaSource extraMediaSource =
                        new ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(Uri.parse(strUrl));
                ((ExoPlayer) mMediaPlayer).prepare(extraMediaSource);
            }
        } else {
            try {
                ((MediaPlayer) mMediaPlayer).reset();
                ((MediaPlayer) mMediaPlayer).setDataSource(strUrl);
                ((MediaPlayer) mMediaPlayer).prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected long getDuration() {
        if (isExoPLayer) {
            return ((ExoPlayer) mMediaPlayer).getDuration();
        } else {
            return ((MediaPlayer) mMediaPlayer).getDuration();
        }
    }

    protected long getCurrentPosition() {
        if (isExoPLayer) {
            return ((ExoPlayer) mMediaPlayer).getCurrentPosition();
        } else {
            return ((MediaPlayer) mMediaPlayer).getCurrentPosition();
        }
    }

    protected void setMediaStateListener(@NonNull MediaStateListener listener) {
        mStateListener = listener;
    }

    /**
     * 设置下次继续播放的位置
     * @param position
     */
    protected void setSeekToPosition(long position) {
        mSeekToPosition = position;
    }

    private Object createMediaPlayer(Context context) {
        if (isExoPLayer) {
            ExoPlayer exoPlayer = ExoPlayerFactory.newSimpleInstance(context, new DefaultRenderersFactory(context),
                    new DefaultTrackSelector(), new DefaultLoadControl());
            mExoPlayerListener = new Player.EventListener() {

                @Override
                public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                    if (mPlaybackState == playbackState) {
                        return;
                    }
                    mPlaybackState = playbackState;
                    if (playbackState == Player.STATE_READY) {
                        if (mSeekToPosition != 0) {
                            seekTo(mSeekToPosition);
                            mSeekToPosition = 0;
                        }
                        start();
                        mStateListener.onStateChanged(STATE_PREPARED);
                        updateMediaState(STATE_PREPARED);
                    } else if (playbackState == Player.STATE_ENDED) {
                        mStateListener.onStateChanged(STATE_COMPLETED);
                        ((ExoPlayer) mMediaPlayer).setPlayWhenReady(false);
                        updateMediaState(STATE_COMPLETED);
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    mStateListener.onStateChanged(STATE_ERROR);
                    updateMediaState(STATE_ERROR);
                }

                @Override
                public void onSeekProcessed() {
                }
            };
            exoPlayer.addListener(mExoPlayerListener);
            return exoPlayer;
        } else {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener((mp) -> {
                seekTo(mSeekToPosition);
                start();
                mStateListener.onStateChanged(STATE_PREPARED);
                mSeekToPosition = 0;
                updateMediaState(STATE_PREPARED);
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                mStateListener.onStateChanged(STATE_ERROR);
                updateMediaState(STATE_ERROR);
                return true;
            });
            mediaPlayer.setOnCompletionListener((mp) -> {
                mStateListener.onStateChanged(STATE_COMPLETED);
                updateMediaState(STATE_COMPLETED);
            });
            mediaPlayer.setOnSeekCompleteListener((mp) -> {
            });
            return mediaPlayer;
        }
    }
    private void updateMediaState(int state) {
        updateMediaState(mPlayUrl, state);
    }

    protected void updateMediaState(String url, int state) {
        MediaBean bean = mMediaMap.get(url);
        if (bean == null) {
            bean = new MediaBean(url);
            bean.state = state;
            bean.stateListener = mStateListener;
        } else {
            bean.state = state;
            if (state == STATE_PAUSE) {
                bean.pausePosition = getCurrentPosition();
            }
        }
        mMediaMap.put(url, bean);
    }
}

