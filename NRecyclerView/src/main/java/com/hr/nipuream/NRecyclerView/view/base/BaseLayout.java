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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
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
 *
 * ===================================================================================================================
 * linear layout, where HeaderView and FooterView are on the screen
 * outside, if the order is headerView, contentView, FooterView,
 * then, it's a pity that you can't always see the FooterView, because the ContentView is occupied.
 * location is match_parent, the solution is to add the FooterView, and then again
 * add ContentView, and finally in the onLayout () method to set up where they are located
 * area location
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
 * |                           |   Screen(ContentView)
 * |                           |
 * |                           |
 * |                           |
 * |                           |
 * |                           |
 * |                           |
 * |-------------------------- |<------------
 * |                           |    FooterView
 * |---------------------------|<-------------
 *
 * =========================================================================================================================
 */
public abstract class BaseLayout extends LinearLayout
        implements NestedScrollingParent{

    private int mTouchSlop;
    protected boolean mIsBeingDragged = false;
    private float mLastMotionY;
    protected float  mInitialMotionY;

    /**
     * push、pull Damping coefficient.
     */
    private float resistance = 0.6f;
    /**
     * OverScroll Effect damping coefficient
     */
    private float overResistance = 0.4f;
    private Scroller mScroller;


    /**
     * push、pull backoff time
     */
    private int duration = 200;

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
     * Is it the last Item
     */
    protected boolean IsLastItem = true;

    protected  boolean IsFirstItem = true;

    private boolean FirstLoadState = false;

    protected String innerView = "NONE";

    private NestedScrollingParentHelper mNestedScrollingParentHelper;

    protected int totalPages = -1;
    protected int currentPages = 1;

    /**
     * To refresh or load, whether to allow the ContentView to scroll
     */
    protected boolean LoadDataScrollEnable = true;

    /**
     * BaseLayout state (this state indicates the state of the ContentView is off the screen).
     */
    public enum CONTENT_VIEW_STATE{

        //normal state
        NORMAL,

        //Corresponding to the up pull process
        PUSH,

        //Corresponding to the drop-down process
        PULL
    }

    protected CONTENT_VIEW_STATE state = CONTENT_VIEW_STATE.NORMAL;

    /**
     * ContentView Rolling direction, provided by ContentView
     * The contentView must provide.
     */
    public enum CONTENT_VIEW_SCROLL_ORIENTATION {
        UP,
        DOWN,
        IDLE
    }

    protected CONTENT_VIEW_SCROLL_ORIENTATION orientation =
            CONTENT_VIEW_SCROLL_ORIENTATION.IDLE;

    protected LinearLayout.LayoutParams layoutParams =
            new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);

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

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.NRecyclerView);
        duration = array.getInteger(R.styleable.NRecyclerView_duration,200);

        setBackgroundColor(array.getColor(R.styleable.NRecyclerView_layout_color,backgroundColor));
        isPullRefreshEnable = array.getBoolean(R.styleable.NRecyclerView_pull_eable,true);
        isPullLoadEnable = array.getBoolean(R.styleable.NRecyclerView_push_able,true);
        overResistance = array.getFloat(R.styleable.NRecyclerView_over_resistance,0.4f);
        resistance = array.getFloat(R.styleable.NRecyclerView_push_resistance,0.6f);
        overScroll = array.getBoolean(R.styleable.NRecyclerView_over_scroll,true);
        innerView = array.getString(R.styleable.NRecyclerView_inner_view);
        LoadDataScrollEnable = array.getBoolean(R.styleable.NRecyclerView_loaddata_scrolleable,false);
        int interpolatorId = array.getResourceId(R.styleable.NRecyclerView_interpolator, android.R.anim.accelerate_decelerate_interpolator);

        Interpolator interpolator = null;

        switch (interpolatorId){
            case android.R.anim.accelerate_decelerate_interpolator:
                interpolator = new AccelerateDecelerateInterpolator();
                break;
            case android.R.anim.accelerate_interpolator:
                interpolator = new AccelerateInterpolator();
                break;
            case android.R.anim.anticipate_interpolator:
                interpolator = new AnticipateInterpolator();
                break;
            case android.R.anim.anticipate_overshoot_interpolator:
                interpolator = new AnticipateOvershootInterpolator();
                break;
            case android.R.anim.bounce_interpolator:
                interpolator = new BounceInterpolator();
                break;
            case android.R.anim.decelerate_interpolator:
                interpolator = new DecelerateInterpolator();
                break;
            case android.R.anim.linear_interpolator:
                interpolator = new LinearInterpolator();
                break;
            case android.R.anim.overshoot_interpolator:
                interpolator = new OvershootInterpolator();
                break;
            default:
                interpolator = new AccelerateDecelerateInterpolator();
                break;
        }

        mScroller = new Scroller(context,interpolator);

        if(TextUtils.isEmpty(innerView)) innerView = "NONE";
        FirstLoadState = isPullLoadEnable;
        contentView = CreateEntryView(context,attrs,innerView);
        headerView = CreateRefreshView(context);
        footerView = CreateLoadView(context);
        addView(headerView,layoutParams);
        addView(footerView,layoutParams);
        addView(contentView);

        if(refreshView != null)
            headerView.addView(refreshView,layoutParams);

        if(loaderView != null)
            footerView.addView(loaderView,layoutParams);

        contentView.setBackgroundColor(array.getColor(R.styleable.NRecyclerView_contentview_color,contentViewColor));
        array.recycle();
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //TODO Auto-generated method stub
        final  int action = ev.getAction();

        if(action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP){
            mIsBeingDragged = false;
            return false;
        }

        if (action != MotionEvent.ACTION_DOWN && mIsBeingDragged) {
            return true;
        }

        if(!mScroller.isFinished() && isRefreshing)
            return true;

        switch(action){

            case MotionEvent.ACTION_DOWN:{
                mLastMotionY = mInitialMotionY = ev.getY();
                mIsBeingDragged = false;
                break;
            }

            case MotionEvent.ACTION_MOVE:{

                if(!LoadDataScrollEnable)
                    if(isRefreshing || isLoadingMore)
                        return true;

                if(!overScroll)
                    return super.onInterceptTouchEvent(ev);

                final float y = ev.getY(), x = ev.getX();
                final float diff, absDiff;
                diff = y - mLastMotionY;
                absDiff = Math.abs(diff);

                if(standView == null){

                    Logger.getLogger().e("absDiff = "+absDiff + "/ mTouchSlop = "+mTouchSlop + "/ diff = "+diff);
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
                        if(bottom == height && IsLastItem){
                            mIsBeingDragged = true;
                            mLastMotionY = y;
                            state = CONTENT_VIEW_STATE.PUSH;
                        }
                        if(currentPages == totalPages && bottom == height){
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

        Logger.getLogger().w("Whether to intercept----->"+mIsBeingDragged);
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
     * if you start, the content of contentView is not filled,
     * then we set it to false. for pullLoad
     */
    private void isFillContent(){
        //The first Child is likely to be an ad bit
        int count = contentView.getChildCount()-1;
        int totalHeight = contentView.getChildAt(contentView.getChildCount()-1)
                .getHeight() * count + contentView.getChildAt(0).getHeight();
        int contentHeight = contentView.getHeight();

        //Add to item decor height
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
        Logger.getLogger().w("SET REFRESHABLE ---->"+enable);
        isPullRefreshEnable = enable;
    }

    public void setPullLoadEnable(boolean enable){
        Logger.getLogger().w("SET LOADABLE ---->"+enable);
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
        if(!mScroller.isFinished())  return false;
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
                                    //go back
                                    startMoveAnim(getScrollY(), Math.abs(getScrollY()), duration);
                                }else{
                                    if(!isRefreshing){
                                        if(isPullRefreshEnable){
                                            //refresh
                                            startMoveAnim(getScrollY(),Math.abs(getScrollY()) -
                                                    refreshView.getHeight(),duration);
                                            refreshView.setState(HeaderStateInterface.REFRESHING);
                                            isRefreshing = true;
                                            if(l != null)
                                            {
                                                setPullLoadEnable(FirstLoadState);
                                                velocityY = 0;
                                                l.refresh();
                                                setPullLoadEnable(false);
                                                if(loaderView != null)
                                                    loaderView.setVisibility(VISIBLE);
                                            }
                                        }
                                    }else{
                                        startMoveAnim(getScrollY(),Math.abs(getScrollY()) -
                                                refreshView.getHeight(),duration);
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
                            if(isPullLoadEnable && !isRefreshing){
                                //TODO LOAD MORE...
                                if(loaderView !=null)
                                {
                                    int absUpy = (int) Math.abs(upY);
                                    if((absUpy*resistance) < loaderView.getHeight())
                                    {
                                        startMoveAnim(getScrollY(),-getScrollY() ,duration);
                                    }else{
                                        if(!isLoadingMore){
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
                                        }else{
//                                            startMoveAnim(getScrollY(),- (Math.abs(getScrollY()) - loaderView.getHeight()),duration);
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

            // We will load the first page data
            IsLastItem = true;

            if(loaderView != null){
                loaderView.setState(LoaderStateInterface.IDLE);
            }
            setPullLoadEnable(true);
            currentPages = 1;
            if(refreshView != null) refreshView.setState(HeaderStateInterface.IDLE);

            velocityY = -1;
            scrollToFirstItemPosition();
        }
    }

    /**
     * Scroll to first item position that every view has different way.
     */
    protected abstract void scrollToFirstItemPosition();

    public void pullMoreEvent(){
        try{
            if(isPullLoadEnable)
            {
                if(loaderView != null)
                {

                    loaderView.setVisibility(VISIBLE);
                    loaderView.setState(LoaderStateInterface.LOADING_MORE);

                    /**
                     * capture the contentView sliding speed, when the slide to the end of the time
                     * display is loaded according to the speed, so it appears to be more smooth
                     */
                    BigDecimal height = new BigDecimal(loaderView.getHeight());
                    BigDecimal velocity = new BigDecimal(velocityY);

                    BigDecimal time = height.divide(velocity,3,BigDecimal.ROUND_HALF_UP);
                    double value = time.doubleValue() * 1000;
                    int duration = (int) value;

                    Logger.getLogger().i("LoaderView sliding time ---->"+duration);
                    startMoveAnim(0,loaderView.getHeight(),duration);

                    isLoadingMore = true;
                    IsLastItem = true;
                    currentPages++;
//                    if(currentPages  == totalPages)
//                        setPullLoadEnable(false);
                    if(l != null) l.load();
                }
            }
        }catch (Exception e){
            Logger.getLogger().e(e.toString());
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
     * copy PullToRefresh
     * but the way to achieve it and it is different, this way is more convenient
     */
    public void pullOverScroll(){
        try{


            if(!mIsBeingDragged && !isRefreshing && isPullRefreshEnable && velocityY > 0){

                int overScrollY = (int) (velocityY/100);

                if(overScrollY > refreshView.getHeight())
                    overScrollY = refreshView.getHeight();

                startMoveAnim(getScrollY(),-overScrollY,200);
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scrollTo(0,0);
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

                Rect rect = getLocalRectPosition(loaderView);
                int theaValue = rect.bottom - rect.top;
                Logger.getLogger().e("getScrollY = "+getScrollY());
                if(getScrollY() > 0){
                    //The contentView under the View roll up, it looks no sense of violation
                    contentView.scrollBy(0,theaValue);
                }

                isLoadingMore = false;
                isNestConfilct = false;

                scrollTo(0,0);
                /**
                 * set the state LoaderView is intended to be
                 * no longer let BaseLayout handle nestScroll, otherwise it will consume the event at the bottom of the PUSH
                 * @see nested sliding condition
                 */
                if(currentPages == totalPages){
                    setPullLoadEnable(false);
                    loaderView.setState(LoaderStateInterface.NO_MORE);
                }
            }
        }
    }

    public void pullEventWhileLoadData(int dy){
        if(dy < 0){
            scrollTo(0,-refreshView.getHeight());
        }else{
            scrollTo(0,loaderView.getHeight());
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
                    if(!isRefreshing){
                        if(refreshView != null){

                            refreshView.setVisibility(View.VISIBLE);
                            scrollTo(0, -(int)(value*resistance));
                            refreshView.setPullDistance((int)(value*resistance),refreshView.getHeight());

                            if((int) (moveY * resistance) >= refreshView.getHeight())
                                refreshView.setState(HeaderStateInterface.RELEASE_REFRESH);
                            else
                                refreshView.setState(HeaderStateInterface.IDLE);
                        }
                    }
                    else{
                        //When LoadDataScrollEnable is true, and then pull down the ContentView when  come here
                        scrollTo(0,- (int)(refreshView.getHeight() + value*resistance));
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
                    if(!isLoadingMore){
                        if(loaderView !=null){

                            scrollTo(0, (int) (value*resistance));
                            if((int)(value*resistance)>=loaderView.getHeight())
                                loaderView.setState(LoaderStateInterface.RELEASE_LOAD_MORE);
                            else
                                loaderView.setState(LoaderStateInterface.IDLE);
                        }
                    }else{
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
        mScroller.startScroll(0, startY, 0, dy, duration);
        invalidate();
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
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
     * Adjust the location of each control in the layout
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

    //TODO =========================================== NestedScrollingParent ==================================================

    private boolean isNest = true;

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        /**
         * the function of setting the isNest is if the load is over, there is no need to slide in the nested
         * because every time this method will intercept events, although the interception did not deal with any thing,
         * return to the BaseLayout or there will be a small carton,
         */
        if(loaderView != null)
            isNest = (loaderView.getState() ==
                    LoaderStateInterface.NO_MORE)?false:true;

        /**
         * 1, vertical scrolling is nested.
         * 2、Nested conditional.
         * 3、In order to prevent the user loaderView height PUSH shows.
         * that there is no half will shrink back, then PUSH will cause the event will be consumed.
         * 4、Don't nest scroll when overScroll is true.
         *
         * three conditions of a small bug when the user PUSH LoaderView half, retracted. Then pull a distance, and then PUSH at this time
         * true. for NRecyclerView in the onScrollListener isNestConfilct inside to change this state
         * @See {@link NRecyclerView}
         */
        boolean isAllowNest =  (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0 &&
                ( (isNest || isRefreshing) || orientation == CONTENT_VIEW_SCROLL_ORIENTATION.UP)
                && !isNestConfilct && !(!isPullRefreshEnable && !isPullLoadEnable);

        Logger.getLogger().d("isNest = "+isNest + "/ orientation = "+orientation + "/ isNestConfilct = "+isNestConfilct);
        Logger.getLogger().w("Whether to allow nested sliding--->"+(isAllowNest? "yes":"no"));

        return isAllowNest;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
    }

    @Override
    public void onStopNestedScroll(View child) {

        Logger.getLogger().d("Callback--->onStopNestedScroll----state = "+state);
        mNestedScrollingParentHelper.onStopNestedScroll(child);

        if(state == CONTENT_VIEW_STATE.PUSH && !isRefreshing && !isLoadingMore){
            Logger.getLogger().d("Processing PUSH results");
            handlePushNestStop();
        }

        nestMoveY = 0;
        if(isHandleRefreshingScroll)
        {
            /**
             * when the load is being loaded, push up the process, if the RefreshView is still in the
             * the top of the screen, then you don't have to.
             */
            if(getLocalRectPosition(refreshView).bottom <= 0 ){
                scrollTo(0,0);
                isHandleRefreshingScroll = false;
            }
        }

        if(isHandleRefreshingWhilePull){
            if(Math.abs(getScrollY()) > refreshView.getHeight()){
                startMoveAnim(getScrollY(),Math.abs(getScrollY()+refreshView.getHeight()),duration);
            }
            else{
                //If you drag the height of RefreshView is not high, it will not deal with
            }
            isHandleRefreshingWhilePull = false;
        }

        if(isHandleLoadingWhilePush){
            if(getScrollY() > loaderView.getHeight()){
                startMoveAnim(getScrollY(),-(getScrollY() - loaderView.getHeight()),duration);
            }
            else{
                //nothing to do.
            }
            isHandleLoadingWhilePush = false;
        }
        isFlingConfilcHandle = false;
    }


    protected boolean isNestConfilct = false;

    private void handlePushNestStop(){
        if(standView == null){
            if(isPullLoadEnable){
                //load more.
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

        //Set the status to NORMAL, because the onNestStop method will call a number of times
        state = CONTENT_VIEW_STATE.NORMAL;
        isNestLoad = false;
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
    }

    private int nestMoveY = 0;
    protected boolean isNestLoad = false;


    /**
     * handling when loading, the user quickly slides
     * cause HeaderView or LoaderView not to move up
     */
    private boolean isFlingConfilcHandle = false;

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {

        View lastView = contentView.getChildAt(contentView.getChildCount()-1);
        Rect rect = getLocalRectPosition(lastView);
        if(rect.bottom == lastView.getHeight() && IsLastItem && !isRefreshing && !isLoadingMore){

            loaderView.setVisibility(VISIBLE);
            nestMoveY += dy;
            state = CONTENT_VIEW_STATE.PUSH;
            pullEvent(-nestMoveY);
            isNestLoad = true;

            /**
             * when we PUSH process, the bottom of the FootView appears, and then down PULL,
             * then the content of contentView will also be rolling along with it, so we have to spend the event here.
             */
            if(nestMoveY > 0){
                consumed[0] = dx;
                consumed[1] = dy;
            }

        }

        if(isRefreshing && dy > 0 ){
            if(getScrollY() < 0 ){
                int scrollValue = 0;
                if(Math.abs(getScrollY()) - dy > 0){
                    scrollValue = -(Math.abs(getScrollY())-dy);
                    consumed[0] = dx;
                    consumed[1] = dy;
                }
                Logger.getLogger().e("scrollValue = "+scrollValue);
                scrollTo(0,scrollValue);
                isFlingConfilcHandle = true;
            }
            isHandleRefreshingScroll = true;
        }

        if(isRefreshing && dy < 0 &&
                getLocalRectPosition(contentView.getChildAt(0)).top == 0 && IsFirstItem){
            if(Math.abs(getScrollY()) < refreshView.getHeight()){
                scrollTo(0,getScrollY() + dy);
                isFlingConfilcHandle = true;
            }
            else{
                isFlingConfilcHandle = true;
            }
            isHandleRefreshingWhilePull = true;
        }

        if(isLoadingMore && dy < 0){
            if(getScrollY() > 0){
                int scrollValue = 0;
                if(getScrollY() - Math.abs(dy) > 0){
                    scrollValue = getScrollY() - Math.abs(dy);
                    consumed[0] = dx;
                    consumed[1] = dy;
                }
                scrollTo(0,scrollValue);
                isFlingConfilcHandle = true;
            }
        }

        if(isLoadingMore && dy > 0 && getLocalRectPosition(lastView).bottom == lastView.getHeight()){
            if(Math.abs(getScrollY()) < loaderView.getHeight()){
                scrollTo(0,getScrollY() + dy);
                isFlingConfilcHandle = true;
            }
            else{
                isFlingConfilcHandle = true;
            }
            isHandleLoadingWhilePush = true;
        }
    }

    private boolean isHandleRefreshingScroll = false;
    private boolean isHandleRefreshingWhilePull = false;
    private boolean isHandleLoadingWhilePush = false;

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Logger.getLogger().e("consumed = "+consumed);
        return false;
    }

    protected float velocityY;

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {

        Logger.getLogger().d("fling velocityY = "+velocityY);
        this.velocityY = Math.abs(velocityY);

        /**
         * If the user has been dragging with hands,
         * and then quickly put down from the middle position,
         * will lead to the phenomenon can not be loaded and onNestStop
         */
        isNestLoad = false;
        if(isFlingConfilcHandle){
            /**
             * Here imitation Sina processing way.
             */
            return true;
        }
        return false;

    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

}
