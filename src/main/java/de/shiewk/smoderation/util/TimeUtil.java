package de.shiewk.smoderation.util;

public abstract class TimeUtil {
    private TimeUtil(){}

    public static String formatTimeLong(long millis){
        long seconds = millis / 1000;

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

        return builder.toString();
    }
}
