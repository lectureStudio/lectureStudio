package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.DLZMatrixError;

/**
 * Class which represents an MatrixRequestException
 *
 * @author Daniel Schröter
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
