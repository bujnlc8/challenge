package site.haihui.challenge.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class MineVO implements Serializable {

    private String nickname;

    private String avatar;

    private Integer totalQuestion;

    private Integer wrongQuestion;

    private Integer wrongValidQuestion;

    private Integer maxScore;

    private Integer coin;

    private Integer grade;

    private String toUpgradeCoin;
}
