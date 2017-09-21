package com.github.games647.lagmonitor;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.stream.Stream;

public class LagUtils {

    public static int byteToMega(long bytes) {
        return (int) (bytes / (1024 * 1024));
    }

    public static float round(double number) {
        return round(number, 2);
    }

    public static float round(double value, int places) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.floatValue();
    }

    public static String readableByteCount(long bytes) {
        //https://stackoverflow.com/questions/3758606/how-to-convert-byte-size-into-human-readable-format-in-java
        int unit = 1024;
        if (bytes < unit) {
            return bytes + " B";
        }

        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = "kMGTPE".charAt(exp - 1) + "i";
        return String.format("%.2f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static long getFolderSize(File folder) {
        File[] files = folder.listFiles();
        if (files == null) {
            return -1;
        }

        return Stream.of(files)
                .parallel()
                .filter(Objects::nonNull)
                .mapToLong((File file) -> {
                    if (file.isFile()) {
                        return file.length();
                    } else {
                        return getFolderSize(file);
                    }
                }).sum();
    }
}
