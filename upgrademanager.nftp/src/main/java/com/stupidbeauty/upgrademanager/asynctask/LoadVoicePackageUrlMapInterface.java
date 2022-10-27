package com.stupidbeauty.upgrademanager.asynctask;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import android.util.Log;
import android.media.MediaDataSource;

public interface LoadVoicePackageUrlMapInterface
{
  Context getContext(); //!< Get context.
  void setPackageNameUrlMap (HashMap<String, String> packageNameUrlMap) ; //!< Set the pckage name url map.
  void setPackageNameInstallerTypeMap(HashMap<String, String> packageNameInstallerTypeMap); //!< set the package name installer type map.
  void setPackageNameInformationUrlMap(HashMap<String, String> packageNameInformationUrlMap); //!< set the map of package name and informatino url.
  void setPackageNameVersionNameMap (HashMap<String, String> packageNameVersionNameMap); //!< 包名与可用版本号之间的映射关系。
  void setPackageNameExtraPackageNamesMap(HashMap<String, List<String> > packageNameExtraPackageNamesMap); //!< Set the map of package name to extra package names list.
} // public interface LoadVoicePackageUrlMapInterface
