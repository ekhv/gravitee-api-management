/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.rest.api.management.v2.rest.resource.kafka_ui;

import io.gravitee.apim.core.cluster.query_service.ClusterQueryService;
import io.gravitee.rest.api.management.v2.rest.resource.AbstractResource;

import io.gravitee.rest.api.service.exceptions.TechnicalManagementException;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpMethod;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.AsyncResponse;
import jakarta.ws.rs.container.Suspended;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
@Slf4j
@Path("/kafka-console")
public class KafkaUIResource extends AbstractResource {

    @GET
    @Path("/{s:.*}")
    public void proxyKafbatBack(@Suspended AsyncResponse finalResponse, @Context UriInfo ui, @PathParam("s") String s) {
        Vertx vertx = Vertx.vertx();
        var httpClient = vertx.createHttpClient();
        Future<HttpClientRequest> requestFuture = httpClient.request(HttpMethod.GET, 8080, "localhost", ui.getPath());
        requestFuture
                .onFailure(throwable -> {
                    finalResponse.resume(throwable);

                    // Close client
                    httpClient.close();
                })
                .onSuccess(request -> {
                    request
                            .response(asyncResponse -> {
                                if (asyncResponse.failed()) {
                                    finalResponse.resume(asyncResponse.cause());

                                    // Close client
                                    httpClient.close();
                                } else {
                                    HttpClientResponse response = asyncResponse.result();

                                    if (response.statusCode() / 100 == 2) {
                                        response.bodyHandler(buffer -> {
                                            MediaType mediaType = MediaType.valueOf(response.getHeader(HttpHeaders.CONTENT_TYPE));

                                            String payload;
                                            if(s.contains("api/clusters")) {
                                                payload = """
                                                        [
                                                            {
                                                                "name": "direct",
                                                                "defaultCluster": null,
                                                                "status": "online",
                                                                "lastError": null,
                                                                "brokerCount": 1,
                                                                "onlinePartitionCount": 0,
                                                                "topicCount": 0,
                                                                "bytesInPerSec": null,
                                                                "bytesOutPerSec": null,
                                                                "readOnly": false,
                                                                "version": "3.9-IV0",
                                                                "features": [
                                                                    "TOPIC_DELETION",
                                                                    "CLIENT_QUOTA_MANAGEMENT"
                                                                ]
                                                            }
                                                        ]""";
                                            } else {
                                                payload = buffer.toString();

                                                payload = payload.replace("""
                                                        href="/""", """
                                                        href=\"""");

                                                payload = payload.replace("""
                                                        src="/""", """
                                                        src=\"""");

                                                payload = payload.replace("""
                                                        window.basePath = '';""", """
                                                        window.basePath = '/management/v2/kafka-console';""");
                                            }

                                            finalResponse.resume(Response.ok(payload, mediaType).build());

                                            // Close client
                                            httpClient.close();
                                        });
                                    } else {
                                        finalResponse.resume(
                                                new TechnicalManagementException(
                                                        " Error on url '" +
                                                        ui.getPath() +
                                                        "'. Status code: " +
                                                        response.statusCode() +
                                                        ". Message: " +
                                                        response.statusMessage(),
                                                        null
                                                )
                                        );
                                    }
                                }
                            })
                            .exceptionHandler(throwable -> {
                                finalResponse.resume(throwable);

                                // Close client
                                httpClient.close();
                            });
                    request.end();
                });

    }


}
