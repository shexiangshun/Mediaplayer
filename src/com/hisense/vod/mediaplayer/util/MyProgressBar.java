package com.hisense.vod.mediaplayer.util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.Log;
import android.view.View;

public class MyProgressBar extends View{
	 private static final int MAX_ANGLE=270;
	 private static final int MIN_ANGLE=2;
	 private static final int WIDTH=14;
	 private static final int ROUND_COLOR=0xff00c8c6;
	 private static final int BOTTOM_COLOR=0x75000000;
	 private static final int ROUND_SPEED=15;
	 private static final int BOTTOM_SPEED=30;
     
     private Paint  mRoundPaints;		//上层进度画笔
     private RectF  mRoundOval;			//矩形区域    			 
	 private Paint mBottomPaint;		//底层画笔
	 private Runnable mRunnable;
	 private int mStartAngle;
	 private int mSweepAngle;	 
	 private boolean mIscreasing=false;
	 
	public MyProgressBar(Context context) {
		super(context);		
        mRoundPaints = new Paint();
        mRoundPaints.setAntiAlias(true);                          
        mBottomPaint = new Paint();
        mBottomPaint.setAntiAlias(true);      
        mBottomPaint.setColor(BOTTOM_COLOR);              
        mRoundOval = new RectF(0, 0,  0, 0);  
        mRoundPaints.setStyle(Paint.Style.STROKE);            	
        mBottomPaint.setStyle(Paint.Style.STROKE);              
        mRoundPaints.setStrokeWidth(WIDTH);       
        mBottomPaint.setStrokeWidth(WIDTH);        
        mRoundPaints.setColor(ROUND_COLOR);        
        mRoundPaints.setStrokeCap(Paint.Cap.ROUND);    
    	mStartAngle=0;
    	mSweepAngle=MAX_ANGLE;
    	mIscreasing=false;
	}
	
    public synchronized void dismiss ()
    { 	
    	if(mRunnable!=null){
    		this.removeCallbacks(mRunnable);
    		mRunnable=null;
    	}    	
    }    
    
    public synchronized void show()
    {    
    	if(mRunnable==null){
    		mRunnable=new RefreshProgressRunnable();
    	}    	
    	this.postDelayed(mRunnable,80);    	
    }    
    
    private class RefreshProgressRunnable implements Runnable {
        public void run() {
            synchronized (MyProgressBar.this) {           	
            	invalidate();
            	if(mIscreasing){
            		mSweepAngle+=ROUND_SPEED;
            		mStartAngle+=BOTTOM_SPEED-ROUND_SPEED/2;
            	}else{
            		mSweepAngle-=ROUND_SPEED;
            		mStartAngle+=BOTTOM_SPEED+ROUND_SPEED/2;
            	}           	
            	
            	if(mSweepAngle<=MIN_ANGLE){            		
            		mIscreasing=true;
            		mSweepAngle=MIN_ANGLE;
            	}
            	
            	if(mSweepAngle>=MAX_ANGLE){
            		mSweepAngle=MAX_ANGLE;
            		mIscreasing=false;
            	}     	
            	
            	if(mStartAngle>=360){
            		mStartAngle-=360;
            	}           	
            	show();
            }
           
        }        
    }
    
    protected void onDetachedFromWindow() {
    	super.onDetachedFromWindow();
    	if(mRunnable!=null){
    		this.removeCallbacks(mRunnable);
    		mRunnable=null;
    	}    	
    }
    
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		Log.i("", "W = " + w + ", H = " + h);		
		
		int sl = getPaddingLeft();
		int sr = getPaddingRight();
		int st = getPaddingTop();
		int sb = getPaddingBottom();				
		mRoundOval.set(sl + WIDTH/2, st + WIDTH/2, w - sr - WIDTH/2, h - sb - WIDTH/2);		
	}
	
	public void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);				
		canvas.drawArc(mRoundOval, 0, 360, false, mBottomPaint);	
		canvas.drawArc(mRoundOval,mStartAngle , mSweepAngle, false, mRoundPaints);		
	}
}

