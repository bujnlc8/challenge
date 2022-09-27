package site.haihui.challenge.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class IgscVO implements Serializable {

    private Integer code;

    private String msg;

    private String token;

    private String captcha;
}
