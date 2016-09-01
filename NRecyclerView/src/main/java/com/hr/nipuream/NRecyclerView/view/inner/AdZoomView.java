package com.hr.nipuream.NRecyclerView.view.inner;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import com.hr.nipuream.NRecyclerView.view.base.InnerBaseView;

/**
 * 描述：
 * 作者：Nipuream
 * 时间: 2016-08-19 16:54
 * 邮箱：571829491@qq.com
 */
public class AdZoomView extends InnerBaseView {

    private float InitY = 0;
    private boolean isPull = false;

    public AdZoomView(Context context) {
        this(context,null);
    }

    public AdZoomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public AdZoomView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        final int action = ev.getAction();

        if(action == MotionEvent.ACTION_DOWN){
            InitY = ev.getY();
            Log.d("InitY",InitY+"");
        }else if(action == MotionEvent.ACTION_MOVE){
            float moveY = ev.getY() - InitY;
            isPull = moveY>0?true:false;
        }

        return super.dispatchTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {

        final int action = e.getAction();

        if(isFistItem && isPull)
            switch (action){
                case MotionEvent.ACTION_MOVE:

                    float moveY =  (e.getY() - InitY);
                    Log.d("moveY",moveY+"");

                    return true;
                default:
                    break;
            }


        isFistItem = false;
        return super.onTouchEvent(e);
    }


}
