package com.stupidbeauty.upgrademanager;

import java.util.Timer;
import java.util.TimerTask;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import java.util.TimerTask;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
// import com.stupidbeauty.voiceui.VoiceUi;
import android.os.Handler;
import android.os.Looper;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.app.Application;
import com.stupidbeauty.upgrademanager.R;
import android.app.NotificationChannel;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import androidx.core.content.FileProvider;
import android.database.ContentObserver;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ClipData;
// import com.stupidbeauty.hxlauncher.rpc.DownloadResult;
// import com.stupidbeauty.hxlauncher.rpc.DownloadListener;
import org.apache.commons.io.FileUtils;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import org.apache.commons.io.FilenameUtils;
import java.io.File;

public class UmDownloadRequestor
{
  private Timer timerObj = null; //!< The timer of cancelling download when no progress for a long time.
  private String actionName; //!< Construct action name.
  private Notification continiusNotification=null; //!<记录的通知
  private DownloadRequestorInterface launcherActivity=null; //!< 启动活动。
  private int NOTIFICATION = 84951; //!< 通知编号。陈欣
  private boolean autoInstall=false; //!< Whether to auto install.
  private Context baseApplication = null; //!< Context
  private String downloadedFilePath; //!< Remember downloaded file path.
  private String packageName=null; //!< 包名。
  public Future<File> fileDownloadFuture; //!<The file download future.
  private NotificationManager mNM;

  private static final String TAG="UmDownloadRequestor"; //!< 输出调试信息了时使用的标记

  private long downloadId; //!<当前的下载编号

  /**
  * 要求安装应用
  * @param downloadFilePath 应用安装包路径
  */
  private void requestInstall(String downloadFilePath)
  {
    String type = "application/vnd.android.package-archive";

    File file=new File(downloadFilePath);
        
    Intent intent = new Intent(Intent.ACTION_VIEW);
      
//       04-01 16:12:41.051 19837 19837 E AndroidRuntime: android.content.ActivityNotFoundException: No Activity found to handle Intent { act=android.intent.action.VIEW dat=file:///storage/emulated/0/Android/data/com.stupidbeauty.hxlauncher/files/Download/5F1E59D37ED5FCA5542C7EB86977A9D4.apk typ=application/vnd.android.package-archive flg=0x10000000 }

    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) 
    {
      try
      {
        String applicationPackageName = baseApplication.getPackageName();
        Uri downloadedApk = FileProvider.getUriForFile(baseApplication, applicationPackageName + ".com.stupidbeauty.upgrademanager.fileprovider", file);

        intent.setClipData(ClipData.newRawUri("", downloadedApk));
          
        intent.setDataAndType(downloadedApk, type);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        
        Log.d(TAG, "requestInstall, intent: " + intent); // Debug.
      }
      catch(IllegalArgumentException e)
      {
//       4/3/22 17:22java.lang.IllegalArgumentException: Failed to find configured root that contains /storage/emulated/0/download/b75da43000c64716bd5a688e65bfb370.apk

        e.printStackTrace();
        
        intent.setDataAndType(Uri.fromFile(file), type);
        Log.d(TAG, "requestInstall, intent: " + intent); // Debug.
      } // catch(IllegalArgumentException e)
    }
    else 
    {
      intent.setDataAndType(Uri.fromFile(file), type);
      Log.d(TAG, "requestInstall, intent: " + intent); // Debug.
    }

    Log.d(TAG, "requestInstall, starting activity to install apk"); // Debug.

