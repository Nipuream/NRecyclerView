# NRecyclerView #

----------

## Feature ##
<br/>
1.一共有两种刷新的方式，一种是根据临界值改变状态，一种是根据下来的距离来改变RefreshView的状态(这里就用美团的效果来参考下)。
<br/>

![refresh style](https://github.com/Nipuream/NRecyclerView/blob/master/art/refresh_style.gif)

<br/>
2.加载的方式也提供了两种风格，一种是快速下滑到底端，加载。另外一种是已经到了底端但是用户还是拖动的，就采用了之前XListView的拖动加载方式。
<br/>

![load style](https://github.com/Nipuream/NRecyclerView/blob/master/art/load_style.gif)
<br/>

3.出了加载更多的方式不同，另外提供了加载结束时，也就是数据全部加载完毕时的显示效果，也有两种方式。一种是直接底部显示一个view，没有更多数据，另外一种就类似QQ的刷新效果，提示没有更多，然后回弹回去。
<br/>
![load end one](https://github.com/Nipuream/NRecyclerView/blob/master/art/load_end_one.gif)
![](https://github.com/Nipuream/NRecyclerView/blob/master/art/load_end_two.gif)


<br/>
4.另外还添加了Android-PullToRefresh的scroll over的效果。
<br/>

![Scroll over 效果](https://github.com/Nipuream/NRecyclerView/blob/master/art/pull_over_scroll.gif)

<br/>
5.当然，我们关心的实用价值，那么当网络错误、或者加载失败，效果该如何呢？NRecyclerView也提供了两种方式，一个是没有广告位的，另外一种是有广告位的。
<br/>

![没有广告位的占位](https://github.com/Nipuream/NRecyclerView/blob/master/art/stand_view_one.gif)
![有广告位的占位](https://github.com/Nipuream/NRecyclerView/blob/master/art/stand_view_two.gif)
<br/>
从效果图中也可以看到，不仅占位还提供了刷新功能，这是好多框架都没有的。

<br/>
6.NRecyclerView还提供了加载数据时不可滑动和可滑动的方式。
<br/>

![](https://github.com/Nipuream/NRecyclerView/blob/master/art/loaddata_scrollenable.gif)
<br/>

7.一般的加载方式，已经提供了差不多了，那么至于RecyclerView的优势当然是LayoutManager效果，我们看看不同LayoutManager加载图片的效果如何把。
<br/>
![](https://github.com/Nipuream/NRecyclerView/blob/master/art/load_image.gif)
<br/>

8.当然，上面都不是重点，很多第三方控件都能做到，我们NRecyclerView要做的是万能加载控件，封装了加载、刷新的功能。比如，我要在刷新、加载控件里面新增一个侧滑删除的功能，这时，必须得我们自己修改源码了，但是NRecyclerView就很容易的解决了，这里用SwipeMenuRecyclerView的控件，我也是随便在github上找的。看看效果：
<br/>
![innerView](https://github.com/Nipuream/NRecyclerView/blob/master/art/inner_view.gif)

## More ##
[打造一个万能刷新加载控件](http://blog.csdn.net/yanghuinipurean/article/details/52840426)
