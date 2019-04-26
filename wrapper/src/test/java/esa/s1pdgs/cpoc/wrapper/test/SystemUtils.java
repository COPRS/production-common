package esa.s1pdgs.cpoc.wrapper.test;

public class SystemUtils {

    public static String getCmdMkdir() {
        boolean isWindows = System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
        String command = "mkdir";
        if (isWindows) {
            command = "C:\\Program Files\\Git\\usr\\bin\\mkdir.exe";
        }
        return command;
    }

    public static String getCmdRmdir() {
        boolean isWindows = System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
        String command = "rmdir";
        if (isWindows) {
            command = "C:\\Program Files\\Git\\usr\\bin\\rmdir.exe";
        }
        return command;
    }

    public static String getCmdLs() {
        boolean isWindows = System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
        String command = "ls";
        if (isWindows) {
            command = "C:\\Program Files\\Git\\usr\\bin\\ls.exe";
        }
        return command;
    }

    public static String getCmdFalse() {
        boolean isWindows = System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
        String command = "false";
        if (isWindows) {
            command = "C:\\Program Files\\Git\\usr\\bin\\false.exe";
        }
        return command;
    }

    public static String getCmdTrue() {
        boolean isWindows = System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
        String command = "true";
        if (isWindows) {
            command = "C:\\Program Files\\Git\\usr\\bin\\true.exe";
        }
        return command;
    }

    public static String getCmdSleep() {
        boolean isWindows = System.getProperty("os.name").toLowerCase()
                .startsWith("windows");
        String command = "sleep";
        if (isWindows) {
            command = "C:\\Program Files\\Git\\usr\\bin\\sleep.exe";
        }
        return command;
    }

}
