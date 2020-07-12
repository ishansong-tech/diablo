package com.ishansong.diablo.admin.exception;

import com.ishansong.diablo.core.exception.DiabloException;
import com.ishansong.diablo.core.model.DiabloAdminResult;
import com.ishansong.diablo.core.utils.GsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.List;
import java.util.Set;

@ControllerAdvice
public class ExceptionHandlers extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionHandlers.class);

    @ExceptionHandler(value = {ConstraintViolationException.class})
    public ResponseEntity<String> handle(ConstraintViolationException e) {
        Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
        StringBuilder strBuilder = new StringBuilder();
        for (ConstraintViolation<?> violation : violations) {
            strBuilder.append(violation.getMessage()).append(",");
        }
        strBuilder.setLength(strBuilder.length() - 1);

        LOGGER.warn("ExceptionHandlers handle ConstraintViolationException", e);
        return new ResponseEntity<>(GsonUtils.getInstance().toJson(DiabloAdminResult.error(strBuilder.toString())), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status,
                                                         WebRequest request) {

        LOGGER.warn("ExceptionHandlers handleBindException BindException", ex);

        return new ResponseEntity<>(GsonUtils.getInstance().toJson(DiabloAdminResult.error(buildMessages(ex.getBindingResult()))),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {

        LOGGER.warn("ExceptionHandlers handleMethodArgumentNotValid MethodArgumentNotValidException", ex);
        return new ResponseEntity<>(GsonUtils.getInstance().toJson(DiabloAdminResult.error(buildMessages(ex.getBindingResult()))),
                HttpStatus.BAD_REQUEST);
    }

    @Override
    public ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex,
                                                                       HttpHeaders headers, HttpStatus status, WebRequest request) {

        LOGGER.warn("ExceptionHandlers handleMissingServletRequestParameter MissingServletRequestParameterException", ex);
        return new ResponseEntity<>(GsonUtils.getInstance().toJson(DiabloAdminResult.error(ex.getMessage())), HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers,
                                                        HttpStatus status, WebRequest request) {

        LOGGER.warn("ExceptionHandlers handleTypeMismatch TypeMismatchException", ex);
        return new ResponseEntity<>(GsonUtils.getInstance().toJson(DiabloAdminResult.error(ex.getMessage())), HttpStatus.BAD_REQUEST);
    }

    private String buildMessages(BindingResult result) {
        StringBuilder resultBuilder = new StringBuilder();

        List<ObjectError> errors = result.getAllErrors();
        if (errors != null && errors.size() > 0) {
            for (ObjectError error : errors) {
                if (error instanceof FieldError) {
                    FieldError fieldError = (FieldError) error;
                    String fieldName = fieldError.getField();
                    String fieldErrMsg = fieldError.getDefaultMessage();
                    resultBuilder.append(fieldErrMsg).append(";");
                }
            }
        }
        return resultBuilder.toString();
    }

    @ExceptionHandler({DiabloException.class, Exception.class})
    protected ResponseEntity<Object> serverExceptionHandler(final Exception exception) {

        String message;
        if (exception instanceof DiabloException) {
            DiabloException soulException = (DiabloException) exception;
            message = soulException.getMessage();
            LOGGER.warn("ExceptionHandlers serverExceptionHandler warning", exception);
        } else {
            message = exception.getMessage();

            LOGGER.error("ExceptionHandlers serverExceptionHandler error", exception);
        }
        return new ResponseEntity<>(DiabloAdminResult.error(message), HttpStatus.BAD_REQUEST);
    }
}
