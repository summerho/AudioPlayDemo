package com.example.audioplaydemo;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.audioplaydemo.util.Utils;

public class NewsFragment extends Fragment implements View.OnClickListener {

    private static final String MP3_URL = "https://www.ytmp3.cn/down/32476.mp3";

    private View mView;

    private ImageView mPlayIv;

    private ImageView mStartIv;

    private ImageView mStopIv;

    private TextView mCurrentPosTv;

    private TextView mDurationTv;

    private boolean isPause = false;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_news_layout, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        mPlayIv = mView.findViewById(R.id.play_iv);
        mStartIv = mView.findViewById(R.id.start_iv);
        mStopIv = mView.findViewById(R.id.stop_iv);
        mCurrentPosTv = mView.findViewById(R.id.current_pos_tv);
        mDurationTv = mView.findViewById(R.id.duration_tv);
        mPlayIv.setOnClickListener(this);
        mStartIv.setOnClickListener(this);
        mStopIv.setOnClickListener(this);
//        MediaPlayerHelper.getInstance(getContext()).setMediaListener(mListener);
//        MediaPlayerHelper.getInstance(getContext()).setAudioFocusChangeListener(mAudioFocusChangeListener);
    }

    private final MediaPlayerImp.MediaListener mListener = new MediaPlayerImp.MediaListener() {
        @Override
        public void onPrepared() {
            Toast.makeText(getContext(), "资讯_开始播放", Toast.LENGTH_SHORT).show();
            mDurationTv.setText("时长：" + Utils.secToTime(MediaPlayerHelper.getInstance(getContext()).getDuration() / 1000));
            mTicker.run();
        }

        @Override
        public void onCompletion() {
            mStartIv.setImageResource(R.mipmap.start);
            isPause = false;
            Toast.makeText(getContext(), "资讯_播放完毕", Toast.LENGTH_SHORT).show();
            mCurrentPosTv.setText("进度：" + Utils.secToTime(0));
            mHandler.removeCallbacksAndMessages(null);
        }

        @Override
        public void onSeekComplete() {

        }

        @Override
        public void onError() {
            Toast.makeText(getContext(), "资讯_播放出错", Toast.LENGTH_SHORT).show();
        }
    };

    private final MediaPlayerImp.AudioFocusChangeListener mAudioFocusChangeListener = new MediaPlayerImp.AudioFocusChangeListener() {
        @Override
        public void onStart() {
            mStartIv.setImageResource(R.mipmap.pause);
            isPause = false;
        }

        @Override
        public void onPause() {
            mStartIv.setImageResource(R.mipmap.start);
            isPause = true;
        }
    };

    private final Runnable mTicker = new Runnable() {
        @Override
        public void run() {
            mCurrentPosTv.setText("进度：" + Utils.secToTime(MediaPlayerHelper.getInstance(getContext()).getCurrentPosition() / 1000));
            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);
            mHandler.postAtTime(mTicker, next);
        }
    };

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_iv) {
            if (!MediaPlayerHelper.getInstance(getContext()).isPlaying() && !isPause) {
                MediaPlayerHelper.getInstance(getContext()).setMediaListener(mListener);
                MediaPlayerHelper.getInstance(getContext()).setAudioFocusChangeListener(mAudioFocusChangeListener);
                MediaPlayerHelper.getInstance(getContext()).play(MP3_URL);
                mStartIv.setImageResource(R.mipmap.pause);
                isPause = false;
            } else if (!MediaPlayerHelper.getInstance(getContext()).isPlaying() && isPause) {
                MediaPlayerHelper.getInstance(getContext()).start();
                mStartIv.setImageResource(R.mipmap.pause);
                isPause = false;
            } else if (MediaPlayerHelper.getInstance(getContext()).isPlaying()) {
                MediaPlayerHelper.getInstance(getContext()).pause();
                mStartIv.setImageResource(R.mipmap.start);
                isPause = true;
            }
        } else if (v.getId() == R.id.stop_iv) {
            MediaPlayerHelper.getInstance(getContext()).stop();
            mStartIv.setImageResource(R.mipmap.start);
            isPause = false;
            mCurrentPosTv.setText("进度：" + Utils.secToTime(0));
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
