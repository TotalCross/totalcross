// Copyright (C) 2026 Amalgam Solucoes em TI Ltda
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import totalcross.Launcher;

class LogicalTextScaleTest {
  @BeforeAll
  static void initializeRuntime() {
    new Launcher();
  }

  @Test
  void labelPreferredSizeUsesFontScaleButNotContentScale() {
    Label label = new Label("DANFE");

    label.gfx.setScales(1.0, 1.0);
    int widthAtOne = label.getPreferredWidth();
    int heightAtOne = label.getPreferredHeight();

    label.gfx.setScales(2.0, 1.0);
    assertEquals(widthAtOne, label.getPreferredWidth());
    assertEquals(heightAtOne, label.getPreferredHeight());

    label.gfx.setScales(1.0, 1.5);
    assertTrue(label.getPreferredWidth() > widthAtOne);
    assertTrue(label.getPreferredHeight() > heightAtOne);
  }

  @Test
  void fontScaleChangeRepositionsPreferredControl() {
    Label label = new Label("DANFE");
    label.setRect(0, 0, Control.PREFERRED, Control.PREFERRED);
    int widthAtOne = label.width;
    int heightAtOne = label.height;

    label.gfx.setScales(1.0, 1.5);

    assertTrue(label.width > widthAtOne);
    assertTrue(label.height > heightAtOne);
  }

  @Test
  void multiEditPreferredSizeUsesFontScaleButNotContentScale() {
    MultiEdit edit = new MultiEdit("99999", 2, 1);
    edit.gfx.setScales(1.0, 1.0);
    int widthAtOne = edit.getPreferredWidth();
    int heightAtOne = edit.getPreferredHeight();

    edit.gfx.setScales(2.0, 1.0);
    assertEquals(widthAtOne, edit.getPreferredWidth());
    assertEquals(heightAtOne, edit.getPreferredHeight());

    edit.gfx.setScales(1.0, 1.5);
    assertTrue(edit.getPreferredWidth() > widthAtOne);
    assertTrue(edit.getPreferredHeight() > heightAtOne);
  }

  @Test
  void labelAutoSplitUsesDestinationFontMeasurement() {
    Label label = new Label();
    label.autoSplit = true;
    label.setRect(0, 0, 50, 100);
    String text = "DANFE texto longo para quebrar linhas";

    label.gfx.setScales(1.0, 1.0);
    label.setText(text);
    String linesAtOne = label.getText();

    label.gfx.setScales(2.0, 1.0);
    label.setText(text);
    assertEquals(linesAtOne, label.getText());

    label.gfx.setScales(1.0, 1.5);
    label.setText(text);
    assertTrue(label.getText().split("\\n").length > linesAtOne.split("\\n").length);
  }

  @Test
  void checkSplitUsesDestinationFontMeasurementAndPreservesNewlines() {
    Check check = new Check("DANFE texto longo\\npara quebrar linhas");

    check.gfx.setScales(1.0, 1.0);
    check.split(50);
    String linesAtOne = check.getText();

    check.gfx.setScales(3.0, 1.0);
    check.split(50);
    assertEquals(linesAtOne, check.getText());

    check.gfx.setScales(1.0, 1.5);
    check.split(50);
    assertTrue(check.getText().split("\\n").length > linesAtOne.split("\\n").length);
  }

  @Test
  void checkAndRadioMetricsIgnoreContentScaleAndTrackFontScale() {
    Check check = new Check("DANFE 25,00");
    Radio radio = new Radio("DANFE 25,00");
    check.gfx.setScales(1.0, 1.0);
    radio.gfx.setScales(1.0, 1.0);
    int checkAtOne = check.getMaxTextWidth();
    int radioAtOne = radio.getPreferredWidth();
    check.gfx.setScales(3.0, 1.0);
    radio.gfx.setScales(3.0, 1.0);
    assertEquals(checkAtOne, check.getMaxTextWidth());
    assertEquals(radioAtOne, radio.getPreferredWidth());
    check.gfx.setScales(1.0, 1.5);
    radio.gfx.setScales(1.0, 1.5);
    assertTrue(check.getMaxTextWidth() > checkAtOne);
    assertTrue(radio.getPreferredWidth() > radioAtOne);
  }

  @Test
  void labelLineWidthCacheIgnoresContentScaleAndTracksFontScale() {
    Label label = new Label("DANFE 25,00");
    label.gfx.setScales(1.0, 1.0);
    int widthAtOne = label.getMaxTextWidth();

    label.gfx.setScales(3.0, 1.0);
    assertEquals(widthAtOne, label.getMaxTextWidth());

    label.gfx.setScales(1.0, 1.5);
    assertTrue(label.getMaxTextWidth() > widthAtOne);
  }

  @Test
  void buttonPreferredSizeUsesFontScaleButNotContentScale() {
    Button button = new Button("DANFE");

    button.gfx.setScales(1.0, 1.0);
    int widthAtOne = button.getPreferredWidth();
    int heightAtOne = button.getPreferredHeight();

    button.gfx.setScales(2.0, 1.0);
    assertEquals(widthAtOne, button.getPreferredWidth());
    assertEquals(heightAtOne, button.getPreferredHeight());

    button.gfx.setScales(1.0, 1.5);
    assertTrue(button.getPreferredWidth() > widthAtOne);
    assertTrue(button.getPreferredHeight() > heightAtOne);
  }

  @Test
  void editPreferredSizeUsesFontScaleButNotContentScale() {
    Edit edit = new Edit("99999");

    edit.gfx.setScales(1.0, 1.0);
    int widthAtOne = edit.getPreferredWidth();
    int heightAtOne = edit.getPreferredHeight();

    edit.gfx.setScales(2.0, 1.0);
    assertEquals(widthAtOne, edit.getPreferredWidth());
    assertEquals(heightAtOne, edit.getPreferredHeight());

    edit.gfx.setScales(1.0, 1.5);
    assertTrue(edit.getPreferredWidth() > widthAtOne);
    assertTrue(edit.getPreferredHeight() > heightAtOne);
  }

  @Test
  void editCursorGeometryUsesFontScaleButNotContentScale() {
    ExposedEdit edit = new ExposedEdit();
    edit.setRect(0, 0, 200, 50);
    edit.setText("DANFE");

    edit.gfx.setScales(1.0, 1.0);
    int widthAtOne = edit.totalWidth();
    int cursorAtOne = edit.cursorAtEnd();

    edit.gfx.setScales(2.0, 1.0);
    assertEquals(widthAtOne, edit.totalWidth());
    assertEquals(cursorAtOne, edit.cursorAtEnd());

    edit.gfx.setScales(1.0, 1.5);
    assertTrue(edit.totalWidth() > widthAtOne);
    assertTrue(edit.cursorAtEnd() > cursorAtOne);
  }

  private static final class ExposedEdit extends Edit {
    int totalWidth() {
      return getTotalCharWidth();
    }

    int cursorAtEnd() {
      return charPos2x(getText().length());
    }
  }
}
