// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
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
import totalcross.sys.SpecialKeys;
import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Edit;
import totalcross.ui.MultiEdit;
import totalcross.ui.Window;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;
import totalcross.ui.event.PenEvent;
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.icon.Icon;
import totalcross.ui.icon.MaterialIcons;
import totalcross.ui.image.Image;

public class VirtualKeyboard extends Window {
  private Edit edt;
  private MultiEdit mult;
  public String value = null;
  /** Ok button */
  private Button btOk;
  /** Cancel button */
  private Button btCancel;
  /** Clear icon */
  private Icon clrIcon;
  /** Delete button */
  private Button btDel;
  /** Edit with the text value */
  private KeyboardEdit valueEdit;

  private Button btnAbc;
  private Button btn123;
  private KeyEvent backspaceEvent = new KeyEvent();
  private int pos;
  /** Keyboard layout images */
  private static Image imgOk, imgCancel, imgBackSpace, img123, imgAbc;

  private static boolean changedImages = false;
  /** Cancel string */
  private static String strCancel = " Cancelar ";
  /** Represents the container's numeric keyboard */
  protected NumericAndSymbolsKeyboard numericKeyboard = null;
  /** Represents the container's letters keyboard */
  protected AlphabetKeyboard alfaKeyboard = null;

  protected static VirtualKeyboard keyboard = null;
  protected static boolean isOpen;
  public boolean btnCancelPressed = false;

  /** Value used to verify if the user has made any changes */
  private String previousValue = "";
  public static int iconColor = Color.getRGB(143, 152, 162);
  private boolean dragStarted;

  private static boolean changeOrientation = false;

  public static void orientationChanged(boolean changed) {
    changeOrientation = changed;
  }

  /**
   * This method returns the last created instance of the VirtualKeyboard. In case there isn't one it will return null
   * (This method should only be used to retrieve an instance, not create one).
   *
   * @return
   */
  public static VirtualKeyboard getInstance() {
    return keyboard;
  }

  /** @return */
  public static VirtualKeyboard getInstance(Control c) {
    if (keyboard == null || changeOrientation) {
      changeOrientation = false;
      keyboard = new VirtualKeyboard();
    }
    String msg = "";
    int maxLength = 0;
    if (c instanceof Edit) {
      keyboard.edt = ((Edit) c);
      msg = ((Edit) c).getText();
      maxLength = ((Edit) c).getMaxLength();
      keyboard.mult = null;
    } else if (c instanceof MultiEdit) {
      keyboard.mult = ((MultiEdit) c);
      msg = ((MultiEdit) c).getText();
      maxLength = ((MultiEdit) c).getMaxLength();
      keyboard.edt = null;
    } else {
      keyboard.edt = null;
      keyboard.mult = null;
    }

    if (c != null) {
      keyboard.valueEdit.setText(msg);
    } else {
      keyboard.valueEdit.setValidChars(null);
      keyboard.valueEdit.setMode(Edit.NORMAL);
      keyboard.valueEdit.setMaxLength(maxLength);
      keyboard.valueEdit.setText("");
    }
    keyboard.alfaKeyboard.changeCase(true);
    keyboard.valueEdit.setMaxLength(100000);
    keyboard.value = null;
    return keyboard;
  }

