import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginPageComponent {
  username = '';
  password = '';
  showPassword = false;
  rememberMe = false;
  errorMessage = '';
  loading = false;

  constructor(private authService: AuthService, private router: Router) {}

  togglePassword(): void {
    this.showPassword = !this.showPassword;
  }

  onSubmit(): void {
    this.errorMessage = '';
    this.loading = true;

    this.authService.login(this.username, this.password).subscribe({
      next: () => {
        this.loading = false;
        const redirectUrl = this.authService.getRedirectUrl();
        this.router.navigate([redirectUrl]);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage = 'Identifiants invalides ou erreur serveur.';
        console.error(err);
      }
    });
  }
}