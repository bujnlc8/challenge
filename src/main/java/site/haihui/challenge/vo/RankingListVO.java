package site.haihui.challenge.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class RankingListVO implements Serializable {

    private Integer resetSeconds;

    private List<RankingVO> list;

    private Integer rank;

    private RankingVO mine;
}
