package site.haihui.challenge.common.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BadRequestException extends BaseException {

    public BadRequestException(String msg) {
        super(400, msg);
    }
}
