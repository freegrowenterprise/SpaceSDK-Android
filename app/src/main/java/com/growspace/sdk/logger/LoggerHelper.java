package com.growspace.sdk.logger;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

/**
 * LoggerHelper 클래스는
 *  - 로그를 콘솔에 출력하고,
 *  - 내부 저장소 파일에 기록하며,
 *  - 필요 시 Downloads 폴더에 TXT/CSV 형식으로 로그를 내보내는 기능을 제공합니다.
 */
public class LoggerHelper {

    // CSV 파일로 내보낼 때 사용할 MIME 타입
    private static final String logsFileMimeTypeCsv = "text/csv";

    // 텍스트 파일로 내보낼 때 사용할 MIME 타입
    private static final String logsFileMimeTypeTxt = "text/plain";

    // 로그 파일의 헤더(첫 줄)에 포함될 컬럼명. CSV 내보내기 시에도 사용됨.
    private static final String logsHeader = "[Time][Demo][Event][DevName][DevMac][Distance][Azimuth][Elevation]\n";

    // 내부 파일 시스템에 기록할 로그 파일의 이름(확장자 없이)
    private static final String logsfileName = "uwbconnect";

    // Context를 통해 파일 입출력, ContentResolver 접근 등에 사용
    private final Context mContext;

    // 로그를 파일에 기록할지 여부를 판단하는 플래그 (true면 파일 기록 활성)
    private boolean mLogsEnabled = true;

    // 현재 데모 이름(모듈 식별자) 로그 출력 및 파일 기록 시 사용
    private String demoName = "Default";

    /**
     * 로그 이벤트 유형을 정의하는 열거형.
     * 각 값은 실제 로그에 기록될 문자열(event)과 매핑됩니다.
     */
    public enum LogEvent {
        LOG_EVENT_DEMO_START("DEMO_START"),                            // 데모 시작 시점
        LOG_EVENT_DEMO_STOP("DEMO_STOP"),                              // 데모 중지 시점
        LOG_EVENT_DEMO_FINISHED("DEMO_FINISHED"),                      // 데모 완전 종료 시점
        LOG_EVENT_BLE_SCAN_START("BLE_SCAN_START"),                    // BLE 스캔 시작 시점
        LOG_EVENT_BLE_SCAN_STOP("BLE_SCAN_STOP"),                      // BLE 스캔 중지 시점
        LOG_EVENT_BLE_DEV_SCANNED("BLE_DEV_SCANNED"),                  // BLE 장치 스캔 결과 시점
        LOG_EVENT_BLE_DEV_CONNECTING("BLE_DEV_CONNECTING"),            // BLE 장치 연결 시도 시점
        LOG_EVENT_BLE_DEV_CONNECTED("BLE_DEV_CONNECTED"),              // BLE 장치 연결 성공 시점
        LOG_EVENT_BLE_DEV_DISCONNECTED("BLE_DEV_DISCONNECTED"),        // BLE 장치 연결 해제 시점
        LOG_EVENT_UWB_RANGING_START("UWB_RANGING_START"),              // UWB ranging 세션 시작 시점
        LOG_EVENT_UWB_RANGING_RESULT("UWB_RANGING_RESULT"),            // UWB ranging 결과 시점
        LOG_EVENT_UWB_RANGING_ERROR("UWB_RANGING_ERROR"),              // UWB ranging 실행 중 오류 발생 시점
        LOG_EVENT_UWB_RANGING_PEER_DISCONNECTED("UWB_RANGING_PEER_DISCONNECTED"), // UWB 상대 디스커넥트 시점
        LOG_EVENT_UWB_RANGING_STOP("UWB_RANGING_STOP");                // UWB ranging 세션 종료 시점

        // 실제 로그 문자열로 사용될 내부 필드
        private final String event;

        // 생성자: enum 값과 매핑될 문자열을 설정
        LogEvent(String str) {
            this.event = str;
        }

        @NonNull
        @Override
        public String toString() {
            // enum을 문자열로 변환할 때 사용
            return this.event;
        }
    }

    /**
     * 생성자
     * @param context 파일 및 ContentResolver 접근을 위한 Android Context
     */
    public LoggerHelper(Context context) {
        this.mContext = context;
    }

    /**
     * 데모 이름 설정
     * @param str 데모 식별용 이름
     */
    public void setDemoName(String str) {
        this.demoName = str;
    }

