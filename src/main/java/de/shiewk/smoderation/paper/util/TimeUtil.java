package de.shiewk.smoderation.paper.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.jetbrains.annotations.Range;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import static net.kyori.adventure.text.Component.*;

public final class TimeUtil {
    private TimeUtil(){}

    public static Component formatTimeLong(long millis){
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

        TextComponent.Builder builder = empty().toBuilder();

        if (years > 0){
            if (!builder.children().isEmpty()){
                builder.appendSpace();
            }
            builder.append(translatable("smod.time.years", text(years)));
        }

        if (months > 0){
            if (!builder.children().isEmpty()){
                builder.appendSpace();
            }
            builder.append(translatable("smod.time.months", text(months)));
        }

        if (weeks > 0){
            if (!builder.children().isEmpty()){
                builder.appendSpace();
            }
            builder.append(translatable("smod.time.weeks", text(weeks)));
        }

        if (days > 0){
            if (!builder.children().isEmpty()){
                builder.appendSpace();
            }
            builder.append(translatable("smod.time.days", text(days)));
        }

        if (hours > 0){
            if (!builder.children().isEmpty()){
                builder.appendSpace();
            }
            builder.append(translatable("smod.time.hours", text(hours)));
        }

        if (minutes > 0){
            if (!builder.children().isEmpty()){
                builder.appendSpace();
            }
            builder.append(translatable("smod.time.minutes", text(minutes)));
        }

        if (seconds > 0){
            if (!builder.children().isEmpty()){
                builder.appendSpace();
            }
            builder.append(translatable("smod.time.seconds", text(seconds)));
        }

        if (builder.children().isEmpty()){
            builder.append(translatable("smod.time.milliseconds", text(millis)));
        }

        return builder.build();
    }

    public static Component calendarTimestamp(long time){
        Date date = new Date(time);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        TimeZone zone = calendar.getTimeZone();
        int second = calendar.get(Calendar.SECOND);
        int minute = calendar.get(Calendar.MINUTE);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int year = calendar.get(Calendar.YEAR);

        int day = calendar.get(Calendar.DAY_OF_MONTH);
        Component month = monthName(calendar.get(Calendar.MONTH));
        return translatable(
                "smod.time.timestamp",
                text(year),
                month,
                text(day),
                text((hour < 10 ? "0" : "") + hour),
                text((minute < 10 ? "0" : "") + minute),
                text((second < 10 ? "0" : "") + second),
                text(zone.getDisplayName(zone.inDaylightTime(calendar.getTime()), TimeZone.SHORT))
        );
    }

    public static Component monthName(@Range(from = 0, to = 11) int m){
        return translatable("smod.time.month." + m);
    }

}
