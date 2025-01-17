/*
 * ====================================================================
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.hc.client5.testing.sync;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.ssl.DefaultClientTlsStrategy;
import org.apache.hc.client5.http.ssl.HostnameVerificationPolicy;
import org.apache.hc.client5.http.ssl.HttpsSupport;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.TlsSocketStrategy;
import org.apache.hc.client5.testing.SSLTestContexts;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.io.CloseMode;
import org.apache.hc.core5.ssl.SSLContexts;
import org.apache.hc.core5.ssl.TrustStrategy;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DefaultClientTlsStrategy}.
 */
public class TestDefaultClientTlsStrategy {

    private HttpServer server;

    @AfterEach
    public void shutDown() throws Exception {
        if (this.server != null) {
            this.server.close(CloseMode.GRACEFUL);
        }
    }

    static class TestX509HostnameVerifier implements HostnameVerifier {

        private boolean fired;

        @Override
        public boolean verify(final String host, final SSLSession session) {
            this.fired = true;
            return true;
        }

        public boolean isFired() {
            return this.fired;
        }

    }

    @Test
    public void testBasicSSL() throws Exception {
        // @formatter:off
        this.server = ServerBootstrap.bootstrap()
                .setSslContext(SSLTestContexts.createServerSSLContext())
                .create();
        // @formatter:on
        this.server.start();

        final HttpClientContext context = new HttpClientContext();
        final TestX509HostnameVerifier hostVerifier = new TestX509HostnameVerifier();
        final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(
                SSLTestContexts.createClientSSLContext(), hostVerifier);
        final HttpHost target = new HttpHost("https", "localhost", server.getLocalPort());
        try (final Socket socket = new Socket(target.getHostName(), target.getPort())) {
            try (final SSLSocket sslSocket = tlsStrategy.upgrade(
                    socket,
                    target.getHostName(),
                    target.getPort(),
                    null,
                    context)) {
                final SSLSession sslsession = sslSocket.getSession();

                Assertions.assertNotNull(sslsession);
                Assertions.assertTrue(hostVerifier.isFired());
            }
        }
    }

    @Test
    public void testBasicDefaultHostnameVerifier() throws Exception {
        // @formatter:off
        this.server = ServerBootstrap.bootstrap()
                .setSslContext(SSLTestContexts.createServerSSLContext())
                .create();
        // @formatter:on
        this.server.start();

        final HttpClientContext context = new HttpClientContext();
        final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(SSLTestContexts.createClientSSLContext());
        final HttpHost target = new HttpHost("https", "localhost", server.getLocalPort());
        try (final Socket socket = new Socket(target.getHostName(), target.getPort())) {
            try (final SSLSocket sslSocket = tlsStrategy.upgrade(
                    socket,
                    target.getHostName(),
                    target.getPort(),
                    null,
                    context)) {
                final SSLSession sslsession = sslSocket.getSession();

                Assertions.assertNotNull(sslsession);
            }
        }
    }

    @Test
    public void testClientAuthSSL() throws Exception {
        // @formatter:off
        this.server = ServerBootstrap.bootstrap()
                .setSslContext(SSLTestContexts.createServerSSLContext())
                .create();
        // @formatter:on
        this.server.start();

        final HttpClientContext context = new HttpClientContext();
        final TestX509HostnameVerifier hostVerifier = new TestX509HostnameVerifier();
        final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(
                SSLTestContexts.createClientSSLContext(), hostVerifier);
        final HttpHost target = new HttpHost("https", "localhost", server.getLocalPort());
        try (final Socket socket = new Socket(target.getHostName(), target.getPort())) {
            try (final SSLSocket sslSocket = tlsStrategy.upgrade(
                    socket,
                    target.getHostName(),
                    target.getPort(),
                    null,
                    context)) {
                final SSLSession sslsession = sslSocket.getSession();

                Assertions.assertNotNull(sslsession);
                Assertions.assertTrue(hostVerifier.isFired());
            }
        }
    }

    @Test
    public void testClientAuthSSLFailure() throws Exception {
        // @formatter:off
        this.server = ServerBootstrap.bootstrap()
                .setSslContext(SSLTestContexts.createServerSSLContext())
                .setSslSetupHandler(sslParameters -> sslParameters.setNeedClientAuth(true))
                .create();
        // @formatter:on
        this.server.start();

        final HttpClientContext context = new HttpClientContext();
        final TestX509HostnameVerifier hostVerifier = new TestX509HostnameVerifier();
        final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(
                SSLTestContexts.createClientSSLContext(), hostVerifier);
        final HttpHost target = new HttpHost("https", "localhost", server.getLocalPort());
        try (final Socket socket = new Socket(target.getHostName(), target.getPort())) {
            Assertions.assertThrows(IOException.class, () -> {
                try (final SSLSocket sslSocket = tlsStrategy.upgrade(
                        socket,
                        target.getHostName(),
                        target.getPort(),
                        null,
                        context)) {
                    final SSLSession sslsession = sslSocket.getSession();

                    Assertions.assertNotNull(sslsession);
                    Assertions.assertTrue(hostVerifier.isFired());
                    sslSocket.getInputStream().read();
                }
            });
        }
    }

