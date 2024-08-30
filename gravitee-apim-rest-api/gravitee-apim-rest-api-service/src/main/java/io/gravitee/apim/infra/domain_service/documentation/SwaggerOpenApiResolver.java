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
package io.gravitee.apim.infra.domain_service.documentation;

import io.gravitee.apim.core.api.model.Api;
import io.gravitee.apim.core.documentation.domain_service.OpenApiDomainService;
import io.gravitee.apim.core.documentation.exception.InvalidPageContentException;
import io.gravitee.apim.core.documentation.model.Page;
import io.gravitee.apim.infra.adapter.ApiAdapter;
import io.gravitee.apim.infra.adapter.PageAdapter;
import io.gravitee.rest.api.model.PageEntity;
import io.gravitee.rest.api.service.PageService;
import io.gravitee.rest.api.service.common.ExecutionContext;
import io.gravitee.rest.api.service.impl.swagger.parser.OAIParser;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * @author Kamiel Ahmadpour (kamiel.ahmadpour at graviteesource.com)
 * @author GraviteeSource Team
 */
@Service
@RequiredArgsConstructor
public class SwaggerOpenApiResolver implements OpenApiDomainService {

    private final PageService pageServiceDelegate;

    @Override
    public void parseOpenApiContent(String content) throws InvalidPageContentException {
        if (content != null) {
            try {
                OAIParser oaiParser = new OAIParser();
                oaiParser.parse(content);
            } catch (Exception e) {
                throw new InvalidPageContentException("Invalid Open Api content " + e.getMessage(), e);
            }
        }
    }

    @Override
    public String transformOpenApiContent(ExecutionContext executionContext, Map<String, String> configuration, String content, Api api) {
        // TODO: Decide if v2 apis are supported
        var apiEntity = ApiAdapter.INSTANCE.toApiEntity(api);

        var pageEntity = new PageEntity();
        pageEntity.setContent(content);
        pageEntity.setConfiguration(configuration);

        pageServiceDelegate.transformSwagger(executionContext, pageEntity, apiEntity);

        return pageEntity.getContent();
    }
}
