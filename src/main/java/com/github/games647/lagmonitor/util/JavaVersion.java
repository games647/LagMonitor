package com.github.games647.lagmonitor.util;

import com.google.common.collect.ComparisonChain;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaVersion implements Comparable<JavaVersion> {

    public static final JavaVersion LATEST = new JavaVersion("13.0.1", 13, 0, 1, false);

    private static final Pattern VERSION_PATTERN = Pattern.compile("((1\\.)?(\\d+))(\\.(\\d+))?(\\.(\\d+))?");

    private final String raw;

    private final int major;
    private final int minor;
    private final int security;
    private final boolean preRelease;

    protected JavaVersion(String raw, int major, int minor, int security, boolean preRelease) {
        this.raw = raw;
        this.major = major;
        this.minor = minor;
        this.security = security;
        this.preRelease = preRelease;
    }

    public JavaVersion(String version) {
        raw = version;
        preRelease = version.contains("-ea") || version.contains("-internal");

        Matcher matcher = VERSION_PATTERN.matcher(version);
        if (!matcher.find()) {
            throw new IllegalStateException("Cannot parse Java version");
        }

        major = Optional.ofNullable(matcher.group(3)).map(Integer::parseInt).orElse(0);
        if (major == 8) {
            // If you have a better solution feel free to contribute
            // Source: http://openjdk.java.net/jeps/223
            // Minor releases containing changes beyond security fixes are multiples of 20. Security releases based on
            // the previous minor release are odd numbers incremented by five, or by six if necessary in order to keep
            // the update number odd.
            int update = Integer.parseInt(version.substring(version.indexOf('_') + 1, version.length()));
            minor = update / 20;
            security = update % 20;
        } else {
            minor = Optional.ofNullable(matcher.group(5)).map(Integer::parseInt).orElse(0);
            security = Optional.ofNullable(matcher.group(7)).map(Integer::parseInt).orElse(0);
        }
    }

    public static JavaVersion detect() {
        return new JavaVersion(System.getProperty("java.version"));
    }

    public String getRaw() {
        return raw;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getSecurity() {
        return security;
    }

    public boolean isPreRelease() {
        return preRelease;
    }

    public boolean isOutdated() {
        return this.compareTo(LATEST) < 0;
    }

    @Override
    public int compareTo(JavaVersion other) {
        return ComparisonChain.start()
                .compare(major, other.major)
                .compare(minor, other.minor)
                .compare(security, other.security)
                .compareTrueFirst(preRelease, other.preRelease)
                .result();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof JavaVersion)) return false;
        JavaVersion that = (JavaVersion) other;
        return major == that.major &&
                minor == that.minor &&
                security == that.security &&
                preRelease == that.preRelease;
    }

    @Override
    public int hashCode() {
        return Objects.hash(raw, major, minor, security, preRelease);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + '{' +
                "raw='" + raw + '\'' +
                ", major=" + major +
                ", minor=" + minor +
                ", security=" + security +
                ", preRelease=" + preRelease +
                '}';
    }
}
