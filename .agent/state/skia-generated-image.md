<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan Skia Generated Image

Milestone ativo: nenhum; Milestone 0 concluído. Próximo milestone: 1, mover e
dividir as fontes sem alteração intencional de comportamento.

Último commit lógico: `docs(vm): record skia image baseline` (commit atual).

Caminhos alterados neste milestone: `.agent/exec-plan-skia-generated-image.md`,
`.agent/state/skia-generated-image.md` e
`.agent/evidence/skia-generated-image.jsonl`.

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

Validação executada: somente download/listagem/extração do anexo, inspeção da
fonte, reprodução baseline no Android e `git diff --check`. Não foram
executados build, testes ou validações do Milestone 1 ou posteriores.

Bloqueios: nenhum bloqueio técnico para Milestone 0. O arquivo de estado
solicitado no prompt e o nome originalmente citado no plano estavam ausentes;
este arquivo segue o nome canônico definido na seção “Working Set and Resume
Protocol” do ExecPlan.

Próxima ação: somente quando solicitado, iniciar Milestone 1 com a relocação e
divisão mecânica de Skia e dos headers de primitivas.
