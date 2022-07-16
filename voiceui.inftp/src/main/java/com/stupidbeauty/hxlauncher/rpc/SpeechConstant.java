package com.wise4ai.voiceassistantsdk;

/**
 * 与SDK相关的常量定义
 */
public class SpeechConstant
{
    /**
     * 应用程序通过SDK向维知语音云发送请求时使用的标识。
     */
    public static final String APPID = "appid";


    /**
     * 用于保存录音文件内容的文件完整路径。接下来的一次语音识别过程的录音内容会被保存到该文件中。
     */
    public static final String ASR_AUDIO_PATH = "asr_audio_path";

    /**
     * 后端接口地址。
     */
    public static final String BACKEND_URL="backend_url";

    /**
     * 设备编号。用于唯一标识当前应用程序运行于其上的设备。
     */
    public static final String EQUIPMENT_NO = "equipment_no"; //!<

    /**
     * 会话编号。本次用户交互会话的编号。
     */
    public static final String SESSION_ID = "sid";

    /**
     * 采样率。语音识别的采样率。
     */
    public static final String SAMPLE_RATE = "sample_rate";

    /**
     * 用户编号。
     */
    public static final String USER_ID = "user_id";

}