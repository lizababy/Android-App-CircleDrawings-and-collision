package com.example.lizalinto.circledrawings;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

/**
 * Created by lizalinto on 4/6/15.
 */
public class Circle {

    public static final int MIN_RADIUS = 50 ;
    private PointF mCenter;
    private float mRadius;
    private PointF mVelocity;
    private Paint mCircleColor;


    public void setRadius(float radius) {
        mRadius = radius;
    }

    public float getRadius() {
        return mRadius;
    }

    public void setCenter(PointF center) {
        mCenter = center;
    }

    public PointF getCenter() {
        return mCenter;
    }

    public PointF getVelocity() {
        return mVelocity;
    }

    public void setVelocity(PointF velocity) {
        mVelocity = velocity;
    }

    public Circle(PointF center) {

        setCenter(center);
        mRadius = MIN_RADIUS;
        setVelocity(new PointF(0,0));

        mCircleColor = new Paint();
        mCircleColor.setColor(0x22ff0000);// transparent red
    }

    public void frictionSlowDown() {

        float currentVelocityX = getVelocity().x * 0.995f;
        float currentVelocityY = getVelocity().y * 0.995f;

        if (Math.abs(currentVelocityX) < 0.01)
            currentVelocityX = 0;
        if (Math.abs(currentVelocityY) < 0.01)
            currentVelocityY = 0;

        setVelocity(new PointF(currentVelocityX, currentVelocityY));

    }


    public void bounceIfHitEdge() {

        float radius = getRadius();
        float currentVelocityX = getVelocity().x ;
        float currentVelocityY = getVelocity().y ;

        if(CircleDrawingView.bound(getCenter().x,radius,CircleDrawingView.getScreenSize().x - radius))
            currentVelocityX = currentVelocityX * -1;

        if(CircleDrawingView.bound(getCenter().y,radius,CircleDrawingView.getScreenSize().y - radius))
            currentVelocityY = currentVelocityY * -1;

        setVelocity(new PointF(currentVelocityX, currentVelocityY));
    }

    public void onDraw(Canvas canvas) {

        canvas.drawCircle(getCenter().x, getCenter().y, getRadius(), mCircleColor);

    }
    public void accelerate(float xAcceleration, float yAcceleration, float timeDeltaSeconds){

        float xVelocity = getVelocity().x + xAcceleration*timeDeltaSeconds;
        float yVelocity = getVelocity().y + yAcceleration*timeDeltaSeconds;
        setVelocity(new PointF(xVelocity, yVelocity));
        moveStep(timeDeltaSeconds);

    }
    public void moveStep(float timeDeltaSeconds){

        frictionSlowDown();
        bounceIfHitEdge();

        float cX = getCenter().x + getVelocity().x* timeDeltaSeconds;
        float cY = getCenter().y + getVelocity().y* timeDeltaSeconds;

        setCenter(new PointF(cX,cY));

    }


}
