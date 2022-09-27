package site.haihui.challenge.mapper;

import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Param;

import site.haihui.challenge.entity.Round;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-17
 */
public interface RoundMapper extends BaseMapper<Round> {

    public List<Round> getCurrentWeekRoundRankList(@Param("start") Date start, @Param("end") Date end);
}
