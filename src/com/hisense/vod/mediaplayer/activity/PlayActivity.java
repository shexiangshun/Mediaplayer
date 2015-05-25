package com.hisense.vod.mediaplayer.activity;

import java.util.HashMap;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

import com.hisense.vod.R;
import com.hisense.vod.mediaplayer.interfaces.IMenuListener;
import com.hisense.vod.mediaplayer.interfaces.IPlayController;
import com.hisense.vod.mediaplayer.interfaces.IPlayListener;
import com.hisense.vod.mediaplayer.util.Config;
import com.hisense.vod.mediaplayer.util.Config.DisplaySize;
import com.hisense.vod.mediaplayer.util.Config.PlayCrop;
import com.hisense.vod.mediaplayer.util.MenuRelativeLayout;
import com.hisense.vod.mediaplayer.util.MyProgressBar;
import com.hisense.vod.mediaplayer.util.StatusManager;
import com.hisense.vod.mediaplayer.util.StatusManager.Status;
import com.hisense.vod.mediaplayer.util.TimeProcessor;
import com.hisense.vod.mediaplayer.util.VodError;
import com.hisense.vod.mediaplayer.video.PlayListManager;
import com.hisense.vod.mediaplayer.video.VideoInfo;
import com.hisense.vod.mediaplayer.video.letv.LetvVideoView;
import com.jamdeo.tv.common.SiloConstants;
import com.jamdeo.tv.common.SiloConstants.SiloType;
import com.qiyi.video.player.data.Definition;
import com.qiyi.video.player.data.IPlaybackInfo;
import com.qiyi.video.player.error.ISdkError;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author shexiangshun
 * @date   2014-12-15
 * @description
 */

public class PlayActivity extends Activity implements IPlayListener,IMenuListener{
	private static final String TAG="PlayActivity";
	private static final int OVERLAY_TIMEOUT = 3000;															//无操作时不显示进度条等信息的时间
	private static final int ERROR_DIALOG_SHOW_TIME=3000;										//	出现错误时对话框显示的时间
	private static final int BUFFERING_TIMEOUT=60*1000;												//缓冲超时的时间
	private static final int LOADING_TIMEOUT=60*1000;													//加载的超时时间
	private static final long BACK_EVENT_VALID_TIME=2000;											//退出时两次back键的有效间隔时间
	private static final int PLAYBACK_MIN_VALID_TIME=60*1000;									//续播时的最小有效时间
	private static final int PREVIEW_DEFAULT_TIME_FOR_VIP_VIDEO=5*60*1000;	//付费影片预览的默认时间
	private static final int RESULT_PREVIEW_END= 0;															//返回Intent的结果，表示预览结束需要收费，需要客户端显示收费界面
    private static final int RESULT_PREVIEW_OTHER= 1;														//返回Intent的结果，表示预览时中途退出，   
    private static final int RESULT_PREVIEW_CANCLE= 2;													//预览结束后取消购买，
	private static final String CMD_PAUSE="pause";																//表示暂停的标识
	private static final String CMD_PLAY="play";																		//表示播放的标识
    
	private static final String TIP_NO_MESSAGE="no message";		//不提示时的默认值
	private static final int FADE_OUT = 1;													//消失上下提示框的消息值	
	private static final int UPDATE_PROGRESS_PLAYING = 2;				//播放时进行相关UI跟新的消息值
	private static final int UPDATE_SEEKING = 3;									//快退、快进时更新UI的消息值
	private static final int FINISH = 4;															//结束当前activity的消息值
	private static final int BUFFERING= 5;													//判断缓冲超时的消息值
	private static final int AUTH_COMPLETE= 6;										//CNTV认证完成的消息值
	private static final int LOADING=7;														//判断加载超时的消息值
	private static final int SILO=8;																//处理silo切换相关的消息
	private static final int NET_SPEED=9;													//显示下载速度的消息
	private static final int AD_ACTION=10;												//广告倒计时的消息
	private static final int CHECK_PAUSE_PLAY=11;								//执行暂停&播放后进行检查，防止出现内部的状态错误，解决无法播放以及暂停按钮不消失的问题
		
	private int[] arrayOfSpeed={60*1000,90*1000,2*60*1000,150*1000,3*60*1000,210*60*1000,4*60*1000,270*1000,5*60*1000};	//快进、快退时每次前进的间隔数组
		
	private FrameLayout mVideoContainer,mRootView;							//用于存放视频的layout
	private View mOverlayHeader,mOverlayBottom,mAdTimming;		//上下提示框、广告倒计时的View
	private TextView mTitle,mSubTitle,mTimeText,mProgressText,mDurationText,mIndicatorText,mSpeedText,mLoadingText,mAdText;	//各种TextView
	private ImageView mCenterImage;																//中间的图片表示暂停的
	private MyProgressBar mRoundBar;															//加载过程中的圆形进度条
	private RelativeLayout mIndicator;																//seek时的指示器
	private RelativeLayout mBackground;														//播放时的背景
	private SeekBar mProgressBar;																		//播放的进度条
	private TimeProcessor mTimePro;																//时间相关字符串转换的类
	private TextView mPlayLimitText;																	//显示播放受限的提示
	private PlayListManager mPlayList;																//用于管理播放列表的类
	private IPlayController mPlayer;																	//用于播放行为的控制类
	private VideoInfo mVideoInfo;																		//当前播放视频的信息
	private MenuRelativeLayout mMenuView;												//菜单的视图
	
	private boolean mShowing=false;															//进度条等信息是否显示,true显示
	private boolean mIsRight=false;																//当前按键是否为右键
	private int mPosition;																						//播放器开始的位置
	private int mDuration;																						//视频的总时长
	private long mBufferStartTime=-1;																//缓冲开始的时间
	private long mSeekStartTime=-1;																//seek开始的时间
	private int mKeyRepeat=-1;																			//按键的重复次数
	private boolean mIsCompleted=false;													//播放是否完成，用于在退出时上报日志
	private boolean mAdPlayEnd=false;														//广告是否播放完成
	private boolean mMenuShowing=false;												//菜单对话框是否显示
	private boolean mLongPressed=false;													//是否为长按
	private long mKeyEventStartTime=0;														//按键事件开始的时间
	private long mKeyEventEslapeTime=0;														//按键事件已经消逝的时间
	private long mBackEventStartTime=0;														//返回键按下的时间
	private int seekTime=0;																					//需要快进的时长
	private int mRetryCount=0;																			//当出错的时候重新加载尝试的次数
	private int mHeaderTime=0;																			//片头时间
	private int mTailerTime=0;																				//片尾时间
	private String mTip=TIP_NO_MESSAGE;														//播放过程中的提示语
	private boolean mSeekSkipHeader=false;												//是否需要seek跳过片头
	private boolean mIsRestart=false;															//是否为重新启动 
	private boolean mSkipTipShowed=false;												//标志跳过片尾的Toast是否显示
	private int mPlayLimitTime=PREVIEW_DEFAULT_TIME_FOR_VIP_VIDEO;	  //付费影片可以预览的时间
	private boolean mIsVideoFree=true;														//是否为收费影片，true为免费 false为收费
	private boolean mVideoPayed=false;														//付费是否完成
	private boolean mResolutionChanging=false;										//当前是否在清晰度切换中
	private Toast mToast;																						//Toast
	private int mAdPlayCnt=0;																				//广告的播放次数，用来区分前插、中插和后插广告
	private int mSavePosition=0;																			//silo切换时保存的位置	
	private boolean mOnStartCalled=false;													//onStart() 方法是否调用
	private Intent mIntent;																					//启动的intent，用于在奇异收费片源是返回数据
	private boolean mExitByUser=false;														//表示是否是用户手动退出播放，在奇异预览结束时区分是不是自动调用
	private boolean mPreViewCompleteShowing=false;							//表示试看结束的对话框是否显示
	
