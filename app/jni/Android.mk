# Copyright (C) 2009 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := king
LOCAL_SRC_FILES := king.c dns_rr_manipulation.c recNameServer.c utility_functions.c
LOCAL_C_INCLUDES += $(LOCAL_PATH)/include
LOCAL_COPY_HEADERS_TO := libcurl
LOCAL_COPY_HEADERS := $(addprefix curl/include/curl/,$(HHEADERS))
LOCAL_MODULE:= libcurl

include $(BUILD_SHARED_LIBRARY)
