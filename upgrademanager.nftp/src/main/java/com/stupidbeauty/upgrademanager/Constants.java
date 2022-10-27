package com.stupidbeauty.upgrademanager;

import android.os.Environment;
import java.util.Set;
import java.io.File;

/**
 * 一些常量的定义。
 * @author root 蔡火胜。
 *
 */
public class Constants 
{
  public static class Literal
  {
    public static final String PACKAGE_NAME = "com.stupidbeauty.voiceui"; //!< 包名。
    public static final String PASSWORD_CX = "236000"; //!< 默认解锁密码。
  }

  /**
  * 文件路径。
  * @author root 蔡火胜。
  *
  */
  public static class FilePath
  {
    public static final String UnknownDeviceMacAddr = "UnknownMacAddr"; //!<未知的网卡物理地址。
  } //public static class FilePath

  /**
  * Operations。
  * @author root 蔡火胜。
  *
  */
  public final class Operation
  {
    public static final String TestShutDown = "com.stupidbeauty.shutdownat2100.testShutDown"; //!<测试关机。
    public static final String ReportMessage="com.stupidbeauty.shutdownat2100.reportMessage"; //!<报告消息到来。
    public static final String PinShortcut="com.stupidbeauty.hxlauncher.pinShortcut"; //!<钉住快捷方式。
    public static final String ToggleBuiltinShortcuts="com.stupidbeauty.hxlauncher.toggleBuiltinShortcutss"; //!<切换是否显示内置快捷方式。
    public static final String ToggleHiveLayout="com.stupidbeauty.hxlauncher.toggleHiveLayout"; //!<切换是否要使用蜂窝布局
    public static final String UnlinkVoiceCommand="com.stupidbeauty.hxlauncher.unlinkVoiceCommand"; //!<断开语音指令的链接
  } //public final class FAQLangKey

  public final class Url
  {
    public static final String WebSocketServerUrl = "ws://192.168.0.108:20402/";
  }

  /**
    * 原始消息。
    * @author root
    *
    */
  public final class NativeMessage
  {
    public static final String APPLICATION_LAUNCHED = "com.stupidbeauty.hxlauncher.constants.nativemessage.applicationLaunched"; //!<应用程序被启动。
    public static final String APPLICATION_LAUNCHED_PACKAGE_KEY = "com.stupidbeauty.hxlauncher.constants.nativemessage.applicationLaunchedPackageKey"; //!<被启动的应用程序的包名的参数键。
  } //public final class NativeMessage

  /**
    * 目录路径。
    * @author root 蔡火胜。
    *
    */
  public static class DirPath
  {
    public static final String FARMING_BOOK_APP_SD_CARD_PATH = Environment.getExternalStorageDirectory().getPath()+ File.separator+ "etc" + File.separator+"ShutDownAt2100"; //女神相机的路径。
    public static final String DCIM_SD_CARD_PATH = Environment.getExternalStorageDirectory().getPath()+ File.separator+ Environment.DIRECTORY_DCIM; //!<相册的路径。
  } //public static class DirPath

  public final class Actions
  {
    public static final String LegacyInstallShortcut = "com.android.launcher.action.INSTALL_SHORTCUT"; //!<传统的安装快捷方式。
  }

  public final class Numbers
  {
    public static final int IgnoreVoiceResultLength=1; //!<短于 这个长度的语音识别结果，不处理
  }
} //public class Constants
