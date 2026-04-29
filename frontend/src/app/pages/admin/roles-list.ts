import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, Role } from '../../services/admin.service';

@Component({
  selector: 'app-roles-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './roles-list.html'
})
export class RolesListComponent implements OnInit {
  roles: Role[] = [];
  loading = false;

  constructor(private svc: AdminService) {}

  ngOnInit(): void {
    this.loading = true;
    this.svc.getRoles().subscribe({
      next: data => { this.roles = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}
