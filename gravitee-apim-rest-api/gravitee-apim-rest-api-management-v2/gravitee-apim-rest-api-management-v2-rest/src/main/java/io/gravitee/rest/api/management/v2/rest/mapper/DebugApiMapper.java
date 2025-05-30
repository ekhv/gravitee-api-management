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
package io.gravitee.rest.api.management.v2.rest.mapper;

import io.gravitee.apim.core.event.model.Event;
import io.gravitee.definition.model.HttpRequest;
import io.gravitee.rest.api.management.v2.rest.model.DebugEvent;
import io.gravitee.rest.api.management.v2.rest.model.DebugHttpRequest;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = { DateMapper.class })
public interface DebugApiMapper {
    DebugApiMapper INSTANCE = Mappers.getMapper(DebugApiMapper.class);

    default HttpRequest map(DebugHttpRequest source) {
        return new HttpRequest(source.getPath(), source.getMethod(), source.getBody(), source.getHeaders());
    }

    DebugEvent map(Event source);
}
