package com.stupidbeauty.upgrademanager.listener;

import java.util.HashMap;
import java.util.HashMap;
import android.content.Context;
import com.stupidbeauty.victoriafresh.VFile;
// import com.stupidbeauty.grebe.DownloadRequestor;
import android.view.View;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import android.util.Log;
import android.media.MediaDataSource;

public interface PackageNameUrlMapDataListener 
{
  public void setVoicePackageUrlMap (HashMap<String, String> voicePackageUrlMap); //!< Set voice package url map.
  public void setPackageNameUrlMap (HashMap<String, String> packageNameUrlMap);
  public void setPackageNameInstallerTypeMap(HashMap<String, String> packageNameInstallerTypeMap); //!< set the package name installer type map.
  public void setPackageNameExtraPackageNamesMap(HashMap<String, List<String> > packageNameExtraPackageNamesMap); //!< Set the map of package name to extra package names list.
  public void setPackageNameInformationUrlMap(HashMap<String, String> packageNameInformationUrlMap);
  public void setPackageNameVersionNameMap (HashMap<String, String> packageNameVersionNameMap);
}
