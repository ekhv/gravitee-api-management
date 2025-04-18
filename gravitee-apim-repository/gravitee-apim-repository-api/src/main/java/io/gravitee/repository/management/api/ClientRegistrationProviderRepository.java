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
package io.gravitee.repository.management.api;

import io.gravitee.repository.exceptions.TechnicalException;
import io.gravitee.repository.management.model.ClientRegistrationProvider;
import java.util.List;
import java.util.Set;

/**
 * @author David BRASSELY (david.brassely at graviteesource.com)
 * @author GraviteeSource Team
 */
public interface ClientRegistrationProviderRepository extends CrudRepository<ClientRegistrationProvider, String> {
    /**
     * List all client registration providers
     * @return all client registration providers
     * @throws TechnicalException if something goes wrong
     */
    Set<ClientRegistrationProvider> findAll() throws TechnicalException;

    /**
     * List all client registration providers by environment
     * @param environmentId
     * @return all client registration providers
     * @throws TechnicalException if something goes wrong
     */
    Set<ClientRegistrationProvider> findAllByEnvironment(String environmentId) throws TechnicalException;

    /**
     * Delete client registration providers by environment
     *
     * @param environmentId The environment ID
     * @return List of deleted IDs for client registration providers
     * @throws TechnicalException
     */
    List<String> deleteByEnvironmentId(String environmentId) throws TechnicalException;
}
