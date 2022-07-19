package com.stupidbeauty.upgrademanager.asynctask;

import android.content.Context;
// import android.content.Intent;
// import android.content.IntentFilter;
import android.database.Cursor;
// import android.net.Uri;
import android.view.View;
// import android.os.AsyncTask;
import java.util.HashMap;

public interface LoadVoicePackageUrlMapInterface
{
  Context getContext(); //!< Get context.
  void setPackageNameUrlMap (HashMap<String, String> packageNameUrlMap) ; //!< Set the pckage name url map.

  void setPackageNameVersionNameMap (HashMap<String, String> packageNameVersionNameMap); //!< 包名与可用版本号之间的映射关系。
} // public interface LoadVoicePackageUrlMapInterface
