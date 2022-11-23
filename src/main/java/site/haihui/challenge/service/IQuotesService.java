package site.haihui.challenge.service;

import com.baomidou.mybatisplus.extension.service.IService;

import site.haihui.challenge.entity.Quotes;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author linghaihui
 * @since 2022-11-23
 */
public interface IQuotesService extends IService<Quotes> {

    public Quotes getRandomOne();
}
