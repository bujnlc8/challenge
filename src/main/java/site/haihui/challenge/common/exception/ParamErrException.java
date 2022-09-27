package site.haihui.challenge.common.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParamErrException extends BadRequestException {

    public ParamErrException(String msg) {
        super(msg);
    }
}
