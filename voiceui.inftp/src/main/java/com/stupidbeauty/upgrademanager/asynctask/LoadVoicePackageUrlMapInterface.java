package com.stupidbeauty.upgrademanager.asynctask;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import java.util.HashMap;

public interface LoadVoicePackageUrlMapInterface
{
  Context getContext(); //!< Get context.
  void setPackageNameUrlMap (HashMap<String, String> packageNameUrlMap) ; //!< Set the pckage name url map.
  void setPackageNameInformationUrlMap(HashMap<String, String> packageNameInformationUrlMap); //!< set the map of package name and informatino url.
  void setPackageNameVersionNameMap (HashMap<String, String> packageNameVersionNameMap); //!< 包名与可用版本号之间的映射关系。
} // public interface LoadVoicePackageUrlMapInterface
