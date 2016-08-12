package com.hr.nipuream.NRecyclerView.view;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.OnScrollListener;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.hr.nipuream.NRecyclerView.view.base.BaseLayout;
import com.hr.nipuream.NRecyclerView.view.base.BaseLoaderView;
import com.hr.nipuream.NRecyclerView.view.base.BaseRefreshView;
import com.hr.nipuream.NRecyclerView.view.base.HeaderStateInterface;
import com.hr.nipuream.NRecyclerView.view.base.LoaderStateInterface;
import com.hr.nipuream.NRecyclerView.view.impl.LoaderView;
import com.hr.nipuream.NRecyclerView.view.impl.RefreshView;

/**
 * 描述：
 * 作者：Nipuream
 * 时间: 2016-08-01 16:42
 * 邮箱：571829491@qq.com
 */
public class NRecyclerView extends BaseLayout {

    private RecyclerView.LayoutManager layoutManager;
    private InnerAdapter adapter;
    private ViewGroup headerView;
    private final RecyclerView.AdapterDataObserver mDataObserver = new DataObserver();

    public NRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setHeaderView(ViewGroup headerView){
        this.headerView = headerView;
    }

    private View AdtureView;

    public void setAdtureView(View view){
        this.AdtureView = view;
    }

    @Override
    protected ViewGroup CreateRefreshView(Context context) {
//        refreshView = LayoutInflater.from(context).
//                inflate(R.layout.refresh_layout,null);
        LinearLayout headerView = new LinearLayout(context);
        headerView.setOrientation(VERTICAL);
        refreshView = new RefreshView(context);
        refreshView.setState(HeaderStateInterface.IDLE);
        return headerView;
    }

