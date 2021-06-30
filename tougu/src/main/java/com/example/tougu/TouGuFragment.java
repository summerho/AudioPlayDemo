package com.example.tougu;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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

public class TouGuFragment extends Fragment implements View.OnClickListener {

    private static final String MP3_URL = "https://www.ytmp3.cn/down/32474.mp3";

    private View mView;

    private Button mPlayBtn;

    private ImageView mStartIv;

    private ImageView mStopIv;

    private TextView mCurrentPosTv;

    private TextView mDurationTv;

    private EditText mEditText;

    private Button mSeekBtn;

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
        MediaPlayerManager.getInstance().registerObserver(mStateListener);
    }

    private void initView() {
        mPlayBtn = mView.findViewById(R.id.play_btn);
        mStartIv = mView.findViewById(R.id.start_iv);
        mStopIv = mView.findViewById(R.id.stop_iv);
        mCurrentPosTv = mView.findViewById(R.id.current_pos_tv);
        mDurationTv = mView.findViewById(R.id.duration_tv);
        mEditText = mView.findViewById(R.id.edit_tv);
        mSeekBtn = mView.findViewById(R.id.seek_btn);
        mPlayBtn.setOnClickListener(this);
        mStartIv.setOnClickListener(this);
        mStopIv.setOnClickListener(this);
        mSeekBtn.setOnClickListener(this);
    }

    private final MediaPlayerImp.MediaStateListener mStateListener = new MediaPlayerImp.MediaStateListener() {
        @Override
        public void onStateChanged(MediaBean bean) {
            switch (bean.state) {
                case MediaPlayerImp.STATE_PREPARED:
                    mStartIv.setImageResource(R.mipmap.pause_new);
                    mDurationTv.setText("时长：" + Utils.secToTime(MediaPlayerHelper.getInstance(getContext()).getDuration() / 1000));
                    mTicker.run();
                    break;
                case MediaPlayerImp.STATE_PAUSE:
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

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.play_btn) {
            MediaPlayerManager.getInstance().play(getContext(), MP3_URL);
        } else if (v.getId() == R.id.start_iv) {
            if (MediaPlayerHelper.getInstance(getContext()).isCompleted(MP3_URL) || MediaPlayerHelper.getInstance(getContext()).isStop(MP3_URL)) {
                MediaPlayerManager.getInstance().play(getContext(), MP3_URL);
            } else if (MediaPlayerHelper.getInstance(getContext()).isPause(MP3_URL)) {
                MediaPlayerManager.getInstance().start(getContext(), MP3_URL);
                mStartIv.setImageResource(R.mipmap.pause_new);
            } else if (MediaPlayerHelper.getInstance(getContext()).isPlaying(MP3_URL)) {
                MediaPlayerManager.getInstance().pause(getContext(), MP3_URL);
                mStartIv.setImageResource(R.mipmap.start_new);
            }
        } else if (v.getId() == R.id.stop_iv) {
            MediaPlayerManager.getInstance().stop(getContext(), MP3_URL);
            mStartIv.setImageResource(R.mipmap.start_new);
            mCurrentPosTv.setText("进度：" + Utils.secToTime(0));
            mHandler.removeCallbacksAndMessages(null);
        } else if (v.getId() == R.id.seek_btn) {
            if (TextUtils.isEmpty(mEditText.getText().toString())) {
                return;
            }
            MediaPlayerManager.getInstance().seekTo(getContext(), MP3_URL, Integer.parseInt(mEditText.getText().toString()) * 1000);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        MediaPlayerManager.getInstance().unregisterObserver(mStateListener);
    }
}
