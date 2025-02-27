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
import { Injectable } from '@angular/core';

import { GioPermissionService } from '../../shared/components/gio-permission/gio-permission.service';

export interface MenuItem {
  icon?: string;
  routerLink?: string;
  displayName: string;
  children?: MenuItem[];
}

export interface GroupItem {
  title: string;
  subtitle?: string;
  items: MenuItem[];
}

@Injectable({
  providedIn: 'root',
})
export class PortalNavigationService {
  constructor(private readonly permissionService: GioPermissionService) {}

  public getCustomizationRoutes(): GroupItem {
    const items: MenuItem[] = [];

    const customization: MenuItem = {
      displayName: 'Customization',
      routerLink: 'customization',
      icon: 'gio:palette',
      children: [],
    };

    if (this.permissionService.hasAnyMatching(['environment-settings-r', 'environment-settings-u'])) {
      customization.children.push({
        displayName: 'Top Bar',
        routerLink: 'top-bar',
        icon: 'gio:top-bar',
      });
    }
    if (this.permissionService.hasAnyMatching(['environment-theme-r', 'environment-theme-u'])) {
      customization.children.push({
        displayName: 'Theme',
        routerLink: 'theme',
        icon: 'gio:color-picker',
      });
    }
    if (this.permissionService.hasAnyMatching(['environment-settings-r', 'environment-settings-u'])) {
      customization.children.push({
        displayName: 'Banner',
        routerLink: 'banner',
        icon: 'gio:chat-lines',
      });
    }

    if (customization.children.length) {
      items.push(customization);
    }

    return {
      title: 'Customization',
      subtitle: 'Customize the look, feel, and behavior of the new Developer Portal.',
      items,
    };
  }
}
