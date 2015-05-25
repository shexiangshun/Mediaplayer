LOCAL_PATH:=$(call my-dir)
include $(CLEAR_VARS)

LOCAL_MODULE :=startloginsrv
LOCAL_SRC_FILES :=startLoginSrv.cpp
LOCAL_LDLIBS := -llog 
   
include $(BUILD_SHARED_LIBRARY)
