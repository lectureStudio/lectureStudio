package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.MatrixError;

public class MatrixPermissionException extends MatrixException{
    public final String Message = "DLZ: Nicht ausreichende Serverrechte";

    public MatrixPermissionException(MatrixError error) {
        super(error);
    }

    @Override
    public String getMessage(){
        return Message;
    }
}
