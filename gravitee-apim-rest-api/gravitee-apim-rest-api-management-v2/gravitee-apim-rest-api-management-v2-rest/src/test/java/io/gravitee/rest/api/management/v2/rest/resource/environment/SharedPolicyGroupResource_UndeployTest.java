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
package io.gravitee.rest.api.management.v2.rest.resource.environment;

import static assertions.MAPIAssertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fixtures.core.model.SharedPolicyGroupFixtures;
import io.gravitee.apim.core.audit.model.AuditInfo;
import io.gravitee.apim.core.shared_policy_group.exception.SharedPolicyGroupNotFoundException;
import io.gravitee.apim.core.shared_policy_group.use_case.UndeploySharedPolicyGroupUseCase;
import io.gravitee.rest.api.management.v2.rest.resource.AbstractResourceTest;
import io.gravitee.rest.api.model.EnvironmentEntity;
import io.gravitee.rest.api.service.common.GraviteeContext;
import jakarta.inject.Inject;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class SharedPolicyGroupResource_UndeployTest extends AbstractResourceTest {

    private static final String ENV_ID = "my-env";
    private static final String SHARED_POLICY_GROUP_ID = "my-shared-policy-group";

    @Inject
    UndeploySharedPolicyGroupUseCase undeploySharedPolicyGroupUseCase;

    @Override
    protected String contextPath() {
        return "/environments/" + ENV_ID + "/shared-policy-groups/" + SHARED_POLICY_GROUP_ID + "/_undeploy";
    }

    @BeforeEach
    void init() {
        super.setUp();
        GraviteeContext.cleanContext();

        EnvironmentEntity environmentEntity = new EnvironmentEntity();
        environmentEntity.setId(ENV_ID);
        environmentEntity.setOrganizationId(ORGANIZATION);
        when(environmentService.findById(ENV_ID)).thenReturn(environmentEntity);
        when(environmentService.findByOrgAndIdOrHrid(ORGANIZATION, ENV_ID)).thenReturn(environmentEntity);

        GraviteeContext.setCurrentEnvironment(ENV_ID);
        GraviteeContext.setCurrentOrganization(ORGANIZATION);
    }

    @AfterEach
    public void tearDown() {
        super.tearDown();
        GraviteeContext.cleanContext();
        reset(undeploySharedPolicyGroupUseCase);
    }

    @Test
    void should_undeploy_shared_policy_group() {
        // Given
        var sharedPolicyGroup = SharedPolicyGroupFixtures.aSharedPolicyGroup();
        when(undeploySharedPolicyGroupUseCase.execute(any())).thenReturn(new UndeploySharedPolicyGroupUseCase.Output(sharedPolicyGroup));

        // When
        var response = rootTarget().request().post(null);

        // Then
        assertThat(response).hasStatus(202);
        var captor = ArgumentCaptor.forClass(UndeploySharedPolicyGroupUseCase.Input.class);
        verify(undeploySharedPolicyGroupUseCase).execute(captor.capture());
        SoftAssertions.assertSoftly(soft -> {
            var input = captor.getValue();
            soft.assertThat(input.environmentId()).isEqualTo(ENV_ID);
            soft.assertThat(input.sharedPolicyGroupId()).isEqualTo(SHARED_POLICY_GROUP_ID);
            soft.assertThat(input.auditInfo()).isInstanceOf(AuditInfo.class);
        });
    }

    @Test
    void should_not_deploy_shared_policy_group_when_not_found() {
        // Given
        when(undeploySharedPolicyGroupUseCase.execute(any())).thenThrow(new SharedPolicyGroupNotFoundException(SHARED_POLICY_GROUP_ID));

        // When
        var response = rootTarget().request().post(null);

        // Then
        assertThat(response).hasStatus(404).asError().hasMessage("SharedPolicyGroup [" + SHARED_POLICY_GROUP_ID + "] cannot be found.");
        var captor = ArgumentCaptor.forClass(UndeploySharedPolicyGroupUseCase.Input.class);
        verify(undeploySharedPolicyGroupUseCase).execute(captor.capture());
        SoftAssertions.assertSoftly(soft -> {
            var input = captor.getValue();
            soft.assertThat(input.environmentId()).isEqualTo(ENV_ID);
            soft.assertThat(input.sharedPolicyGroupId()).isEqualTo(SHARED_POLICY_GROUP_ID);
            soft.assertThat(input.auditInfo()).isInstanceOf(AuditInfo.class);
        });
    }
}
