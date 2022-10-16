package site.haihui.challenge.utils;

import java.util.regex.Pattern;

import org.springframework.util.DigestUtils;

public class StringUtils {

    /**
     * 以`*`对字符串进行脱敏
     * 
     * @param content
     * @param begin 包含
     * @param end 不包含
     * @return
     */
    public static String getStarString(String content, int begin, int end) {
        if (begin >= content.length() || begin < 0) {
            return content;
        }
        if (end > content.length() || end < 0) {
            return content;
        }
        if (begin >= end) {
            return content;
        }
        String starStr = "";
        for (int i = begin; i < end; i++) {
            starStr = starStr + "*";
        }
        return content.substring(0, begin) + starStr + content.substring(end, content.length());
    }

    /**
     * 判断字符串是否全部为数字
     * 
     * @param str
     * @return
     */
    public static boolean isNumeric(String str) {
        Pattern pattern = Pattern.compile("[0-9]*");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断字符串是否为null和长度为0
     * 
     * @param str
     * @return
     */
    public static boolean isBlank(String str) {
        if (null == str || str.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * md5 hash
     * 
     * @param str
     * @return
     */
    public static String getMd5Str(String str) {
        return DigestUtils.md5DigestAsHex(str.getBytes());
    }

    /**
     * 粗略判断手机号
     * 
     * @param str
     * @return
     */
    public static boolean isMobile(String str) {
        Pattern pattern = Pattern.compile("^1[0-9]{10}$");
        return pattern.matcher(str).matches();
    }

    /**
     * 粗略判断身份证
     * 
     * @param str
     * @return
     */
    public static boolean isIdCard(String str) {
        Pattern pattern = Pattern.compile("^[1-9]\\d{16}[0-9Xx]{1}$");
        return pattern.matcher(str).matches();
    }

    public static String makeRankListCacheKey(Integer type) {
        return "rankinglist:" + Time.getCurrentWeekOfYear() + ":" + type;
    }
}
