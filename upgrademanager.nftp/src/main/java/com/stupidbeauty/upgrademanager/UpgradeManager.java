package com.stupidbeauty.upgrademanager;

import java.util.Random;
import android.os.Debug;
import com.stupidbeauty.upgrademanager.UpgradeManager;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import io.github.g00fy2.versioncompare.Version;
import com.stupidbeauty.upgrademanager.listener.PackageNameUrlMapDataListener;
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
import java.util.List;
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
import android.media.MediaPlayer;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashMap;
import android.content.Context;
import com.stupidbeauty.victoriafresh.VFile;
import com.stupidbeauty.upgrademanager.asynctask.LoadVoicePackageUrlMapTask;
import com.stupidbeauty.upgrademanager.asynctask.LoadVoicePackageUrlMapInterface;

public class UpgradeManager implements DownloadRequestorInterface, LoadVoicePackageUrlMapInterface
{
  private boolean checkingUpgrade=false; //!< if we are already checking for upgrade.
  private int checkCounter=0; //!< Check counter.
  private HashMap<String, String> packageNameInformationUrlMap; //!< 包名与信息页面地址之间的映射关系。
  private final DownloadRequestor downloadRequestor ; //!< Download requestor. For download url package map file.
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
	private HashMap<String, String> voicePackageUrlMap; //!<语音识别结果与包名之间的映射关系。
  private HashMap<String, String> packageNameInstallerTypeMap; //!< Map of package name to installer type.
  private HashMap<String, List<String> > packageNameExtraPackageNamesMap; //!< Map of packge name to extra package names.
  private PackageNameUrlMapDataListener packageNameUrlMapDataListener; //!< Package name url map data listener.

  /**
  * Set package name url map data listener.
  */
  public void setPackageNameUrlMapDataListener(PackageNameUrlMapDataListener listener)
  {
    packageNameUrlMapDataListener = listener;
  } // public void setPackageNameUrlMapDataListener(PackageNameUrlMapDataListener listener)

  /**
  * Set the map of package name to extra package names list.
  */
  public void setPackageNameExtraPackageNamesMap(HashMap<String, List<String> > packageNameExtraPackageNamesMap)
  {
    //     陈欣
    this.packageNameExtraPackageNamesMap=packageNameExtraPackageNamesMap;
      
    if (packageNameUrlMapDataListener!=null) // Listenre exists.
    {
      packageNameUrlMapDataListener.setPackageNameExtraPackageNamesMap(packageNameExtraPackageNamesMap);
    } // if (packageNameUrlMapDataListener!=null) // Listenre exists.
    
    if (packageNameExtraPackageNamesMap.size()==0) // Empty map
    {
      checkUpgrade(); // Check pugrade again.
    } // if (packageNameExtraPackageNamesMap.size()==0) // Empty map
  } // public void setPackageNameExtraPackageNamesMap(HashMap<String, List<String> > packageNameExtraPackageNamesMap)

  /**
  * 设置包名与信息页面地址之间的映射。
  */
  public void setPackageNameInformationUrlMap(HashMap<String, String> packageNameInformationUrlMap) 
  {
    this.packageNameInformationUrlMap=packageNameInformationUrlMap;
      
      if (packageNameUrlMapDataListener!=null) // Listenre exists.
      {
        packageNameUrlMapDataListener.setPackageNameInformationUrlMap(packageNameInformationUrlMap);
      } // if (packageNameUrlMapDataListener!=null) // Listenre exists.
	} // public void setPackageNameInformationUrlMap(HashMap<String, String> packageNameInformationUrlMap)

  /**
  * 设置包名与下载地址之间的映射关系。
  */
  @Override
  public void setPackageNameUrlMap (HashMap<String, String> packageNameUrlMap) 
  {
    this.packageNameUrlMap=packageNameUrlMap;
    
    if (packageNameUrlMapDataListener!=null)
    {
      packageNameUrlMapDataListener.setPackageNameUrlMap(packageNameUrlMap);
    } // if (packageNameUrlMapDataListener!=null)
  } //public void setPackageNameUrlMap (HashMap<String, String> packageNameUrlMap)
  
  /**
  * 设置应用程序名字与下载地址之间的映射关系。
  */
  @Override
  public void setVoicePackageUrlMap (HashMap<String, String> voicePackageUrlMap) 
  {
    this.voicePackageUrlMap=voicePackageUrlMap;
    
    if (packageNameUrlMapDataListener!=null)
    {
      packageNameUrlMapDataListener.setVoicePackageUrlMap(voicePackageUrlMap);
    } // if (packageNameUrlMapDataListener!=null)
  } //public void setPackageNameUrlMap (HashMap<String, String> packageNameUrlMap)
  
