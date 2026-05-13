import { Component, OnInit, signal } from '@angular/core';
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
export class LoginComponent implements OnInit {
  username  = '';
  password  = '';
  loading   = signal(false);
  errorMsg  = signal<string | null>(null);
  showPass  = signal(false);

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    // S'assure que les champs sont vides à chaque arrivée sur la page
    // (utile après une déconnexion : on évite que le navigateur ait gardé
    // les valeurs précédentes au niveau du modèle Angular).
    this.username = '';
    this.password = '';
  }

  onSubmit(): void {
    if (!this.username.trim() || !this.password.trim()) {
      this.errorMsg.set('Veuillez saisir votre login et mot de passe.');
      return;
    }

    this.loading.set(true);
    this.errorMsg.set(null);

    this.authService.login(this.username.trim(), this.password.trim()).subscribe({
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
