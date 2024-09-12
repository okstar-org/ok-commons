/*
 * * Copyright (c) 2022 船山信息 chuanshaninfo.com
 * OkStack is licensed under Mulan PubL v2.
 * You can use this software according to the terms and conditions of the Mulan
 * PubL v2. You may obtain a copy of Mulan PubL v2 at:
 *          http://license.coscl.org.cn/MulanPubL-2.0
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PubL v2 for more details.
 * /
 */

package org.okstar.platform.common.web.rest.transport;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ContextResolver;
import org.eclipse.persistence.jaxb.JAXBContextProperties;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.moxy.json.MoxyJsonConfig;
import org.glassfish.jersey.moxy.json.MoxyJsonFeature;
import org.glassfish.jersey.moxy.xml.MoxyXmlFeature;
import org.okstar.platform.common.web.auth.AuthenticationMode;
import org.okstar.platform.common.web.auth.AuthenticationToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;

/**
 * The Class RestClient.
 */
public final class RestClient {

    /**
     * The Constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RestClient.class);

    /**
     * The uri.
     */
    private String baseURI;

    /**
     * The token.
     */
    private AuthenticationToken token;

    /**
     * The password.
     */
    private String password;

    /**
     * The connection timeout.
     */
    private int connectionTimeout;

    /**
     * The headers.
     */
    private MultivaluedMap<String, Object> headers;

    /**
     * The media type.
     */
    private SupportedMediaType mediaType;

    /**
     * Gets the.
     *
     * @param <T>              the generic type
     * @param restPath         the rest path
     * @param expectedResponse the expected response
     * @param queryParams      the query params
     * @return the t
     */
    public <T> T get(
            String restPath,
            Class<T> expectedResponse,
            Map<String, String> queryParams) {
        LOG.info("GET: {}", restPath);
        return call(HttpMethod.GET,
                restPath,
                expectedResponse,
                null,
                queryParams);
    }

    public <T> T post(
            String restPath,
            Class<T> expectedResponse,
            Object payload,
            Map<String, String> queryParams
    ) {
        LOG.info("POST: {}", restPath);
        return call(HttpMethod.POST,
                restPath,
                expectedResponse,
                payload,
                queryParams);
    }

    /**
     * Post.
     *
     * @param restPath    the rest path
     * @param payload     the payload
     * @param queryParams the query params
     * @return the response
     */
    public Response post(
            String restPath,
            Object payload,
            Map<String, String> queryParams) {
        LOG.debug("POST: {}", restPath);
        return call(HttpMethod.POST,
                restPath,
                Response.class,
                payload,
                queryParams);
    }

    /**
     * Put.
     *
     * @param restPath    the rest path
     * @param payload     the payload
     * @param queryParams the query params
     * @return the response
     */
    public Response put(
            String restPath,
            Object payload,
            Map<String, String> queryParams) {
        LOG.debug("PUT: {}", restPath);
        return call(HttpMethod.PUT,
                restPath,
                Response.class,
                payload,
                queryParams);
    }

    /**
     * Delete.
     *
     * @param restPath    the rest path
     * @param queryParams the query params
     * @return the response
     */
    public Response delete(
            String restPath,
            Map<String, String> queryParams) {
        LOG.debug("DELETE: {}", restPath);
        return call(HttpMethod.DELETE,
                restPath,
                Response.class,
                null,
                queryParams);
    }

    /**
     * Gets the.
     *
     * @param <T>              the generic type
     * @param methodName       the method name
     * @param restPath         the rest path
     * @param expectedResponse the clazz
     * @param payload          the payload
     * @param queryParams      the query params
     * @return the t
     */
    @SuppressWarnings("unchecked")
    public <T> T call(
            String methodName,
            String restPath,
            Class<T> expectedResponse,
            Object payload,
            Map<String, String> queryParams) {
        try (Client client = createRestClient()) {
            WebTarget webTarget = createWebTarget(client, restPath, queryParams);
            Response result = webTarget
                    .request()
                    .headers(headers)
                    .accept(mediaType.getMediaType())
                    .method(methodName,
                            Entity.entity(payload, mediaType.getMediaType()),
                            Response.class);

            if (expectedResponse.getName().equals(Response.class.getName())) {
                return (T) result;
            }

            if (result != null && isStatusCodeOK(result, restPath)) {
                return (T) result.readEntity(expectedResponse);
            }

            throw new WebApplicationException("Unhandled response", result);

        } catch (RuntimeException | KeyManagementException | NoSuchAlgorithmException | URISyntaxException e) {
            throw new WebApplicationException(e);
        }

    }

