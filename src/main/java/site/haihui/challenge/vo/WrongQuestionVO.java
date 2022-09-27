package site.haihui.challenge.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class WrongQuestionVO implements Serializable {

    private Integer id;

    private String content;

    private String options;

    private Integer answer;

    private Integer correctAnswer;

    private String createTime;

    private Integer questionId;
}
