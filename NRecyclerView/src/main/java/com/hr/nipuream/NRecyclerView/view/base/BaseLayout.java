package com.hr.nipuream.NRecyclerView.view.base;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.Scroller;
import com.hr.nipuream.NRecyclerView.R;
import com.hr.nipuream.NRecyclerView.view.util.Logger;
import java.math.BigDecimal;

/**
 * 描述：最外层的布局，支持嵌套滚动
 * 职责：刷新、加载
 * 作者：Nipuream
 * 时间: 2016-08-01 15:16
 * 邮箱：571829491@qq.com
 */
public abstract class BaseLayout extends LinearLayout implements NestedScrollingParent {

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

    protected int ITEM_DIVIDE_SIZE = 0;
    /**
     * 是否是最后一个Item
     */
    protected boolean IsLastItem = true;

    protected  boolean IsFirstItem = true;

    private boolean FirstLoadState = false;

    protected String innerView = "none";

    private NestedScrollingParentHelper mNestedScrollingParentHelper;


    protected int totalPages = -1;

    protected int currentPages = 1;

    public enum CONTENT_VIEW_STATE{
        NORMAL,
        PUSH,
        PULL
    }

    protected CONTENT_VIEW_STATE state = CONTENT_VIEW_STATE.NORMAL;
    protected LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);

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

        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        setOrientation(VERTICAL);
        ViewConfiguration config = ViewConfiguration.get(context);
        mTouchSlop = config.getScaledTouchSlop();
        DecelerateInterpolator interpolator = new DecelerateInterpolator();
        mScroller = new Scroller(context,interpolator);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.NRecyclerView);
        duration = array.getInteger(R.styleable.NRecyclerView_duration,500);

        setBackgroundColor(array.getColor(R.styleable.NRecyclerView_layout_color,backgroundColor));
        isPullRefreshEnable = array.getBoolean(R.styleable.NRecyclerView_pull_eable,true);
        isPullLoadEnable = array.getBoolean(R.styleable.NRecyclerView_push_able,true);
        overResistance = array.getFloat(R.styleable.NRecyclerView_over_resistance,0.4f);
        resistance = array.getFloat(R.styleable.NRecyclerView_push_resistance,0.6f);
        overScroll = array.getBoolean(R.styleable.NRecyclerView_over_scroll,true);
        innerView = array.getString(R.styleable.NRecyclerView_inner_view);

        if(TextUtils.isEmpty(innerView))
            innerView = "none";

        FirstLoadState = isPullLoadEnable;
        contentView = CreateEntryView(context,attrs,innerView);
        headerView = CreateRefreshView(context);
        footerView = CreateLoadView(context);

        /**
         * 线性布局，其中HeaderView和FooterView在屏幕
         * 的外面，如果添加顺序为 headerView,contentView,FooterView,
         * 那么，很可惜，你始终看不到FooterView，因为ContentView占据的
         * 位置是match_parent，解决的方案是 先添加FooterView,然后再
         * 添加ContentView,最后在onLayout()方法里面去设置下他们分别所在的
         * 区域位置
         * @See onLayout
         *
         * |---------------------------|<-----------
         * |                           |   HeaderView
         * |---------------------------|<-----------
         * |                           |
         * |                           |
         * |                           |
         * |                           |
         * |                           |
         * |                           |    屏幕(ContentView)
         * |                           |
         * |                           |
         * |                           |
         * |                           |
         * |                           |
         * |                           |
         * |-------------------------- |<------------
         * |                           |    FooterView
         * |---------------------------|<-------------
         */
        addView(headerView,layoutParams);
        addView(footerView,layoutParams);
        addView(contentView);

        if(refreshView != null)
            headerView.addView(refreshView,layoutParams);

        if(loaderView != null)
            footerView.addView(loaderView,layoutParams);
