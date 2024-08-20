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
import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PortalCustomizationComponent } from './portal-customization.component';

import { GioTestingModule } from '../../shared/testing';
import { GioTestingPermissionProvider } from '../../shared/components/gio-permission/gio-permission.service';

describe('PortalCustomizationComponent', () => {
  let component: PortalCustomizationComponent;
  let fixture: ComponentFixture<PortalCustomizationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PortalCustomizationComponent, GioTestingModule],
      providers: [
        {
          provide: GioTestingPermissionProvider,
          useValue: ['environment-theme-u'],
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(PortalCustomizationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
