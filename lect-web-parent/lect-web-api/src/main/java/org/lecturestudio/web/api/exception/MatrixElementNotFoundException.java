package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.DLZMatrixError;


/**
 * @author Daniel Schröter
 * Class which represents an MatrixElementNotFoundException
 */
public class MatrixElementNotFoundException extends MatrixException{
    public final String Message = "DLZ: Element nicht gefunden";

    public MatrixElementNotFoundException(DLZMatrixError error) {
        super(error);
    }

    @Override
    public String getMessage(){
        return Message;
    }
}
