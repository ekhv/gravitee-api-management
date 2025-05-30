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
import { Pipe, PipeTransform } from '@angular/core';

import { MultiFilter, SimpleFilter } from '../../../models';

type IterableFilter = MultiFilter | string[] | Set<number>;

const SEPARATOR = ', ';

@Pipe({ name: 'chipValue', standalone: false })
export class ChipValuePipe implements PipeTransform {
  transform(filter: SimpleFilter | IterableFilter): string {
    if (filter instanceof Set) {
      return Array.from(filter)?.join(SEPARATOR);
    }

    if (Array.isArray(filter)) {
      return filter
        .map((value) => {
          if (value.label) {
            return value.label;
          }
          return value;
        })
        ?.join(SEPARATOR);
    }

    return filter?.label;
  }
}