	/**
	 * 监听网络状况和VIDDA silo 切换的Receiver
	 * 1.CONNECTIVITY_ACTION 网络状况发生改变
	 * 2.SILO_CHANGED_INTENT VIDDA silo切换的广播
	 */
	private  BroadcastReceiver AdReceiver= new BroadcastReceiver() {
		boolean networkDisconnectHappen=false;
		
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)){
				ConnectivityManager connectMgr = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
				NetworkInfo ethInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
				NetworkInfo wifiNetInfo = connectMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				if(!ethInfo.isConnected() && !wifiNetInfo.isConnected()){
					Log.i(TAG," network not connected, show toast");
					networkDisconnectHappen=true;
					showToast("主人，网络连接已断开，您只能再观看一小会了！",true);
				}else{
					if(networkDisconnectHappen){
						Log.i(TAG," network has been connected, show toast");
						showToast("主人，网络连接已恢复，您可以继续观看了！",true);
						networkDisconnectHappen=false;
					}
				}
			}else if(intent.getAction().equals(SiloConstants.SILO_CHANGED_INTENT)){
				SiloType siloType = (SiloType)intent.getSerializableExtra(SiloConstants.CURRENT_SILO_TYPE_EXTRA);
				if (siloType != null) {
					Log.i(TAG, "Current siloType=" + siloType);
					if(siloType==SiloType.VOD){
						if(mHandler.hasMessages(SILO)){
							mHandler.removeMessages(SILO);
						}
						Log.i(TAG," silo change to app center,and mOnStartCalled="+mOnStartCalled);
						if(!mOnStartCalled){
							sendDelayMessage(SILO,10,false);
						}
					}
				}
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		StatusManager.setStatus(Status.UNKOWN);
		Log.i(TAG," ###########################Caution!#################################");
		Log.i(TAG," This player is used for VOD, version:"+Config.Version);
		Log.i(TAG," ####################################################################");
		setContentView(R.layout.player);
		mIntent=getIntent();
		try {
			mPlayList = new PlayListManager(new JSONObject(mIntent.getExtras().getString("videoInfo")));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(TAG," PlayListManager init exception:"+e.getMessage());
		}
		mPosition=mPlayList.getPlaybackPosition();
		mVideoContainer =(FrameLayout)findViewById(R.id.view_container);
		mRootView =(FrameLayout)findViewById(R.id.rootView);
		mOverlayHeader = findViewById(R.id.header);
		mOverlayBottom = findViewById(R.id.bottom);
		mTitle = (TextView) findViewById(R.id.title);
		mSubTitle = (TextView) findViewById(R.id.subtitle);
		mTimeText = (TextView) findViewById(R.id.time);
		mSpeedText = (TextView) findViewById(R.id.text_speed);
		mProgressText=(TextView) findViewById(R.id.text_progress);
		mDurationText=(TextView) findViewById(R.id.text_duration);
		mLoadingText=(TextView) findViewById(R.id.text_loading);
		mIndicatorText=(TextView) findViewById(R.id.indicator_text);
		mAdTimming= findViewById(R.id.ad_timming);
		mAdText= (TextView) findViewById(R.id.ad_text);
		mProgressBar=(SeekBar)findViewById(R.id.progressbar);
		mCenterImage=(ImageView)findViewById(R.id.center_image);
		mIndicator=(RelativeLayout)findViewById(R.id.indicator);
		mBackground=(RelativeLayout)findViewById(R.id.background);
		mPlayLimitText=(TextView) findViewById(R.id.text_play_limit);
		mRoundBar=new MyProgressBar(this);
		mTimePro=new TimeProcessor();
		mProgressBar.setEnabled(false);
	}
	
	protected void onResume(){
		super.onResume();
		mPlayer.onActivityResume(this);
	}
	
	protected void onRestart(){
		super.onRestart();
		if(StatusManager.DEBUG) Log.i(TAG,"########################onRestart called################");
		mIsRestart=true;
		if(mCenterImage!=null&&mCenterImage.isShown()){
			mCenterImage.setVisibility(View.INVISIBLE);
		}
		
		if(mAdTimming!=null&&mAdTimming.isShown()){
			mAdTimming.setVisibility(View.INVISIBLE);
		}
	}
	
	//设置奇异的返回结果
	public void setQiyiResult(int resultCode){
		setResult(resultCode, mIntent);
	}
	
	protected void onPause(){
		super.onPause();
		if(StatusManager.DEBUG) Log.i(TAG,"########################onPause called################");
		try{
			int cur=mPlayer.getCurrentPosition();
			if(cur>0){
				Log.i(TAG,"onPause get current position is: "+cur);
				mSavePosition=cur;
			}else{
				if(mPosition>0){
					mSavePosition=mPosition;
					Log.i(TAG,"onPause for getCurrentPosition=0, mSavePosition use the mPosition:"+mPosition);
				}else{
					Log.i(TAG,"onPause for getCurrentPosition=0, mSavePosition keep the last value:"+mSavePosition);
				}
			}
		}catch(Exception e){
			Log.e(TAG,"get current position exception:"+e.getMessage());
		}
		
		if(mToast!=null){
			mToast.cancel();
			mToast=null;
		}
	}
	
	protected void onStop(){
		if(StatusManager.DEBUG) Log.i(TAG,"########################onStop called################");
		super.onStop();
		mPlayer.onActivityStop(this);
		try{
			Log.i(TAG,"unregister receiver called ");
			unregisterReceiver(AdReceiver);
		}catch(Exception e){
			Log.e(TAG, "unregister receiver failed exception:"+e.getMessage());
		}
		end();
	}
	
	/**
	 * 在退出前进行相关释放动作
	 */
	private void end(){
		Log.i(TAG," end called, remove Messages!!!");
		mTip=TIP_NO_MESSAGE;
		if(mHandler.hasMessages(UPDATE_PROGRESS_PLAYING)){
			mHandler.removeMessages(UPDATE_PROGRESS_PLAYING);
		}
		
		if(mHandler.hasMessages(SILO)){
			mHandler.removeMessages(SILO);
		}

		if(mHandler.hasMessages(AD_ACTION)){
			mHandler.removeMessages(AD_ACTION);
		}

		if(mHandler.hasMessages(NET_SPEED)){
			mHandler.removeMessages(NET_SPEED);
		}
		
		if(mHandler.hasMessages(BUFFERING)){
			mHandler.removeMessages(BUFFERING);
		}
		if(mHandler.hasMessages(LOADING)){
			mHandler.removeMessages(LOADING);
		}
		if(mHandler.hasMessages(CHECK_PAUSE_PLAY)){
			mHandler.removeMessages(CHECK_PAUSE_PLAY);
		}
		if(!mIsCompleted){
			reportVideoExit();
		}
		dismissLoadingView();
		mPlayer.release();
		PlayListManager.setDisplaySize(DisplaySize.ORIGINAL);      //在退出时将画面比例设置为自适应 K690U-634
		StatusManager.setStatus(Status.UNKOWN);
	}
	
	protected void onStart(){
		super.onStart();
		Log.i(TAG,"#################onStart called############### ");
		IntentFilter intentf = new IntentFilter();	
		intentf.addAction(SiloConstants.SILO_CHANGED_INTENT);
		intentf.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		try{
			registerReceiver(AdReceiver, intentf);
		}catch(Exception e){
			Log.e(TAG," register receiver exception:"+e.getMessage());
		}
			
		Log.i(TAG,"register receiver called ");
		mVideoInfo =mPlayList.getCurrentVideo();
		startPlay(mAdPlayEnd,mPosition);
		showLoadingView(true,true);
		mOnStartCalled=true;
	}
	
	private void siloStartPlay(){
		Log.i(TAG," siloStartPlay called, and mPosition="+mPosition);
		mVideoInfo =mPlayList.getCurrentVideo();
		startPlay(mAdPlayEnd,mPosition);
		showLoadingView(true,true);
	}
	
	/**
	 * 
	 * @param showTitle	 显示“即将播放：xxx"
	 * @param showBackground  是否显示背景图片
	 */
	private void showLoadingView(boolean showTitle,boolean showBackground){
		Log.i(TAG," show laoding View called ");
		if(showTitle){
			mLoadingText.setVisibility(View.VISIBLE);
			mLoadingText.setText("即将播放  ： "+mVideoInfo.getTitle());
		}else{
			mLoadingText.setVisibility(View.GONE);
		}
		mRootView.removeView(mBackground);
		if(showBackground){
			//mBackground.setBackgroundResource(R.drawable.bg_upcoming);
			mRootView.addView(mBackground, 1, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT));
		}
