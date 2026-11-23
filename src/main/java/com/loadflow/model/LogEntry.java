package com.loadflow.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class LogEntry {
    public enum Level { INFO, WARN, ERROR, SUCCESS }

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final String timestamp;
    private final Level level;
    private final String message;

    public LogEntry(Level level, String message) {
        this.timestamp = LocalTime.now().format(TIME_FMT);
        this.level = level;
        this.message = message;
    }

    public String getTimestamp() { return timestamp; }
    public Level getLevel() { return level; }
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return "[" + timestamp + "] [" + level + "] " + message;
    }
}
