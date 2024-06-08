package de.shiewk.smoderation.util;

import org.jetbrains.annotations.Range;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public abstract class TimeUtil {
    private TimeUtil(){}

    public static String formatTimeLong(long millis){
        long seconds = millis / 1000;
        millis -= seconds * 1000;

        long minutes = seconds / 60;
        seconds -= minutes * 60;

        long hours = minutes / 60;
        minutes -= hours * 60;

        long days = hours / 24;
        hours -= days * 24;

        long years = days / 365;
        days -= years * 365;

        long months = days / 30;
        days -= months * 30;

        long weeks = days / 7;
        days -= weeks * 7;

        StringBuilder builder = new StringBuilder();

        if (years > 0){
            if (!builder.isEmpty()){
                builder.append(" ");
            }
            builder.append("%s years".formatted(years));
        }

        if (months > 0){
            if (!builder.isEmpty()){
                builder.append(" ");
            }
            builder.append("%s months".formatted(months));
        }

        if (weeks > 0){
            if (!builder.isEmpty()){
                builder.append(" ");
            }
            builder.append("%s weeks".formatted(weeks));
        }

        if (days > 0){
            if (!builder.isEmpty()){
                builder.append(" ");
            }
            builder.append("%s days".formatted(days));
        }

        if (hours > 0){
            if (!builder.isEmpty()){
                builder.append(" ");
            }
            builder.append("%s hours".formatted(hours));
        }

        if (minutes > 0){
            if (!builder.isEmpty()){
                builder.append(" ");
            }
            builder.append("%s minutes".formatted(minutes));
        }

        if (seconds > 0){
            if (!builder.isEmpty()){
                builder.append(" ");
            }
            builder.append("%s seconds".formatted(seconds));
        }

        if (builder.isEmpty()){
            builder.append("%s ms".formatted(millis));
        }

        return builder.toString();
    }

    public static long parseDurationMillisSafely(String in){
        try {
            return parseDurationMillis(in);
        } catch (Throwable e){
            return -1;
        }
    }

    public static long parseDurationMillis(String in){
        if (in.endsWith("ms")){
            return Long.parseLong(in.substring(0, in.length()-2));
        } else if (in.endsWith("s")){
            return Long.parseLong(in.substring(0, in.length()-1)) * 1000L;
        } else if (in.endsWith("min")){
            return Long.parseLong(in.substring(0, in.length()-3)) * 60000L;
        } else if (in.endsWith("h")){
            return Long.parseLong(in.substring(0, in.length()-1)) * 3600000L;
        } else if (in.endsWith("d")){
            return Long.parseLong(in.substring(0, in.length()-1)) * 86400000L;
        } else if (in.endsWith("w")){
            return Long.parseLong(in.substring(0, in.length()-1)) * 604800000L;
        } else if (in.endsWith("mo")){
            return Long.parseLong(in.substring(0, in.length()-2)) * 2592000000L;
        } else if (in.endsWith("y")){
            return Long.parseLong(in.substring(0, in.length()-1)) * 31536000000L;
        } else {
            return -1;
        }
    }

    public static String calendarTimestamp(long time){
        Date date = new Date(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        TimeZone zone = calendar.getTimeZone();
        int second = calendar.get(Calendar.SECOND);
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int year = calendar.get(Calendar.YEAR);

        String day = numberWithSuffix(calendar.get(Calendar.DAY_OF_MONTH));
        String month = monthName(calendar.get(Calendar.MONTH));
        return "%s %s %s, %s:%s:%s %s".formatted(
                month,
                day,
                year,
                hour < 10 ? "0" + hour : hour,
                minute < 10 ? "0" + minute : minute,
                second < 10 ? "0" + second : second,
                zone.getDisplayName(false, TimeZone.SHORT)
        );
    }

    private static String numberWithSuffix(int i){
        return i + numberSuffix(i);
    }

    private static String numberSuffix(int i){
        if ((i % 10) == 1 && i != 11){
            return "st";
        } else if ((i % 10) == 2 && i != 12){
            return "nd";
        } else if ((i % 10) == 3 && i != 13){
            return "rd";
        }
        return "th";
    }

    public static String monthName(@Range(from = 0, to = 11) int m){
        switch (m){
            case 0 -> {
                return "January";
            }
            case 1 -> {
                return "February";
            }
            case 2 -> {
                return "March";
            }
            case 3 -> {
                return "April";
            }
            case 4 -> {
                return "May";
            }
            case 5 -> {
                return "June";
            }
            case 6 -> {
                return "July";
            }
            case 7 -> {
                return "August";
            }
            case 8 -> {
                return "September";
            }
            case 9 -> {
                return "October";
            }
            case 10 -> {
                return "November";
            }
            case 11 -> {
                return "December";
            }
        }
        return "Unknown Month";
    }
}
