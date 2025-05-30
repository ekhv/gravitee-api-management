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

import io.gravitee.definition.model.DefinitionVersion;
import io.gravitee.definition.model.HttpRequest;
import io.gravitee.definition.model.HttpResponse;
import io.gravitee.definition.model.v4.ApiType;
import java.util.List;
import java.util.Set;

/**
 * Interface to represent a debug API proxy.
 * This interface needs to be implemented by all debug API proxy classes (V2 and V4)
 */
public interface DebugApiProxy {
    HttpRequest getRequest();
    HttpResponse getResponse();
    List<DebugStep> getDebugSteps();
    HttpResponse getBackendResponse();
    PreprocessorStep getPreprocessorStep();
    DebugMetrics getMetrics();

    String getId();
    DefinitionVersion getDefinitionVersion();
    ApiType getType();
    Set<String> getTags();

    void setPreprocessorStep(PreprocessorStep preprocessorStep);
    void setDebugSteps(List<DebugStep> debugSteps);
    void setBackendResponse(HttpResponse backendResponse);
    void setMetrics(DebugMetrics metrics);
    void setResponse(HttpResponse response);
}