    @Test
    public void testSSLTrustVerification() throws Exception {
        // @formatter:off
        this.server = ServerBootstrap.bootstrap()
                .setSslContext(SSLTestContexts.createServerSSLContext())
                .create();
        // @formatter:on
        this.server.start();

        final HttpClientContext context = new HttpClientContext();
        // Use default SSL context
        final SSLContext defaultSslContext = SSLContexts.createDefault();

        final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(defaultSslContext,
                NoopHostnameVerifier.INSTANCE);

        final HttpHost target = new HttpHost("https", "localhost", server.getLocalPort());
        try (final Socket socket = new Socket(target.getHostName(), target.getPort())) {
            Assertions.assertThrows(SSLException.class, () -> {
                try (final SSLSocket sslSocket = tlsStrategy.upgrade(
                        socket, target.getHostName(), target.getPort(), null, context)) {
                    // empty for now
                }
            });
        }
    }

    @Test
    public void testSSLTrustVerificationOverrideWithCustom() throws Exception {
        final TrustStrategy trustStrategy = (chain, authType) -> chain.length == 1;
        testSSLTrustVerificationOverride(trustStrategy);
    }

    private void testSSLTrustVerificationOverride(final TrustStrategy trustStrategy)
            throws Exception, IOException, NoSuchAlgorithmException, KeyManagementException, KeyStoreException {
        // @formatter:off
        this.server = ServerBootstrap.bootstrap()
                .setSslContext(SSLTestContexts.createServerSSLContext())
                .create();
        // @formatter:on
        this.server.start();

        final HttpClientContext context = new HttpClientContext();

        // @formatter:off
        final SSLContext sslContext = SSLContexts.custom()
            .loadTrustMaterial(null, trustStrategy)
            .build();
        // @formatter:on
        final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(sslContext,
                NoopHostnameVerifier.INSTANCE);

        final HttpHost target = new HttpHost("https", "localhost", server.getLocalPort());
        try (final Socket socket = new Socket(target.getHostName(), target.getPort())) {
            try (final SSLSocket sslSocket = tlsStrategy.upgrade(
                    socket,
                    target.getHostName(),
                    target.getPort(),
                    null,
                    context)) {
                // empty for now
            }
        }
    }

    @Test
    public void testSSLDisabledByDefault() throws Exception {
        // @formatter:off
        this.server = ServerBootstrap.bootstrap()
                .setSslContext(SSLTestContexts.createServerSSLContext())
                .setSslSetupHandler(sslParameters -> sslParameters.setProtocols(new String[] {"SSLv3"}))
                .create();
        // @formatter:on
        this.server.start();

        final HttpClientContext context = new HttpClientContext();
        final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(
                SSLTestContexts.createClientSSLContext());
        final HttpHost target = new HttpHost("https", "localhost", server.getLocalPort());
        try (final Socket socket = new Socket(target.getHostName(), target.getPort())) {
            Assertions.assertThrows(IOException.class, () ->
                    tlsStrategy.upgrade(
                            socket,
                            target.getHostName(),
                            target.getPort(),
                            null,
                            context));
        }
    }

    @Test
    public void testWeakCiphersDisabledByDefault() {
        final String[] weakCiphersSuites = {
                "SSL_RSA_WITH_RC4_128_SHA",
                "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_DH_anon_WITH_AES_128_CBC_SHA",
                "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                "SSL_RSA_WITH_NULL_SHA",
                "SSL_RSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_ECDHE_ECDSA_WITH_RC4_128_SHA",
                "TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA",
                "TLS_DH_anon_WITH_AES_256_GCM_SHA384",
                "TLS_ECDH_anon_WITH_AES_256_CBC_SHA",
                "TLS_RSA_WITH_NULL_SHA256",
                "SSL_RSA_EXPORT_WITH_RC4_40_MD5",
                "SSL_DH_anon_EXPORT_WITH_RC4_40_MD5",
                "TLS_KRB5_EXPORT_WITH_RC4_40_SHA",
                "SSL_RSA_EXPORT_WITH_RC2_CBC_40_MD5"
        };
        for (final String cipherSuite : weakCiphersSuites) {
            final Exception exception = Assertions.assertThrows(Exception.class, () ->
                    testWeakCipherDisabledByDefault(cipherSuite));
            assertThat(exception, CoreMatchers.anyOf(
                    CoreMatchers.instanceOf(IOException.class),
                    CoreMatchers.instanceOf(IllegalArgumentException.class)));
        }
    }

