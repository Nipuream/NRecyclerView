package com.hr.nipuream.NRecyclerView.view.base;

/**
 * 描述：
 * 作者：Nipuream
 * 时间: 2016-08-03 14:35
 * 邮箱：571829491@qq.com
 */
public interface HeaderStateInterface {

    /**
     * 下拉刷新
     */
    public static final int IDLE = 0x12;


    /**
     * 正在刷新
     */
    public static final int REFRESHING = 0x13;


    /**
     * 释放刷新
     */
    public static final int RELEASE_REFRESH = 0x14;

    /**
     * 暂无更新
     */
    public static final int NO_UPDATE = 0x15;


    /**
     * 设置状态
     * @param state
     */
    public void setState(int state);


    public int getState();


    /**
     * 下拉的距离
     * @param distance
     */
    public void setPullDistance(int distance,int height);



}
