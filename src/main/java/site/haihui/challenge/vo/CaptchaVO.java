package site.haihui.challenge.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class CaptchaVO implements Serializable {

    private String token;

    private String imageData;
}
