package com.hr.nipuream.NRecyclerView.view.base;

import android.content.Context;
import android.widget.LinearLayout;

/**
 * 描述：
 * 作者：Nipuream
 * 时间: 2016-08-08 14:45
 * 邮箱：571829491@qq.com
 */
public class BaseRefreshView extends LinearLayout implements HeaderStateInterface{

    protected  Context context;
    protected  int state;

    public BaseRefreshView(Context context) {
        super(context);
        this.context = context;
        init(context);
    }

    protected void init(Context context){}


    @Override
    public void setState(int state) {
        this.state = state;
    }

    @Override
    public int getState() {
        return state;
    }

    @Override
    public void setPullDistance(int distance, int height) {

    }


}
