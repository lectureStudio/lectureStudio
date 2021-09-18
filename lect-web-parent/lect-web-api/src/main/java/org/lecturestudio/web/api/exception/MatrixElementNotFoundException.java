package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.DLZMatrixError;


/**
 * Class which represents an MatrixElementNotFoundException
 *
 * @author Daniel Schröter
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
