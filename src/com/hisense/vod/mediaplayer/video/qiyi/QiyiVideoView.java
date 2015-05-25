package com.hisense.vod.mediaplayer.video.qiyi;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;

import com.hisense.vod.mediaplayer.interfaces.IPlayController;
import com.hisense.vod.mediaplayer.interfaces.IPlayListener;
import com.hisense.vod.mediaplayer.util.Config;
import com.hisense.vod.mediaplayer.util.VodError;
import com.hisense.vod.mediaplayer.util.Config.DisplaySize;
import com.hisense.vod.mediaplayer.video.PlayListManager;
import com.hisense.vod.mediaplayer.video.VideoInfo;
import com.qiyi.video.player.IVideoStateListener;
import com.qiyi.video.player.QiyiVideoPlayer;
import com.qiyi.video.player.QiyiVideoPlayer.DisplayMode;

public class QiyiVideoView  implements IPlayController{
	private static final String TAG="QiyiVideoView";
	private static QiyiVideoPlayer player;
	private QiyiVideoInfo mVideo;
	private int position;	
	private boolean mSeekWhenPrepared=false;
	//private String mPlatform=PlayListManager.getPlatform();			//如果为K720,则需要在两分钟以后执行seek的时候重新获取Url，其它平台不需要这样做
	private long mSetDataSourceTime=0;								    //setdatasource方法执行的时间
	private IPlayListener mListener;								    //seek时的刷新UI的listener
	private String mAuthId=QiyiVideoInfo.getAuthId();
	
	public QiyiVideoView(){
		
	}
	
	public QiyiVideoPlayer createPlayer(Context context, FrameLayout layout,FrameLayout.LayoutParams flParams, IPlayListener listener){	
		mListener=listener;
		if(mAuthId==null){
			VodError error=new VodError(VodError.ERROR_PERFIX_QIYI+"002","获取奇异鉴权Id失败，请检查传入参数...");
			mListener.onVodError(error);
		}
		Log.i(TAG," create player, mAuthId="+mAuthId);
		player=QiyiVideoPlayer.createVideoPlayer(context, layout, flParams,  null, listener,mAuthId);
		return player;
	}
	
	@Override
	public void setDataSource(VideoInfo video,boolean adPlayed,int startPos) {
		// TODO Auto-generated method stub
		if(video==null){
			VodError error=new VodError(VodError.ERROR_PERFIX_QIYI+"001","获取视频信息失败，请检查传入参数...");
			mListener.onVodError(error);
			return;
		}
		mSetDataSourceTime=System.currentTimeMillis();		
		Log.i(TAG," setdatasource called and  mSetDataSourceTime="+mSetDataSourceTime);
		mVideo=(QiyiVideoInfo)video;
		player.setVideo(mVideo);
		if(startPos>0){
			Log.i(TAG," setDataSource, mSeekWhenPrepared="+mSeekWhenPrepared+" position="+startPos);
			mSeekWhenPrepared=true;
			position=startPos;
		}
	}

	private void getOtherUrl() {
		// TODO Auto-generated method stub
		int curRes=Integer.parseInt(mVideo.getResolution());
		int value[]={41,31,21,11};
	
		for(int i=0; i<3;i++){
			for(int j=0;j<4;j++){
				Log.i(TAG," value="+mVideo.getUrls().get(String.valueOf(value[j]))+" abs="+Math.abs(curRes-value[j]));
				if(Math.abs(curRes-value[j])==10*(i+1)){
					String s=mVideo.getUrls().get(String.valueOf(value[j]));					
					if(!s.equals(Config.NONE_URL)){
						Log.i(TAG," the new res="+String.valueOf(value[j]));
						mVideo.setDefinition(PlayListManager.getQiyiDefinition(String.valueOf(value[j])));	
						return;
					}
				}
			}			
		}
	}

	@Override
	public void setResolution(String res) {
		// TODO Auto-generated method stub
		Log.i("TAG"," change resolution to "+PlayListManager.getQiyiDefinition(res)+" old is "+mVideo.getDefinition());
		player.switchBitStream(PlayListManager.getQiyiDefinition(res));					
	}

	@Override
	public void seekWhenPrepared(int headerTime) {
		// TODO Auto-generated method stub
		if(mSeekWhenPrepared){
			Log.i(TAG," seekWhenPrepared called seek to position: "+position);
			player.seekTo(position);
			mSeekWhenPrepared=false;
		}else{
			if(headerTime>0){
				Log.i(TAG," seekWhenPrepared called and seek to headerTime:"+headerTime);
				player.seekTo(headerTime);
				mSeekWhenPrepared=false;
			}
		}
	}

	public void updateDefinitions() {
		// TODO Auto-generated method stub
		String res=mVideo.getResolution();
		String playUrl=mVideo.getUrls().get(res);
		if(playUrl.equals(Config.NONE_URL)){
			getOtherUrl();			
		}
	}
	
	@Override
	public void start() {
		// TODO Auto-generated method stub
		player.start();
	}


	@Override
	public void pause() {
		// TODO Auto-generated method stub
		player.pause();
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		Log.i(TAG," qiyi video view stop called ");
		player.stop();
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
		player.releasePlayer();
	}

	@Override
	public void seek(int pos) {
		// TODO Auto-generated method stub	
		Log.i(TAG," seekto: "+pos);
		player.seekTo(pos);
		player.start();	
	}

	@Override
	public int getCurrentPosition() {
		// TODO Auto-generated method stub
		return player.getCurrentPosition();
	}

	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		return player.getDuration();
	}

	@Override
	public void start(int pos) {
		// TODO Auto-generated method stub
		player.start(pos);
	}

	@Override
	public boolean isPlaying() {
		// TODO Auto-generated method stub
		return player.isPlaying();
	}

	@Override
	public int getAdDownCount() {
		// TODO Auto-generated method stub
		return player.getAdCountDownTimeProvider().getCountDownTime();
	}

	@Override
	public void setDisPlaySize(DisplaySize size) {
		// TODO Auto-generated method stub				
		switch(size){
		case ORIGINAL:
			Log.i(TAG," now can not use for original");
			player.setDisplayMode(DisplayMode.MODE_ORIGINAL);
			break;
		case FULL_SCREEN:
			player.setDisplayMode(DisplayMode.MODE_STRETCH_TO_FIT);
			Log.i(TAG," now set full screen 16:9");
			break;
		case FULL_SCREEN_4_3:
			player.setDisplayMode(DisplayMode.MODE_CUSTOM_RATIO);
			player.setCustomAspectRatio((float) 4 / 3);
			Log.i(TAG," now set full screen 4:3");
			break;				
		}		
	}

	@Override
	public void onActivityResume(Activity activity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onActivityStop(Activity activity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setDataSource(String url, boolean isFree, int previewTime) {
		// TODO Auto-generated method stub
		
	}	
}
