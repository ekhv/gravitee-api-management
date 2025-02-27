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

import { ComponentHarness } from '@angular/cdk/testing';
import { MatAccordionHarness } from '@angular/material/expansion/testing';
import { MatTableHarness } from '@angular/material/table/testing';

export class ApiScoreHarness extends ComponentHarness {
  public static readonly hostSelector = 'app-api-score';

  private evaluateButtonLocator = this.locatorFor("[data-testid='evaluate-button']");
  private accordionLocator = this.locatorForAll(MatAccordionHarness);
  private tablesLocator = this.locatorForOptional(MatTableHarness);

  public getAccordion = async () => {
    return await this.accordionLocator();
  };

  public clickEvaluate = () => this.evaluateButtonLocator().then((button) => button.click());

  public getTables = async () => {
    return await this.tablesLocator();
  };
}
