/**
* 全局异常处理
*/
package site.haihui.challenge.common.exception;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import site.haihui.challenge.common.constant.Config;
import site.haihui.challenge.common.response.ResponseResult;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     * 
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = BaseException.class)
    @ResponseBody
    public ResponseResult<Object> baseExceptionHandler(HttpServletRequest req, BaseException e) {
        logger.error("Error occur: {}", e.getMsg(), e);
        return ResponseResult.makeResponse(null, e.getCode(), e.getMsg());
    }

    /**
     * 参数异常等，http status code 返回400
     * 
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<ResponseResult<Object>> badRequestException(HttpServletRequest req,
            BadRequestException e) {
        HttpStatus httpStatus = HttpStatus.resolve(400);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (e instanceof ParamErrException) {
            return new ResponseEntity<ResponseResult<Object>>(ResponseResult.makeResponse(null,
                    Config.ResponseStatus.ParamErr.getResponseCode(), e.getMsg()),
                    headers, httpStatus);
        }
        return new ResponseEntity<ResponseResult<Object>>(ResponseResult.fail(e.getMsg()), headers, httpStatus);
    }

    /**
     * 未登录异常，http status code 返回401
     *
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = UnAuthorizedException.class)
    public ResponseEntity<ResponseResult<Object>> unauthorizedException(HttpServletRequest req,
            UnAuthorizedException e) {
        HttpStatus httpStatus = HttpStatus.resolve(401);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        Config.ResponseStatus notLogin = Config.ResponseStatus.UerNotLogin;
        return new ResponseEntity<ResponseResult<Object>>(
                ResponseResult.makeResponse(null, notLogin.getResponseCode(), notLogin.getDescription()), headers,
                httpStatus);
    }

    /**
     * 兜底异常处理
     * 
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public ResponseResult<Object> exceptionHandler(HttpServletRequest req, Exception e) {
        logger.error("Unknown error occur:", e);
        return ResponseResult.fail("服务器开小差~");
    }

    /**
     * 参数校验异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseResult<Object> handleBindGetException(MethodArgumentNotValidException e) {
        List<String> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.toList());
        return ResponseResult.fail("参数异常: " + String.join(",", errors));
    }

    /**
     * 缺少参数异常
     * 
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ResponseResult<Object> missingParamException(HttpServletRequest req,
            MissingServletRequestParameterException e) {
        return ResponseResult.makeResponse(null,
                Config.ResponseStatus.ParamErr.getResponseCode(), Config.ResponseStatus.ParamErr.getDescription());
    }

    /**
     * 请求方法错误
     * 
     * @param req
     * @param e
     * @return
     */
    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ResponseBody
    public ResponseResult<Object> methodNotAllowException(HttpServletRequest req,
            HttpRequestMethodNotSupportedException e) {
        return ResponseResult.fail("请求方法错误");
    }
}
