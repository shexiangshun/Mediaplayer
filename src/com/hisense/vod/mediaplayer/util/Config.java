package com.hisense.vod.mediaplayer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 
 * @author shexiangshun
 * <br> date 2014.11.10
 * <br>主要是用来设置不同的机型的播控行为。
 *
 */
public class Config {
	private static final String TAG="Config";	
	public static final String SD="11";					//标清
	public static final String HD="21";					//高清
	public static final String FHD="31";				//超清
	public static final String BD="41";					//1080P	
	public static final String NONE_URL="-1";			//
	
	public static String Version="2.0.5.0511";			//版本号，用于区分不同版本
	private String  mPlatform="unknown";  //对应的平台名称	
	private boolean mShowBuffering=true;  //是否显示缓冲对话框的flag,主要用于XT900/XT910平台
	private boolean mCopyAssets=true;     //是否copy Assets目录中的配置文件并运行Yst组件，对于vidda系列，其是不需要copy的
	private boolean mIsOld=false;	      //判断是否为老版本，老版本的播放组件是开机启动server的
	private Context mContext;	
	private String[] allPlatform={"PX1900","K681","XT910","L288","K220","XT810","PX2700",
			"K680","K320U","K320U_NT","K370","VIDDA3"};
	
			
	public enum DisplaySize{   
		UNKOWN,					//未知
		ORIGINAL,				//原始
		FULL_SCREEN,			//全屏
		FULL_SCREEN_4_3;		//全屏4:3
	}
		
	public enum PlayCrop{
		SKIP,					//跳过
		NOT_SKIP;				//不跳过
	}
		
	/**
	 * 
	 * @param context  上下文环境
	 * @param inputPlatform 对应的平台信息
	 * <br> 根据机型信息来获取相应的播放行为
	 */
	public Config(Context context, String inputPlatform){
		Log.i(TAG," Config constructor inputPlatform= "+ inputPlatform);
		mContext=context;
		mPlatform=inputPlatform;		
		checkPlatform();	
		checkShowBuffering();
		checkCopyAssets();		
		mIsOld=checkVersion();
		
		Log.i(TAG,"after check the config params is mPlatform="+mPlatform
				+" mShowBuffering="+mShowBuffering+" mCopyAssets="+mCopyAssets+" mIsOld="+mIsOld);
	}
	
	/**
	 * 
	 * @return  获取机型信息
	 */
	public String getPlatform(){
		return mPlatform;
	}
	
	/**
	 * 
	 * @return  是否需要copy assets目录下的ini文件和Yst组件 . true需要  ， false不需要  
	 */
	public Boolean ifCopyAssets(){
		return mCopyAssets;
	}	
		
	/**
	 * 
	 * @return 是否显示视频缓冲对话框，true显示，false不显示
	 */
	public boolean ifShowBuffering(){
		return mShowBuffering;
	}
	
	/**
	 * 
	 * @return 是否为K681的老版本，true是，false不是
	 */
	public boolean isOldVersion(){
		return mIsOld;
	}
	
	private void checkCopyAssets(){
		if(mPlatform.equalsIgnoreCase("K680")||mPlatform.equalsIgnoreCase("PX2700")){
			mCopyAssets=false;
		}else{
			mCopyAssets=true;
		}
	}
	
	private boolean checkVersion() {
		// TODO Auto-generated method stub
		try {
			 String ps= "ps | grep  YstCdnManager";
		     Log.i(TAG,"execute command:"+ps);
		     Process psPro = Runtime.getRuntime().exec(ps);
		     InputStream inputStream =psPro.getInputStream();
		     Reader reader = new InputStreamReader(inputStream);
		     BufferedReader buffer = new BufferedReader(reader);
		     String line="";
		     do{
		        line = buffer.readLine();
		        if (line != null){
		        	Log.i(TAG," line："+line);
		        	continue;
		        }
		        psPro.waitFor();
		        buffer.close();
		        reader.close();
		        Log.i(TAG," /system/bin/YstCdnManager is not run");
		        return false;
		      }while (!(line.contains("/system/bin/YstCdnManager")&&(!line.contains("grep"))));
		      psPro.waitFor();
		      buffer.close();
		      reader.close();
		      Log.d(TAG,"/system/bin/YstCdnManager is already running:\n "+line);
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

	//XT900的缓冲开始和缓冲结束的消息有时并不配对，所以不能显示缓冲提示框
	private void checkShowBuffering(){
		if(mPlatform.equalsIgnoreCase("XT900")||mPlatform.equalsIgnoreCase("XT910")){
			mShowBuffering=false;
		}else{
			mShowBuffering=true;
		}
	}
	
	private void checkPlatform(){
		if(mPlatform==null||mPlatform.equals("unknown")){
			try{
				String modelName=System.getProperty("ro.product.model");
			    if(hasPlatform(modelName)){
			    	Log.i(TAG," get ro.product.model correct, name:"+modelName);
			    	mPlatform=modelName;
			    }else{
			    	Log.i(TAG," get ro.product.model error, name:"+modelName);
			    	mPlatform="unknown";
			    }
			}catch(Exception e){
				Log.e(TAG," get ro.product.model exception:"+e.getMessage());
				mPlatform="unknown";
			}
		}else{
			if(hasPlatform(mPlatform)){
				Log.i(TAG," check platform suucess , name:"+mPlatform);
			}else{
				Log.e(TAG," check platform error, name:"+mPlatform);
				mPlatform="unknown";
			}
		}
	}
	
	private boolean hasPlatform(String name){
		if(name==null){
			return false;
		}
		
		for(String s:allPlatform){
			if(s.equalsIgnoreCase(name)){
				return true;
			}
		}
		return false;
	}
}
