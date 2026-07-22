<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan 397

Milestone ativo: nenhum; milestones 1 a 4 concluídos. Próximo milestone: 5, substituir polling e o despacho inseguro à fila principal.

Último commit lógico: `fix(scanner,ios): handle camera setup asynchronously` (o identificador pode ser obtido com `git rev-parse HEAD`).

Caminhos ativos: `TotalCrossVM/src/nm/ui/darwin/mainview.m`, `TotalCrossVM/src/nm/ui/darwin/barcode_session_state.h`, `TotalCrossVM/src/nm/ui/darwin/barcode_session_state_test.c`, `.agent/exec-plan-397-ios-barcode-freeze.md`, `.agent/state/exec-plan-397-ios-barcode-freeze.md` e `.agent/evidence/397-app-freezes-on-readbarcode.jsonl`.

Próxima ação concreta: iniciar o milestone 5, substituindo o `dispatch_sync` cego e a espera com `Sleep(100)` pelo sinal de conclusão da sessão. Não iniciar o milestone 6 nem alterar a codificação UTF-8/lifecycle.

Validação concluída: o teste C de estado passou e `git diff --check` passou. A busca estrutural confirmou `requestAccessForMediaType`, validação de entrada/saída, interseção de metadata e `startRunning` na fila serial da sessão. A sintaxe de `mainview.m` não pôde ser verificada isoladamente: a compilação para no cabeçalho CocoaPods ausente `YTPlayerView.h`, antes do arquivo modificado. Por direção explícita do usuário, testes e reprodução em aparelho físico foram dispensados. Nenhum build Xcode foi executado.

Decisões ativas: `TCBarcodeSession.captureQueue` contém o trabalho AVFoundation; negação de permissão, modo desconhecido, câmera indisponível, input/output inválido e metadata vazio finalizam a sessão. A apresentação é o overlay `_barcodeOverlay`; a espera por polling e o `dispatch_sync` permanecem intencionalmente para o milestone 5.

Bloqueio: nenhum para o milestone 4. A ausência de `YTPlayerView.h` bloqueia apenas a checagem sintática isolada; o teste C puro do estado passou. A ausência de dispositivo físico permanece uma limitação registrada, mas a reprodução foi dispensada pelo usuário.

Arquivos deliberadamente fora de escopo: `TotalCrossSDK`, `TotalCrossVM/src/nm/io/device/scanner/zxing.c`, `TotalCrossVM/src/event/darwin`, `TotalCrossVM/xcode`, dependências geradas e artefatos locais.

Comando de retomada: `git switch fix/397-app-freezes-on-readbarcode && git status --short -- TotalCrossVM/src/nm/ui/darwin/mainview.m TotalCrossVM/src/nm/ui/darwin/mainview.h TotalCrossVM/src/nm/io/device/scanner/zxing.c .agent/exec-plan-397-ios-barcode-freeze.md .agent/state/exec-plan-397-ios-barcode-freeze.md .agent/evidence/397-app-freezes-on-readbarcode.jsonl`.
