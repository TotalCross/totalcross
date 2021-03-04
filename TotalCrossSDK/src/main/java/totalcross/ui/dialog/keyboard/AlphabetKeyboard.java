// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only
package totalcross.ui.dialog.keyboard;

import totalcross.sys.Settings;
import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.font.Font;
import totalcross.ui.gfx.Color;
import totalcross.ui.image.Image;
import totalcross.ui.gfx.Graphics;
import totalcross.sys.Convert;

public class AlphabetKeyboard extends Container {

  // First line
  public CaseSensitiveButton btQ;
  public CaseSensitiveButton btW;
  public CaseSensitiveButton btE;
  public CaseSensitiveButton btR;
  public CaseSensitiveButton btT;
  public CaseSensitiveButton btY;
  public CaseSensitiveButton btU;
  public CaseSensitiveButton btI;
  public CaseSensitiveButton btO;
  public CaseSensitiveButton btP;

  // Second line
  public CaseSensitiveButton btA;
  public CaseSensitiveButton btS;
  public CaseSensitiveButton btD;
  public CaseSensitiveButton btF;
  public CaseSensitiveButton btG;
  public CaseSensitiveButton btH;
  public CaseSensitiveButton btJ;
  public CaseSensitiveButton btK;
  public CaseSensitiveButton btL;

  // Third line
  public CaseSensitiveButton btZ;
  public CaseSensitiveButton btX;
  public CaseSensitiveButton btC;
  public CaseSensitiveButton btV;
  public CaseSensitiveButton btB;
  public CaseSensitiveButton btN;
  public CaseSensitiveButton btM;

  public Button btComma;
  public Button btPeriod;
  public Button btSpace;
  public Button btCancel, btn123, btDel;

  private int FORE_COLOR = Color.BLACK;

  public Button btCase;

  public Button btSlash;

  public AlphabetKeyboard() {
    // First line
    btQ = new CaseSensitiveButton("Q");
    btW = new CaseSensitiveButton("W");
    btE = new CaseSensitiveButton("E");
    btR = new CaseSensitiveButton("R");
    btT = new CaseSensitiveButton("T");
    btY = new CaseSensitiveButton("Y");
    btU = new CaseSensitiveButton("U");
    btI = new CaseSensitiveButton("I");
    btO = new CaseSensitiveButton("O");
    btP = new CaseSensitiveButton("P");
    // Second line
    btA = new CaseSensitiveButton("A");
    btS = new CaseSensitiveButton("S");
    btD = new CaseSensitiveButton("D");
    btF = new CaseSensitiveButton("F");
    btG = new CaseSensitiveButton("G");
    btH = new CaseSensitiveButton("H");
    btJ = new CaseSensitiveButton("J");
    btK = new CaseSensitiveButton("K");
    btL = new CaseSensitiveButton("L");
    // Third line
    btZ = new CaseSensitiveButton("Z");
    btX = new CaseSensitiveButton("X");
    btC = new CaseSensitiveButton("C");
    btV = new CaseSensitiveButton("V");
    btB = new CaseSensitiveButton("B");
    btN = new CaseSensitiveButton("N");
    btM = new CaseSensitiveButton("M");
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
    final float X = width * 0.01f;
    final float Y = height * 0.01f;
    final int HEIGHT_BUTTON = this.fm.height * 3;
    final int WIDTH_BUTTON = (int) (((width - (11 * X)) / 10));

    final int aLeft = LEFT + (int) X;
    final int hGap = (int) X;
    final int vGap = (int) (Y);
    final int aHeight = HEIGHT_BUTTON;

    add(btn123, aLeft, BOTTOM - vGap, WIDTH_BUTTON * 2, aHeight);
    add(btComma, AFTER + hGap, SAME, WIDTH_BUTTON, aHeight, btn123);
    add(btPeriod, AFTER + hGap, SAME, WIDTH_BUTTON, aHeight);
    add(btSpace, AFTER + hGap, SAME, (WIDTH_BUTTON * 3), aHeight);
    add(btSlash, AFTER + hGap, SAME, WIDTH_BUTTON, aHeight);
    add(btCancel, AFTER + hGap, SAME, FILL - hGap, aHeight);

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

  private void  addButtonLine(
      int WIDTH_BUTTON, int aLeft, int hGap, int vGap, int aHeight, Button... buttons) {
    add(buttons[0], aLeft, BEFORE - vGap, WIDTH_BUTTON, aHeight);
    configureKeyboardKey(buttons[0]);
    for (int i = 1; i < buttons.length; i++) {
      add(buttons[i], AFTER + hGap, SAME, WIDTH_BUTTON, aHeight);
      configureKeyboardKey(buttons[i]);
    }
  }

  private void configureKeyboardKey(Button button) {
    button.setForeColor(FORE_COLOR);
    button.setBackColor(Color.getRGB(233, 233, 235));
    // find a better way to disable effects, this method produces double pen_up events
    // button.effect = null;
  }

  public void changeCase(boolean toUpperCase) {
    isUpperCase = toUpperCase;
    if (toUpperCase) {
      btCase.setText("[a]");
    } else {
      btCase.setText("[A]");
    }
  }

  class TextBlob {
    String text;
    Font font;
    String[] lines;
    int[] linesW;
    int maxTW;

    TextBlob(String text, Font font) {
      this.text = text;
      this.font = font;

      lines = Convert.tokenizeString(text, '\n');
      if (text != null) {
        if (linesW == null || linesW.length != lines.length) {
          linesW = new int[lines.length];
        }
        int[] linesW = this.linesW;
        maxTW = 0;
        for (int i = lines.length - 1; i >= 0; i--) {
          linesW[i] = font.fm.stringWidth(lines[i]);
          maxTW = Math.max(maxTW, linesW[i]);
        }
      }
    }
  }

  class TextBlobButton extends Button {
    TextBlob blob;

    TextBlobButton(String text) {
      super((String) null);
      if (text != null) {
        this.setText(new TextBlob(text, this.font));
      }
    }

    TextBlobButton(TextBlob blob) {
      super((String) null);
      this.setText(blob);
    }

    public void setText(TextBlob blob) {
      this.blob = blob;
      this.font = blob.font;
      this.text = blob.text;
      this.lines = blob.lines;
      this.linesW = blob.linesW;
      this.maxTW = blob.maxTW;
      onBoundsChanged(false);
    }

    @Override
    public void setText(String text) {
      this.setText(new TextBlob(text, this.font));
    }
  }

  boolean isUpperCase = true;

  class CaseSensitiveButton extends TextBlobButton {
    TextBlob tb1;
    TextBlob tb2;
    boolean isUpperCase = true;

    CaseSensitiveButton(String t) {
      super((String) null);
      tb1 = new TextBlob(t.toUpperCase(), this.font);
      tb2 = new TextBlob(t.toLowerCase(), this.font);
      this.setText(tb1);
    }

    CaseSensitiveButton(String t1, String t2) {
      super((String) null);
      tb1 = new TextBlob(t1, this.font);
      tb2 = new TextBlob(t2, this.font);
      this.setText(tb1);
    }

    @Override
    public void onPaint(Graphics g) {
      if (AlphabetKeyboard.this.isUpperCase != this.isUpperCase) {
        this.setText(AlphabetKeyboard.this.isUpperCase ? tb1 : tb2);
        this.isUpperCase = AlphabetKeyboard.this.isUpperCase;
      }
      super.onPaint(g);
    }
  }
}
