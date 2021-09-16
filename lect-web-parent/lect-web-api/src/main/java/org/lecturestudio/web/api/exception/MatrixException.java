package org.lecturestudio.web.api.exception;

import org.lecturestudio.web.api.model.DLZMatrixError;

public class MatrixException extends RuntimeException{
    private final DLZMatrixError error;


    public MatrixException(DLZMatrixError error) {
        this.error = error;
    }

    @Override
    public String getMessage(){
        return "DLZ-Fehler";
    }

    public DLZMatrixError getMatrixError() {
        return error;
    }
}
