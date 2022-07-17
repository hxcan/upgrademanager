package com.stupidbeauty.hxlauncher.rpc;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.util.Log;
import com.stupidbeauty.hxlauncher.rpc.RecognizerResult;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * 内部接口。
 */
public class AsrRequestCallback implements Callback
{
    private SpeechRecognizer speechRecognizer=null; //!<语音识别器实例

    private Context context; //!<用于获取系统资源的上下文对象
    private static final String TAG="AsrRequestCallback"; //!<输出调试信息时使用的标记
    private RecognizerListener recognizerListener; //!<识别结果监听器

    public void setRecognizerListener(RecognizerListener recognizerListener) {
        this.recognizerListener = recognizerListener;
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e)
    {
        Log.d(TAG, "33, onFailure, exception: "); //Debug.
        e.printStackTrace();

        String exceptionClassName = e.getClass().getName();

        Log.d(TAG, "40, onFailure, class name: " + exceptionClassName);

        int errorCode= ErrorCode.ERROR_UNKNOWN; //错误码

        if (exceptionClassName.equals("java.net.SocketTimeoutException")) //网络超时
        {
            errorCode=ErrorCode.ERROR_NETWORK_TIMEOUT; //错误码

        } //if (exceptionClassName.equals("java.net.SocketTimeoutException")) //网络超时
        else if (exceptionClassName.equals("java.net.ConnectException")) //网络连接失败
        {
            errorCode=ErrorCode.ERROR_NET_CONNECTSOCK; //网络连接失败
        } //else if (exceptionClassName.equals("java.net.ConnectException")) //网络连接失败
        else if (exceptionClassName.equals("java.net.UnknownServiceException")) //明文HTTP
        {
            errorCode=ErrorCode.ERROR_CLEARTEXT_HTTP_NOT_PERMITTED; //明文HTTP
        } //else if (exceptionClassName.equals("java.net.UnknownServiceException")) //明文HTTP
        else if (exceptionClassName.equals("java.net.UnknownHostException")) //网络不通
        {
            errorCode=ErrorCode.ERROR_NETWORK_UNREACHABLE; //未连接到网络
        } //else if (exceptionClassName.equals("java.net.UnknownServiceException")) //明文HTTP

        Handler uiHandler = new Handler(Looper.getMainLooper());

        final int finalErrorCode = errorCode;

        Runnable runnable= new Runnable()
        {
            /**
             * 具体执行的代码
             */
            public void run()
            {
                SpeechError speechError=new SpeechError(finalErrorCode); //构造错误对象

                recognizerListener.onError(speechError); //报告错误
            } //public void run()
        };

        uiHandler.post(runnable);
    } //public void onFailure(@NotNull Call call, @NotNull IOException e)

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException 
    {
        final String responseContent=response.body().string(); //获取回复结果

        Handler uiHandler = new Handler(Looper.getMainLooper());

        Runnable runnable= new Runnable()
        {
            /**
             * 具体执行的代码
             */
            public void run()
            {
                reportResponse(responseContent); //报告回复结果
            } //public void run()
        };

        uiHandler.post(runnable);
    }

    /**
     * 报告回复结果
     * @param responseContent 回复内容
     */
    private void reportResponse(String responseContent)
    {
        RecognizerResult resultObject=new RecognizerResult(responseContent); //构造结果对象

        recognizerListener.onResult(resultObject, true); //报告结果
    } //private void reportResponse(String responseContent)
}
