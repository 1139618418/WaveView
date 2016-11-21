package me.happy.demo.view.LockPattern;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/11/14.
 * author：Zcoder2013
 * blog：http://blog.csdn.net/u011507982
 */

public class LockPatternView extends View{

    private Paint pointPaint,linePaint; //画笔
    private List<Point> allPointList= new ArrayList<>(); //所有点的集合
    private List<Point> currentPointList = new ArrayList<>(); //选中点的集合
    private float moveX,moveY; //手指移动坐标
    private final int radius = 12; //半径
    private final int bigRadius = 42; //外圆半径
    private final int count = 3; //矩阵 3*3
    private boolean isActionUp = false; //手指是否抬起

    private int state; //状态
    private static final int STATE_PRESS  = 1; // 手指按下
    private static final int STATE_ERROR = 2;// 选中的密码错误或密码太短，太长

    public LockPatternView(Context context) {
        super(context);
    }

    public LockPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        // 初始化点的画笔
        pointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pointPaint.setStrokeWidth(2f);
        pointPaint.setStyle(Paint.Style.STROKE);
        pointPaint.setAntiAlias(true); // 抗锯齿

        // 初始化线的画笔
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(radius-2f);
        linePaint.setColor(Color.parseColor("#A9A9A9"));
        linePaint.setAntiAlias(true); // 抗锯齿
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //测量 设置当前view宽高一样->正方形
        int specSize = MeasureSpec.getSize(widthMeasureSpec);
        setMeasuredDimension(specSize,specSize);
    }

    int size;

    @Override
    protected void onDraw(Canvas canvas) {

        //获取当前view的宽度
        size = getWidth();
        //画笔颜色
        pointPaint.setColor(Color.BLACK);
        //初始化所有点的集合并画9个点
        allPointList.clear();
        for (int i = 1; i <= count; i++) {
            for (int j = 1; j <= count; j++) {
                allPointList.add(new Point(size * i / 4, size * j / 4));
                canvas.drawCircle(size * i / 4, size * j / 4, radius, pointPaint);
            }
        }

        // 绘制已有的连线
        for (int i = 1; i< currentPointList.size(); i++) {
            Point firstPoint = currentPointList.get(i - 1);
            Point secondPoint = currentPointList.get(i);
            canvas.drawLine(firstPoint.x, firstPoint.y, secondPoint.x, secondPoint.y, linePaint);
        }

        //绘制当前点和手指移动的连线
        if (currentPointList.size() > 0 && !isActionUp) {
            Point point = currentPointList.get(currentPointList.size() - 1);
            canvas.drawLine(point.x,point.y,moveX,moveY, linePaint);
        }

        //绘制当前状态下的点
        for (Point point:currentPointList) {
            if(state==STATE_PRESS){
                //实心画小圆
                pointPaint.setColor(Color.parseColor("#006400"));
                pointPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(point.x,point.y, radius+1, pointPaint);
                //空心画大圆
                pointPaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(point.x,point.y, bigRadius, pointPaint);
            }
            if(state==STATE_ERROR){
                pointPaint.setColor(Color.RED);
                pointPaint.setStyle(Paint.Style.FILL);
                canvas.drawCircle(point.x,point.y, radius+1, pointPaint);
                pointPaint.setStyle(Paint.Style.STROKE);
                canvas.drawCircle(point.x,point.y, bigRadius, pointPaint);
            }
        }
    }

    Runnable runnable;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //当手指按下重置所有的点
                resetPoint();
                isActionUp = false;
                state = STATE_PRESS;
            case MotionEvent.ACTION_MOVE:
                moveX = event.getX(); // 这里要实时记录手指的坐标
                moveY = event.getY();
                int pointSize = currentPointList.size();
                //获得当前选中的点
                Point point = getCurrentPoint(moveX,moveY);
                //判断点是选中的 并且 没有添加到选中的集合中
                if(null!=point&&!isContainPoint(point)){
                    //判断是否已经有了选中的点
                    if(pointSize>0){
                        //获取集合里面最后一个选中的点
                        Point prePoint = currentPointList.get(pointSize-1);
                        //用当前点和之前的点判断出是否有中间点
                        Point middlePoint = getMiddlePoint(prePoint,point);
                        //如果有中间点 加入选中集合 （自动选中中间点的功能）
                        if(null!=middlePoint){
                            currentPointList.add(middlePoint);
                        }
                    }
                    //将选中点加入集合
                    currentPointList.add(point);
                }
                //重绘
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                isActionUp = true;
                pointSize = currentPointList.size();
                //这里判断选中点的数量小于4个 则表示错误
                if(pointSize<4){
                    state = STATE_ERROR;
                }
                invalidate();
                //两秒以后清除选中的点
                clearPoint();
                break;
        }
        return true;
    }

    //两秒以后清除选中的点
    private void clearPoint(){
        runnable = new Runnable() {
            @Override
            public void run() {
                currentPointList.clear();
                invalidate();
            }
        };
        postDelayed(runnable,2000);
    }

    //重置
    private void resetPoint(){
        if(null!=runnable) removeCallbacks(runnable);
        currentPointList.clear();
    }

    //获取选中的点
    private Point getCurrentPoint(float moveX, float moveY) {
        for(Point point:allPointList){
            //两点之间的距离<=外圆半径
            if( Math.sqrt( Math.pow((moveX-point.x),2) + Math.pow((moveY-point.y),2) )<=bigRadius){
                return point;
            }
        }
        return null;
    }

    //判断是否选中点是否在选中集合中
    public boolean isContainPoint(Point currentPoint){
        for(Point point:currentPointList){
            if(point.x==currentPoint.x&&point.y==currentPoint.y){
                return true;
            }
        }
        return false;
    }

    //获取中间的点
    private Point getMiddlePoint(Point prePoint, Point nextPoint) {
        //先拿到距离
        double distance = Math.sqrt( Math.pow((nextPoint.x - prePoint.x),2) + Math.pow((nextPoint.y - prePoint.y),2) );
        //斜着的情况
        if(Math.abs(nextPoint.x - prePoint.x)==size/2
                &&Math.abs(nextPoint.y - prePoint.y)==size/2){
            return new Point(size/2,size/2);
        };
        //横着的情况
        if(Math.abs(nextPoint.x - prePoint.x)==size/2&&distance==size/2){
            return new Point(size/2,nextPoint.y);
        };
        //竖着的情况
        if(Math.abs(nextPoint.y - prePoint.y)==size/2&&distance==size/2){
            return new Point(nextPoint.x,size/2);
        };
        return null;
    }

    class Point{

        int x,y;

        public Point(int x,int y){
            this.x = x;
            this.y = y;
        }
    }
}