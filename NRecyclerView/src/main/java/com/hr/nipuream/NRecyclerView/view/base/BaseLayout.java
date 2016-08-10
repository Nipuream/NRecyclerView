package com.hr.nipuream.NRecyclerView.view.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;

import com.hr.nipuream.NRecyclerView.R;

/**
 * 描述：
 * 作者：Nipuream
 * 时间: 2016-08-01 15:16
 * 邮箱：571829491@qq.com
 */
public abstract class BaseLayout extends LinearLayout{

    private int mTouchSlop;
    protected boolean mIsBeingDragged = false;
    private float mLastMotionY;
    protected float  mInitialMotionY;

    /**
     * push、pull 阻尼系数
     */
    private float resistance = 0.6f;

    /**
     * OverScroll 效果阻尼系数
     */
    private float overResistance = 0.4f;
    private Scroller mScroller;
    //    private ListView mListView;
    private boolean isMove = false;

    /**
     * push、pull 回退时间
     */
    private int duration = 300;
    //    private ScrollRershListener l;
    protected boolean isRefreshing = false;
    protected boolean isLoadingMore = false;
    protected ViewGroup headerView;
    protected ViewGroup footerView;
    protected ViewGroup contentView;

    /**
     * refresh enable
     */
    protected boolean isPullRefreshEnable = true;
    /**
     * load more enable
     */
    protected boolean isPullLoadEnable = true;

    protected  int backgroundColor = Color.parseColor("#F0EFEF");
    protected  int contentViewColor = Color.WHITE;

    protected BaseRefreshView refreshView;
    protected BaseLoaderView loaderView;
    protected ViewGroup standView;

    private  boolean overScroll = true;

    public enum CONTENT_VIEW_STATE{
        NORMAL,
        PUSH,
        PULL
    }

    protected CONTENT_VIEW_STATE state = CONTENT_VIEW_STATE.NORMAL;

