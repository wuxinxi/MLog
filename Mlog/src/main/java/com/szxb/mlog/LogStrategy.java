package com.szxb.mlog;

public interface LogStrategy {

  void log(int priority, String tag, String message);
}
