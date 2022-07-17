package com.stupidbeauty.grebe;

// import com.stupidbeauty.voiceui.VoiceUi;
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
// import com.stupidbeauty.hxlauncher.application.HxLauncherApplication;
// import com.stupidbeauty.hxlauncher.bean.ApplicationListData;
// import com.stupidbeauty.hxlauncher.rpc.CloudRequestorZzaqwb;
// import com.stupidbeauty.hxlauncher.LauncherActivity;
// import com.stupidbeauty.hxlauncher.R;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
// import com.stupidbeauty.hxlauncher.R;
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
// import com.stupidbeauty.hxlauncher.service.DownloadNotificationService;
import com.stupidbeauty.hxlauncher.rpc.DownloadResult;
import com.stupidbeauty.hxlauncher.rpc.DownloadListener;
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
// import com.stupidbeauty.hxlauncher.application.HxLauncherApplication;
// import com.stupidbeauty.hxlauncher.bean.ApplicationListData;
// import com.stupidbeauty.hxlauncher.rpc.CloudRequestorZzaqwb;
import com.stupidbeauty.hxlauncher.rpc.RecognizerResult;

import org.apache.commons.io.FilenameUtils;

import java.io.File;

public class DownloadRequestor
{
  private Notification continiusNotification=null; //!<记录的通知
  private DownloadRequestorInterface launcherActivity=null; //!< 启动活动。
  private int NOTIFICATION = 84951; //!< 通知编号。陈欣
  private boolean autoInstall=false; //!< Whether to auto install.
  private Context baseApplication = null; //!< Context

  private String packageName=null; //!< 包名。
  public Future<File> fileDownloadFuture; //!<The file download future.
  private NotificationManager mNM;

  private static final String TAG="DownloadRequestor"; //!<输出调试信息了时使用的标记

//   private CloudRequestorZzaqwb cloudRequestorZzaqwb=new CloudRequestorZzaqwb(); //!<云端请求发送器

  private long downloadId; //!<当前的下载编号

  private BroadcastReceiver onDownloadComplete = new BroadcastReceiver() 
  {
//   04-01 08:46:40.772  3082  5366 W DownloadManager: [3203] Stop requested with status INSUFFICIENT_SPACE_ERROR: Failed to allocate 78744689 because only 60489728 allocatable
// 04-01 08:46:40.772  3082  5366 D DownloadManager: [3203] Finished with status INSUFFICIENT_SPACE_ERROR

    @Override
    public void onReceive(Context context, Intent intent) 
    {
      //Fetching the download id received with the broadcast
      long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
      //Checking if the received broadcast is for our enqueued download by matching download id

      if (downloadId == id) 
      {
        Toast.makeText(baseApplication, "Download Completed", Toast.LENGTH_SHORT).show();

        Bundle extras=intent.getExtras(); //获取额外数据

        Log.d(TAG, "onReceive, extras: " + extras); //Debug.

        final DownloadManager dManager = (DownloadManager) baseApplication.getSystemService(Context.DOWNLOAD_SERVICE); //Get the download manager.

        Uri uri = dManager.getUriForDownloadedFile(id);

        Log.d(TAG, "onReceive, uri: " + uri); //Debug.

        DownloadManager.Query q = new DownloadManager.Query();

        q.setFilterById(downloadId);

        Cursor c = ((DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE)).query(q);
        if (c.moveToFirst()) 
        {
          int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));

          Log.d(TAG, "onReceive, status: " + status); //debug
                    
          if (status == DownloadManager.STATUS_SUCCESSFUL) // 下载成功。
          {
            int reason = (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));

            Log.d(TAG, "onReceive, reason: " + reason); //debug.

            String sourceUri = (c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI)));

            Log.d(TAG, "onReceive, source url: " + sourceUri); //debug.

            String fullUrl = uri.toString();

            String downloadFilePath = (c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))).replace("file://", "");

            Log.d(TAG, "onReceive, download file path: " + downloadFilePath); //debug.

            requestInstall(downloadFilePath); //要求安装应用
                      
          } // if (status == DownloadManager.STATUS_SUCCESSFUL) // 下载成功。
          else // 下载失败
          {
            notifyDownloadFail(); // 告知下载失败。
          } // else // 下载失败
        }

        c.close(); //关闭游标
      }
    }
  };

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
        Uri downloadedApk = FileProvider.getUriForFile(baseApplication, "com.stupidbeauty.fileprovider", file);

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

  public DownloadRequestor(Context context)
  {
    baseApplication = context; //获取应用程序对象。
    
    baseApplication.registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

//     cloudRequestorZzaqwb.setContext(baseApplication); //设置上下文
      
    DownloadManager.Query q=new DownloadManager.Query(); // 构造查询对象。
      
    final DownloadManager downloadManager = (DownloadManager)baseApplication.getSystemService(Context.DOWNLOAD_SERVICE); // 获取下载管理器对象。
  }

  DownloadListener voiceCommandListerner=new DownloadListener() 
  {
    private static final String TAG="RecognizerListener"; //!<输出调试信息时使用的标记。

    @Override
    public void onResult(DownloadResult recognizerResult, boolean isFinalResult) 
    {
      String wholePath=recognizerResult.getResultString(); // 获取文件路径。

        File apkFile=new File( wholePath);
            
        String apkFilePath= wholePath; // 获取路径。

        //             陈欣，安装
        requestInstall(apkFilePath); // 要求安装。
      }

      @Override
      public void onError(com.stupidbeauty.hxlauncher.rpc.SpeechError speechError) 
      {
        Log.d(TAG, "192, onError "); //Debug.
            
        notifyDownloadFail(); // 通知，下载失败。
      }
    };
    
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
  * 使用离子下载来下载。
  * @param uri 要下载的网址。
  */
  private void downloadByIon(Uri uri)
  {
    String targetUrl=uri.toString(); //获取目标URL。

    String fileName=uri.getLastPathSegment(); // 获取文件名。陈欣
        
    File downloadFolder = baseApplication.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

    final String wholePath =downloadFolder.getPath()+ File.separator  + fileName;

    fileDownloadFuture= Ion.with(baseApplication)
      .load(targetUrl) 
      .setTimeout(15000) //Set the time out to be 15s.
      .progress(new ProgressCallback() 
      {
        @Override
        public void onProgress(long downloaded, long total) 
        {
//           Log.d(TAG, "downloadByIon, progress: " + downloaded + "/" + total + ", " + targetUrl); // 报告进度。
        }
      })
      .setLogging(TAG, Log.DEBUG).write(new File( wholePath))
      .setCallback(new FutureCallback<File>() 
      {
        @Override
        public void onCompleted(Exception e, File file) 
        {
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
                requestInstall(wholePath); // 要求安装。陈欣。
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
    } //private void downloadByIon(Uri uri)


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
