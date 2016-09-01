package com.hr.nipuream.NRecyclerView.view.base;

import android.content.Context;
import android.widget.LinearLayout;

/**
 * 描述：
 * 作者：Nipuream
 * 时间: 2016-08-08 14:48
 * 邮箱：571829491@qq.com
 */
public class BaseLoaderView extends LinearLayout implements LoaderStateInterface{

    protected Context context;

    protected int state = IDLE;

    public BaseLoaderView(Context context) {
        super(context);
        this.context = context;
        init(context);
    }

    protected void init(Context context){
    }

    @Override
    public void setState(int state) {
         this.state = state;
    }

    @Override
    public int getState() {
        return state;
    }


}
