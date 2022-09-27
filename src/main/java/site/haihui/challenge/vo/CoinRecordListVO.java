package site.haihui.challenge.vo;

import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class CoinRecordListVO implements Serializable {

    private List<CoinRecordVO> list;

    private Integer total;
}
