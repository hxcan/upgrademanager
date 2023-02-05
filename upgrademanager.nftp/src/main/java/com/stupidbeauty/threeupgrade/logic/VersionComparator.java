package com.stupidbeauty.threeupgrade.logic;

import com.stupidbeauty.threeupgrade.logic.VersionComparator;
import io.github.g00fy2.versioncompare.Version;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.LauncherApps;
import com.android.volley.RequestQueue;
import com.google.gson.Gson;
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
import java.util.List;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.ShortcutInfo;
import android.graphics.drawable.AnimationDrawable;
import android.util.Pair;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class VersionComparator
{
  private HashMap<String, Integer> packageNamePositionMap=new HashMap<>(); //!< 包名字符串与图标位置之间的映射。
  private HashMap<String, Integer> packageNameItemNamePositionMap=new HashMap<>(); //!< 包名加类名的字符串与图标位置之间的映射。

  private static final String TAG="VersionComparator"; //!< 输出调试信息时使用的标记。
	
  /**
  * Check if is higher vesrion
  */
  public boolean isHigerVersion(String availableVersonName, String currentVersionName)
  {
    boolean result=false;

    if (currentVersionName!=null) // Current version exists
    {
      currentVersionName=currentVersionName.replaceAll("V", "");
      currentVersionName=currentVersionName.replaceAll("v", "");
      currentVersionName=currentVersionName.replaceAll(" ", "");
    } // if (currentVersionName!=null) // Current version exists
    
    if (availableVersonName!=null)
    {
      availableVersonName=availableVersonName.replaceAll("V", "");
      availableVersonName=availableVersonName.replaceAll("v", "");
      availableVersonName=availableVersonName.replaceAll(" ", "");
    } // availableVersonName

    Version availableVersion= new Version(availableVersonName); // 已有版本对象。

    if (availableVersion.isHigherThan(currentVersionName)) // 有新版本
    {
      result=true;
    } //if (availableVersonName > currentVersionName) // 有新版本
    
    return result;
  } //private void solveLauncherIntents()
}
