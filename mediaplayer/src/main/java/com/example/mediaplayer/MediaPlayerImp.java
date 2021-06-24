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

    public static final int STATE_PREPARED = 0;

    public static final int STATE_PAUSE = 1;

    public static final int STATE_COMPLETED = 2;

    public static final int STATE_ERROR = 3;

    public static final int STATE_GAIN_AUDIO_FOCUS = 4;

    public static final int STATE_LOSS_AUDIO_FOCUS = 5;

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

    public void reset() {
        if (isExoPLayer) {
            //no reset do nothing
        } else {
            ((MediaPlayer) mMediaPlayer).reset();
        }
    }
    /**
     * 是否正在播放
     */
    public boolean isPlaying() {
        if (isExoPLayer) {
            return ((ExoPlayer) mMediaPlayer).getPlayWhenReady();
        } else {
            return ((MediaPlayer) mMediaPlayer).isPlaying();
        }
    }

    /**
     * 是否处于暂停状态
     */
    public boolean isPause(String url) {
        MediaBean bean = mMediaMap.get(url);
        return bean != null && bean.isPause;
    }

    /**
     * 是否处于播放结束状态
     */
    public boolean isCompleted(String url) {
        MediaBean bean = mMediaMap.get(url);
        return bean != null && bean.isCompleted;
    }

    /**
     * 是否处于播放停止状态
     */
    public boolean isStop(String url) {
        MediaBean bean = mMediaMap.get(url);
        return bean != null && bean.isStop;
    }

    public MediaBean getMediaBean(String url) {
        return mMediaMap.get(url);
    }

    public void start() {
        if (!requestFocus()) {
            return;
        }
        if (isExoPLayer) {
            ((ExoPlayer) mMediaPlayer).setPlayWhenReady(true);
        } else {
            ((MediaPlayer) mMediaPlayer).start();
        }
    }

    public void pause() {
        if (isExoPLayer) {
            ((ExoPlayer) mMediaPlayer).setPlayWhenReady(false);
        } else {
            ((MediaPlayer) mMediaPlayer).pause();
        }
        updateMediaStatus(true, false, false);
    }

    public void stop() {
        if (isExoPLayer) {
            ((ExoPlayer) mMediaPlayer).setPlayWhenReady(false);
            ((ExoPlayer) mMediaPlayer).stop();
        } else {
            ((MediaPlayer) mMediaPlayer).stop();
        }
        updateMediaStatus(false, false, true);
    }

    public void release() {
        if (isExoPLayer) {
            ((ExoPlayer) mMediaPlayer).removeListener(mExoPlayerListener);
            ((ExoPlayer) mMediaPlayer).release();
        } else {
            ((MediaPlayer) mMediaPlayer).release();
        }
    }

    public void seekTo(long msec) {
        if (isExoPLayer) {
            ((ExoPlayer) mMediaPlayer).seekTo(msec);
        } else {
            ((MediaPlayer) mMediaPlayer).seekTo((int) msec);
        }
    }

    public void playAsync(String strUrl) {
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

    public long getDuration() {
        if (isExoPLayer) {
            return ((ExoPlayer) mMediaPlayer).getDuration();
        } else {
            return ((MediaPlayer) mMediaPlayer).getDuration();
        }
    }

    public long getCurrentPosition() {
        if (isExoPLayer) {
            return ((ExoPlayer) mMediaPlayer).getCurrentPosition();
        } else {
            return ((MediaPlayer) mMediaPlayer).getCurrentPosition();
        }
    }

    public void setMediaStateListener(@NonNull MediaStateListener listener) {
        mStateListener = listener;
    }

    /**
     * 设置下次继续播放的位置
     * @param position
     */
    public void setSeekToPosition(long position) {
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
                        updateMediaStatus(false, false, false);
                    } else if (playbackState == Player.STATE_ENDED) {
                        mStateListener.onStateChanged(STATE_COMPLETED);
                        ((ExoPlayer) mMediaPlayer).setPlayWhenReady(false);
                        updateMediaStatus(false, true, false);
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    mStateListener.onStateChanged(STATE_ERROR);
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
                updateMediaStatus(false, false, false);
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                mStateListener.onStateChanged(STATE_ERROR);
                return true;
            });
            mediaPlayer.setOnCompletionListener((mp) -> {
                mStateListener.onStateChanged(STATE_COMPLETED);
                updateMediaStatus(false, true, false);
            });
            mediaPlayer.setOnSeekCompleteListener((mp) -> {
            });
            return mediaPlayer;
        }
    }

    private void updateMediaStatus(boolean isPause, boolean isCompleted, boolean isStop) {
        updateMediaStatus(mPlayUrl, isPause, isCompleted, isStop);
    }

    public void updateMediaStatus(String url, boolean isPause, boolean isCompleted, boolean isStop) {
        MediaBean bean = mMediaMap.get(url);
        if (bean == null) {
            bean = new MediaBean(url);
            bean.stateListener = mStateListener;
        } else {
            bean.isPause = isPause;
            bean.isCompleted = isCompleted;
            bean.isStop = isStop;
            bean.pausePosition = getCurrentPosition();
        }
        mMediaMap.put(url, bean);
    }

}

