// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package danfe.fixture;

import totalcross.ui.ImageControl;
import totalcross.ui.Label;
import totalcross.ui.Edit;
import totalcross.ui.MainWindow;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.font.FontMetrics;
import totalcross.ui.image.Image;
import totalcross.sys.Settings;

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
    if (getCommandLine().indexOf("/logical-ui-assert") >= 0) {
      Graphics screen = getGraphics();
      double contentScale = screen.getContentScale();
      int physicalWidth = (int) Math.round(Settings.screenWidth * contentScale);
      int physicalHeight = (int) Math.round(Settings.screenHeight * contentScale);
      FontMetrics metrics = getFont().fm;
      Label metricLabel = new Label("DANFE 25,00");
      add(metricLabel, 0, 0, PREFERRED, PREFERRED);
      Graphics labelGraphics = metricLabel.getGraphics();
      labelGraphics.setScales(contentScale, 1.0);
      int widthAtOne = metricLabel.getPreferredWidth();
      labelGraphics.setScales(contentScale * 2.0, 1.0);
      int widthAtDoubleContentScale = metricLabel.getPreferredWidth();
      labelGraphics.setScales(contentScale, 1.5);
      int widthAtFontScale = metricLabel.getPreferredWidth();
      Edit metricEdit = new Edit("99999");
      add(metricEdit, 0, 20, PREFERRED, PREFERRED);
      Graphics editGraphics = metricEdit.getGraphics();
      editGraphics.setScales(contentScale, 1.0);
      int editWidthAtOne = metricEdit.getPreferredWidth();
      editGraphics.setScales(contentScale * 2.0, 1.0);
      int editWidthAtDoubleContentScale = metricEdit.getPreferredWidth();
      editGraphics.setScales(contentScale, 1.5);
      int editWidthAtFontScale = metricEdit.getPreferredWidth();
      System.out.println("LOGICAL_UI_SCALE logical=" + Settings.screenWidth + "x" + Settings.screenHeight
          + " physical=" + physicalWidth + "x" + physicalHeight + " contentScale=" + contentScale
          + " ascentD=" + metrics.getAscentD() + " descentD=" + metrics.getDescentD()
          + " leadingD=" + metrics.getLeadingD() + " heightD=" + metrics.getHeightD()
          + " advanceD=" + metrics.stringWidthD("DANFE 25,00")
          + " labelWidths=" + widthAtOne + "," + widthAtDoubleContentScale + "," + widthAtFontScale
          + " editWidths=" + editWidthAtOne + "," + editWidthAtDoubleContentScale + "," + editWidthAtFontScale);
      exit(0);
    }
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
