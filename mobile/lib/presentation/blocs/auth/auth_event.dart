import 'package:equatable/equatable.dart';

abstract class AuthEvent extends Equatable {
  const AuthEvent();

  @override
  List<Object?> get props => [];
}

class LoginRequested extends AuthEvent {
  final String phone;
  final String pin;

  const LoginRequested({required this.phone, required this.pin});

  @override
  List<Object?> get props => [phone, pin];
}

class RegisterRequested extends AuthEvent {
  final String phone;
  final String pin;
  final String nomComplet;
  final String? email;
  final Map<String, String>? address;

  const RegisterRequested({
    required this.phone,
    required this.pin,
    required this.nomComplet,
    this.email,
    this.address,
  });

  @override
  List<Object?> get props => [phone, pin, nomComplet, email, address];
}

class LogoutRequested extends AuthEvent {}

class AppStarted extends AuthEvent {}

/// Session révoquée côté réseau (ex. refresh token refusé) : forcer l’UI hors connexion.
class RemoteSessionInvalidated extends AuthEvent {}
