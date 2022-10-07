package site.haihui.challenge.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Time {

    /**
     * 获取当前时间戳(s)
     *
     */
    public static Long currentTimeSeconds() {
        return new Date().getTime() / 1000;
    }

    /**
     * 获取当前时间戳(ms)
     *
     */
    public static Long currentTimeMillSeconds() {
        return new Date().getTime();
    }

    /**
     * 时间戳(s)转字符串时间
     */
    public static String timestampToString(Integer timeStamp, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(new Date(timeStamp.longValue() * 1000));
    }

    public static String simpleTimestampToString(Integer timeStamp) {
        return Time.timestampToString(timeStamp, "yyyy-MM-dd HH:mm:ss");
    }

    /**
     * 日期转换成`yyyy-MM-dd HH:mm:ss形式`的字符串
     * 
     * @param date
     * @return
     */
    public static String dateToString(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    /**
     * 将时间字符串转换成时间戳(s)
     */
    public static Long stringToTimestamp(String dateStr) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            return simpleDateFormat.parse(dateStr).getTime() / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * 获取年龄段称号，如90后
     * 
     * @param year
     * @return
     */
    public static String userAge(Integer year) {
        String result = "";
        if (year >= 1950 && year <= 1959) {
            result = "50后";
        } else if (year >= 1960 && year <= 1969) {
            result = "60后";
        } else if (year >= 1970 && year <= 1979) {
            result = "70后";
        } else if (year >= 1980 && year <= 1989) {
            result = "80后";
        } else if (year >= 1990 && year <= 1999) {
            result = "90后";
        } else if (year >= 2000 && year <= 2009) {
            result = "00后";
        } else if (year >= 2010 && year <= 2019) {
            result = "10后";
        } else if (year >= 2020 && year <= 2030) {
            result = "20后";
        }
        return result;
    }

    /**
     * 根据日期获取星座
     * 
     * @param date
     * @return
     */
    public static String getConstellationBybirthday(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMdd");
        Integer monthDay = Integer.parseInt(simpleDateFormat.format(date));
        String result = "";
        if (monthDay >= 321 && monthDay <= 419) {
            result = "白羊座";
        } else if (monthDay >= 420 && monthDay <= 520) {
            result = "金牛座";
        } else if (monthDay >= 521 && monthDay <= 621) {
            result = "双子座";
        } else if (monthDay >= 622 && monthDay <= 722) {
            result = "巨蟹座";
        } else if (monthDay >= 723 && monthDay <= 822) {
            result = "狮子座";
        } else if (monthDay >= 823 && monthDay <= 922) {
            result = "处女座";
        } else if (monthDay >= 923 && monthDay <= 1023) {
            result = "天秤座";
        } else if (monthDay >= 1024 && monthDay <= 1122) {
            result = "天蝎座";
        } else if (monthDay >= 1123 && monthDay <= 1221) {
            result = "射手座";
        } else if (monthDay >= 1222 || monthDay <= 119) {
            result = "摩羯座";
        } else if (monthDay >= 120 && monthDay <= 218) {
            result = "水瓶座";
        } else if (monthDay >= 219 && monthDay <= 320) {
            result = "双鱼座";
        }
        return result;
    }

    public static Date getCurrentYMD() {
        Date now = new Date();
        Calendar ca = Calendar.getInstance();
        ca.setTime(now);
        Calendar ca1 = Calendar.getInstance();
        ca1.set(ca.get(Calendar.YEAR), ca.get(Calendar.MONTH), ca.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
        return ca1.getTime();
    }

    public static Long getTimeAgo(Date dt, Integer month, Integer day) {
        Calendar ca = Calendar.getInstance();
        ca.setTime(dt);
        ca.add(Calendar.MONTH, month);
        ca.add(Calendar.DAY_OF_MONTH, day);
        return ca.getTime().getTime() / 1000;
    }

    public static String formatTime(Long t) {
        Date today = getCurrentYMD();
        Long oneMonthAgo = getTimeAgo(today, -1, 0);
        Long oneWeekAgo = getTimeAgo(today, 0, -6);
        Long twoDayAgo = getTimeAgo(today, 0, -2);
        Long yesterday = getTimeAgo(today, 0, -1);
        Long now = getTimeAgo(today, 0, 0);
        if (t < oneMonthAgo) {
            return "";
        } else if (t < oneWeekAgo) {
            return "1周前";
        } else if (t < twoDayAgo) {
            return "3天前";
        } else if (t < yesterday) {
            return timestampToString(t.intValue(), "MM-dd");
        } else if (t < now) {
            return "昨日 " + timestampToString(t.intValue(), "HH:mm");
        } else if (t >= now) {
            return "今日 " + timestampToString(t.intValue(), "HH:mm");
        }
        return "";
    }

    public static Date getWeekStartDate() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date date = cal.getTime();
        return date;
    }

    public static Date getWeekEndDate() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date date = cal.getTime();
        return date;
    }

    /**
     * 获取是一年中的第几周
     * 
     * @param dt
     * @return
     */
    public static Integer getWeekOfYear(Date dt) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    public static Integer getCurrentWeekOfYear() {
        return getWeekOfYear(new Date());
    }
}
