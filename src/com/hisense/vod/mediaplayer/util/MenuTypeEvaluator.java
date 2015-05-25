package com.hisense.vod.mediaplayer.util;

import android.animation.TypeEvaluator;
import android.util.Log;

public class MenuTypeEvaluator implements TypeEvaluator{
	private static final String TAG="MenuTypeEvaluator";
	private float alpha;
	private float X;
	private float Y;
	
	@Override
	public Object evaluate(float fraction, Object startValue, Object endValue) {
		// TODO 自动生成的方法存根
		MenuTypeEvaluator end=(MenuTypeEvaluator)endValue;
		MenuTypeEvaluator start=(MenuTypeEvaluator)startValue;
		float e_alpha=start.getAlpha()+fraction*(end.getAlpha()-start.getAlpha());
		float e_X=start.getValueX()+(end.getValueX()-start.getValueX())*fraction;
		float e_Y=start.getValueY()+(end.getValueY()-start.getValueY())*fraction;
		Log.i(TAG," alpha="+e_alpha+" X="+e_X+" Y="+Y);
		return new MenuTypeEvaluator(e_alpha,e_X,e_Y);
	}
	
	public float getAlpha(){
		return alpha;
	}
	
	public float getValueX(){
		return X;
	}
	
	public float getValueY(){
		return Y;
	}
	
	public MenuTypeEvaluator(float a, float x,float y){
		alpha=a;
		X=x;
		Y=y;
	}
}
