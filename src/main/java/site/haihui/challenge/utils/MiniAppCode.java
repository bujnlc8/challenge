package site.haihui.challenge.utils;

import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import site.haihui.challenge.common.exception.BaseException;

/** Shared validation for WeChat-generated miniapp code images. */
public final class MiniAppCode {
    private MiniAppCode() {}

    public static void requireEnvironment(String environment) {
        if (!Arrays.asList("release", "trial", "develop").contains(environment)) {
            throw new BaseException(400, "小程序环境无效");
        }
    }

    public static Map<String, Object> imagePayload(byte[] image) {
        String mime;
        if (image != null && image.length >= 8 && (image[0] & 255) == 137 && image[1] == 80 && image[2] == 78
            && image[3] == 71 && image[4] == 13 && image[5] == 10 && image[6] == 26 && image[7] == 10) {
            mime = "image/png";
        } else if (image != null && image.length >= 3 && (image[0] & 255) == 255 && (image[1] & 255) == 216
            && (image[2] & 255) == 255) {
            mime = "image/jpeg";
        } else {
            throw new BaseException(400, "小程序码生成失败，请稍后重试");
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("mime_type", mime);
        result.put("image_base64", Base64.getEncoder().encodeToString(image));
        return result;
    }
}
