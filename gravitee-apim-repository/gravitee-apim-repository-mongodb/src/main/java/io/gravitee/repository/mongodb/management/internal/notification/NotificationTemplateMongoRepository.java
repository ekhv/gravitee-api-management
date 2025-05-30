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
package io.gravitee.repository.mongodb.management.internal.notification;

import io.gravitee.repository.management.model.NotificationTemplateReferenceType;
import io.gravitee.repository.mongodb.management.internal.model.NotificationTemplateMongo;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author Florent CHAMFROY (florent.chamfroy at graviteesource.com)
 * @author GraviteeSource Team
 */
@Repository
public interface NotificationTemplateMongoRepository extends MongoRepository<NotificationTemplateMongo, String> {
    @Query("{ 'hook': ?0, 'scope': ?1, 'referenceId': ?2, 'referenceType': ?3 }")
    List<NotificationTemplateMongo> findByHookAndScopeAndReferenceIdAndReferenceType(
        String hook,
        String scope,
        String referenceId,
        String referenceType
    );

    @Query("{ 'referenceId': ?0, 'referenceType': ?1 }")
    List<NotificationTemplateMongo> findByReferenceIdAndReferenceType(String referenceId, String referenceType);

    @Query("{ type: ?0, 'referenceId': ?1, 'referenceType': ?2 }")
    List<NotificationTemplateMongo> findByType(String type, String referenceId, String referenceType);

    @Query(value = "{ 'referenceId': ?0, 'referenceType': ?1 }", delete = true)
    List<NotificationTemplateMongo> deleteByReferenceIdAndReferenceType(
        String referenceId,
        NotificationTemplateReferenceType referenceType
    );
}