    public BaseLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        // TODO Auto-generated constructor stub
        init(context,attrs,0);
    }

    public BaseLayout(Context context) {
        super(context);
        init(context,null,0);
    }

    public BaseLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs,defStyleAttr);
    }

    private void init(final Context context, AttributeSet attrs,int defStyleAttr){

        setOrientation(VERTICAL);
        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();
        DecelerateInterpolator interpolator = new DecelerateInterpolator();
        mScroller = new Scroller(context,interpolator);

        contentView = CreateEntryView(context,attrs);
        headerView = CreateRefreshView(context);
        footerView = CreateLoadView(context);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(headerView,layoutParams);
        addView(footerView,layoutParams);
        addView(contentView);

        if(refreshView != null)
            headerView.addView(refreshView,layoutParams);

        if(loaderView != null)
            footerView.addView(loaderView,layoutParams);
//        headerView.addView(refreshView,layoutParams);
//        footerView.addView(loaderView,layoutParams);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.NRecyclerView);

        duration = array.getInteger(R.styleable.NRecyclerView_duration,500);
        contentView.setBackgroundColor(array.getColor(R.styleable.NRecyclerView_contentview_color,contentViewColor));
        setBackgroundColor(array.getColor(R.styleable.NRecyclerView_layout_color,backgroundColor));
        isPullRefreshEnable = array.getBoolean(R.styleable.NRecyclerView_pull_eable,true);
        isPullLoadEnable = array.getBoolean(R.styleable.NRecyclerView_push_able,true);
        overResistance = array.getFloat(R.styleable.NRecyclerView_over_resistance,0.4f);
        resistance = array.getFloat(R.styleable.NRecyclerView_push_resistance,0.6f);
        overScroll = array.getBoolean(R.styleable.NRecyclerView_over_scroll,true);

        array.recycle();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // TODO Auto-generated method stub
        final  int action = ev.getAction();

        if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP){
            mIsBeingDragged = false;
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && mIsBeingDragged) {
            return true;
        }

        switch(action){
            case MotionEvent.ACTION_DOWN:{
                mLastMotionY = mInitialMotionY = ev.getY();
                mIsBeingDragged = false;
                break;
            }
            case MotionEvent.ACTION_MOVE:{

                if(isRefreshing || isLoadingMore)
                    return true;

                final float y = ev.getY(), x = ev.getX();
                final float diff, absDiff;
                diff = y - mLastMotionY;
                absDiff = Math.abs(diff);

                if(standView == null){

                    if(absDiff > mTouchSlop && diff > 1){
                        if(getLocalRectPosition(contentView.getChildAt(0)).top == 0){
                            mIsBeingDragged = true;
                            mLastMotionY = y;
                            state = CONTENT_VIEW_STATE.PULL;
                        }
                    }

                    if(absDiff > mTouchSlop && diff < 0){
                        View lastView = contentView.getChildAt(contentView.getChildCount()-1);
                        int bottom = getLocalRectPosition(lastView).bottom;
                        int height = lastView.getHeight();


                        int count = contentView.getChildCount()-1;
                        int totalHeight = lastView.getHeight() * count + contentView.getChildAt(0).getHeight();
                        int contentHeight = contentView.getHeight();

                        if(totalHeight <
                                contentHeight)
                            isPullLoadEnable = false;


                        if(bottom == height){
                            mIsBeingDragged = true;
                            mLastMotionY = y;
                            state = CONTENT_VIEW_STATE.PUSH;
                        }

                    }

                }else{

                    if(absDiff > mTouchSlop && diff > 1)
                    {
                        mIsBeingDragged = true;
                        state = CONTENT_VIEW_STATE.PULL;
                    }

                    if(absDiff >mTouchSlop && diff <0){
                        mIsBeingDragged = true;
                        state = CONTENT_VIEW_STATE.PUSH;
                    }

                }
                break;
            }
        }
        return mIsBeingDragged;
    }

    public Rect getLocalRectPosition(View view){

        Rect rect = new Rect();

        if(view != null)
        {
            view.getLocalVisibleRect(rect);
            return rect;
        }

        return rect;
    }


    protected abstract ViewGroup CreateRefreshView(Context context);

    protected abstract ViewGroup CreateEntryView(Context context,AttributeSet attrs);

    protected abstract ViewGroup CreateLoadView(Context context);

    public boolean isRefreshing(){
        return isRefreshing;
    }

    public boolean isLoadingMore(){
        return isLoadingMore;
    }

    public void setPullRefreshEnable(boolean enable){
        isPullRefreshEnable = enable;
    }

    public void setPullLoadEnable(boolean enable){
        isPullLoadEnable = enable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN
                && event.getEdgeFlags() != 0)
            return false;

        if(isMove)
            return false;

        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:{
                mLastMotionY = mInitialMotionY = event.getY();
                return true;
            }
            case MotionEvent.ACTION_MOVE:{
                if (mIsBeingDragged) {
                    mLastMotionY = event.getY();
                    float moveY = mLastMotionY - mInitialMotionY;
                    pullEvent(moveY);
                    return true;
                }
                break;
            }
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:{

                float upY = event.getY() - mInitialMotionY;

                if(mIsBeingDragged)
                {
                    if(state == CONTENT_VIEW_STATE.PULL){
                        if(isPullRefreshEnable){

                            if(refreshView != null)
                            {
                                if((int) (upY * resistance) < (refreshView.getHeight()/2 * 3))
                                {
                                    //直接缩回去
                                    startMoveAnim(getScrollY(), Math.abs(getScrollY()), duration);
                                }else{
                                    //执行刷新
                                    startMoveAnim(getScrollY(),Math.abs(getScrollY()) -
                                            refreshView.getHeight(),duration);
                                    refreshView.setState(HeaderStateInterface.REFRESHING);
                                    isRefreshing = true;
                                    if(l != null)
                                    {
                                        l.refresh();
                                        if(loaderView != null)
                                            loaderView.setVisibility(View.VISIBLE);
                                    }
                                }
                            }

                        }else{
                            if(overScroll)
                                startMoveAnim(getScrollY(),Math.abs(getScrollY()),duration);
                        }
                        mIsBeingDragged = false;
                    }else if(state == CONTENT_VIEW_STATE.PUSH){
                        if(standView == null){
                            if(isPullLoadEnable){
                                //加载更多

                                if(loaderView !=null)
                                {
                                    int absUpy = (int) Math.abs(upY);

                                    if((absUpy*resistance) < (loaderView.getHeight()/2*3))
                                    {
                                        startMoveAnim(getScrollY(),-getScrollY() ,duration);
                                    }else{
                                        if(isPullLoadEnable){
                                            startMoveAnim(getScrollY(),-(Math.abs(getScrollY())-loaderView.getHeight()),duration);
                                            loaderView.setState(LoaderStateInterface.LOADING_MORE);
                                            isLoadingMore = true;
                                            if(l !=null)
                                                l.load();
                                        }
                                    }
                                }
                            }else{
                                if(overScroll)
                                    startMoveAnim(getScrollY(),-getScrollY() ,duration);
                            }
                        }else{
                            startMoveAnim(getScrollY(),-getScrollY() ,duration);
                        }
                        mIsBeingDragged = false;
                    }
                    return true;
                }
                break;
            }
        }
        return super.onTouchEvent(event);
    }


    public void endRefresh(){
        startMoveAnim(getScrollY(), Math.abs(getScrollY()), duration);
        mIsBeingDragged = false;
        isRefreshing = false;
    }

    public void pullMoreEvent(){
        if(isPullLoadEnable)
        {
            if(loaderView != null)
            {
                loaderView.setState(LoaderStateInterface.LOADING_MORE);
                scrollTo(0,loaderView.getHeight());
                isLoadingMore = true;
                if(l != null)
                    l.load();
            }
        }
    }


    public void pullNoMoreEvent(){
        loaderView.setState(LoaderStateInterface.NO_MORE);
        postDelayed(new Runnable() {
            @Override
            public void run() {
                if(loaderView != null)
                {
                    startMoveAnim(getScrollY(),-loaderView.getHeight(),duration);
                    isLoadingMore = false;
                    setPullLoadEnable(false);
                }
            }
        },500);
    }

    public void endLoadingMore(){
        if(loaderView != null)
        {
            scrollTo(0,0);
            contentView.scrollBy(0,loaderView.getHeight()+1);
            isLoadingMore = false;
        }
    }

    public void pullEvent(float moveY){
        int value = (int) Math.abs(moveY);
        if(moveY > 0){
            if(state == CONTENT_VIEW_STATE.PULL)
            {
                if(isPullRefreshEnable)
                {
                    if(refreshView != null){
                        refreshView.setVisibility(View.VISIBLE);
                        scrollTo(0, - (int)(value*resistance));
                        if((int) (moveY * resistance) >= (refreshView.getHeight()/2 * 3))
                            refreshView.setState(HeaderStateInterface.RELEASE_REFRESH);
                        else
                            refreshView.setState(HeaderStateInterface.IDLE);
                    }
                }else{
                    if(overScroll){
                        if(refreshView !=null)
                            refreshView.setVisibility(View.INVISIBLE);
                        scrollTo(0,-(int)(value*overResistance));
                    }
                }
            }
        }else if(moveY < 0){
            if(state == CONTENT_VIEW_STATE.PUSH){
                if(isPullLoadEnable)
                {
                    if(loaderView !=null){
                        loaderView.setVisibility(View.VISIBLE);
                        scrollTo(0, (int) (value*resistance));
                        if((int)(value*resistance)>=loaderView.getHeight()/2*3)
                            loaderView.setState(LoaderStateInterface.RELEASE_LOAD_MORE);
                        else
                            loaderView.setState(LoaderStateInterface.IDLE);
                    }
                }else{
                    if(overScroll){
                        if(loaderView !=null)
                            loaderView.setVisibility(View.INVISIBLE);
                        scrollTo(0,(int)(value*overResistance));
                    }
                }
            }
        }
    }


    public void startMoveAnim(int startY, int dy, int duration) {
        isMove = true;
        mScroller.startScroll(0, startY, 0, dy, duration);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
            isMove = true;
        } else {
            isMove = false;
        }
        super.computeScroll();
    }

    public interface RefreshAndLoadingListener{
        void refresh();
        void load();
    }

    private RefreshAndLoadingListener l;

    public void setOnRefreshAndLoadingListener(RefreshAndLoadingListener l){
        this.l = l;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if(refreshView != null){
            headerView.layout(0,-refreshView.getHeight(),getWidth(),0);
        }

        contentView.layout(0,0,getWidth(),getHeight());

        if(loaderView != null)
            footerView.layout(0,getHeight(),getWidth(),getHeight()+ loaderView.getHeight());
    }

}
