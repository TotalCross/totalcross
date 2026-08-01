// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package danfe.fixture;

import totalcross.ui.ImageControl;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;

/** Deterministic, non-private macOS visual fixture for logical image scaling. */
public class DanfeScalingApp extends MainWindow {
  private static final int WIDTH = 360;
  private static final int HEIGHT = 540;

  @Override
  public void initUI() {
    Image document;
    try {
      document = new Image(WIDTH, HEIGHT);
    } catch (Exception exception) {
      throw new IllegalStateException("Unable to create DANFE fixture image", exception);
    }
    render(document.getGraphics());
    add(new ImageControl(document), CENTER, CENTER, PREFERRED, PREFERRED);
  }

  private static void render(Graphics graphics) {
    graphics.backColor = Color.WHITE;
    graphics.fillRect(0, 0, WIDTH, HEIGHT);
    graphics.backColor = Color.DARK;
    graphics.fillRect(12, 12, 336, 30);
    graphics.foreColor = Color.WHITE;
    graphics.drawText("DANFE - DOCUMENTO AUXILIAR", 20, 20);
    graphics.foreColor = Color.BLACK;
    graphics.drawText("EMISSOR: Comércio Exemplo São Paulo Ltda.", 20, 60);
    graphics.drawText("DESTINATÁRIO: Cliente Demonstração", 20, 80);
    graphics.drawText("Produto de descrição longa para validação", 20, 120);
    graphics.drawText("Qtd. 2  Valor unitário 12,50  Total 25,00", 20, 140);
    graphics.drawText("Rua das Flores, 100 - São Paulo - SP - 01000-000", 20, 160);
    graphics.drawText("TOTAL DA NOTA: R$ 25,00", 20, 180);
    graphics.backColor = Color.BLACK;
    for (int run = 0; run < 31; run++) {
      graphics.fillRect(20 + run * 10, 460, 4, 40);
    }
    graphics.drawText("Documento de demonstração sem dados privados", 20, 520);
  }
}
