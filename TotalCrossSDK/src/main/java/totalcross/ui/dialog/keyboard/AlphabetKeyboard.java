// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.dialog.keyboard;

import totalcross.sys.Settings;
import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;

public class AlphabetKeyboard extends Container {

  // First line
  public Button btQ;
  public Button btW;
  public Button btE;
  public Button btR;
  public Button btT;
  public Button btY;
  public Button btU;
  public Button btI;
  public Button btO;
  public Button btP;

  // Second line
  public Button btA;
  public Button btS;
  public Button btD;
  public Button btF;
  public Button btG;
  public Button btH;
  public Button btJ;
  public Button btK;
  public Button btL;

  // Third line
  public Button btZ;
  public Button btX;
  public Button btC;
  public Button btV;
  public Button btB;
  public Button btN;
  public Button btM;

  public Button btComma;
  public Button btPeriod;
  public Button btSpace;
  public Button btCancel, btn123, btDel;

  private int FORE_COLOR = Color.BLACK;

  public Button btCase;

  public Button btSlash;

  private int screenWidth, screenHeight, topHeight;

  public AlphabetKeyboard(Integer width, Integer height, Integer topHeight) {
    this.screenWidth = width != null ? width : Settings.screenWidth;
    this.screenHeight = height != null ? height : Settings.screenHeight;
    this.topHeight = topHeight != null ? topHeight : 0;

    // First line
    btQ = new Button("Q");
    btW = new Button("W");
    btE = new Button("E");
    btR = new Button("R");
    btT = new Button("T");
    btY = new Button("Y");
    btU = new Button("U");
    btI = new Button("I");
    btO = new Button("O");
    btP = new Button("P");
    // Second line
    btA = new Button("A");
    btS = new Button("S");
    btD = new Button("D");
    btF = new Button("F");
    btG = new Button("G");
    btH = new Button("H");
    btJ = new Button("J");
    btK = new Button("K");
    btL = new Button("L");
    // Third line
    btZ = new Button("Z");
    btX = new Button("X");
    btC = new Button("C");
    btV = new Button("V");
    btB = new Button("B");
    btN = new Button("N");
    btM = new Button("M");
    btComma = new Button(",");
    btPeriod = new Button(".");
    // Last line
    btSpace = new Button("             ");
    btSlash = new Button("/");
    btCase = new Button("[a]");

    btn123 = new Button("?123");
    btCancel = new Button("Cancel");
    try {
      int size = fmH * 3 / 2;
      btDel = new Button(new Image("totalcross/res/del.png").getSmoothScaledInstance(size, size));
    } catch (Exception e) {
      btDel = new Button("Del");
    }
  }

  @Override
  public void initUI() {
    final float X = screenWidth * 0.01f;
    final float Y = screenHeight * 0.01f;
    final int HEIGHT_BUTTON = (int) (((screenHeight - (6 * Y)) / 4));
    final int WIDTH_BUTTON = (int) (((screenWidth - (11 * X)) / 10));

    setRect(LEFT, TOP + topHeight, FILL, FILL);

    add(btComma);
    add(btPeriod);
    add(btSpace);
    add(btSlash);
    add(btn123);
    add(btCancel);

    final int aLeft = LEFT + (int) X;
    final int hGap = (int) X;
    final int vGap = (int) (Y);
    final int aHeight = HEIGHT_BUTTON;

    btn123.setRect(aLeft, BOTTOM - vGap, WIDTH_BUTTON * 2, aHeight);
    btComma.setRect(AFTER + hGap, SAME, WIDTH_BUTTON, aHeight);
    btPeriod.setRect(AFTER + hGap, SAME, WIDTH_BUTTON, aHeight);
    btSpace.setRect(AFTER + hGap, SAME, (WIDTH_BUTTON * 3), aHeight);
    btSlash.setRect(AFTER + hGap, SAME, WIDTH_BUTTON, aHeight);
    btCancel.setRect(AFTER + hGap, SAME, FILL - hGap, aHeight);

    // Third line
    addButtonLine(
        WIDTH_BUTTON,
        aLeft + (int) (WIDTH_BUTTON * 0.5),
        hGap,
        vGap,
        aHeight,
        btCase,
        btZ,
        btX,
        btC,
        btV,
        btB,
        btN,
        btM,
        btDel);
    btCase.setRect(aLeft, KEEP, (int) (WIDTH_BUTTON * 1.5), KEEP);
    btDel.setRect(KEEP, KEEP, FILL - hGap, KEEP);

    // Second line
    addButtonLine(
        WIDTH_BUTTON,
        aLeft + (WIDTH_BUTTON / 2),
        hGap,
        vGap,
        aHeight,
        btA,
        btS,
        btD,
        btF,
        btG,
        btH,
        btJ,
        btK,
        btL);

    // First line
    addButtonLine(
        WIDTH_BUTTON, aLeft, hGap, vGap, aHeight, btQ, btW, btE, btR, btT, btY, btU, btI, btO, btP);

    // Last line
    configureKeyboardKey(btComma);
    configureKeyboardKey(btPeriod);
    configureKeyboardKey(btSpace);
    configureKeyboardKey(btSlash);
    configureKeyboardKey(btn123);
    configureKeyboardKey(btCancel);

    btCase.setBackForeColors(Color.getRGB(143, 152, 162), Color.WHITE);
    btn123.setBackForeColors(Color.getRGB(143, 152, 162), Color.WHITE);
    btCancel.setBackForeColors(Color.getRGB(143, 152, 162), Color.WHITE);
    btDel.setBackForeColors(Color.getRGB(143, 152, 162), Color.WHITE);
  }

