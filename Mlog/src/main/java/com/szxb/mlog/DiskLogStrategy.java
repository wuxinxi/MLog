package com.szxb.mlog;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 后台线程在Android上文件日志操作。实现类可以自由直接执行I / O操作
 * fileName:2017-02-02_0.csv
 */
public class DiskLogStrategy implements LogStrategy {

    private final Handler handler;

    public DiskLogStrategy(Handler handler) {
        this.handler = handler;
    }

    @Override
    public void log(int level, String tag, String message) {
        // do nothing on the calling thread, simply pass the tag/msg to the background thread
        handler.sendMessage(handler.obtainMessage(level, message));
    }

    static class WriteHandler extends Handler {

        private final String folder;
        private final int maxFileSize;
        private final String fileName;
        private final String fileType;


        WriteHandler(Looper looper, String folder, int maxFileSize, String fileName, String fileType) {
            super(looper);
            this.folder = folder;
            this.maxFileSize = maxFileSize;
            this.fileName = fileName;
            this.fileType = fileType;
        }

        @SuppressWarnings("checkstyle:emptyblock")
        @Override
        public void handleMessage(Message msg) {
            String content = (String) msg.obj;

            FileWriter fileWriter = null;
            File logFile = getLogFile(folder, fileName + "-" + Utils.getCurrentTime());

            try {
                fileWriter = new FileWriter(logFile, true);

                writeLog(fileWriter, content);

                fileWriter.flush();
                fileWriter.close();
            } catch (IOException e) {
                if (fileWriter != null) {
                    try {
                        fileWriter.flush();
                        fileWriter.close();
                    } catch (IOException e1) { /* fail silently */ }
                }
            }
        }

        /**
         * This is always called on a single background thread.
         * Implementing classes must ONLY write to the fileWriter and nothing more.
         * The abstract class takes care of everything else including close the stream and catching IOException
         *
         * @param fileWriter an instance of FileWriter already initialised to the correct file
         */
        private void writeLog(FileWriter fileWriter, String content) throws IOException {
            //解决导出csv中文乱码
            fileWriter.append((char) 0xFEFF);
            fileWriter.append(content);
        }

        private File getLogFile(String folderName, String fileName) {

            File folder = new File(folderName);
            if (!folder.exists()) {
                //TODO: What if folder is not created, what happens then?
                boolean mkdirs = folder.mkdirs();
            }

            int newFileCount = 0;
            File newFile;
            File existingFile = null;

            newFile = new File(folder, String.format("%s_%s.%s", fileName, newFileCount, fileType));
            while (newFile.exists()) {
                existingFile = newFile;
                newFileCount++;
                newFile = new File(folder, String.format("%s_%s.%s", fileName, newFileCount, fileType));
            }

            if (existingFile != null) {
                if (existingFile.length() >= maxFileSize) {
                    return newFile;
                }
                return existingFile;
            }

            return newFile;
        }
    }
}