package org.lecturestudio.web.api.filter;

import static java.util.Objects.nonNull;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;

/**
 * @author Daniel Schröter, Alex Andres
 * Implements an authorization filter which contains the DLZAccessToken
 */
public class AuthorizationFilter implements ClientRequestFilter {

    private static final String BEARER = "Bearer ";
    private static String token;

    /**
     * Method to set the DLZAccessToken
     * @param Token
     */
    public static void setToken(String Token) {
        token = Token;
    }

    @Override
    public void filter(ClientRequestContext requestContext) {
        if (nonNull(token)) {
            requestContext.getHeaders().putSingle(AUTHORIZATION, BEARER + token);
        }
    }

}
