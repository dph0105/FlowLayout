package com.jeremy.administrator.testapplication.flowlayout;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2017/6/8.
 */

public class FlowLayout extends ViewGroup {
    public FlowLayout(Context context) {
        this(context,null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public FlowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int sizeWidth = MeasureSpec.getSize(widthMeasureSpec);
        int modeWidth = MeasureSpec.getMode(widthMeasureSpec);
        int sizeHeight = MeasureSpec.getSize(heightMeasureSpec);
        int modeHeight = MeasureSpec.getMode(heightMeasureSpec);

        //wrap_content
        int width = 0;
        int height = 0;

        //记录每一行的宽度与高度
        int lineWidth = 0;
        int lineHeight = 0;

        int count = getChildCount();
        for (int i=0;i<count;i++){
            View child = getChildAt(i);
            //测量子View的宽和高
            measureChild(child,widthMeasureSpec,heightMeasureSpec);
            //得到LayoutParams
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            //子View占据的宽高
            int childWidth = child.getMeasuredWidth()+lp.leftMargin+lp.rightMargin;  //getMeasureWidth是view的实际大小,getWidth则是view在屏幕中的大小
            int childHeight = child.getMeasuredHeight()+lp.topMargin+lp.bottomMargin;

            //若当,那前行的宽度+这个新增的子View的宽度大于了父ViewGroup的宽度么换行
            if(lineWidth+childWidth>sizeWidth-getPaddingLeft()-getPaddingRight()) {
                //通过每一行的宽度的对比,最后获得最大的宽度
                width = Math.max(width,lineWidth);
                //新的一行的宽度为新增的子View的宽度
                lineWidth = childWidth;
                //高度由原来的高度加上子View的高度
                height+=lineHeight;//换行时才增加高度
                //记录行高
                lineHeight = childHeight;
            }else {
                //叠加行宽
                lineWidth+=childWidth;
                //得到当前行的最大高度
                lineHeight = Math.max(lineHeight,childHeight);
            }
            if(i==count-1) {
                width = Math.max(lineWidth,width);
                //由于换行时才增加高度,最后一个控件必须要添加高度,否则没有最后一行的高度
                height+=lineHeight;
            }
        }

        //设置自身的宽高
        setMeasuredDimension(modeWidth==MeasureSpec.EXACTLY?sizeWidth:width + getPaddingLeft() + getPaddingRight(),
                             modeHeight==MeasureSpec.EXACTLY?sizeHeight:height + getPaddingTop() + getPaddingBottom());


    }

    /**
     * 储存所有的View
     */
    private List<List<View>> childViews = new ArrayList<>();

    /**
     * 每一行的高度
     */
    private List<Integer> lineHeights = new ArrayList<>();

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        childViews.clear();
        lineHeights.clear();

        //当前ViewGroup的宽度
        int width = getWidth();

        int lineWidth = 0;
        int lineHeight = 0;

        List<View> lineViews = new ArrayList<>();

        int count = getChildCount();
        for (int i=0;i<count;i++){
            View child = getChildAt(i);
            MarginLayoutParams lp = ((MarginLayoutParams) child.getLayoutParams());

            int childWidth = child.getMeasuredWidth();
            int childHeight = child.getMeasuredHeight();

            //如果换行
            if(childWidth+lineWidth+lp.leftMargin+lp.rightMargin> width - getPaddingLeft()-getPaddingRight()) {
                //记录行高
                lineHeights.add(lineHeight);
                //记录当前行的Views
                childViews.add(lineViews);

                //重置行宽和行高
                lineWidth = 0;
                lineHeight = childHeight+lp.topMargin+lp.bottomMargin;
                lineViews = new ArrayList<>();
            }

            lineWidth+=childWidth+lp.leftMargin+lp.rightMargin;
            //对比获得当前行最大行高
            lineHeight = Math.max(lineHeight,childHeight+lp.topMargin+lp.bottomMargin);
            lineViews.add(child);



        }
        //for循环结束后处理最后一行
        lineHeights.add(lineHeight);
        childViews.add(lineViews);

        //设置子View的位置
        int left =  getPaddingLeft();
        int top = getPaddingTop();

        int lineNum = childViews.size();
        for (int i=0;i<lineNum;i++){
            //当前行的所有View
            lineViews = childViews.get(i);
            lineHeight= lineHeights.get(i);

            for (int j=0;j<lineViews.size();j++){
                View child = lineViews.get(j);
                //判断child的状态
                if(child.getVisibility()==View.GONE) {
                    continue;
                }

                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

                int lc = left +lp.leftMargin;
                int tc = top+lp.topMargin;
                int rc = lc+child.getMeasuredWidth();
                int bc = tc+child.getMeasuredHeight();

                //为子View进行布局
                child.layout(lc,tc,rc,bc);
                left+= child.getMeasuredWidth()+lp.leftMargin+lp.rightMargin;

            }
            //换行时
            left = getPaddingLeft();
            top+= lineHeight;
        }

    }

    /**
     * 与当前ViewGroup对应的LayoutParams
     * @param attrs
     * @return
     */
    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(),attrs);
    }
}
