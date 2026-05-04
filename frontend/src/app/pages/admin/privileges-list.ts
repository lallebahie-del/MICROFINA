import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService, Privilege } from '../../services/admin.service';

@Component({
  selector: 'app-privileges-list',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './privileges-list.html'
})
export class PrivilegesListComponent implements OnInit {
  privileges: Privilege[] = [];
  loading = false;

  constructor(private svc: AdminService) {}

  ngOnInit(): void {
    this.loading = true;
    this.svc.getPrivileges().subscribe({
      next: data => { this.privileges = data; this.loading = false; },
      error: () => { this.loading = false; }
    });
  }
}