    /**
     * 로그 파일 기록 활성화/비활성화 설정
     * @param z true면 파일에도 로그를 기록, false면 콘솔만 출력
     */
    public void setLogsEnabled(boolean z) {
        this.mLogsEnabled = z;
    }

    /**
     * 단순 이벤트 로깅 메서드
     * @param str 이벤트 식별 문자열(보통 LogEvent.toString())
     */
    public void log(String str) {
        // 콘솔에 로그 출력 (시간 + 데모이름 + 이벤트)
        logConsole(this.demoName, str);
        // 파일 기록이 활성화되어 있으면 내부 파일에 로그 추가
        if (this.mLogsEnabled) {
            logFile(this.demoName, str);
        }
    }

    /**
     * 디바이스 이름/주소와 함께 로깅
     * @param str 이벤트 식별 문자열
     * @param str2 디바이스 이름
     * @param str3 디바이스 MAC 주소
     */
    public void log(String str, String str2, String str3) {
        // 콘솔에 로그 출력 (시간 + 데모 + 이벤트 + DevName + DevMac)
        logConsole(this.demoName, str, str2, str3);
        // 파일 기록
        if (this.mLogsEnabled) {
            logFile(this.demoName, str, str2, str3);
        }
    }

    /**
     * 거리·방위·고도 등 추가 정보와 함께 로깅
     * @param str   이벤트 식별 문자열
     * @param str2  디바이스 이름
     * @param str3  디바이스 MAC
     * @param str4  거리 정보
     * @param str5  방위(azimuth) 정보
     * @param str6  고도(elevation) 정보
     */
    public void log(String str, String str2, String str3, String str4, String str5, String str6) {
        // 콘솔에 로그 출력 (시간 + 데모 + 이벤트 + DevName + DevMac + Distance + Azimuth + Elevation)
        logConsole(this.demoName, str, str2, str3, str4, str5, str6);
        // 파일 기록
        if (this.mLogsEnabled) {
            logFile(this.demoName, str, str2, str3, str4, str5, str6);
        }
    }

    /**
     * 내부 파일에 저장된 모든 로그를 문자열로 읽어옴
     * @return 로그 전체 문자열
     * @throws FileNotFoundException 파일이 존재하지 않을 때 던져짐
     */
    public String readLogs() throws FileNotFoundException {
        return readFile(0);
    }

    /**
     * 마지막 i줄만 읽기
     * @param i 가져올 마지막 줄 개수
     * @return 해당 줄 수만큼의 로그 문자열
     * @throws FileNotFoundException 파일이 없을 때
     */
    public String readLogs(int i) throws FileNotFoundException {
        return readFile(i);
    }

    /**
     * 로그를 TXT 파일로 내보내기
     *  - 파일명: {YYYYMMDD}_uwbconnect.txt
     *  - Downloads 폴더에 저장
     * @throws FileNotFoundException 내부 로그 파일이 없으면 발생
     */
    public void exportLogsTxt() throws FileNotFoundException {
        // 1) 파일명 생성: 현재 날짜_uwbconnect.txt
        String str = getTimeExport() + "_uwbconnect.txt";

        // 2) MediaStore에 저장할 속성 준비
        ContentValues contentValues = new ContentValues();
        contentValues.put("_display_name", str);
        contentValues.put("mime_type", logsFileMimeTypeTxt);
        contentValues.put("relative_path", Environment.DIRECTORY_DOWNLOADS);

        // 3) ContentResolver로 새 파일 URI 생성
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri insert = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

        if (insert != null) {
            // 4) 헤더 + 전체 로그 문자열 결합
            String concat = logsHeader.concat(readLogs());
            try {
                // 5) OutputStream 열기
                OutputStream openOutputStream = contentResolver.openOutputStream(insert);
                if (openOutputStream == null) {
                    Log.e("LoggerHelper", "Failed to open output stream for TXT export");
                } else {
                    // 6) 바이트로 변환 후 쓰기
                    openOutputStream.write(concat.getBytes());
                    openOutputStream.close();
                }
            } catch (Exception e) {
                Log.e("LoggerHelper", "Error writing TXT file: " + e.getMessage());
            }
        }

        // 7) pending 상태 해제
        contentValues.clear();
        contentValues.put("is_pending", 0);
        if (insert != null) {
            contentResolver.update(insert, contentValues, null, null);
        }
    }

