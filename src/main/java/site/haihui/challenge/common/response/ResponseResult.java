package site.haihui.challenge.common.response;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;
import site.haihui.challenge.common.constant.Config.ResponseStatus;

@Data
@Builder
public class ResponseResult<T> implements Serializable {

    /**
     * response code
     */
    private int code;

    /**
     * response message
     */
    private String message;

    /**
     * response data
     */
    private T data;

    public static <T> ResponseResult<T> success(T data) {
        return ResponseResult.<T>builder().data(data).message(ResponseStatus.SUCCESS.getDescription())
                .code(ResponseStatus.SUCCESS.getResponseCode()).build();
    }

    public static <T> ResponseResult<T> noData(T data) {
        return ResponseResult.<T>builder().data(data).message(ResponseStatus.NODATA.getDescription())
                .code(ResponseStatus.NODATA.getResponseCode()).build();
    }

    public static <T> ResponseResult<T> fail(String message) {
        return ResponseResult.<T>builder().data(null).message(message).code(ResponseStatus.FAIL.getResponseCode())
                .build();
    }

    public static <T> ResponseResult<T> makeResponse(T data, int code, String message) {
        return ResponseResult.<T>builder().data(data).message(message).code(code).build();
    }
}
