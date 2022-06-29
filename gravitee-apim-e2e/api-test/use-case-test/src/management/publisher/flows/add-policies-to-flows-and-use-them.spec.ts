/*
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import { afterAll, beforeAll, describe, expect } from '@jest/globals';
import { APIsApi } from '@management-apis/APIsApi';
import { forManagementAsApiUser } from '@client-conf/*';
import { ApplicationsApi } from '@management-apis/ApplicationsApi';
import { ApiEntity } from '@management-models/ApiEntity';
import { ApplicationEntity } from '@management-models/ApplicationEntity';
import { ApisFaker } from '@management-fakers/ApisFaker';
import { PlansFaker } from '@management-fakers/PlansFaker';
import { PlanSecurityType } from '@management-models/PlanSecurityType';
import { PlanStatus } from '@management-models/PlanStatus';
import { LifecycleAction } from '@management-models/LifecycleAction';
import { ApplicationsFaker } from '@management-fakers/ApplicationsFaker';
import { teardownApisAndApplications } from '@lib/management';
import { PathOperatorOperatorEnum } from '@management-models/PathOperator';
import { fetchGatewaySuccess } from '@lib/gateway';
import { LoadBalancerTypeEnum } from '@management-models/LoadBalancer';

const orgId = 'DEFAULT';
const envId = 'DEFAULT';

const apisResource = new APIsApi(forManagementAsApiUser());
const applicationsResource = new ApplicationsApi(forManagementAsApiUser());

describe('Add policies to flows and use them', () => {
  let createdApi: ApiEntity;
  let createdApplication: ApplicationEntity;
  let cachedResponseBody: any;

  beforeAll(async () => {
    createdApi = await apisResource.importApiDefinition({
      orgId,
      envId,
      body: ApisFaker.apiImport({
        plans: [PlansFaker.plan({ security: PlanSecurityType.KEYLESS, status: PlanStatus.PUBLISHED })],
        proxy: ApisFaker.proxy({
          groups: [
            {
              name: 'default-group',
              endpoints: [
                {
                  inherit: true,
                  name: 'default',
                  target: `${process.env.WIREMOCK_BASE_URL}/whattimeisit`,
                  weight: 1,
                  backup: false,
                  type: 'http',
                },
              ],
              load_balancing: {
                type: LoadBalancerTypeEnum.ROUNDROBIN,
              },
              http: {
                connectTimeout: 5000,
                idleTimeout: 60000,
                keepAlive: true,
                readTimeout: 10000,
                pipelining: false,
                maxConcurrentConnections: 100,
                useCompression: true,
                followRedirects: false,
              },
            },
          ],
        }),
        resources: [
          {
            name: 'test-cache',
            type: 'cache',
            enabled: true,
            configuration: {
              maxEntriesLocalHeap: 1000,
              timeToIdleSeconds: 0,
              timeToLiveSeconds: 5,
            },
          },
        ],
        flows: [
          {
            name: '',
            path_operator: {
              path: '/',
              operator: PathOperatorOperatorEnum.STARTSWITH,
            },
            condition: '',
            consumers: [],
            methods: [],
            pre: [
              {
                name: 'HTTP Callout',
                description: '',
                enabled: true,
                policy: 'policy-http-callout',
                condition: "{#request.headers['x-callout-name'] != null}",
                configuration: {
                  variables: [
                    {
                      name: 'finalBody',
                      value: '{#calloutResponse.content}',
                    },
                  ],
                  method: 'GET',
                  fireAndForget: false,
                  scope: 'REQUEST',
                  errorStatusCode: '500',
                  errorCondition: '{#calloutResponse.status >= 400 and #calloutResponse.status <= 599}',
                  url: `${process.env.WIREMOCK_BASE_URL}/hello?name={#request.headers['x-callout-name']}`,
                  exitOnError: false,
                },
              },
              {
                name: 'Cache',
                description: '',
                enabled: true,
                policy: 'cache',
                configuration: {
                  timeToLiveSeconds: 5,
                  cacheName: 'test-cache',
                  methods: ['GET', 'OPTIONS', 'HEAD'],
                  useResponseCacheHeaders: false,
                  scope: 'API',
                  responseCondition: '{#upstreamResponse.status == 200}',
                  key: "{#request.headers['x-cache-key']}",
                },
              },
            ],
            post: [
              {
                name: 'Assign content',
                description: '',
                enabled: true,
                policy: 'policy-assign-content',
                condition: "{#request.headers['x-callout-name'] != null}",
                configuration: {
                  scope: 'RESPONSE',
                  body: "${context.attributes['finalBody']}",
                },
              },
            ],
            enabled: true,
          },
        ],
      }),
    });

    await apisResource.doApiLifecycleAction({
      envId,
      orgId,
      api: createdApi.id,
      action: LifecycleAction.START,
    });

    createdApplication = await applicationsResource.createApplication({
      envId,
      orgId,
      newApplicationEntity: ApplicationsFaker.newApplication(),
    });
  });

  describe('Use cache policy, waiting for cache eviction', () => {
    beforeAll(async () => {
      cachedResponseBody = await fetchGatewaySuccess({
        contextPath: `${createdApi.context_path}`,
        headers: {
          'x-cache-key': 'gravitee-test',
        },
      }).then((res) => res.json());
    });

    test('Should respond using cache resource', async () => {
      const responseBody = await fetchGatewaySuccess({
        contextPath: `${createdApi.context_path}`,
        headers: {
          'x-cache-key': 'gravitee-test',
        },
      }).then((res) => res.json());

      expect(responseBody).toEqual(cachedResponseBody);
    });

    test('Should respond with endpoint body after cache has expired', async () => {
      const responseBody = await fetchGatewaySuccess({
        contextPath: `${createdApi.context_path}`,
        headers: {
          'x-cache-key': 'gravitee-test',
        },
        timeout: 6000, // wait for cache eviction before calling the entrypoint
      }).then((res) => res.json());

      expect(responseBody).not.toEqual(cachedResponseBody);
    });
  });

  describe('Use cache policy, using a new cache key', () => {
    beforeAll(async () => {
      cachedResponseBody = await fetchGatewaySuccess({
        contextPath: `${createdApi.context_path}`,
        headers: {
          'x-cache-key': 'gravitee-test',
        },
      }).then((res) => res.json());
    });

    test('Should respond with endpoint body with a new cache key', async () => {
      const responseBody = await fetchGatewaySuccess({
        contextPath: `${createdApi.context_path}`,
        failAfterMs: 5000,
        headers: {
          'x-cache-key': 'gravitee-dev',
        },
      }).then((res) => res.json());

      expect(responseBody).not.toEqual(cachedResponseBody);
    });
  });

  describe('Use cache policy, bypassing cache from a header', () => {
    beforeAll(async () => {
      cachedResponseBody = await fetchGatewaySuccess({
        contextPath: `${createdApi.context_path}`,
        headers: {
          'x-cache-key': 'gravitee-test',
        },
      }).then((res) => res.json());
    });

    test('Should respond with endpoint body with cache bypass header', async () => {
      const responseBody = await fetchGatewaySuccess({
        contextPath: `${createdApi.context_path}`,
        failAfterMs: 5000,
        headers: {
          'x-cache-key': 'gravitee-test',
          'x-gravitee-cache': 'BY_PASS',
        },
      }).then((res) => res.json());

      expect(responseBody).not.toEqual(cachedResponseBody);
    });
  });

  describe('Use cache policy, bypassing cache from a query parameter', () => {
    beforeAll(async () => {
      cachedResponseBody = await fetchGatewaySuccess({
        contextPath: `${createdApi.context_path}`,
        headers: {
          'x-cache-key': 'gravitee-test',
        },
      }).then((res) => res.json());
    });

    test('Should respond with endpoint body with cache bypass query parameter', async () => {
      const responseBody = await fetchGatewaySuccess({
        contextPath: `${createdApi.context_path}?cache=BY_PASS_`,
        failAfterMs: 5000,
        headers: {
          'x-cache-key': 'gravitee-dev',
        },
      }).then((res) => res.json());

      expect(responseBody).not.toEqual(cachedResponseBody);
    });
  });

  describe('Use HTTP callout policy and assign content', () => {
    beforeAll(async () => {
      cachedResponseBody = await fetchGatewaySuccess({
        contextPath: `${createdApi.context_path}`,
        headers: {
          'x-cache-key': 'gravitee-test',
        },
      }).then((res) => res.json());
    });

    test('Should callout using request header and assign body content', async () => {
      const responseBody = await fetchGatewaySuccess({
        contextPath: `${createdApi.context_path}`,
        headers: {
          'x-callout-name': 'gravitee',
        },
      }).then((res) => res.json());

      expect(responseBody.timestamp).toBeUndefined();
      expect(responseBody.message).toBe('Hello, Gravitee!');
    });
  });

  afterAll(async () => {
    await teardownApisAndApplications(orgId, envId, [createdApi?.id], [createdApplication?.id]);
  });
});
