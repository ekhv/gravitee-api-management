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
package io.gravitee.repository.mongodb.management.internal.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "#{@environment.getProperty('management.mongodb.prefix')}scoring_rulesets")
public class ScoringRulesetMongo extends Auditable {

    public enum Format {
        GRAVITEE_FEDERATION,
        GRAVITEE_MESSAGE,
        GRAVITEE_PROXY,
        GRAVITEE_NATIVE,
        GRAVITEE_V2,
        OPENAPI,
        ASYNCAPI,
    }

    @Id
    private String id;

    private String name;
    private String description;
    private String payload;
    private Format format;
    private String referenceId;
    private String referenceType;
}
