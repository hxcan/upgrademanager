package com.stupidbeauty.grebe;

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
import com.stupidbeauty.hxlauncher.rpc.RecognizerResult;
import org.apache.commons.io.FilenameUtils;
import java.io.File;

public class DownloadRequestor
{
  private String actionName; //!< Construct action name.
  private Notification continiusNotification=null; //!<???????????????
  private DownloadRequestorInterface launcherActivity=null; //!< ???????????????
  private int NOTIFICATION = 84951; //!< ?????????????????????
  private boolean autoInstall=false; //!< Whether to auto install.
  private Context baseApplication = null; //!< Context
  private String downloadedFilePath; //!< Remember downloaded file path.
  private String packageName=null; //!< ?????????
  public Future<File> fileDownloadFuture; //!<The file download future.
  private NotificationManager mNM;

  private static final String TAG="DownloadRequestor"; //!<???????????????????????????????????????

  private long downloadId; //!<?????????????????????

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

        Bundle extras=intent.getExtras(); //??????????????????

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
                    
          if (status == DownloadManager.STATUS_SUCCESSFUL) // ???????????????
          {
            int reason = (c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));

            Log.d(TAG, "onReceive, reason: " + reason); //debug.

            String sourceUri = (c.getString(c.getColumnIndex(DownloadManager.COLUMN_URI)));

            Log.d(TAG, "onReceive, source url: " + sourceUri); //debug.

            String fullUrl = uri.toString();

