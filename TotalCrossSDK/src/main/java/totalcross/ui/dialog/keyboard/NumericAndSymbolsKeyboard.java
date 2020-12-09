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

public class NumericAndSymbolsKeyboard extends Container {

  // First line
  public Button bt1;
  public Button bt2;
  public Button bt3;
  public Button bt4;
  public Button bt5;
  public Button bt6;
  public Button bt7;
  public Button bt8;
  public Button bt9;
  public Button bt0;

  // Second line
  public Button btAt;
  public Button btSharp;
  public Button btCurrency;
  public Button btUnderscore;
  public Button btAnd;
  public Button btMinus;
  public Button btPlus;
  public Button btLeftParenthesis;
  public Button btRightParenthesis;
  public Button btBackslash;

  // Third line
  public Button btPercent;
  public Button btMultiply;
  public Button btDoubleQuote;
  public Button btSingleQuote;
  public Button btColon;
  public Button btSemicolon;
  public Button btExclamation;
  public Button btInterrogation;

  // Last line
  public Button btComma;
  public Button btPeriod;
  public Button btSpace;

  public Button btCancel, btnABC, btDel;

  private int FORE_COLOR = Color.BLACK;

  public Button btSlash;

  public NumericAndSymbolsKeyboard() {
    // First line
    bt1 = new Button("1");
    bt2 = new Button("2");
    bt3 = new Button("3");
    bt4 = new Button("4");
    bt5 = new Button("5");
    bt6 = new Button("6");
    bt7 = new Button("7");
    bt8 = new Button("8");
    bt9 = new Button("9");
    bt0 = new Button("0");

    // Second line
    btAt = new Button("@");
    btSharp = new Button("#");
    btCurrency = new Button("$");
    btUnderscore = new Button("_");
    btAnd = new Button("&");
    btMinus = new Button("-");
    btPlus = new Button("+");
    btLeftParenthesis = new Button("(");
    btRightParenthesis = new Button(")");
    btBackslash = new Button("\\");

    // Third line
    btPercent = new Button("%");
    btMultiply = new Button("*");
    btDoubleQuote = new Button("\"");
    btSingleQuote = new Button("'");
    btColon = new Button(":");
    btSemicolon = new Button(";");
    btExclamation = new Button("!");
    btInterrogation = new Button("?");
    btComma = new Button(",");
    btPeriod = new Button(".");

    // Last line
    btSpace = new Button("             ");
    btSlash = new Button("/");

    btnABC = new Button("ABC");
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

    // Last line
    add(btnABC, aLeft, BOTTOM - vGap, WIDTH_BUTTON * 2, aHeight);
    add(btComma, AFTER + hGap, SAME, WIDTH_BUTTON, aHeight);
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
        btPercent,
        btMultiply,
        btDoubleQuote,
        btSingleQuote,
        btColon,
        btSemicolon,
        btExclamation,
        btInterrogation,
        btDel);
    btDel.setRect(KEEP, KEEP, FILL - hGap, KEEP);

    // Second line
    addButtonLine(
        WIDTH_BUTTON,
        aLeft,
        hGap,
        vGap,
        aHeight,
        btAt,
        btSharp,
        btCurrency,
        btUnderscore,
        btAnd,
        btMinus,
        btPlus,
        btLeftParenthesis,
        btRightParenthesis,
        btBackslash);

    // First line
    addButtonLine(
        WIDTH_BUTTON, aLeft, hGap, vGap, aHeight, bt1, bt2, bt3, bt4, bt5, bt6, bt7, bt8, bt9, bt0);

    // Last line
    configureKeyboardKey(btComma);
    configureKeyboardKey(btPeriod);
    configureKeyboardKey(btSpace);
    configureKeyboardKey(btSlash);
    configureKeyboardKey(btnABC);
    configureKeyboardKey(btCancel);

    btnABC.setBackForeColors(Color.getRGB(143, 152, 162), Color.WHITE);
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
    button.setForeColor(FORE_COLOR);
    // button.setFont(getFont().asBold()); // not supported with default system font
    button.setBackColor(Color.getRGB(233, 233, 235));
    button.effect = null;
  }
}
