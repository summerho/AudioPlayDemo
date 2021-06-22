package com.example.audioplaydemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.audioplaydemo.util.Utils;

public class TouGuFragment extends Fragment implements View.OnClickListener {

    private static final String MP3_URL = "https://www.0dutv.com/upload/dance/20200316/C719452E3C7834080007662021EA968E.mp3";

    private View mView;

    private Button mPlayBtn;

    private ImageView mStartIv;

    private ImageView mStopIv;

    private TextView mCurrentPosTv;

    private TextView mDurationTv;

    private boolean isPause = false;

    private boolean isCompletion = false;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_tougu_layout, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        mPlayBtn = mView.findViewById(R.id.play_btn);
        mStartIv = mView.findViewById(R.id.start_iv);
        mStopIv = mView.findViewById(R.id.stop_iv);
        mCurrentPosTv = mView.findViewById(R.id.current_pos_tv);
        mDurationTv = mView.findViewById(R.id.duration_tv);
        mPlayBtn.setOnClickListener(this);
        mStartIv.setOnClickListener(this);
        mStopIv.setOnClickListener(this);
    }

    private final MediaPlayerImp.MediaListener mListener = new MediaPlayerImp.MediaListener() {
        @Override
        public void onPrepared() {
            mStartIv.setImageResource(R.mipmap.pause_new);
            isPause = false;
            Toast.makeText(getContext(), "投顾_开始播放", Toast.LENGTH_SHORT).show();
            mDurationTv.setText("时长：" + Utils.secToTime(MediaPlayerHelper.getInstance(getContext()).getDuration() / 1000));
            mTicker.run();
        }

        @Override
        public void onPause() {
            mStartIv.setImageResource(R.mipmap.start_new);
            isPause = true;
        }

        @Override
        public void onCompletion() {
            mStartIv.setImageResource(R.mipmap.start_new);
            isPause = true;
            isCompletion = true;
            Toast.makeText(getContext(), "投顾_播放完毕", Toast.LENGTH_SHORT).show();
            mCurrentPosTv.setText("进度：" + Utils.secToTime(0));
            mHandler.removeCallbacksAndMessages(null);
        }

        @Override
        public void onSeekComplete() {

        }

        @Override
        public void onError() {
            Toast.makeText(getContext(), "投顾_播放出错", Toast.LENGTH_SHORT).show();
        }
    };

    private final MediaPlayerImp.AudioFocusChangeListener mAudioFocusChangeListener = new MediaPlayerImp.AudioFocusChangeListener() {
        @Override
        public void onGainAudioFocus() {
            mStartIv.setImageResource(R.mipmap.pause_new);
            isPause = false;
        }

        @Override
        public void onLossAudioFocus() {
            mStartIv.setImageResource(R.mipmap.start_new);
            isPause = true;
        }
    };

    private final Runnable mTicker = new Runnable() {
        @Override
        public void run() {
            if (!isPause) {
                mCurrentPosTv.setText("进度：" + Utils.secToTime(MediaPlayerHelper.getInstance(getContext()).getCurrentPosition() / 1000));
            }
            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);
            mHandler.postAtTime(mTicker, next);
        }
    };

    private void play(String url) {
        MediaPlayerHelper.getInstance(getContext()).setMediaListener(mListener);
        MediaPlayerHelper.getInstance(getContext()).setAudioFocusChangeListener(mAudioFocusChangeListener);
        MediaPlayerHelper.getInstance(getContext()).play(url);
    }

    private void start(String url) {
        MediaPlayerHelper.getInstance(getContext()).setMediaListener(mListener);
        MediaPlayerHelper.getInstance(getContext()).setAudioFocusChangeListener(mAudioFocusChangeListener);
        MediaPlayerHelper.getInstance(getContext()).start(url);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.play_btn) {
            play(MP3_URL);
        } else if (v.getId() == R.id.start_iv) {
            if (!MediaPlayerHelper.getInstance(getContext()).isPlaying(MP3_URL) && isCompletion) {
                play(MP3_URL);
            } else if (!MediaPlayerHelper.getInstance(getContext()).isPlaying(MP3_URL) && isPause) {
                start(MP3_URL);
                mStartIv.setImageResource(R.mipmap.pause_new);
                isPause = false;
                isCompletion = false;
            } else if (MediaPlayerHelper.getInstance(getContext()).isPlaying(MP3_URL)) {
                MediaPlayerHelper.getInstance(getContext()).pause(MP3_URL);
                mStartIv.setImageResource(R.mipmap.start_new);
                isPause = true;
                isCompletion = false;
            }
        } else if (v.getId() == R.id.stop_iv) {
            MediaPlayerHelper.getInstance(getContext()).stop(MP3_URL);
            mStartIv.setImageResource(R.mipmap.start_new);
            isPause = false;
            isCompletion = true;
            mCurrentPosTv.setText("进度：" + Utils.secToTime(0));
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
