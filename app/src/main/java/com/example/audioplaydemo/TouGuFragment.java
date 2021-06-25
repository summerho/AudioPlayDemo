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
import com.example.mediaplayer.MediaPlayerHelper;
import com.example.mediaplayer.MediaPlayerImp;

public class TouGuFragment extends Fragment implements View.OnClickListener {

    private static final String MP3_URL = "https://www.ytmp3.cn/down/32474.mp3";

    private View mView;

    private Button mPlayBtn;

    private ImageView mStartIv;

    private ImageView mStopIv;

    private TextView mCurrentPosTv;

    private TextView mDurationTv;

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

    private final MediaPlayerImp.MediaStateListener mStateListener = new MediaPlayerImp.MediaStateListener() {
        @Override
        public void onStateChanged(int state) {
            switch (state) {
                case MediaPlayerImp.STATE_PREPARED:
                    mStartIv.setImageResource(R.mipmap.pause_new);
                    mDurationTv.setText("时长：" + Utils.secToTime(MediaPlayerHelper.getInstance(getContext()).getDuration() / 1000));
                    mTicker.run();
                    break;
                case MediaPlayerImp.STATE_PAUSE:
                case MediaPlayerImp.STATE_LOSS_AUDIO_FOCUS:
                    mStartIv.setImageResource(R.mipmap.start_new);
                    break;
                case MediaPlayerImp.STATE_COMPLETED:
                    mStartIv.setImageResource(R.mipmap.start_new);
                    mCurrentPosTv.setText("进度：" + Utils.secToTime(0));
                    mHandler.removeCallbacksAndMessages(null);
                    break;
                case MediaPlayerImp.STATE_ERROR:
                    mStartIv.setImageResource(R.mipmap.start_new);
                    Toast.makeText(getContext(), "资讯_播放出错", Toast.LENGTH_SHORT).show();
                    break;
                case MediaPlayerImp.STATE_GAIN_AUDIO_FOCUS:
                    mStartIv.setImageResource(R.mipmap.pause_new);
                    break;
                default:
                    break;
            }
        }
    };

    private final Runnable mTicker = new Runnable() {
        @Override
        public void run() {
            if (MediaPlayerHelper.getInstance(getContext()).isPlaying(MP3_URL)) {
                mCurrentPosTv.setText("进度：" + Utils.secToTime(MediaPlayerHelper.getInstance(getContext()).getCurrentPosition() / 1000));
            }
            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);
            mHandler.postAtTime(mTicker, next);
        }
    };

    private void play(String url) {
        MediaPlayerHelper.getInstance(getContext()).setMediaStateListener(mStateListener);
        MediaPlayerHelper.getInstance(getContext()).play(url);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.play_btn) {
            play(MP3_URL);
        } else if (v.getId() == R.id.start_iv) {
            if (!MediaPlayerHelper.getInstance(getContext()).isPlaying(MP3_URL) && MediaPlayerHelper.getInstance(getContext()).isCompleted(MP3_URL)
                    || MediaPlayerHelper.getInstance(getContext()).isStop(MP3_URL)) {
                play(MP3_URL);
            } else if (!MediaPlayerHelper.getInstance(getContext()).isPlaying(MP3_URL) && MediaPlayerHelper.getInstance(getContext()).isPause(MP3_URL)) {
                MediaPlayerHelper.getInstance(getContext()).start(MP3_URL);
                mStartIv.setImageResource(R.mipmap.pause_new);
            } else if (MediaPlayerHelper.getInstance(getContext()).isPlaying(MP3_URL)) {
                MediaPlayerHelper.getInstance(getContext()).pause(MP3_URL);
                mStartIv.setImageResource(R.mipmap.start_new);
            }
        } else if (v.getId() == R.id.stop_iv) {
            MediaPlayerHelper.getInstance(getContext()).stop(MP3_URL);
            mStartIv.setImageResource(R.mipmap.start_new);
            mCurrentPosTv.setText("进度：" + Utils.secToTime(0));
            mHandler.removeCallbacksAndMessages(null);
        }
    }
}
