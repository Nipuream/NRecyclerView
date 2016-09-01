package com.hr.nipuream.NRecyclerView.view.base;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 描述：
 * 作者：Nipuream
 * 时间: 2016-08-23 16:11
 * 邮箱：571829491@qq.com
 */
public class InnerBaseView extends RecyclerView{

    protected View adView;
    protected boolean isFistItem;

    public void setAdView(View view){
        this.adView = view;
    }

    public void setFistItem(boolean isFistItem){
        this.isFistItem = isFistItem;
        Log.d("isFirstItem",this.isFistItem+"");
    }

    public InnerBaseView(Context context) {
        super(context);
    }

    public InnerBaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public InnerBaseView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

}
