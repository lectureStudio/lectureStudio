package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.MatrixError;

public class MatrixElementNotFoundException extends MatrixException{
    public final String Message = "DLZ: Element nicht gefunden";

    public MatrixElementNotFoundException(MatrixError error) {
        super(error);
    }

    @Override
    public String getMessage(){
        return Message;
    }
}
