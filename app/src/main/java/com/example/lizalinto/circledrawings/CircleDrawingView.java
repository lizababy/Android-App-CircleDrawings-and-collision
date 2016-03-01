package com.example.lizalinto.circledrawings;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;

/**
 * Created by lizalinto on 4/6/15.
 */
public class CircleDrawingView extends View implements SensorEventListener {

    private static final String TAG = "CircleDrawingView" ;

    private Paint mBackgroundPaint;
    private boolean mIsTouched;
    private static Point mScreenSize = null;
    private int circleIndex;//circle ID

    private long lastUpdateTime = 0;


    public boolean isTouched() {
        return mIsTouched;
    }

    public void setTouched(boolean isTouched) {
        mIsTouched = isTouched;
    }

    public static Point getScreenSize() {

        return mScreenSize;
    }

    public void setScreenSize(int screenWidth,int screenHeight) {
        mScreenSize = new Point(screenWidth, screenHeight);
    }

    private Circle mNewCircle;


    public ArrayList<Circle> mCircles = new ArrayList<>();


    private GestureDetector mGestureDetector;
    private PointF newCenter;

    public CircleDrawingView(Context context) {
        super(context);
    }

    public CircleDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mGestureDetector = new GestureDetector(context, new GestureListener());


        mBackgroundPaint = new Paint();
        mBackgroundPaint.setColor(0xfff8efe0);//off White
    }

    @Override
    protected void onDraw(Canvas canvas){
        if(mScreenSize==null)
            setScreenSize(canvas.getWidth(),canvas.getHeight());

        canvas.drawPaint(mBackgroundPaint);
        for(Circle circle : mCircles){

            circle.onDraw(canvas);

        }
    }

    public boolean onTouchEvent(MotionEvent event){

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                Log.i(TAG,"  ACTION_DOWN");
                if(isTouched()){
                    setTouched(false);// increasing radius OFF
                }

                break;


            case MotionEvent.ACTION_UP:
                Log.i(TAG,"  ACTION_UP");
                if(isTouched()){
                    setTouched(false);// increasing radius OFF
                }

                break;
        }
        return mGestureDetector.onTouchEvent(event);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (lastUpdateTime == 0) {
            lastUpdateTime = event.timestamp;
            return; }
        long timeDelta = event.timestamp - lastUpdateTime;
        lastUpdateTime = event.timestamp;
        float xAcceleration = round(event.values[0]);
        float yAcceleration = round(event.values[1]);
        int circleIndex =0;

        for(Circle circle : mCircles) {

            if(circle.getVelocity().x !=0 || circle.getVelocity().y !=0) {

                circle.accelerate(xAcceleration, yAcceleration, timeDelta / 1000000000.0f);

                resetCenter(circle);
            }

            onCollide(circleIndex,timeDelta/1000000000.0f);

            circleIndex++;
        }
        invalidate();
    }

    private float round(float value) {
        return Math.round(value*100)/100.0f;
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {



        @Override
        public boolean onDown(MotionEvent e) {

            newCenter = new PointF(e.getX(),e.getY());
            Log.i(TAG, "Received event at x=" + newCenter.x + ",y= " + newCenter.y + ":");


            circleIndex = identifyCircle();//identify the circle index in the touch position


            if (circleIndex <0 ){//no existing circle in current touch position
                if(!isTouchedEdge(newCenter,Circle.MIN_RADIUS)){
                    setTouched(true);
                    addNewCircle();
                    invalidate();
                }
            }
            Log.i(TAG,"  onDown");
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            if(isTouched()) {

                increaseRadius();//increase the radius ON
            }
            Log.i(TAG, "onLongPress: ");

        }


        @Override
        public boolean onScroll(MotionEvent startEvent, MotionEvent endEvent, float distanceX, float distanceY) {
            Log.i(TAG, "onScroll: " + endEvent.toString() );
            if(isTouched()){
                setTouched(false);// increasing radius OFF
            }

            if(circleIndex >=0){

                Circle movingCircle = mCircles.get(circleIndex);
                PointF currentEvent = new PointF(endEvent.getX(),endEvent.getY());
                movingCircle.setCenter(currentEvent);
                //Reset center if circle moved beyond edge of screen

                resetCenter(movingCircle);

                invalidate();
            }
            return true;
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.i(TAG, "onFling: " + velocityX +" "+ velocityY);


            if(circleIndex >=0) {
                Circle movingCircle = mCircles.get(circleIndex);

                movingCircle.setVelocity(new PointF(velocityX,velocityY));

                animateMotion(circleIndex);

            }

            return true;
        }
    }

    private void animateMotion(final int circleIndex) {


        new Thread (new Runnable() {
            @Override
            public void run() {
                animateStep(circleIndex);
            }
        }).start();
    }

    public void animateStep(final int circleIndex){
        post(new Runnable() {
            @Override
            public void run() {
                try{
                    Circle movingC = mCircles.get(circleIndex);
                    movingC.moveStep(.02f);//delay of 20 ms for standard for game
                    resetCenter(movingC);
                    onCollide(circleIndex, .02f);
                    invalidate();
                    animateStep(circleIndex);

                }catch (Throwable t){
                    Log.i(TAG,t.toString());
                }

            }
        });

    }

    private void onCollide(int circleIndex, float timeDelaySec) {

        Circle circle1 = mCircles.get(circleIndex);
        float r1 = circle1.getRadius();
        int localIndex = 0;

        for(Circle circle2 : mCircles){

            if(localIndex != circleIndex){

                double xDiff = circle1.getCenter().x - circle2.getCenter().x;
                double xSquare =  Math.pow(xDiff, 2);
                double yDiff =circle1.getCenter().y - circle2.getCenter().y;
                double ySquare = Math.pow(yDiff, 2);
                double distance =  Math.sqrt(xSquare + ySquare);
                float r2 = circle2.getRadius();

                if(distance <= r1 + r2 ){

                    PointF u1 = new PointF(circle1.getVelocity().x, circle1.getVelocity().y);
                    PointF u2 = new PointF(circle2.getVelocity().x, circle2.getVelocity().y);

                    float vX1 = (u1.x*(r1/r2 - 1) + 2*u2.x)/(r1/r2 + 1);
                    float vY1 = (u1.y*(r1/r2 - 1) + 2*u2.y)/(r1/r2 + 1);

                    float vX2 = (u2.x*(1 - r1/r2) + 2*u1.x*r1/r2)/(r1/r2 + 1);
                    float vY2 = (u2.y*(1 - r1/r2) + 2*u1.y*r1/r2)/(r1/r2 + 1);

                    circle1.setVelocity(new PointF(vX1,vY1));
                    circle2.setVelocity(new PointF(vX2,vY2));
                    circle1.moveStep(timeDelaySec);
                    circle2.moveStep(timeDelaySec);
                }

            }
            localIndex++;
        }

    }

    public static boolean isTouchedEdge(PointF center, float radius) {

        return bound(center.x, radius, getScreenSize().x - radius) ||
                bound(center.y, radius, getScreenSize().y - radius);

    }

    public  static boolean bound(float x_y,float min, float max)
    {
        return x_y <= min || x_y >= max;
    }

     public void resetCenter(Circle movingC) {

        PointF currentCenter =movingC.getCenter();
        float radius = movingC.getRadius();

        if(isTouchedEdge(currentCenter,radius)){//Reset center if circle moved beyond edge of screen

            if(currentCenter.x < radius)
                currentCenter.x = radius;

            if(currentCenter.y < radius)
                currentCenter.y = radius;

            if(currentCenter.x > getScreenSize().x - radius)
                currentCenter.x = getScreenSize().x - radius;

            if(currentCenter.y > getScreenSize().y - radius)
                currentCenter.y = getScreenSize().y - radius;

        }
    }


    private int identifyCircle() {

        int index = 0;

        for(Circle circle : mCircles){
            float circleRadius = circle.getRadius();
            float minX = newCenter.x - circleRadius;
            float minY = newCenter.y - circleRadius;
            float maxX = newCenter.x + circleRadius;
            float maxY = newCenter.y + circleRadius;

            float circleCenterX = circle.getCenter().x;
            float circleCenterY = circle.getCenter().y;

            if((circleCenterX > minX) && (circleCenterX< maxX) &&
                    (circleCenterY > minY) && (circleCenterY <maxY)){
                return index;
            }
            index ++;
        }
        return -1;
    }

    private void addNewCircle() {
        mNewCircle = new Circle(newCenter);
        mCircles.add(mNewCircle);
    }

    private void increaseRadius() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (isTouched()) {
                    if(isTouchedEdge(mNewCircle.getCenter(), mNewCircle.getRadius()))break;
                    mNewCircle.setRadius(mNewCircle.getRadius() + 5);
                    postInvalidate();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}


