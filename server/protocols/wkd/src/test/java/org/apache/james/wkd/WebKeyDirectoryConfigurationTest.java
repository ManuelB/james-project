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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Java6Assertions.assertThat;

import java.util.Optional;

import org.apache.james.util.Port;
import org.junit.jupiter.api.Test;

class WebKeyDirectoryConfigurationTest {

    public static final boolean ENABLED = true;
    public static final boolean DISABLED = false;

    @Test
    void buildShouldThrowWhenEnableIsMissing() {
        assertThatThrownBy(() -> WebKeyDirectoryConfiguration.builder().build())
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("You should specify if WebKeyDirectory server should be started");
    }

    @Test
    void buildShouldWorkWhenRandomPort() {
        WebKeyDirectoryConfiguration expectedWebKeyDirectoryConfiguration = new WebKeyDirectoryConfiguration(
            ENABLED, Optional.empty(), "wkd-keystore.jks", "changeit");
        WebKeyDirectoryConfiguration webKeyDirectoryConfiguration = WebKeyDirectoryConfiguration
            .builder().enable().randomPort().build();
        assertThat(webKeyDirectoryConfiguration)
            .isEqualToComparingFieldByField(expectedWebKeyDirectoryConfiguration);
    }

    @Test
    void buildShouldWorkWhenFixedPort() {
        WebKeyDirectoryConfiguration expectedWebKeyDirectoryConfiguration = new WebKeyDirectoryConfiguration(
            ENABLED, Optional.of(Port.of(80)), "wkd-keystore.jks", "changeit");

        WebKeyDirectoryConfiguration webKeyDirectoryConfiguration = WebKeyDirectoryConfiguration
            .builder().enable().port(Port.of(80)).build();

        assertThat(webKeyDirectoryConfiguration)
            .isEqualToComparingFieldByField(expectedWebKeyDirectoryConfiguration);
    }

    @Test
    void buildShouldWorkWhenDisabled() {
        WebKeyDirectoryConfiguration expectedWebKeyDirectoryConfiguration = new WebKeyDirectoryConfiguration(
            DISABLED, Optional.empty(), "wkd-keystore.jks", "changeit");

        WebKeyDirectoryConfiguration webKeyDirectoryConfiguration = WebKeyDirectoryConfiguration
            .builder().disable().build();
        assertThat(webKeyDirectoryConfiguration)
            .isEqualToComparingFieldByField(expectedWebKeyDirectoryConfiguration);
    }
}