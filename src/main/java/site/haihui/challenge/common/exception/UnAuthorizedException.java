package site.haihui.challenge.common.exception;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class UnAuthorizedException extends BaseException {

    public UnAuthorizedException(String msg) {
        super(401, msg);
    }
}