  protected VirtualKeyboard() {
    // This title is big to allow the window to be dragged even if there's no title
    super.titleGap = 0;

    /* Constant that defines the horizontal factor . */
    float X = Settings.screenWidth * 0.01f;
    /* Constant that defines the vertical factor . */
    float Y = Settings.screenHeight * 0.01f;

    setRect(LEFT, BOTTOM, FILL, SCREENSIZE + 50);

    setInsets(0, 0, 0, 0);

    /* Creating the background */
    BackGroundContainer bkgContainer = new BackGroundContainer();
    add(bkgContainer);

    bkgContainer.setRect(LEFT, TOP, FILL, FILL);
    if (imgOk != null) {
      btOk = Builder.createButton(" Ok ");
    } else {
      btOk = Builder.createButton(" Ok ");
    }
    if (imgCancel != null) {
      btCancel = new Button(imgCancel);
      btCancel.transparentBackground = true;
      btCancel.setBorder(BORDER_NONE);
      btCancel.setBorder(Button.BORDER_3D_VERTICAL_GRADIENT);
      btCancel.bottomColor3DG = Color.WHITE;
      btCancel.topColor3DG = Color.WHITE;
      btCancel.borderWidth3DG = 0;
      btCancel.setForeColor(Color.BLACK);
    } else {
      btCancel = new Button(strCancel);
    }
    clrIcon = new Icon(MaterialIcons._CLEAR);
    clrIcon.setFont(clrIcon.getFont().adjustedBy(-1));
    clrIcon.setForeColor(iconColor );
    if (imgBackSpace != null) {
      btDel = new Button(imgBackSpace);
      btDel.setBorder(Button.BORDER_3D_VERTICAL_GRADIENT);
      btDel.bottomColor3DG = Color.WHITE;
      btDel.topColor3DG = Color.WHITE;
      btDel.borderWidth3DG = 0;
      btDel.setForeColor(Color.BLACK);
    } else {
      btDel = new Button(" < ");
    }
    if (imgAbc != null) {
      btnAbc = new Button(imgAbc);
      btnAbc.setBorder(Button.BORDER_3D_VERTICAL_GRADIENT);
      btnAbc.bottomColor3DG = Color.WHITE;
      btnAbc.topColor3DG = Color.WHITE;
      btnAbc.borderWidth3DG = 0;
      btnAbc.setForeColor(Color.BLACK);
    } else {
      btnAbc = new Button("abc");
    }
    if (img123 != null) {
      btn123 = new Button(img123);
      btn123.setBorder(Button.BORDER_3D_VERTICAL_GRADIENT);
      btn123.bottomColor3DG = Color.WHITE;
      btn123.topColor3DG = Color.WHITE;
      btn123.borderWidth3DG = 0;
      btn123.setForeColor(Color.BLACK);
    } else {
      btn123 = new Button("123");
    }

    edt = null;
    mult = null;
    valueEdit = new KeyboardEdit();
    add(valueEdit);
    valueEdit.drawLine = false;
    valueEdit.transparentBackground = true;
    valueEdit.setKeyboard(Edit.KBD_NONE);

    int buttonWidth = 2 * fmH;

    int WIDTH_BUTTON = (int) ((Settings.screenWidth - (5 * X)) / 4);
    int HEIGHT_BUTTON =
        (int)
            (6
                * ((Settings.screenHeight < Settings.screenWidth
                        ? Settings.screenWidth
                        : Settings.screenHeight)
                    * 0.01));

    valueEdit.setRect(
        LEFT + ((int) X), TOP + (int) (Y), (int) ((WIDTH_BUTTON * 3) + (2 * X)), HEIGHT_BUTTON);

    add(clrIcon);
    add(btOk);

    int btLimparY = (HEIGHT_BUTTON - buttonWidth) / 2;

    clrIcon.setRect(
        valueEdit.getWidth() - (buttonWidth), TOP + (int) (1.5*Y) + btLimparY, buttonWidth, buttonWidth);

    btOk.setRect(AFTER + ((int) X), SAME, WIDTH_BUTTON, HEIGHT_BUTTON, valueEdit);

    add(btCancel);
    add(btDel);
    add(btnAbc);
    btnAbc.setVisible(false);
    add(btn123);
    alfaKeyboard =
        new AlphabetKeyboard(
            getAbsoluteRect().width,
            getAbsoluteRect().height - (HEIGHT_BUTTON + (int) (Y)),
            HEIGHT_BUTTON + (int) (Y));
    numericKeyboard =
        new NumericAndSymbolsKeyboard(
            getAbsoluteRect().width,
            getAbsoluteRect().height - (HEIGHT_BUTTON + (int) (Y)),
            HEIGHT_BUTTON + (int) (Y));

    backspaceEvent.type = KeyEvent.KEY_PRESS;
    backspaceEvent.target = valueEdit;
    backspaceEvent.key = SpecialKeys.BACKSPACE;

    // COLORIR A TELA
    //    if (!imagensMudadas) {
    //      btOk.setForeColor(BLUE_COLOR);
    //      btCancel.setForeColor(BLUE_COLOR);
    //      btLimpar.setForeColor(BLUE_COLOR);
    //      btDel.setForeColor(BLUE_COLOR);
    //      btnAbc.setForeColor(BLUE_COLOR);
    //      btn123.setForeColor(BLUE_COLOR);
    //    }
  }

