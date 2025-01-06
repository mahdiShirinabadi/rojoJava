package com.melli.hub.exception;

import com.melli.hub.domain.response.base.BaseResponse;
import com.melli.hub.domain.response.base.ErrorDetail;
import com.melli.hub.service.StatusService;
import com.melli.hub.util.StringUtils;
import com.melli.hub.utils.Helper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.auth.AuthenticationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Log4j2
public class AppExceptionsHandler extends ResponseEntityExceptionHandler {

    private final Helper responseHelper;
    private final StatusService statusService;


    @ExceptionHandler(value = {Exception.class})
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> handleAnyException(Exception exception, WebRequest request) {
        ExceptionUtils.getStackTrace(exception);
        log.error("exception in request ==> {} with message {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getStackTrace());
        log.error("error in request" + exception.getMessage());
        try {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(StatusService.GENERAL_ERROR);
            errorDetail.setMessage(statusService.findByCode(String.valueOf(StatusService.GENERAL_ERROR)).getPersianDescription());
            return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(false, errorDetail));
        } catch (Exception ex) {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(StatusService.GENERAL_ERROR);
            errorDetail.setMessage("Exception: error read description");
            return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(false, errorDetail));
        }
    }

    @ExceptionHandler(value = {AuthenticationException.class})
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> handleAuthenticationException(AuthenticationException exception, WebRequest request) {
        log.error("AuthenticationException in request ==> {} , message ===> {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getMessage());
        try {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(StatusService.TOKEN_NOT_VALID);
            errorDetail.setMessage(statusService.findByCode(String.valueOf(StatusService.TOKEN_NOT_VALID)).getPersianDescription());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new BaseResponse<>(false, errorDetail));
        } catch (Exception ex) {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(StatusService.TOKEN_NOT_VALID);
            errorDetail.setMessage("AuthenticationException: error read description");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new BaseResponse<>(false, errorDetail));
        }

    }

    @ExceptionHandler(value = {AccessDeniedException.class})
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> handleAccessDeniedException(AccessDeniedException exception, WebRequest request) {
        log.error("AccessDeniedException in request ==> {} message ===> {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getMessage());
        try {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(StatusService.USER_NOT_PERMISSION);
            errorDetail.setMessage(statusService.findByCode(String.valueOf(StatusService.USER_NOT_PERMISSION)).getPersianDescription());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new BaseResponse<>(false, errorDetail));
        } catch (Exception ex) {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(StatusService.USER_NOT_PERMISSION);
            errorDetail.setMessage("AccessDeniedException: error read description");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new BaseResponse<>(false, errorDetail));
        }
    }

    @ExceptionHandler(value = {InternalServiceException.class})
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> handleServiceException(InternalServiceException exception, WebRequest request) {
        log.error("service exception in request ==> {} , message ===> {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getMessage());
        try {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(exception.getStatus());
            String message = statusService.findByCode(String.valueOf(exception.getStatus())).getPersianDescription();
            if (StringUtils.isPersianString(exception.getMessage())) {
                message = StringUtils.fixSomeWord(exception.getMessage());
            }
            if (exception.getParameters() != null) {
                message = StringSubstitutor.replace(message, exception.getParameters(), "${", "}");
            }
            errorDetail.setMessage(message);
            return ResponseEntity.status(exception.getHttpStatus()).body(new BaseResponse<>(false, errorDetail));
        } catch (Exception ex) {
            ErrorDetail errorDetail = new ErrorDetail();
            errorDetail.setCode(exception.getStatus());
            errorDetail.setMessage("InternalServiceException: error read description");
            return ResponseEntity.status(exception.getHttpStatus()).body(new BaseResponse<>(false, errorDetail));
        }
    }

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<BaseResponse<ObjectUtils.Null>> handleConstraintViolationExceptions(ConstraintViolationException exception, WebRequest request) {
        log.error("exception in request ==> {} , message ===> {}", ((ServletWebRequest) request).getRequest().getRequestURI(), exception.getMessage());
        ConstraintViolation<?> violation = exception.getConstraintViolations().iterator().next();
        ErrorDetail errorDetail = new ErrorDetail();
        errorDetail.setCode(StatusService.GENERAL_ERROR);
        errorDetail.setMessage(violation.getMessage());
        return ResponseEntity.status(HttpStatus.OK).body(new BaseResponse<>(false, errorDetail));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            log.error("error in parameter ({}) , and error is ({}) and input value is ({})", fieldName, errorMessage, ((FieldError) error).getRejectedValue());
            errors.put(fieldName, errorMessage);
        });
        ErrorDetail errorDetail = new ErrorDetail(ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage(), StatusService.GENERAL_ERROR);
        return new ResponseEntity<>(responseHelper.fillBaseResponse(false, new Date(), errorDetail), HttpStatus.OK);

    }


}
