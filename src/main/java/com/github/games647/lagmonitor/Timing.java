package com.github.games647.lagmonitor;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang.builder.ToStringBuilder;

public class Timing implements Comparable<Timing> {

    private final String category;

    private long totalTime;
    private long totalCount;

    private Map<String, Timing> subcategories;

    public Timing(String category) {
        this.category = category;
    }

    public Timing(String category, long totalTime, long count) {
        this.category = category;
        this.totalTime = totalTime;
        this.totalCount = count;
    }

    public String getCategoryName() {
        return category;
    }

    public long getTotalTime() {
        return totalTime;
    }

    public void addTotal(long total) {
        this.totalTime += total;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void addCount(long count) {
        this.totalCount += count;
    }

    public Map<String, Timing> getSubCategories() {
        return subcategories;
    }

    public void addSubcategory(String name, long totalTime, long count) {
        if (subcategories == null) {
            //lazy creating
            subcategories = Maps.newHashMap();
        }

        Timing timing = subcategories.computeIfAbsent(name, key -> new Timing(key, totalTime, count));
        timing.addTotal(totalTime);
        timing.addCount(totalTime);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timing timing = (Timing) o;
        return totalTime == timing.totalTime &&
                totalCount == timing.totalCount &&
                Objects.equals(category, timing.category) &&
                Objects.equals(subcategories, timing.subcategories);
    }

    @Override
    public int hashCode() {
        return Objects.hash(category, totalTime, totalCount, subcategories);
    }

    @Override
    public int compareTo(Timing other) {
        return Long.compare(totalTime, other.totalTime);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("category", category)
                .append("totalTime", totalTime)
                .append("totalCount", totalCount)
                .append("subcategories", subcategories)
                .toString();
    }
}
