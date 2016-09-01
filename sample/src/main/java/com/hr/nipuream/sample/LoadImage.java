package com.hr.nipuream.sample;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import com.hr.nipuream.NRecyclerView.view.NRecyclerView;
import com.hr.nipuream.sample.util.Images;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class LoadImage extends AppCompatActivity  implements
        NRecyclerView.RefreshAndLoadingListener{

    private NRecyclerView recyclerMagicView;
    private List<String> images = new ArrayList<>();
    private List<String> currentImages = new ArrayList<>();
    private int currentPages = 1;
    private int pages = 5;
    private LoadImageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load_image);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("LoadImage");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        recyclerMagicView = (NRecyclerView) findViewById(R.id.recyclerMagicView);

        GridLayoutManager layoutManager = new GridLayoutManager(this,1);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerMagicView.setLayoutManager(layoutManager);
        recyclerMagicView.setOnRefreshAndLoadingListener(this);

        images.addAll(Arrays.asList(Images.imageThumbUrls));
        addImages();
        adapter = new LoadImageAdapter(currentImages);
        recyclerMagicView.setAdapter(adapter);
    }

    private void addImages(){
        for(int i=(currentPages-1)*17;i<(17*currentPages);i++){
            currentImages.add(images.get(i));
        }
        currentPages++;
    }


    @Override
    public void refresh() {
        new AsyncTask<Void,Void,Integer>(){
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 1;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                currentImages.clear();
                currentPages = 1;
                addImages();
                adapter.setItems(currentImages);
                recyclerMagicView.endRefresh();
            }
        }.execute();

    }

    @Override
    public void load() {
        new AsyncTask<Void,Void,Integer>(){
            @Override
            protected Integer doInBackground(Void... params) {
                try {
                    TimeUnit.SECONDS.sleep(3);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return 1;
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);

                if(currentPages > pages){
                    recyclerMagicView.pullNoMoreEvent();
                }else{
                    addImages();
                    adapter.setItems(currentImages);
                    recyclerMagicView.endLoadingMore();
                }
            }
        }.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.load_image_menu,menu);
        return true;
    }

    private boolean isStaggered = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        isStaggered = false;

        switch (item.getItemId()){
            case R.id.span_one:
            {
                GridLayoutManager layoutManager = new GridLayoutManager(this,1);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerMagicView.setLayoutManager(layoutManager);
            }
            break;
            case R.id.span_two:
            {
                GridLayoutManager layoutManager = new GridLayoutManager(this,2);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerMagicView.setLayoutManager(layoutManager);
            }
            break;
            case R.id.span_three:
            {
                GridLayoutManager layoutManager = new GridLayoutManager(this,3);
                layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                recyclerMagicView.setLayoutManager(layoutManager);
            }
            break;
            case R.id.Staggered:
            {
                isStaggered = true;
                StaggeredGridLayoutManager mStaggeredGridLayoutManager = new StaggeredGridLayoutManager(2, OrientationHelper.VERTICAL);
                recyclerMagicView.setLayoutManager(mStaggeredGridLayoutManager);
            }
            break;
        }

        return super.onOptionsItemSelected(item);
    }

    class LoadImageAdapter extends RecyclerView.Adapter<LoadImageAdapter.ViewHolder>{

        private List<String> images;
        private int screenWidth;
        private Random random;


        public LoadImageAdapter(List<String> images){
            this.images = images;
            DisplayMetrics dm = new DisplayMetrics();
            //获取屏幕信息
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            screenWidth = dm.widthPixels;
            random = new Random();
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_image_item,parent,false);
            if(isStaggered){
                View cardView = view.findViewById(R.id.load_image_cardview);
                float typedValue = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,random.nextInt(200)+50,getResources().getDisplayMetrics());
                ViewGroup.LayoutParams lp = cardView.getLayoutParams();
                lp.height = (int) typedValue;
                lp.width = random.nextInt(screenWidth/2)+screenWidth/2;
                cardView.setLayoutParams(lp);
            }
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            SampleApp.instance.getmImageLoader().get(images.get(position),holder.iv);
        }

        @Override
        public int getItemCount() {
            return images.size();
        }

        public void setItems(List<String> images){
            this.images = images;
            this.notifyDataSetChanged();
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            private CardView cardView;
            private ImageView iv;

            public ViewHolder(View itemView) {
                super(itemView);
                cardView = (CardView) itemView.findViewById(R.id.load_image_cardview);
                iv = (ImageView)itemView.findViewById(R.id.load_image_iv);
            }
        }
    }




}
