package com.example.audioplaydemo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.audioplaydemo.widget.BottomTab;
import com.example.audioplaydemo.widget.FragmentTabHost;
import com.example.tougu.TouGuFragment;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private LayoutInflater mInflater;

    private final List<BottomTab> mBottomTabs = new ArrayList<>(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initTab();
    }

    // 初始化底部标签栏
    private void initTab() {
        BottomTab bottomTab_news = new BottomTab(NewsFragment.class, R.string.news_fragment,
                R.drawable.select_icon_news);
        BottomTab bottomTab_about = new BottomTab(TouGuFragment.class, R.string.tougu_fragment,
                R.drawable.select_icon_about);
        mBottomTabs.add(bottomTab_news);
        mBottomTabs.add(bottomTab_about);
        // 设置FragmentTab
        mInflater = LayoutInflater.from(this);
        FragmentTabHost mTabHost = findViewById(android.R.id.tabhost);
        mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        for (BottomTab bottomTab : mBottomTabs) {
            TabHost.TabSpec tabSpec = mTabHost.newTabSpec(getString(bottomTab.getTitle()));
            tabSpec.setIndicator(buildIndicator(bottomTab));
            mTabHost.addTab(tabSpec, bottomTab.getFragment(), null);
        }
        mTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

            }
        });
        mTabHost.getTabWidget().setShowDividers(LinearLayout.SHOW_DIVIDER_NONE);
        mTabHost.setCurrentTab(0);
    }

    // 设置底部tab的图片和文字
    private View buildIndicator(BottomTab bottomTab) {
        View view = mInflater.inflate(R.layout.tab_indicator, null);
        ImageView img = view.findViewById(R.id.icon_tab);
        TextView text = view.findViewById(R.id.txt_indicator);
        img.setBackgroundResource(bottomTab.getIcon());
        text.setText(bottomTab.getTitle());
        return view;
    }
}
