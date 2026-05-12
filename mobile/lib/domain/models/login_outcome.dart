sealed class LoginOutcome {
  const LoginOutcome();
}

final class LoginSuccess extends LoginOutcome {
  final String token;

  const LoginSuccess(this.token);
}

enum LoginFailureKind {
  invalidCredentials,
  network,
  unknown,
}

final class LoginFailure extends LoginOutcome {
  final LoginFailureKind kind;

  const LoginFailure(this.kind);
}
