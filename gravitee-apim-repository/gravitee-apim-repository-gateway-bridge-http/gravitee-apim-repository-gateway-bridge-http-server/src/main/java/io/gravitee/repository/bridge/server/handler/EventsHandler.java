/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.repository.bridge.server.handler;

import static io.gravitee.repository.bridge.server.utils.ParamUtils.getPageNumber;
import static io.gravitee.repository.bridge.server.utils.ParamUtils.getPageSize;

import io.gravitee.common.data.domain.Page;
import io.gravitee.repository.bridge.server.handler.response.AbstractHandler;
import io.gravitee.repository.management.api.EventRepository;
import io.gravitee.repository.management.api.search.EventCriteria;
import io.gravitee.repository.management.api.search.builder.PageableBuilder;
import io.gravitee.repository.management.model.Event;
import io.gravitee.repository.management.model.EventType;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.WorkerExecutor;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public class EventsHandler extends AbstractHandler {

    @Autowired
    private EventRepository eventRepository;

    private static final long DEFAULT_PAGE_SIZE = 10;
    private static final long DEFAULT_PAGE_NUMBER = 1;

    public EventsHandler(WorkerExecutor bridgeWorkerExecutor) {
        super(bridgeWorkerExecutor);
    }

    public void search(RoutingContext ctx) {
        final JsonObject searchPayload = ctx.getBodyAsJson();
        final EventCriteria eventCriteria = readCriteria(searchPayload);

        final String sPageNumber = ctx.request().getParam("page");

        if (sPageNumber != null) {
            bridgeWorkerExecutor.executeBlocking(
                promise -> {
                    Long pageSize = getPageSize(ctx, DEFAULT_PAGE_SIZE);
                    Long pageNumber = getPageNumber(ctx, DEFAULT_PAGE_NUMBER);

                    try {
                        promise.complete(
                            eventRepository.search(
                                eventCriteria,
                                new PageableBuilder().pageNumber(pageNumber.intValue()).pageSize(pageSize.intValue()).build()
                            )
                        );
                    } catch (Exception ex) {
                        LOGGER.error("Unable to search for events", ex);
                        promise.fail(ex);
                    }
                },
                false,
                (Handler<AsyncResult<Page<Event>>>) event -> handleResponse(ctx, event)
            );
        } else {
            bridgeWorkerExecutor.executeBlocking(
                (Handler<Promise<List<Event>>>) promise -> {
                    try {
                        promise.complete(eventRepository.search(eventCriteria));
                    } catch (Exception ex) {
                        LOGGER.error("Unable to search for events", ex);
                        promise.fail(ex);
                    }
                },
                false,
                event -> handleResponse(ctx, event)
            );
        }
    }

    public void create(RoutingContext ctx) {
        bridgeWorkerExecutor.executeBlocking(
            promise -> {
                try {
                    Event event = ctx.getBodyAsJson().mapTo(Event.class);
                    promise.complete(eventRepository.create(event));
                } catch (Exception ex) {
                    LOGGER.error("Unable to create an event", ex);
                    promise.fail(ex);
                }
            },
            false,
            (Handler<AsyncResult<Event>>) event -> handleResponse(ctx, event)
        );
    }

    public void update(RoutingContext ctx) {
        bridgeWorkerExecutor.executeBlocking(
            promise -> {
                try {
                    Event event = ctx.getBodyAsJson().mapTo(Event.class);
                    promise.complete(eventRepository.update(event));
                } catch (Exception ex) {
                    LOGGER.error("Unable to update an event", ex);
                    promise.fail(ex);
                }
            },
            false,
            (Handler<AsyncResult<Event>>) event -> handleResponse(ctx, event)
        );
    }

    public void findById(RoutingContext ctx) {
        final String idParam = ctx.request().getParam("eventId");

        bridgeWorkerExecutor.executeBlocking(
            (Handler<Promise<Optional<Event>>>) promise -> {
                try {
                    promise.complete(eventRepository.findById(idParam));
                } catch (Exception ex) {
                    LOGGER.error("Unable to find event by id {}", idParam, ex);
                    promise.fail(ex);
                }
            },
            false,
            event -> handleResponse(ctx, event)
        );
    }

    public void createOrPatch(RoutingContext ctx) {
        bridgeWorkerExecutor.executeBlocking(
            promise -> {
                try {
                    Event event = ctx.getBodyAsJson().mapTo(Event.class);
                    promise.complete(eventRepository.createOrPatch(event));
                } catch (Exception ex) {
                    LOGGER.error("Unable to create or update an event", ex);
                    promise.fail(ex);
                }
            },
            false,
            (Handler<AsyncResult<Event>>) event -> handleResponse(ctx, event)
        );
    }

    static EventCriteria readCriteria(JsonObject payload) {
        EventCriteria.EventCriteriaBuilder builder = EventCriteria.builder();

        if (payload != null) {
            Long fromVal = payload.getLong("from");
            if (fromVal != null && fromVal > 0) {
                builder.from(fromVal);
            }

            Long toVal = payload.getLong("to");
            if (toVal != null && toVal > 0) {
                builder.to(toVal);
            }

            JsonArray typesArr = payload.getJsonArray("types");
            if (typesArr != null) {
                Set<EventType> types = typesArr.stream().map(obj -> EventType.valueOf((String) obj)).collect(Collectors.toSet());
                builder.types(types);
            }

            JsonObject propertiesObj = payload.getJsonObject("properties");
            if (propertiesObj != null) {
                propertiesObj.getMap().forEach(builder::property);
            }

            JsonArray environmentsArr = payload.getJsonArray("environments");
            if (environmentsArr != null) {
                final List<String> environments = environmentsArr.stream().map(String.class::cast).collect(Collectors.toList());

                builder.environments(environments);
            }
        }

        return builder.build();
    }
}
