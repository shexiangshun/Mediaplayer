package com.hisense.vod.mediaplayer.util;

public class VodError {   
	private static final String TAG="VodError";
	public static final String ERROR_PERFIX_WASU="E1:";
	public static final String ERROR_PERFIX_CNTV="E2:";
	public static final String ERROR_PERFIX_QIYI="E3:";
	public static final String ERROR_PERFIX_SOHU="E4:";	
	public static final String ERROR_PERFIX_LETV="E5:";
	public static final String ERROR_CODE_SOHU_THROWABLE="001";    
	
    private String mWhat;
    private String mExtra;
    
    public VodError(String what,String extra){
    	mWhat=what;
    	mExtra=extra;
    }
    
    public String getErrorCode(){
    	return mWhat;
    }
    
    public String getExtra(){
    	return mExtra;
    }   
}
