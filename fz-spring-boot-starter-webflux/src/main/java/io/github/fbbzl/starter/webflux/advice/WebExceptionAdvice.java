package io.github.fbbzl.starter.webflux.advice;


import io.github.fbbzl.starter.core.exception.BizException;
import io.github.fbbzl.starter.webflux.R;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.MethodNotAllowedException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;

import java.sql.SQLIntegrityConstraintViolationException;

import static cn.hutool.core.exceptions.ExceptionUtil.getRootCauseMessage;
import static cn.hutool.core.util.ObjectUtil.defaultIfBlank;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

/**
 * controller advice
 * handle all webflux exception
 *
 * @author fengbinbin
 * @version 1.0
 * @since 3/23/2022 10:10 AM
 */
@Slf4j
@Order(AdviceOrder.WEB_EXCEPTION)
@RestControllerAdvice
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class WebExceptionAdvice
{

    /**
     * business exception
     */
    @ExceptionHandler(BizException.class)
    public ResponseEntity<R<Void>> handleBizException(BizException exception)
    {
        String bizExceptionMessage = getRootCauseMessage(exception);
        int    httpStatusCode      = exception.getVerb().getHttpStatusCode();
        log.error("business exception occurred: {}", defaultIfBlank(bizExceptionMessage, "business exception"));
        return ResponseEntity.status(httpStatusCode).body(R.fail(String.valueOf(httpStatusCode), bizExceptionMessage, null));
    }

    /**
     * ResponseStatusException
     */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<R<Void>> handleResponseStatusException(ResponseStatusException exception)
    {
        org.springframework.http.HttpStatusCode statusCode = exception.getStatusCode();
        log.warn("response status exception occurred: {}", exception.getReason());
        return ResponseEntity.status(statusCode).body(R.fail(String.valueOf(statusCode.value()), exception.getReason(), null));
    }

    /**
     * argument bind exception
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(WebExchangeBindException.class)
    public Object handleWebExchangeBindException(WebExchangeBindException exception)
    {
        String exceptionMessage = exception.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(joining(";"));
        log.error("web exchange bind exception occurred: {}", defaultIfBlank(getRootCauseMessage(exception), "request argument bind exception"));
        return badRequest(exceptionMessage);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException exception)
    {
        String exceptionMessage = exception.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(joining(";"));
        log.error("bind exception occurred: {}", defaultIfBlank(getRootCauseMessage(exception), "request argument bind exception"));
        return badRequest(exceptionMessage);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ServerWebInputException.class)
    public Object handleServerWebInputException(ServerWebInputException exception)
    {
        String convertExceptionMessage = getRootCauseMessage(exception);
        log.error("server web input exception occurred: {}", defaultIfBlank(convertExceptionMessage, "server web input exception"));
        return badRequest(convertExceptionMessage);
    }

    /**
     * method arguments validate exception ConstraintViolationException
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleConstraintViolationException(ConstraintViolationException exception)
    {
        String exceptionMessage = exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(joining(";"));
        log.error("constraint violation exception occurred: {}", defaultIfBlank(getRootCauseMessage(exception), "constraint violation exception"));
        return badRequest(exceptionMessage);
    }

    /**
     * handle jsr303 exception MethodArgumentNotValidException
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException exception)
    {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String     message    = isNull(fieldError) ? erroredMethodMessage(exception) : fieldError.getDefaultMessage();
        log.error("argument not valid exception occurred: {}", defaultIfBlank(getRootCauseMessage(exception), "handle method argument not valid exception"));
        return badRequest(message);
    }

    /**
     * MethodNotAllowedException
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(MethodNotAllowedException.class)
    public Object handleMethodNotAllowedException(MethodNotAllowedException exception)
    {
        log.error("http request method not supported exception occurred: {}", defaultIfBlank(getRootCauseMessage(exception), "handle http request method not supported exception"));
        return failed(HttpStatus.METHOD_NOT_ALLOWED, exception.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DuplicateKeyException.class)
    public Object handleDuplicateKey(DuplicateKeyException ex) {
        log.warn("duplicate key exception occurred", ex);
        return badRequest("some properties of the data already exist, check the unique fields and try again");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Object handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);

        log.warn("data integrity violation exception occurred", ex);
        if (root instanceof SQLIntegrityConstraintViolationException) {
            return badRequest("the data does not meet uniqueness or integrity constraints, please check and try again");
        }

        return badRequest("data is not saved, please check the contents");
    }

    /**
     * RuntimeException
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException exception)
    {
        log.error("runtime exception occurred: ", exception);
        return R.fail("internal server error");
    }

    /**
     * Exception
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception exception)
    {
        log.error("exception occurred: ", exception);
        return R.fail("internal server error");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(ClassNotFoundException.class)
    public Object classNotFoundException(ClassNotFoundException exception)
    {
        String convertExceptionMessage = getRootCauseMessage(exception);
        log.error("http exception occurred: {}", defaultIfBlank(convertExceptionMessage, "class not found exception"));
        return R.fail("can not read class info, make sure class is exist");
    }

    //************************************************ private start *************************************************//

    private static <DATA> R<DATA> badRequest(String message)
    {
        return failed(HttpStatus.BAD_REQUEST, message);
    }

    private static <DATA> R<DATA> failed(HttpStatus httpStatus, String message)
    {
        return R.fail(String.valueOf(httpStatus.value()), message, null);
    }

    private String erroredMethodMessage(MethodArgumentNotValidException exception)
    {
        return "method [" + exception.getParameter().getMethod() + "] argument not-validate error";
    }
}