    private void testWeakCipherDisabledByDefault(final String cipherSuite) throws Exception {
        // @formatter:off
        this.server = ServerBootstrap.bootstrap()
                .setSslContext(SSLTestContexts.createServerSSLContext())
                .setSslSetupHandler(sslParameters -> sslParameters.setProtocols(new String[] {cipherSuite}))
                .create();
        // @formatter:on
        this.server.start();

        final HttpClientContext context = new HttpClientContext();
        final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(
                SSLTestContexts.createClientSSLContext());
        final HttpHost target = new HttpHost("https", "localhost", server.getLocalPort());
        try (final Socket socket = new Socket(target.getHostName(), target.getPort())) {
            tlsStrategy.upgrade(
                    socket,
                    target.getHostName(),
                    target.getPort(),
                    null,
                    context);
        }
    }

    @Test
    public void testHostnameVerificationClient() throws Exception {
        // @formatter:off
        this.server = ServerBootstrap.bootstrap()
                .setSslContext(SSLTestContexts.createServerSSLContext())
                .create();
        // @formatter:on
        this.server.start();

        final HttpHost target1 = new HttpHost("https", "localhost", server.getLocalPort());

        try (final Socket socket = new Socket(InetAddress.getLocalHost(), server.getLocalPort())) {
            final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(
                    SSLTestContexts.createClientSSLContext(),
                    HostnameVerificationPolicy.CLIENT,
                    HttpsSupport.getDefaultHostnameVerifier());
            final HttpClientContext context = new HttpClientContext();
            final SSLSocket upgradedSocket = tlsStrategy.upgrade(
                    socket,
                    target1.getHostName(),
                    target1.getPort(),
                    null,
                    context);
            final SSLSession session = upgradedSocket.getSession();
            MatcherAssert.assertThat(Objects.toString(session.getPeerPrincipal()), Matchers.startsWith("CN=localhost"));
        }

        final HttpHost target2 = new HttpHost("https", "some-other-host", server.getLocalPort());

        try (final Socket socket = new Socket(InetAddress.getLocalHost(), server.getLocalPort())) {
            final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(
                    SSLTestContexts.createClientSSLContext(),
                    HostnameVerificationPolicy.CLIENT,
                    HttpsSupport.getDefaultHostnameVerifier());
            final HttpClientContext context = new HttpClientContext();
            Assertions.assertThrows(SSLPeerUnverifiedException.class, () ->
                    tlsStrategy.upgrade(
                            socket,
                            target2.getHostName(),
                            target2.getPort(),
                            null,
                            context));
        }

        try (final Socket socket = new Socket(InetAddress.getLocalHost(), server.getLocalPort())) {
            final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(
                    SSLTestContexts.createClientSSLContext(),
                    HostnameVerificationPolicy.CLIENT,
                    NoopHostnameVerifier.INSTANCE);
            final HttpClientContext context = new HttpClientContext();
            final SSLSocket upgradedSocket = tlsStrategy.upgrade(
                    socket,
                    target1.getHostName(),
                    target1.getPort(),
                    null,
                    context);
            final SSLSession session = upgradedSocket.getSession();
            MatcherAssert.assertThat(Objects.toString(session.getPeerPrincipal()), Matchers.startsWith("CN=localhost"));
        }
    }

    @Test
    public void testHostnameVerificationBuiltIn() throws Exception {
        // @formatter:off
        this.server = ServerBootstrap.bootstrap()
                .setSslContext(SSLTestContexts.createServerSSLContext())
                .create();
        // @formatter:on
        this.server.start();

        final HttpHost target1 = new HttpHost("https", "localhost", server.getLocalPort());

        try (final Socket socket = new Socket(InetAddress.getLocalHost(), server.getLocalPort())) {
            final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(
                    SSLTestContexts.createClientSSLContext(),
                    HostnameVerificationPolicy.BUILTIN,
                    NoopHostnameVerifier.INSTANCE);
            final HttpClientContext context = new HttpClientContext();
            final SSLSocket upgradedSocket = tlsStrategy.upgrade(
                    socket,
                    target1.getHostName(),
                    target1.getPort(),
                    null,
                    context);
            final SSLSession session = upgradedSocket.getSession();
            MatcherAssert.assertThat(Objects.toString(session.getPeerPrincipal()), Matchers.startsWith("CN=localhost"));
        }

        final HttpHost target2 = new HttpHost("https", "some-other-host", server.getLocalPort());

        try (final Socket socket = new Socket(InetAddress.getLocalHost(), server.getLocalPort())) {
            final TlsSocketStrategy tlsStrategy = new DefaultClientTlsStrategy(
                    SSLTestContexts.createClientSSLContext(),
                    HostnameVerificationPolicy.BUILTIN,
                    NoopHostnameVerifier.INSTANCE);
            final HttpClientContext context = new HttpClientContext();
            Assertions.assertThrows(SSLHandshakeException.class, () ->
                    tlsStrategy.upgrade(
                            socket,
                            target2.getHostName(),
                            target2.getPort(),
                            null,
                            context));
        }
    }

}
