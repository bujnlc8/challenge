package site.haihui.challenge.common.exception;

import site.haihui.challenge.common.constant.Config.ResponseStatus;

public class CommonException extends BaseException {

    public CommonException(String msg) {
        super(ResponseStatus.FAIL.getResponseCode(), msg);
    }
}
