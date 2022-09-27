package site.haihui.challenge.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class QuestionListVO implements Serializable {

    private List<QuestionVO> list;

    private Integer roundId;

    private Integer questionNum;

    private Integer lastSkipChance;

    private Integer lastReliveTimes;

    private Integer timeout;
}