//        headerView.addView(refreshView,layoutParams);
//        footerView.addView(loaderView,layoutParams);

        contentView.setBackgroundColor(array.getColor(R.styleable.NRecyclerView_contentview_color,contentViewColor));
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

                if(!overScroll)
                    return super.onInterceptTouchEvent(ev);

                final float y = ev.getY(), x = ev.getX();
                final float diff, absDiff;
                diff = y - mLastMotionY;
                absDiff = Math.abs(diff);

                if(standView == null){

                    if(absDiff > mTouchSlop && diff > 1){
                        if(getLocalRectPosition(contentView.getChildAt(0)).top == 0 && IsFirstItem){
                            mIsBeingDragged = true;
                            mLastMotionY = y;
                            state = CONTENT_VIEW_STATE.PULL;
                        }
                    }

                    if(absDiff > mTouchSlop && diff < 0){
                        View lastView = contentView.getChildAt(contentView.getChildCount()-1);
                        int bottom = getLocalRectPosition(lastView).bottom;
                        int height = lastView.getHeight();

                        isFillContent();

                        if(bottom == height && IsLastItem ){
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

    private Rect rect = new Rect();

    public Rect getLocalRectPosition(View view){

        if(view != null)
        {
            view.getLocalVisibleRect(rect);
            return rect;
        }else
            rect = new Rect();

        return rect;
    }

    /**
     * 如果开始，contentView中的内容没有填满，
     * 则我们设置为 pullLoad 为 false.
     */
    private void isFillContent(){

        //第一个Child可能是广告位
        int count = contentView.getChildCount()-1;
        int totalHeight = contentView.getChildAt(contentView.getChildCount()-1)
                .getHeight() * count + contentView.getChildAt(0).getHeight();
        int contentHeight = contentView.getHeight();

        //添加 item decor 的高度
        totalHeight += ITEM_DIVIDE_SIZE * count;

        if(totalHeight <
                contentHeight)
            isPullLoadEnable = false;
    }


    protected abstract ViewGroup CreateRefreshView(Context context);

    protected abstract ViewGroup CreateEntryView(Context context,AttributeSet attrs,String innerView);

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

    public void setOverScrollEnable(boolean enable){
        overScroll = enable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN
                && event.getEdgeFlags() != 0)
            return false;

        if(isMove)  return false;

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
                                if((int) (upY * resistance) < refreshView.getHeight())
                                {
                                    //TODO GOBACK
                                    startMoveAnim(getScrollY(), Math.abs(getScrollY()), duration);
                                }else{
                                    //TODO 刷新
                                    startMoveAnim(getScrollY(),Math.abs(getScrollY()) -
                                            refreshView.getHeight(),duration);
                                    refreshView.setState(HeaderStateInterface.REFRESHING);
                                    isRefreshing = true;
                                    if(l != null)
                                    {
                                        setPullLoadEnable(FirstLoadState);
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
                                //TODO LOAD MORE...
                                if(loaderView !=null)
                                {
                                    int absUpy = (int) Math.abs(upY);
                                    if((absUpy*resistance) < loaderView.getHeight())
                                    {
                                        startMoveAnim(getScrollY(),-getScrollY() ,duration);
                                    }else{
                                        if(isPullLoadEnable){
                                            startMoveAnim(getScrollY(),-(Math.abs(getScrollY())-loaderView.getHeight()),duration);
                                            loaderView.setState(LoaderStateInterface.LOADING_MORE);
                                            isLoadingMore = true;
                                            currentPages++;
                                            if(currentPages == totalPages)
                                                setPullLoadEnable(false);
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
        if(isRefreshing()){
            startMoveAnim(getScrollY(), Math.abs(getScrollY()), duration);
            mIsBeingDragged = false;
            isRefreshing = false;
            //TODO 我们将会加载第一页数据
            IsLastItem = true;
            if(loaderView != null) loaderView.setState(LoaderStateInterface.IDLE);
            currentPages = 1;
            if(refreshView != null) refreshView.setState(HeaderStateInterface.IDLE);
        }
    }

    public void pullMoreEvent(){

        if(isPullLoadEnable)
        {
            if(loaderView != null)
            {
                loaderView.setState(LoaderStateInterface.LOADING_MORE);
//                scrollTo(0,loaderView.getHeight());

                /**
                 * 捕获到contentView滑动的速度，当滑动到底端的时候
                 * 根据速度来显示正在加载，这样显得更加圆滑
                 */
//                int duration = (int) (loaderView.getHeight()/velocityY);
                BigDecimal height = new BigDecimal(loaderView.getHeight());
                BigDecimal velocity = new BigDecimal(velocityY);

                BigDecimal time = height.divide(velocity,3,BigDecimal.ROUND_HALF_UP);
                double value = time.doubleValue() * 1000;
                int duration = (int) value;

                Logger.getLogger().i("duration = "+duration);
                startMoveAnim(0,loaderView.getHeight(),duration);

                isLoadingMore = true;
                IsLastItem = true;
                currentPages++;
                if(currentPages  == totalPages)
                    setPullLoadEnable(false);
                if(l != null) l.load();
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

    /**
     * 模仿 PullToRefresh
     * 但是实现方式和它并不同，这种方式更为简便
     */
    public void pullOverScroll(){
        try{

            Logger.getLogger().e("mIsBeingDragged = "+mIsBeingDragged +"/ isRefreshing = "+isRefreshing + "/ isPullRefreshEnable = "+isPullRefreshEnable + "/ velocityY = "+velocityY);

            if(!mIsBeingDragged && !isRefreshing && isPullRefreshEnable && velocityY > 0){

                int overScrollY = (int) (velocityY/100);

                if(overScrollY > refreshView.getHeight())
                    overScrollY = refreshView.getHeight();

                startMoveAnim(getScrollY(),-overScrollY,200);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollTo(0,0);
                        //暂时的解决策略，还没有想到更好的解决方案
                        if(loaderView.getState() != LoaderStateInterface.NO_MORE)
                            velocityY = 0;
                    }
                },250);
            }
        }catch (Exception e){
            Logger.getLogger().e(e.toString());
        }
    }


    public void endLoadingMore(){
        if(isLoadingMore()){
            if(loaderView != null)
            {
                scrollTo(0,0);
                //TODO 把contentView底下的子View 滚上来，看起来就没有违和感
                contentView.scrollBy(0,loaderView.getHeight());
                isLoadingMore = false;
                isNestConfilct = false;

                /**
                 * 设置 LoaderView 的 state的用意是
                 * 不再让BaseLayout 处理　nestScroll，否则在最底端 PUSH的时候会消耗事件
                 * @see 嵌套滑动条件
                 */
                if(currentPages == totalPages)
                    loaderView.setState(LoaderStateInterface.NO_MORE);
//                velocityY = 0;
            }
        }
    }

    public void pullEvent(float moveY){

        int value = (int) Math.abs(moveY);
        Logger.getLogger().d("state = "+state);

        if(moveY > 0){
            if(state == CONTENT_VIEW_STATE.PULL)
            {
                if(isPullRefreshEnable)
                {
                    if(refreshView != null){
                        refreshView.setVisibility(View.VISIBLE);
                        scrollTo(0, - (int)(value*resistance));
                        if((int) (moveY * resistance) >= refreshView.getHeight())
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
//                        loaderView.setVisibility(View.VISIBLE);
                        scrollTo(0, (int) (value*resistance));
                        if((int)(value*resistance)>=loaderView.getHeight())
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

    /**
     * 调整布局中各个控件的位置
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        if(refreshView != null){
            headerView.layout(0,-refreshView.getHeight(),getWidth(),0);
        }

        if(standView == null)
            contentView.layout(0,0,getWidth(),getHeight());
        else
            standView.layout(0,0,getWidth(),getHeight());

        if(loaderView != null)
            footerView.layout(0,getHeight(),getWidth(),getHeight()+ loaderView.getHeight());
    }



    //TODO =========================================== 嵌套滚动 ==================================================


    private boolean isNest = true;

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {

        //----------------------------------------------------------------------------
        /**
         * 设置 isNest的作用是 如果加载完了之后，就没有必要在嵌套滑动了
         * 因为每次这个方法会拦截事件，虽然拦截没有处理任何事情，返回给BaseLayout处理还是会出现小许的卡顿，
         * 不信可以亲试
         */
        if(loaderView != null)
            isNest = (loaderView.getState() ==
                    LoaderStateInterface.NO_MORE)?false:true;
        //--------------------------------------------------------------------------------

        /**
         * 1、垂直滚动才嵌套
         * 2、isNest 嵌套满足的条件
         * 3、为了防止用户PUSH loaderView的height显示没有一半会缩回去，此时再PUSH会导致事件都会被消费。
         *
         * 条件三 有个小bug 当用户PUSH LoaderView 一半的时候，缩回。然后再往上拉一段距离，再PUSH 此时
         * isNestConfilct 为true.在NRecyclerView的onScrollListener里面去改变这个状态
         * @See {@link NRecyclerView}
         *
         */
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 && isNest && !isNestConfilct;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onStopNestedScroll(View child) {

        Logger.getLogger().d("回调--->onStopNestedScroll----state = "+state);
        mNestedScrollingParentHelper.onStopNestedScroll(child);

        if(state == CONTENT_VIEW_STATE.PUSH){
            Logger.getLogger().d("处理PUSH 结果");
            handlePushNestStop();
        }
        nestMoveY = 0;
    }


    protected boolean isNestConfilct = false;

    private void handlePushNestStop(){
        if(standView == null){
            if(isPullLoadEnable){
                //TODO LOADMORE...
                if(loaderView !=null)
                {
                    int absUpy = Math.abs(nestMoveY);

                    if((absUpy*resistance) < loaderView.getHeight())
                    {
                        startMoveAnim(getScrollY(),-getScrollY(),duration);
                        isNestConfilct = true;
                    }else{
                        if(isPullLoadEnable){
                            startMoveAnim(getScrollY(),-(Math.abs(getScrollY())-loaderView.getHeight()),duration);
                            loaderView.setState(LoaderStateInterface.LOADING_MORE);
                            isLoadingMore = true;
                            currentPages++;
                            if(currentPages == totalPages)
                                setPullLoadEnable(false);
                            if(l !=null)  l.load();
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

        //TODO  把状态设置为NORMAL,因为onNestStop方法会调用多次
        state = CONTENT_VIEW_STATE.NORMAL;
        isNestLoad = false;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        //TODO 因为5.0以下的手机会报 LinearLayout 没有onNestedScroll方法
//        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
    }

    private int nestMoveY = 0;
    protected boolean isNestLoad = false;

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

        View lastView = contentView.getChildAt(contentView.getChildCount()-1);
        Rect rect = getLocalRectPosition(lastView);
        if(rect.bottom == lastView.getHeight() && IsLastItem){

            nestMoveY += dy;
            state = CONTENT_VIEW_STATE.PUSH;
            pullEvent(-nestMoveY);
            isNestLoad = true;

            //------------------------------------------------------------------
            /**
             * 当我们PUSH过程中，底部的FootView出现了，再往下PULL，
             * 那么contentView中的内容也会随之一起滚动，所以，我们这里要把事件消费掉。
             */
            if(nestMoveY > 0){
                consumed[0] = dx;
                consumed[1] = dy;
            }
            //---------------------------------------------------------------------
        }
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    protected float velocityY;

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {

        Logger.getLogger().d("fling velocityY = "+velocityY);
        this.velocityY = Math.abs(velocityY);

        //----------------------------------------------------------
        /**
         * 如果用户一直用手拖着，不放然后从中间位置迅猛放下，会导致无法加载现象和onNestStop一样
         */
        isNestLoad = false;
        //---------------------------------------------------------

        return false;
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

}
