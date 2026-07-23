<!--
Copyright (C) 2026 Amalgam Solucoes em TI Ltda

SPDX-License-Identifier: LGPL-2.1-only
-->

# Estado do ExecPlan Skia Generated Image

Milestone ativo: nenhum; Milestone 3 concluído. Próximo milestone: 4, criar o
smoke test derivado do anexo e validar os destinos finais.

Commits lógicos deste milestone:

- `14082b5d1 fix(vm): make skia bitmap authoritative`
- `5973bd890 fix(vm): draw image surfaces through skia`

Arquivos principais alterados: `TotalCrossVM/src/nm/ui/image_Image.c`,
`TotalCrossVM/src/nm/ui/ImagePrimitives_c.h`,
`TotalCrossVM/src/nm/ui/GraphicsPrimitives_c.h`,
`TotalCrossVM/src/nm/ui/GraphicsPrimitivesSkia_c.h`,
`TotalCrossVM/src/nm/ui/skia/skia.h`, `skia_internal.h`,
`skia_surface.cpp` e `skia_primitives.cpp`.

Entregue no Milestone 3:

- `Image.pixels` só promove o primeiro bitmap quando `Image.textureId < 0`;
- `applyChanges()` e a promoção de fontes não reupam um bitmap existente;
- desenho imagem→imagem usa canvas Skia, sem o caminho ativo de cópia entre
  arrays Java;
- `getPixel()`, `getRGB()` e `getPixelRow()` leem a representação Skia quando
  disponível, com conversão explícita entre `PixelConv` e RGBA/SkColor;
- transformações array-only não alcançadas por esse caminho, como scale,
  rotate e color transforms, foram mantidas intencionalmente.

Validação executada:

- `git diff --check`: passou antes do registro documental final;
- `ninja -C build-skia-structure`: passou e vinculou `libtcvm.dylib`; log
  completo em `/tmp/skia-m3-ninja.log`;
- `/tmp/skia-authority-probe`: passou `getPixelRow()`, leitura/escrita de
  `getPixel()`/`getRGB()`, desenho imagem→imagem, ausência de reupload após
  mutação do array original e recriação sem conteúdo stale;
- `/tmp/skia-surface-probe`: passou novamente após o novo `libtcvm.dylib`.

Durante o desenvolvimento, uma variável local duplicada, o uso inexistente de
`SkCanvas.width()/height()` e uma leitura direta stale do bitmap foram
detectados e corrigidos; as execuções finais passaram.

Não foram executadas validações de milestones posteriores: SDK, Java SE,
macOS/Android deployment e smoke test ficam deferidos conforme o plano.

Bloqueios: nenhum bloqueio técnico. O caminho de estado solicitado no prompt,
`.agent/state/exec-plan-skia-generated-image.md`, continua ausente; este é o
estado canônico definido pelo ExecPlan.

Próxima ação: iniciar somente quando solicitado o Milestone 4, criando o smoke
test derivado de `Tcsort.zip`; não executar ainda a matriz final de destinos.
