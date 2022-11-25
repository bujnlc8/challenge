package site.haihui.challenge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import org.apache.ibatis.annotations.Param;

import site.haihui.challenge.entity.Quotes;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author linghaihui
 * @since 2022-11-23
 */
public interface QuotesMapper extends BaseMapper<Quotes> {

    public Quotes getRandomOne(@Param(value = "quote") String quote, @Param(value = "author") String author);

}
