package site.haihui.challenge.entity;

import java.io.Serializable;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author linghaihui
 * @since 2022-09-17
 */
@Data
public class Round implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer uid;

    private Integer totalQuestion;

    private Integer correctQuestion;

    private Integer score;

    private Integer isOver;

    private Integer timeout;

    private Date createTime;

    private Date updateTime;
}
