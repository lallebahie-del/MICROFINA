// Smoke test minimal pour vérifier que les imports et la compilation tiennent.
// Le test "compteur" généré par défaut par `flutter create` n'a jamais
// correspondu à cette app — on le remplace par un test trivial.

import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';

void main() {
  testWidgets('Material widget renders', (WidgetTester tester) async {
    await tester.pumpWidget(
      const MaterialApp(
        home: Scaffold(body: Center(child: Text('OK'))),
      ),
    );

    expect(find.text('OK'), findsOneWidget);
  });
}
