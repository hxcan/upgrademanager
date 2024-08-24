package com.stupidbeauty.upgrademanager.parser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeStampParser 
{
  public static long main(String timestampString) 
  {
    long lastModifiedTimestamp = 0;
    // 给定的时间戳字符串
    // String timestampString = "Sat Aug 24 13:12:11 2024";
    
    // 定义日期格式
    // SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

    try 
    {
      // 解析时间戳字符串
      Date parsedDate = dateFormat.parse(timestampString);
      
      // 获取毫秒数
      lastModifiedTimestamp = parsedDate.getTime();
      
      // 输出解析后的时间戳
      System.out.println("Parsed Timestamp (milliseconds): " + lastModifiedTimestamp);
    }
    catch (ParseException e) 
    {
      // 如果时间戳无法解析，则处理异常
      e.printStackTrace();
    }
        
    return lastModifiedTimestamp;
  }
}
