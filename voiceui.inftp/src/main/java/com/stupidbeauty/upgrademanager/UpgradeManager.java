package com.stupidbeauty.upgrademanager;

import io.github.g00fy2.versioncompare.Version;
// import com.stupidbeauty.hxlauncher.interfaces.LocalServerListLoadListener;
// import com.stupidbeauty.hxlauncher.rpc.CloudRequestorZzaqwb;
// import org.apache.commons.collections4.MultiMap;
// import org.apache.commons.collections4.map.MultiValueMap;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.StrictMode;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import android.util.Log;
import android.media.MediaDataSource;
import com.google.gson.Gson;
import com.upokecenter.cbor.CBORObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import com.google.gson.Gson;
import com.upokecenter.cbor.CBORObject;
import com.google.gson.Gson;
import com.stupidbeauty.voiceui.bean.VoicePackageMapJsonItem;
import com.stupidbeauty.voiceui.bean.VoicePackageUrlMapData;
import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashMap;
import android.content.Context;
import com.stupidbeauty.victoriafresh.VFile;
import com.stupidbeauty.grebe.DownloadRequestor;
import com.stupidbeauty.grebe.DownloadRequestorInterface;
import com.stupidbeauty.hxlauncher.asynctask.LoadVoicePackageUrlMapTask;
import com.stupidbeauty.hxlauncher.asynctask.LoadVoicePackageUrlMapInterface;
import com.stupidbeauty.hxlauncher.bean.ApplicationNameInternationalizationData;

public class UpgradeManager implements DownloadRequestorInterface, LoadVoicePackageUrlMapInterface
{
  private final DownloadRequestor downloadRequestor ; //!< Download requestor. For download url package map file.
  private VoicePackageUrlMapData voicePackageUrlMapData; //!<语音识别结果与软件包下载地址之间的映射。
  private int recognizeCounter=0; //!<识别计数器．
  private ErrorListener errorListener=null; //!< Error listener.
  private int port=1421; //!< Port.
  private boolean allowActiveMode=true; //!<  Whether to allow active mode.
  private static final String TAG="VoiceUi"; //!< 输出调试信息时使用的标记。
  private String recordSoundFilePath; //!< 录音文件路径．
  private MediaPlayer mediaPlayer;
  private static final float BEEP_VOLUME = 0.20f;
  private HashMap<String, String> voiceUiTextSoundFileMap=null; //!< 声音内容与声音文件名之间的映射关系。
  private HashMap<String, String> packageNameUrlMap; //!< 包名与下载地址之间的映射关系。

  /**
  * 设置包名与下载地址之间的映射关系。
  */
  @Override
  public void setPackageNameUrlMap (HashMap<String, String> packageNameUrlMap) 
  {
    this.packageNameUrlMap=packageNameUrlMap;
  } //public void setPackageNameUrlMap (HashMap<String, String> packageNameUrlMap)
  
  private HashMap<String, String> packageNameVersionNameMap; //!< 包名与可用版本号之间的映射关系。
  
  @Override
  public void setPackageNameVersionNameMap (HashMap<String, String> packageNameVersionNameMap) //!< 包名与可用版本号之间的映射关系。
  {
    this.packageNameVersionNameMap=packageNameVersionNameMap;
      
    compareVersionName(); // Compare version name.
  } //public void setPackageNameVersionNameMap (HashMap<String, String> packageNameVersionNameMap)
  
	/**
	 * 获取版本名字。
	 * @param packageName 包名。陈欣
	 * @return 这个软件包现在的版本名字。
	 */
	public String getVersionName(String packageName)
	{
      String versionName=null;
      try
      {
		PackageManager packageManager=context.getPackageManager(); //获取软件包管理器。
		PackageInfo packageInfo=packageManager.getPackageInfo(packageName,0); //获取对应的软件包信息。

        versionName= packageInfo.versionName; // 获取版本号名字。
      }
      catch (PackageManager.NameNotFoundException e) //未找到该软件包。
      {
		e.printStackTrace(); //报告错误。
      } //catch (PackageManager.NameNotFoundException e) //未找到该软件包。

      return versionName;
	} //public String getVersionName(String packageName)
	
	/**
	 *  // 获取可用的版本名字。
	 * @param packageName
	 * @return
	 */
	public String getAvailableVersionName(String packageName)
	{
      String result= packageNameVersionNameMap.get(packageName); // 获取可用 版本号名字。

      return result;
	} //public String getAvailableVersionName(String packageName)
	
