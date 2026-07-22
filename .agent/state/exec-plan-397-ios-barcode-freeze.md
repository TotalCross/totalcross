<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan 397

Milestone ativo: nenhum. O milestone 7 está concluído com a validação de archive iPhoneOS; a evidência em aparelho físico continua dispensada por direção explícita do usuário.

Último commit lógico: `test(scanner,ios): record successful device archive`.

Caminhos ativos: `TotalCrossVM/src/nm/ui/darwin/mainview.m`, `TotalCrossVM/src/nm/ui/darwin/barcode_session_state.h`, `TotalCrossVM/src/nm/ui/darwin/barcode_session_state_test.c`, `.agent/exec-plan-397-ios-barcode-freeze.md`, `.agent/state/exec-plan-397-ios-barcode-freeze.md` e `.agent/evidence/397-app-freezes-on-readbarcode.jsonl`.

Próxima ação concreta: parar no limite do ExecPlan solicitado. Uma retomada posterior pode elaborar o relatório editorial final, sem reexecutar a validação do milestone 7.

Validação concluída: o teste C de estado passou e `git diff --check` passou. A busca estrutural confirmou `dispatch_async` para o início, `dispatch_semaphore_wait` para a thread chamadora e ausência de `Sleep(100)` no caminho de scanner. O fluxo iPhoneOS de CMake, CocoaPods, patch e `xcodebuild ... archive` completou com êxito e gerou o archive e o result bundle locais. Por direção explícita do usuário, testes e reprodução em aparelho físico foram dispensados.

Decisões ativas: a chamada da VM aguarda `completionSignal`; a UI inicia por `dispatch_async`; chamada na main thread retorna `***`; setup expira em 15 segundos e scan ativo não expira automaticamente. UTF-8 e lifecycle ainda pertencem ao milestone 6.

Bloqueio: nenhum para o milestone 7. A primeira tentativa de simulador falhou por falta de `axtls/axtls_pbkdf2.h`; após a atualização de `totalcross-depot-tools`, o header iOS arm64 foi resolvido. A primeira integração do CocoaPods não aplicou os headers do alvo CMake por atributos de projeto não reconhecidos pela versão local de `xcodeproj`; uma segunda execução de `pod install`, seguida do patch previsto, aplicou as configurações e o archive passou. A ausência de dispositivo físico permanece dispensada pelo usuário.

Arquivos deliberadamente fora de escopo: `TotalCrossSDK`, correção de dependência axtls/crypto, arquivos Xcode/CocoaPods gerados pelo build, dependências geradas e artefatos locais.

Comando de retomada: `git switch fix/397-app-freezes-on-readbarcode && git status --short -- TotalCrossVM/src/nm/ui/darwin/mainview.m TotalCrossVM/src/nm/ui/darwin/mainview.h TotalCrossVM/src/nm/io/device/scanner/zxing.c .agent/exec-plan-397-ios-barcode-freeze.md .agent/state/exec-plan-397-ios-barcode-freeze.md .agent/evidence/397-app-freezes-on-readbarcode.jsonl`.
