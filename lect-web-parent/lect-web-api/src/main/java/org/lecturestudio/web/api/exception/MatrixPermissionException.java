package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.DLZMatrixError;

/**
 * Class which represents an MatrixPermissionException
 *
 * @author Daniel Schröter
 */
public class MatrixPermissionException extends MatrixException{
    public final String Message = "DLZ: Nicht ausreichende Serverrechte";

    public MatrixPermissionException(DLZMatrixError error) {
        super(error);
    }

    @Override
    public String getMessage(){
        return Message;
    }
}
