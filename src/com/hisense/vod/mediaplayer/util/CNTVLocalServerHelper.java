package com.hisense.vod.mediaplayer.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;
import android.os.SystemProperties;


public class CNTVLocalServerHelper{
	private static final String TAG="CNTVLocalServerHelper"; 
	private static final String PREF="cntv_assets"; 
	private static final String ISFIRST="isFirst"; 
	private static final String PATH="/data/data/com.hisense.vod/files";
	private static final int YST_CDN_MANAGER=3;
	private static final int YST_LOGIN_SRV=2;
	private static final int YST_QOSASS=1;	
	private Context mContext;
	private String mPlatform;
	private String[] iniFileName={"ConfigInfo.ini","DeviceID.ini","DeviceInfo.ini","GefoCfg.ini","LogCfg.ini","PlayerAdapteCfg.ini",
			"Version.ini","ystWeatherReportSelectedCityList.ini"};
	private String[] YstFileName={"YstCdnManager","YstLoginSrv","YstQOSASS"};	
	
	
	private native int startLocalSrv(int mode);
	
	/**
	 * 
	 * @param context 上下文环境
	 * @param platform 对应的平台号
	*/
	public CNTVLocalServerHelper(Context context,String platform){
		mContext=context;
		mPlatform=platform;
	}
	
	public void init(){
		if(isFirst()){
			copyIniFiles();
			copyYstFiles();			
			SharedPreferences pref=mContext.getSharedPreferences(PREF,Context.MODE_PRIVATE );
			SharedPreferences.Editor editor=pref.edit();
			editor.putBoolean(ISFIRST, false);	
			editor.commit();
		}		
		chmodFiles();
		startLocalServer();
	}
	
	private void copyIniFiles(){
		Log.i(TAG,"copy ini files strat ");
		String dstPath=mContext.getFilesDir().getPath()+"/ini";		
		for(int i=0;i<iniFileName.length;i++){
			Log.i(TAG," now copy file is "+iniFileName[i]);
			copyFile(iniFileName[i],dstPath,mPlatform);
		}		
	}
	
	private void copyYstFiles(){
		Log.i(TAG," copy Yst file start");
		String dstPath=mContext.getFilesDir().getPath();
		for(int i=0;i<YstFileName.length;i++){
			Log.i(TAG," now copy file is "+YstFileName[i]);
			copyFile(YstFileName[i],dstPath);
		}	
	}

	private void chmodFiles(){	
		String cmd="chmod -R 777 " +PATH;
		execCmd(cmd);
	}
	
	private void copyFile(String ASSETS_NAME, String savePath) {
		String filename = savePath + "/" + ASSETS_NAME;
		Log.i(TAG," copy file: the saved file is :"+filename);
		File dir = new File(savePath);		
		if (!dir.exists()){
			dir.mkdir();
		}
		
		try {
			if (!(new File(filename)).exists()) {
				InputStream is = mContext.getResources().getAssets().open(ASSETS_NAME);
				FileOutputStream fos = new FileOutputStream(filename);
				byte[] buffer = new byte[1024];
				int count = 0;
				while ((count = is.read(buffer)) > 0) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			}
		}catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG," write file exception:"+e.getMessage());
		}
	}
	
	private void copyFile(String ASSETS_NAME, String savePath,String platform) {
		String fileName = savePath + "/" + ASSETS_NAME;
		String srcFile=platform+"/"+ASSETS_NAME;
		Log.i(TAG," copy file from "+srcFile+" to "+fileName);
		
		File dir = new File(savePath);		
		if (!dir.exists()){
			dir.mkdir();
		}
		
		try {			
			File file=new File(fileName);
			if(file.exists()){
				file.delete();
			}
			InputStream is = mContext.getResources().getAssets().open(srcFile);
			FileOutputStream fos = new FileOutputStream(fileName);
			byte[] buffer = new byte[1024];
			int count = 0;
			while ((count = is.read(buffer)) > 0) {
				fos.write(buffer, 0, count);
			}
			fos.close();
			is.close();			
		}catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG," write file exception:"+e.getMessage());
		}
	}
	
	private void execCmd(String cmd){
		try {
			
			Log.i(TAG," now exec cmd is "+cmd);
			Process pro=Runtime.getRuntime().exec(cmd);			
			//pro.waitFor();
			InputStreamReader inputStr = new InputStreamReader(pro.getInputStream());
			BufferedReader br = new BufferedReader(inputStr);
			String temp = "";
			while((temp = br.readLine())!= null){
				Log.i(TAG,"exec cmd: "+ temp);
			}
				
			Log.i(TAG," exec cmd "+cmd+" end");
			//pro.destroy();
			br.close();
			inputStr.close();		
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private boolean isFirst(){
		SharedPreferences pref=mContext.getSharedPreferences(PREF,Context.MODE_PRIVATE );
		return pref.getBoolean(ISFIRST, true);	 
	}
	
	
	
	public void startLocalServer(){	
		int ret =-1;
		
		if(!isStarted("YstQOSASS")){
			ret=startLocalSrv(YST_QOSASS);			
			if(ret>=0){
				Log.i(TAG," start QOSASS serevr success by JNI");
			}else{
				Log.e(TAG," can not start QOSASS server by JNI");
			}	
		}
		if(!isStarted("YstLoginSrv")){
		    //the env path is seted inside JNI					
		    ret=startLocalSrv(YST_LOGIN_SRV);
			if(ret>=0){
				Log.i(TAG," start CNTV login serevr success by JNI");
			}else{
				Log.e(TAG," can not start  CNTV login server by JNI");
			}			
		}
		
		if(!isStarted("YstCdnManager")){
			ret=startLocalSrv(YST_CDN_MANAGER);			
			if(ret>=0){
				Log.i(TAG," start cdn manager serevr success by JNI");
			}else{
				Log.e(TAG," can not start cdn manger server by JNI");
			}				
		}	          
	}	

	private boolean isStarted(String name){
		try {			 
			 String ps= "ps | grep ";
		     String ps2= ps + name;
		     Log.i(TAG,"execute command:"+ps2);
		     Process psPro = Runtime.getRuntime().exec(ps2);
		     InputStream inputStream =psPro.getInputStream();
		     Reader reader = new InputStreamReader(inputStream);
		     BufferedReader buffer = new BufferedReader(reader);
		     String line="";
		     do{
		        line = buffer.readLine();
		        if (line != null)
		          continue;
		        psPro.waitFor();		      
		        buffer.close();
		        reader.close();
		        Log.i(TAG,name+" is not run");
		        return false;
		      }while (!(line.contains(name)&&(!line.contains("grep"))));		     
		      
		      psPro.waitFor();		     
		      buffer.close();
		      reader.close();
		      Log.d(TAG,name+" is already running:\n "+line);
		      return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();	
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
		}
		return false;
	}	
}
