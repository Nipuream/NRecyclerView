package com.hr.nipuream.sample;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hr.nipuream.NRecyclerView.view.NRecyclerView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PullAndPush extends AppCompatActivity implements
        NRecyclerView.RefreshAndLoadingListener{

    private NRecyclerView recyclerMagicView;
    private MyAdapter adapter;
    private List<String> datas = new ArrayList<String>();
    private int state = 1;

    private List<String> currentDatas = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("PullAndPush");
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
        recyclerMagicView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).marginResId(R.dimen.margin_left).build());
        recyclerMagicView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

//        GridLayoutManager layoutManager = new GridLayoutManager(this,3);

        recyclerMagicView.setLayoutManager(layoutManager);
        recyclerMagicView.setOnRefreshAndLoadingListener(this);

        View view = LayoutInflater.from(this).inflate(R.layout.adventure_layout,(ViewGroup)findViewById(android.R.id.content),false);
        recyclerMagicView.setAdtureView(view);

        datas = Arrays.asList(getResources().getStringArray(R.array.data));

        addItems();
        adapter = new MyAdapter(currentDatas);
        recyclerMagicView.setAdapter(adapter);
    }



    @Override
    public void refresh() {

        currentPage = 1;

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
                if(integer == 1){
                    recyclerMagicView.resetEntryView();
                    addItems();
                    adapter.setItems(currentDatas);
                    recyclerMagicView.endRefresh();
                }
            }

        }.execute();
    }

    private int currentPage = 1;
    private int totalPages = 6;

    @Override
    public void load() {

        currentPage ++;

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

                    if(currentPage >= totalPages){
                        recyclerMagicView.pullNoMoreEvent();
                    }else{
                        addItems();
                        adapter.setItems(currentDatas);
                        recyclerMagicView.endLoadingMore();
                    }

            }
        }.execute();
    }

    private void addItems(){

        List<String> strs = new ArrayList<>();

        for(int i= (currentPage-1)*15;i<currentPage*15;i++ ){
            strs.add(datas.get(i));
        }

        if(recyclerMagicView.isRefreshing())
            currentDatas = strs;
        else
            currentDatas.addAll(strs);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pull_push_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){
            case R.id.normal:
                state = 1;
                break;
            case R.id.over_scroll:
                recyclerMagicView.setPullLoadEnable(false);
                recyclerMagicView.setPullRefreshEnable(false);
                break;
            case R.id.error:
                ViewGroup errorView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.load_error,(ViewGroup) findViewById(android.R.id.content),false);
                recyclerMagicView.setEntryView(errorView);
                break;
            case R.id.reset:
                state = 1;
                recyclerMagicView.setPullLoadEnable(true);
                recyclerMagicView.setPullRefreshEnable(true);
                recyclerMagicView.resetEntryView();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHoader>{

        private List<String> data;

        public MyAdapter(List<String> data){
            this.data = data;
        }

        @Override
        public ViewHoader onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.test,parent,false);
            return new ViewHoader(view);
        }

        @Override
        public void onBindViewHolder(ViewHoader holder, int position) {
            String str = data.get(position);
            holder.tv.setText(str);
        }

        public void setItems(List<String> data){
            this.data = data;
            this.notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return data == null ? 0:data.size();
        }

        public class ViewHoader extends RecyclerView.ViewHolder{

            private TextView tv;
            public ViewHoader(View itemView) {
                super(itemView);
                tv = (TextView) itemView.findViewById(R.id.tv);
            }
        }
    }
}
