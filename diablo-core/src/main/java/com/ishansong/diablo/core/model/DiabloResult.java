package com.ishansong.diablo.core.model;

import com.ishansong.diablo.core.exception.CommonErrorCode;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.io.Serializable;

@Data
public class DiabloResult implements Serializable {

    private static final long serialVersionUID = -2792556188993845048L;

    private Integer status;

    private String err;

    private Object data;

    public DiabloResult() {

    }

    public DiabloResult(final Integer status, final String err, final Object data) {

        this.status = status;
        this.err = err;
        this.data = data;
    }

    public static DiabloResult success() {
        return success("");
    }

    public static DiabloResult success(final Object data) {
        return get(CommonErrorCode.SUCCESSFUL, null, data);
    }

    public static DiabloResult error(final String err) {
        return error(CommonErrorCode.ERROR, err);
    }

    public static DiabloResult error(final String err, String data) {
        return get(CommonErrorCode.ERROR, err, data);
    }

    public static DiabloResult error(final int status, final String err) {
        return get(status, err, null);
    }

    public static DiabloResult timeout(final String err) {
        return error(HttpStatus.REQUEST_TIMEOUT.value(), err);
    }

    private static DiabloResult get(final int status, final String err, final Object data) {
        return new DiabloResult(status, err, data);
    }

}
