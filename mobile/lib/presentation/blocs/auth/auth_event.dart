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

class LogoutRequested extends AuthEvent {}

class AppStarted extends AuthEvent {}