//		}else{
//			mBackground.setBackgroundColor(0x00000000);
//		}
		mSpeedText.setVisibility(View.VISIBLE);
		FrameLayout.LayoutParams flParams = new FrameLayout.LayoutParams(180,180, Gravity.CENTER);
		mRootView.removeView(mRoundBar);
		mRootView.addView(mRoundBar,  flParams);
		mRoundBar.show();
	}
	
	/**
	 * 消逝加载的界面
	 */
	private void dismissLoadingView(){
		mLoadingText.setVisibility(View.GONE);
		mSpeedText.setVisibility(View.GONE);
		mRoundBar.dismiss();
		mRootView.removeView(mRoundBar);
	}
	
	/**
	 * 开始播放相关影片
	 * @param adPlayed    广告是否播放完了
	 * @param pos		       开始播放的position
	 */
	private void startPlay(boolean adPlayed, int pos){
		Log.i(TAG," start Play called now setdatasource");
		int seekTime=pos;
		PlayListManager.resetPlayer();
		mPlayer=mVideoInfo.getPlayer(mVideoContainer, this, this);
		seekTime=checkSkipHeaderAndTip(pos);
		if(StatusManager.getStatus()!=Status.ERROR){
			mPlayer.setDataSource(mVideoInfo,adPlayed,seekTime);
			sendDelayMessage(LOADING,LOADING_TIMEOUT,true);
			sendDelayMessage(NET_SPEED,500,true);
			StatusManager.setStatus(Status.PREPARING);
		}else{
			Log.e(TAG," fatal error when get player,please check!!!");
		}
	}
	
	/**
	 * 判断开始播放的位置和提示语	
	 * @param pos 开始播放的位置
	 * @return    
	 */
	private int checkSkipHeaderAndTip(int pos) {
		// TODO 自动生成的方法存根
		int ret=pos;
		if(!mIsRestart){
			mIsRestart=false;
			if(pos>=PLAYBACK_MIN_VALID_TIME){
				String time=mTimePro.getTipString(pos);
				ret=pos;
				mTip="上次观看到"+time+"，为您从此处播放！";
			}else{ 
				if(0<pos&&pos<PLAYBACK_MIN_VALID_TIME){
					mTip="您上次观看的时间小于1分钟，为您从头开始播放！";
					ret=0;
				}else{
					mTip=TIP_NO_MESSAGE;
				}
			}
			if(mVideoInfo.getVendor().equals("QIYI")){
				if(PlayListManager.getPlayCrop()==PlayCrop.SKIP){
					if(mIsVideoFree){
						mSeekSkipHeader=true;
					}
				}
			}
		}else{
			Log.i(TAG," restart=true so seek to the saved position:"+mSavePosition);
			ret=mSavePosition;
		}
		return ret;
	}

	private void updateViews(VideoInfo video){
		mTitle.setText(video.getTitle());
		mSubTitle.setText(video.getSubTtle());
		}
	
	private  Handler mHandler = new Handler() {
		long nowTimeStamp,lastTimeStamp,nowTotalRxBytes,lastTotalRxBytes;
		int saveAdDownCount=0;
		
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case FADE_OUT:
				Bundle data=msg.getData();
				hideOverlay(data.getBoolean("force"));
				break;
			case UPDATE_PROGRESS_PLAYING:
				Log.i(TAG," update progress Status="+StatusManager.getStatus());
				if(mShowing){
					mTimeText.setText(mTimePro.getTime());
				}
				sendDelayMessage(UPDATE_PROGRESS_PLAYING,1000,false);
				if(StatusManager.getStatus()==Status.PLAYING){
					try{
						int cur=mPlayer.getCurrentPosition();
						Log.i(TAG," update progress getCurrentPosition=:"+cur);
						if(cur>0){
							mPosition = cur;
						}
						mProgressBar.setProgress(mPosition);
						mProgressText.setText(mTimePro.getTimeString(mPosition));
					    //Log.i(TAG," cur="+cur+" mTailerTime="+mTailerTime+" mDuration="+mDuration+ " mSeekSkipHeader="+mSeekSkipHeader);
						if(mSeekSkipHeader){
							if(mTailerTime<=0||mTailerTime>=mDuration){
								Log.i(TAG, " mTailerTime<=0 or mTailerTime>=mDuration, break!!!");
								break;
							}else{
								if(cur>=mTailerTime-5000&&cur<mTailerTime){
									Log.i(TAG, " show the skip tailer toast");
		        					if(!mSkipTipShowed){
		        						showToast("即将为您跳过片尾！",false);
		        						mSkipTipShowed=true;
		        					}
		        				}else if(cur>mTailerTime) {
		        					Log.i(TAG, " called onMoiveComplete for skip tailer");
		        					onMovieComplete();
		        				}
							}
						}
					}catch(Exception e){
						Log.e(TAG, " get progress in update palying exception:"+e.getMessage());
					}
				}
				break;
			case UPDATE_SEEKING:
				Log.i(TAG,"handler update seeking called.");
				mKeyEventEslapeTime=System.currentTimeMillis()-mKeyEventStartTime;
				 if(mKeyEventEslapeTime>0&&mKeyEventEslapeTime<=2000){
			        seekTime+=arrayOfSpeed[0];
			     }else if(mKeyEventEslapeTime>2000&&mKeyEventEslapeTime<=4000){
			        seekTime+=arrayOfSpeed[1];
			     }else if(mKeyEventEslapeTime>4000&&mKeyEventEslapeTime<=6000){
			        seekTime+=arrayOfSpeed[2];
			     }else if(mKeyEventEslapeTime>6000&&mKeyEventEslapeTime<=8000){
			        seekTime+=arrayOfSpeed[3];
			     }else if(mKeyEventEslapeTime>8000&&mKeyEventEslapeTime<=10000){
			        seekTime+=arrayOfSpeed[4];
			     }else if(mKeyEventEslapeTime>10000&&mKeyEventEslapeTime<=12000){
			        seekTime+=arrayOfSpeed[5];
			     }else if(mKeyEventEslapeTime>12000&&mKeyEventEslapeTime<=14000){
			        seekTime+=arrayOfSpeed[6];
			     }else if(mKeyEventEslapeTime>14000&&mKeyEventEslapeTime<=16000){
			        seekTime+=arrayOfSpeed[7];
			     }else if(mKeyEventEslapeTime>16000){
			        seekTime+=arrayOfSpeed[8];
			     }
				 
	            if(mShowing&&(StatusManager.getStatus()==Status.SEEKING)){
					if(mIsRight){
						if((!mIsVideoFree)&&(!mVideoPayed)){
							if(mPosition+seekTime>=mPlayLimitTime){
								setIndicatorProgress(mPlayLimitTime);
							}else{
								setIndicatorProgress(mPosition+seekTime);
							}
						}else{
							if(mPosition+seekTime>=mDuration){
								setIndicatorProgress(mDuration-5*1000);
							}else{
								setIndicatorProgress(mPosition+seekTime);
							}
						}
					}else{
						if(mPosition-seekTime<=0){
							setIndicatorProgress(0);
						}else{
							setIndicatorProgress(mPosition-seekTime);
						}
					}
				}
				sendDelayMessage(UPDATE_SEEKING,200,false);
				break;
			case AD_ACTION:
				if(!mAdPlayEnd){
					try{
						int adDownCount=mPlayer.getAdDownCount();
	        			if(adDownCount<saveAdDownCount){
	        				showAdTimming(String.valueOf(adDownCount));
	        			}
	        			saveAdDownCount=adDownCount;
	        			sendDelayMessage(AD_ACTION,1000,false);
	        			Log.i(TAG," 播放广告： 剩余="+mPlayer.getAdDownCount());
	        		}catch(Exception e){
	        			Log.e(TAG," receiver exception:"+e.getMessage());
	        		}
				}
				break;
			case NET_SPEED:
				if(StatusManager.getStatus()==Status.PREPARING||StatusManager.getStatus()==Status.BUFFERING){
					try{
						nowTimeStamp=System.currentTimeMillis();
	        			nowTotalRxBytes=TrafficStats.getTotalRxBytes()/1024;
	            		long speed = ((nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp));
	            		lastTimeStamp=nowTimeStamp;
	            		lastTotalRxBytes=nowTotalRxBytes;
	            		Log.i(TAG," now download speed is "+speed+"KB/s");
	            		mSpeedText.setText("下载速度 ："+speed+"KB/s");
	            		sendDelayMessage(NET_SPEED,500,false);
	    			}catch(Exception e){
	        			Log.e(TAG," receiver exception:"+e.getMessage());
	        		}
				}
				break;
			case CHECK_PAUSE_PLAY:
				Bundle b=msg.getData();
				int pos=b.getInt("pos");
				int p=mPlayer.getCurrentPosition();
				String cmd=b.getString("cmd");
				Status s=StatusManager.getStatus();
				Log.i(TAG," mHandler CHECK_PAUSE_PLAY, pos="+pos+" cmd="+cmd+" Status="+s);
				if(s==Status.PLAYING){
					if(cmd.equals(CMD_PAUSE)){
						Log.e(TAG," Status error, now Status is PLAYING but cmd is  PAUSE!!!");
						mPlayer.pause();
						mCenterImage.setVisibility(View.VISIBLE);
						StatusManager.setStatus(Status.PAUSE);
						showOverlay(OVERLAY_TIMEOUT,false); 
					}else{
						Log.i(TAG," play success now position="+p+" cmd position="+pos);
					}
				}else if(s==Status.PAUSE){
					if(cmd.equals(CMD_PAUSE)){
						if(p==pos){
							Log.i(TAG," pause success");
						}else{
							Log.e(TAG," pause error, change status to PLAYING");
							mPlayer.start();
							mCenterImage.setVisibility(View.GONE);
							StatusManager.setStatus(Status.PLAYING);
						}
					}else if(s==Status.BUFFERING){
						Message msg8=new Message();
						msg8.what=CHECK_PAUSE_PLAY;
						msg8.setData(b);
						sendMessageDelayed(msg8,500);
						Log.i(TAG,"  buffering , send msg again for check pause&play");
					}
				}
				break;
			case FINISH:
				finish();
				break;
			case BUFFERING:
				bufferingTimeout();
				break;
			case AUTH_COMPLETE:
				String  url=msg.getData().getString("url");
				mPlayer.setDataSource(url,mVideoInfo.getPrice().equals("0"),mPlayLimitTime/1000);
				break;
			case LOADING:
				loadingTimeout();
				break;
			case SILO:
				siloStartPlay();
				break;
			}
		}
	};	
	
	private void setIndicatorProgress(int progress){
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)mIndicator.getLayoutParams();
		if(mDuration==0){
			try{
				mDuration=mPlayer.getDuration();
			}catch(Exception e){
				Log.e(TAG," get duration exception:"+e.getMessage());
			}
		}
		mIndicatorText.setText(mTimePro.getTimeString(progress));
		float rate=(float)progress/(float)mDuration;
		int left=(int)(1540*rate+93);
		if(left>1650){
			left=1650;
		}
		Log.i(TAG,"progress="+progress+" margin left="+left);
		params.setMargins(left, 160, 0, 0);
		mIndicator.requestLayout();
	}
	
	public void longPress(int keyCode, KeyEvent event){
		Log.i(TAG," onKeyLongPress called");
		
		if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT||keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
			if(!mLongPressed){
				mLongPressed=true;
				if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
					mIsRight=false;
				}else{
					mIsRight=true;
				}
				seekTime=0;
				StatusManager.setStatus(Status.SEEKING);
				try{
					int pos=mPlayer.getCurrentPosition();
					mPosition=pos;
				}catch(Exception e){
					Log.e(TAG, " get positon error in long press");
				}
				mKeyEventStartTime=System.currentTimeMillis();
				//如果为乐视，不显示中间的快进图标
				if(!mVideoInfo.getVendor().equals("LETV")){
					mCenterImage.setVisibility(View.VISIBLE);
	    			if(mIsRight){
	    				mCenterImage.setBackgroundResource(R.drawable.ic_player_speed);
	    			}else{
	    				mCenterImage.setBackgroundResource(R.drawable.ic_player_fastreverse);
	    			}
	    			mIndicator.setVisibility(View.VISIBLE);
	    			setIndicatorProgress(mPosition);
				}
				Log.i(TAG," long pressed keycode="+keyCode+" isright="+mIsRight+" start time="+mKeyEventStartTime);
				Message msg=new Message();
				msg.what=UPDATE_SEEKING;
				mHandler.sendMessage(msg);
			}
		}
	}
	
	protected void bufferingTimeout() {
		// TODO Auto-generated method stub
		Log.e(TAG," exit player for buffering timeout");
		StatusManager.setStatus(Status.ERROR);
		showToast("缓冲超时，请检查网络，正在退出播放器...",true);
	}

	protected void loadingTimeout() {
		// TODO Auto-generated method stub
		Log.e(TAG," exit player for loading timeout");
		StatusManager.setStatus(Status.ERROR);
		showToast("加载网络数据超时，请检查网络，正在退出播放器...",true);
	}

	private void hideOverlay(boolean force) {
		if (mShowing) {
			if(!force){
				if(StatusManager.getStatus()==Status.PAUSE){
					return;
				}
			}
			if(StatusManager.DEBUG) Log.i(TAG, "remove View!");
			mOverlayHeader.startAnimation(AnimationUtils.loadAnimation(this,R.anim.up_translate));
			mOverlayHeader.setVisibility(View.INVISIBLE);
			//乐视为直播模式，不要显示进度条
			if(!mVideoInfo.getVendor().equals("LETV")){
				mOverlayBottom.startAnimation(AnimationUtils.loadAnimation(this,R.anim.down_translate));
				mOverlayBottom.setVisibility(View.INVISIBLE);
			}
			mShowing = false;
			if((!mIsVideoFree)&&(!mVideoPayed)){
				Log.i(TAG," vip video and not payed, show limit text when top bar dismiss");
				mPlayLimitText.setVisibility(View.VISIBLE);
			}
		}
	}
	
	private void showOverlay(int timeout,boolean force) {
		if(StatusManager.DEBUG) Log.i(TAG,"showOverlay called");
		if (!mShowing) {
			mShowing = true;
			try{
				int progress = mPlayer.getCurrentPosition();
				mPosition=progress;
				mProgressBar.setProgress(mPosition);
				mProgressText.setText(mTimePro.getTimeString(mPosition));
			}catch(Exception e){
				Log.e(TAG,"get position error in showOverlay");
			}
			mTimeText.setText(mTimePro.getTime());
			mOverlayHeader.setVisibility(View.VISIBLE);
			//乐视为直播模式，不显示进度条 
			if(!mVideoInfo.getVendor().equals("LETV")){
				mOverlayBottom.setVisibility(View.VISIBLE);
			}
			
			if((!mIsVideoFree)&&(!mVideoPayed)){
				Log.i(TAG," vip video and not payed, hide limit text for show top bar");
				mPlayLimitText.setVisibility(View.INVISIBLE);
			}
		}
		Message msg = mHandler.obtainMessage(FADE_OUT);
		if (timeout != 0) {
			mHandler.removeMessages(FADE_OUT);
			Bundle data=new Bundle();
			data.putBoolean("force", force);
			msg.setData(data);
			mHandler.sendMessageDelayed(msg, timeout);
		}
	}
	
	private void showAdTimming(String seconds ){
		dismissLoadingView();
		StatusManager.setStatus(Status.PLAY_AD);
		mAdTimming.setVisibility(View.VISIBLE);
		int s=Integer.parseInt(seconds);
		if(s<0){
			s=0;
		}
		String text="广告 "+s+"秒";
		mAdText.setText(text);
	}
	
	public boolean dispatchKeyEvent(KeyEvent event){
		int keyCode=event.getKeyCode();
		mKeyRepeat=event.getRepeatCount();
		Status s=StatusManager.getStatus();
		if(StatusManager.DEBUG) Log.i(TAG,"dispatch key event called,keyCode="+keyCode+" repeat="+event.getRepeatCount()+" action="+event.getAction()+" status="+s);
		if(s==Status.PLAY_AD){
			return super.dispatchKeyEvent(event);
		}
		
		if(mMenuShowing){
			if(event.getAction()==KeyEvent.ACTION_DOWN){
				if(keyCode==KeyEvent.KEYCODE_MENU&&mKeyRepeat==0){
					dismissMenuView();
					return true;
				}
			}
			return mMenuView.dispatchKeyEvent(event);
		}
		
		if(mPreViewCompleteShowing){
			if((keyCode==KeyEvent.KEYCODE_BACK&&mKeyRepeat==0)){
				finish();
			}
			return super.dispatchKeyEvent(event);
		}
		
		if(event.getAction()==KeyEvent.ACTION_DOWN){
			if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT||keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
				if(!canSeekable()){
					return super.dispatchKeyEvent(event);
				}
				showOverlay(OVERLAY_TIMEOUT,false); 
				if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
					mIsRight=false;
				}else{
					mIsRight=true;
				}
				if(mKeyRepeat==0){
					
				}else{
					longPress(keyCode,event);
				}
			}else if(keyCode==KeyEvent.KEYCODE_ENTER||keyCode==23||keyCode==KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE){
				if(0==mKeyRepeat){
					if(s==Status.PLAYING){
						pause();
					}else if(s==Status.PAUSE){
						play();
					}
					showOverlay(OVERLAY_TIMEOUT,false);
				}
			}else if(keyCode==KeyEvent.KEYCODE_DPAD_UP||keyCode==KeyEvent.KEYCODE_DPAD_DOWN){
				showOverlay(OVERLAY_TIMEOUT,false);
			}else if(keyCode==KeyEvent.KEYCODE_BACK){
				if(mShowing){
					hideOverlay(true);
					return true;
				}else{
					long secondTime=System.currentTimeMillis();
					Log.i(TAG," back event called,secondTime="+secondTime+" mBackEventStartTime"+mBackEventStartTime);
					if(secondTime-mBackEventStartTime>BACK_EVENT_VALID_TIME){
						showToast("再按一次返回，将退出播放。",false);
						mBackEventStartTime=secondTime;
						return true;
					}else{
						Log.i(TAG,"  exit player by user!!");
						mExitByUser=true;
					}
				}
			}else if(keyCode==KeyEvent.KEYCODE_MENU&&mKeyRepeat==0){
				Log.i(TAG," show menu dialog in dispatch keyevent");
				if(canShowMenu()){
					showMenuView();
				}
			}
		}else if(event.getAction()==KeyEvent.ACTION_UP){
			if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT||keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
				try{
					if(!mLongPressed){
						int curPosition=mPlayer.getCurrentPosition();
						if(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT){
							seek(curPosition+30*1000);
						}else{
							seek(curPosition-30*1000);
						}
					}else{
						Log.i(TAG,"dispatch keyevent action up and seek ");
						seekEnd();
						mLongPressed=false;
						if(mHandler.hasMessages(UPDATE_SEEKING)){
							mHandler.removeMessages(UPDATE_SEEKING);
						}
					}
				}catch(Exception e){
					Log.e(TAG," get current position error in dispatch keyEnvent");
				}
			}
		}
		return super.dispatchKeyEvent(event);
	}
	
	private void sendCheckPausePlayMsg(String cmd){
		int pos;
		try{
			pos=mPlayer.getCurrentPosition();
			Log.i(TAG," getCurrentPosition pos="+pos);
		}catch(Exception e){
			pos=mPosition;
			Log.i(TAG," getCurrentPosition exception, use mPosition="+pos);
		}
		if(mHandler.hasMessages(CHECK_PAUSE_PLAY)){
			mHandler.removeMessages(CHECK_PAUSE_PLAY);
		}
		Message msg=new Message();
		msg.what=CHECK_PAUSE_PLAY;
		Bundle bundle= new Bundle();
		bundle.putString("cmd",cmd);
		bundle.putInt("pos", pos);
		msg.setData(bundle);
		mHandler.sendMessageDelayed(msg,500);
		Log.i(TAG," send play/pause msg, pos="+pos+" cmd="+cmd);
	}
	
	private boolean canShowMenu() {
		// TODO Auto-generated method stub
		Status s=StatusManager.getStatus();
		if(s==Status.PLAY_AD||s==Status.PREPARING||s==Status.ERROR){
			return false;
		}
		return true;
	}
	
	private void dismissMenuView(){
		mMenuShowing=false;
		if(mMenuView!=null){
			mRootView.removeView(mMenuView);
			mMenuView=null;
		}
	}
	
	private void showMenuView(){
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = (MenuRelativeLayout)inflater.inflate(R.layout.menu, null);
		mRootView.addView(mMenuView);
		mMenuShowing=true;
		mMenuView.init(mMenuView,mVideoInfo,PlayListManager.getDisplaySize(),PlayListManager.getPlayCrop(),mVideoInfo.getResolution());	
		mMenuView.showFirstMenu();
		mMenuView.setChangeListener(this);
	}

	private boolean canSeekable(){
		Status s= StatusManager.getStatus();
		return s==Status.PAUSE||s==Status.PLAYING||s==Status.SEEKING||s==Status.BUFFERING;
	}
	
	private void pause(){
		if(StatusManager.DEBUG) Log.i(TAG,"pause!");
		try{
			if(mPlayer.isPlaying()){
				if(!mVideoInfo.getVendor().equals("LETV")){
					mCenterImage.setVisibility(View.VISIBLE);
					mCenterImage.setBackgroundResource(R.drawable.ic_player_play);
					StatusManager.setStatus(Status.PAUSE);
					sendCheckPausePlayMsg(CMD_PAUSE);
				}
				mPlayer.pause();
			}
		}catch(Exception e){
			Log.e(TAG," pasue error:"+e.getMessage()+" and cause is "+e.getCause());
		}
	}
	
	private void play(){
		if(StatusManager.DEBUG) Log.i(TAG,"play!");
		mPlayer.start();
		StatusManager.setStatus(Status.PLAYING);
		mCenterImage.setVisibility(View.GONE);
		sendCheckPausePlayMsg(CMD_PLAY);
	}
	
	private void seek(int sTime){
		if(StatusManager.DEBUG) Log.i(TAG,"seek to "+sTime+" and Duration="+mDuration);
		if(mCenterImage.getVisibility()==View.VISIBLE){
			Log.i(TAG," center Image is showing when seek, dismiss now!");
			mCenterImage.setVisibility(View.INVISIBLE);
		}
		int time = 0;
		
		if((!mIsVideoFree)&&(!mVideoPayed)){
			if(sTime<0){
				time=0;
			}else if(sTime>mPlayLimitTime-5*1000){
				time=mPlayLimitTime;
			}else{
				time=sTime;
			}
		}else{
			if(sTime<=0){
				time=0;
			}else if(sTime>mDuration){
				time=mDuration-5*1000;
			}else{
				time=sTime;
			}
		}
		
		mSeekStartTime=System.currentTimeMillis();
		mPlayer.seek(time);
	}

	private  void showToast(String text,boolean ifLong){
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.toast, null);
		TextView textView=(TextView)view.findViewById(R.id.text_toast);
		textView.setText(text);
	    mToast=new Toast(this);
	    if(ifLong){
	    	mToast.setDuration(Toast.LENGTH_LONG);
	    }else{
	    	mToast.setDuration(Toast.LENGTH_SHORT);
	    }
	    mToast.setGravity(Gravity.CENTER_VERTICAL, 0, 378);
	    mToast.setView(view);
	    mToast.show();
	    if(StatusManager.getStatus()==Status.ERROR){
	    	sendDelayMessage(FINISH,ERROR_DIALOG_SHOW_TIME,false);
		}
	}
	
	public void seekEnd() {
		// TODO Auto-generated method stub
		if(StatusManager.DEBUG) Log.i(TAG," seek end called seekTime="+seekTime+" flag="+mIsRight+" position="+mPosition);
		if(mIsRight){
			seek(mPosition+seekTime);
		}else{
			seek(mPosition-seekTime);
		}
		StatusManager.setStatus(Status.PLAYING);
		mIndicator.setVisibility(View.GONE);
		mCenterImage.setVisibility(View.GONE);
	}
	
	/**
	 * 广告播放完成时所需要做的处理
	 */
	@Override
	// TODO Auto-generated method stub
	public void onAdEnd() {
		Log.i(TAG," onAdEnd called!!!");
		mAdPlayEnd=true;
		mAdTimming.setVisibility(View.INVISIBLE);
		
		if(1==mAdPlayCnt){   //第一次播放的广告，表示前贴片广告
			StatusManager.setStatus(Status.PREPARING);
			sendDelayMessage(NET_SPEED,500,true);
			sendDelayMessage(LOADING,LOADING_TIMEOUT,true);
		}else{
			StatusManager.setStatus(Status.PLAYING);
		}
	}
	
	private void sendDelayMessage(int what,int delaytime,boolean remove){
		Log.i(TAG," sendDelayMesage  what="+what+" delaytime="+delaytime+" remove="+remove);
		if(remove&&mHandler.hasMessages(what)){
			mHandler.removeMessages(what);
		}
		
		Message msg=new Message();
		msg.what=what;
		mHandler.sendMessageDelayed(msg, delaytime);
	}
	
	@Override
	public void onAdStart() {
		// TODO Auto-generated method stub		
		Log.i(TAG,"onAdStart called");
		mAdPlayCnt++;
		mRootView.removeView(mBackground);
		//mBackground.setBackgroundColor(0x00000000);
		if(mHandler.hasMessages(LOADING)){
			mHandler.removeMessages(LOADING);
		}
		dismissLoadingView();
		StatusManager.setStatus(Status.PLAY_AD);
		sendDelayMessage(AD_ACTION,10,true);
		mAdPlayEnd=false;
	}

	/**
	 * 直播的时候不支持快进、快退、暂停等
	 */
	@Override
	public void actionNotSupport(String action) {
		// TODO 自动生成的方法存根
		Log.i(TAG," actionNotSurpprot called, action="+action);
		if(action.equals("seek")){
			showToast("直播模式下无法快进/快退,请耐心欣赏精彩节目！",false);
		}else{
			showToast("主人，直播模式下无法暂停！",false);
		}
	}
	
	@Override
	public void onBitStreamListReady(List<Definition> urls) {
		// TODO Auto-generated method stub	
		HashMap<String,String> map=new HashMap<String,String>();
		if(urls.contains(Definition.DEFINITON_1080P)){
			map.put(Config.BD, "1080p");
		}else{
			map.put(Config.BD, Config.NONE_URL);
		}
		
		if(urls.contains(Definition.DEFINITON_720P)){
			map.put(Config.FHD, "720p");
		}else{
			map.put(Config.FHD, Config.NONE_URL);
		}
		
		if(urls.contains(Definition.DEFINITON_HIGH)){
			map.put(Config.HD, "hd");
			//map.put(Config.SD, "sd");
		}else{
			map.put(Config.HD, Config.NONE_URL);
			//map.put(Config.SD, Config.NONE_URL);
		}
		map.put(Config.SD, Config.NONE_URL);
		mVideoInfo.setUrls(map);
	}

	@Override
	public void onBufferEnd() {
		// TODO Auto-generated method stub
		if(StatusManager.DEBUG) Log.i(TAG,"onInfo: buffering end");
		//上报video_buffering的日志。
		mVideoInfo.reportVideoBuffering(this,mBufferStartTime, System.currentTimeMillis());
		mBufferStartTime=-1;
		if(mHandler.hasMessages(BUFFERING)){
			mHandler.removeMessages(BUFFERING);
		}
		
		Status s=StatusManager.getStatus();
		if(!(s==Status.PLAY_AD)){
			StatusManager.setStatus(Status.PLAYING);
		}
		dismissLoadingView();
	}

	@Override
	public void onBufferStart() {
		// TODO Auto-generated method stub
		if(StatusManager.DEBUG) Log.i(TAG,"onInfo: buffering start");
		Status s=StatusManager.getStatus();
		if(s==Status.PLAY_AD||mMenuShowing){
			return;
		}
		StatusManager.setStatus(Status.BUFFERING);
		mBufferStartTime=System.currentTimeMillis();
		showLoadingView(false,false);
		//设置的超时检测
		sendDelayMessage(BUFFERING,BUFFERING_TIMEOUT,false);
		sendDelayMessage(NET_SPEED,500,true);
	}
	
	//底层播放出现错误时的处理逻辑
	@Override
	public boolean onError( int what, String extra) {
		// TODO Auto-generated method stub
		String message;
		Log.e(TAG,"Error: what="+what+" extra="+extra+" and mRetryCout="+mRetryCount);
		
		if(mRetryCount==0){
			retry();
			return false;
		}
		
		String errorMsg="错误代码：("+what+","+extra+")"+"  描述信息：";
		switch(what){
		case MediaPlayer.MEDIA_ERROR_IO:
			message="加载网络数据出错,请检查网络,正在退出播放器...";
			break;
		case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
			message="操作超时，正在退出播放器...";
			break;
		case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
			message="解析数据格式出错,正在退出播放器...";
			break;
		default:
			message="未知错误，正在退出播放器...";
			break;
		}
		errorMsg+=message;
		StatusManager.setStatus(Status.ERROR);
		mVideoInfo.reportVideoError(this, mPosition,""+what,extra,message);
		showToast(errorMsg,true);
		return false;
	}
	
	@Override
	public boolean onError(IPlaybackInfo video, ISdkError error) {
		// TODO 自动生成的方法存根
		String what=error.getCode();
		String errorMsg;
		Log.e(TAG,"qiyi onError: what="+what+" extra="+error.getMsgFromError()+" and mRetryCout="+mRetryCount);
		
		if(mRetryCount==0){
			retry();
			return false;
		}
		if(what!=null&&what.equals("A00110")){
			errorMsg="抱歉，此片源已下线，退出播放器...";
		}else{
			errorMsg="错误代码：("+error.getCode()+","+error.getMsgFromError()+")"+"  描述信息：未知错误";
		}
		StatusManager.setStatus(Status.ERROR);
		showToast(errorMsg,true);
		mVideoInfo.reportVideoError(this, mPosition, error.getCode(), error.getMsgFromError(), " ");
		return false;
	}

	@Override
	public void onPreviewInfoReady(boolean isPreview, int limitTime) {
		// TODO 自动生成的方法存根
		Log.i(TAG," Qiyi onPreviewInfoReady called,isPreview="+isPreview+" limitTime="+limitTime);
		mVideoPayed=mIsVideoFree=isPreview?false:true;
		mPlayLimitTime=limitTime*1000;
	}
	
	//第三方自身逻辑上的错误	
	@Override
	public void onVodError(VodError error) {
		// TODO 自动生成的方法存根
		String errorMsg="错误代码："+error.getErrorCode()+"  描述信息："+error.getExtra();
		StatusManager.setStatus(Status.ERROR);
		mVideoInfo.reportVideoError(this, mPosition, error.getErrorCode(), error.getExtra()," ");
		showToast(errorMsg,true);
	}

	private void retry() {
		// TODO Auto-generated method stub
		mRetryCount++;
		startPlay(mAdPlayEnd,mPosition);
		StatusManager.setStatus(Status.PREPARING);
	}

	@Override
	public void onHeaderTailerInfoReady(int headerTime, int tailerTime) {
		// TODO Auto-generated method stub		
		Log.i(TAG," onHeaderTailerInfoReady called, headerTime="+headerTime+" tailerTime="+tailerTime);
		mHeaderTime=headerTime*1000;
		mTailerTime=tailerTime*1000;
	}

	@Override
	public void onMovieComplete() {
		if(StatusManager.DEBUG) Log.i(TAG,"play completed ");
		mIsCompleted=true;
		if((!mIsVideoFree)&&(!mVideoPayed)){
			reportVideoExit();											//预览结束，上报中途退出
		}else{
			reportVideoEnd();											//完整的播放结束，上报结束的log
		}
		dismissMenuView();
		if(mPlayList.hasNext()){
			Log.i(TAG," paly next video");
			mHeaderTime=0;
			mTailerTime=0;
			mAdPlayCnt=0;
			mSkipTipShowed=false;
			mSeekSkipHeader=false;
			mPlayLimitTime=PREVIEW_DEFAULT_TIME_FOR_VIP_VIDEO;
			mIsVideoFree=true;
			mAdPlayEnd=false;
			mVideoPayed=false;
			hideOverlay(true);
			mPlayer.stop();
			mPosition=0;
			boolean reset=mPlayList.shouldResetPlayerAndMoveToNext();
			mVideoInfo=mPlayList.getCurrentVideo();
			if(reset){
				PlayListManager.resetPlayer();
			}
			mPlayer=mVideoInfo.getPlayer(mVideoContainer,this,this);
			mTip=TIP_NO_MESSAGE;
			checkSkipHeaderAndTip(0);
			mPlayer.setDataSource(mVideoInfo,false,0);
			StatusManager.setStatus(Status.PREPARING);
			sendDelayMessage(NET_SPEED,500,true);
			showLoadingView(true,true);
		}else{
			if((!mIsVideoFree)&&(!mVideoPayed)){
				Log.i(TAG," preview mode, not finish for showing buy dialog");
			}else{
				finish();
			}
		}
	}

	@Override
	public void onMoviePause() {
		// TODO Auto-generated method stub
	}

	@Override
	public void onMovieStart() {
		// TODO Auto-generated method stub
		Log.i(TAG," onMoiveStart called ");
		if(mVideoInfo.getVendor().equals("WASU")){
			videoStart();
		}
		
		if(mVideoInfo.getVendor().equals("QIYI")&&mAdPlayEnd){
			videoStart();
		}
	}

	private void videoStart() {
		// TODO Auto-generated method stub
		Log.i(TAG," now moive start");
		mRootView.removeView(mBackground);
		//mBackground.setBackgroundColor(0x00000000);
		dismissLoadingView();
		mIsCompleted=false;
		if(mHandler.hasMessages(LOADING)){
			mHandler.removeMessages(LOADING);
		}
		mDuration=mPlayer.getDuration();
		Log.i(TAG," videoStart getDuration="+mDuration);
		if((!mIsVideoFree)&&(!mVideoPayed)){
			Log.i(TAG," this video is vip and not payed, play in preview mode");
			showPlayLimitView();
		}
		mDurationText.setText(mTimePro.getTimeString(mDuration));
		mProgressBar.setMax(mDuration);
		Log.i(TAG," videoStart called, mSeekSkipHeader="+mSeekSkipHeader+" mResolutionChanging="+mResolutionChanging);
		
		if(mSeekSkipHeader){
			if(mHeaderTime>0&&(!mResolutionChanging)){
				Log.i(TAG," video start seekto header time:"+mHeaderTime);
				mPlayer.seekWhenPrepared(mHeaderTime);
			}else{
				Log.i(TAG," video start  seekto 0 for mHeaderTime=0 or is changing resolution ");
				mPlayer.seekWhenPrepared(0);
			}
		}else{
			Log.i(TAG," video start  seekto 0 for not skipHeader ");
			mPlayer.seekWhenPrepared(0);
		}
		mPlayer.setDisPlaySize(PlayListManager.getDisplaySize());
		mPlayer.start();
		
		//上报video_start的日志
		reportVideoStart();		
		showOverlay(OVERLAY_TIMEOUT,false);
		sendDelayMessage(UPDATE_PROGRESS_PLAYING,1000,true);
		updateViews(mVideoInfo);
		StatusManager.setStatus(Status.PLAYING);
		if((!mTip.equals(TIP_NO_MESSAGE))&&(!mResolutionChanging)){
			showToast(mTip,false);
		}
		mResolutionChanging=false;
		if(mCenterImage.getVisibility()==View.VISIBLE){
			Log.i(TAG," center Image is showing when video start, dismiss now!");
			mCenterImage.setVisibility(View.INVISIBLE);
		}
		mOnStartCalled=false;
	}

	private void reportVideoStart(){
		mVideoInfo.reportVideoStart(this,PlayListManager.getPayType(),PlayListManager.getPlayType());
	}
	
	private void reportVideoExit(){
		mVideoInfo.reportVideoExit(this,mPosition,PlayListManager.getPayType(),PlayListManager.getPlayType());
	}
	
	private void reportVideoEnd(){
		mVideoInfo.reportVideoEnd(this,PlayListManager.getPayType(),PlayListManager.getPlayType());
	}
	
	@Override
	public void onMovieStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlaybackBitStreamSelected(Definition arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPrepared() {
		Log.i(TAG,"onPrePared called and Status="+StatusManager.getStatus());
		//mBackground.setBackgroundColor(0x00000000);
		mRootView.removeView(mBackground);
		if(!(StatusManager.getStatus()==Status.PLAY_AD)){
			videoStart();
		}else{
			mPlayer.start();
		}
	}

	@Override
	public void onSeekComplete() {
		// TODO Auto-generated method stub
		Status s=StatusManager.getStatus();
		if(StatusManager.DEBUG) Log.i(TAG," onSeekCompleted called and Status="+s);
		//上报video_seek日志		
		if((s!=Status.PLAY_AD)&&(s!=Status.PREPARING)){
			showOverlay(OVERLAY_TIMEOUT,false);
		}
		mVideoInfo.reportVideoSeek(this, mSeekStartTime, System.currentTimeMillis());
		mSeekStartTime=-1;
	}

	@Override
	public void onVideoSizeChange(int arg0, int arg1) {
		// TODO Auto-generated method stub		
	}
	
	private void showPlayLimitView() {
		// TODO 自动生成的方法存根
		Log.i(TAG," showPlayLimitView called ");
		int previewMinute=mPlayLimitTime/60000;
		mPlayLimitText.setText("您可以试看"+previewMinute+"分钟，购买后仍能继续观看。");
		mPlayLimitText.setVisibility(View.VISIBLE);
	}
	 
	private void dismissPlayLimitView() {
		// TODO 自动生成的方法存根		
		mPlayLimitText.setVisibility(View.GONE);
	}

	//奇异的预览完成的回调 
	@Override
	public void onPreviewCompleted() {
		// TODO 自动生成的方法存根
		Log.i(TAG," qiyi preview complete,set the result, mPosition="+mPosition+"  mPlayLimitTime="+mPlayLimitTime);
		if(!mExitByUser){
			Log.i(TAG," preview complete!");
			//setQiyiResult(RESULT_PREVIEW_END);
			showPreviewCompleteView();
		}else{
			Log.i(TAG," preview exit by user!!");
			setQiyiResult(RESULT_PREVIEW_OTHER);
			
		}
		mExitByUser=false;
	}
	
	private void showPreviewCompleteView(){
		if(mMenuShowing){
			Log.i(TAG," dismiss menu view when preview complete");
			dismissMenuView();
		}
		mPreViewCompleteShowing=true;
		LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout preview = (RelativeLayout)inflater.inflate(R.layout.preview, null);
		Button buy=(Button)preview.findViewById(R.id.buy);
		Button cancel=(Button)preview.findViewById(R.id.cancel);
		buy.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.i(TAG," now buy clicked,show pay UI ");
				setQiyiResult(RESULT_PREVIEW_END);
				finish();
			}
		});
		
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				Log.i(TAG," cancle!!!");
				setQiyiResult(RESULT_PREVIEW_CANCLE);
				finish();
			}
		});
		buy.requestFocus();
		mRootView.addView(preview);
	}
	
	@Override
	public void onPlayLimitedPreView(Integer limitTime) {
		// TODO 自动生成的方法存根
		Log.i(TAG," onPlayLimitedPreView called and limitTime="+limitTime);
		mIsVideoFree=false;
		if(limitTime>0){
			mPlayLimitTime=limitTime;
		}
	}

	@Override
	public void onPlayLimitedPayed() {
		// TODO 自动生成的方法存根
		showToast("支付完成，可成功观看",true);
		mPlayer.start();
		mVideoPayed=true;
		dismissPlayLimitView();
	}

	@Override
	public void onPlayLimitedExceed() {
		// TODO 自动生成的方法存根
		//showToast("此影片为收费影片,请在支付后观看！",true);
		mIsVideoFree=false;
		mPlayer.pause();
	}
	
	@Override
	public void onPayResult(String description) {
		// TODO Auto-generated method stub
		Log.i(TAG," onPayResult called msg="+description);
		showToast(description,true);
	}
	
	@Override
	public void onAuthCompleted(String Url) {
		// TODO Auto-generated method stub
		Log.i(TAG," onAuth completed send msg");
		Message msg = mHandler.obtainMessage(AUTH_COMPLETE);
		Bundle data=new Bundle();
		data.putString("url", Url);
		msg.setData(data);
		mHandler.sendMessage(msg);
	}
	
	@Override
	public void onFirstFrameStart() {
		// TODO Auto-generated method stub
		Log.i(TAG," the first frame start!!!!!");
	}
	
	@Override
	public void displaySizeChanged(DisplaySize size) {
		// TODO 自动生成的方法存根
		mPlayer.setDisPlaySize(size);
		PlayListManager.setDisplaySize(size);
		dismissMenuView();
	}

	@Override
	public void playCropChanged(PlayCrop crop ) {
		// TODO 自动生成的方法存根
		PlayListManager.setPlayCrop(crop);
		if(crop==PlayCrop.SKIP){
			if(mIsVideoFree){
				mSeekSkipHeader=true;
			}
		}else{
			mSeekSkipHeader=false;
		}
		dismissMenuView();
	}
	
	@Override
	public void dismissMenu() {
		// TODO 自动生成的方法存根
		dismissMenuView();
	}

	@Override
	public void notSupportSkipHeader() {
		// TODO 自动生成的方法存根
		showToast("抱歉，此片源不支持跳过片头片尾！",false);
	}
	
	@Override
	public void onLetvStatusChange(String status) {
		// TODO 自动生成的方法存根
		Log.i(TAG,"onLetvStatusChange called,status="+status);
		String message;
		if(status.equals(LetvVideoView.STATUS_INVALID)){
			message="无效的直播节目，退出播放器...";
			StatusManager.setStatus(Status.ERROR);
		}else if(status.equals(LetvVideoView.STATUS_OVER)){
			message=" 直播结束，退出播放...";
		}else {
			Log.i(TAG," other status, do nothing!!!");
			return;
		}
		showToast(message,true);
	}
	
	@Override
	public void resolutionChanged(String res) {
		// TODO 自动生成的方法存根
		mVideoInfo.reportVideoResolutionChange(this, PlayListManager.getResolution(), res);
		mResolutionChanging=true;
		PlayListManager.setResolution(res,mVideoInfo);
		mPlayer.setResolution(res);
		dismissMenuView();
		showToast("清晰度切换中，请稍候...",true);
		showLoadingView(false,false);
		StatusManager.setStatus(Status.PREPARING);
		sendDelayMessage(NET_SPEED,500,true);
	}
	
	static
	{
		System.loadLibrary("startloginsrv");
	}
}
