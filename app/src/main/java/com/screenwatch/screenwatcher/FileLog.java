package com.screenwatch.screenwatcher;

import android.app.Application;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

public class FileLog {
    public static final String TAG = "@@@@#!!!!";
    public static final String serviceFilename = "LedBellService.txt";
    public static final String activityFilename = "LedBellActivity.txt";

    public static void d(String msg) {
        String logLine = getLocation()+ msg;
        android.util.Log.d(TAG,logLine);
        appendLog(logLine,activityFilename);
    }
    public static void s(String msg) {
        String logLine = getLocation()+ msg;
        android.util.Log.d(TAG,logLine);
        appendLog("Service:"+logLine,serviceFilename);
    }

    private static String getLocation() {
        final String className = FileLog.class.getName();
        final StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        boolean found = false;

        for (int i = 0; i < traces.length; i++) {
            StackTraceElement trace = traces[i];

            try {
                if (found) {
                    if (!trace.getClassName().startsWith(className)) {
                        Class<?> clazz = Class.forName(trace.getClassName());
                        return "[" + getClassName(clazz) + ":" + trace.getMethodName() + ":" + trace.getLineNumber() + "] ";
                    }
                } else if (trace.getClassName().startsWith(className)) {
                    found = true;
                    continue;
                }
            } catch (ClassNotFoundException e) {
            }
        }

        return "[]: ";
    }

    private static String getClassName(Class<?> clazz) {
        if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.getSimpleName())) {
                return clazz.getSimpleName();
            }

            return getClassName(clazz.getEnclosingClass());
        }

        return "";
    }

    public static File getLogFile(String filename) {
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename );
        return file;
    }


    public static void createLog(String filename) {
        File file = getLogFile(filename);
        if (file.exists()) file.delete();
        try {
            file.createNewFile();

            appendLog( "Created at " + new Date().toString(),filename);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void appendLog(String line,String filename) {
        File file = getLogFile(filename);
        if (!file.exists()) createLog(filename);

        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file, true));
            bufferedWriter.write(line);
            bufferedWriter.newLine();
            bufferedWriter.flush();
            bufferedWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
