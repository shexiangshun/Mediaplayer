#include<stdlib.h>
#include<jni.h>
#include <android/log.h>
#include<sys/types.h>
#include<unistd.h>
#include <errno.h>
#include <sys/wait.h>
#include<stdio.h>
#include<malloc.h>
#include<string.h>

#define  LOG_TAG    "JNI"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)
int YST_CDN_MANAGER=3;
int YST_LOGIN_SRV=2;
int YST_QOSASS=1;
int READ_BUF_SIZE=50;
int MAC_LENGTH=17;
char* MAC_FILE="/sys/class/net/eth0/address";

char* checkMac(char * s){
	char checked_mac[READ_BUF_SIZE];
	int j=0;

	if(NULL==s){
		LOGE("the mac is NULL, return ");
		return NULL;
	}

	LOGI(" the length of mac before filter is %d",strlen(s));
	for(int i=0;*(s+i)!='\0';i++){
		char ch=*(s+i);
		if((ch>='a'&&ch<='z')||(ch>='A'&&ch<='Z')||(ch>='0'&&ch<='9')||ch==':'){
			checked_mac[j]=ch;
			j++;
		}
	}
	checked_mac[j]='\0';
	if(strlen(checked_mac)==MAC_LENGTH){
		LOGI("check mac success, the mac is %s and the length is %d",checked_mac,strlen(checked_mac));
		return checked_mac;
	}else{
		LOGI("check mac error, the mac is %s and the length is %d",checked_mac,strlen(checked_mac));
		return NULL;
	}
}

char* getEth0Mac(){
	FILE * fd;
	char* mid;
	char mac[READ_BUF_SIZE];
	LOGI(" start get mac from eth0");
	fd=fopen(MAC_FILE,"r");
	if(fd==NULL){
		LOGE( "open file:/sys/class/net/eth0/address error, is it exist or has the permit");
		return NULL;
	}else{
		char * mid=fgets(mac,READ_BUF_SIZE,fd);
		if(NULL==mid){
			LOGE(" read mac error");
		}else{
			LOGI(" the mac read is %s",mac);
			fclose(fd);
			char * real_mac=checkMac(mid);
			return real_mac;
		}
	}
	return NULL;
}

int my_system(const char * cmd){

	FILE * fp;
	int res;
	char buf[1024];

	if (cmd == NULL){
		LOGI("my_system cmd is NULL, return -1!\n");
		return -1;
	}else{
		LOGI(" my_system called cmd=%s",cmd);
	}
	if ((fp = popen(cmd, "r") ) == NULL){
		perror("popen");
		LOGE("popen error: %s/n", strerror(errno));
		return -2;
	}else {
		while(fgets(buf, sizeof(buf), fp)){
			LOGI("ret: %s", buf);
		}
		if ( (res = pclose(fp)) == -1){
			LOGE("close popen file pointer fp error!\n");
			return res;
		}else if (res == 0){
			return res;
		}else{
			LOGI("popen res is :%d\n", res);
			return res;
		}
	}
}

int my_exec(char* cmd, char**arg,char** env){
	pid_t pid;
	pid=fork();
	if(0==pid){
		LOGI("I am child process!!!");
		if(execve(cmd,arg,env)<0){
			LOGE(" execve %s error and errno=%d",cmd,errno);
			if(errno==ENOENT){
				LOGE(" ENOENT:%s",strerror(errno));
				return -1;
			}else if(errno==EFAULT){
				LOGE(" EFAULT:%s",strerror(errno));
				return -2;
			}else if(errno==EACCES){
				LOGE("EACCES: %s",strerror(errno));
				return -3;
			}else{
				LOGE(" UNKNOWN:%s",strerror(errno));
				return -4;
			}
		}else{
			LOGI("exec %s success!!!!",cmd);
		}
	}else if(pid<0){
		LOGE(" fork error!!!");
		return -5;
	}
}

jint startLocalSrv(JNIEnv *env, jobject thiz, jint mode){
	LOGI(" JNI start Local server and mode=%d",mode);
	int ret=-1;
    char* LOGIN="/data/data/com.hisense.vod/files/YstLoginSrv";
    char* CDN="/data/data/com.hisense.vod/files/YstCdnManager";
    char* QOSASS="/data/data/com.hisense.vod/files/YstQOSASS";
    char* path="YST_PLATFORM_PATH=/data/data/com.hisense.vod/files";
    char* s_mac;

//    char* LOGIN="/data/data/com.jamdeo.vod.mediaplayer/files/YstLoginSrv &";
//    char* CDN="/data/data/com.jamdeo.vod.mediaplayer/files/YstCdnManager &";
//    char* QOSASS="/data/data/com.jamdeo.vod.mediaplayer/files/YstQOSASS &";
//    char* path="YST_PLATFORM_PATH=/data/data/com.jamdeo.vod.mediaplayer/files";
    char env_mac[128]="YST_PLATFORM_GEFO_MAC=";

	ret=putenv(path);
	LOGI(" putenv ret=%d",ret);
	char * test=getenv("YST_PLATFORM_PATH");
	if(test==NULL){
		LOGE(" the test env path is null ");
	}else{
		LOGI(" the test env YST_PLATFORM_PATH=%s",test);
	}
	if(mode==YST_CDN_MANAGER){
		//ret=my_system(CDN);
		char* arg2[]={CDN,"&",NULL};
		char* env2[]={path,NULL};
		ret=my_exec(CDN,arg2,env2);
	}else if(mode==YST_LOGIN_SRV){
		char * eth0_mac=getEth0Mac();
		if(NULL==eth0_mac){
			LOGE(" get eth0 mac error");
			return -1;
		}else{
			s_mac=strcat(env_mac,eth0_mac);
			if(NULL==s_mac){
				LOGE(" strcat error");
				return -1;
			}else{
				LOGI(" the env for mac is :%s",s_mac);
				putenv(s_mac);
				char * test_env=getenv("YST_PLATFORM_GEFO_MAC");
				LOGI(" get mac from env is: %s",test_env);
			}
		}
		//ret=my_system(LOGIN);
		char* arg[]={LOGIN,"&",NULL};
		char* env[]={path,s_mac,NULL};
		ret=my_exec(LOGIN,arg,env);
	}else if(mode==YST_QOSASS){
		//ret=my_system(QOSASS);
		char* arg1[]={QOSASS,"&",NULL};
		char* env1[]={path,NULL};
		ret=my_exec(QOSASS,arg1,env1);
	}

	if(ret>=0){
		LOGI(" system success ");
	}else{
		LOGE(" system fail ");
	}

	return ret;
}

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv *env;

	//LOGI("JNI_OnLoad called");
	if (vm->GetEnv((void**) &env, JNI_VERSION_1_4) != JNI_OK) {
		LOGE("Failed to get the environment using GetEnv()");
		return -1;
	}

	JNINativeMethod methodsJ[] = {
			{ "startLocalSrv", "(I)I",(void*) startLocalSrv },
	};
	jclass k;
	k = env->FindClass("com/hisense/vod/mediaplayer/util/CNTVLocalServerHelper");
	(env)->RegisterNatives(k, methodsJ, 1);

	return JNI_VERSION_1_4;
}

