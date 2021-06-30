package com.example.news;

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

import com.example.commons.Utils;
import com.example.mediaplayer.MediaBean;
import com.example.mediaplayer.MediaPlayerHelper;
import com.example.mediaplayer.MediaPlayerImp;

public class NewsFragment extends Fragment implements View.OnClickListener {

    private static final String MP3_URL1 = "https://www.ytmp3.cn/down/32473.mp3";

    private static final String MP3_URL2 = "https://www.ytmp3.cn/down/32476.mp3";

    private View mView;

    private Button mPlayBtn1;

    private Button mPlayBtn2;

    private ImageView mStartIv;

    private ImageView mStopIv;

    private TextView mCurrentPosTv;

    private TextView mDurationTv;

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private String mPlayUrl;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_news_layout, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
        MediaPlayerManager.getInstance().registerObserver(mStateListener);
    }

    private void initView() {
        mPlayBtn1 = mView.findViewById(R.id.play_btn1);
        mPlayBtn2 = mView.findViewById(R.id.play_btn2);
        mStartIv = mView.findViewById(R.id.start_iv);
        mStopIv = mView.findViewById(R.id.stop_iv);
        mCurrentPosTv = mView.findViewById(R.id.current_pos_tv);
        mDurationTv = mView.findViewById(R.id.duration_tv);
        mPlayBtn1.setOnClickListener(this);
        mPlayBtn2.setOnClickListener(this);
        mStartIv.setOnClickListener(this);
        mStopIv.setOnClickListener(this);
    }

    private final MediaPlayerImp.MediaStateListener mStateListener = new MediaPlayerImp.MediaStateListener() {
        @Override
        public void onStateChanged(MediaBean bean) {
            if (bean == null) {
                return;
            }
            switch (bean.state) {
                case MediaPlayerImp.STATE_PREPARED:
                    mStartIv.setImageResource(R.mipmap.pause);
                    mDurationTv.setText("时长：" + Utils.secToTime(MediaPlayerHelper.getInstance(getContext()).getDuration() / 1000));
                    mTicker.run();
                    break;
                case MediaPlayerImp.STATE_PAUSE:
                    mStartIv.setImageResource(R.mipmap.start);
                    break;
                case MediaPlayerImp.STATE_COMPLETED:
                    mStartIv.setImageResource(R.mipmap.start);
                    mCurrentPosTv.setText("进度：" + Utils.secToTime(0));
                    mHandler.removeCallbacksAndMessages(null);
                    break;
                case MediaPlayerImp.STATE_ERROR:
                    mStartIv.setImageResource(R.mipmap.start);
                    Toast.makeText(getContext(), "资讯_播放出错", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    private final Runnable mTicker = new Runnable() {
        @Override
        public void run() {
            if (MediaPlayerHelper.getInstance(getContext()).isPlaying(mPlayUrl)) {
                mCurrentPosTv.setText("进度：" + Utils.secToTime(MediaPlayerHelper.getInstance(getContext()).getCurrentPosition() / 1000));
            }
            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);
            mHandler.postAtTime(mTicker, next);
        }
    };

    private void play(String url) {
        MediaPlayerManager.getInstance().play(getContext(), url);
        mPlayUrl = url;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.play_btn1) {
            play(MP3_URL1);
        } else if (v.getId() == R.id.play_btn2) {
            play(MP3_URL2);
        } else if (v.getId() == R.id.start_iv) {
            if (MediaPlayerHelper.getInstance(getContext()).isCompleted(mPlayUrl) || MediaPlayerHelper.getInstance(getContext()).isStop(mPlayUrl)) {
                play(mPlayUrl);
            } else if (MediaPlayerHelper.getInstance(getContext()).isPause(mPlayUrl)) {
                MediaPlayerManager.getInstance().start(getContext(), mPlayUrl);
                mStartIv.setImageResource(R.mipmap.pause);
            } else if (MediaPlayerHelper.getInstance(getContext()).isPlaying(mPlayUrl)) {
                MediaPlayerManager.getInstance().pause(getContext(), mPlayUrl);
                mStartIv.setImageResource(R.mipmap.start);
            }
        } else if (v.getId() == R.id.stop_iv) {
            MediaPlayerManager.getInstance().stop(getContext(), mPlayUrl);
            mStartIv.setImageResource(R.mipmap.start);
            mCurrentPosTv.setText("进度：" + Utils.secToTime(0));
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MediaPlayerManager.getInstance().unregisterObserver(mStateListener);
    }
}
