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
package io.gravitee.rest.api.service.impl;

import static io.gravitee.common.component.Lifecycle.State.STARTED;
import static io.gravitee.common.component.Lifecycle.State.STOPPED;
import static io.gravitee.definition.model.DefinitionContext.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.gravitee.definition.model.DefinitionContext;
import io.gravitee.rest.api.model.EventType;
import io.gravitee.rest.api.model.api.ApiCRDEntity;
import io.gravitee.rest.api.model.api.ApiCRDStatusEntity;
import io.gravitee.rest.api.model.api.ApiEntity;
import io.gravitee.rest.api.service.*;
import io.gravitee.rest.api.service.common.ExecutionContext;
import io.gravitee.rest.api.service.common.GraviteeContext;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

/**
 * @author Kamiel Ahmadpour (kamiel.ahmadpour at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class ApiCRDService_UpdateTest {

    @Mock
    private ApiService apiService;

    @Mock
    private ApiDuplicatorService apiDuplicatorService;

    @Mock
    private ParameterService parameterService;

    @Mock
    private PlanService planService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private ApiCRDService apiCRDService = new ApiCDRServiceImpl();

    private static final String API_ID = "id-api";
    private static final String API_CROSS_ID = "id-cross-api";
    private static final String DEFAULT_ENVIRONMENT_ID = "DEFAULT";
    private static final String API_NAME = "myAPI";

    @Test
    public void shouldUpdate() throws JsonProcessingException {
        ExecutionContext ec = GraviteeContext.getExecutionContext();
        ApiCRDEntity apiCRD = anApiCRDEntity();
        when(apiService.findByEnvironmentIdAndCrossId(DEFAULT_ENVIRONMENT_ID, API_CROSS_ID)).thenReturn(Optional.of(toApiEntity(apiCRD)));

        apiCRD.setName("update_name");
        when(apiDuplicatorService.updateWithImportedDefinition(ec, API_ID, objectMapper.writeValueAsString(apiCRD)))
            .thenReturn(toApiEntity(apiCRD));

        final ApiCRDStatusEntity apiCRDStatus = apiCRDService.importApiDefinitionCRD(ec, apiCRD);

        assertNotNull(apiCRDStatus);
        assertEquals(API_ID, apiCRDStatus.getId());
        assertEquals(API_CROSS_ID, apiCRDStatus.getCrossId());
        assertEquals(DEFAULT_ENVIRONMENT_ID, apiCRDStatus.getEnvironmentId());
        assertEquals(apiCRD.getState(), apiCRDStatus.getState());

        // Picture management as a dedicated service, so we should reuse the same picture as the one saved
        verify(apiService, times(1)).findByEnvironmentIdAndCrossId(DEFAULT_ENVIRONMENT_ID, API_CROSS_ID);
        verify(apiService, times(0)).deploy(any(), any(), any(), any(), any());
        verify(apiService, times(0)).start(eq(ec), eq(API_ID), any());
        verify(parameterService, times(0)).findAsBoolean(any(), any(), any());
    }

    @Test
    public void shouldUpdateAndDeployApi() throws JsonProcessingException {
        ExecutionContext ec = GraviteeContext.getExecutionContext();
        ApiCRDEntity apiCRD = anApiCRDEntity();
        when(apiService.findByEnvironmentIdAndCrossId(DEFAULT_ENVIRONMENT_ID, API_CROSS_ID)).thenReturn(Optional.of(toApiEntity(apiCRD)));

        apiCRD.setDefinitionContext(new DefinitionContext(ORIGIN_KUBERNETES, MODE_FULLY_MANAGED, ORIGIN_MANAGEMENT));
        apiCRD.setName("update_name");
        when(apiDuplicatorService.updateWithImportedDefinition(ec, API_ID, objectMapper.writeValueAsString(apiCRD)))
            .thenReturn(toApiEntity(apiCRD));

        final ApiCRDStatusEntity apiCRDStatus = apiCRDService.importApiDefinitionCRD(ec, apiCRD);

        assertNotNull(apiCRDStatus);
        assertEquals(API_ID, apiCRDStatus.getId());
        assertEquals(API_CROSS_ID, apiCRDStatus.getCrossId());
        assertEquals(DEFAULT_ENVIRONMENT_ID, apiCRDStatus.getEnvironmentId());
        assertEquals(apiCRD.getState(), apiCRDStatus.getState());

        // Picture management as a dedicated service, so we should reuse the same picture as the one saved
        verify(apiService, times(1)).findByEnvironmentIdAndCrossId(DEFAULT_ENVIRONMENT_ID, API_CROSS_ID);
        verify(apiService, times(1)).deploy(any(), any(), any(), any(), any());
        verify(apiService, times(0)).start(eq(ec), eq(API_ID), any());
        verify(apiService, times(0)).start(eq(ec), eq(API_ID), any());
        verify(parameterService, times(0)).findAsBoolean(any(), any(), any());
    }

    @Test
    public void shouldUpdateDeployAndStopApi() throws JsonProcessingException {
        ExecutionContext ec = GraviteeContext.getExecutionContext();
        ApiCRDEntity apiCRD = anApiCRDEntity();
        when(apiService.findByEnvironmentIdAndCrossId(DEFAULT_ENVIRONMENT_ID, API_CROSS_ID)).thenReturn(Optional.of(toApiEntity(apiCRD)));

        apiCRD.setDefinitionContext(new DefinitionContext(ORIGIN_KUBERNETES, MODE_FULLY_MANAGED, ORIGIN_MANAGEMENT));
        apiCRD.setName("update_name");
        apiCRD.setState(STOPPED);
        when(apiDuplicatorService.updateWithImportedDefinition(ec, API_ID, objectMapper.writeValueAsString(apiCRD)))
            .thenReturn(toApiEntity(apiCRD));

        final ApiCRDStatusEntity apiCRDStatus = apiCRDService.importApiDefinitionCRD(ec, apiCRD);

        assertNotNull(apiCRDStatus);
        assertEquals(API_ID, apiCRDStatus.getId());
        assertEquals(API_CROSS_ID, apiCRDStatus.getCrossId());
        assertEquals(DEFAULT_ENVIRONMENT_ID, apiCRDStatus.getEnvironmentId());
        assertEquals(apiCRD.getState(), apiCRDStatus.getState());

        // Picture management as a dedicated service, so we should reuse the same picture as the one saved
        verify(apiService, times(1)).findByEnvironmentIdAndCrossId(DEFAULT_ENVIRONMENT_ID, API_CROSS_ID);
        verify(apiService, times(1)).deploy(any(), any(), any(), any(), any());
        verify(apiService, times(0)).start(eq(ec), eq(API_ID), any());
        verify(apiService, times(1)).stop(eq(ec), eq(API_ID), any());
        verify(parameterService, times(1)).findAsBoolean(any(), any(), any());
    }

    public ApiCRDEntity anApiCRDEntity() {
        ApiCRDEntity crd = new ApiCRDEntity();
        crd.setId(API_ID);
        crd.setCrossId(API_CROSS_ID);
        crd.setName(API_NAME);
        crd.setDescription(API_NAME);
        crd.setDefinitionContext(new DefinitionContext(ORIGIN_KUBERNETES, MODE_FULLY_MANAGED, ORIGIN_KUBERNETES));
        crd.setState(STARTED);

        return crd;
    }

    public ApiEntity toApiEntity(ApiCRDEntity crd) {
        ApiEntity api = new ApiEntity();
        api.setId(crd.getId());
        api.setCrossId(crd.getCrossId());
        api.setName(crd.getName());
        api.setDescription(crd.getDescription());
        api.setDefinitionContext(crd.getDefinitionContext());
        api.setState(crd.getState());

        return api;
    }
}