    baseApplication.startActivity(intent);
  } //private void requestInstall(String downloadFilePath)

  public UmDownloadRequestor(Context context)
  {
    baseApplication = context; //获取应用程序对象。
    
    actionName="com.stupidbeauty.upgrademanager."+ baseApplication.getPackageName(); // Construct action name.
    
    mNM = (NotificationManager)baseApplication.getSystemService(Context.NOTIFICATION_SERVICE); // Get notification manager.
  } // public UmDownloadRequestor(Context context)

  /**
  * Report download finished.
  */
  private void notifyDownloadFinish(String wholePath)
  {
    if (launcherActivity!=null)
    {
      launcherActivity.reportDownloadFinished(packageName, wholePath); // 报告，下载 finished.
    }
  } // private void notifyDownloadFinish()
    
  /**
  * 通知，下载失败。
  */
  private void  notifyDownloadFail() 
  {
    String contentText="Failed to download";

    if (launcherActivity!=null)
    {
      launcherActivity.reportDownloadFailed(packageName); // 报告，下载失败。
    }
  } //private void  notifyDownloadFail()
    
  /**
  * 请求下载指定网址的安装包。
  */
  public void requestDownloadUrl(Uri uri, String refererUrl, String applicationName, String packageName)
  {
    boolean shouldDownload=false; // 是否应当下载。
        
    shouldDownload=true; // 应当下载。

    Log.d(TAG, "requestDownloadUrl, download file path: " + uri); //debug.

    String fullUrl = uri.toString();

    Log.d(TAG, "requestDownloadUrl, url scheme: " + uri.getScheme()); //debug.

    downloadByIon(uri); // 使用离子下载来下载。
  }
  
  /**
  * 检查是不是APK文件。
  */
  private boolean checkIsApkFile(String apkFilePath) 
  {
    boolean result=false;

    PackageManager packageManager = baseApplication.getPackageManager();
    
    PackageInfo packageInfo=packageManager.getPackageArchiveInfo(apkFilePath, 0);
    
    if (packageInfo!=null) // 有有效的包信息。
    {
      String versionName=packageInfo.versionName; // Get the versin name.

      if (packageName==null) // No package name provided
      {
        result=true; // It is apk file.
      } // if (packageName==null) // No package name provided
      else if (packageName.equals(packageInfo.packageName)) // Package name euqals
      {
        result=true; // It is apk file.
      } // else if (packageName.equals(packageInfo.packageName)) // Package name euqals
    }
    
    Log.d(TAG, "checkIsApkFile, result: "+ result); // Debug.

    return result;
  } // private boolean checkIsApkFile(String apkFilePath)
  
  /**
  * Cancel download.
  */
  public void cancelDownload()
  {
//     HxLauncherApplication baseApplication = HxLauncherApplication.getInstance(); //获取应用程序对象。
    Ion.getDefault(baseApplication).cancelAll(baseApplication);
  } // public void cancelDownload() // Cancel download.

  /**
  * 使用离子下载来下载。
  * @param uri 要下载的网址。
  */
  private void downloadByIon(Uri uri)
  {
    final String targetUrl=uri.toString(); //获取目标URL。

    String fileName=uri.getLastPathSegment(); // 获取文件名。陈欣

    File downloadFolder = baseApplication.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

    final String wholePath =downloadFolder.getPath()+ File.separator  + fileName;
    
    downloadedFilePath=wholePath; // Remember downloaded file path.

    fileDownloadFuture= Ion.with(baseApplication)
      .load(targetUrl) 
      .setTimeout(15000) //Set the time out to be 15s.
      .progress(new ProgressCallback() 
      {
        @Override
        public void onProgress(long downloaded, long total) 
        {
          timerObj.cancel(); // Cancel the timer of cancel.
          startTimeoutCancelTimer(); // Start time out cancel timer.
          Log.d(TAG, "downloadByIon, 274, progress: " + downloaded + "/" + total + ", " + targetUrl); // 报告进度。
        }
      })
      .setLogging(TAG, Log.DEBUG).write(new File( wholePath))
      .setCallback(new FutureCallback<File>() 
      {
        @Override
        public void onCompleted(Exception e, File file) 
        {
          timerObj.cancel(); // Cancel the timer of cancel.
          
          // download done...
          // do stuff with the File or error

          if (e!=null) //Some error occured.
          {
//             Toast.makeText(baseApplication, "Download Failed" + targetUrl, Toast.LENGTH_SHORT).show();

            Log.d(TAG,"download error:"); //Debug.
            e.printStackTrace(); //Report error.
                            
            //                             陈欣
            notifyDownloadFail(); // 报告下载失败。
          } //if (e!=null) //Some error occured.
          else // 下载完毕
          {
//             Toast.makeText(baseApplication, "Download Completed" + wholePath, Toast.LENGTH_SHORT).show();

            if (autoInstall) // Auto install
            {
              if (checkIsApkFile(wholePath)) // 是安装包文件。
              {
                registerReceiverInstall(); // Register receiver of install.
              
                showNotificationInstall(); // Show notification to request install.
//                 requestInstall(wholePath); // 要求安装。陈欣。
              } // if (checkIsApkFile(wholePath)) // 是安装包文件。
              else // 不是安装包。
              {
                notifyDownloadFail(); // 报告下载失败。
              } // else // 不是安装包。
            }
            else // Not auto install
            {
              notifyDownloadFinish(wholePath); // Report download finished.
            }
          } //else // 下载完毕
        }
      });
      startTimeoutCancelTimer(); // Start time out cancel timer.
    } //private void downloadByIon(Uri uri)
    
  /**
  * Start time out cancel timer.
  */
  private void startTimeoutCancelTimer() 
  {
    //    chen xin.

    timerObj = new Timer();
    TimerTask timerTaskObj = new TimerTask() 
    {
      public void run() 
      {
        Handler uiHandler = new Handler(Looper.getMainLooper());

        Runnable runnable= new Runnable()
        {
          /**
          * 具体执行的代码
          */
          public void run()
          {
            Log.d(TAG, "startTimeoutCancelTimer, 390, cancelling"); // Debug.

            cancelDownload(); // Cancel download.
              
            notifyDownloadFail(); // Notify download fail.
          } //public void run()
        };

        uiHandler.post(runnable);
      }
    };
    Log.d(TAG, "startTimeoutCancelTimer, 358, scheduling"); // Debug.
    timerObj.schedule(timerTaskObj, 60*1000); // 延时启动。
  } // private void startTimeoutCancelTimer()
  
  /**
  * Un register install receiver.
  */
  private void unregisterReceiverInstall() 
  {
    baseApplication.unregisterReceiver(mBroadcastReceiver);
  } // private void unregisterReceiverInstall()
    
  /**
  * Register receiver of install.
  */
  private void registerReceiverInstall() 
  {
    long startTimestamp=System.currentTimeMillis(); // 记录开始时间戳。
    Log.w(TAG, "registerBroadcastReceiver, 1876, enter registerBroadcastReceiver, timestamp: " + System.currentTimeMillis()); //Debug.
    Log.d(TAG, "registerBroadcastReceiver."); //Debug.

    //注册全局的广播接收器：

    //兰心输入法正在为某个软件包输入：
    IntentFilter lanimeInputtingIntentFilter=new IntentFilter(); //创建意图过滤器。
    lanimeInputtingIntentFilter.addAction(actionName); // Execute upgrade

    baseApplication.registerReceiver(mBroadcastReceiver, lanimeInputtingIntentFilter); //注册广播事件接收器。
  } // private void registerReceiverInstall()

  /**
  * 广播接收器。
  */
  private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
  {
    private final String TAG="BroadcastReceiver"; //!<输出调试信息时使用的标记。

    @SuppressWarnings("ConstantConditions")
    @Override
      /**
      * 接收到广播。
      */
      public void onReceive(Context context, Intent intent)
      {
        String action = intent.getAction(); //获取广播中带的动作字符串。

        Log.d(TAG,"1587, onReceive,got broadcast:"+action + ", equals package_added?: " + (Intent.ACTION_PACKAGE_ADDED.equals(action))); //Debug.

        if (actionName.equals(action)) // Upgrade.
        {
          requestInstall(downloadedFilePath); // Request install.
        
          unregisterReceiverInstall(); // Un register install receiver.
        } //if (Constants.NativeMessage.APPLICATION_LAUNCHED.equals(action)) //虚拟卡启动结果。
        else if (Intent.ACTION_WALLPAPER_CHANGED.equals(action)) //壁纸变化。
        {
        } //else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) //应用被安装。
      } //public void onReceive(Context context, Intent intent)
    }; //private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()

    /**
    * Show notification to request install.
    */
    private void showNotificationInstall() 
    {
      // In this sample, we'll use the same text for the ticker and the expanded notification
    

      // The PendingIntent to launch our activity if the user selects this notification
      PendingIntent contentIntent = PendingIntent.getBroadcast(baseApplication, 0, new Intent(actionName), 0); // Set a broadcast intent.

      CharSequence downloadingText=baseApplication.getText(R.string.foundNewVersion); // 构造字符串，正在下载。陈欣。

      NotificationChannel chan = new NotificationChannel( "#include", "My Foreground Service", NotificationManager.IMPORTANCE_LOW);
            
      mNM.createNotificationChannel(chan);
    
      ApplicationInfo applicationInfo = baseApplication.getApplicationInfo(); // Get application info object.
    
      int applicationIcon=applicationInfo.icon; //获取应用程序的图标。
      int applicationLabel=applicationInfo.labelRes; //获取应用程序的文字。

      CharSequence text = baseApplication.getText(applicationLabel);

      // Set the info for the views that show in the notification panel.
      Notification notification = new Notification.Builder(baseApplication)
        //       .setSmallIcon(R.drawable.ic_launcher)  // the status icon
        .setSmallIcon(applicationIcon)  // the status icon
        .setTicker(text)  // the status text
        .setWhen(System.currentTimeMillis())  // the time stamp
        .setContentTitle(baseApplication.getText(applicationLabel))  // the label of the entry
        //       .setContentTitle(applicationLabel)  // the label of the entry
        .setContentText(downloadingText)  // the contents of the entry
        .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
        .setPriority(Notification.PRIORITY_HIGH)   // heads-up
        .setChannelId("#include")
        .build();

    continiusNotification=notification; //记录通知

    // Send the notification.
    mNM.notify(NOTIFICATION, notification);

    } // private void showNotificationInstall()

    /**
     * 使用云端请求器来下载。
     * @param uri 要下载的网址
     */
    private void downloadByCloudRequestor(Uri uri, String packageName)
    {
//       cloudRequestorZzaqwb.startDownload(uri, voiceCommandListerner, packageName); // 开始下载安装包。陈欣
    } //private void downloadByCloudRequestor(Uri uri)

    private void downloadByDownloadManager(Uri uri, String refererUrl, String applicationName, Context baseApplication) 
    {
      final DownloadManager dManager = (DownloadManager) baseApplication.getSystemService(Context.DOWNLOAD_SERVICE); //Get the download manager.
      DownloadManager.Request request = new DownloadManager.Request(uri); //Create the request.

      String fileName = FilenameUtils.getName(uri.getLastPathSegment()); //解析出文件名。

      // 设置下载路径和文件名
      request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName); //设置下载路径的文件名。
      
//       04-01 14:53:17.995  4213  4213 E AndroidRuntime: java.lang.IllegalStateException: Not one of standard directories: download

      request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //When completed , notify.

      // 设置为可见和可管理
      request.setVisibleInDownloadsUi(true); //Visible.

      request.addRequestHeader("Referer", refererUrl); //设置引荐网址。

      request.setTitle(applicationName); //设置标题

      //下载。Get the download id.
      downloadId = dManager.enqueue(request);
    }

    public void requestDownloadUrl(String url, String refererUrl, String applicatinName, String packageName, DownloadRequestorInterface launcherActivity, boolean autoInstall)
    {
      this.launcherActivity=launcherActivity;
      this.packageName=packageName;
      this.autoInstall=autoInstall;
    
      Uri uri=Uri.parse(url);

      requestDownloadUrl(uri, refererUrl, applicatinName, packageName);
    }
}
