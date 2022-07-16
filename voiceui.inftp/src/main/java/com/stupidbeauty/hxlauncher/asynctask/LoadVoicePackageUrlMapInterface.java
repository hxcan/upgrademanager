package com.stupidbeauty.hxlauncher.asynctask;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import java.util.HashMap;
import android.view.View;
import android.os.AsyncTask;
import java.util.HashMap;

public interface LoadVoicePackageUrlMapInterface
{
  Context getContext(); //!< Get context.
  
  void setPackageNameUrlMap (HashMap<String, String> packageNameUrlMap) ;

  void setPackageNameVersionNameMap (HashMap<String, String> packageNameVersionNameMap); //!< 包名与可用版本号之间的映射关系。
}
