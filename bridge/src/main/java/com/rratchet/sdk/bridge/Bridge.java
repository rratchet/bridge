/*
 * Copyright (c) 2019. RRatChet Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * 文件名称：Bridge.java
 * 文件描述：
 *
 * 创 建 人：ASLai(laijianhua@rratchet.com)
 *
 * 上次修改时间：2019-04-30 15:23:43
 *
 * 修 改 人：ASLai(laijianhua@rratchet.com)
 * 修改时间：2019-04-30 15:26:41
 * 修改备注：
 */

package com.rratchet.sdk.bridge;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <pre>
 *
 *      作 者 :        ASLai(laijianhua@rratchet.com).
 *      日 期 :        2019/4/28
 *      版 本 :        V1.0
 *      描 述 :        description
 *
 *
 * </pre>
 *
 * @author ASLai
 */
public class Bridge {

    private final ConcurrentMap<String, JavaScriptCallback> callbackMap = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, NativeCodeHandler>  handlerMap  = new ConcurrentHashMap<>();

    private WebView mWebView;

    private JavaScriptProvider mJavaScriptProvider;
    private NativeCodeProvider mNativeCodeProvider;

    Bridge(WebView webView) {
        mWebView = webView;
        mJavaScriptProvider = new JavaScriptProviderImpl(webView);
        mNativeCodeProvider = new NativeCodeProviderImpl(new DefaultMessageListener());
        checkWebSettings();
    }

    public static Bridge create(WebView webView) {
        return new Bridge(webView);
    }

    static boolean isEmpty(@Nullable CharSequence str) {
        return str == null || str.length() == 0;
    }

    private void checkWebSettings() {
        WebSettings settings = mWebView.getSettings();
        // 判断是否开启了JavaScript支持
        if (!settings.getJavaScriptEnabled()) {
            settings.setJavaScriptEnabled(true);
        }
    }

    public String acceptJavaScriptPrompt(String message) {

        String result = null;
        if (mNativeCodeProvider != null) {
            if (mNativeCodeProvider.checkSafeCall(message)) {
                result = mNativeCodeProvider.execute(message);
            }
        }
        return result;
    }

    public void registerMessageHandler(@NonNull String handlerName, @NonNull NativeCodeHandler handler) {
        handlerMap.put(handlerName, handler);
    }

    public void unregisterMessageHandler(@NonNull String handlerName) {
        handlerMap.remove(handlerName);
    }

    public void sendMessage(String handlerName) {
        sendMessage(handlerName, (String) null, null);
    }

    public void sendMessage(String handlerName, JavaScriptCallback callback) {
        sendMessage(handlerName, null, callback);
    }

    public void sendMessage(String handlerName, String data) {
        sendMessage(handlerName, data, null);
    }

    public void sendMessage(String handlerName, String data, JavaScriptCallback callback) {
        Command command = Command.create(handlerName, data);
        sendMessage(command, callback);
    }

    public void sendCustomMessage(String methodName, Object... params) {
        Command command = Command.custom(methodName, params);
        sendMessage(command);
    }

    public void sendMessage(@NonNull Command command) {
        sendMessage(command, null, null);
    }

    public void sendMessage(@NonNull Command command, @Nullable JavaScriptCallback callback) {
        sendMessage(command, callback, null);
    }


    public void sendMessage(@NonNull Command command, ValueCallback<String> callback) {
        sendMessage(command, null, callback);
    }

    public void sendMessage(@NonNull Command command, @Nullable JavaScriptCallback callback, ValueCallback<String> valueCallback) {
        if (command.getMessage() != null && callback != null) {
            String callbackId = UUID.randomUUID().toString();
            command.getMessage().setCallbackId(callbackId);
            callbackMap.put(callbackId, callback);
        }
        if (valueCallback == null) {
            mJavaScriptProvider.execute(command);
        } else {
            mJavaScriptProvider.execute(command, valueCallback);
        }
    }

    private void respondMessage(String callbackId, Message message) {
        Command command = Command.create(message.handlerName, message.data);
        command.getMessage().setResponseId(callbackId);
        mJavaScriptProvider.execute(command);
    }

    private JavaScriptCallback buildResponseCallback(@NonNull final String callbackId) {

        if (TextUtils.isEmpty(callbackId)) {
            return null;
        }

        return new JavaScriptCallback() {
            @Override
            public String onMessageCallback(Message message) {
                respondMessage(callbackId, message);
                return "";
            }
        };
    }

    private JavaScriptCallback buildEmptyResponseCallback() {
        return new JavaScriptCallback() {
            @Override
            public String onMessageCallback(Message message) {
                // 本消息不需要给js回调结果
                return null;
            }
        };
    }

    interface MessageListener {

        String onMessageCallback(Message message);

        String onMessageHandle(Message message);
    }

    private class DefaultMessageListener implements MessageListener {
        @Override
        public String onMessageCallback(Message message) {
            String responseId = message.getResponseId();

            if (isEmpty(responseId)) {
                return null;
            }

            JavaScriptCallback callback = callbackMap.get(responseId);
            if (callback == null) {
                return null;
            }

            callbackMap.remove(responseId);

            String result = callback.onMessageCallback(message);

            return result;
        }

        @Override
        public String onMessageHandle(Message message) {
            String handlerName = message.getHandlerName();

            if (isEmpty(handlerName)) {
                return null;
            }

            NativeCodeHandler handler = handlerMap.get(handlerName);

            if (handler == null) {
                return null;
            }

            JavaScriptCallback callback;
            if (message.isNeedCallback()) {
                callback = buildResponseCallback(message.getCallbackId());
            } else {
                //不需要callback的创建空的callback，防止在使用callback时抛出空指针
                callback = buildEmptyResponseCallback();
            }

            String result = handler.onMessageHandle(message, callback);

            return result;
        }
    }

}