    /**
     * Checks if is status code ok.
     *
     * @param response the response
     * @param uri      the uri
     * @return true, if is status code ok
     */
    private boolean isStatusCodeOK(Response response, String uri) {
        if (response.getStatus() == Response.Status.OK.getStatusCode()
                || response.getStatus() == Response.Status.CREATED.getStatusCode()) {
            return true;
        } else if (response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()) {
            throw new NotAuthorizedException("UNAUTHORIZED: Your credentials are wrong. "
                    + "Please check your username/password or the secret key.", response);
        } else if (response.getStatus() == Response.Status.CONFLICT.getStatusCode()
                || response.getStatus() == Response.Status.NOT_FOUND.getStatusCode()
                || response.getStatus() == Response.Status.FORBIDDEN.getStatusCode()
                || response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode()) {
            String errorResponse = response.readEntity(String.class);
            throw new ClientErrorException(errorResponse, response);
        } else if (response.getStatus() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            throw new WebApplicationException("Server Error", response);
        } else {
            throw new WebApplicationException("Unsupported status", response);
        }
    }

    /**
     * Creates the web target.
     *
     * @param restPath    the rest path
     * @param queryParams the query params
     * @return the web target
     */
    private WebTarget createWebTarget(Client client, String restPath, Map<String, String> queryParams) throws URISyntaxException {
        WebTarget webTarget;

        URI u;
        if (!baseURI.endsWith("/") && !restPath.startsWith("/")) {
            u = new URI(this.baseURI + "/" + restPath);
        } else {
            u = new URI(this.baseURI + restPath);
        }
        LOG.info("Call=> {}", u);
        webTarget = client.target(u);
        if (queryParams != null && !queryParams.isEmpty()) {
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (entry.getKey() != null && entry.getValue() != null) {
                    LOG.debug("PARAM: {} = {}", entry.getKey(), entry.getValue());
                    webTarget = webTarget.queryParam(entry.getKey(), entry.getValue());
                }
            }
        }


