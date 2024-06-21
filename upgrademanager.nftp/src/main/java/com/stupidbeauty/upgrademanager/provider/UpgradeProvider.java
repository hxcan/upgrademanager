package com.stupidbeauty.upgrademanager.provider;

import android.content.ClipData;
import androidx.core.content.FileProvider;
import android.database.ContentObserver;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import android.os.Build;
import androidx.core.content.FileProvider;
import android.database.ContentObserver;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import org.apache.commons.io.FileUtils;
import com.koushikdutta.async.future.FutureCallback;
import java.io.File;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.DownloadListener;
import android.widget.Toast;
import org.apache.commons.io.FilenameUtils;

public class UpgradeProvider extends FileProvider
{
	private long donwloadManagerDownloadId; //!<The download id of download manager.

	private static final String TAG = "SbrDownloadListener"; //!<输出调试信息时使用的标记。
}
