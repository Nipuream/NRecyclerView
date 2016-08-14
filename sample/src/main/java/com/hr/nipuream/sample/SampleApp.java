package com.hr.nipuream.sample;

import android.app.Application;

import com.android.volley.cache.DiskLruBasedCache;
import com.android.volley.cache.plus.SimpleImageLoader;

/**
 * 描述：
 * 作者：Nipuream
 * 时间: 2016-08-12 18:47
 * 邮箱：571829491@qq.com
 */
public class SampleApp extends Application{

    private SimpleImageLoader mImageLoader;

    public SimpleImageLoader getmImageLoader() {
        return mImageLoader;
    }

    public static SampleApp instance;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        DiskLruBasedCache.ImageCacheParams cacheParams = new DiskLruBasedCache.ImageCacheParams(getApplicationContext(), "gzCache");
        cacheParams.setMemCacheSizePercent(0.5f);
        mImageLoader = new SimpleImageLoader(getApplicationContext(),cacheParams);
    }
}
