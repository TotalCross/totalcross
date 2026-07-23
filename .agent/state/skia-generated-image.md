<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan Skia Generated Image

Milestone ativo: nenhum; Milestone 1 concluído. Próximo milestone: 2, adicionar
superfícies Skia próprias das imagens e encaminhar o desenho para o canvas
selecionado.

Último commit lógico: `refactor(vm): split skia rendering sources` (commit
atual).

Caminhos alterados neste milestone: `TotalCrossVM/CMakeLists.txt`,
`TotalCrossVM/src/nm/ui/GraphicsPrimitives.h`,
`TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h`,
`TotalCrossVM/src/nm/ui/GraphicsPrimitivesSkia_c.h`,
`TotalCrossVM/src/nm/ui/GraphicsPrimitivesText_c.h`,
`TotalCrossVM/src/nm/ui/GraphicsPrimitivesShapes_c.h`,
`TotalCrossVM/src/nm/ui/GraphicsPrimitivesScreen_c.h`,
`TotalCrossVM/src/nm/ui/PalmFont_c.h`,
`TotalCrossVM/src/nm/ui/android/gfx_Graphics_c.h`,
`TotalCrossVM/src/nm/ui/font_Font.c`,
`TotalCrossVM/src/nm/ui/image_Image.c`,
`TotalCrossVM/src/nm/ui/linux/gfx_Graphics_c.h`, e os arquivos relocados
`TotalCrossVM/src/nm/ui/skia/skia.h` e `skia.cpp`.
Também foram criados `skia_internal.h`, `skia_surface.cpp` e
`skia_primitives.cpp`.

Anexo inspecionado: `Tcsort.zip`, baixado de
`https://github.com/user-attachments/files/30019877/Tcsort.zip`, SHA-256
`de4df098fc00ff35b419ff616d3a0172f4eb2e17ae7c72b8808a477037c8b641`. A lista
contém `Tcsort.apk` (22480377 bytes) e `tcsort.java` (1770 bytes). A fonte
cria `MonoImage(576, 576)`, chama `getGraphics()`, preenche branco, desenha
uma borda preta em `(10, 10)` com tamanho `556x556`, grava `nome.png` e abre o
arquivo com `Vm.exec("viewer", ...)`.

Baseline Android: o APK instalou e iniciou no emulador `emulator-5554` com
`totalcross.appphdb/.Loader`. O arquivo `/data/user/0/totalcross.appphdb/nome.png`
foi extraído para `/tmp/tcsort-baseline-nome.png`; é PNG RGBA 576x576, e o
RGBA decodificado teve `nonzero_bytes=0`, confirmando a saída transparente e
vazia. A captura de tela, o logcat e o arquivo gerado permanecem em `/tmp`;
os caminhos e hashes estão no JSONL de evidência.

Validação do milestone: `cmake -S TotalCrossVM -B build-skia-structure
-DCMAKE_BUILD_TYPE=Release -G Ninja` passou; `ninja -C build-skia-structure`
passou e gerou `libtcvm.dylib`; em `TotalCrossSDK`,
`./gradlew-agent clean dist --warning-mode=none --console=plain` passou com 0
 tarefas falhas; `ctest --test-dir build-skia-structure --output-on-failure`
retornou sucesso, mas sem testes registrados. Logs do CMake/Ninja estão em
`/tmp/skia-m1-*.log`; o resumo completo do SDK está em
`TotalCrossSDK/agent-logs/20260723-030119-clean-agent.log`.

Bloqueios: nenhum bloqueio técnico para o Milestone 1. O arquivo de estado
solicitado no prompt e o nome originalmente citado no plano estavam ausentes;
este arquivo segue o nome canônico definido na seção “Working Set and Resume
Protocol” do ExecPlan.

Próxima ação: iniciar somente quando solicitado o Milestone 2, adicionando
superfícies Skia próprias das imagens; não executar ainda suas validações.
