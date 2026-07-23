<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan Skia Generated Image

Milestone ativo: nenhum; Milestone 4 concluído. Próximo passo: reconciliar o
ExecPlan e executar a matriz final de destinos quando solicitado.

Commits lógicos deste milestone:

- `3c20258f9 test(vm): add issue 417 image smoke test`

Arquivos principais alterados: `tests/smoke/issue-417-generated-image/Tcsort.java`,
`check_result.py` e `README.md`.

Entregue no Milestone 4:

- fonte `Tcsort.java` reduzida, mas fiel ao fluxo `MonoImage(576, 576)`,
  `getGraphics()`, preenchimento branco, borda preta e `createPng()` em
  `nome.png`;
- asserções determinísticas de quatro pixels selecionados e uma linha RGBA;
- `issue-417-result.json` com plataforma, caminho de implementação, dimensões,
  pixels esperados/observados, tamanho e CRC32 do PNG;
- `check_result.py` sem dependências externas, validando JSON e estrutura PNG;
- instruções de compilação/deployment sem depender novamente do ZIP original.

Validação executada:

- `javac -Xlint:none -cp TotalCrossSDK/build/libs/totalcross-sdk-7.2.2.jar:TotalCrossSDK/build/libs/tcui-7.2.2.jar -d <temp> tests/smoke/issue-417-generated-image/Tcsort.java`: passou;
- `python3 -m py_compile tests/smoke/issue-417-generated-image/check_result.py`: passou;
- `git diff --check`: passou.

Não foi executado deployment/runtime do smoke test neste milestone. O baseline
Android transparente derivado do anexo já está registrado no Milestone 0; a
execução do teste corrigido em Java SE, macOS e Android pertence à matriz final
e não foi iniciada.

Não foram executadas validações posteriores: SDK, Java SE, macOS/Android
deployment ou a matriz final.

Bloqueios: nenhum bloqueio técnico. O caminho de estado solicitado no prompt,
`.agent/state/exec-plan-skia-generated-image.md`, continua ausente; este é o
estado canônico definido pelo ExecPlan.

Próxima ação: reconciliar o relatório editorial e, somente quando solicitado,
executar a matriz final de Java SE, macOS e Android.