  private void addButtonLine(
      int WIDTH_BUTTON, int aLeft, int hGap, int vGap, int aHeight, Button... buttons) {
    add(buttons[0], aLeft, BEFORE - vGap, WIDTH_BUTTON, aHeight);
    configureKeyboardKey(buttons[0]);
    for (int i = 1; i < buttons.length; i++) {
      add(buttons[i], AFTER + hGap, SAME, WIDTH_BUTTON, aHeight);
      configureKeyboardKey(buttons[i]);
    }
  }

  private void configureKeyboardKey(Button button) {
    Font font = getFont().asBold();
    button.setForeColor(FORE_COLOR);
    button.setFont(font);
    button.setBackColor(Color.getRGB(233, 233, 235));
  }

  public void changeCase(boolean toUpperCase) {
    if (toUpperCase) {
      btCase.setText("[a]");
      btQ.setText(btQ.getText().toUpperCase());
      btW.setText(btW.getText().toUpperCase());
      btE.setText(btE.getText().toUpperCase());
      btR.setText(btR.getText().toUpperCase());
      btT.setText(btT.getText().toUpperCase());
      btY.setText(btY.getText().toUpperCase());
      btU.setText(btU.getText().toUpperCase());
      btI.setText(btI.getText().toUpperCase());
      btO.setText(btO.getText().toUpperCase());
      btP.setText(btP.getText().toUpperCase());
      // Second line
      btA.setText(btA.getText().toUpperCase());
      btS.setText(btS.getText().toUpperCase());
      btD.setText(btD.getText().toUpperCase());
      btF.setText(btF.getText().toUpperCase());
      btG.setText(btG.getText().toUpperCase());
      btH.setText(btH.getText().toUpperCase());
      btJ.setText(btJ.getText().toUpperCase());
      btK.setText(btK.getText().toUpperCase());
      btL.setText(btL.getText().toUpperCase());
      // Third line
      btZ.setText(btZ.getText().toUpperCase());
      btX.setText(btX.getText().toUpperCase());
      btC.setText(btC.getText().toUpperCase());
      btV.setText(btV.getText().toUpperCase());
      btB.setText(btB.getText().toUpperCase());
      btN.setText(btN.getText().toUpperCase());
      btM.setText(btM.getText().toUpperCase());
    } else {
      btCase.setText("[A]");
      btQ.setText(btQ.getText().toLowerCase());
      btW.setText(btW.getText().toLowerCase());
      btE.setText(btE.getText().toLowerCase());
      btR.setText(btR.getText().toLowerCase());
      btT.setText(btT.getText().toLowerCase());
      btY.setText(btY.getText().toLowerCase());
      btU.setText(btU.getText().toLowerCase());
      btI.setText(btI.getText().toLowerCase());
      btO.setText(btO.getText().toLowerCase());
      btP.setText(btP.getText().toLowerCase());
      // Second line
      btA.setText(btA.getText().toLowerCase());
      btS.setText(btS.getText().toLowerCase());
      btD.setText(btD.getText().toLowerCase());
      btF.setText(btF.getText().toLowerCase());
      btG.setText(btG.getText().toLowerCase());
      btH.setText(btH.getText().toLowerCase());
      btJ.setText(btJ.getText().toLowerCase());
      btK.setText(btK.getText().toLowerCase());
      btL.setText(btL.getText().toLowerCase());
      // Third line
      btZ.setText(btZ.getText().toLowerCase());
      btX.setText(btX.getText().toLowerCase());
      btC.setText(btC.getText().toLowerCase());
      btV.setText(btV.getText().toLowerCase());
      btB.setText(btB.getText().toLowerCase());
      btN.setText(btN.getText().toLowerCase());
      btM.setText(btM.getText().toLowerCase());
    }
  }
}
