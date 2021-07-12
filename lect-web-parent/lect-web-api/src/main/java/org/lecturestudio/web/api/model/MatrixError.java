package org.lecturestudio.web.api.model;

import java.util.StringJoiner;

public class MatrixError {
    private String errcode;
    private String error;

    public String getErrorCode() {
        return errcode;
    }

    public String getErrorMessage() {
        return error;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MatrixError.class.getSimpleName() + "[",
                "]")
                .add("errcode='" + errcode + "'")
                .add("error='" + error + "'")
                .toString();
    }
}