  @Override
  protected void onPopup() {
    btnCancelPressed = false;
    setFocus(valueEdit);
    int end = valueEdit.getLength();
    valueEdit.setCursorPos(end, end);
    previousValue = valueEdit.getText();

    if (previousValue != null && previousValue.equals("")) {
      clrIcon.setVisible(false);
    } else {
      clrIcon.setVisible(true);
    }
  }

  @Override
  protected void postPopup() {
    isOpen = true;
  }

  @Override
  public void postUnpop() {
    Window.getTopMost().repaintNow();
    isOpen = false;
  }

  /**
   * Organizes the visible buttons in accordance with the valid characters.
   *
   * @return returns a boolean which signals if the letters keyboard should appear or not
   */
  protected boolean organizarVisibilidadeTecladoAlfa() {

    alfaKeyboard.btComma.setVisible(valueEdit.isCharValid(','));
    alfaKeyboard.btPeriod.setVisible(valueEdit.isCharValid('.'));
    alfaKeyboard.btSlash.setVisible(valueEdit.isCharValid('/'));
    alfaKeyboard.btSpace.setVisible(valueEdit.isCharValid(' '));

    boolean q = valueEdit.isCharValid('q');
    alfaKeyboard.btQ.setVisible(q);
    boolean w = valueEdit.isCharValid('w');
    alfaKeyboard.btW.setVisible(w);
    boolean e = valueEdit.isCharValid('e');
    alfaKeyboard.btE.setVisible(e);
    boolean r = valueEdit.isCharValid('r');
    alfaKeyboard.btR.setVisible(r);
    boolean t = valueEdit.isCharValid('t');
    alfaKeyboard.btT.setVisible(t);
    boolean yTmp = valueEdit.isCharValid('y');
    alfaKeyboard.btY.setVisible(yTmp);
    boolean u = valueEdit.isCharValid('u');
    alfaKeyboard.btU.setVisible(u);
    boolean i = valueEdit.isCharValid('i');
    alfaKeyboard.btI.setVisible(i);
    boolean o = valueEdit.isCharValid('o');
    alfaKeyboard.btO.setVisible(o);
    boolean p = valueEdit.isCharValid('p');
    alfaKeyboard.btP.setVisible(p);
    boolean a = valueEdit.isCharValid('a');
    alfaKeyboard.btA.setVisible(a);
    boolean s = valueEdit.isCharValid('s');
    alfaKeyboard.btS.setVisible(s);
    boolean d = valueEdit.isCharValid('d');
    alfaKeyboard.btD.setVisible(d);
    boolean f = valueEdit.isCharValid('f');
    alfaKeyboard.btF.setVisible(f);
    boolean g = valueEdit.isCharValid('g');
    alfaKeyboard.btG.setVisible(g);
    boolean h = valueEdit.isCharValid('h');
    alfaKeyboard.btH.setVisible(h);
    boolean j = valueEdit.isCharValid('j');
    alfaKeyboard.btJ.setVisible(j);
    boolean k = valueEdit.isCharValid('k');
    alfaKeyboard.btK.setVisible(k);
    boolean l = valueEdit.isCharValid('l');
    alfaKeyboard.btL.setVisible(l);
    boolean z = valueEdit.isCharValid('z');
    alfaKeyboard.btZ.setVisible(z);
    boolean xTmp = valueEdit.isCharValid('x');
    alfaKeyboard.btX.setVisible(xTmp);
    boolean c = valueEdit.isCharValid('c');
    alfaKeyboard.btC.setVisible(c);
    boolean v = valueEdit.isCharValid('v');
    alfaKeyboard.btV.setVisible(v);
    boolean b = valueEdit.isCharValid('b');
    alfaKeyboard.btB.setVisible(b);
    boolean n = valueEdit.isCharValid('n');
    alfaKeyboard.btN.setVisible(n);
    boolean m = valueEdit.isCharValid('m');
    alfaKeyboard.btM.setVisible(m);

    return (q || w || e || r || t || yTmp || u || i || o || p || a || s || d || f || g || h || j
        || k || l || z || xTmp || c || v || b || n || m);
  }

