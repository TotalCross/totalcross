<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan 397

Milestone ativo: nenhum; milestones 1 a 3 concluídos. Próximo milestone: 4, tornar a permissão e a configuração da captura assíncronas.

Último commit lógico: `fix(scanner,ios): present scanner in active scene` (o identificador pode ser obtido com `git rev-parse HEAD`).

Caminhos ativos: `TotalCrossVM/src/nm/ui/darwin/mainview.m`, `TotalCrossVM/src/nm/ui/darwin/barcode_session_state.h`, `TotalCrossVM/src/nm/ui/darwin/barcode_session_state_test.c`, `.agent/exec-plan-397-ios-barcode-freeze.md`, `.agent/state/exec-plan-397-ios-barcode-freeze.md` e `.agent/evidence/397-app-freezes-on-readbarcode.jsonl`.

Próxima ação concreta: iniciar o milestone 4, separando permissão e configuração de AVFoundation da fila principal e roteando cada falha pelo finalizador existente. Não iniciar o milestone 5 nem substituir ainda a espera por polling.

Validação concluída: `clang -std=c11 -Wall -Wextra -Werror -I TotalCrossVM/src/nm/ui/darwin TotalCrossVM/src/nm/ui/darwin/barcode_session_state_test.c -o /tmp/tc-barcode-session-state-test && /tmp/tc-barcode-session-state-test` passou. `git diff --check` passou. A busca estrutural confirmou que `mountBarCodeWindow` não existe mais e que `barwindow` não é usado para apresentação. A sintaxe de `mainview.m` não pôde ser verificada isoladamente: a compilação parou no cabeçalho CocoaPods ausente `YTPlayerView.h`, antes do arquivo modificado. Por direção explícita do usuário, testes e reprodução em aparelho físico foram dispensados. Nenhum build Xcode foi executado.

Decisões ativas: manter os logs diagnósticos até a captura física; eles usam identificador monotônico e registram presença de objetos/estado, sem registrar conteúdo de código de barras. O estado de sessão fica em `TCBarcodeSession`; o finalizador rejeita gerações obsoletas e chamadas repetidas, faz a limpeza sem manter o lock e sinaliza a conclusão uma vez. A apresentação é o overlay `_barcodeOverlay` na view de `MainViewController`; a espera por polling e o `dispatch_sync` permanecem intencionalmente para o milestone 5.

Bloqueio: nenhum para o milestone 3. A ausência de `YTPlayerView.h` bloqueia apenas a checagem sintática isolada; o teste C puro do estado passou. A ausência de dispositivo físico permanece uma limitação registrada, mas a reprodução foi dispensada pelo usuário.

Arquivos deliberadamente fora de escopo: `TotalCrossSDK`, `TotalCrossVM/src/nm/io/device/scanner/zxing.c`, `TotalCrossVM/src/event/darwin`, `TotalCrossVM/xcode`, dependências geradas e artefatos locais.

Comando de retomada: `git switch fix/397-app-freezes-on-readbarcode && git status --short -- TotalCrossVM/src/nm/ui/darwin/mainview.m TotalCrossVM/src/nm/ui/darwin/mainview.h TotalCrossVM/src/nm/io/device/scanner/zxing.c .agent/exec-plan-397-ios-barcode-freeze.md .agent/state/exec-plan-397-ios-barcode-freeze.md .agent/evidence/397-app-freezes-on-readbarcode.jsonl`.
