package com.hr.nipuream.sample;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class MainActivity extends AppCompatActivity implements
        NRecyclerView.RefreshAndLoadingListener{

    private NRecyclerView recyclerMagicView;
    private MyAdapter adapter;
    private List<String> datas = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

        List<String> strs = Arrays.asList(getResources().getStringArray(R.array.data));
        datas.addAll(strs);
        adapter = new MyAdapter(datas);
        recyclerMagicView.setAdapter(adapter);
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
                if(integer == 1){
                    recyclerMagicView.resetEntryView();
                    datas.add(0,"刷新");
                    adapter.setItems(datas);
                    recyclerMagicView.endRefresh();
                }
            }

        }.execute();
    }

    private int currentPage = 1;
    private int totalPages = 5;

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
                if(integer == 1){
                    if(currentPage >= totalPages){
                        recyclerMagicView.pullNoMoreEvent();
                    }else{
                        datas.add(datas.size(),"加载");
                        datas.add(datas.size(),"加载");
                        datas.add(datas.size(),"加载");
                        datas.add(datas.size(),"加载");
                        datas.add(datas.size(),"加载");
                        datas.add(datas.size(),"加载");
                        datas.add(datas.size(),"加载");
                        datas.add(datas.size(),"加载");
                        datas.add(datas.size(),"加载");
                        adapter.setItems(datas);
                        recyclerMagicView.endLoadingMore();
                        currentPage ++;
                    }
                }
            }
        }.execute();
    }

    @Override
    public void onBackPressed() {
        ViewGroup errorView = (ViewGroup) LayoutInflater.from(this).inflate(R.layout.load_error,(ViewGroup) findViewById(android.R.id.content),false);
        recyclerMagicView.setEntryView(errorView);
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
