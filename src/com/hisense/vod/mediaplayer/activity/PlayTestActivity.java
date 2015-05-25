package com.hisense.vod.mediaplayer.activity;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.hisense.vod.R;
import com.hisense.vod.mediaplayer.util.HttpRequest;
import com.hisense.vod.mediaplayer.util.MD5Signature;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class PlayTestActivity extends Activity {
	private static final String TAG="HisensePayActivity";
	private static final String URL="http://192.168.1.5/vod.php?vendor=";
	private static final String LOCAL_URL="{\"index\":0,\"licence\":\"wasu\",\"platform\":\"K681\",\"payType\":\"2\",\"playType\":\"1\",\"position\":0,\"resolution\":\"31\",\"resourcetype\":\"1\",\"skip_header\":\"0\",\"videos\":[{\"episode_id\":\"20020771842\",\"fee\":\"0\",\"paymentId\":\"22332\",\"program_id\":\"40010013384\",\"subTitle\":\" \",\"title\":\"\u8d85\u4f538\",\"vendor\":\"KU6\",\"urls\":{\"11\":\"-1\",\"21\":\"http://chyd-wsvod.wasu.tv/data11/ott/344/2014-11/14/1415955153756_904271/playlist.m3u8\",\"31\":\"http://chyd-wsvod.wasu.tv/data11/ott/344/2014-11/14/1415955153756_904271/playlist.m3u8\",\"41\":\"-1\"}}]}";
	private ListView mListNetwork,mListLocal;
	
	public void onCreate(Bundle bundle){
		super.onCreate(bundle);
		this.setContentView(R.layout.textpay);
		mListNetwork=(ListView)findViewById(R.id.list_network);
		mListLocal=(ListView)findViewById(R.id.list_local);
		List<String>  network=new ArrayList<String>();
		network.add("爱奇艺");
		network.add("CNTV");
		network.add("华数");
		network.add("搜狐");
		network.add("乐视");
		network.add("酷六");
		mListNetwork.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,network));
		mListNetwork.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,	int position, long id) {
				// TODO Auto-generated method stub
				Log.i(TAG, " list network "+position+" item clicked");
				startNetworkPlay(position);
			}
		});
		
		mListLocal.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_expandable_list_item_1,network));
		mListLocal.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,	int position, long id) {
				// TODO Auto-generated method stub
				Log.i(TAG, " list network "+position+" item clicked");
				startLocalPlay(position);
			}
		});
	}
	
	private void startNetworkPlay(int position){
		Log.i(TAG,"startNetworkPlay called, psoition="+position);
		Toast.makeText(PlayTestActivity.this, "开始请求网络数据！！！",Toast.LENGTH_LONG).show();
		String url=URL;
		switch(position){
		case 0:
			url+="QIYI";
			break;
		case 1:
			url+="CNTV";
			break;
		case 2:
			url+="WASU";
			break;
		case 3:
			url+="SOHU";
			break;
		case 4:
			url+="LETV";
			break;
		case 5:
			url+="KU6";
			break;
		}
		new getTask().execute(url);
	}
	
