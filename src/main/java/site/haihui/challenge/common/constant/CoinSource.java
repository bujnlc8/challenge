package site.haihui.challenge.common.constant;

public class CoinSource {

    public static final Integer NEW_USER = 1;

    public static final Integer ANSWER = 2;

    public static final Integer SHARE = 3;

    public static final Integer RELIVE = 4;

    public static final Integer TRAIN = 5;

    public static final Integer CHALLENGE = 6;

    public static final Integer ANSWER_CORRECT = 7;

    public static final Integer FEEDBACK = 8;

    public static final Integer FIVE_K = 9;

    public static final Integer TEN_K = 10;

    public static final Integer FIVE_CONTINUE = 11;

    public static final Integer TEN_CONTINUE = 12;

    public static final Integer TWENTY_CONTINUE = 13;

    public static String getSourceText(Integer source) {
        switch (source) {
            case 1:
                return "新用户注册";
            case 2:
                return "每日答题";
            case 3:
                return "分享给朋友";
            case 4:
                return "复活消耗";
            case 5:
                return "练习达标";
            case 6:
                return "答题满百";
            case 7:
                return "答题奖励";
            case 8:
                return "反馈奖励";
            case 9:
                return "单轮满5k";
            case 10:
                return "单轮满1w";
            case 11:
                return "连对5题";
            case 12:
                return "连对10题";
            case 13:
                return "连对20题";
        }
        return "";
    }

    public static Integer getCoinAmount(Integer source) {
        switch (source) {
            case 1:
                return 10000;
            case 2:
                return 1000;
            case 3:
                return 1000;
            case 4:
                return -1000;
            case 5:
                return 10000;
            case 6:
                return 10000;
            case 8:
                return 500;
            case 9:
                return 500;
            case 10:
                return 1000;
            case 11:
                return 500;
            case 12:
                return 1000;
            case 13:
                return 5000;
        }
        return 0;
    }
}
