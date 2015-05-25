package com.hisense.vod.mediaplayer.util;

import java.util.ArrayList;
import java.util.HashMap;

import com.hisense.vod.R;
import com.hisense.vod.mediaplayer.interfaces.IMenuListener;
import com.hisense.vod.mediaplayer.util.Config.DisplaySize;
import com.hisense.vod.mediaplayer.util.Config.PlayCrop;
import com.hisense.vod.mediaplayer.video.VideoInfo;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public  class MenuRelativeLayout extends RelativeLayout {	
	private static final String TAG="MenuRelativeLayout";	
	private static final int ANIMATION_SCREEN_IMAGE_TIME=100;         //开始时屏幕比例，跳过片头片尾等图标的动画持续时间
	private static final int ANIMATION_FIRST_TEXT_TIME=50;			  //屏幕比例等文字动画持续的时间
	private static final int ANIMATION_ARROW_IMAGE_TIME=50;           //一二级菜单切换时，箭头图片动画持续的时间
	private static final int ANIMATION_SECOND_MENU_TIME=50;           //二级菜单的动画时间
	private static final int ANIMATION_FIRST_ITEM_TIME=50;            //一级菜单向上、向下移动时的动画时间
	private static final int ANIMATION_SECOND_ITEM_TIME=50;           //二级菜单向上、向下移动时的动画时间
	
	private int mFirstItem=1;       								  //清晰度
	private int mSecondItem=-1;      								  //超清	  					
	private int mSize;												  //二级菜单的个数
	private int mDefaultSecondItem;									  //默认已经选择的二级菜单的序号
	private boolean mCanSkipHeader=false;							  //表示是否支持跳过片头片尾
	
	private MenuState mCurrentState=MenuState.UNKNOWN;				  //表示当前的菜单处于的状态	
	private Context mContext;	
	private RelativeLayout mLayoutFirstMenu,mLayoutSecondMenu;		  //一级菜单和二级菜单的layout	
	private ImageView mImageArrow;										
	private RelativeLayout mLayoutFirstText;
	private ImageButton mImageButtonResolution,mImageButtonScreen,mImageButtonCrop;
	private TextView mTextResolution,mTextScreen,mTextCrop,mTextRadio1,mTextRadio2,mTextRadio3,mTextRadio4;	
	private VideoInfo mVideoInfo;
	private ImageView mRadio1,mRadio2,mRadio3,mRadio4;	
	private DisplaySize mDefaultDisplaySize;                      
	private PlayCrop mDefaultCrop;
	private String mDefaultResolution;	
	private IMenuListener mListener;	
	ArrayList<String> s=new ArrayList<String>();	
	
	public enum MenuState{
		UNKNOWN,		//初始的未知状态
		ANIMATIONING,	//显示动画的过程中
		FIRST_MENU,		//显示一级菜单
		SECOND_MENU;	//显示二级菜单
	}
	
	public MenuRelativeLayout(Context context) {
		this(context,null);
	}

	public MenuRelativeLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO 自动生成的构造函数存根
		mContext=context;		
	}
	
	/**
	 * 
	 * @param view  传入的menu view 的实例
	 * @param videoInfo  当前正在播放的视频信息
	 * @param displaySize  默认的屏幕比例
	 * @param crop         默认的是否跳过片头片尾
	 * @param resolution   默认的清晰度
	 *  初始化成员变量。 
	 */	
	public void init(View view,VideoInfo videoInfo, DisplaySize displaySize, PlayCrop crop, String resolution){	
		mLayoutFirstMenu=(RelativeLayout)view.findViewById(R.id.first_menu);
		mLayoutFirstText=(RelativeLayout)view.findViewById(R.id.first_text);
		mLayoutSecondMenu=(RelativeLayout)view.findViewById(R.id.second_menu);
		mImageArrow=(ImageView)view.findViewById(R.id.arrow);
		mImageButtonResolution=(ImageButton)view.findViewById(R.id.btn_resolution);
		mImageButtonScreen=(ImageButton)view.findViewById(R.id.btn_screen);
		mImageButtonCrop=(ImageButton)view.findViewById(R.id.btn_crop);
		mTextResolution=(TextView)view.findViewById(R.id.text_resolution);		
		mTextScreen=(TextView)view.findViewById(R.id.text_screen);
		mTextCrop=(TextView)view.findViewById(R.id.text_crop);
		mTextRadio1=(TextView)view.findViewById(R.id.text_radio1);
		mTextRadio2=(TextView)view.findViewById(R.id.text_radio2);
		mTextRadio3=(TextView)view.findViewById(R.id.text_radio3);
		mTextRadio4=(TextView)view.findViewById(R.id.text_radio4);
		mRadio1=(ImageView)view.findViewById(R.id.radio1);
		mRadio2=(ImageView)view.findViewById(R.id.radio2);
		mRadio3=(ImageView)view.findViewById(R.id.radio3);
		mRadio4=(ImageView)view.findViewById(R.id.radio4);		
		mImageButtonResolution.requestFocus();
		mFirstItem=1;
		mSize=2;
		mDefaultDisplaySize=displaySize;
		mDefaultCrop=crop;			
		mDefaultResolution=resolution;		
		mVideoInfo=videoInfo;	
		mCurrentState=MenuState.UNKNOWN;
		mCanSkipHeader=(mVideoInfo.getVendor().equals("QIYI"));
	}
	
	
	/**
	 * 显示一级菜单的动画，动画的过程：
	 *  同时向上移动屏幕比例图标，向下移动跳过片头片尾图标->向右移动文字内容->如果需要，将文字移动到上次保留的位置
	 */
	public synchronized void showFirstMenu(){
		MenuTypeEvaluator start, end;
        AnimatorSet set = new AnimatorSet();
        float screenX = mImageButtonScreen.getX() > 0.0 ? mImageButtonScreen.getX() : 120;
        float screenY = mImageButtonScreen.getY() > 0.0 ? mImageButtonScreen.getY() : 480;
        float cropX = mImageButtonCrop.getX() > 0.0 ? mImageButtonCrop.getX() : 120;
        float cropY = mImageButtonCrop.getY() > 0.0 ? mImageButtonCrop.getY() : 480;
        float resolutionX = mImageButtonResolution.getX() > 0.0 ? mImageButtonResolution.getX(): 120;
        float resolutionY = mImageButtonResolution.getY() > 0.0 ? mImageButtonResolution.getY(): 480;
        
        if (mFirstItem == 0) {
            start = new MenuTypeEvaluator(0f, resolutionX, resolutionY);
            end = new MenuTypeEvaluator(1f, resolutionX,
                    resolutionY + 180);
            ValueAnimator animationResolution = getAnimation(mImageButtonResolution, start, end,
                    ANIMATION_SCREEN_IMAGE_TIME);
            start = new MenuTypeEvaluator(0f, cropX, cropY);
            end = new MenuTypeEvaluator(1f, cropX, cropY + 360);
            ValueAnimator animationCrop = getAnimation(mImageButtonCrop, start, end,
                    (int) (ANIMATION_SCREEN_IMAGE_TIME * 1.3));

            set.playTogether(animationResolution, animationCrop);
            start = new MenuTypeEvaluator(0f, mLayoutFirstText.getX(), mLayoutFirstText.getY());
            end = new MenuTypeEvaluator(1f, mLayoutFirstText.getX() + 78, mLayoutFirstText.getY());
            ValueAnimator animationFirstTextX = getAnimation(mLayoutFirstText, start, end,
                    ANIMATION_FIRST_TEXT_TIME);
            set.play(animationFirstTextX).after(animationCrop);
        } else if (mFirstItem == 1) {
            start = new MenuTypeEvaluator(0f, screenX, screenY);
            end = new MenuTypeEvaluator(1f, screenX,
                    screenY - 180);
            ValueAnimator animationScreen = getAnimation(mImageButtonScreen, start, end,
                    ANIMATION_SCREEN_IMAGE_TIME);
            start = new MenuTypeEvaluator(0f, cropX, cropY);
            end = new MenuTypeEvaluator(1f, cropX, cropY + 180);
            ValueAnimator animationCrop = getAnimation(mImageButtonCrop, start, end,
                    ANIMATION_SCREEN_IMAGE_TIME);
            set.playTogether(animationScreen, animationCrop);
            start = new MenuTypeEvaluator(0f, mLayoutFirstText.getX(), mLayoutFirstText.getY());
            end = new MenuTypeEvaluator(1f, mLayoutFirstText.getX() + 78, mLayoutFirstText.getY());
            ValueAnimator animationFirstTextX = getAnimation(mLayoutFirstText, start, end,
                    ANIMATION_FIRST_TEXT_TIME);
            set.play(animationFirstTextX).after(animationScreen);
        } else if (mFirstItem == 2) {
            start = new MenuTypeEvaluator(0f, resolutionX,
                    resolutionY);
            end = new MenuTypeEvaluator(1f, resolutionX,
                    resolutionY - 180);
            ValueAnimator animationResolution = getAnimation(mImageButtonResolution, start, end,
                    ANIMATION_SCREEN_IMAGE_TIME);
            start = new MenuTypeEvaluator(0f, screenX, screenY);
            end = new MenuTypeEvaluator(1f, screenX,
                    screenY - 360);
            ValueAnimator animationScreen = getAnimation(mImageButtonScreen, start, end,
                    (int) (ANIMATION_SCREEN_IMAGE_TIME * 1.3));
            set.playTogether(animationResolution, animationScreen);
            start = new MenuTypeEvaluator(0f, mLayoutFirstText.getX(), mLayoutFirstText.getY());
            end = new MenuTypeEvaluator(1f, mLayoutFirstText.getX() + 78, mLayoutFirstText.getY());
            ValueAnimator animationFirstTextX = getAnimation(mLayoutFirstText, start, end,
                    ANIMATION_FIRST_TEXT_TIME);
            set.play(animationFirstTextX).after(animationScreen);
        }

        set.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                // TODO 自动生成的方法存根
                Log.i(TAG, " showFirstMenu end,and check if move to the last Item ");
                mCurrentState = MenuState.FIRST_MENU;
            }
        });
        set.start();
    }
	
	/**
	 * 将一级菜单转换为二级菜单	
	 */
	private synchronized void changeMenuToSecond() {
		// TODO 自动生成的方法存根		
		
		final AnimatorSet set=new AnimatorSet(); 
    	MenuTypeEvaluator start=new MenuTypeEvaluator(1f,mLayoutFirstText.getX(),mLayoutFirstText.getY());
    	MenuTypeEvaluator end=new MenuTypeEvaluator(0f,mLayoutFirstText.getX()-78,mLayoutFirstText.getY()); 
    	ValueAnimator animationFirstText = getAnimation(mLayoutFirstText,start,end, ANIMATION_FIRST_TEXT_TIME);   
    	
     	start=new MenuTypeEvaluator(0f,mImageArrow.getX(),mImageArrow.getY());
    	end=new MenuTypeEvaluator(1f,mImageArrow.getX()+78,mImageArrow.getY());
    	ValueAnimator animationArrow = getAnimation(mImageArrow,start,end,ANIMATION_ARROW_IMAGE_TIME); 
    	
    	if(mFirstItem==0){ 
    		start=new MenuTypeEvaluator(1f,mImageButtonResolution.getX(),mImageButtonResolution.getY());
        	end=new MenuTypeEvaluator(0f,mImageButtonResolution.getX(),mImageButtonResolution.getY()-180); 
        	ValueAnimator animationResolution = getAnimation(mImageButtonResolution,start,end,ANIMATION_SCREEN_IMAGE_TIME);
        	
    		start=new MenuTypeEvaluator(1f,mImageButtonCrop.getX(),mImageButtonCrop.getY());
    		end=new MenuTypeEvaluator(0f,mImageButtonCrop.getX(),mImageButtonCrop.getY()-360); 
    		ValueAnimator animationCrop = getAnimation(mImageButtonCrop,start,end,(int)(ANIMATION_SCREEN_IMAGE_TIME*1.3));
    		set.play(animationFirstText).before(animationCrop);
    		set.playTogether(animationResolution, animationCrop);
    		set.play(animationArrow).after(animationCrop);
    	}else if(mFirstItem==2){
    		start=new MenuTypeEvaluator(1f,mImageButtonResolution.getX(),mImageButtonResolution.getY());
        	end=new MenuTypeEvaluator(0f,mImageButtonResolution.getX(),mImageButtonResolution.getY()+180); 
        	ValueAnimator animationResolution = getAnimation(mImageButtonResolution,start,end,ANIMATION_SCREEN_IMAGE_TIME);
        	
    		start=new MenuTypeEvaluator(1f,mImageButtonScreen.getX(),mImageButtonScreen.getY());
    		end=new MenuTypeEvaluator(0f,mImageButtonScreen.getX(),mImageButtonScreen.getY()+360); 
    		ValueAnimator animationScreen = getAnimation(mImageButtonScreen,start,end,(int)(ANIMATION_SCREEN_IMAGE_TIME*1.3));
    		set.play(animationFirstText).before(animationScreen);
    		set.playTogether(animationResolution, animationScreen );
    		set.play(animationArrow).after(animationScreen);
    	}else{
    		start=new MenuTypeEvaluator(1f,mImageButtonScreen.getX(),mImageButtonScreen.getY());
        	end=new MenuTypeEvaluator(0f,mImageButtonScreen.getX(),mImageButtonScreen.getY()+180); 
        	ValueAnimator animationScreen = getAnimation(mImageButtonScreen,start,end,ANIMATION_SCREEN_IMAGE_TIME);
        	
    		start=new MenuTypeEvaluator(1f,mImageButtonCrop.getX(),mImageButtonCrop.getY());
    		end=new MenuTypeEvaluator(0f,mImageButtonCrop.getX(),mImageButtonCrop.getY()-180); 
    		ValueAnimator animationCrop = getAnimation(mImageButtonCrop,start,end,ANIMATION_SCREEN_IMAGE_TIME);
    		set.play(animationFirstText).before(animationScreen);
    		set.playTogether(animationCrop, animationScreen );
    		set.play(animationArrow).after(animationScreen);
    	}   	
    	
    	start=new MenuTypeEvaluator(0f,mLayoutSecondMenu.getX(),mLayoutSecondMenu.getY());
    	end=new MenuTypeEvaluator(1f,mLayoutSecondMenu.getX()+66,mLayoutSecondMenu.getY());
    	ValueAnimator animationSencondMenu = getAnimation(mLayoutSecondMenu,start,end,ANIMATION_SECOND_MENU_TIME);
    	set.play(animationSencondMenu).after(animationArrow);
    	
    	set.addListener(new AnimatorListenerAdapter(){       	
    		public void onAnimationEnd(Animator animation) {
				// TODO 自动生成的方法存根
				Log.i(TAG," change to second menu end");
				mCurrentState=MenuState.SECOND_MENU;
				
			}
    	});  
    	set.start();	
	}
	
	/**
	 * 检查二级菜单的焦点是否正常，恢复到默认选择的item
	 */
	private synchronized void checkSecondItem(){
		Log.i(TAG," checkSecondItem called and mSecondItem="+mSecondItem+" mDefaultSecondItem="+mDefaultSecondItem);		
		
		AnimatorListenerAdapter l=new AnimatorListenerAdapter(){
			public void onAnimationEnd(Animator animation) {
				// TODO 自动生成的方法存根
				Log.i(TAG," change to second menu end");
				//mCurrentState=MenuState.SECOND_MENU;	
				changeMenuToSecond();
			}
		};
		if(mSecondItem==mDefaultSecondItem){
			//mCurrentState=MenuState.SECOND_MENU;	
			 changeMenuToSecond();
		}else if(mSecondItem>mDefaultSecondItem){
			animationSecondMenu(true,true,l,mSecondItem-mDefaultSecondItem,0f);
		}else{
			animationSecondMenu(false,true,l,mDefaultSecondItem-mSecondItem,0f);
		}
		Log.i(TAG," after checkSecondItem mSecondItem="+mSecondItem+" mDefaultSecondItem="+mDefaultSecondItem);
	}
	
	/**
	 * 将二级菜单转换为一级菜单
	 */
	private void changeMenuToFirst() {
		// TODO 自动生成的方法存根		
		mCurrentState=MenuState.ANIMATIONING;
    	MenuTypeEvaluator start=new MenuTypeEvaluator(1f,mLayoutSecondMenu.getX(),mLayoutSecondMenu.getY());
    	MenuTypeEvaluator end=new MenuTypeEvaluator(0f,mLayoutSecondMenu.getX()-66,mLayoutSecondMenu.getY());
    	ValueAnimator animationSecondMenu = getAnimation(mLayoutSecondMenu,start,end,ANIMATION_SECOND_MENU_TIME); 	
    	
    	start=new MenuTypeEvaluator(1f,mImageArrow.getX(),mImageArrow.getY());
    	end=new MenuTypeEvaluator(0f,mImageArrow.getX()-78,mImageArrow.getY());
    	ValueAnimator animationArrow = getAnimation(mImageArrow,start,end,ANIMATION_ARROW_IMAGE_TIME);     	
    	AnimatorSet set=new AnimatorSet();    	
    	set.play(animationArrow).after(animationSecondMenu);    	   	
    	
    	set.addListener(new AnimatorListenerAdapter(){
    		public void onAnimationEnd(Animator animation) {
				// TODO 自动生成的方法存根
				Log.i(TAG," animSet end");				
				showFirstMenu();
			}
    	});    	
    	set.start();   	
	}
	
	/**
	 * 根据需要获取对应的动画
	 * @param view  需要执行动画的View
	 * @param start	动画开始时的属性值，主要有三个属性， X、Y、和alpha
	 * @param end   动画结束时的属性值
	 * @param duration 动画持续的时间
	 * @return  动画对象实例
	 */
	private ValueAnimator getAnimation(final View view, MenuTypeEvaluator start,  MenuTypeEvaluator end,long duration){
	    ValueAnimator animation = ValueAnimator.ofObject( start,start,end);    			
	    animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){    		
		@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				// TODO 自动生成的方法存根
				MenuTypeEvaluator value=(MenuTypeEvaluator)animation.getAnimatedValue();
				Log.i(TAG," alpha="+value.getAlpha()+" y="+value.getValueY()+" x="+value.getValueX());
				view.setAlpha(value.getAlpha());
				view.setY(value.getValueY());
				view.setX(value.getValueX());
				view.invalidate();
			}    		
	    });  
	    animation.setDuration(duration);
	    animation.setInterpolator(new LinearInterpolator());
	    return animation;
	}
	
	public void setChangeListener(IMenuListener listener){
		mListener=listener;
	}
	
	/**
	 * 当菜单显示的时候，处理按键事件
	 */
	public boolean dispatchKeyEvent(KeyEvent event){
		Log.i(TAG," dispatchKeyEvent called and keycode="+event.getKeyCode()+"action="+event.getAction()+" repeat="+event.getRepeatCount()+" mCurrentState="+mCurrentState);
		if(event.getAction()==KeyEvent.ACTION_DOWN&&event.getRepeatCount()==0){
			switch(mCurrentState){
			case UNKNOWN:
			case ANIMATIONING:
				if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
					mListener.dismissMenu();
				}
				break;
			case FIRST_MENU:
				if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP){
					updateFirstMenuView(true);	
				}else if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN){
					updateFirstMenuView(false);
				}else if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_RIGHT||event.getKeyCode()==KeyEvent.KEYCODE_DPAD_CENTER){
					changeMenu(true);	
				}else if(event.getKeyCode()==KeyEvent.KEYCODE_BACK){
					mListener.dismissMenu();
				}
				break;
			case SECOND_MENU:
				if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_UP){
					updateSecondMenuView(true);	
				}else if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_DOWN){
					updateSecondMenuView(false);
				}else if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_LEFT||event.getKeyCode()==KeyEvent.KEYCODE_BACK){
					changeMenu(false);					
				}else if(event.getKeyCode()==KeyEvent.KEYCODE_DPAD_CENTER){
					secondItemClick();
				}
				break;
			}
		}			
		return true;
	}
	
	/**
	 * 处理二级菜单的点击，生效对应的选项
	 */
	private void secondItemClick() {
		// TODO 自动生成的方法存根
		if(mSecondItem==mDefaultSecondItem){
			Log.i(TAG," return for clicked item is default one");
			return;
		}
		
		switch(mSecondItem){
		case 0:
			mRadio1.setBackgroundResource(R.drawable.btn_radio_on_focused);
			getDefaultRadio().setBackgroundResource(R.drawable.btn_radio_off_normal);	
			if(0==mFirstItem){
				mListener.displaySizeChanged(DisplaySize.ORIGINAL);
			}else{
				mListener.resolutionChanged(s.get(0));
			}
			break;
		case 1:
			if(0==mFirstItem){
				mRadio2.setBackgroundResource(R.drawable.btn_radio_on_focused);
				mRadio3.setBackgroundResource(R.drawable.btn_radio_off_normal);
				mListener.displaySizeChanged(DisplaySize.FULL_SCREEN);
			}else if(2==mFirstItem){
				mRadio2.setBackgroundResource(R.drawable.btn_radio_on_focused);
				mRadio3.setBackgroundResource(R.drawable.btn_radio_off_normal);
				mListener.playCropChanged(PlayCrop.SKIP);
			}else{
				mRadio2.setBackgroundResource(R.drawable.btn_radio_on_focused);
				getDefaultRadio().setBackgroundResource(R.drawable.btn_radio_off_normal);
				if(2==mSize){
					mListener.resolutionChanged(s.get(0));
				}else {
					mListener.resolutionChanged(s.get(1));
				}
			}
			break;
		case 2:
			if(0==mFirstItem){
				mRadio3.setBackgroundResource(R.drawable.btn_radio_on_focused);
				mRadio2.setBackgroundResource(R.drawable.btn_radio_off_normal);
				mListener.displaySizeChanged(DisplaySize.FULL_SCREEN_4_3);
			}else if(2==mFirstItem){
				mRadio3.setBackgroundResource(R.drawable.btn_radio_on_focused);
				mRadio2.setBackgroundResource(R.drawable.btn_radio_off_normal);
				mListener.playCropChanged(PlayCrop.NOT_SKIP);
			}else{
				mRadio3.setBackgroundResource(R.drawable.btn_radio_on_focused);
				getDefaultRadio().setBackgroundResource(R.drawable.btn_radio_off_normal);
				if(2==mSize){
					mListener.resolutionChanged(s.get(1));
				}else {
					mListener.resolutionChanged(s.get(2));
				}
			}
			break;
		case 3:
			mRadio4.setBackgroundResource(R.drawable.btn_radio_on_focused);
			getDefaultRadio().setBackgroundResource(R.drawable.btn_radio_off_normal);
			mListener.resolutionChanged(s.get(3));
			break;
		}
	}
	
	/**
	 * 
	 * @return 返回默认的Radio,已在区别在点击的时候是否需要去执行转换
	 */
	private ImageView getDefaultRadio() {
		// TODO 自动生成的方法存根
		if(mDefaultSecondItem==0){
			return mRadio1;
		}else if(mDefaultSecondItem==1){
			return mRadio2;
		}else if(mDefaultSecondItem==2){
			return mRadio3;
		}else {
			return mRadio4;
		}
	}
	
	/**
	 * 向上或向下移动一级菜单
	 * @param isUp	是否为向上
	 * @param changeItem   是否改变firstItem的值
	 * @param listener	   动画完成的回调
	 */
	private synchronized void animationFirstMenu(boolean isUp,boolean changeItem,ValueAnimator.AnimatorListener listener){
		Log.i(TAG,"smoothFirstMenu called and isUP="+isUp);
		MenuTypeEvaluator start,end;   
		if(isUp){
			if(changeItem){
				mFirstItem-=1;
			}			
			start=new MenuTypeEvaluator(1f,mLayoutFirstMenu.getX(),mLayoutFirstMenu.getY());
			end=new MenuTypeEvaluator(1f,mLayoutFirstMenu.getX(),mLayoutFirstMenu.getY()+180); 
		}else{
			if(changeItem){
				mFirstItem+=1;
			}			
			start=new MenuTypeEvaluator(1f,mLayoutFirstMenu.getX(),mLayoutFirstMenu.getY());
			end=new MenuTypeEvaluator(1f,mLayoutFirstMenu.getX(),mLayoutFirstMenu.getY()-180); 
		}
		ValueAnimator animationFirstMenu = getAnimation(mLayoutFirstMenu,start,end,ANIMATION_FIRST_ITEM_TIME);
		animationFirstMenu.addListener(listener);
		animationFirstMenu.start();
	}	
	
	/**
	 * 向上或向下滑动二级菜单,默认的是移动一个step
	 * @param isUp	是否为向上
	 * @param changeItem	是否改变secondItem的值
	 * @param listener		动画完成的回调
	 */
	private void animationSecondMenu(boolean isUp, boolean changeItem,ValueAnimator.AnimatorListener listener){
		animationSecondMenu(isUp,changeItem,listener,1,1.0f);
	}	
	
	/**
	 * 向上或向下移动二级菜单，可设置step
	 * @param isUp	是否为向上
	 * @param changeItem  是否改变secondItem的值
	 * @param listener    动画完成的回调
	 * @param steps       移动的step数
	 */
	private void animationSecondMenu(boolean isUp, boolean changeItem,ValueAnimator.AnimatorListener listener,int steps,float alpha){
		Log.i(TAG,"animationSecondMenu called and isUP="+isUp);
		MenuTypeEvaluator start,end;   
		if(isUp){
			if(changeItem){
				mSecondItem-=steps;
			}			
			start=new MenuTypeEvaluator(alpha,mLayoutSecondMenu.getX(),mLayoutSecondMenu.getY());
			end=new MenuTypeEvaluator(alpha,mLayoutSecondMenu.getX(),mLayoutSecondMenu.getY()+150*steps); 
		}else{
			if(changeItem){
				mSecondItem+=steps;
			}			
			start=new MenuTypeEvaluator(alpha,mLayoutSecondMenu.getX(),mLayoutSecondMenu.getY());
			end=new MenuTypeEvaluator(alpha,mLayoutSecondMenu.getX(),mLayoutSecondMenu.getY()-150*steps); 
		}
		ValueAnimator animationFirstMenu = getAnimation(mLayoutSecondMenu,start,end,ANIMATION_SECOND_ITEM_TIME);
		animationFirstMenu.addListener(listener);
		animationFirstMenu.start();
	}	
	
	/**
	 * 
	 * @param isUp 是否为方向上键
	 * 用于在一级菜单滑动之后重新设置焦点
	 */
	private void resetFirstMenuFocus(boolean isUp){
		if(!isUp){
			if(mFirstItem==1){
				mImageButtonResolution.requestFocus();
				mTextResolution.setTextAppearance(mContext, R.style.text_menu_focus);
				mTextScreen.setTextAppearance(mContext, R.style.text_menu_unfocus);
			}else if(mFirstItem==2){
				mImageButtonCrop.requestFocus();
				mTextResolution.setTextAppearance(mContext, R.style.text_menu_unfocus);
				mTextCrop.setTextAppearance(mContext, R.style.text_menu_focus);
			}
		}else{
			if(mFirstItem==1){
				mImageButtonResolution.requestFocus();
				mTextResolution.setTextAppearance(mContext, R.style.text_menu_focus);
				mTextCrop.setTextAppearance(mContext, R.style.text_menu_unfocus);
			}else if(mFirstItem==0){
				mImageButtonScreen.requestFocus();
				mTextResolution.setTextAppearance(mContext, R.style.text_menu_unfocus);
				mTextScreen.setTextAppearance(mContext, R.style.text_menu_focus);
			}
		}
	}
	
	/**
	 * 
	 * @param isUp 是否为方向上键
	 * 用于在二级菜单滑动之后重新设置焦点
	 */
	private void resetSecondMenuFocus(boolean isUp){
		Log.i(TAG," reset focus mSecondItem="+mSecondItem);
		setSecondItemFcoused(mSecondItem);
		if(!isUp){			
			setSecondItemUnfcoused(mSecondItem-1);
		}else{			
			setSecondItemUnfcoused(mSecondItem+1);
		}
	}
	
	/**
	 * 
	 * @param i 序列号
	 * 设置二级菜单中的某一项为unfocus状态
	 */
	private void setSecondItemUnfcoused(int i) {
		// TODO 自动生成的方法存根
		Log.i(TAG," setSecondItemUnfcoused called and i="+i+"mDefaultSecondItem="+mDefaultSecondItem);
		if(i==0){
			if(0==mDefaultSecondItem){
				mRadio1.setBackgroundResource(R.drawable.btn_radio_on_normal);				
			}else{
				mRadio1.setBackgroundResource(R.drawable.btn_radio_off_normal);
			}
			mTextRadio1.setTextAppearance(mContext, R.style.text_menu_unfocus);
		}else if(i==1){
			if(1==mDefaultSecondItem){
				mRadio2.setBackgroundResource(R.drawable.btn_radio_on_normal);				
			}else{
				mRadio2.setBackgroundResource(R.drawable.btn_radio_off_normal);
			}
			mTextRadio2.setTextAppearance(mContext, R.style.text_menu_unfocus);
		}else if(i==2){
			if(2==mDefaultSecondItem){
				mRadio3.setBackgroundResource(R.drawable.btn_radio_on_normal);				
			}else{
				mRadio3.setBackgroundResource(R.drawable.btn_radio_off_normal);
			}
			mTextRadio3.setTextAppearance(mContext, R.style.text_menu_unfocus);
		}else{
			if(3==mDefaultSecondItem){
				mRadio4.setBackgroundResource(R.drawable.btn_radio_on_normal);				
			}else{
				mRadio4.setBackgroundResource(R.drawable.btn_radio_off_normal);
			}
			mTextRadio4.setTextAppearance(mContext, R.style.text_menu_unfocus);
		}
	}
	
	/**
	 * 
	 * @param i 序列号
	 * 设置二级菜单中的某一项为focus状态
	 */
	private void setSecondItemFcoused(int i) {
		// TODO 自动生成的方法存根
		Log.i(TAG," setSecondItemFcoused called and i="+i+"mDefaultSecondItem="+mDefaultSecondItem);
		if(i==0){
			if(0==mDefaultSecondItem){
				mRadio1.setBackgroundResource(R.drawable.btn_radio_on_focused);				
			}else{
				mRadio1.setBackgroundResource(R.drawable.btn_radio_off_focused);
			}
			mTextRadio1.setTextAppearance(mContext, R.style.text_menu_focus);
		}else if(i==1){
			if(1==mDefaultSecondItem){
				mRadio2.setBackgroundResource(R.drawable.btn_radio_on_focused);				
			}else{
				mRadio2.setBackgroundResource(R.drawable.btn_radio_off_focused);
			}
			mTextRadio2.setTextAppearance(mContext, R.style.text_menu_focus);
		}else if(i==2){
			if(2==mDefaultSecondItem){
				mRadio3.setBackgroundResource(R.drawable.btn_radio_on_focused);				
			}else{
				mRadio3.setBackgroundResource(R.drawable.btn_radio_off_focused);
			}
			mTextRadio3.setTextAppearance(mContext, R.style.text_menu_focus);
		}else{
			if(3==mDefaultSecondItem){
				mRadio4.setBackgroundResource(R.drawable.btn_radio_on_focused);				
			}else{
				mRadio4.setBackgroundResource(R.drawable.btn_radio_off_focused);
			}
			mTextRadio4.setTextAppearance(mContext, R.style.text_menu_focus);
		}
	}
	
	/**
	 * 
	 * @param toSecond true表示从一级菜单转换到二级菜单，false表示从二级菜单转变为一级菜单
	 * 在一二级菜单之间进行转换
	 */
	private synchronized void changeMenu(boolean toSecond){		
		if(toSecond){		
			initSecondMenuView();    	
		}else{			
			changeMenuToFirst();			
		}
	}	
	
	/**
	 * 
	 * @param isUp 是否为方向上键，true向上滑动，false向下滑动
	 * 用于滑动一级菜单
	 */
	private synchronized  void updateFirstMenuView(boolean isUp){		
		Log.i(TAG,"updateFirstMenuView called and isUp="+isUp+" and mFirstItem="+mFirstItem);
		AnimatorListenerAdapter l=new AnimatorListenerAdapter(){
			public void onAnimationEnd(Animator animation) {
				// TODO 自动生成的方法存根
				Log.i(TAG," update first menu end and mFirstItem="+mFirstItem);
				mCurrentState=MenuState.FIRST_MENU;
			}
		};
		if(isUp&&(mFirstItem==0)){
			Log.i(TAG," reach the top, return");	
			return;
		}
		if((!isUp)&&(mFirstItem==2)){
			Log.i(TAG," reach the bottom, return");	
			return;
		}		
		mCurrentState=MenuState.ANIMATIONING;
		animationFirstMenu(isUp,true,l);
		resetFirstMenuFocus(isUp);
	}
		
	/**
	 * 
	 * @param isUp 是否为方向上键，true向上滑动，false向下滑动
	 * 用于滑动二级菜单
	 */
	private  synchronized  void updateSecondMenuView(boolean isUp){
		Log.i(TAG," updateSecondMenuView called isUp="+isUp+" and mSecondItem="+mSecondItem+" mFirstItem="+mFirstItem);
		AnimatorListenerAdapter l=new AnimatorListenerAdapter(){
			public void onAnimationEnd(Animator animation) {
				// TODO 自动生成的方法存根
				Log.i(TAG," update first menu end");
				mCurrentState=MenuState.SECOND_MENU;
			}
		};
		if(1==mSize){
			Log.i(TAG," only one item,return");
			return;
		}else if(2==mSize){
			if(isUp){
				if(mSecondItem==1){
					Log.i(TAG," secoend menu has reach the top,rturn");
					return;
				}
				if(mFirstItem==2){
					if(!mCanSkipHeader){
						Log.i(TAG," now moive not spport skip header");
						mListener.notSupportSkipHeader();
						return;
					}
				}
				
			}else{
				if(mSecondItem==2){
					Log.i(TAG," secoend menu has reach the bottom,rturn");
					return;
				}
			}
		}else{
			if(isUp){
				if(mSecondItem==0){
					Log.i(TAG," secoend menu has reach the top,rturn");
					return;
				}
			}else{
				if(mSecondItem==mSize-1){
					Log.i(TAG," secoend menu has reach the bottom,rturn");
					return;
				}
			}
		}	
		mCurrentState=MenuState.ANIMATIONING;
		animationSecondMenu(isUp,true,l);
		resetSecondMenuFocus(isUp);
	}
	
	/**
	 * 初始化二级菜单的内容
	 */
	private void initSecondMenuView(){
		mCurrentState=MenuState.ANIMATIONING;
		mSize=2;
		if(mSecondItem==-1){
			mSecondItem=1;
		}
		if(mFirstItem==0){
			mSize=3;
			mRadio1.setVisibility(View.VISIBLE);
			mTextRadio1.setVisibility(View.VISIBLE);
			mTextRadio1.setText("自适应");
			mRadio3.setVisibility(View.VISIBLE);		
			mTextRadio3.setVisibility(View.VISIBLE);
			mTextRadio3.setText("4:3");
			mRadio4.setVisibility(View.GONE);
			mTextRadio4.setVisibility(View.GONE);						
			mTextRadio2.setText("16:9");	
			switch(mDefaultDisplaySize){
			case FULL_SCREEN:
				mDefaultSecondItem=1;
				setSecondItemFcoused(1);
				setSecondItemUnfcoused(2);	
				setSecondItemUnfcoused(0);
				break;
			case FULL_SCREEN_4_3:
				mDefaultSecondItem=2;
				setSecondItemFcoused(2);
				setSecondItemUnfcoused(1);
				setSecondItemUnfcoused(0);
				break;
			case ORIGINAL:
				mDefaultSecondItem=0;
				setSecondItemFcoused(0);
				setSecondItemUnfcoused(1);
				setSecondItemUnfcoused(2);
				break;
			}
		}else if(mFirstItem==1){
			initResolutionView();
		}else{
			mRadio1.setVisibility(View.GONE);
			mTextRadio1.setVisibility(View.GONE);
			mRadio3.setVisibility(View.VISIBLE);			
			mTextRadio3.setVisibility(View.VISIBLE);
			mTextRadio3.setText("不跳过");
			mRadio4.setVisibility(View.GONE);
			mTextRadio4.setVisibility(View.GONE);						
			mTextRadio2.setText("跳过");		
			if(mCanSkipHeader&&mDefaultCrop==PlayCrop.SKIP){
				mDefaultSecondItem=1;
				setSecondItemFcoused(1);
				setSecondItemUnfcoused(2);			
			}else{	
				mDefaultSecondItem=2;
				setSecondItemFcoused(2);
				setSecondItemUnfcoused(1);				
			}
		}		
		Log.i(TAG," after init second menu mSecondItem="+mSecondItem+" mDefaultSecondItem="+mDefaultSecondItem);
	//	changeMenuToSecond();
		checkSecondItem();
	}

	/**
	 * 初始化清晰度的相关UI
	 * @param listener 
	 */
	private void initResolutionView(){		
		HashMap<String,String> Urls = mVideoInfo.getUrls();		
		s.clear();
		if(!Urls.get(Config.BD).equals(Config.NONE_URL)){
			s.add(Config.BD);
		}
		if(!Urls.get(Config.FHD).equals(Config.NONE_URL)){
			s.add(Config.FHD);
		}
		if(!Urls.get(Config.HD).equals(Config.NONE_URL)){
			s.add(Config.HD);
		}	
		if(!Urls.get(Config.SD).equals(Config.NONE_URL)){
			s.add(Config.SD);
		}
		mSize=s.size();
		switch(mSize){
		case 1:
			mDefaultSecondItem=1;
			mRadio1.setVisibility(View.GONE);
			mTextRadio1.setVisibility(View.GONE);		
			mRadio3.setVisibility(View.GONE);
			mTextRadio3.setVisibility(View.GONE);
			mRadio4.setVisibility(View.GONE);
			mTextRadio4.setVisibility(View.GONE);					
			mTextRadio2.setText(getResolutionText(s.get(0)));			
			setSecondItemFcoused(1);			
			break;
		case 2:
			mRadio1.setVisibility(View.GONE);
			mTextRadio1.setVisibility(View.GONE);
			mRadio3.setVisibility(View.VISIBLE);			
			mTextRadio3.setVisibility(View.VISIBLE);
			mTextRadio3.setText(getResolutionText(s.get(1)));
			mRadio4.setVisibility(View.GONE);
			mTextRadio4.setVisibility(View.GONE);							
			mTextRadio2.setText(getResolutionText(s.get(0)));
			if(mDefaultResolution.equals(s.get(0))){
				mDefaultSecondItem=1;
				setSecondItemFcoused(1);
				setSecondItemUnfcoused(2);									
			}else{
				mDefaultSecondItem=2;
				setSecondItemFcoused(2);				
				setSecondItemUnfcoused(1);				
			}
			break;
		case 3:
			mRadio1.setVisibility(View.VISIBLE);			
			mTextRadio1.setVisibility(View.VISIBLE);
			mTextRadio1.setText(getResolutionText(s.get(0)));
			mRadio3.setVisibility(View.VISIBLE);			
			mTextRadio3.setText(getResolutionText(s.get(2)));
			mTextRadio3.setVisibility(View.VISIBLE);
			mRadio4.setVisibility(View.GONE);
			mTextRadio4.setVisibility(View.GONE);						
			mTextRadio2.setText(getResolutionText(s.get(1)));
			if(mDefaultResolution.equals(s.get(0))){
				mDefaultSecondItem=0;
				setSecondItemFcoused(0);
				setSecondItemUnfcoused(1);
				setSecondItemUnfcoused(2);				
			}else if(mDefaultResolution.equals(s.get(1))){	
				mDefaultSecondItem=1;
				setSecondItemFcoused(1);
				setSecondItemUnfcoused(2);
				setSecondItemUnfcoused(0);				
			}else{
				mDefaultSecondItem=2;
				setSecondItemFcoused(2);
				setSecondItemUnfcoused(0);
				setSecondItemUnfcoused(1);			
			}
			break;
		case 4:
			mRadio1.setVisibility(View.VISIBLE);			
			mTextRadio1.setVisibility(View.VISIBLE);
			mTextRadio1.setText(getResolutionText(s.get(0)));
			mRadio3.setVisibility(View.VISIBLE);			
			mTextRadio3.setText(getResolutionText(s.get(2)));
			mTextRadio3.setVisibility(View.VISIBLE);
			mRadio4.setVisibility(View.VISIBLE);			
			mTextRadio4.setVisibility(View.VISIBLE);
			mTextRadio4.setText(getResolutionText(s.get(3)));					
			mTextRadio2.setText(getResolutionText(s.get(1)));
			if(mDefaultResolution.equals(s.get(0))){
				mDefaultSecondItem=0;
				setSecondItemFcoused(0);				
				setSecondItemUnfcoused(1);
				setSecondItemUnfcoused(2);
				setSecondItemUnfcoused(3);								
			}else if(mDefaultResolution.equals(s.get(1))){	
				mDefaultSecondItem=1;
				setSecondItemFcoused(1);			
				setSecondItemUnfcoused(0);
				setSecondItemUnfcoused(2);
				setSecondItemUnfcoused(3);				
			}else if(mDefaultResolution.equals(s.get(2))){
				mDefaultSecondItem=2;
				setSecondItemFcoused(2);
				setSecondItemUnfcoused(0);
				setSecondItemUnfcoused(1);
				setSecondItemUnfcoused(3);			
			}else{
				mDefaultSecondItem=3;
				setSecondItemFcoused(3);
				setSecondItemUnfcoused(0);
				setSecondItemUnfcoused(1);
				setSecondItemUnfcoused(2);			
			}
			break;
		}				
	}
	
	/**
	 * 
	 * @param res 清晰度
	 * @return  清晰度对应的文字表示
	 */
	private String getResolutionText(String res){
		if(res.equals(Config.BD)){
			return "1080P";
		}else if(res.equals(Config.FHD)){
			return "超清";
		}else if(res.equals(Config.HD)){
			return "高清";
		}else{
			return "标清";
		}
	}	
}
