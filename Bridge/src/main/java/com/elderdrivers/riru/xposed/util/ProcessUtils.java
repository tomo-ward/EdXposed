package com.elderdrivers.riru.xposed.util;

import android.os.Process;
import android.text.TextUtils;

import com.elderdrivers.riru.xposed.Main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

public class ProcessUtils {

    public static String getCurrentProcessName() {
        String prettyName = Main.appProcessName;
        if (!TextUtils.isEmpty(prettyName)) {
            return prettyName;
        }
        return getProcessName(Process.myPid());
    }

    /**
     * a common solution from https://stackoverflow.com/a/21389402
     * <p>
     * use {@link com.elderdrivers.riru.xposed.Main#appProcessName} to get current process name
     */
    public static String getProcessName(int pid) {
        BufferedReader cmdlineReader = null;
        try {
            cmdlineReader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(
                            "/proc/" + pid + "/cmdline"),
                    "iso-8859-1"));
            int c;
            StringBuilder processName = new StringBuilder();
            while ((c = cmdlineReader.read()) > 0) {
                processName.append((char) c);
            }
            return processName.toString();
        } catch (Throwable throwable) {
            Utils.logW("getProcessName: " + throwable.getMessage());
        } finally {
            try {
                if (cmdlineReader != null) {
                    cmdlineReader.close();
                }
            } catch (Throwable throwable) {
                Utils.logE("getProcessName: " + throwable.getMessage());
            }
        }
        return "";
    }

    public static boolean isLastPidAlive(File lastPidFile) {
        String lastPidInfo = FileUtils.readLine(lastPidFile);
        try {
            String[] split = lastPidInfo.split(":", 2);
            return checkProcessAlive(Integer.parseInt(split[0]), split[1]);
        } catch (Throwable throwable) {
            Utils.logW("error when check last pid " + lastPidFile + ": " + throwable.getMessage());
            return false;
        }
    }

    public static void saveLastPidInfo(File lastPidFile, int pid, String processName) {
        try {
            if (!lastPidFile.exists()) {
                lastPidFile.getParentFile().mkdirs();
                lastPidFile.createNewFile();
            }
        } catch (Throwable throwable) {
        }
        FileUtils.writeLine(lastPidFile, pid + ":" + processName);
    }

    public static boolean checkProcessAlive(int pid, String processName) {
        String existsPrcName = getProcessName(pid);
        Utils.logW("checking pid alive: " + pid + ", " + processName + ", processName=" + existsPrcName);
        return existsPrcName.equals(processName);
    }
}
