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
import { commands, Config, Job, reusable } from '@circleci/circleci-config-sdk';
import { OpenJdkExecutor } from '../executors';
import { Command } from '@circleci/circleci-config-sdk/dist/src/lib/Components/Commands/exports/Command';
import { computeImagesTag, isSupportBranchOrMaster } from '../utils';
import { CircleCIEnvironment } from '../pipelines';
import { AddDockerImageInSnykCommand, CreateDockerContextCommand, DockerAzureLoginCommand, DockerAzureLogoutCommand } from '../commands';
import { orbs } from '../orbs';
import { config } from '../config';
import { SetupRemoteDockerCommand } from '../commands/cmd-setup-remote-docker';

export class BuildBackendImagesJob {
  public static create(dynamicConfig: Config, environment: CircleCIEnvironment): Job {
    const tag = computeImagesTag(environment.branch);

    const createDockerContextCmd = CreateDockerContextCommand.get();
    dynamicConfig.addReusableCommand(createDockerContextCmd);

    const dockerAzureLoginCmd = DockerAzureLoginCommand.get(dynamicConfig);
    dynamicConfig.addReusableCommand(dockerAzureLoginCmd);

    const dockerAzureLogoutCmd = DockerAzureLogoutCommand.get();
    dynamicConfig.addReusableCommand(dockerAzureLogoutCmd);

    const steps: Command[] = [
      new commands.Checkout(),
      new commands.workspace.Attach({ at: '.' }),
      SetupRemoteDockerCommand.get(),
      new reusable.ReusedCommand(createDockerContextCmd),
      new reusable.ReusedCommand(dockerAzureLoginCmd),
      new commands.Run({
        name: 'Build rest api and gateway docker images',
        command: `docker buildx build --push --platform=linux/arm64,linux/amd64 -f gravitee-apim-rest-api/docker/Dockerfile \\
-t graviteeio.azurecr.io/apim-management-api:${tag} \\
rest-api-docker-context

docker buildx build --push --platform=linux/arm64,linux/amd64 -f gravitee-apim-gateway/docker/Dockerfile \\
-t graviteeio.azurecr.io/apim-gateway:${tag} \\
gateway-docker-context`,
      }),
    ];

    // to move in support branch or master
    dynamicConfig.importOrb(orbs.keeper).importOrb(orbs.aquasec);
    steps.push(
      new commands.Run({
        name: 'Build Docker images locally',
        command: `docker buildx build --platform=linux/amd64 -f gravitee-apim-rest-api/docker/Dockerfile \\
-t graviteeio.azurecr.io/apim-management-api:${tag} \\
--output type=docker rest-api-docker-context

docker buildx build --platform=linux/amd64 -f gravitee-apim-gateway/docker/Dockerfile \\
-t graviteeio.azurecr.io/apim-gateway:${tag} \\
--output type=docker gateway-docker-context`,
      }),
      new reusable.ReusedCommand(orbs.keeper.commands['env-export'], {
        'secret-url': config.secrets.aquaKey,
        'var-name': 'AQUA_KEY',
      }),
      new reusable.ReusedCommand(orbs.keeper.commands['env-export'], {
        'secret-url': config.secrets.aquaSecret,
        'var-name': 'AQUA_SECRET',
      }),
      new reusable.ReusedCommand(orbs.keeper.commands['env-export'], {
        'secret-url': config.secrets.aquaRegistryUsername,
        'var-name': 'AQUA_USERNAME',
      }),
      new reusable.ReusedCommand(orbs.keeper.commands['env-export'], {
        'secret-url': config.secrets.aquaRegistryPassword,
        'var-name': 'AQUA_PASSWORD',
      }),
      new reusable.ReusedCommand(orbs.keeper.commands['env-export'], {
        'secret-url': config.secrets.aquaScannerKey,
        'var-name': 'SCANNER_TOKEN',
      }),
      new reusable.ReusedCommand(orbs.keeper.commands['env-export'], {
        'secret-url': config.secrets.githubApiToken,
        'var-name': 'GITHUB_TOKEN',
      }),
      new reusable.ReusedCommand(orbs.aquasec.commands['install_billy']),
      new reusable.ReusedCommand(orbs.aquasec.commands['pull_aqua_scanner_image']),
      new reusable.ReusedCommand(orbs.aquasec.commands['register_artifact'], {
        artifact_to_register: `graviteeio.azurecr.io/apim-management-api:${tag}`,
        debug: true,
      }),
      new reusable.ReusedCommand(orbs.aquasec.commands['register_artifact'], {
        artifact_to_register: `graviteeio.azurecr.io/apim-gateway:${tag}`,
        debug: true,
      }),
      new reusable.ReusedCommand(orbs.aquasec.commands['scan_docker_image'], {
        docker_image_to_scan: `graviteeio.azurecr.io/apim-management-api:${tag}`,
        scanner_url: config.aqua.scannerUrl,
      }),
      new reusable.ReusedCommand(orbs.aquasec.commands['scan_docker_image'], {
        docker_image_to_scan: `graviteeio.azurecr.io/apim-gateway:${tag}`,
        scanner_url: config.aqua.scannerUrl,
      }),
    );

    if (isSupportBranchOrMaster(environment.branch)) {
      dynamicConfig.importOrb(orbs.keeper);

      steps.push(
        new reusable.ReusedCommand(orbs.keeper.commands['env-export'], {
          'secret-url': config.secrets.snykApiToken,
          'var-name': 'SNYK_API_TOKEN',
        }),
        new reusable.ReusedCommand(orbs.keeper.commands['env-export'], {
          'secret-url': config.secrets.snykOrgId,
          'var-name': 'SNYK_ORG_ID',
        }),
        new reusable.ReusedCommand(orbs.keeper.commands['env-export'], {
          'secret-url': config.secrets.snykIntegrationId,
          'var-name': 'SNYK_INTEGRATION_ID',
        }),
        new reusable.ReusedCommand(AddDockerImageInSnykCommand.get(), {
          'docker-image-name': 'apim-management-api',
          version: tag,
        }),
        new reusable.ReusedCommand(AddDockerImageInSnykCommand.get(), {
          'docker-image-name': 'apim-gateway',
          version: tag,
        }),
      );
    }

    steps.push(new reusable.ReusedCommand(DockerAzureLogoutCommand.get()));

    return new Job('job-build-images', OpenJdkExecutor.create(), steps); // TODO check if we can use cimg/base:stable executor
  }
}
