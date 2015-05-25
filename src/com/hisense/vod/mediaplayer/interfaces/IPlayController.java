package com.hisense.vod.mediaplayer.interfaces;

import android.app.Activity;

import com.hisense.vod.mediaplayer.util.Config.DisplaySize;
import com.hisense.vod.mediaplayer.video.VideoInfo;

public interface IPlayController {
	public void setDataSource(VideoInfo video, boolean adPlayed, int pos);	
	public void start();
	public void start(int pos);
	public void seek(int pos);
	public void pause();
	public void stop();
	public void release();
	public int getCurrentPosition();
	public int getDuration();
	public boolean isPlaying();
	public void setDataSource(String url,boolean isFree, int previewTime);
	public int getAdDownCount();
	public void setDisPlaySize(DisplaySize size);;
	public void setResolution(String res);
	public void seekWhenPrepared(int mHeaderTime);
	public void onActivityResume(Activity activity);
	public void onActivityStop(Activity activity);
}
