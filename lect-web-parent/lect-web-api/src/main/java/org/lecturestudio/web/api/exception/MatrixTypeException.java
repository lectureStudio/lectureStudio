package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.DLZMatrixError;

/**
 * @author Daniel Schröter
 * Class which represents an MatrixTypeException
 */
public class MatrixTypeException extends MatrixException{
    public final String Message = "DLZ: Format wird nicht unterstützt";

    public MatrixTypeException(DLZMatrixError error) {
        super(error);
    }

    @Override
    public String getMessage(){
        return Message;
    }
}