        return webTarget;
    }

    /**
     * The Constructor.
     *
     * @param builder the builder
     */
    private RestClient(RestClientBuilder builder) {
        this.baseURI = builder.baseURI;
        this.connectionTimeout = builder.connectionTimeout;
        this.setHeaders(builder.headers);
        this.token = builder.token;
        this.mediaType = builder.mediaType;
    }

    /**
     * Creater rest client.
     *
     * @return the client
     * @throws KeyManagementException   the key management exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    private Client createRestClient() throws KeyManagementException, NoSuchAlgorithmException {
        ClientConfig clientConfig = new ClientConfig();
        // Set connection timeout
        if (this.connectionTimeout != 0) {
            clientConfig.property(ClientProperties.CONNECT_TIMEOUT, this.connectionTimeout);
            clientConfig.property(ClientProperties.READ_TIMEOUT, this.connectionTimeout);
        }

        clientConfig
                .register(MoxyJsonFeature.class)
                .register(MoxyXmlFeature.class)
                .register(createMoxyJsonResolver());

        Client client = null;
        if (this.baseURI.startsWith("https")) {
            client = createSLLClient(clientConfig);
        } else {
            client = ClientBuilder.newClient(clientConfig);
        }

        return client;
    }

    public static ContextResolver<MoxyJsonConfig> createMoxyJsonResolver() {
        return new MoxyJsonConfig()
                .setAttributePrefix("")
                .setValueWrapper("value")
                .property(JAXBContextProperties.JSON_WRAPPER_AS_ARRAY_NAME, true)
                .resolver();
    }

    /**
     * Creates the sll client.
     *
     * @param clientConfig the client config
     * @return the client config
     * @throws KeyManagementException   the key management exception
     * @throws NoSuchAlgorithmException the no such algorithm exception
     */
    private Client createSLLClient(ClientConfig clientConfig)
            throws KeyManagementException, NoSuchAlgorithmException {
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
        }};

        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, trustAllCerts, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

        ClientBuilder.newClient(clientConfig);

        Client client = ClientBuilder.newBuilder()
                .sslContext(sc)
                .hostnameVerifier(new HostnameVerifier() {
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                })
                .withConfig(clientConfig).build();

        return client;
    }

    /**
     * The Class Builder.
     */
    public static class RestClientBuilder {

        /**
         * The uri.
         */
        private String baseURI;

        /**
         * The connection timeout.
         */
        private int connectionTimeout;

        /**
         * The headers.
         */
        private MultivaluedMap<String, Object> headers;

        /**
         * The token.
         */
        private AuthenticationToken token;

        /**
         * The media type.
         */
        private SupportedMediaType mediaType;

        /**
         * The Constructor.
         *
         * @param baseUri the base uri
         */
        public RestClientBuilder(String baseUri) {
            this.headers = new MultivaluedHashMap<String, Object>();
            this.baseURI = baseUri;
        }

        /**
         * Connection timeout.
         *
         * @param connectionTimeout the connection timeout
         * @return the builder
         */
        public RestClientBuilder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        /**
         * Authentication token.
         *
         * @param token the token
         * @return the rest client builder
         */
        public RestClientBuilder authenticationToken(AuthenticationToken token) {
            if (token == null) {
                return this;
            }

            this.token = token;

            if (token.getAuthMode() == AuthenticationMode.SHARED_SECRET_KEY) {
                headers.add(HttpHeaders.AUTHORIZATION, token.getSharedSecretKey());
            } else if (token.getAuthMode() == AuthenticationMode.BASIC_AUTH) {
                String base64 = Base64.getEncoder().encodeToString((token.getUsername() + ":" + token.getPassword()).getBytes());
                headers.add(HttpHeaders.AUTHORIZATION, "Basic " + base64);
            }

            if (token.getHeaders() != null)
                headers.putAll(token.getHeaders());

            return this;
        }

        /**
         * Headers.
         *
         * @param headers the headers
         * @return the rest client builder
         */
        public RestClientBuilder headers(MultivaluedMap<String, Object> headers) {
            this.headers = headers;
            return this;
        }

        /**
         * .
         *
         * @param mediaType the mediaType
         * @return the rest client builder
         */
        public RestClientBuilder mediaType(SupportedMediaType mediaType) {
            this.mediaType = mediaType;
            return this;
        }

        /**
         * Builds the.
         *
         * @return the rest client resource
         */
        public RestClient build() {
            return new RestClient(this);
        }

    }

    /**
     * Gets the uri.
     *
     * @return the uri
     */
    public String getUri() {
        return baseURI;
    }

    /**
     * Sets the uri.
     *
     * @param uri the new uri
     */
    public void setUri(String uri) {
        this.baseURI = uri;
    }

    /**
     * Gets the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the new password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the connection timeout.
     *
     * @return the connection timeout
     */
    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    /**
     * Sets the connection timeout.
     *
     * @param connectionTimeout the new connection timeout
     */
    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    /**
     * Gets the token.
     *
     * @return the token
     */
    public AuthenticationToken getToken() {
        return token;
    }

    /**
     * Sets the token.
     *
     * @param token the token
     */
    public void setToken(AuthenticationToken token) {
        this.token = token;
        if (token.getAuthMode() == AuthenticationMode.SHARED_SECRET_KEY) {
            headers.add(HttpHeaders.AUTHORIZATION, "Bearer " + token.getSharedSecretKey());
        }
    }

    /**
     * Gets the headers.
     *
     * @return the headers
     */
    public MultivaluedMap<String, Object> getHeaders() {
        return headers;
    }

    /**
     * Sets the headers.
     *
     * @param headers the headers
     */
    public void setHeaders(MultivaluedMap<String, Object> headers) {
        this.headers = headers;
    }

    /**
     * Gets the mediaType.
     *
     * @return the mediaType
     */
    public SupportedMediaType getMediaType() {
        return mediaType;
    }

    /**
     * Sets the mediaType.
     *
     * @param mediaType the mediaType
     */
    public void setMediaType(SupportedMediaType mediaType) {
        this.mediaType = mediaType;
    }
}
