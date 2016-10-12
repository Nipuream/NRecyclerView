package com.hr.nipuream.NRecyclerView.view.impl;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.hr.nipuream.NRecyclerView.R;
import com.hr.nipuream.NRecyclerView.view.base.BaseRefreshView;

/**
 * 描述：
 * 作者：Nipuream
 * 时间: 2016-08-02 16:28
 * 邮箱：571829491@qq.com
 */
public class RefreshView extends BaseRefreshView {

    private View headerView;
    private TextView headerTv;
    private ProgressBar progressBar;
    private ImageView refreshIv;
    private Animation mRotateUpAnim;
    private Animation mRotateDownAnim;
    private final int ROTATE_ANIM_DURATION = 180;
    private int currentState = IDLE;

    public RefreshView(Context context) {
        super(context);
    }

    @Override
    public void setState(int state) {
        super.setState(state);

        if(currentState == state)
            return;

        if (state == REFRESHING) {
            refreshIv.clearAnimation();
            refreshIv.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
        } else {	// 显示箭头图片
            refreshIv.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
        }

        switch (state){
            case IDLE:
                if(currentState == RELEASE_REFRESH){
                    refreshIv.startAnimation(mRotateDownAnim);
                }else if(currentState == REFRESHING){
                    refreshIv.clearAnimation();
                }
                headerTv.setText(context.getString(R.string.refresh_idle));
                break;
            case REFRESHING:
                headerTv.setText(context.getString(R.string.refreshing));
                break;
            case RELEASE_REFRESH:
                if(currentState != RELEASE_REFRESH){
                    refreshIv.clearAnimation();
                    refreshIv.startAnimation(mRotateUpAnim);
                    headerTv.setText(context.getString(R.string.release_refresh));
                }
                break;
        }
        currentState = state;
    }


    @Override
    public void init(Context context){
        headerView = LayoutInflater.from(context).inflate(R.layout.refresh_layout,null);
        headerTv = (TextView) headerView.findViewById(R.id.header_tv);
        progressBar = (ProgressBar) headerView.findViewById(R.id.refresh_view_progressbar);
        refreshIv = (ImageView)headerView.findViewById(R.id.refresh_view_iv);
        refreshIv.setImageResource(R.mipmap.tableview_pull_refresh_arrow_down);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.addView(headerView,lp);

        mRotateUpAnim = new RotateAnimation(0.0f, -180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateUpAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateUpAnim.setFillAfter(true);
        mRotateDownAnim = new RotateAnimation(-180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        mRotateDownAnim.setDuration(ROTATE_ANIM_DURATION);
        mRotateDownAnim.setFillAfter(true);
    }

}

