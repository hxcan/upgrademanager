package com.stupidbeauty.upgrademanager.loader;

import com.stupidbeauty.codeposition.CodePosition;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.BufferedReader;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import com.stupidbeauty.upgrademanager.loader.VoicePackageUrlMapLoader;
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
import com.stupidbeauty.upgrademanager.asynctask.LoadVoicePackageUrlMapInterface;
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

public class VoicePackageUrlMapLoader
{
  private static final String TAG="VoicePackageUrlMapLoader"; //!< 输出调试信息时使用的标记。
//   private String filePath; //!< file path.
  private String exzFilePath; //!< exz data file path.
  private HashMap<String, String> voicePackageUrlMap; //!<语音识别结果与包名之间的映射关系。
  private VoicePackageUrlMapLoader voicePackageUrlMapLoader=null; //!< voice package url map loader.

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

//   private LoadVoicePackageUrlMapInterface launcherActivity=null; //!< 启动活动。
  
  /**
  * 载入语音识别结果与下载网址之间的映射。使用CBOR。陈欣。
  */
  public void loadVoicePackageUrlMapCbor(byte[] photoBytes)
  {
//     File photoFile=new File(filePath); // The data file.

    voicePackageUrlMap=new HashMap<>(); //创建映射。
    packageNameUrlMap=new HashMap<>(); //创建映射
    packageNameInstallerTypeMap=new HashMap<>(); // Create map of installer type.
    packageNameInformationUrlMap=new HashMap<>(); // 创建映射。
    packageNameExtraPackageNamesMap=new HashMap<>(); // Create map.
    packageNameVersionNameMap=new HashMap<>(); // 创建映射。陈欣
    packageNameApplicationNameMap=new HashMap<>(); //创建映射
    packageNameIconUrlMap=new HashMap<>(); //创建映射. package name to icon url.

    try
    {
//       byte[] photoBytes= FileUtils.readFileToByteArray(photoFile); // 将 data 文件内容全部读取。

      CBORObject videoStreamMessage= CBORObject.DecodeFromBytes(photoBytes); //解析消息。

      Collection<CBORObject> subFilesList=videoStreamMessage.get("voicePackageMapJsonItemList").getValues();

      for (CBORObject currentSubFile: subFilesList) //一个个子文件地比较其文件名。
      {
        String voiceCommand=currentSubFile.get("voiceCommand").AsString();
        String packageUrl=currentSubFile.get("packageUrl").AsString();
        String installerType=currentSubFile.get("installerType").AsString(); // Get installer type. xapk or apk
        String packageName=currentSubFile.get("packageName").AsString();
        String informationUrl=currentSubFile.get("informationUrl").AsString(); // 获取信息页面地址。
        String iconUrl=currentSubFile.get("iconUrl").AsString(); // Get package icon url.

        ArrayList<String> extraPackageNames = new ArrayList<>();

        Collection<CBORObject> extraPackageNamesList=currentSubFile.get("extraPackageNames").getValues();
        
        if (extraPackageNamesList.size() > 0) // There is extra package names list
        {
          extraPackageNames.add(packageName); // Add the package name itself.

          for(CBORObject extraPackgaeName: extraPackageNamesList)
          {
            String extraPackageNameString = extraPackgaeName.AsString();
            
            extraPackageNames.add(extraPackageNameString);
          } // for(CBORObject extraPackgaeName: extraPackageNamesList)

          for(String currentPackgaeName: extraPackageNames) // Add to map one by one
          {
            packageNameExtraPackageNamesMap.put(currentPackgaeName, extraPackageNames); // Add map, package name to extram package names list.
          } // for(String currentPackgaeName: extraPackageNames) // Add to map one by one
        } // if (extraPackageNamesList.size() > 0) // There is extra package names list
              
        CBORObject versionNameObject=currentSubFile.get("versionName");

        if (versionNameObject!=null) // Version name object exists
        {
        } //versionNameObject
        else // Object not exist
        {
          versionNameObject=currentSubFile.get(FieldCode.VersionName); // Get by int key.
        } // else // Object not exist
                  
        if (versionNameObject!=null) // Version name object exists
        {
          String versionName=versionNameObject.AsString();

          packageNameVersionNameMap.put(packageName, versionName); // 加入映射。
        } //versionNameObject
        else // Object not exist
        {
          versionNameObject=currentSubFile.get(FieldCode.VersionName); // Get by int key.
        } // else // Object not exist
                  
        voicePackageUrlMap.put(voiceCommand, packageUrl); //加入映射。
        packageNameUrlMap.put(packageName, packageUrl); //加入映射。
        packageNameInstallerTypeMap.put(packageName, installerType); // 加入映射。 installer type.
        packageNameApplicationNameMap.put( packageName, voiceCommand); //加入映射，包名与应用程序名的映射
        packageNameInformationUrlMap.put(packageName, informationUrl); // 加入映射，包名与信息页面地址的映射。
        packageNameIconUrlMap.put(packageName, iconUrl); // Add map item.
        
        for(String currentPackgaeName: extraPackageNames) // Add to map one by one
        {
//           packageNameExtraPackageNamesMap.put(currentPackgaeName, extraPackageNames); // Add map, package name to extram package names list.
          packageNameUrlMap.put(currentPackgaeName, packageUrl); // 加入映射。
        } // for(String currentPackgaeName: extraPackageNames) // Add to map one by one
      } //for (FileMessageContainer.FileMessage currentSubFile:videoStreamMessage.getSubFilesList()) //一个个子文件地比较其
    } // try
    catch (CBORException e)
    {
      Log.d(TAG, "loadVoicePackageUrlMapCbor, 192, exz data file partly downloaded, ignoring: "+ exzFilePath); //Debug.
    } // catch (CBORException e)
  } //private void  loadVoicePackageUrlMapCbor()
	
  /**
  * Transfer data.
  */
  public void transferData(LoadVoicePackageUrlMapInterface launcherActivity)
  {
    if (packageNameVersionNameMap!=null) // Actually loaded data
    {
      launcherActivity.setVoicePackageUrlMap(voicePackageUrlMap);
      launcherActivity.setPackageNameUrlMap(packageNameUrlMap);
      launcherActivity.setPackageNameInstallerTypeMap(packageNameInstallerTypeMap); // Set package name installer type map.
      launcherActivity.setPackageNameVersionNameMap(packageNameVersionNameMap);
      launcherActivity.setPackageNameInformationUrlMap(packageNameInformationUrlMap); // 设置包名与信息页面地址之间的映射。
      launcherActivity.setPackageNameExtraPackageNamesMap(packageNameExtraPackageNamesMap); // Set the map of package name to extra package names list.
      launcherActivity.setPackageNameApplicationNameMap(packageNameApplicationNameMap); // Set map of package name to application name.
      launcherActivity.setPackageNameIconUrlMap(packageNameIconUrlMap); // Set the map of package name and icon url.
    } // if (packageNameVersionNameMap!=null) // Actually loaded data
    else // Not loaded data
    {
      Log.d(TAG, CodePosition.newInstance().toString()+ ", no data loaded, skip"); // Debug.
    } // else // Not loaded data
  } // private void transferData()
}
