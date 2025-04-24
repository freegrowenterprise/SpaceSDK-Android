package com.growspace.sdk.logger;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class LoggerHelper {
    private static final String logsFileMimeTypeCsv = "text/csv";
    private static final String logsFileMimeTypeTxt = "text/plain";
    private static final String logsHeader = "[Time][Demo][Event][DevName][DevMac][Distance][Azimuth][Elevation]\n";
    private static final String logsfileName = "uwbconnect";
    private final Context mContext;
    private boolean mLogsEnabled = true;
    private String demoName = "Default";

    public enum LogEvent {
        LOG_EVENT_DEMO_START("DEMO_START"),
        LOG_EVENT_DEMO_STOP("DEMO_STOP"),
        LOG_EVENT_DEMO_FINISHED("DEMO_FINISHED"),
        LOG_EVENT_BLE_SCAN_START("BLE_SCAN_START"),
        LOG_EVENT_BLE_SCAN_STOP("BLE_SCAN_STOP"),
        LOG_EVENT_BLE_DEV_SCANNED("BLE_DEV_SCANNED"),
        LOG_EVENT_BLE_DEV_CONNECTING("BLE_DEV_CONNECTING"),
        LOG_EVENT_BLE_DEV_CONNECTED("BLE_DEV_CONNECTED"),
        LOG_EVENT_BLE_DEV_DISCONNECTED("BLE_DEV_DISCONNECTED"),
        LOG_EVENT_UWB_RANGING_START("UWB_RANGING_START"),
        LOG_EVENT_UWB_RANGING_RESULT("UWB_RANGING_RESULT"),
        LOG_EVENT_UWB_RANGING_ERROR("UWB_RANGING_ERROR"),
        LOG_EVENT_UWB_RANGING_PEER_DISCONNECTED("UWB_RANGING_PEER_DISCONNECTED"),
        LOG_EVENT_UWB_RANGING_STOP("UWB_RANGING_STOP");

        private final String event;

        LogEvent(String str) {
            this.event = str;
        }

        @Override
        public String toString() {
            return this.event;
        }
    }

    public LoggerHelper(Context context) {
        this.mContext = context;
    }

    public void setDemoName(String str) {
        this.demoName = str;
    }

    public void setLogsEnabled(boolean z) {
        this.mLogsEnabled = z;
    }

    public void log(String str) {
        logConsole(this.demoName, str);
        if (this.mLogsEnabled) {
            logFile(this.demoName, str);
        }
    }

    public void log(String str, String str2, String str3) {
        logConsole(this.demoName, str, str2, str3);
        if (this.mLogsEnabled) {
            logFile(this.demoName, str, str2, str3);
        }
    }

    public void log(String str, String str2, String str3, String str4, String str5, String str6) {
        logConsole(this.demoName, str, str2, str3, str4, str5, str6);
        if (this.mLogsEnabled) {
            logFile(this.demoName, str, str2, str3, str4, str5, str6);
        }
    }

    public String readLogs() throws FileNotFoundException {
        return readFile(0);
    }

    public String readLogs(int i) throws FileNotFoundException {
        return readFile(i);
    }

    public void exportLogsTxt() throws FileNotFoundException {
        String str = getTimeExport() + "_uwbconnect.txt";
        ContentValues contentValues = new ContentValues();
        contentValues.put("_display_name", str);
        contentValues.put("mime_type", logsFileMimeTypeTxt);
        contentValues.put("relative_path", Environment.DIRECTORY_DOWNLOADS);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri insert = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        if (insert != null) {
            String concat = logsHeader.concat(readLogs());
            try {
                OutputStream openOutputStream = contentResolver.openOutputStream(insert);
                openOutputStream.write(concat.getBytes());
                openOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        contentValues.clear();
        contentValues.put("is_pending", (Integer) 0);
        contentResolver.update(insert, contentValues, null, null);
    }

    public void exportLogsCsv() throws FileNotFoundException {
        String str = getTimeExport() + "_uwbconnect.csv";
        ContentValues contentValues = new ContentValues();
        contentValues.put("_display_name", str);
        contentValues.put("mime_type", logsFileMimeTypeCsv);
        contentValues.put("relative_path", Environment.DIRECTORY_DOWNLOADS);
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri insert = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        if (insert != null) {
            String convertToCsv = convertToCsv(logsHeader.concat(readLogs()));
            try {
                OutputStream openOutputStream = contentResolver.openOutputStream(insert);
                openOutputStream.write(convertToCsv.getBytes());
                openOutputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        contentValues.clear();
        contentValues.put("is_pending", (Integer) 0);
        contentResolver.update(insert, contentValues, null, null);
    }

    public void clearLogs() {
        this.mContext.deleteFile(logsfileName);
    }

    private void writeFile(String str) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(this.mContext.openFileOutput(logsfileName, Context.MODE_APPEND));
            outputStreamWriter.write(str);
            outputStreamWriter.write("\n");
            outputStreamWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String readFile(int i) throws FileNotFoundException {
        int i2 = 0;
        StringBuilder sb = new StringBuilder();
        int i3 = 0;
        if (i > 0) {
            try {
                Scanner scanner = new Scanner(this.mContext.openFileInput(logsfileName));
                i2 = 0;
                while (scanner.hasNextLine()) {
                    scanner.nextLine();
                    i2++;
                }
                scanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            i2 = 0;
        }
        Scanner scanner2 = new Scanner(this.mContext.openFileInput(logsfileName));
        while (scanner2.hasNextLine()) {
            String nextLine = scanner2.nextLine();
            if (i <= 0 || i2 - i3 <= i) {
                sb.append(nextLine);
                sb.append("\n");
            }
            i3++;
        }
        scanner2.close();
        return sb.toString();
    }

    private void logConsole(String str) {
        System.out.println("[" + getTimeLogs() + "][" + str + "]");
    }

    private void logConsole(String str, String str2) {
        System.out.println("[" + getTimeLogs() + "][" + str + "][" + str2 + "]");
    }

    private void logConsole(String str, String str2, String str3) {
        System.out.println("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "]");
    }

    private void logConsole(String str, String str2, String str3, String str4) {
        System.out.println("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "]");
    }

    private void logConsole(String str, String str2, String str3, String str4, String str5) {
        System.out.println("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "][" + str5 + "]");
    }

    private void logConsole(String str, String str2, String str3, String str4, String str5, String str6) {
        System.out.println("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "][" + str5 + "][" + str6 + "]");
    }

    private void logConsole(String str, String str2, String str3, String str4, String str5, String str6, String str7) {
        System.out.println("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "][" + str5 + "][" + str6 + "][" + str7 + "]");
    }

    private void logFile(String str) {
        writeFile("[" + getTimeLogs() + "][" + str + "]");
    }

    private void logFile(String str, String str2) {
        writeFile("[" + getTimeLogs() + "][" + str + "][" + str2 + "]");
    }

    private void logFile(String str, String str2, String str3) {
        writeFile("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "]");
    }

    private void logFile(String str, String str2, String str3, String str4) {
        writeFile("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "]");
    }

    private void logFile(String str, String str2, String str3, String str4, String str5) {
        writeFile("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "][" + str5 + "]");
    }

    private void logFile(String str, String str2, String str3, String str4, String str5, String str6) {
        writeFile("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "][" + str5 + "][" + str6 + "]");
    }

    private void logFile(String str, String str2, String str3, String str4, String str5, String str6, String str7) {
        writeFile("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "][" + str5 + "][" + str6 + "][" + str7 + "]");
    }

    private String getTimeLogs() {
        return DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss.SSS").format(LocalDateTime.now());
    }

    private String getTimeExport() {
        return DateTimeFormatter.ofPattern("yyyyMMdd").format(LocalDateTime.now());
    }

    private String convertToCsv(String str) {
        StringBuilder sb = new StringBuilder();
        for (String str2 : str.split("\\r?\\n")) {
            if (!str2.isEmpty()) {
                sb.append(str2.substring(1, str2.length() - 1).replace("][", ","));
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
