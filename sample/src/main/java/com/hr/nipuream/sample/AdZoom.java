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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hr.nipuream.NRecyclerView.view.NRecyclerView;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class AdZoom extends AppCompatActivity implements NRecyclerView.RefreshAndLoadingListener{

    private NRecyclerView recyclerView;
    private List<String> datas ;

    private List<String> currentDatas = new ArrayList<String>();

    private int currentPage = 1;
    private int totalPages = 6;

    private MyAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ad_zoom);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        toolbar.setTitle("AdZoom");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        recyclerView = (NRecyclerView) findViewById(R.id.adzoom_recyclerView);
        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(this).marginResId(R.dimen.margin_left).build(),2);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setOnRefreshAndLoadingListener(this);

        ViewGroup view = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.ad_zoom_header,
                (ViewGroup)findViewById(android.R.id.content),false);

        recyclerView.setAdtureView(view);

        datas = Arrays.asList(getResources().getStringArray(R.array.data));

        addItems();
        adapter = new MyAdapter(currentDatas);
        recyclerView.setAdapter(adapter);
    }


    private void addItems(){

        List<String> strs = new ArrayList<>();

        for(int i= (currentPage-1)*15;i<currentPage*15;i++ ){
            strs.add(datas.get(i));
        }

        currentDatas.addAll(strs);

    }

    @Override
    public void refresh() {

    }

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
                    recyclerView.pullNoMoreEvent();
                }else{
                    addItems();
                    adapter.setItems(currentDatas);
                    recyclerView.endLoadingMore();
                }

            }
        }.execute();
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
