<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan 397

Milestone ativo: nenhum; milestone 1 concluído. Próximo milestone: 2, introduzir estado explícito por sessão e um único caminho idempotente de conclusão.

Último commit lógico: `test(scanner,ios): capture barcode freeze state` (o identificador pode ser obtido com `git rev-parse HEAD`).

Caminhos ativos: `TotalCrossVM/src/nm/ui/darwin/mainview.m`, `.agent/exec-plan-397-ios-barcode-freeze.md`, `.agent/state/exec-plan-397-ios-barcode-freeze.md` e `.agent/evidence/397-app-freezes-on-readbarcode.jsonl`.

Próxima ação concreta: iniciar o milestone 2, inspecionando o contrato de erro atual entre `Scanner.java`, `zxing.c` e `mainview.m` antes de substituir `callingBarcode` por estado de sessão explícito. Não iniciar o milestone 3 nem alterar a apresentação da UI.

Validação concluída: `git diff --check` passou. `xcrun xctrace list devices` confirmou que o ambiente contém apenas o host e simuladores; a busca em `TotalCrossVM/xcode/Debug-iphonesimulator` não encontrou um aplicativo executável para reprodução. Por direção explícita do usuário, testes e reprodução em aparelho físico foram dispensados. Nenhum build Xcode ou teste automatizado foi executado.

Decisões ativas: manter os logs diagnósticos até a captura física; eles usam identificador monotônico e registram presença de objetos/estado, sem registrar conteúdo de código de barras. O milestone 1 está concluído por análise estática e instrumentação; não há alegação de comportamento em dispositivo físico.

Bloqueio: nenhum para o milestone 1. A ausência de dispositivo físico permanece uma limitação registrada, mas a reprodução foi dispensada pelo usuário.

Arquivos deliberadamente fora de escopo: `TotalCrossSDK`, `TotalCrossVM/src/nm/io/device/scanner/zxing.c`, `TotalCrossVM/src/event/darwin`, `TotalCrossVM/xcode`, dependências geradas e artefatos locais.

Comando de retomada: `git switch fix/397-app-freezes-on-readbarcode && git status --short -- TotalCrossVM/src/nm/ui/darwin/mainview.m TotalCrossVM/src/nm/io/device/scanner/zxing.c .agent/exec-plan-397-ios-barcode-freeze.md .agent/state/exec-plan-397-ios-barcode-freeze.md .agent/evidence/397-app-freezes-on-readbarcode.jsonl`.
