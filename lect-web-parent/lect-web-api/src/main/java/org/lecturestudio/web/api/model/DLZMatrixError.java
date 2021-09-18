package org.lecturestudio.web.api.model;

import java.util.StringJoiner;

/**
 * Class which represents an Matrix Error which is received after a faulty request
 *
 * @author Daniel Schröter
 */
public class DLZMatrixError {
    private String errcode;
    private String error;

    /**
     * Getter-method which returns the errorcode
     * @return the specific errorcode
     */
    public String getErrorCode() {
        return errcode;
    }

    /**
     * Getter-method which returns the errormessage
     * @return the specific errormessage
     */
    public String getErrorMessage() {
        return error;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DLZMatrixError.class.getSimpleName() + "[",
                "]")
                .add("errcode='" + errcode + "'")
                .add("error='" + error + "'")
                .toString();
    }
}
