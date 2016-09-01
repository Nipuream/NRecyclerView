package com.hr.nipuream.sample;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class InitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_init);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

//        toolbar.setLogo(R.mipmap.ic_launcher);
        toolbar.setTitle("InitActivity");
        toolbar.setTitleTextColor(Color.WHITE);
//        toolbar.setSubtitle("Sub title");

        setSupportActionBar(toolbar);
//        toolbar.setNavigationIcon(R.mipmap.ic_launcher);
    }

    public void pull_push(View view){
        startActivity(new Intent(this,PullAndPush.class));
    }

    public void load_image(View view){
         startActivity(new Intent(this,LoadImage.class));
    }

    public void adventrue_zoom(View view){
        startActivity(new Intent(this,AdZoom.class));
    }

}
