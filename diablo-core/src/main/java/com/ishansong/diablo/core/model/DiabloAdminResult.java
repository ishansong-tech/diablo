package com.ishansong.diablo.core.model;

import com.ishansong.diablo.core.exception.CommonErrorCode;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Data
public class DiabloAdminResult implements Serializable {

    private static final long serialVersionUID = -2792556188993845048L;

    private Integer code;

    private String message;

    private Object data;


    public DiabloAdminResult() {

    }

    public DiabloAdminResult(final Integer code, final String message, final Object data) {

        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static DiabloAdminResult success() {
        return success("");
    }

    public static DiabloAdminResult success(final String msg) {
        return success(msg, null);
    }

    public static DiabloAdminResult success(final Object data) {
        return success(null, data);
    }

    public static DiabloAdminResult success(final String msg, final Object data) {
        return get(CommonErrorCode.SUCCESSFUL, msg, data);
    }

    public static DiabloAdminResult error(final String msg) {
        return error(CommonErrorCode.ERROR, msg);
    }

    public static DiabloAdminResult error(final String msg, String data) {
        return get(CommonErrorCode.ERROR, msg, data);
    }

    public static DiabloAdminResult error(final int code, final String msg) {
        return get(code, msg, null);
    }

    public static DiabloAdminResult timeout(final String msg) {
        return error(HttpStatus.REQUEST_TIMEOUT.value(), msg);
    }

    private static DiabloAdminResult get(final int code, final String msg, final Object data) {
        return new DiabloAdminResult(code, msg, data);
    }

}
