package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.DLZMatrixError;

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
