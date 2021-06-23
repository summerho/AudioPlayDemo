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

import static android.content.Context.AUDIO_SERVICE;

public class MediaPlayerImp {

    private AudioManager mAm;

    private MediaListener mListener;

    private AudioFocusChangeListener mUIStatusListener;

    private final Context mContext;

    private final boolean isExoPLayer;

    private final Object mMediaPlayer;

    private Player.EventListener mExoPlayerListener;

    private int mPlaybackState = -1;

    private long mSeekToPosition;

    public interface MediaListener {
        void onPrepared();

        void onPause();

        void onCompletion();

        void onSeekComplete();

        void onError();
    }

    public interface AudioFocusChangeListener {
        void onGainAudioFocus();

        void onLossAudioFocus();
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
            mUIStatusListener.onGainAudioFocus();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            pause();
            mUIStatusListener.onLossAudioFocus();
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
            pause();
            mUIStatusListener.onLossAudioFocus();
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

    public boolean isPlaying() {
        if (isExoPLayer) {
            return ((ExoPlayer) mMediaPlayer).getPlayWhenReady();
        } else {
            return ((MediaPlayer) mMediaPlayer).isPlaying();
        }
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
    }

    public void stop() {
        if (isExoPLayer) {
            ((ExoPlayer) mMediaPlayer).setPlayWhenReady(false);
            ((ExoPlayer) mMediaPlayer).stop();
        } else {
            ((MediaPlayer) mMediaPlayer).stop();
        }
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

    public void setMediaListener(@NonNull MediaListener listener) {
        this.mListener = listener;
    }

    public void setAudioFocusChangeListener(@NonNull AudioFocusChangeListener listener) {
        mUIStatusListener = listener;
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
                        mListener.onPrepared();
                    } else if (playbackState == Player.STATE_ENDED) {
                        mListener.onCompletion();
                        ((ExoPlayer) mMediaPlayer).setPlayWhenReady(false);
                    }
                }

                @Override
                public void onPlayerError(ExoPlaybackException error) {
                    mListener.onError();
                }

                @Override
                public void onSeekProcessed() {
                    mListener.onSeekComplete();
                }
            };
            exoPlayer.addListener(mExoPlayerListener);
            return exoPlayer;
        } else {
            MediaPlayer mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnPreparedListener((mp) -> {
                seekTo(mSeekToPosition);
                start();
                mListener.onPrepared();
                mSeekToPosition = 0;

            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                mListener.onError();
                return true;
            });
            mediaPlayer.setOnCompletionListener((mp) -> {
                mListener.onCompletion();
            });
            mediaPlayer.setOnSeekCompleteListener((mp) -> {
                mListener.onSeekComplete();
            });
            return mediaPlayer;
        }
    }

}
