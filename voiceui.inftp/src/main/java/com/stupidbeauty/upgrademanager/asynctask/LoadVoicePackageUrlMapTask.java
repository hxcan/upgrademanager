package com.stupidbeauty.upgrademanager.asynctask;

import android.util.Log;
import com.stupidbeauty.upgrademanager.asynctask.LoadVoicePackageUrlMapTask;
import java.lang.reflect.Type;
import java.lang.reflect.Field;
import com.upokecenter.cbor.CBORObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import android.annotation.SuppressLint;
// import com.stupidbeauty.hxlauncher.bean.VoicePackageUrlMapData;
import com.stupidbeauty.hxlauncher.bean.WakeLockPackageNameSetData;
import com.stupidbeauty.hxlauncher.datastore.RuntimeInformationStore;
import java.util.Locale;
import com.google.gson.Gson;
import com.stupidbeauty.upgrademanager.Constants;
import com.stupidbeauty.hxlauncher.bean.ApplicationNamePair;
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
import com.stupidbeauty.hxlauncher.bean.ApplicationNamePair;
import java.util.List;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.AnimationDrawable;
import android.util.Pair;
// import com.stupidbeauty.hxlauncher.bean.VoicePackageMapJsonItem;
// import com.stupidbeauty.hxlauncher.bean.VoicePackageUrlMapData;
import com.stupidbeauty.hxlauncher.bean.WakeLockPackageNameSetData;
import com.stupidbeauty.hxlauncher.datastore.RuntimeInformationStore;

public class LoadVoicePackageUrlMapTask extends AsyncTask<Object, Void, Object>
{
  private static final String TAG="LoadVoicePackageUrlMapTask"; //!< 输出调试信息时使用的标记。
//   private VoicePackageUrlMapData voicePackageUrlMapData; //!<语音识别结果与软件包下载地址之间的映射。
  private String filePath; //!< file path.
  private HashMap<String, String> voicePackageUrlMap; //!<语音识别结果与包名之间的映射关系。

  public HashMap<String, String> getPackageNameUrlMap() 
  {
    return packageNameUrlMap;
  }

  private HashMap<String, String> packageNameUrlMap; //!<包名与下载地址之间的映射关系。
  private HashMap<String, String> packageNameInformationUrlMap; //!<包名与信息页面地址之间的映射关系。
  private HashMap<String, String> packageNameVersionNameMap; //!< 包名与可用版本号之间的映射关系。
  private  HashMap<String, String > packageNameApplicationNameMap; //!<包名与应用程序名的映射

  private LoadVoicePackageUrlMapInterface launcherActivity=null; //!< 启动活动。

  private void  loadVoicePackageUrlMapCbor() // 载入语音识别结果与下载网址之间的映射。使用CBOR。陈欣。
  {
//     String qrcFileName="voicePackageUrlMap.cbor.cx"; //文件名。
//     String fullQrcFileName=":/VoicePackageUrlMapInternationalization/"+qrcFileName; //构造完整的qrc文件名。

//     VFile qrcHtmlFile=new VFile(launcherActivity.getContext(), fullQrcFileName); //qrc网页文件。

    File photoFile=new File(filePath); // The data file.

    try
    {
    byte[] photoBytes= FileUtils.readFileToByteArray(photoFile); //将照片文件内容全部读取。

    
//     byte[] photoBytes= qrcHtmlFile.getFileContent(); //将照片文件内容全部读取。
        
    CBORObject videoStreamMessage= CBORObject.DecodeFromBytes(photoBytes); //解析消息。
        
    //         陈欣

    Collection<CBORObject> subFilesList=videoStreamMessage.get("voicePackageMapJsonItemList").getValues();
        
    voicePackageUrlMap=new HashMap<>(); //创建映射。
    packageNameUrlMap=new HashMap<>(); //创建映射
    packageNameInformationUrlMap=new HashMap<>(); // 创建映射。
    packageNameVersionNameMap=new HashMap<>(); // 创建映射。陈欣
    packageNameApplicationNameMap=new HashMap<>(); //创建映射

    for (CBORObject currentSubFile: subFilesList) //一个个子文件地比较其文件名。
    {
      String voiceCommand=currentSubFile.get("voiceCommand").AsString();
      String packageUrl=currentSubFile.get("packageUrl").AsString();
      String packageName=currentSubFile.get("packageName").AsString();
      String informationUrl=currentSubFile.get("informationUrl").AsString(); // 获取信息页面地址。
            
      CBORObject versionNameObject=currentSubFile.get("versionName");
            
      if (versionNameObject!=null)
      {
        String versionName=versionNameObject.AsString();

        packageNameVersionNameMap.put(packageName, versionName); // 加入映射。
      } //versionNameObject
                
      voicePackageUrlMap.put(voiceCommand, packageUrl); //加入映射。
      packageNameUrlMap.put(packageName, packageUrl); //加入映射。
      packageNameApplicationNameMap.put( packageName, voiceCommand); //加入映射，包名与应用程序名的映射
      packageNameInformationUrlMap.put(packageName, informationUrl); // 加入映射，包名与信息页面地址的映射。

//                 if (currentSubFile.get("name").AsString().equals(subFileName)) //正是这个文件。
//                 {
//                     foundSubFile=true; //找到了子文件。
//                     subFile=currentSubFile; //记录。
// 
//                     break; //跳出。
//                 } //if (currentSubFile.getName().equals(subFileName)) //正是这个文件。
            } //for (FileMessageContainer.FileMessage currentSubFile:videoStreamMessage.getSubFilesList()) //一个个子文件地比较其

            Log.d(TAG, "loadVoicePackageUrlMapCbor, packageNameApplicationNameMap list size: "+ packageNameApplicationNameMap.size()); //Debug.
    }
    catch (IOException e)
    {
      e.printStackTrace();
    } //catch (IOException e)
  } //private void  loadVoicePackageUrlMapCbor()
	
  @Override
  protected Object doInBackground(Object... params)
  {
    Boolean result=false; //结果，是否成功。

    launcherActivity=(LoadVoicePackageUrlMapInterface)(params[0]); // 获取映射对象
    filePath=(String)(params[1]); // file path.
            
    loadVoicePackageUrlMapCbor(); // 载入语音识别结果与下载网址之间的映射。使用CBOR。陈欣。
            
    boolean addPhotoFile=false; //Whether to add photo file

    return voicePackageUrlMap;
  } //protected Object doInBackground(Object... params)

  /**
  * 报告结果。
  * @param result 结果。是否成功。
  */
  @Override
  protected void onPostExecute(Object result)
  {
    launcherActivity.setPackageNameUrlMap(packageNameUrlMap);
    launcherActivity.setPackageNameVersionNameMap(packageNameVersionNameMap);
    launcherActivity.setPackageNameInformationUrlMap(packageNameInformationUrlMap); // 设置包名与信息页面地址之间的映射。
  } //protected void onPostExecute(Boolean result)
}
