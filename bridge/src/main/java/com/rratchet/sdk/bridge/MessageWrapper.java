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
 * 文件名称：MessageWrapper.java
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

import android.util.Log;


import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.HashMap;

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
public class MessageWrapper {

    private final static String   TAG                   = MessageWrapper.class.getSimpleName();
    private final static String   RETURN_RESULT_FORMAT  = "{\"code\": %d, \"result\": %s}";
    private static final String   MSG_PROMPT_HEADER     = "Bridge:";
    private static final String   KEY_OBJ               = "obj";
    private static final String   KEY_METHOD            = "method";
    private static final String   KEY_TYPES             = "types";
    private static final String   KEY_ARGS              = "args";

    private HashMap<String, Method> mMethodsMap;
    private Object                  mInterfaceObj;
    private String                  mInterfacedName;
    private String                  mPreloadInterfaceJs;


    public String getPreloadInterfaceJs() {
        return mPreloadInterfaceJs;
    }

    private String getReturn(JSONObject reqJson, int stateCode, Object result, long time) {
        String insertRes;
        if (result == null) {
            insertRes = "null";
        } else if (result instanceof String) {
            result = ((String) result).replace("\"", "\\\"");
            insertRes = "\"".concat(String.valueOf(result)).concat("\"");
        } else { // 其他类型直接转换
            insertRes = String.valueOf(result);

            // 兼容：如果在解决WebView注入安全漏洞时，js注入采用的是XXX:function(){return prompt(...)}的形式，函数返回类型包括：void、int、boolean、String；
            // 在返回给网页（onJsPrompt方法中jsPromptResult.confirm）的时候强制返回的是String类型，所以在此将result的值加双引号兼容一下；
            // insertRes = "\"".concat(String.valueOf(result)).concat("\"");
        }
        String resStr = String.format(RETURN_RESULT_FORMAT, stateCode, insertRes);
            Log.d(TAG, "call time: " + (android.os.SystemClock.uptimeMillis() - time) + ", request: " + reqJson + ", result:" + resStr);
        return resStr;
    }

    private static String promptMsgFormat(String object, String method, String types, String args) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append(KEY_OBJ).append(":").append(object).append(",");
        sb.append(KEY_METHOD).append(":").append(method).append(",");
        sb.append(KEY_TYPES).append(":").append(types).append(",");
        sb.append(KEY_ARGS).append(":").append(args);
        sb.append("}");
        return sb.toString();
    }

    /**
     * 是否是“Java接口类中方法调用”的内部消息；
     *
     * @param message
     * @return
     */
    static boolean isSafeWebViewCallMsg(String message) {
        return message.startsWith(MSG_PROMPT_HEADER);
    }

    static JSONObject getMsgJSONObject(String message) {
        message = message.substring(MSG_PROMPT_HEADER.length());
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(message);
        } catch (JSONException e) {
            e.printStackTrace();
            jsonObject = new JSONObject();
        }
        return jsonObject;
    }

    static String getInterfacedName(JSONObject jsonObject) {
        return jsonObject.optString(KEY_OBJ);
    }

}
