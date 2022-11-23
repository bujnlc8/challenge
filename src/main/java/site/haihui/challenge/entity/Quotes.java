package site.haihui.challenge.entity;

import java.io.Serializable;

import lombok.Data;

/**
 * <p>
 * 
 * </p>
 *
 * @author linghaihui
 * @since 2022-11-23
 */
@Data
public class Quotes implements Serializable {

    private static final long serialVersionUID = 1L;

    private String quote;

    private String author;
}
