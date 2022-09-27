package site.haihui.challenge.service.impl;

import site.haihui.challenge.entity.Feedback;
import site.haihui.challenge.mapper.FeedbackMapper;
import site.haihui.challenge.service.IFeedbackService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-24
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements IFeedbackService {
}
