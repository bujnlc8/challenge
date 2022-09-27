package site.haihui.challenge.common.exception;

import lombok.Getter;
import lombok.Setter;
import site.haihui.challenge.common.constant.Config.ResponseStatus;

@Setter
@Getter
public class DataBaseException extends BaseException {

    public DataBaseException() {
        super(ResponseStatus.DataBaseErr.getResponseCode(), ResponseStatus.DataBaseErr.getDescription());
    }
}