    @Override
    protected ViewGroup CreateEntryView(final Context context, AttributeSet attrs) {
        contentView = new RecyclerView(context,attrs);
        ((RecyclerView)contentView).addOnScrollListener(new OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }

            //todo we should load more when recyclerView scroll to bottom.
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                int lastVisiblePos = getLastVisibleItem();
                if( (newState == RecyclerView.SCROLL_STATE_IDLE) &&
                        lastVisiblePos +1 == adapter.getItemCount()){

                    //已经滑动到最底端
                    if(!isLoadingMore && isPullLoadEnable){
                        View lastView = contentView.getChildAt(contentView.getChildCount()-1);
                        Rect rect = getLocalRectPosition(lastView);
                        if(lastView.getHeight() == rect.bottom){
                            pullMoreEvent();
                        }
                    }
                }
            }
        });

        return contentView;
    }


    private int getLastVisibleItem(){

        int lastVisiblePos = 0;
        if(layoutManager != null)
        {
            if(layoutManager instanceof LinearLayoutManager)
                lastVisiblePos = ((LinearLayoutManager) layoutManager).findLastVisibleItemPosition();
            else if(layoutManager instanceof GridLayoutManager)
                ((GridLayoutManager) layoutManager).findLastVisibleItemPosition();
            else if(layoutManager instanceof StaggeredGridLayoutManager){
                int[] lastVisiblePositions = ((StaggeredGridLayoutManager) layoutManager).findLastVisibleItemPositions(new int[((StaggeredGridLayoutManager) layoutManager).getSpanCount()]);
                lastVisiblePos = getMaxElem(lastVisiblePositions);
            }
        }

        return lastVisiblePos;
    }


    private int getMaxElem(int[] arr) {
        int size = arr.length;
        int maxVal = Integer.MIN_VALUE;
        for (int i = 0; i < size; i++) {
            if (arr[i]>maxVal)
                maxVal = arr[i];
        }
        return maxVal;
    }

    public void setEntryView(ViewGroup viewGroup){
        try{
            removeViewInLayout(contentView);
            standView = viewGroup;
            addView(standView,2);
            if(footerView != null)
                footerView.setVisibility(View.INVISIBLE);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void resetEntryView(){
        try{
            if(standView != null){
                removeViewInLayout(standView);
                addView(contentView,2);
                ((RecyclerView)contentView).scrollToPosition(0);
                standView = null;
                if(footerView != null)
                    footerView.setVisibility(View.VISIBLE);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    public void addItemDecoration(RecyclerView.ItemDecoration decor,int size){
        if(contentView != null)
        {
            ((RecyclerView)contentView).addItemDecoration(decor);
            ITEM_DIVIDE_SIZE = size;
        }
        else
            throw new IllegalStateException("You hasn't add contentView in baseLayout");
    }

    public void addItemDecoration(RecyclerView.ItemDecoration decor,int index,int size){
        if(contentView != null)
        {
            ((RecyclerView)contentView).addItemDecoration(decor,index);
            ITEM_DIVIDE_SIZE = size;
        }
        else
            throw new IllegalStateException("You hasn't add contentView in baseLayout");
    }

    public void setItemAnimator(RecyclerView.ItemAnimator animator){
        if(contentView != null)
            ((RecyclerView)contentView).setItemAnimator(animator);
        else
            throw new IllegalStateException("You hasn't add contentView in baseLayout");
    }



    @Override
    protected ViewGroup CreateLoadView(Context context) {
        LinearLayout footerView = new LinearLayout(context);
        footerView.setOrientation(VERTICAL);
        loaderView = new LoaderView(context);
        loaderView.setState(LoaderStateInterface.LOADING_MORE);
        return footerView;
    }


    public void setLayoutManager(RecyclerView.LayoutManager layout){
        this.layoutManager = layout;
        ((RecyclerView)contentView).setLayoutManager(layout);
    }

    public void setAdapter(RecyclerView.Adapter adapter){
        this.adapter = new InnerAdapter(adapter);
        ((RecyclerView)contentView).setAdapter(this.adapter);
        adapter.registerAdapterDataObserver(mDataObserver);
        mDataObserver.onChanged();
    }

    public void setRefreshView(BaseRefreshView view){
        if(headerView != null){
            if(refreshView != null)
                headerView.removeViewInLayout(refreshView);
            refreshView = view;
            headerView.addView(refreshView,0);
            ViewGroup.LayoutParams lp = refreshView.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            refreshView.setLayoutParams(lp);
            requestLayout();
        }
    }

    public void setLoaderView(BaseLoaderView view){
        if(footerView !=null){
            if(loaderView != null)
                footerView.removeViewInLayout(loaderView);
            loaderView = view;
            footerView.addView(loaderView,0);
            ViewGroup.LayoutParams lp = loaderView.getLayoutParams();
            lp.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lp.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            loaderView.setLayoutParams(lp);
            requestLayout();
        }
    }


    private class DataObserver extends RecyclerView.AdapterDataObserver {

        @Override
        public void onChanged() {
            if (adapter != null) {
                adapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            adapter.notifyItemRangeInserted(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            adapter.notifyItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            adapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            adapter.notifyItemRangeRemoved(positionStart, itemCount);
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            adapter.notifyItemMoved(fromPosition, toPosition);
        }
    };


    private class InnerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

        private RecyclerView.Adapter adapter;

        private static final int TYPE_ADVENTRUE = 14;
        private static final int TYPE_NORMAL = 15;

        public InnerAdapter(RecyclerView.Adapter adapter){
            this.adapter = adapter;
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
            if(AdtureView !=null){
                RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
                if (manager instanceof GridLayoutManager) {
                    final GridLayoutManager gridManager = ((GridLayoutManager)
                            manager);
                    gridManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                        @Override
                        public int getSpanSize(int position) {
                            return (position == 0)
                                    ? gridManager.getSpanCount() : 1;
                        }
                    });
                }
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            if(AdtureView !=null){
                ViewGroup.LayoutParams lp = holder.itemView.getLayoutParams();
                if (lp != null
                        && lp instanceof StaggeredGridLayoutManager.LayoutParams
                        && (holder.getLayoutPosition()==0)) {
                    StaggeredGridLayoutManager.LayoutParams p = (StaggeredGridLayoutManager.LayoutParams) lp;
                    p.setFullSpan(true);
                }
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if(viewType == TYPE_ADVENTRUE){
                return new InnerViewHolder(AdtureView);
            }
            return adapter.onCreateViewHolder(parent,viewType);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if(AdtureView != null){
                if(position == 0)
                    return;
                int adjPosition = position - 1;
                int adapterCount;
                if (adapter != null) {
                    adapterCount = adapter.getItemCount();
                    if (adjPosition < adapterCount) {
                        adapter.onBindViewHolder(holder, adjPosition);
                        return;
                    }
                }
            }else{
                adapter.onBindViewHolder(holder,position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            if(AdtureView != null)
            {
                if(position == 0)
                    return TYPE_ADVENTRUE;
                return TYPE_NORMAL;
            }
            else
                return TYPE_NORMAL;
        }

        @Override
        public int getItemCount() {
            return AdtureView == null ? adapter.getItemCount():adapter.getItemCount()+1;
        }

        private class InnerViewHolder extends RecyclerView.ViewHolder{
            public InnerViewHolder(View itemView) {
                super(itemView);
            }
        }

    }


}
