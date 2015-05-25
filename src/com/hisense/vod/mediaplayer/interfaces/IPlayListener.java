package com.hisense.vod.mediaplayer.interfaces;

import com.qiyi.video.player.IVideoStateListener;

public interface IPlayListener extends IVideoStateListener{
	public void onAuthCompleted(String playUrl);
	public void onFirstFrameStart();	
	public void onPlayLimitedPreView(Integer limitTime);
	public void onPlayLimitedExceed();
	public void onPlayLimitedPayed();
	public boolean onError(int what, String extra);
	public void onVodError( com.hisense.vod.mediaplayer.util.VodError error);	
	public void actionNotSupport(String action);
	public void onLetvStatusChange(String status);
	public void onPayResult(String description);
}
