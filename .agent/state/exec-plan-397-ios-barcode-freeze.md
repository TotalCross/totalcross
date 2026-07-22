<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan 397

Milestone ativo: nenhum; milestones 1 a 6 concluídos. Próximo milestone: 7, encerrar com evidência de artefato e dispositivo.

Último commit lógico: `fix(scanner,ios): clean up repeated barcode scans` (o identificador pode ser obtido com `git rev-parse HEAD`).

Caminhos ativos: `TotalCrossVM/src/nm/ui/darwin/mainview.m`, `TotalCrossVM/src/nm/ui/darwin/barcode_session_state.h`, `TotalCrossVM/src/nm/ui/darwin/barcode_session_state_test.c`, `.agent/exec-plan-397-ios-barcode-freeze.md`, `.agent/state/exec-plan-397-ios-barcode-freeze.md` e `.agent/evidence/397-app-freezes-on-readbarcode.jsonl`.

Próxima ação concreta: iniciar o milestone 7 somente quando houver autorização para build/evidência final; não executar validações finais agora.

Validação concluída: o teste C de estado passou e `git diff --check` passou. A busca estrutural confirmou `dispatch_async` para o início, `dispatch_semaphore_wait` para a thread chamadora e ausência de `Sleep(100)` no caminho de scanner. A sintaxe de `mainview.m` não pôde ser verificada isoladamente: a compilação para no cabeçalho CocoaPods ausente `YTPlayerView.h`, antes do arquivo modificado. Por direção explícita do usuário, testes e reprodução em aparelho físico foram dispensados. Nenhum build Xcode foi executado.

Decisões ativas: a chamada da VM aguarda `completionSignal`; a UI inicia por `dispatch_async`; chamada na main thread retorna `***`; setup expira em 15 segundos e scan ativo não expira automaticamente. UTF-8 e lifecycle ainda pertencem ao milestone 6.

Bloqueio: nenhum para o milestone 6. A ausência de `YTPlayerView.h` bloqueia apenas a checagem sintática isolada; o teste C puro do estado passou. A ausência de dispositivo físico permanece uma limitação registrada, mas a reprodução foi dispensada pelo usuário.

Arquivos deliberadamente fora de escopo: `TotalCrossSDK`, `TotalCrossVM/src/nm/io/device/scanner/zxing.c`, `TotalCrossVM/src/event/darwin`, `TotalCrossVM/xcode`, dependências geradas e artefatos locais.

Comando de retomada: `git switch fix/397-app-freezes-on-readbarcode && git status --short -- TotalCrossVM/src/nm/ui/darwin/mainview.m TotalCrossVM/src/nm/ui/darwin/mainview.h TotalCrossVM/src/nm/io/device/scanner/zxing.c .agent/exec-plan-397-ios-barcode-freeze.md .agent/state/exec-plan-397-ios-barcode-freeze.md .agent/evidence/397-app-freezes-on-readbarcode.jsonl`.
