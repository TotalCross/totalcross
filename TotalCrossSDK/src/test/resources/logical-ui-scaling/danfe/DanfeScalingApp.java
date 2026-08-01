// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package danfe.fixture;

import totalcross.ui.ImageControl;
import totalcross.ui.Label;
import totalcross.ui.Button;
import totalcross.ui.Check;
import totalcross.ui.Radio;
import totalcross.ui.Edit;
import totalcross.ui.Container;
import totalcross.ui.LayoutUnit;
import totalcross.ui.MainWindow;
import totalcross.ui.MultiEdit;
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
      MultiEdit metricMultiEdit = new MultiEdit("99999", 2, 1);
      add(metricMultiEdit, 0, 50, PREFERRED, PREFERRED);
      Graphics multiEditGraphics = metricMultiEdit.getGraphics();
      multiEditGraphics.setScales(contentScale, 1.0);
      int multiEditHeightAtOne = metricMultiEdit.getPreferredHeight();
      multiEditGraphics.setScales(contentScale * 2.0, 1.0);
      int multiEditHeightAtDoubleContentScale = metricMultiEdit.getPreferredHeight();
      multiEditGraphics.setScales(contentScale, 1.5);
      int multiEditHeightAtFontScale = metricMultiEdit.getPreferredHeight();
      Button metricButton = new Button("DANFE 25,00");
      add(metricButton, 0, 70, PREFERRED, PREFERRED);
      metricButton.getGraphics().setScales(contentScale, 1.0);
      int buttonWidthAtOne = metricButton.getPreferredWidth();
      metricButton.getGraphics().setScales(contentScale * 2.0, 1.0);
      int buttonWidthAtDoubleContentScale = metricButton.getPreferredWidth();
      metricButton.getGraphics().setScales(contentScale, 1.5);
      int buttonWidthAtFontScale = metricButton.getPreferredWidth();
      Check metricCheck = new Check("DANFE texto longo para quebrar linhas");
      add(metricCheck, 0, 100, PREFERRED, PREFERRED);
      metricCheck.getGraphics().setScales(contentScale, 1.0);
      metricCheck.split(50);
      int checkWidthAtOne = metricCheck.getMaxTextWidth();
      metricCheck.getGraphics().setScales(contentScale * 2.0, 1.0);
      int checkWidthAtDoubleContentScale = metricCheck.getMaxTextWidth();
      metricCheck.getGraphics().setScales(contentScale, 1.5);
      metricCheck.split(50);
      int checkWidthAtFontScale = metricCheck.getMaxTextWidth();
      Radio metricRadio = new Radio("DANFE 25,00");
      add(metricRadio, 0, 130, PREFERRED, PREFERRED);
      metricRadio.getGraphics().setScales(contentScale, 1.0);
      int radioWidthAtOne = metricRadio.getPreferredWidth();
      metricRadio.getGraphics().setScales(contentScale * 2.0, 1.0);
      int radioWidthAtDoubleContentScale = metricRadio.getPreferredWidth();
      metricRadio.getGraphics().setScales(contentScale, 1.5);
      int radioWidthAtFontScale = metricRadio.getPreferredWidth();
      Image logicalImage;
      try {
        logicalImage = Image.createLogical(3, 2, 2);
      } catch (Exception exception) {
        throw new IllegalStateException("Unable to create logical image assertion", exception);
      }
      logicalImage.getGraphics().backColor = Color.BLACK;
      logicalImage.getGraphics().fillRect(1, 0, 1, 1);
      logicalImage.applyChanges();
      if (logicalImage.getWidth() != 3 || logicalImage.getHeight() != 2
          || logicalImage.getPixelWidth() != 6 || logicalImage.getPixelHeight() != 4) {
        throw new IllegalStateException("Logical image dimension assertion failed: "
            + logicalImage.getWidth() + "x" + logicalImage.getHeight() + "/"
            + logicalImage.getPixelWidth() + "x" + logicalImage.getPixelHeight());
      }
      Container pixelRoot = new Container();
      pixelRoot.setLayoutUnit(LayoutUnit.PIXEL);
      add(pixelRoot, 0, 80, 200, 100);
      pixelRoot.getGraphics().setScales(contentScale, 1.0);
      Container pixelChild = new Container();
      pixelRoot.add(pixelChild, 20, 10, 100, 40);
      double danfeAdvance = metrics.stringWidthD("DANFE 25,00");
      double accentedAdvance = metrics.stringWidthD("Comércio São Paulo");
      double representativePairAdvance = metrics.stringWidthD("AV");
      int compatibleDanfeAdvance = metrics.stringWidth("DANFE 25,00");
      char[] danfeChars = "DANFE 25,00".toCharArray();
      int arrayDanfeAdvance = metrics.stringWidth(danfeChars, 0, danfeChars.length);
      int bufferDanfeAdvance = metrics.sbWidth(new StringBuffer("DANFE 25,00"));
      System.out.println("LOGICAL_UI_IMAGE logical=" + logicalImage.getWidth() + "x" + logicalImage.getHeight()
          + " physical=" + logicalImage.getPixelWidth() + "x" + logicalImage.getPixelHeight());
      if (!(metrics.getAscentD() > 0 && metrics.getDescentD() >= 0 && metrics.getLeadingD() >= 0
          && metrics.getHeightD() >= metrics.getAscentD() + metrics.getDescentD()
          && danfeAdvance > 0 && accentedAdvance > 0 && representativePairAdvance > 0
          && compatibleDanfeAdvance == (int) Math.ceil(danfeAdvance)
          && arrayDanfeAdvance == compatibleDanfeAdvance && bufferDanfeAdvance == compatibleDanfeAdvance
          && widthAtOne == widthAtDoubleContentScale && widthAtFontScale > widthAtOne
          && editWidthAtOne == editWidthAtDoubleContentScale && editWidthAtFontScale > editWidthAtOne
          && multiEditHeightAtOne == multiEditHeightAtDoubleContentScale
          && multiEditHeightAtFontScale > multiEditHeightAtOne
          && buttonWidthAtOne == buttonWidthAtDoubleContentScale && buttonWidthAtFontScale > buttonWidthAtOne
          && checkWidthAtOne == checkWidthAtDoubleContentScale && checkWidthAtFontScale > checkWidthAtOne
          && radioWidthAtOne == radioWidthAtDoubleContentScale && radioWidthAtFontScale > radioWidthAtOne
          && pixelChild.getX() == 10 && pixelChild.getY() == 5 && pixelChild.getWidth() == 50 && pixelChild.getHeight() == 20)) {
        throw new IllegalStateException("Logical text metric assertion failed");
      }
      System.out.println("LOGICAL_UI_SCALE logical=" + Settings.screenWidth + "x" + Settings.screenHeight
          + " physical=" + physicalWidth + "x" + physicalHeight + " contentScale=" + contentScale
          + " ascentD=" + metrics.getAscentD() + " descentD=" + metrics.getDescentD()
          + " leadingD=" + metrics.getLeadingD() + " heightD=" + metrics.getHeightD()
          + " advanceD=" + danfeAdvance + " accentedAdvanceD=" + accentedAdvance
          + " representativePairAdvanceD=" + representativePairAdvance + " compatibleAdvance=" + compatibleDanfeAdvance
          + " arrayAdvance=" + arrayDanfeAdvance + " bufferAdvance=" + bufferDanfeAdvance
          + " labelWidths=" + widthAtOne + "," + widthAtDoubleContentScale + "," + widthAtFontScale
          + " editWidths=" + editWidthAtOne + "," + editWidthAtDoubleContentScale + "," + editWidthAtFontScale
          + " multiEditHeights=" + multiEditHeightAtOne + "," + multiEditHeightAtDoubleContentScale + "," + multiEditHeightAtFontScale
          + " buttonWidths=" + buttonWidthAtOne + "," + buttonWidthAtDoubleContentScale + "," + buttonWidthAtFontScale
          + " checkWidths=" + checkWidthAtOne + "," + checkWidthAtDoubleContentScale + "," + checkWidthAtFontScale
          + " radioWidths=" + radioWidthAtOne + "," + radioWidthAtDoubleContentScale + "," + radioWidthAtFontScale
          + " logicalImage=3x2/6x4"
          + " pixelChild=" + pixelChild.getX() + "," + pixelChild.getY() + "," + pixelChild.getWidth() + "," + pixelChild.getHeight());
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