  /**
  * Compare version name.
  */
  private void compareVersionName() 
  {
    String packageName=context.getPackageName(); // Get my own package name.
    
//       HxLauncherApplication hxlauncherApplication=HxLauncherApplication.getInstance(); // 获取应用对象。
      String currentVersionName=getVersionName(packageName); // 获取版本名字。

      String availableVersonName = getAvailableVersionName(packageName); // 获取可用的版本名字。

      Log.d(TAG, "checkUpgrade. avaialable version: " + availableVersonName + ", current version: " + currentVersionName); //Debug.

      Version availableVersion= new Version(availableVersonName); // 已有版本对象。

      if (availableVersion.isHigherThan(currentVersionName)) // 有新版本
      {
        startDownloadApk(); // Start download apk
      } //if (availableVersonName > currentVersionName) // 有新版本
      else // 无新版本
      {
//         hideUpgradeIcon(); // 隐藏升级按钮。
      } //else // 无新版本
  } // private void compareVersionName()
  
  /**
  * 报告，下载 finished.
  */
  @Override
  public void reportDownloadFinished(String packageName) 
  {
    loadVoicePackageUrlMap(); // Load the voice package url map.
  } // public void reportDownloadFinished(String packageName)
  
	/**
	 * 载入语音识别结果与包下载地址之间的映射。
	 */
	private void loadVoicePackageUrlMap()
	{
      LoadVoicePackageUrlMapTask translateRequestSendTask =new LoadVoicePackageUrlMapTask(); //创建异步任务。

      translateRequestSendTask.execute(this); //执行任务。
	} //private void loadVoicePackageUrlMap()

  /**
  * 报告，下载失败。
  */
  @Override
  public void  reportDownloadFailed(String packageName) 
  {
    //       陈欣
  } // public void  reportDownloadFailed(String packageName)

  public void setErrorListener(ErrorListener errorListener)    
  {
    this.errorListener = errorListener;
  } //public void setErrorListener(ErrorListener errorListener)    
    
  public void onError(Integer errorCode) 
  {
    if (errorListener!=null)
    {
      errorListener.onError(errorCode); // Report error.
    }
  } //public void onError(Integer errorCode)
    
  /**
  * Set to allow or not allow active mode.
  */
  public void setAllowActiveMode(boolean allowActiveMode)
  {
    this.allowActiveMode=allowActiveMode;
  } //private void setAllowActiveMode(allowActiveMode)
    
  public void setPort(int port)
  {
    this.port=port;
  } //public void setPort(int port)
        
  /**
  * 载入映射文件。
  */
  private HashMap<String, String> loadVoiceUiTextSoundFileMap() 
  {
    HashMap<String, String> packageNameApplicationNameMap=new HashMap<>(); // 结果。
    
    try 
    {
      String qrcFileName="voiceSoundMap.json"; //文件名。

      String fullQrcFileName=":/VoiceUi/"+qrcFileName; //构造完整的qrc文件名。
      
      int victoriaFreshDataFileId=context.getResources().getIdentifier("victoriafreshdata_voiceui", "raw", context.getPackageName()); //获取数据文件编号。
      int victoriaFreshIndexFileId=context.getResources().getIdentifier("victoriafresh_voiceui", "raw", context.getPackageName()); //获取索引文件编号。

      VFile qrcHtmlFile=new VFile(context, victoriaFreshIndexFileId, victoriaFreshDataFileId, fullQrcFileName); //qrc网页文件。

      String fileContent=qrcHtmlFile.getFileTextContent(); //获取文件的完整内容。
      
      Log.d(TAG, "loadVoiceUiTextSoundFileMap, file content: " + fileContent); // Debug.

      Gson gson=new Gson();

      voicePackageUrlMapData = gson.fromJson(fileContent, VoicePackageUrlMapData.class); //解析。

      packageNameApplicationNameMap=new HashMap<>(); //创建映射

      if (voicePackageUrlMapData!=null) //解析得到的映射数据不为空。
      {
        for(VoicePackageMapJsonItem currentItem: voicePackageUrlMapData.getVoicePackageMapJsonItemList()) //一个个地添加。
        {
          packageNameApplicationNameMap.put( currentItem.getPackageName(),currentItem.voiceCommand); //加入映射，包名与应用程序名的映射
        } //for(VoicePackageMapJsonItem currentItem: voicePackageUrlMapData.getVoicePackageMapJsonItemList()) //一个个地添加。
      } //if (voicePackageUrlMapData!=null) //解析得到的映射数据不为空。
    }
    catch (Exception ioe) 
    {
      mediaPlayer = null;
    }

    return packageNameApplicationNameMap;
  } // private void loadVoiceUiTextSoundFileMap()

