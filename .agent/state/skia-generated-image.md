<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan Skia Generated Image

Milestone ativo: nenhum; a matriz final foi concluída em 2026-07-23.

O caminho solicitado no prompt, `.agent/state/exec-plan-skia-generated-image.md`,
continua ausente. Este arquivo é o estado canônico referenciado pelo próprio
ExecPlan.

Commits lógicos desta retomada:

- `90109883a fix(vm): correct skia pixel color order`
- `84b618d93 test(vm): harden generated image smoke test`

Resultado da matriz final, executada a partir da mesma revisão do smoke:

- Java SE: passou; checker `java-byte-array`, exit 0;
- macOS arm64: passou; checker `native-skia`, exit 0;
- Android `emulator-5554`: passou; APK implantado, checker `native-skia` passou.

No Android, o app foi parado externamente depois da coleta. `MainWindow.exit`
podia correr contra o teardown assíncrono do loader e abortar ao bloquear um
mutex destruído; os artefatos já estavam completos e o novo procedimento não
produziu novo crash. No macOS headless, o JSON informa `Settings.platform` como
`Linux` por causa da configuração Ninja/CMake, embora o binário seja macOS
arm64 e use `native-skia`. Java SE emitiu apenas o erro não fatal de telemetria
opcional `NoClassDefFoundError: net/harawata/appdirs/AppDirsFactory`.

Validações executadas nesta retomada:

- build nativo macOS arm64 com CMake/Ninja: passou;
- `:tcvm:fetchNativeDependencies`: passou;
- `:tcvm:externalNativeBuildCleanRelease`: passou;
- `:app:assembleStandardRelease`: passou;
- compilação/deployment/runtime/checker Java SE: passou;
- compilação/deployment/runtime/checker macOS: passou;
- compilação/deployment/runtime/checker Android: passou;
- `git diff --check`: passou.

Logs e artefatos detalhados estão em `.agent/evidence/skia-generated-image.jsonl`.
Não foram executadas validações de milestones posteriores.
