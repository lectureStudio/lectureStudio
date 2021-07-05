package org.lecturestudio.web.api.filter;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.lecturestudio.web.api.exception.*;
import org.lecturestudio.web.api.model.MatrixError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
public class MatrixExceptionFilter implements ResponseExceptionMapper<MatrixException> {
    @Override
    public MatrixException toThrowable(Response response) {
        int status = response.getStatus();
        MatrixError error = response.readEntity(MatrixError.class);

        switch (status) {
            case 415:
                //Unsupported media type
                return new MatrixTypeException(error);
            case 404:
                //Not found
                return new MatrixElementNotFoundException(error);
            case 403:
                //Client does not fulfill requested Permissions
                return new MatrixPermissionException(error);
            case 401:
                //False access token
                return new MatrixUnauthorizedException(error);
            case 429:
                //Too many requests
                return new MatrixRequestException(error);
            default:
                return new MatrixException(error);
        }
    }
}

