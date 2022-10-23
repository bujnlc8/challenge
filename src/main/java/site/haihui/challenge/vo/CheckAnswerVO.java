package site.haihui.challenge.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class CheckAnswerVO implements Serializable {

    private Integer result;

    private Integer totalScore;

    private Integer rightTotalQuestion;

    private Integer rightAnswer = 0;

    private String toast = "";

    private Integer rank = 0;

    private String analysis = "";
}
