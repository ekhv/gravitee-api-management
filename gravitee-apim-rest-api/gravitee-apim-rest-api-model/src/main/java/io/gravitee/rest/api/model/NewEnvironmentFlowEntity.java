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
package io.gravitee.rest.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.EnumSet;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@Schema(name = "NewEnvironmentFlow")
public class NewEnvironmentFlowEntity {

    @NotBlank
    @NotEmpty(message = "Environment flow's name must not be empty")
    @Schema(description = "Environment flow's name.", example = "My Environment flow")
    private String name;

    @Schema(description = "Version", example = "1")
    private String version;

    @NotNull
    @Schema(description = "Phase", example = "REQUEST, PUBLISH")
    private EnumSet<EnvironmentFlowPhase> phase;

    @Schema(description = "List of policies (steps)")
    private List<EnvironmentFlowStep> policies;
}
