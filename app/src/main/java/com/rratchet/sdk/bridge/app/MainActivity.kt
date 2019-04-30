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
 * 文件名称：MainActivity.kt
 * 文件描述：
 *
 * 创 建 人：ASLai(laijianhua@rratchet.com)
 *
 * 上次修改时间：2019-04-30 15:24:58
 *
 * 修 改 人：ASLai(laijianhua@rratchet.com)
 * 修改时间：2019-04-30 15:26:41
 * 修改备注：
 */

package com.rratchet.sdk.bridge.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.JsPromptResult
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.rratchet.sdk.bridge.Bridge

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    var bridge: Bridge? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.web_view)

        initData()

        loadWebView()
    }

    fun onSendMessage(view: View?) {
        bridge!!.sendMessage("testCallJs", "Android调用JavaScript") { message ->
            Log.d("Bridge", message.toString())
            "success"
        }
    }

    private fun initData() {

        bridge = Bridge.create(webView)
        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsPrompt(view: WebView, url: String, message: String, defaultValue: String, result: JsPromptResult): Boolean {
                val prompt = bridge!!.acceptJavaScriptPrompt(message)
                return if (prompt == null || prompt.isEmpty()) {
                    super.onJsPrompt(view, url, message, defaultValue, result)
                } else {
                    result.confirm(prompt)
                    true
                }
            }
        }

        bridge!!.registerMessageHandler("testAndroid") { message, callback ->
            Log.d("Bridge", message.toString())
            message.data = "success"
            callback.onMessageCallback(message)
            "success"
        }

        bridge!!.registerMessageHandler("showToast") { message, callback ->
            Log.d("Bridge", message.toString())
            message.data = "success"
            callback.onMessageCallback(message)
            "showToast"
        }

    }

    private fun loadWebView() {
        val url = "file:///android_asset/www/index.html"
        Log.e("MainActivity", "loadUrl:$url")
        webView.loadUrl(url)
    }

}
