package com.stupidbeauty.hxlauncher.rpc;

import com.stupidbeauty.hxlauncher.rpc.RecognizerResult;

/**
 * 用于接收识别结果、分析结果、错误事件的回调接口。
 */
public interface RecognizerListener {

    /**
     * 接收到识别和分析结果。
     * @param recognizerResult 结果对象。
     * @param isFinalResult 是否是最终结果。
     */
    void onResult(RecognizerResult recognizerResult, boolean isFinalResult);

    /**
     * 发生了错误事件。
     * @param speechError 错误事件信息对象。
     */
    void onError(SpeechError speechError);
}
