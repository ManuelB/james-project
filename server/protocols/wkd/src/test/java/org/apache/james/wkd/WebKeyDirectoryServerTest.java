/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/
package org.apache.james.wkd;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.james.filesystem.api.FileSystem;
import org.apache.james.server.core.configuration.Configuration;
import org.apache.james.wkd.WebKeyDirectoryConfiguration;
import org.apache.james.wkd.WebKeyDirectoryRoutes;
import org.apache.james.wkd.WebKeyDirectoryServer;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableSet;

import io.restassured.RestAssured;

class WebKeyDirectoryServerTest {
    private static final WebKeyDirectoryConfiguration DISABLED_CONFIGURATION = WebKeyDirectoryConfiguration
        .builder().disable().build();
    private static final WebKeyDirectoryConfiguration TEST_CONFIGURATION = WebKeyDirectoryConfiguration
        .builder().enable().randomPort().keyStorePassword("changeit").build();
    private static final ImmutableSet<WebKeyDirectoryRoutes> NO_ROUTES = ImmutableSet.of();

    @Test
    void serverShouldAnswerWhenStarted() throws FileNotFoundException {
        File keyStoreFile = new File("src/test/resources/wkd-keystore.jks");
        FileSystem fileSystem = mock(FileSystem.class);
        when(fileSystem.getFile(any(String.class))).thenReturn(keyStoreFile);
        WebKeyDirectoryServer webKeyDirectoryServer = new WebKeyDirectoryServer(TEST_CONFIGURATION,
            NO_ROUTES, fileSystem, Configuration.builder().workingDirectory(".").build());
        webKeyDirectoryServer.start();

        try {
            RestAssured.useRelaxedHTTPSValidation();
            RestAssured.config().getSSLConfig().with().keyStore("classpath:wkd-keystore.jks",
                "changeit");

            given().trustStore(keyStoreFile, "changeit").when()
                .get("https://localhost:" + webKeyDirectoryServer.getPort().getValue() + "/").then()
                .statusCode(404);
        } finally {
            webKeyDirectoryServer.stop();
        }
    }

    @Test
    void startShouldNotThrowWhenConfigurationDisabled() {
        WebKeyDirectoryServer WebKeyDirectoryServer = new WebKeyDirectoryServer(
            DISABLED_CONFIGURATION, NO_ROUTES, null, null);

        assertThatCode(WebKeyDirectoryServer::start).doesNotThrowAnyException();
    }

    @Test
    void stopShouldNotThrowWhenConfigurationDisabled() {
        WebKeyDirectoryServer WebKeyDirectoryServer = new WebKeyDirectoryServer(
            DISABLED_CONFIGURATION, NO_ROUTES, null, null);
        WebKeyDirectoryServer.start();

        assertThatCode(WebKeyDirectoryServer::stop).doesNotThrowAnyException();
    }

    @Test
    void getPortShouldThrowWhenServerIsNotStarted() {
        WebKeyDirectoryServer WebKeyDirectoryServer = new WebKeyDirectoryServer(TEST_CONFIGURATION,
            NO_ROUTES, null, null);

        assertThatThrownBy(WebKeyDirectoryServer::getPort)
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void getPortShouldThrowWhenDisabledConfiguration() {
        WebKeyDirectoryServer WebKeyDirectoryServer = new WebKeyDirectoryServer(
            DISABLED_CONFIGURATION, NO_ROUTES, null, null);
        WebKeyDirectoryServer.start();

        assertThatThrownBy(WebKeyDirectoryServer::getPort)
            .isInstanceOf(IllegalStateException.class);
    }
}
