import { Component, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, CommonModule],
  templateUrl: './login.html',
  styleUrl:    './login.css'
})
export class LoginComponent {
  username  = '';
  password  = '';
  loading   = signal(false);
  errorMsg  = signal<string | null>(null);
  showPass  = signal(false);

  constructor(private authService: AuthService) {}

  onSubmit(): void {
    if (!this.username.trim() || !this.password.trim()) {
      this.errorMsg.set('Veuillez saisir votre login et mot de passe.');
      return;
    }

    this.loading.set(true);
    this.errorMsg.set(null);

    this.authService.login(this.username.trim(), this.password).subscribe({
      next:  ()    => this.loading.set(false),
      error: (err) => {
        this.loading.set(false);
        this.errorMsg.set(err.message);
      }
    });
  }

  toggleShowPass(): void {
    this.showPass.set(!this.showPass());
  }
}