  /**
   * Organizes the visible buttons in accordance with the valid characters.
   *
   * @return returns a boolean which signals if the numeric keyboard should appear or not
   */
  protected boolean organizeNumericKeyboardVisibility() {
    boolean virgula = valueEdit.isCharValid(',');
    numericKeyboard.btComma.setVisible(virgula);
    boolean um = valueEdit.isCharValid('1');
    numericKeyboard.bt1.setVisible(um);
    boolean dois = valueEdit.isCharValid('2');
    numericKeyboard.bt2.setVisible(dois);
    boolean tres = valueEdit.isCharValid('3');
    numericKeyboard.bt3.setVisible(tres);
    boolean quatro = valueEdit.isCharValid('4');
    numericKeyboard.bt4.setVisible(quatro);
    boolean cinco = valueEdit.isCharValid('5');
    numericKeyboard.bt5.setVisible(cinco);
    boolean seis = valueEdit.isCharValid('6');
    numericKeyboard.bt6.setVisible(seis);

    boolean sete = valueEdit.isCharValid('7');
    numericKeyboard.bt7.setVisible(sete);
    boolean oito = valueEdit.isCharValid('8');
    numericKeyboard.bt8.setVisible(oito);
    boolean nove = valueEdit.isCharValid('9');
    numericKeyboard.bt9.setVisible(nove);
    boolean zero = valueEdit.isCharValid('0');
    numericKeyboard.bt0.setVisible(zero);
    boolean ponto = valueEdit.isCharValid('.');
    numericKeyboard.btPeriod.setVisible(ponto);

    return (zero || um || dois || tres || quatro || cinco || seis || sete || oito || nove);
  }

  /**
   * Sets the value used when you open the keyboard.
   *
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
    if (value != null) {
      valueEdit.setText(value);
    }
  }

  /**
   * Returns the value on the VirtualKeyboard
   *
   * @return String <b>value</b>
   */
  public String getText() {
    return valueEdit.getText();
  }

  /**
   * Sets the text on the VirtualKeyboard Edit.
   *
   * @param text <b>new text</b>
   */
  public void setText(String text) {
    this.valueEdit.setText(text);
  }

  /**
   * Sets the possible characters to be written on the keyboard. If the value is null, any character can be used.
   */
  public void setValidChars(String validCharsString) {
    valueEdit.setValidChars(validCharsString);
    boolean alphabetKeyboard = organizarVisibilidadeTecladoAlfa();
    if (!alphabetKeyboard) {
      swap(numericKeyboard);
      //      numericKeyboard.btABC.setVisible(false);
      alfaKeyboard.btn123.setVisible(false);
      btnAbc.setVisible(false);
      btn123.setVisible(false);
    }
    boolean numericKeyboard = organizeNumericKeyboardVisibility();
    if (numericKeyboard && alphabetKeyboard) {
      swap(alfaKeyboard);
      //      numericKeyboard.btABC.setVisible(true);
      alfaKeyboard.btn123.setVisible(true);
    }
    if (!alphabetKeyboard && !numericKeyboard) {
      unpop();
    }
  }

  /**
   * Sets the max string size
   *
   * @param maxLength2
   */
  public void setMaxLength(int maxLength) {
    valueEdit.setMaxLength(maxLength);
  }

  public void setMode(byte mode) {
    valueEdit.setMode(mode);
  }

  @Override
  public void setFocus(Control c) {
    if (getFocus() == valueEdit) {
      pos = valueEdit.getCursorPos()[1];
    }
    super.setFocus(c);
  }

