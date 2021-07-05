package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.MatrixError;

public class MatrixTypeException extends MatrixException{
    public final String Message = "DLZ: Format wird nicht unterstützt";

    public MatrixTypeException(MatrixError error) {
        super(error);
    }

    @Override
    public String getMessage(){
        return Message;
    }
}