    /**
     * 로그를 CSV 파일로 내보내기
     *  - 파일명: {YYYYMMDD}_uwbconnect.csv
     *  - 로그 헤더 및 내부 로그를 CSV 형식으로 변환 후 저장
     * @throws FileNotFoundException 내부 파일이 없으면 발생
     */
    public void exportLogsCsv() throws FileNotFoundException {
        // 1) 파일명 생성
        String str = getTimeExport() + "_uwbconnect.csv";

        // 2) MediaStore 저장 속성 설정
        ContentValues contentValues = new ContentValues();
        contentValues.put("_display_name", str);
        contentValues.put("mime_type", logsFileMimeTypeCsv);
        contentValues.put("relative_path", Environment.DIRECTORY_DOWNLOADS);

        // 3) 새 파일 URI 얻기
        ContentResolver contentResolver = this.mContext.getContentResolver();
        Uri insert = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);

        if (insert != null) {
            // 4) 헤더+로그를 CSV 문자열로 변환
            String convertToCsv = convertToCsv(logsHeader.concat(readLogs()));
            try {
                // 5) OutputStream 열고 쓰기
                OutputStream openOutputStream = contentResolver.openOutputStream(insert);
                if (openOutputStream == null) {
                    Log.e("LoggerHelper", "Failed to open output stream for CSV export");
                } else {
                    openOutputStream.write(convertToCsv.getBytes());
                    openOutputStream.close();
                }
            } catch (Exception e) {
                Log.e("LoggerHelper", "Error writing CSV file: " + e.getMessage());
            }
        }

