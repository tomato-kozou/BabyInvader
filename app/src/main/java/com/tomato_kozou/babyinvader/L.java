package com.tomato_kozou.babyinvader;

import android.util.Log;

public class L {
    public static void d(String message) {
        // スタックトレースｋら現在の呼び出し情報を取得
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        // 1: getStackTrace, 2: d(L.java), 3: 呼び出し元(MainActivity.javaなど）
        StackTraceElement e = elements[3];

        String className = e.getFileName().replace(".java", "");
        String methodName = e.getMethodName();
        int lineNumber = e.getLineNumber();

        String tag = "MY_LOG";
        String logMessage = String.format("[%s.%s():%d] %s", className, methodName, lineNumber, message);

        Log.d(tag, logMessage);


    }
}
