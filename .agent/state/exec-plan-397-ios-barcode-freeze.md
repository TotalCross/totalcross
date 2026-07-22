<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan 397

Milestone ativo: 1, reprodução e fatos observáveis. Slice concluído: instrumentação diagnóstica limitada. Slice pendente: reprodução física e captura dos stacks/estados.

Último commit lógico: `test(scanner,ios): capture barcode freeze state` (o identificador pode ser obtido com `git rev-parse HEAD`).

Caminhos ativos: `TotalCrossVM/src/nm/ui/darwin/mainview.m`, `.agent/exec-plan-397-ios-barcode-freeze.md`, `.agent/state/exec-plan-397-ios-barcode-freeze.md` e `.agent/evidence/397-app-freezes-on-readbarcode.jsonl`.

Próxima ação concreta: conectar um iPad físico (preferencialmente 8ª geração com iOS 17.5, ou registrar o modelo/versão alternativo), implantar o aplicativo mínimo que chama `Scanner.readBarcode("")`, reproduzir o travamento e salvar os logs `TCBarcode` mais os stacks da thread principal e da thread chamadora da VM. Não alterar o comportamento do scanner antes dessa captura.

Validação concluída: `git diff --check` passou. `xcrun xctrace list devices` confirmou que o ambiente contém apenas o host e simuladores; a busca em `TotalCrossVM/xcode/Debug-iphonesimulator` não encontrou um aplicativo executável para reprodução. Nenhum build Xcode, teste automatizado ou teste de dispositivo foi executado, pois pertencem a evidência posterior ou requerem o dispositivo ausente.

Decisões ativas: manter os logs diagnósticos até a captura física; eles usam identificador monotônico e registram presença de objetos/estado, sem registrar conteúdo de código de barras. Milestones 2 a 7 permanecem fora de escopo.

Bloqueio: não há dispositivo iOS físico conectado nem aplicativo mínimo implantado disponível neste workspace; um simulador não comprova a captura física requerida pelo milestone.

Arquivos deliberadamente fora de escopo: `TotalCrossSDK`, `TotalCrossVM/src/nm/io/device/scanner/zxing.c`, `TotalCrossVM/src/event/darwin`, `TotalCrossVM/xcode`, dependências geradas e artefatos locais.

Comando de retomada: `git switch fix/397-app-freezes-on-readbarcode && git status --short -- TotalCrossVM/src/nm/ui/darwin/mainview.m .agent/exec-plan-397-ios-barcode-freeze.md .agent/state/exec-plan-397-ios-barcode-freeze.md .agent/evidence/397-app-freezes-on-readbarcode.jsonl`.
