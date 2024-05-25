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
import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { GioConfirmDialogComponent, GioConfirmDialogData } from '@gravitee/ui-particles-angular';
import { FormControl, FormGroup } from '@angular/forms';
import { BehaviorSubject, Observable, of } from 'rxjs';
import { catchError, filter, map, switchMap } from 'rxjs/operators';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { MatDialog } from '@angular/material/dialog';

import { CategoryService } from '../../../../../services-ngx/category.service';
import { EnvironmentSettingsService } from '../../../../../services-ngx/environment-settings.service';
import { SnackBarService } from '../../../../../services-ngx/snack-bar.service';
import { Category } from '../../../../../entities/category/Category';
import { GioPermissionService } from '../../../../../shared/components/gio-permission/gio-permission.service';

@Component({
  selector: 'app-categories-ngx',
  templateUrl: './categories-ngx.component.html',
  styleUrl: './categories-ngx.component.scss',
})
export class CategoriesNgxComponent implements OnInit {
  categoriesDS$: Observable<Category[]> = of([]);
  displayedColumns: string[] = ['picture', 'name', 'description', 'count', 'actions'];
  portalSettingsForm: FormGroup<{ enabled: FormControl<boolean> }>;

  private categoryList = new BehaviorSubject(1);
  private destroyRef = inject(DestroyRef);

  constructor(
    private categoryService: CategoryService,
    private environmentSettingsService: EnvironmentSettingsService,
    private readonly snackBarService: SnackBarService,
    private matDialog: MatDialog,
    private readonly permissionService: GioPermissionService,
  ) {}

  ngOnInit(): void {
    // If read-only, remove actions column
    if (!this.permissionService.hasAnyMatching(['environment-category-u', 'environment-category-d'])) {
      this.displayedColumns.pop();
    }

    this.portalSettingsForm = new FormGroup<{ enabled: FormControl<boolean> }>({
      enabled: new FormControl(this.environmentSettingsService.getSnapshot().portal.apis.categoryMode.enabled),
    });
    this.categoriesDS$ = this.categoryList.pipe(
      switchMap((_) => this.categoryService.list(true)),
      map((categories) => categories.sort((a, b) => a.order - b.order)),
      catchError((_) => of([])),
    );
  }

  deleteCategory(category: Category) {
    this.matDialog
      .open<GioConfirmDialogComponent, GioConfirmDialogData, boolean>(GioConfirmDialogComponent, {
        data: {
          title: 'Delete Category',
          content: `Are you sure you want to delete the category '${category.name}'?`,
          confirmButton: 'Delete',
        },
        role: 'alertdialog',
        id: 'confirmDialog',
      })
      .afterClosed()
      .pipe(
        filter((confirmed) => confirmed),
        switchMap((_) => this.categoryService.delete(category.id)),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (_) => {
          this.snackBarService.success(`'${category.name}' deleted successfully`);
          this.categoryList.next(1);
        },
        error: ({ error }) => this.snackBarService.error(error?.message ?? 'Error during deletion'),
      });
  }

  moveUp(index: number, categoryList: Category[]) {
    this.switchCategoryPlaces(categoryList[index], categoryList[index - 1]);
  }

  moveDown(index: number, categoryList: Category[]) {
    this.switchCategoryPlaces(categoryList[index + 1], categoryList[index]);
  }

  showCategory(category: Category) {
    const categoryToUpdate: Category = { ...category, hidden: false };
    this.categoryService
      .update(categoryToUpdate)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (_) => {
          this.snackBarService.success(`Category [${category.name}] is now shown`);
          this.categoryList.next(1);
        },
        error: ({ error }) => this.snackBarService.error(error.message),
      });
  }
  hideCategory(category: Category) {
    const categoryToUpdate: Category = { ...category, hidden: true };
    this.categoryService
      .update(categoryToUpdate)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (_) => {
          this.snackBarService.success(`Category [${category.name}] is now hidden`);
          this.categoryList.next(1);
        },
        error: ({ error }) => this.snackBarService.error(error.message),
      });
  }

  private switchCategoryPlaces(categoryToMoveUp: Category, categoryToMoveDown: Category): void {
    categoryToMoveDown.order += 1;
    categoryToMoveUp.order -= 1;

    this.categoryService
      .updateList([categoryToMoveUp, categoryToMoveDown])
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (_) => {
          this.snackBarService.success(`Category order has been changed`);
          this.categoryList.next(1);
        },
        error: ({ error }) => this.snackBarService.error(error.message),
      });
  }
}
