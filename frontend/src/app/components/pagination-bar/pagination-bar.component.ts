import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DEFAULT_LIST_PAGE_SIZE, listClampPage, listPageIndices, listTotalPages } from '../../shared/list-pagination';

@Component({
  selector: 'app-pagination-bar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pagination-bar.component.html',
  styleUrl: './pagination-bar.component.css'
})
export class PaginationBarComponent {
  /** Index de page (0-based) */
  @Input() page = 0;
  @Input() totalElements = 0;
  @Input() pageSize = DEFAULT_LIST_PAGE_SIZE;

  @Output() pageChange = new EventEmitter<number>();

  get totalPages(): number {
    return listTotalPages(this.totalElements, this.pageSize);
  }

  get pageIndices(): number[] {
    return listPageIndices(this.totalElements, this.pageSize);
  }

  get displayTotalPages(): number {
    return Math.max(1, this.totalPages);
  }

  emitPage(p: number): void {
    const clamped = listClampPage(p, this.totalElements, this.pageSize);
    this.pageChange.emit(clamped);
  }
}
