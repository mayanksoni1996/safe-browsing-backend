package tech.mayanksoni.threatdetectionbackend.utils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeProcessor {
    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\\\d+)([a-zA-Z])$");
    public static Instant processTime(String timeString){
        Matcher timePatternMatcher = TIME_PATTERN.matcher(timeString);
        int timeValue = Integer.parseInt(timePatternMatcher.group(1));
        char timeUnit = timePatternMatcher.group(2).charAt(0);
        return Instant.now().plus(timeValue, getChronoUnit(timeUnit));
    }

    private static ChronoUnit getChronoUnit(char timeString){
        return switch (timeString) {
            case 'd' -> ChronoUnit.DAYS;
            case 'h' -> ChronoUnit.HOURS;
            case 'm' -> ChronoUnit.MINUTES;
            case 's' -> ChronoUnit.SECONDS;
            default -> throw new IllegalArgumentException("Invalid time unit: " + timeString);
        };
    }

}
