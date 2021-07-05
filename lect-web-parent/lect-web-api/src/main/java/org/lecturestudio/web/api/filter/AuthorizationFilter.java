package org.lecturestudio.web.api.filter;

import static java.util.Objects.nonNull;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.ext.Provider;

public class AuthorizationFilter implements ClientRequestFilter {

    private static final String BEARER = "Bearer ";
    private static String token;

    /*public static void setToken(String Token) {
        token = Token;
    }*/

    @Override
    public void filter(ClientRequestContext requestContext) {
        String token = System.getProperty("dlz.token");

        if (nonNull(token)) {
            requestContext.getHeaders().putSingle(AUTHORIZATION, BEARER + token);
        }
    }

}
