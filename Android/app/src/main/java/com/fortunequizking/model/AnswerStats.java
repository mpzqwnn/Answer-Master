package com.fortunequizking.model;

/**
 * 用户答题统计数据类
 */
public class AnswerStats {
    private int todayCount; // 今日答题数
    private int totalCount; // 历史答题总数
    private int todayCorrectCount; // 今日答对题数

    public AnswerStats() {
    }

    public AnswerStats(int todayCount, int totalCount) {
        this.todayCount = todayCount;
        this.totalCount = totalCount;
    }

    public int getTodayCount() {
        return todayCount;
    }

    public void setTodayCount(int todayCount) {
        this.todayCount = todayCount;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getTodayCorrectCount() {
        return todayCorrectCount;
    }

    public void setTodayCorrectCount(int todayCorrectCount) {
        this.todayCorrectCount = todayCorrectCount;
    }
}