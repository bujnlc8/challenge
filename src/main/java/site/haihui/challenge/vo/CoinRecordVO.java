package site.haihui.challenge.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class CoinRecordVO implements Serializable {

    private Integer amount;

    private String sourceText;

    private Integer source;

    private String createTime;
}