  @Override
  public void onEvent(Event event) {
    Object target = event.target;
    switch (event.type) {
      case KeyEvent.SPECIAL_KEY_PRESS:
        KeyEvent ke = (KeyEvent) event;
        if (ke.isActionKey()) {
          btOk.requestFocus();
          btOk.simulatePress();
          clickBtOk();
        }
        break;
      case ControlEvent.PRESSED:
        if (target == btOk) {
          clickBtOk();
        } // Cancel button
        else if (target == btCancel
            || target == alfaKeyboard.btCancel
            || target == numericKeyboard.btCancel) {
          value = null;
          btnCancelPressed = true;
          unpop();
        } // Clear Icon
        else if (target == clrIcon) {
          clrIcon.setVisible(false);
          valueEdit.setText("");
          valueEdit.requestFocus();
        } // Del button
        else if (target == btDel
            || target == alfaKeyboard.btDel
            || target == numericKeyboard.btDel) {
          setFocus(valueEdit);
          valueEdit.setCursorPos(pos, pos);
          valueEdit.onEvent(backspaceEvent);
        } else if (target == btn123 || target == alfaKeyboard.btn123) {
          swap(numericKeyboard);
          btnAbc.setVisible(true);
          btn123.setVisible(false);
          setCursorPos();
        } else if (target == btnAbc || target == numericKeyboard.btnABC) {
          swap(alfaKeyboard);
          btnAbc.setVisible(false);
          btn123.setVisible(true);
          setCursorPos();
        } else if (target == alfaKeyboard.btCase) {
          String newCase = alfaKeyboard.btCase.getText().trim();
          boolean toUpperCase = newCase.equals("[A]");
          alfaKeyboard.changeCase(toUpperCase);
        } else if (target instanceof Button) {
          valueEdit.setCursorPos(pos, pos);
          Button button = (Button) target;
          String text = button.getText().trim();
          KeyEvent e = new KeyEvent();
          e.target = valueEdit;
          char key = ' ';
          if (text.length() > 0) {
            key = text.charAt(0);
          }
          e.key = key;
          valueEdit.onEvent(e);
          pos++;
          setFocus(valueEdit);
          valueEdit.setCursorPos(pos, pos);
          valueEdit.repaintNow();
        }

        if (valueEdit.getText() != null && valueEdit.getText().length() > 0) {
          clrIcon.setVisible(true);
        } else {
          clrIcon.setVisible(false);
        }

        break;
      case PenEvent.PEN_DRAG_START:
    	  dragStarted = true;
    	  break;
      case PenEvent.PEN_UP:
    	  if(target == clrIcon) {
    		  if(!dragStarted) {
    			  clrIcon.postPressedEvent();
    		  } else {
    			  dragStarted = false;
    		  }
    	  }
    }
  }

  /** Method called when the Ok Button is pressedMétodo chamado ao clicar no botão OK */
  private void clickBtOk() {
    value = valueEdit.getText();
    if (value == null) {
      value = "";
    }
    // In case the user doesn't alter the value on the combo, then the keyboard
    // has to simulate the same behavior as if cancel was pressed.
    // Which means, it can't do anything after.
    if (value.equalsIgnoreCase(previousValue)) {
      btnCancelPressed = true;
    }

    if (edt != null) {
      edt.setText(value);
    } else if (mult != null) {
      mult.setText(value);
    }
    unpop();
  }

  public void setCursorPosEnd() {
    final int length = valueEdit.getLength();
    valueEdit.setCursorPos(length, length);
  }

  public void setCursorPos() {
    setFocus(valueEdit);
    int len = valueEdit.getTextFull().length();
    valueEdit.setCursorPos(len, len);
  }

  /**
   * Updates the messages.
   *
   * @param strCancelar
   * @param strLimpar
   * @deprecated
   */
  public static void updateMessages(String strCancelar, String strLimpar) {
    VirtualKeyboard.strCancel = strCancelar;
  }
  /**
   * Updates the cancel string;
   * 
   * @param strCancel
   * */
  public static void updateStrCancel(String strCancel) {
	  VirtualKeyboard.strCancel = strCancel;
  }

  public static void updateImages(
      Image imgOk,
      Image imgCancel,
      Image imgBackSpace,
      Image img123,
      Image imgAbc) {
    changedImages = true;
    VirtualKeyboard.imgOk = imgOk;
    VirtualKeyboard.imgCancel = imgCancel;
    VirtualKeyboard.imgBackSpace = imgBackSpace;
    VirtualKeyboard.img123 = img123;
    VirtualKeyboard.imgAbc = imgAbc;
  }

  @Override
  public void screenResized() {
    super.screenResized();
    unpop();
  }

  /** Background Container used on MainWindow*/
  private class BackGroundContainer extends Container {
    /** Paint method for this container*/
    @Override
    public void onPaint(Graphics g) {
      /*
       * Painting the background
       */
      if (changedImages) {
        g.drawRoundGradient(
            0,
            0,
            width,
            height,
            0,
            0,
            0,
            0,
            Color.getRGB(143, 152, 162),
            Color.getRGB(71, 81, 94),
            true);
      }
    }
  }
}
