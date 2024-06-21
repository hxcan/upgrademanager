package com.stupidbeauty.upgrademanager.asynctask;

import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;
import com.koushikdutta.async.future.FutureCallback;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.stupidbeauty.upgrademanager.loader.VoicePackageUrlMapLoader;
import com.stupidbeauty.upgrademanager.UmDownloadRequestor;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import com.upokecenter.cbor.CBORObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import android.annotation.SuppressLint;
import com.upokecenter.cbor.CBORException;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import com.google.gson.Gson;
import com.stupidbeauty.upgrademanager.Constants;
import com.stupidbeauty.victoriafresh.VFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import static android.content.Intent.ACTION_PACKAGE_CHANGED;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import android.view.View;
import android.os.AsyncTask;
import java.util.HashMap;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import org.apache.commons.io.FilenameUtils;
import java.io.File;
import java.util.List;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.AnimationDrawable;
import android.util.Pair;
import com.stupidbeauty.upgrademanager.bean.FieldCode;
import com.stupidbeauty.extremezip.EXtremeZip;
// import com.stupidbeauty.hxlauncher.datastore.RuntimeInformationStore;

public class StartIonDownloadAsyncTask extends AsyncTask<Object, Void, Object>
{
  private String downloadedFilePath; //!< Remember downloaded file path.
  public Future<File> fileDownloadFuture; //!<The file download future.
  private static final String TAG="StartIonDownloadAsyncTask"; //!< 输出调试信息时使用的标记。
  private String filePath; //!< file path.
  private String exzFilePath; //!< exz data file path.
  private HashMap<String, String> voicePackageUrlMap; //!<语音识别结果与包名之间的映射关系。
  private VoicePackageUrlMapLoader voicePackageUrlMapLoader=null; //!< voice package url map loader.
  private UmDownloadRequestor downloadRequestor; //!< The download reqeustor.

  public HashMap<String, String> getPackageNameUrlMap() 
  {
    return packageNameUrlMap;
  }

  private HashMap<String, String> packageNameUrlMap; //!<包名与下载地址之间的映射关系。
  private HashMap<String, String> packageNameInstallerTypeMap; //!< Map of package name to installer type.
  private HashMap<String, String> packageNameInformationUrlMap; //!<包名与信息页面地址之间的映射关系。
  private HashMap<String, List<String> > packageNameExtraPackageNamesMap; //!< The map of package name to extra package names.
  private HashMap<String, String> packageNameVersionNameMap; //!< 包名与可用版本号之间的映射关系。
  private  HashMap<String, String > packageNameApplicationNameMap; //!<包名与应用程序名的映射
  private HashMap<String, String> packageNameIconUrlMap; //!< The map of package name and icon url.

  private LoadVoicePackageUrlMapInterface launcherActivity=null; //!< 启动活动。
  
  /**
  * uncompress the compressed data file.
  */
  private String exuzDataFile(String filePath) 
  {
    String result;
    
    EXtremeZip extremezip=new EXtremeZip(); // Create extremezip object.

    Context baseApplication=launcherActivity.getContext(); // Get the context.

    try
    {
      extremezip.exuz(filePath, baseApplication); // Compress.
    }
    catch(ClassCastException e)
    {
    }
    
    File downloadFolder = baseApplication.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

    final String wholePath =downloadFolder.getPath()+ File.separator  + "voicePackageUrlMap.cbor.cx";
    
    Log.d(TAG, "exuzDataFile, whole path: "+ wholePath); // Debug.

    result=wholePath;
    
    return result;
  } // private String exuzDataFile(String filePath)
  
  /**
  * 载入语音识别结果与下载网址之间的映射。使用CBOR。陈欣。 Use the loader
  */
  private void loadVoicePackageUrlMapCborLoader() 
  {
    voicePackageUrlMapLoader=new VoicePackageUrlMapLoader(); // Create the loader.
    File photoFile=new File(filePath); // The data file.

    try
    {
      byte[] photoBytes= FileUtils.readFileToByteArray(photoFile); // 将 data 文件内容全部读取。
      
      voicePackageUrlMapLoader.loadVoicePackageUrlMapCbor(photoBytes); // Load content.
    } // try
    catch (IOException e)
    {
      Log.d(TAG, "loadVoicePackageUrlMapCbor, 183, exz data file partly downloaded, ignoring: "+ exzFilePath); //Debug.
    } //catch (IOException e)
    catch (CBORException e)
    {
      // Log.d(TAG, "loadVoicePackageUrlMapCbor, 192, exz data file partly downloaded, ignoring: "+ exzFilePath); //Debug.
      Log.d(TAG, CodePosition.newInstance().toString()+ ", exz data file partly downloaded, ignoring: "+ exzFilePath); //Debug.
    } // catch (CBORException e)
  } // private void loadVoicePackageUrlMapCborLoader()

  @Override
  protected Object doInBackground(Object... params)
  {
    Boolean result=false; //结果，是否成功。

    downloadRequestor=(UmDownloadRequestor)(params[0]); // Get the download reqeustor.
    String fileName=(String)(params[1]); // file path. 
    Context baseApplication = (Context)(params[2]); // Context.
    String targetUrl = (String)(params[3]); // Taget url.
    
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
          downloadRequestor.handleDownloadProgress(targetUrl, total, downloaded); // Handle download progress.
        }
      })
      .write(new File( wholePath));
      
      
      fileDownloadFuture.setCallback(new FutureCallback<File>() 
      {
        @Override
        public void onCompleted(Exception e, File file) 
        {
          downloadRequestor.handleDownloadCompleted(wholePath, e); // Handle download completed.
        } // public void onCompleted(Exception e, File file) 
      });

    return voicePackageUrlMap;
  } //protected Object doInBackground(Object... params)

  /**
  * 报告结果。
  * @param result 结果。是否成功。
  */
  @Override
  protected void onPostExecute(Object result)
  {
    // voicePackageUrlMapLoader.transferData(launcherActivity);
    
//     transferData(); // Transfer data.
  
    downloadRequestor.setFileDownloadFuture(fileDownloadFuture); // SEt the file download future.
    downloadRequestor.setDownloadedFilePath(  downloadedFilePath); // Set the downloaded file path.
  } //protected void onPostExecute(Boolean result)
}
