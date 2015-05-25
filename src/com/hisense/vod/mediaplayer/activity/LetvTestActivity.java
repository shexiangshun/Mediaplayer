package com.hisense.vod.mediaplayer.activity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;





import com.hisense.vod.R;
import com.letv.livesdk.LetvLiveSdkManager;
import com.letv.livesdk.OnLiveProgramUpdateListener;
import com.letv.livesdk.OnSdkStateChangeListener;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class LetvTestActivity extends Activity {
	private static final String TAG="LetvTestActivity";
	private int mCurrentState=LetvLiveSdkManager.LIVE_SDK_STATE_UNINITIALIZED;
	private String mLiveId="3020150423165041";
	private boolean mPayClicked=false;
	private boolean mPlayClicked=false;
	private boolean mSdkPrepared=false;
	private int mRefreshTime=0;
	private TextView mTextStatus,mTextPay,mTextPrice;
	
	
	private OnSdkStateChangeListener mSdkListener=new OnSdkStateChangeListener(){

		@Override
		public void onStateChanged() {
			// TODO 自动生成的方法存根
			mCurrentState=LetvLiveSdkManager.getCurrentState();
			Log.i(TAG," letv sdk state changed, and mCurrentState="+mCurrentState);
			if(mCurrentState==LetvLiveSdkManager.LIVE_SDK_STATE_INITIALIZING){
				mTextStatus.setText("SDK状态：初始化中。。。");
				Log.i(TAG," letv sdk inializing");
			}else if(mCurrentState==LetvLiveSdkManager.LIVE_SDK_STATE_PREPARED){
				Log.i(TAG," letv sdk prepared, check if program available ");
				mTextStatus.setText("SDK状态：初始化完成！");
				mSdkPrepared=true;
				updateViewAndPlay(mPlayClicked);
				if(mPlayClicked){
					Log.i(TAG," play button already prepared, check if payed and play!!!");
					mPlayClicked=false;
				}
			}else if(mCurrentState==LetvLiveSdkManager.LIVE_SDK_STATE_ERROR){
				Log.i(TAG," letv sdk init error!");
				mTextStatus.setText("SDK状态：初始化失败！");
			}
		}
	};
	
	private OnLiveProgramUpdateListener mUpdateListener=new OnLiveProgramUpdateListener(){
		@Override
		public void onProgramUpdate() {
			// TODO 自动生成的方法存根
			if(!LetvLiveSdkManager.isProgramAvailable(mLiveId)){
				//boolean ok = LetvLiveSdkManager.refreshProgramList();
				Log.i(TAG," program is not available again, refresh!=====>");
				return;
			}			
			updateViewAndPlay(false);
			if(mPayClicked){
				boolean isPayed=LetvLiveSdkManager.isProgramPurchased(mLiveId);
				if(isPayed){
					mTextPay.setText("支付状态：已支付");
					Toast.makeText(LetvTestActivity.this, "支付成功，可以正常观看", Toast.LENGTH_LONG).show();	
				}else{
					Toast.makeText(LetvTestActivity.this, "抱歉，支付失败！", Toast.LENGTH_LONG).show();	
				}	
				mPayClicked=false;
			}					
		}		
	};	
	
	private void updateViewAndPlay(boolean play){
		if(!LetvLiveSdkManager.isProgramAvailable(mLiveId)){
			if(0==mRefreshTime){
				mRefreshTime++;
				boolean ok = LetvLiveSdkManager.refreshProgramList();
				Log.i(TAG," program is not available, refresh!=====>"+ok);
				return;
			}
		}
		
		boolean isFree=LetvLiveSdkManager.isProgramFree(mLiveId);
		boolean isPayed=LetvLiveSdkManager.isProgramPurchased(mLiveId);
		Log.i(TAG,"isFree="+isFree+" isPayed="+isPayed);
		if(isFree){
			mTextPrice.setText("价格：免费");
		}else{
			String price=LetvLiveSdkManager.getGeneralPrice(mLiveId);
			String price2=LetvLiveSdkManager.getLoginPrice(mLiveId);
			Log.i(TAG," price="+price+" login price="+price2);
			mTextPrice.setText("价格:"+price+"元");
			if(isPayed){
				mTextPay.setText("支付状态：已支付");
			}
		}
		if(play){
			if(isPayed||isFree){
				Log.i(TAG," play for payed or free");
				startPlay();
			}else{
				Log.i(TAG," can not play for not payed,pay now");
				Toast.makeText(this, "此片源为付费片源，请在购买后观看！！", Toast.LENGTH_LONG).show();
			}
		}
	}
	
	public void onResume(){
		super.onResume();		
	}
	
	private void startPlay(){
		JSONObject streams=new JSONObject();
		JSONObject letv=new JSONObject();	
		try {
			streams.put("1000", "标清");
			streams.put("1300", "高清");
			streams.put("720p", "超清");
			letv.put("title", "乐视直播音乐会");
			letv.put("subTitle", "2015年7月");
			letv.put("program_id", "12345");
			letv.put("episode_id", "12345");
			letv.put("liveId", mLiveId);
			letv.put("startTime", "1423782505000");
			letv.put("endTime", "1426260180000");
			letv.put("vendor", "LETV");
			letv.put("fee", 1f);
			letv.put("ref", "1000933");        //乐视信息上报是的页面
			letv.put("streams", streams);	
			JSONArray  videos=new JSONArray();
			JSONObject playList=new JSONObject();
		
			playList.put("resolution","31");
			videos.put(letv);  //LETV
			playList.put("videos", videos);
			playList.put("index", 0);
			playList.put("position", (int)0);
			playList.put("skip_header", "0");
			playList.put("platform", "L288");
			Log.i("JSON",playList.toString());
			Intent intent=new Intent("com.hisense.vod.mediaplayer.PLAY");
			Bundle bundle=new Bundle();
			bundle.putString("videoInfo",playList.toString());
			intent.putExtras(bundle);
			startActivity(intent);		
		} catch (JSONException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
		
	}
	
	public void click(View view){
		switch(view.getId()){
		case R.id.letv:
			if(mSdkPrepared){
				Log.i(TAG," sdk already prepared, check if payed and play!!!");
				updateViewAndPlay(true);
			}
			break;
		case R.id.pay:
			mPayClicked=true;
			Log.i(TAG," pay now!!!");
			LetvLiveSdkManager.payLiveProgram(mLiveId);
			break;
		}
	}
	
	class SdkInitTask extends AsyncTask<String,Integer,String >{		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			LetvLiveSdkManager.init(LetvTestActivity.this);
			return null;
		}		
	}
	
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		this.setContentView(R.layout.activity_fullscreen);
		LetvLiveSdkManager.addLiveProgramUpdateListener(mUpdateListener);
		LetvLiveSdkManager.addSdkStateChangeListener(mSdkListener);
		mTextStatus=(TextView)findViewById(R.id.status);
		mTextPay=(TextView)findViewById(R.id.text_pay);
		mTextPrice=(TextView)findViewById(R.id.price);
		new SdkInitTask().execute("");		
	}	
	
	public void onDestroy(){
		super.onDestroy();
		LetvLiveSdkManager.removeLiveProgramUpdateListener(mUpdateListener);
		LetvLiveSdkManager.removeSdkStateChangeListener(mSdkListener);
		LetvLiveSdkManager.exit();
	}
}
