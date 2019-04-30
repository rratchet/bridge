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
 * 文件名称：JavaScriptProviderImpl.java
 * 文件描述：
 *
 * 创 建 人：ASLai(laijianhua@rratchet.com)
 *
 * 上次修改时间：2019-04-30 15:23:08
 *
 * 修 改 人：ASLai(laijianhua@rratchet.com)
 * 修改时间：2019-04-30 15:26:41
 * 修改备注：
 */

package com.rratchet.sdk.bridge;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.webkit.ValueCallback;
import android.webkit.WebView;


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
class JavaScriptProviderImpl implements JavaScriptProvider {

    private WebView mWebView;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    public JavaScriptProviderImpl(WebView webView) {
        mWebView = webView;
    }

    private void loadUrl(String javascript) {
        mWebView.loadUrl(javascript);
    }

    private void evaluateJavascript(String javascript, final ValueCallback<String> callback) {
        mWebView.evaluateJavascript(javascript, new ValueCallback<String>() {
            @Override
            public void onReceiveValue(String value) {
                if (callback != null) {
                    callback.onReceiveValue(value);
                }
            }
        });
    }

    private void safeEvaluateJavascript(final String javascript, final ValueCallback<String> callback) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                evaluateJavascript(javascript, callback);
            }
        });
    }

    @Override
    public void execute(String javascript) {
        loadUrl(javascript);
    }

    @Override
    public void execute(final String javascript, final ValueCallback<String> callback) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {

            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            safeEvaluateJavascript(javascript, callback);
        } else {
            loadUrl(javascript);
        }
    }

    @Override
    public void execute(Command command) {
        String javascript = command.transform();
        execute(javascript);
    }

    @Override
    public void execute(Command command, ValueCallback<String> callback) {
        String javascript = command.transform();
        execute(javascript, callback);
    }
}
