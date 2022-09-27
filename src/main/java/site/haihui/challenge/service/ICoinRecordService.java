package site.haihui.challenge.service;

import com.baomidou.mybatisplus.extension.service.IService;

import site.haihui.challenge.entity.CoinRecord;
import site.haihui.challenge.vo.CoinRecordListVO;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-21
 */
public interface ICoinRecordService extends IService<CoinRecord> {

    public boolean operateCoin(Integer uid, Integer source, Integer roundId, Integer amount);

    public Integer getCoin(Integer uid);

    public CoinRecordListVO getCoinRecord(Integer uid, Integer page, Integer size);

    public Integer getGrade(Integer uid);

    public Integer getToUpgradeCoin(Integer uid);
}
