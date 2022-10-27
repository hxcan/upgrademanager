package com.stupidbeauty.grebe;

public interface DownloadRequestorInterface
{
  void  reportDownloadFailed(String packageName) ; //!< * 报告，下载失败。
  void reportDownloadFinished(String packageName, String filePath); //!< Report, download finished.
}
