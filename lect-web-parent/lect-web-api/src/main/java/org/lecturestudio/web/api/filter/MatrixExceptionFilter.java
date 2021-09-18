package org.lecturestudio.web.api.filter;

import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;
import org.lecturestudio.web.api.exception.*;
import org.lecturestudio.web.api.model.DLZMatrixError;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Class which determines which Matrix Exception needs to be thrown concerning the given errorcode
 *
 * @author Michel Heidkamp, Daniel Schröter
 */
@Provider
public class MatrixExceptionFilter implements ResponseExceptionMapper<MatrixException> {
    @Override
    public MatrixException toThrowable(Response response) {
        int status = response.getStatus();
        DLZMatrixError error = response.readEntity(DLZMatrixError.class);

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

