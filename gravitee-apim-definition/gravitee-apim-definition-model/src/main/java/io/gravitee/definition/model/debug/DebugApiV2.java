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
package io.gravitee.definition.model.debug;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.gravitee.definition.model.Api;
import io.gravitee.definition.model.HttpRequest;
import io.gravitee.definition.model.HttpResponse;
import io.gravitee.definition.model.v4.ApiType;
import java.io.Serializable;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@SuperBuilder
public class DebugApiV2 extends Api implements DebugApiProxy, Serializable {

    @JsonProperty("request")
    private HttpRequest request;

    @JsonProperty("response")
    private HttpResponse response;

    @JsonProperty("debugSteps")
    private List<DebugStep> debugSteps;

    @JsonProperty("preprocessorStep")
    private PreprocessorStep preprocessorStep;

    @JsonProperty("backendResponse")
    private HttpResponse backendResponse;

    @JsonProperty("metrics")
    private DebugMetrics metrics;

    public DebugApiV2() {}

    public DebugApiV2(Api other, HttpRequest request) {
        super(other);
        this.request = request;
    }

    @Override
    public ApiType getType() {
        return ApiType.PROXY;
    }
}