  private MediaPlayer buildMediaPlayer(Context activity, String text)
  {
    voiceUiTextSoundFileMap = loadVoiceUiTextSoundFileMap(); // 载入映射文件。
  
    MediaPlayer mediaPlayer = new MediaPlayer();
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

//     AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.victoriafreshdata_voiceui); //提示音。

//     context.getPackageName()

//     int vfsDatafileDescriptor = context.getResources().getIdentifier("victoriafreshdata_voiceui", "raw", Constants.Literal.PACKAGE_NAME); //获取数据文件编号。
    int vfsDatafileDescriptor = context.getResources().getIdentifier("victoriafreshdata_voiceui", "raw", context.getPackageName()); //获取数据文件编号。

//     AssetFileDescriptor file = activity.getResources().openRawResourceFd(vfsDatafileDescriptor); //提示音。
//     AssetFileDescriptor file = activity.getResources().openRawResourceFd(com.stupidbeauty.voiceui.R.raw.victoriafreshdata_voiceui); //提示音。

    try 
    {
//       String qrcFileName="voicePackageNameMap.ost"; //文件名。
      String qrcFileName=voiceUiTextSoundFileMap.get(text); // 声音文件名。

      String fullQrcFileName=":/VoiceUi/"+qrcFileName; //构造完整的qrc文件名。
      
      int victoriaFreshDataFileId=context.getResources().getIdentifier("victoriafreshdata_voiceui", "raw", context.getPackageName()); //获取数据文件编号。
      int victoriaFreshIndexFileId=context.getResources().getIdentifier("victoriafresh_voiceui", "raw", context.getPackageName()); //获取索引文件编号。

//       int victoriaFreshDataFileId=context.getResources().getIdentifier("victoriafreshdata_voiceui", "raw", Constants.Literal.PACKAGE_NAME); //获取数据文件编号。
//       int victoriaFreshIndexFileId=context.getResources().getIdentifier("victoriafresh_voiceui", "raw", Constants.Literal.PACKAGE_NAME); //获取索引文件编号。


      VFile qrcHtmlFile=new VFile(context, victoriaFreshIndexFileId, victoriaFreshDataFileId, fullQrcFileName); //qrc网页文件。

      int soundLength=qrcHtmlFile.getLength();
      int soundStartOffset=qrcHtmlFile.getStartOffset();
      
      MediaDataSource soundMediaSource=qrcHtmlFile.getMediaDataSource(); // 获取媒体数据源。

      mediaPlayer.setDataSource(soundMediaSource); // 设置数据源。
      
      mediaPlayer.prepare();
    }
    catch (IOException ioe) 
    {
      ioe.printStackTrace(); // 报告错误。
      mediaPlayer = null;
    }
    return mediaPlayer;
  }

  public UpgradeManager(Context context) 
  {
    downloadRequestor = new DownloadRequestor(context); // Download requestor. For download url package map file.

    this.context = context;
  }
  
  @Override
  public Context getContext()
  {
    return context;
  }

  private Context context; //!< Context.

  public void say(String text)
  {
    mediaPlayer = buildMediaPlayer(context, text);
  } //public void start()
  
  /**
  * Start download apk
  */
  private void startDownloadApk() 
  {
    String packageName=context.getPackageName(); // Package name.
    String applicationName="LJ.Mei"; // Application name.
    String internationalizationName=packageNameUrlMap.get(packageName); // Data file url.
    boolean autoInstall=true; // Auto install.
    
    downloadRequestor.requestDownloadUrl(internationalizationName, internationalizationName, applicationName, packageName, this, autoInstall); // 要求下载网址
  } // private void startDownloadApk()

  /**
  * Check upgrade
  */
  public void checkUpgrade()
  {
    //     Chen xin.
    
    // Start downloading the data file:
    
    String packageName="S.Xin"; // Package name.
    String applicationName="LJ.Mei"; // Application name.
    String internationalizationName="https://stupidbeauty.com/ArticleImages/1837/voicePackageUrlMap.cbor.cx"; // Data file url.
    boolean noAutoInstall=false;
    
    downloadRequestor.requestDownloadUrl(internationalizationName, internationalizationName, applicationName, packageName, this, noAutoInstall); //要求下载网址
  } //public void commandRecognizebutton2()

  /**
    * 播放提示间，表明已经提交文字。
    */
  protected void playAlarm()
  {
    AudioManager audioManager=(AudioManager) (context.getSystemService(Context.AUDIO_SERVICE)); //获取声音管理器。

    int ringerMode=audioManager.getRingerMode(); //获取声音模式。

    if (ringerMode==AudioManager.RINGER_MODE_NORMAL) //有声音模式。
    {
      if (mediaPlayer!=null) // Media player exists.
      {
        mediaPlayer.start();
      } // if (mediaPlayer!=null) // Media player exists.
    } //if (ringerMode!=AudioManager.RINGER_MODE_NORMAL) //静音模式。
  } //protected void playAlarm()
}
