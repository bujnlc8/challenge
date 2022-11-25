package site.haihui.challenge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import site.haihui.challenge.entity.Quotes;
import site.haihui.challenge.mapper.QuotesMapper;
import site.haihui.challenge.service.IQuotesService;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author linghaihui
 * @since 2022-11-23
 */
@Service
public class QuotesServiceImpl extends ServiceImpl<QuotesMapper, Quotes> implements IQuotesService {

    @Autowired
    private QuotesMapper quotesMapper;

	@Override
	public Quotes getRandomOne(String quote, String author) {
		return quotesMapper.getRandomOne(quote, author);
	}

}
