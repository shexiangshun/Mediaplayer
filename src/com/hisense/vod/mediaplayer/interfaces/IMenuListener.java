package com.hisense.vod.mediaplayer.interfaces;

public interface IMenuListener {
	public void displaySizeChanged(com.hisense.vod.mediaplayer.util.Config.DisplaySize size);
	public void playCropChanged(com.hisense.vod.mediaplayer.util.Config.PlayCrop crop);
	public void resolutionChanged(String res);
	public void dismissMenu();
	public void notSupportSkipHeader();
}
