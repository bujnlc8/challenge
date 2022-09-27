package site.haihui.challenge.common.constant;

public class WeixinMiniMessageType {

    public static final Integer APPLY_ADOPT = 0; // 申请领养通知

    public static final Integer APPLY_ADOPT_RESULT = 1; // 申请领养结果通知

    public static final Integer ADOPT_REPORT_USER_REMIND = 2; // 宠安失信人黑名单更新

    public static final Integer ADOPT_TO_BE_AUDITED_REMIND = 3; // 宠安家送养协议未审核提醒

    public static final Integer ADOPT_NEW_PETS_REMIND = 4; // 宠安家宠物上线提醒

    public static final Integer ADOPT_CANCEL_FORBID = 5; // 用户解除禁言通知

    public static final Integer AGREEMENT_AND_ADOPT_PROGRESS = 6; // 协议和领养进度通知

    public static final Integer ADOPT_PRIVATE_NOTICE = 7; // 领养私信通知

    public static final Integer ADOPT_ABOUT_RECORD_DYNAMICES = 8; // 宠安家打卡相关通知

    public static String getTemplateIdByType(Integer type) {
        String templateId = "";
        switch (type) {
            case 0:
                templateId = "61qWtr6RMsCbsC82V3kXMqsoLOjBD2jETs8wYC1WmC4";
                break;
            case 1:
                templateId = "j02FYIAHIYTKzFkXduXMocd-Khlh9G4cxTZ0dS73QiE";
                break;
            case 2:
                templateId = "GHAkd72VSyOEIjHk1Se1sANhE6GqzniiGxOMNeTxAsw";
                break;
            case 3:
                templateId = "Eac5VfQ8c9kPlRQEFR9Vcj2-UtH4NrDbMxEYS7W9a48";
                break;
            case 4:
                templateId = "_2G3B8NYpUIMHl0shwEaMzaVcg2yS4AeFByklWoUK9I";
                break;
            case 5:
                templateId = "FPLrDWJJoDbThZUuiam0f0BXEsRzBTI1KVQmk4IvjpM";
                break;
            case 6:
                templateId = "SKMQ0suD-OPpeEWcIZ0DheFuMKQnNQdX6CGuwc2EqEw";
                break;
            case 7:
                templateId = "Gb2IOETd6zSY49kiWP758IYaSiQApiH2m49naLnR_Gs";
                break;
            case 8:
                templateId = "8t5NENjl7O_xJvuPkXZBRHWlkur81eeljzuBb3guSao";
                break;
        }
        return templateId;
    }
}
