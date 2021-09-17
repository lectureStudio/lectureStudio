package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.DLZMatrixError;

/**
 * @author Daniel Schröter
 * Class which represents an MatrixRequestException
 */
public class MatrixRequestException extends MatrixException{
    public final String Message = "DLZ: Zu viele Anfragen";

    public MatrixRequestException(DLZMatrixError error) {
        super(error);
    }

    @Override
    public String getMessage(){
        return Message;
    }
}
