package com.fz.starter.web;


import com.fz.starter.core.exception.ExceptionVerb.BizException;
import com.fz.starter.pojo.R;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static cn.hutool.core.exceptions.ExceptionUtil.getRootCauseMessage;
import static cn.hutool.core.util.ObjectUtil.defaultIfBlank;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.joining;

/**
 * controller advice
 * handle all mvc exception
 *
 * @author fengbinbin
 * @version 1.0
 * @since 3/23/2022 10:10 AM
 */
@Slf4j
@RestControllerAdvice
@ConditionalOnWebApplication
public class WebAdvisor {

    /**
     * business exception
     */
    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(BizException.class)
    public Object handleBizException(BizException exception) {
        String bizExceptionMessage = getRootCauseMessage(exception);
        log.error("business exception occurred: {}", defaultIfBlank(bizExceptionMessage, "business exception"));
        return R.fail(exception.getVerb().getBisCode(), bizExceptionMessage);
    }

    /**
     * argument bind exception
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BindException.class)
    public Object handleBindException(BindException exception) {
        String exceptionMessage = exception.getBindingResult().getAllErrors().stream().map(ObjectError::getDefaultMessage).collect(joining(";"));
        log.error("bind exception occurred: {}", defaultIfBlank(getRootCauseMessage(exception), "request argument bind exception"));
        return badRequest(exceptionMessage);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageConversionException.class)
    public Object convertException(HttpMessageConversionException exception) {
        String convertExceptionMessage = getRootCauseMessage(exception);
        log.error("http message convert exception occurred: {}", defaultIfBlank(convertExceptionMessage, "http message convert exception"));
        return badRequest(convertExceptionMessage);
    }

    /**
     * {@link RequestParam } MissingServletRequestParameterException
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Object handleMissingServletRequestParameterException(MissingServletRequestParameterException exception) {
        log.error("missing servlet request parameter exception occurred: {}", defaultIfBlank(getRootCauseMessage(exception), "missing servlet request parameter exception"));
        return badRequest(exception.getMessage());
    }

    /**
     * method arguments validate exception ConstraintViolationException
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public Object handleConstraintViolationException(ConstraintViolationException exception) {
        String exceptionMessage = exception.getConstraintViolations().stream().map(ConstraintViolation::getMessage).collect(joining(";"));
        log.error("constraint violation exception occurred: {}", defaultIfBlank(getRootCauseMessage(exception), "constraint violation exception"));
        return badRequest(exceptionMessage);
    }

    /**
     * handle jsr303 exception MethodArgumentNotValidException
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Object handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        FieldError fieldError = exception.getBindingResult().getFieldError();
        String     message    = isNull(fieldError) ? erroredMethodMessage(exception) : fieldError.getDefaultMessage();
        log.error("argument not valid exception occurred: {}", defaultIfBlank(getRootCauseMessage(exception), "handle method argument not valid exception"));
        return badRequest(message);
    }

    /**
     * HttpRequestMethodNotSupportedException
     */
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public Object handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        log.error("http request method not supported exception occurred: {}", defaultIfBlank(getRootCauseMessage(exception), "handle http request method not supported exception"));
        return badRequest(exception.getMessage());
    }

    /**
     * RuntimeException
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(RuntimeException.class)
    public Object handleRuntimeException(RuntimeException exception) {
        log.error("runtime exception occurred: ", exception);
        return R.fail(defaultIfBlank(getRootCauseMessage(exception), "unknown system error..."));
    }

    /**
     * Exception
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Object handleException(Exception exception) {
        log.error("exception occurred: ", exception);
        return R.fail(defaultIfBlank(getRootCauseMessage(exception), "unknown system error..."));
    }

    //************************************************ private start *************************************************//

    private static <T> R<T> badRequest(String message) {
        return R.fail(String.valueOf(HttpStatus.BAD_REQUEST.value()), message, null);
    }

    private String erroredMethodMessage(MethodArgumentNotValidException exception) {
        return "method [" + exception.getParameter().getMethod() + "] argument not-validate error";
    }
}
