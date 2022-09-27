package site.haihui.challenge.utils;

import java.text.DecimalFormat;

public class Numbers {

    /**
     * 将数字转换成缩写 xk,xw
     * 
     * @param num
     * @return
     */
    public static String formatNumberAbbreviation(Integer num) {
        DecimalFormat df = new DecimalFormat("0.00");
        if (num >= 10000) {
            return df.format((float) num / 10000f) + "w";
        } else if (num >= 1000) {
            return df.format((float) num / 1000f) + "k";
        }
        return num.toString();
    }

    public static boolean isBlank(Integer num) {
        if (null != num && num > 0) {
            return false;
        }
        return true;
    }
}
