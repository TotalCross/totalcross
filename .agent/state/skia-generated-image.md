<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan Skia Generated Image

Milestone ativo: nenhum; Milestone 2 concluído. Próximo milestone: 3, tornar
`SkBitmap` autoritativo para leituras, escritas, desenho entre imagens e saída.

Commits lógicos deste milestone:

- `6c840cd41 refactor(vm): own skia image surfaces together`
- `2a8e8c194 fix(vm): route skia drawing to target surfaces`

Arquivos principais alterados: `TotalCrossVM/src/nm/ui/skia/skia.h`,
`skia_internal.h`, `skia.cpp`, `skia_surface.cpp`, `skia_primitives.cpp`,
`GraphicsPrimitives_c.h`, `GraphicsPrimitivesSkia_c.h`,
`GraphicsPrimitivesText_c.h`, `GraphicsPrimitivesShapes_c.h` e
`GraphicsPrimitivesScreen_c.h`.

Entregue no Milestone 2:

- `SkiaImageSurface` possui `SkBitmap` e `std::unique_ptr<SkCanvas>` juntos;
- `imageSurfaces` mantém IDs zero-based estáveis, com slots deletados nulos;
- `SKIA_SCREEN_SURFACE_ID` é `-1` e `SKIA_INVALID_SURFACE_ID` é `-2`;
- `skiaGetCanvas()` e `skiaGetBitmap()` rejeitam IDs inválidos/deletados;
- clipping, pixels, image drawing e primitivas resolvem o canvas selecionado;
- o helper C retorna o canvas da tela ou obtém/cria o surface da imagem.

Validação executada:

- `git diff --check`: passou;
- `ninja -C build-skia-structure`: passou e vinculou `libtcvm.dylib`;
- probe headless temporário: passou IDs `0/1`, isolamento tela/imagem,
  clipping e falha segura após deleção/ID inválido.

O primeiro probe usou uma expectativa incorreta para a cor inicial da tela e
falhou nessa asserção; foi corrigido para comparar o valor antes/depois, e o
probe final passou. Não foram executadas validações do Milestone 3 ou
posteriores: SDK, Java SE, macOS/Android deployment e smoke test ficam
deferidos conforme o plano.

Bloqueios: nenhum bloqueio técnico. O caminho de estado solicitado no prompt,
`.agent/state/exec-plan-skia-generated-image.md`, continua ausente; este é o
estado canônico definido pelo ExecPlan.

Próxima ação: iniciar somente quando solicitado o Milestone 3, auditando
promoção e autoridade do bitmap; não executar ainda suas validações.
