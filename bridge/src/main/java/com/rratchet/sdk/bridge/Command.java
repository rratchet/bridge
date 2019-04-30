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
 * 文件名称：Command.java
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

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
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
public class Command {


    /**
     * The Method name.
     */
    private String methodName;

    /**
     * The Params.
     */
    private Object[] params;

    /**
     * The Message.
     */
    private Message message;

    /**
     * Create command.
     *
     * @param handlerName the handler name
     * @param data        the data
     * @return the command
     */
    public static Command create(String handlerName, String data) {
        Command command = new Command();
        command.methodName = Config.RECEIVE_MESSAGE_METHOD;
        Message message = new Message();
        message.setHandlerName(handlerName);
        message.setData(data);
        command.message = message;
        return command;
    }

    /**
     * Custom command.
     *
     * @param methodName the method name
     * @param params     the params
     * @return the command
     */
    public static Command custom(String methodName, Object... params) {
        Command command = new Command();
        command.methodName = methodName;
        command.params = params;
        return command;
    }

    /**
     * Is json boolean.
     *
     * @param target the target
     * @return the boolean
     */
    static boolean isJson(String target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        }
        boolean tag = false;
        try {
            if (target.startsWith("[")) {
                new JSONArray(target);
            } else {
                new JSONObject(target);
            }
            tag = true;
        } catch (JSONException ignore) {
            tag = false;
        }
        return tag;
    }

    /**
     * Concat string.
     *
     * @param params the params
     * @return the string
     */
    static String concat(Object... params) {
        StringBuilder mStringBuilder = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            String param = String.valueOf(params[i]);
            if (!isJson(param)) {
                mStringBuilder.append("\"").append(param).append("\"");
            } else {
                mStringBuilder.append(param);
            }
            if (i != params.length - 1) {
                mStringBuilder.append(" , ");
            }
        }
        return mStringBuilder.toString();
    }


    /**
     * Gets message.
     *
     * @return the message
     */
    public Message getMessage() {
        return message;
    }

    /**
     * Transform string.
     *
     * @return the string
     */
    public String transform() {
        StringBuilder sb = new StringBuilder();
        sb.append("javascript:" + methodName);
        if (message != null) {
            sb.append("('").append(message.toString()).append("')");
        } else {
            if (params == null || params.length == 0) {
                sb.append("()");
            } else {
                sb.append("(").append(concat(params)).append(")");
            }
        }

        return sb.toString();
    }
}
