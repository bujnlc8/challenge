package site.haihui.challenge.vo;

import java.io.Serializable;

import lombok.Data;

@Data
public class RankingVO implements Serializable {

    private Integer uid;

    private String nickname;

    private String avatarUrl;

    private Integer totalScore;

    private Integer totalQuestion;
}
