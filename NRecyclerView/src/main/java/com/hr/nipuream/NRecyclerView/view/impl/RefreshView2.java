package com.hr.nipuream.NRecyclerView.view.impl;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hr.nipuream.NRecyclerView.R;
import com.hr.nipuream.NRecyclerView.view.base.BaseRefreshView;
import com.hr.nipuream.NRecyclerView.view.util.Logger;

/**
 * 描述：
 * 作者：Nipuream
 * 时间: 2016-10-12 14:30
 * 邮箱：571829491@qq.com
 */
public class RefreshView2 extends BaseRefreshView{

    private View headerView;
    private MeiTuanRefreshFirstStepView firstIv;
    private MeiTuanRefreshSecondStepView secondIv;
    private MeiTuanRefreshThirdStepView threeIv;
    private AnimationDrawable secondAnim,threeAnim;
    private int currentState = IDLE;


    public RefreshView2(Context context) {
        super(context);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        headerView = LayoutInflater.from(context).inflate(R.layout.refresh1_layout,null);
        firstIv = (MeiTuanRefreshFirstStepView)headerView.findViewById(R.id.refresh_view_iv1);
        secondIv = (MeiTuanRefreshSecondStepView)headerView.findViewById(R.id.refresh_view_iv2);
        threeIv = (MeiTuanRefreshThirdStepView)headerView.findViewById(R.id.refresh_view_iv3);

        secondIv.setBackgroundResource(R.drawable.pull_to_refresh_second_anim);
        secondAnim = (AnimationDrawable) secondIv.getBackground();
        threeIv.setBackgroundResource(R.drawable.pull_to_refresh_third_anim);
        threeAnim = (AnimationDrawable) threeIv.getBackground();

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.addView(headerView,lp);
    }

    @Override
    public void setState(int state) {
        super.setState(state);
        switch (state){
            case IDLE:
                firstIv.setVisibility(VISIBLE);
                secondIv.setVisibility(GONE);
                threeIv.setVisibility(GONE);
                secondAnim.stop();
                threeAnim.stop();
                break;
            case RELEASE_REFRESH:
                firstIv.setVisibility(GONE);
                secondIv.setVisibility(VISIBLE);
                threeIv.setVisibility(GONE);
                secondAnim.start();
                threeAnim.stop();
                break;
            case REFRESHING:
                firstIv.setVisibility(GONE);
                secondIv.setVisibility(GONE);
                threeIv.setVisibility(VISIBLE);
                secondAnim.stop();
                threeAnim.start();
                break;
        }
        currentState = state;
    }

    @Override
    public void setPullDistance(int distance, int height) {
        super.setPullDistance(distance, height);

        if(currentState == IDLE){
            Logger.getLogger().e("distance = "+distance + "/ height = "+height);
            firstIv.setCurrentProgress((float)distance/(float)height);
        }

    }



}
