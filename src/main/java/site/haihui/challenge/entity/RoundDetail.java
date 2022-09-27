package site.haihui.challenge.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-17
 */
@TableName("round_detail")
@Data
public class RoundDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer roundId;

    private Integer uid;

    private Integer questionId;

    private Integer costSecond;

    private Integer score;

    private Integer answer;

    private Date createTime;

    private Date updateTime;
}
