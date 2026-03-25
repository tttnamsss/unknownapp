package com.enrollment.model;

/**
 * Represents a scheduled time slot for a course, including day(s) and start/end times.
 */
public class TimeSlot {

    private String days;      // e.g. "MWF" or "TTh"
    private String startTime; // 24-hour format "HH:mm"
    private String endTime;   // 24-hour format "HH:mm"

    public TimeSlot() {}

    public TimeSlot(String days, String startTime, String endTime) {
        this.days = days;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * Returns true if this time slot overlaps with the given time slot.
     * Two slots conflict if they share at least one common day AND their time ranges overlap.
     */
    public boolean overlaps(TimeSlot other) {
        if (other == null || this.days == null || other.days == null) {
            return false;
        }
        if (!shareDays(this.days, other.days)) {
            return false;
        }
        // Convert times to minutes for easy comparison
        int thisStart = toMinutes(this.startTime);
        int thisEnd = toMinutes(this.endTime);
        int otherStart = toMinutes(other.startTime);
        int otherEnd = toMinutes(other.endTime);

        if (thisStart < 0 || thisEnd < 0 || otherStart < 0 || otherEnd < 0) {
            return false;
        }
        // Overlap when one interval starts before the other ends
        return thisStart < otherEnd && otherStart < thisEnd;
    }

    /** Returns true when the two day strings share at least one day character/token. */
    private boolean shareDays(String d1, String d2) {
        // Normalize to uppercase
        String a = d1.toUpperCase();
        String b = d2.toUpperCase();

        // Treat "T" as Thursday and "Th" as a distinct token; handle "TTh" style strings
        String[] tokens = {"TH", "M", "T", "W", "F", "S", "U"};
        for (String token : tokens) {
            if (a.contains(token) && b.contains(token)) {
                return true;
            }
        }
        return false;
    }

    /** Converts "HH:mm" to total minutes. Returns -1 if format is invalid. */
    private int toMinutes(String time) {
        if (time == null || !time.contains(":")) {
            return -1;
        }
        try {
            String[] parts = time.split(":");
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public String toString() {
        return days + " " + startTime + "-" + endTime;
    }
}
