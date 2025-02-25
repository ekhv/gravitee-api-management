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
import { Inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { isNil } from 'lodash';

import { Constants } from '../entities/Constants';
import { AnalyticsRequestParam } from '../entities/analytics/analyticsRequestParam';
import {
  AnalyticsCountResponse,
  AnalyticsGroupByResponse,
  AnalyticsStatsResponse,
  AnalyticsTopApisResponse,
  AnalyticsV4StatsResponse,
} from '../entities/analytics/analyticsResponse';
import { AnalyticsResponseStatusRanges } from '../entities/management-api-v2/analytics/analyticsResponseStatusRanges';
import { GioChartLineData, GioChartLineOptions } from '../shared/components/gio-chart-line/gio-chart-line.component';
import { TimeRangeParams } from '../shared/utils/timeFrameRanges';
import {
  AnalyticsAverageResponseTimes,
  AnalyticsBucket,
  AnalyticsDefinitionVersion,
  AnalyticsResponseStatus,
  AnalyticsTopFailedApisRes,
  AnalyticsV4ResponseStatus,
  AnalyticsV4ResponseTimes,
  TopApplicationsByRequestsCountRes,
} from '../entities/analytics/analytics';

@Injectable({
  providedIn: 'root',
})
export class AnalyticsService {
  constructor(
    private readonly http: HttpClient,
    @Inject(Constants) private readonly constants: Constants,
  ) {}

  private mapChartName(key: string): string {
    const options = {
      'avg_response-time': 'Global latency (ms)',
      'avg_api-response-time': 'API latency (ms)',
    };
    return options[key] || key;
  }

  public createBuckets(data: AnalyticsAverageResponseTimes | AnalyticsResponseStatus): AnalyticsBucket[] {
    return data.values.map((value) => value.buckets).flat();
  }

  public createChartInput(data: AnalyticsAverageResponseTimes | AnalyticsResponseStatus): GioChartLineData[] {
    const buckets: AnalyticsBucket[] = this.createBuckets(data);
    return buckets.map(
      ({ name, data }: AnalyticsBucket): GioChartLineData => ({
        name: this.mapChartName(name),
        values: data,
      }),
    );
  }

  public createChartOptions(data: AnalyticsAverageResponseTimes | AnalyticsResponseStatus): GioChartLineOptions {
    return {
      pointStart: data.timestamp?.from,
      pointInterval: data.timestamp?.interval,
    };
  }

  getResponseStatus({ from, to, interval }: TimeRangeParams): Observable<AnalyticsResponseStatus> {
    const url = `${this.constants.env.baseURL}/analytics?type=date_histo&aggs=field:status&interval=${interval}&from=${from}&to=${to}`;
    return this.http.get<AnalyticsResponseStatus>(url);
  }

  getStats(params: AnalyticsRequestParam): Observable<AnalyticsStatsResponse> {
    const url =
      `${this.constants.env.baseURL}/analytics?type=stats` +
      `&field=${params.field}` +
      `&interval=${params.interval}` +
      `&from=${params.from}` +
      `&to=${params.to}`;
    return this.http.get<AnalyticsStatsResponse>(url);
  }

  getGroupBy(params: AnalyticsRequestParam): Observable<AnalyticsGroupByResponse> {
    const queryParams = Object.entries(params ?? [])
      .map(([key, value]) => (isNil(value) ? null : `${key}=${value}`))
      .filter((v) => v != null);

    const url = `${this.constants.env.baseURL}/analytics?type=group_by&${queryParams.join('&')}`;
    return this.http.get<AnalyticsGroupByResponse>(url);
  }

  getCount(params: AnalyticsRequestParam): Observable<AnalyticsCountResponse> {
    const url =
      `${this.constants.env.baseURL}/analytics?type=count` +
      `&field=${params.field}` +
      `&interval=${params.interval}` +
      `&from=${params.from}` +
      `&to=${params.to}`;
    return this.http.get<AnalyticsCountResponse>(url);
  }

  getV4ApiResponseStatus(from: number, to: number): Observable<AnalyticsResponseStatusRanges> {
    const url = `${this.constants.env.v2BaseURL}/analytics/response-status-ranges?from=${from}&to=${to}`;
    return this.http.get<AnalyticsResponseStatusRanges>(url);
  }

  getTopApis(from: number, to: number): Observable<AnalyticsTopApisResponse> {
    const url = `${this.constants.env.v2BaseURL}/analytics/top-hits?from=${from}&to=${to}`;
    return this.http.get<AnalyticsTopApisResponse>(url);
  }

  // TopFailedApisRes

  getTopFailedApis(from: number, to: number): Observable<AnalyticsTopFailedApisRes> {
    // TODO: uncomment/fix when BE ready:
    // const url = `${this.constants.env.v2BaseURL}/analytics/top-failed-apis?from=${from}&to=${to}`;
    // return this.http.get<AnalyticsTopFailedApisRes>(url);

    // TODO: remove when backend ready;
    return of({
      data: [
        {
          id: '9f060ca4-326b-473a-860c-a4326be73a28',
          definitionVersion: AnalyticsDefinitionVersion.V2,
          name: 'Test api',
          failedCalls: to - from,
          failedCallsRatio: 0.25,
        },
        {
          id: '28113601-5197-4815-9136-015197b81592',
          definitionVersion: AnalyticsDefinitionVersion.V4,
          name: 'brand new api',
          failedCalls: 2,
          failedCallsRatio: 0.2857142857142857,
        },
      ],
    });
  }

  getV4RequestResponseStats(from: number, to: number): Observable<AnalyticsV4StatsResponse> {
    const url = `${this.constants.env.v2BaseURL}/analytics/request-response-time?from=${from}&to=${to}`;
    return this.http.get<AnalyticsV4StatsResponse>(url);
  }

  getV4ResponseTimes({ from, to }: TimeRangeParams): Observable<AnalyticsV4ResponseTimes> {
    const url = `${this.constants.env.v2BaseURL}/analytics/response-time-over-time?from=${from}&to=${to}`;
    return this.http.get<AnalyticsV4ResponseTimes>(url);
  }

  getV4ResponseStatus({ from, to }: TimeRangeParams): Observable<AnalyticsV4ResponseStatus> {
    const url = `${this.constants.env.v2BaseURL}/analytics/response-status-overtime?from=${from}&to=${to}`;
    return this.http.get<AnalyticsV4ResponseStatus>(url);
  }

  getTopApplicationsByRequestsCount({ from, to }: TimeRangeParams): Observable<TopApplicationsByRequestsCountRes> {
    const url = `${this.constants.env.v2BaseURL}/analytics/top-apps-by-request-count?from=${from}&to=${to}`;
    return this.http.get<TopApplicationsByRequestsCountRes>(url);
  }
}
