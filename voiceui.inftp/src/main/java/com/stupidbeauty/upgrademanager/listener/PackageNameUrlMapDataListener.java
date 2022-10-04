package com.stupidbeauty.upgrademanager.listener;

import java.util.HashMap;
import java.util.HashMap;
import android.content.Context;
import com.stupidbeauty.victoriafresh.VFile;
import com.stupidbeauty.grebe.DownloadRequestor;
import android.view.View;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;

public interface PackageNameUrlMapDataListener 
{
  public void setPackageNameUrlMap (HashMap<String, String> packageNameUrlMap);
  public void setPackageNameInformationUrlMap(HashMap<String, String> packageNameInformationUrlMap);
  public void setPackageNameVersionNameMap (HashMap<String, String> packageNameVersionNameMap);
}
