package com.szxb.mlog;

public interface FormatStrategy {

  void log(int priority, String tag, String message);
}
