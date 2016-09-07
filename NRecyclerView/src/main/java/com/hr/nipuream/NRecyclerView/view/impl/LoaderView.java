package com.hr.nipuream.NRecyclerView.view.impl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hr.nipuream.NRecyclerView.R;
import com.hr.nipuream.NRecyclerView.view.base.BaseLoaderView;

/**
 * 描述：
 * 作者：Nipuream
 * 时间: 2016-08-05 15:07
 * 邮箱：571829491@qq.com
 */
public class LoaderView extends BaseLoaderView {

    private View loaderView;
    private TextView stateTv;
    private ProgressBar progressBar;

    public LoaderView(Context context) {
        super(context);
//        initViews(context);
    }

    @Override
    public void setState(int state) {
        super.setState(state);
        switch (state){
            case IDLE:
                stateTv.setText(context.getString(R.string.pull_load_more));
                progressBar.setVisibility(GONE);
                break;
            case RELEASE_LOAD_MORE:
                stateTv.setText(context.getString(R.string.release_load_more));
                progressBar.setVisibility(GONE);
                break;
            case LOADING_MORE:
                stateTv.setText(context.getString(R.string.loading));
                progressBar.setVisibility(VISIBLE);
                break;
            case NO_MORE:
                stateTv.setText(context.getString(R.string.no_more));
                progressBar.setVisibility(GONE);
                break;
        }
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        loaderView = LayoutInflater.from(context).inflate(R.layout.loader_layout,null);
        stateTv = (TextView)loaderView.findViewById(R.id.loader_state);
        progressBar = (ProgressBar) loaderView.findViewById(R.id.loader_progressbar);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(loaderView,lp);
    }
}