        // 6) pending 상태 해제
        contentValues.clear();
        contentValues.put("is_pending", 0);
        if (insert != null) {
            contentResolver.update(insert, contentValues, null, null);
        }
    }

    /**
     * 내부 로그 파일 삭제
     *  - Context.deleteFile(logsfileName) 호출
     */
    public void clearLogs() {
        this.mContext.deleteFile(logsfileName);
    }

    /**
     * 내부 파일에 문자열을 한 줄씩 append 모드로 기록
     * @param str 기록할 문자열 (줄바꿈 없음)
     */
    private void writeFile(String str) {
        try {
            // MODE_APPEND: 기존 파일 끝에 추가
            OutputStreamWriter outputStreamWriter =
                    new OutputStreamWriter(this.mContext.openFileOutput(logsfileName, Context.MODE_APPEND));
            outputStreamWriter.write(str);
            outputStreamWriter.write("\n");  // 한 줄 끝에 개행
            outputStreamWriter.close();
        } catch (Exception e) {
            Log.e("LoggerHelper", "Error writing file: " + e.getMessage());
        }
    }

    /**
     * 내부 파일에서 로그를 읽어옴
     * @param i 0이면 전체, 양수면 마지막 i줄
     * @return 읽어들인 로그 문자열(줄 단위 개행 포함)
     * @throws FileNotFoundException 파일 없으면 발생
     */
    private String readFile(int i) throws FileNotFoundException {
        int totalLines = 0;   // 전체 줄 수 카운터
        int currentLine = 0;  // 현재 처리한 줄 수
        StringBuilder sb = new StringBuilder();

        if (i > 0) {
            // 1) 전체 줄 수 계산
            try {
                Scanner scanner = new Scanner(this.mContext.openFileInput(logsfileName));
                while (scanner.hasNextLine()) {
                    scanner.nextLine();
                    totalLines++;
                }
                scanner.close();
            } catch (Exception e) {
                Log.e("LoggerHelper", "Error reading file: " + e.getMessage());
            }
        }

        // 2) 실제로 조건에 맞는 줄만 StringBuilder에 추가
        Scanner scanner2 = new Scanner(this.mContext.openFileInput(logsfileName));
        while (scanner2.hasNextLine()) {
            String nextLine = scanner2.nextLine();
            // i <= 0 (전체) 또는 마지막 i줄만
            if (i <= 0 || totalLines - currentLine <= i) {
                sb.append(nextLine).append("\n");
            }
            currentLine++;
        }
        scanner2.close();
        return sb.toString();
    }

    /**
     * 콘솔에 로그 출력 (1개 파라미터)
     * @param str  데모 이름
     */
    private void logConsole(String str) {
        System.out.println("[" + getTimeLogs() + "][" + str + "]");
    }

    /**
     * 콘솔에 로그 출력 (2개 파라미터)
     * @param str  데모 이름
     * @param str2 이벤트 또는 디바이스 이름
     */
    private void logConsole(String str, String str2) {
        System.out.println("[" + getTimeLogs() + "][" + str + "][" + str2 + "]");
    }

    /**
     * 콘솔에 로그 출력 (3개 파라미터)
     */
    private void logConsole(String str, String str2, String str3) {
        System.out.println("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "]");
    }

    /**
     * 콘솔에 로그 출력 (4개 파라미터)
     */
    private void logConsole(String str, String str2, String str3, String str4) {
        System.out.println("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "]");
    }

    /**
     * 콘솔에 로그 출력 (5개 파라미터)
     */
    private void logConsole(String str, String str2, String str3, String str4, String str5) {
        System.out.println("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "][" + str5 + "]");
    }

    /**
     * 콘솔에 로그 출력 (6개 파라미터)
     */
    private void logConsole(String str, String str2, String str3, String str4, String str5, String str6) {
        System.out.println("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "][" + str5 + "][" + str6 + "]");
    }

    /**
     * 콘솔에 로그 출력 (7개 파라미터)
     */
    private void logConsole(String str, String str2, String str3, String str4, String str5, String str6, String str7) {
        System.out.println("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "][" + str5 + "][" + str6 + "][" + str7 + "]");
    }

    /**
     * 내부 파일에 로그 기록 (1개 파라미터)
     */
    private void logFile(String str) {
        writeFile("[" + getTimeLogs() + "][" + str + "]");
    }

    /**
     * 내부 파일에 로그 기록 (2개 파라미터)
     */
    private void logFile(String str, String str2) {
        writeFile("[" + getTimeLogs() + "][" + str + "][" + str2 + "]");
    }

    /**
     * 내부 파일에 로그 기록 (3개 파라미터)
     */
    private void logFile(String str, String str2, String str3) {
        writeFile("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "]");
    }

    /**
     * 내부 파일에 로그 기록 (4개 파라미터)
     */
    private void logFile(String str, String str2, String str3, String str4) {
        writeFile("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "]");
    }

    /**
     * 내부 파일에 로그 기록 (5개 파라미터)
     */
    private void logFile(String str, String str2, String str3, String str4, String str5) {
        writeFile("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "][" + str5 + "]");
    }

    /**
     * 내부 파일에 로그 기록 (6개 파라미터)
     */
    private void logFile(String str, String str2, String str3, String str4, String str5, String str6) {
        writeFile("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "][" + str5 + "][" + str6 + "]");
    }

    /**
     * 내부 파일에 로그 기록 (7개 파라미터)
     */
    private void logFile(String str, String str2, String str3, String str4, String str5, String str6, String str7) {
        writeFile("[" + getTimeLogs() + "][" + str + "][" + str2 + "][" + str3 + "][" + str4 + "][" + str5 + "][" + str6 + "][" + str7 + "]");
    }

    /**
     * 현재 시각을 "yyyy/MM/dd HH:mm:ss.SSS" 형식으로 반환
     */
    private String getTimeLogs() {
        return DateTimeFormatter
                .ofPattern("yyyy/MM/dd HH:mm:ss.SSS")
                .format(LocalDateTime.now());
    }

    /**
     * 내보내기용 날짜를 "yyyyMMdd" 형식으로 반환
     */
    private String getTimeExport() {
        return DateTimeFormatter
                .ofPattern("yyyyMMdd")
                .format(LocalDateTime.now());
    }

    /**
     * 로그 헤더 + 본문을 CSV 형식으로 변환
     *  - 각 줄의 "[...][...]" 형태를 "...,..."로 변경
     * @param str 변환 대상 문자열 (헤더 포함)
     * @return CSV 변환된 문자열
     */
    private String convertToCsv(String str) {
        StringBuilder sb = new StringBuilder();
        // 줄 단위로 분리 (\r\n 또는 \n)
        for (String line : str.split("\\r?\\n")) {
            if (!line.isEmpty()) {
                // 맨 앞과 뒤의 [] 제거, 내부의 "]["를 쉼표로 대체
                sb.append(line.substring(1, line.length() - 1).replace("][", ","));
                sb.append("\n");
            }
        }
        return sb.toString();
    }
}
