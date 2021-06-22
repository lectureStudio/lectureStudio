package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.MatrixError;

public class MatrixException extends Exception{
    private final MatrixError error;


    public MatrixException(MatrixError error) {
        this.error = error;
    }

    @Override
    public String getMessage(){
        return "DLZ-Fehler";
    }

    public MatrixError getMatrixError() {
        return error;
    }
}