            String downloadFilePath = (c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))).replace("file://", "");

            Log.d(TAG, "onReceive, download file path: " + downloadFilePath); //debug.

            requestInstall(downloadFilePath); //??????????????????
                      
          } // if (status == DownloadManager.STATUS_SUCCESSFUL) // ???????????????
          else // ????????????
          {
            notifyDownloadFail(); // ?????????????????????
          } // else // ????????????
        }

        c.close(); //????????????
      }
    }
  };

  /**
  * ??????????????????
  * @param downloadFilePath ?????????????????????
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
        Uri downloadedApk = FileProvider.getUriForFile(baseApplication, "com.stupidbeauty.upgrademanager.fileprovider", file);

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
    baseApplication = context; //???????????????????????????
    
    actionName="com.stupidbeauty.upgrademanager."+ baseApplication.getPackageName(); // Construct action name.
    
    baseApplication.registerReceiver(onDownloadComplete,new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    mNM = (NotificationManager)baseApplication.getSystemService(Context.NOTIFICATION_SERVICE); // Get notification manager.

    DownloadManager.Query q=new DownloadManager.Query(); // ?????????????????????
      
    final DownloadManager downloadManager = (DownloadManager)baseApplication.getSystemService(Context.DOWNLOAD_SERVICE); // ??????????????????????????????
  }

  DownloadListener voiceCommandListerner=new DownloadListener() 
  {
    private static final String TAG="RecognizerListener"; //!<???????????????????????????????????????

    @Override
    public void onResult(DownloadResult recognizerResult, boolean isFinalResult) 
    {
      String wholePath=recognizerResult.getResultString(); // ?????????????????????

        File apkFile=new File( wholePath);
            
        String apkFilePath= wholePath; // ???????????????

        //             ???????????????
        requestInstall(apkFilePath); // ???????????????
      }

      @Override
      public void onError(com.stupidbeauty.hxlauncher.rpc.SpeechError speechError) 
      {
        Log.d(TAG, "192, onError "); //Debug.
            
        notifyDownloadFail(); // ????????????????????????
      }
    };
    
    /**
    * Report download finished.
    */
    private void notifyDownloadFinish(String wholePath)
    {
      if (launcherActivity!=null)
      {
        launcherActivity.reportDownloadFinished(packageName, wholePath); // ??????????????? finished.
      }
    } // private void notifyDownloadFinish()
    
  /**
  * ????????????????????????
  */
  private void  notifyDownloadFail() 
  {
    String contentText="Failed to download";

    if (launcherActivity!=null)
    {
      launcherActivity.reportDownloadFailed(packageName); // ????????????????????????
    }
  } //private void  notifyDownloadFail()
    
  /**
  * ???????????????????????????????????????
  */
  public void requestDownloadUrl(Uri uri, String refererUrl, String applicationName, String packageName)
  {
    boolean shouldDownload=false; // ?????????????????????
        
    shouldDownload=true; // ???????????????

    Log.d(TAG, "requestDownloadUrl, download file path: " + uri); //debug.

    String fullUrl = uri.toString();

    Log.d(TAG, "requestDownloadUrl, url scheme: " + uri.getScheme()); //debug.
        
    downloadByIon(uri); // ??????????????????????????????
  }
  
  /**
  * ???????????????APK?????????
  */
  private boolean checkIsApkFile(String apkFilePath) 
  {
    boolean result=false;

    PackageManager packageManager = baseApplication.getPackageManager();
    
    PackageInfo packageInfo=packageManager.getPackageArchiveInfo(apkFilePath, 0);
    
    if (packageInfo!=null) // ????????????????????????
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
  * ??????????????????????????????
  * @param uri ?????????????????????
  */
  private void downloadByIon(Uri uri)
  {
    String targetUrl=uri.toString(); //????????????URL???

    String fileName=uri.getLastPathSegment(); // ????????????????????????
        
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
//           Log.d(TAG, "downloadByIon, progress: " + downloaded + "/" + total + ", " + targetUrl); // ???????????????
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
                            
            //                             ??????
            notifyDownloadFail(); // ?????????????????????
          } //if (e!=null) //Some error occured.
          else // ????????????
          {
//             Toast.makeText(baseApplication, "Download Completed" + wholePath, Toast.LENGTH_SHORT).show();

            if (autoInstall) // Auto install
            {
              if (checkIsApkFile(wholePath)) // ?????????????????????
              {
                registerReceiverInstall(); // Register receiver of install.
              
                showNotificationInstall(); // Show notification to request install.
//                 requestInstall(wholePath); // ????????????????????????
              } // if (checkIsApkFile(wholePath)) // ?????????????????????
              else // ??????????????????
              {
                notifyDownloadFail(); // ?????????????????????
              } // else // ??????????????????
            }
            else // Not auto install
            {
              notifyDownloadFinish(wholePath); // Report download finished.
            }
          } //else // ????????????
        }
      });
    } //private void downloadByIon(Uri uri)
    
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
      long startTimestamp=System.currentTimeMillis(); // ????????????????????????
      Log.w(TAG, "registerBroadcastReceiver, 1876, enter registerBroadcastReceiver, timestamp: " + System.currentTimeMillis()); //Debug.
      Log.d(TAG, "registerBroadcastReceiver."); //Debug.

      //?????????????????????????????????

      //????????????????????????????????????????????????
      IntentFilter lanimeInputtingIntentFilter=new IntentFilter(); //????????????????????????
      lanimeInputtingIntentFilter.addAction(actionName); // Execute upgrade

      baseApplication.registerReceiver(mBroadcastReceiver, lanimeInputtingIntentFilter); //??????????????????????????????
    } // private void registerReceiverInstall()

    /**
     * ??????????????????
     */
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver()
    {
      private final String TAG="BroadcastReceiver"; //!<???????????????????????????????????????

      @SuppressWarnings("ConstantConditions")
      @Override
      /**
      * ??????????????????
      */
      public void onReceive(Context context, Intent intent)
      {
        String action = intent.getAction(); //???????????????????????????????????????

        Log.d(TAG,"1587, onReceive,got broadcast:"+action + ", equals package_added?: " + (Intent.ACTION_PACKAGE_ADDED.equals(action))); //Debug.

        if (actionName.equals(action)) // Upgrade.
        {
          requestInstall(downloadedFilePath); // Request install.
        
          unregisterReceiverInstall(); // Un register install receiver.
        } //if (Constants.NativeMessage.APPLICATION_LAUNCHED.equals(action)) //????????????????????????
        else if (Intent.ACTION_WALLPAPER_CHANGED.equals(action)) //???????????????
        {
        } //else if (Intent.ACTION_PACKAGE_ADDED.equals(action)) //??????????????????
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

      CharSequence downloadingText=baseApplication.getText(R.string.foundNewVersion); // ??????????????????????????????????????????

      NotificationChannel chan = new NotificationChannel( "#include", "My Foreground Service", NotificationManager.IMPORTANCE_LOW);
            
      mNM.createNotificationChannel(chan);
    
      ApplicationInfo applicationInfo = baseApplication.getApplicationInfo(); // Get application info object.
    
      int applicationIcon=applicationInfo.icon; //??????????????????????????????
      int applicationLabel=applicationInfo.labelRes; //??????????????????????????????

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

    continiusNotification=notification; //????????????

    // Send the notification.
    mNM.notify(NOTIFICATION, notification);

    } // private void showNotificationInstall()

    /**
     * ?????????????????????????????????
     * @param uri ??????????????????
     */
    private void downloadByCloudRequestor(Uri uri, String packageName)
    {
//       cloudRequestorZzaqwb.startDownload(uri, voiceCommandListerner, packageName); // ??????????????????????????????
    } //private void downloadByCloudRequestor(Uri uri)

    private void downloadByDownloadManager(Uri uri, String refererUrl, String applicationName, Context baseApplication) 
    {
      final DownloadManager dManager = (DownloadManager) baseApplication.getSystemService(Context.DOWNLOAD_SERVICE); //Get the download manager.
      DownloadManager.Request request = new DownloadManager.Request(uri); //Create the request.

      String fileName = FilenameUtils.getName(uri.getLastPathSegment()); //?????????????????????

      // ??????????????????????????????
      request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName); //?????????????????????????????????
      
//       04-01 14:53:17.995  4213  4213 E AndroidRuntime: java.lang.IllegalStateException: Not one of standard directories: download

      request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); //When completed , notify.

      // ???????????????????????????
      request.setVisibleInDownloadsUi(true); //Visible.

      request.addRequestHeader("Referer", refererUrl); //?????????????????????

      request.setTitle(applicationName); //????????????

      //?????????Get the download id.
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
