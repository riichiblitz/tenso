package com.yufimtsev.tenhouj;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Log {

    private static final int MAX_LOG_PER_FILE = 5000;
    private static final SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("YYYY-MM-DD-HH-mm-ss");

    private static FileWriter writer;
    private static long logCounter;

    private static final int MAX_LOG_BUFFER = 1000;
    private static ArrayList<String> recyclerBuffer = new ArrayList<String>(MAX_LOG_BUFFER);
    private static int recyclerCursor = 0;

    static {
        resetLogFile();
    }

    public static void d(String message) {
        d("", message);
    }

    public static void d(Exception exception) {
        d("", exception);
    }

    public static void d(String tag, String message) {
        if (++logCounter > MAX_LOG_PER_FILE) {
            resetLogFile();
        }
        String logMessage = DATE_TIME_FORMAT.format(new Date()) + " " + tag + ": " + message;
        try {
            append(logMessage);
        } catch (IOException e) {
            System.out.println("COULD NOT APPEND TO LOGFILE");
            resetLogFile();
        }
    }

    public static void d(String tag, Exception exception) {
        if (++logCounter > MAX_LOG_PER_FILE) {
            resetLogFile();
        }
        String logMessage = DATE_TIME_FORMAT.format(new Date()) + " " + tag + ": EXCEPTION";
        exception.printStackTrace();
        try {
            append(logMessage);
            append(exception.getMessage());
        } catch (IOException e) {
            System.out.println("COULD NOT APPEND TO LOGFILE");
            resetLogFile();
        }
    }

    public static void resetLogFile() {
        if (writer != null) {
            try {
                writer.flush();
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String logName = "log-" + DATE_TIME_FORMAT.format(new Date()) + ".txt";

        File file = new File(logName);
        System.out.print("New log file: " + file.getAbsolutePath() +"... ");
        try {
            writer = new FileWriter(file);
            System.out.println("OK");
        } catch (IOException e) {
            System.out.println("FAILED");
            e.printStackTrace();
        }
    }

    private static synchronized void append(String log) throws IOException {
        writer.append(log);
        if (recyclerBuffer.size() < MAX_LOG_BUFFER) {
            recyclerBuffer.add(log);
        } else {
            recyclerBuffer.set(recyclerCursor, log);
        }
        recyclerCursor++;
        if (recyclerCursor == MAX_LOG_BUFFER) {
            recyclerCursor = 0;
        }
        System.out.println(log);
    }

    public static String collect() {
        StringBuilder result = new StringBuilder();
        if (recyclerBuffer.size() < MAX_LOG_BUFFER) {
            for (String log : recyclerBuffer) result.append(log).append('\n');
        } else {
            for (int i = recyclerCursor + 1; i < MAX_LOG_BUFFER; i++) {
               result.append(recyclerBuffer.get(i)).append('\n');
            }
            for (int i = 0; i < recyclerCursor + 1; i++) {
                result.append(recyclerBuffer.get(i)).append('\n');
            }
        }
        return result.toString();
    }

}
