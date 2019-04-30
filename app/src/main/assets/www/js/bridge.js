const PROTOCOL_NAME = "bridge"
const HOST_NAME = "message"

const KEY_CALLBACK_ID = "callbackId"
const KEY_RESPONSE_ID = "responseId"
const KEY_HANDLER_NAME = "handlerName"

const messageCallbacks = {}
const messageHandlers = {}

/**
 * 生成回调id
 */
function _generate_callback_id() {

    var chars = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('');
    var uuid = [], i;

    // rfc4122, version 4 form
    var r;

    // rfc4122 requires these characters
    uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-';
    uuid[14] = '4';

    // Fill in random data.  At i==19 set the high bits of clock sequence as
    // per rfc4122, sec. 4.1.5
    for (i = 0; i < 36; i++) {
        if (!uuid[i]) {
            r = 0 | Math.random()*16;
            uuid[i] = chars[(i == 19) ? (r & 0x3) | 0x8 : r];
        }
    }
    return uuid.join('');
}

/**
 * 执行原生方法
 */
function _execute_native_method(message) {
    return prompt(PROTOCOL_NAME + '://' + HOST_NAME + '?' + JSON.stringify(message))
}

/**
 * 发送消息
 */
function sendMessage(message, callback) {

    if (!message || !message[KEY_HANDLER_NAME]) {
        return
    }

    if (callback) {
        const callbackId = _generate_callback_id()
        messageCallbacks[callbackId] = callback
        message[KEY_CALLBACK_ID] = callbackId
    }

    const result = _execute_native_method(message)

    return result
}

/**
 * 消息回调
 */
function onMessageCallback(message) {

    const responseId = message[KEY_RESPONSE_ID]
    if (!responseId) {
        return
    }

    const callback = messageCallbacks[responseId]
    if (!callback) {
        return
    }

    callback(message)
}

/**
 * 收到消息
 */
function onMessageHandle(messageJson) {

    const message = JSON.parse(messageJson)
    if (!message) {
        return
    }

    if (message[KEY_RESPONSE_ID]) {
        onMessageCallback(message)
        return
    }

    var callback
    const callbackId = message[KEY_CALLBACK_ID]
    if (callbackId) {
        callback = function (responseMessage) {
            responseMessage[KEY_CALLBACK_ID] = callbackId
            sendMessage(responseMessage)
        }
    }

    var handler
    const handlerName = message[KEY_HANDLER_NAME]
    if (handlerName) {
        handler = messageHandlers[handlerName]
    }

    if (handler) {
        handler(message, callback)
    } else {
        console.log('未找到对应的js处理：' + messageJson)
    }
}

/**
 * 注册消息持有者
 */
function registerMessageHandler(handlerName, handler) {

    if (!handlerName || !handler) {
        return false
    }
    messageHandlers[handlerName] = handler
    return true
}

/**
 * 注销消息持有者
 */
function unregisterMessageHandler(handlerName, handler) {

    if (!handlerName || !handler) {
        return false
    }

    if (!messageHandlers[handlerName]) {
        delete messageHandlers[handlerName]
        return true
    }

    return false
}

const bridge = window.bridge = {
    sendMessage: sendMessage,
    onMessageCallback: onMessageCallback,
    onMessageHandle: onMessageHandle,
    registerMessageHandler: registerMessageHandler,
    unregisterMessageHandler: unregisterMessageHandler
}