  /**
  * Set package name installer type map.
  */
  @Override
  public void setPackageNameInstallerTypeMap(HashMap<String, String> packageNameInstallerTypeMap)
  {
    this.packageNameInstallerTypeMap=packageNameInstallerTypeMap;
    
    if (packageNameUrlMapDataListener!=null) // There is a listener.
    {
      packageNameUrlMapDataListener.setPackageNameInstallerTypeMap(packageNameInstallerTypeMap);
    } // if (packageNameUrlMapDataListener!=null) // There is a listener.
  } // public void setPackageNameInstallerTypeMap(HashMap<String, String> packageNameInstallerTypeMap)
  
  private HashMap<String, String> packageNameVersionNameMap; //!< 包名与可用版本号之间的映射关系。
  
  @Override
  public void setPackageNameVersionNameMap (HashMap<String, String> packageNameVersionNameMap) //!< 包名与可用版本号之间的映射关系。
  {
    this.packageNameVersionNameMap=packageNameVersionNameMap;

    if (packageNameUrlMapDataListener!=null)
    {
      packageNameUrlMapDataListener.setPackageNameVersionNameMap(packageNameVersionNameMap);
    } // if (packageNameUrlMapDataListener!=null)

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
      } //else // 无新版本
  } // private void compareVersionName()
  
  /**
  * 报告，下载 finished.
  */
  @Override
  public void reportDownloadFinished(String packageName, String filePath)
  {
    checkingUpgrade=false;
    Log.d(TAG, "reportDownloadFinished, 230, loading package url from downloaded file"); // Debug.

    loadVoicePackageUrlMap(filePath); // Load the voice package url map.
  } // public void reportDownloadFinished(String packageName)
  
  /**
  * 载入语音识别结果与包下载地址之间的映射。
  */
  private void loadVoicePackageUrlMap(String filePath)
  {
    LoadVoicePackageUrlMapTask translateRequestSendTask =new LoadVoicePackageUrlMapTask(); //创建异步任务。

    translateRequestSendTask.execute(this, filePath); //执行任务。
  } //private void loadVoicePackageUrlMap()

  /**
  * 报告，下载失败。
  */
  @Override
  public void  reportDownloadFailed(String packageName) 
  {
    checkingUpgrade=false;
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
        
  public UpgradeManager(Context context) 
  {
    downloadRequestor = new DownloadRequestor(context); // Download requestor. For download url package map file.

    this.context = context;
  } // public UpgradeManager(Context context) 
  
  @Override
  public Context getContext()
  {
    return context;
  } // public Context getContext()

  private Context context; //!< Context.

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
//     HxLauncherApplication hxlauncherApplication=HxLauncherApplication.getInstance(); // 获取应用对象。

    String fileName="voicePackageUrlMap.cbor.cx.exz";

    File downloadFolder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

    final String wholePath =downloadFolder.getPath()+ File.separator  + fileName;

    Log.d(TAG, "checkUpgrade, 319, loading package url from local file"); // Debug.
    loadVoicePackageUrlMap(wholePath); // Load the voice package url map.

    if (checkingUpgrade) // It is already checking.
    {
    } // if (checkingUpgrade) // It is already checking.
    else // not already checking.
    {
      // Start downloading the data file:
      
      String packageName="S.Xin"; // Package name.
      String applicationName="LJ.Mei"; // Application name.
      
      String packageNameApplicationId=context.getPackageName(); // Package name.


      String internationalizationName="http://139.162.164.8/ArticleImages/1837/voicePackageUrlMap.cbor.cx.exz?applicationId="+packageNameApplicationId+"&counter="+checkCounter; // Data file url. compressed.
      
      
      Random random=new Random();
      
      int nextInt=random.nextInt();
      
//       if ((nextInt % 2) == 0)
      {
        internationalizationName="https://stupidbeauty.com/ArticleImages/1837/voicePackageUrlMap.cbor.cx.exz?applicationId="+packageNameApplicationId+"&counter="+checkCounter; // Data file url. compressed.
      }
      
      
      
      checkCounter++;
      
      boolean noAutoInstall=false;
      
      downloadRequestor.requestDownloadUrl(internationalizationName, internationalizationName, applicationName, packageName, this, noAutoInstall); //要求下载网址
      
      checkingUpgrade=true;
    } // else // not already checking.
  } //public void commandRecognizebutton2()
}
