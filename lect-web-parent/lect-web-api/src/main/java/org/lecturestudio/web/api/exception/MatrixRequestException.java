package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.MatrixError;

public class MatrixRequestException extends MatrixException{
    public final String Message = "DLZ: Zu viele Anfragen";

    public MatrixRequestException(MatrixError error) {
        super(error);
    }

    @Override
    public String getMessage(){
        return Message;
    }
}