//	private void  startPay(){
//		Intent intent = new Intent();
//	    intent.setAction("com.hisense.hitv.payment.QC");
//	    intent.putExtra("appName", "聚好看"); //应用名称
//	    intent.putExtra("packageName", "com.hisense.vod");//包名
//	    String sign= MD5Signature.md5("com.hisense.vod"+"2CBED0AE7ADD96439C59EE553FDF5213"); //将自己的应用包名联通海信分配的md5key一同进行md5签名， 	//md5的签名方法在demo里有可直接使用
//	    intent.putExtra("paymentMD5Key", sign);
//	    intent.putExtra("tradeNum", ""+System.currentTimeMillis());
//	    intent.putExtra("goodsPrice", "0.01");
//	    intent.putExtra("goodsName", "收费片源_禁闭岛");
//	    intent.putExtra("alipayUserAmount", "hsyzf@hisense.com");
//	    intent.putExtra("notifyUrl","http://10.18.207.180/payment/api/order/notifyresult");	    
//	    try{
//	    	Log.i(TAG," start pay activity for result");
//	    	startActivityForResult(intent, 1);
//	    }catch(Exception e){
//	    	Log.i(TAG,"catch exception,so go to market");
//	    	Intent it = new Intent(Intent.ACTION_VIEW);
//	    	it.setData(Uri.parse("himarket://details?id="+ "com.hisense.hitv.payment"));	
//	    	startActivity(it);
//	    }	    
//	}
//	
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
//		if(1==requestCode&&resultCode==Activity.RESULT_OK){
//			String payResult=data.getStringExtra("payResult");
//			if(payResult==null){
//				Log.i(TAG," fatal error, pay result is null");
//				Toast.makeText(this, "错误，支付结果返回为空！！！", Toast.LENGTH_LONG).show();
//				return;
//			}
//			
//			if(payResult.equals("SUCCESS")||payResult.equals("FINISH")){
//				Log.i(TAG," pay success!!!");
//				Toast.makeText(this, "支付成功，现在可以观看片源了！！！", Toast.LENGTH_LONG).show();
//			}else if(payResult.equals("WAIT_BUYER_PAY")){
//				Log.i(TAG,"wait for pay!!!");
//				Toast.makeText(this, "支付未完成，你可以在手机上继续支付！！！", Toast.LENGTH_LONG).show();
//			}else if(payResult.equals("WAIT_BUYER_PAY")){
//				Log.i(TAG,"wait for pay!!!");
//				Toast.makeText(this, "支付未完成，你可以在手机上继续支付！！！", Toast.LENGTH_LONG).show();
//			}else {
//				Log.i(TAG,"pay error:"+payResult);
//				Toast.makeText(this, "支付错误："+payResult, Toast.LENGTH_LONG).show();
//			}
//		}else{
//			Log.i(TAG,"onActivityResult error requestCode="+requestCode+" resultCode="+resultCode);
//			Toast.makeText(this, "返回结果错误！！！", Toast.LENGTH_LONG).show();
//		}
//	}
	
class getTask extends AsyncTask<String,Integer,String >{
		
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			String ret= HttpRequest.Get(params[0]);
			Log.i(TAG," response is "+ret);
			try {
				JSONObject json =new JSONObject(ret);
				Intent intent=new Intent("com.hisense.vod.mediaplayer.PLAY");
				Bundle bundle=new Bundle();
				bundle.putString("videoInfo", json.toString());
				intent.putExtras(bundle);
				PlayTestActivity.this.startActivity(intent);
				return "0";
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.e(TAG," json exception:"+e.getMessage());
				return "-1";
			}
		}
		
		protected void onPostExecute(String result) {
			if(result.equals("0")){
				Toast.makeText(PlayTestActivity.this, "网络播放视频成功！！！",Toast.LENGTH_LONG).show();
			}else{
				Toast.makeText(PlayTestActivity.this, "网络播放视频失败！！！",Toast.LENGTH_LONG).show();
			}
		}
	}
	
	private void startLocalPlay(int position){
		JSONObject qiyi2=new JSONObject();
		JSONArray  videos=new JSONArray();
		JSONObject playList=new JSONObject();
		try {
			qiyi2.put("title", "超级8");
			qiyi2.put("subTitle", " ");
			qiyi2.put("program_id", "40010013384");
			qiyi2.put("episode_id", "20020771842");
			qiyi2.put("vendor", "QIYI");
			qiyi2.put("fee", "0");			
			qiyi2.put("vrsAlbumId", "151096");
			qiyi2.put("vrsTvId", "161682"); 
			qiyi2.put("authId", "5597732_2515c030ed8fd7d3e1ad64a39b53b2b");	
			qiyi2.put("paymentId", "22332");				//后台获取的奇异支付id
			
			videos.put(qiyi2);  //QIYI
			playList.put("resolution","31");
			playList.put("videos", videos);
			playList.put("index", 0);
			playList.put("position", (int)0);
			playList.put("skip_header", "0");
			playList.put("licence", "wasu");                	//拍照方 wasu或者是CNTV
			playList.put("resourceType","1");				//播放器启动来源，1为详情页，2为微信，3为语音
			playList.put("playType", "1");						//播放的类型 1试看、2直播、0其它
			playList.put("payType", "2");							//支付的类型， 免费、收费、试看
			playList.put("platform", "VIDDA3");
			
			Intent intent=new Intent("com.hisense.vod.mediaplayer.PLAY");
			Bundle bundle=new Bundle();
			bundle.putString("videoInfo", playList.toString());
			//bundle.putString("videoInfo",LOCAL_URL);
			intent.putExtras(bundle);
			startActivity(intent);
		} catch (JSONException e) {
			// TODO 自动生成的 catch 块
			e.printStackTrace();
		}
	}
}
