package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.DLZMatrixError;

/**
 * Class which represents an MatrixUnauthorizedException
 *
 * @author Daniel Schröter
 */
public class MatrixUnauthorizedException extends MatrixException {

    public final String Message = "DLZ: Falscher access Token verwendet";

    public MatrixUnauthorizedException(DLZMatrixError error) {
        super(error);
    }

    @Override
    public String getMessage(){
        return Message;
    }
}
