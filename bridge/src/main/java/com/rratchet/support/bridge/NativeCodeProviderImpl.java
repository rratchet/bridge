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
 * 文件名称：NativeCodeProviderImpl.java
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

package com.rratchet.support.bridge;

import android.net.Uri;

import org.json.JSONObject;

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
class NativeCodeProviderImpl implements NativeCodeProvider {

    private Bridge.MessageListener mMessageListener;

    public NativeCodeProviderImpl(Bridge.MessageListener messageListener) {
        mMessageListener = messageListener;
    }

    @Override
    public boolean checkSafeCall(String message) {
        Uri uri = Uri.parse(message);
        if (uri == null) {
            return false;
        }

        if (!Config.PROTOCOL_SCHEME_NAME.equalsIgnoreCase(uri.getScheme())) {
            return false;
        }
        return true;
    }

    @Override
    public String execute(String message) {

        Uri uri = Uri.parse(message);

        String json = uri.getQuery();

        int code = -1;
        String result = null;

        try {
            JSONObject jsonObject = new JSONObject(json);
            String callbackId = jsonObject.optString(Config.KEY_CALLBACK_ID, "");
            String responseId = jsonObject.optString(Config.KEY_RESPONSE_ID, "");
            String handlerName = jsonObject.optString(Config.KEY_HANDLER_NAME, "");
            String data = jsonObject.optString(Config.KEY_DATA, "");

            Message entity = new Message();
            entity.callbackId = callbackId;
            entity.responseId = responseId;
            entity.handlerName = handlerName;
            entity.data = data;

            if (entity.isResponse()) {
                code = 0;
                result = mMessageListener.onMessageCallback(entity);
            } else {
                code = 0;
                result = mMessageListener.onMessageHandle(entity);
            }

        } catch (Exception e) {
            e.printStackTrace();
            code = -1;
            result = e.getLocalizedMessage();
        }

        String value = String.format(Config.RETURN_RESULT_FORMAT, code, result);
        return value;
    }

}
