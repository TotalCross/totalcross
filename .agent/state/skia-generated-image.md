<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan Skia Generated Image

Milestone ativo: nenhum; Milestone 0 concluído. Próximo milestone: 1, mover e
dividir as fontes sem alteração intencional de comportamento.

Último commit lógico: `9bf9faeb3 refactor(vm): relocate skia sources`.

Caminhos alterados neste milestone até agora: `TotalCrossVM/CMakeLists.txt`,
`TotalCrossVM/src/nm/ui/GraphicsPrimitives.h`,
`TotalCrossVM/src/nm/ui/PalmFont_c.h`,
`TotalCrossVM/src/nm/ui/android/gfx_Graphics_c.h`,
`TotalCrossVM/src/nm/ui/font_Font.c`,
`TotalCrossVM/src/nm/ui/image_Image.c`,
`TotalCrossVM/src/nm/ui/linux/gfx_Graphics_c.h`, e os arquivos relocados
`TotalCrossVM/src/nm/ui/skia/skia.h` e `skia.cpp`.

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

Validação executada: download/listagem/extração do anexo, inspeção da fonte,
reprodução baseline no Android e `git diff --check` antes do commit de
relocação. O build estrutural do Milestone 1 ainda não foi executado; nenhum
teste ou validação de milestone posterior foi executado.

Bloqueios: nenhum bloqueio técnico para o Milestone 1. O arquivo de estado
solicitado no prompt e o nome originalmente citado no plano estavam ausentes;
este arquivo segue o nome canônico definido na seção “Working Set and Resume
Protocol” do ExecPlan.

Próxima ação: dividir `skia.cpp` em unidades lógicas e separar os blocos de
`GraphicsPrimitives_c.h`, depois executar somente o build estrutural previsto
para fechar o Milestone 1.
