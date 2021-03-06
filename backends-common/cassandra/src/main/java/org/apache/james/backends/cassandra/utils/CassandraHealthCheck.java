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

package org.apache.james.backends.cassandra.utils;

import javax.inject.Inject;

import org.apache.james.core.healthcheck.ComponentName;
import org.apache.james.core.healthcheck.HealthCheck;
import org.apache.james.core.healthcheck.Result;

import com.datastax.driver.core.Session;

import reactor.core.publisher.Mono;

/**
 * Health check for the Cassandra backend.
 *
 */
public class CassandraHealthCheck implements HealthCheck {

    private static final ComponentName COMPONENT_NAME = new ComponentName("Cassandra backend");
    private static final String SAMPLE_QUERY = "SELECT NOW() FROM system.local";

    private final CassandraAsyncExecutor queryExecutor;

    @Inject
    public CassandraHealthCheck(Session session) {
        this.queryExecutor = new CassandraAsyncExecutor(session);
    }

    @Override
    public ComponentName componentName() {
        return COMPONENT_NAME;
    }

    @Override
    public Mono<Result> checkReactive() {
        // execute a simple query to check if cassandra is responding
        // idea from: https://stackoverflow.com/questions/10246287
        return queryExecutor.execute(SAMPLE_QUERY)
            .map(resultSet -> Result.healthy(COMPONENT_NAME))
            .onErrorResume(e -> Mono.just(Result.unhealthy(COMPONENT_NAME, "Error checking Cassandra backend", e)));
    }
}
