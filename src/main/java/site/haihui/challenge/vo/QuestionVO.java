package site.haihui.challenge.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class QuestionVO implements Serializable {

    private Integer id;

    private String content;

    private String options;

    private Integer level;

    private Integer category;
}
