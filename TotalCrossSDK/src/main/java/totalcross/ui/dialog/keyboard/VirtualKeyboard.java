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
import totalcross.ui.gfx.Color;
import totalcross.ui.gfx.Graphics;
import totalcross.ui.image.Image;

public class VirtualKeyboard extends Window {
  private Edit edt;
  private MultiEdit mult;
  public String value = null;
  /** Botao de OK */
  private Button btOk;
  /** Botao de Cancelar */
  private Button btCancel;
  /** Botao de Limpar tudo */
  private Button btLimpar;
  /** Botao de deletar o ultimo digito */
  private Button btDel;
  /** Edit do valor que será digitado na tela */
  private KeyboardEdit valorEdt;

  private Button btnAbc;
  private Button btn123;
  private KeyEvent backspaceEvent = new KeyEvent();
  private int pos;
  /** Images que serão utilizadas no layout do teclado */
  private static Image imgOk, imgCancela, imgLimpar, imgBackSpace, img123, imgAbc;

  private static boolean imagensMudadas = false;
  /** Variáveis que guardarão as mesagens dos botões de cancelar e limpar */
  private static String strCancelar = " Cancelar ", strLimpar = " Limpar ";
  /** Representa o container do teclado numÃÂ©rico */
  protected NumericAndSymbolsKeyboard numericKeyboard = null;
  /** Representa o container do teclado alfa */
  protected AlphabetKeyboard alfaKeyboard = null;

  protected static VirtualKeyboard keyboard = null;
  protected static boolean isOpen;
  public boolean btnCancelClicked = false;

  /* Valor usado para verificar se o usuario fez alguma alteração no valor */
  private String valorAnterior = "";

  private static boolean changeOrientation = false;

  public static void orientationChanged(boolean changed) {
    changeOrientation = changed;
  }

  /**
   * Método que retorna a última instância criada do teclado... Caso não exista nenhuma instância
   * criada irá retornar nulo. (Esse método só deve ser utilizado para recuperar a instância e não
   * para criar o teclado..)
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
      keyboard.valorEdt.setText(msg);
    } else {
      keyboard.valorEdt.setValidChars(null);
      keyboard.valorEdt.setMode(Edit.NORMAL);
      keyboard.valorEdt.setMaxLength(maxLength);
      keyboard.valorEdt.setText("");
    }
    keyboard.alfaKeyboard.changeCase(true);
    keyboard.valorEdt.setMaxLength(100000);
    keyboard.value = null;
    return keyboard;
  }

  protected VirtualKeyboard() {
    // Esse titulo eh gigante para possibilitar que a janela seja arrastada
    // mesmo que nao tenha titulo
    super.titleGap = 0;

    /* Constante que define o fator horizontal . */
    float X = Settings.screenWidth * 0.01f;
    /* Constante que define o fator horizontal . */
    float Y = Settings.screenHeight * 0.01f;
    /* Constante que define a cor azul. */
    int BLUE_COLOR = Color.getRGB(44, 0, 139);

    setRect(LEFT, BOTTOM, FILL, SCREENSIZE + 50);

    setInsets(0, 0, 0, 0);

    /* Colocando o BG */
    BackGroundContainer bGc = new BackGroundContainer();
    add(bGc);

    bGc.setRect(LEFT, TOP, FILL, FILL);
    if (imgOk != null) {
      btOk = Builder.createButton(" Ok ");
    } else {
      btOk = Builder.createButton(" Ok ");
    }
    if (imgCancela != null) {
      btCancel = new Button(imgCancela);
      btCancel.transparentBackground = true;
      btCancel.setBorder(BORDER_NONE);
      btCancel.setBorder(Button.BORDER_3D_VERTICAL_GRADIENT);
      btCancel.bottomColor3DG = Color.WHITE;
      btCancel.topColor3DG = Color.WHITE;
      btCancel.borderWidth3DG = 0;
      btCancel.setForeColor(Color.BLACK);
    } else {
      btCancel = new Button(strCancelar);
    }
    if (imgLimpar != null) {
      btLimpar = new Button(imgLimpar);
      btLimpar.transparentBackground = true;
      btLimpar.setVisible(false);
      btLimpar.setBorder(BORDER_NONE);
    } else {
      try {
        btLimpar = new Button(new Image("totalcross/res/x.png").getSmoothScaledInstance(fmH, fmH));
        btLimpar.transparentBackground = true;
        btLimpar.setVisible(false);
        btLimpar.setBorder(BORDER_NONE);
      } catch (Exception e) {
        btLimpar = new Button(strLimpar);
      }
    }
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
    valorEdt = new KeyboardEdit();
    add(valorEdt);
    valorEdt.setKeyboard(Edit.KBD_NONE);

    int buttonWidth = 2 * fmH;

    int WIDTH_BUTTON = (int) ((Settings.screenWidth - (5 * X)) / 4);
    int HEIGHT_BUTTON =
        (int)
            (6
                * ((Settings.screenHeight < Settings.screenWidth
                        ? Settings.screenWidth
                        : Settings.screenHeight)
                    * 0.01));

    valorEdt.setRect(
        LEFT + ((int) X), TOP + (int) (Y), (int) ((WIDTH_BUTTON * 3) + (2 * X)), HEIGHT_BUTTON);

    add(btLimpar);
    add(btOk);

    int btLimparY = (HEIGHT_BUTTON - buttonWidth) / 2;

    btLimpar.setRect(
        valorEdt.getWidth() - (buttonWidth), TOP + (int) (Y) + btLimparY, buttonWidth, buttonWidth);

    btOk.setRect(AFTER + ((int) X), SAME, WIDTH_BUTTON, HEIGHT_BUTTON, valorEdt);

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
    backspaceEvent.target = valorEdt;
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
    btnCancelClicked = false;
    setFocus(valorEdt);
    int end = valorEdt.getLength();
    valorEdt.setCursorPos(end, end);
    valorAnterior = valorEdt.getText();

    if (valorAnterior != null && valorAnterior.equals("")) {
      btLimpar.setVisible(false);
    } else {
      btLimpar.setVisible(true);
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
   * Organiza os botoes que sao visiveis de acordo com os caracteres validos do edit.
   *
   * @return retorna um boolean sinalizando se o teclado alfa deve ou nao aparecer
   */
  protected boolean organizarVisibilidadeTecladoAlfa() {

    alfaKeyboard.btComma.setVisible(valorEdt.isCharValid(','));
    alfaKeyboard.btPeriod.setVisible(valorEdt.isCharValid('.'));
    alfaKeyboard.btSlash.setVisible(valorEdt.isCharValid('/'));
    alfaKeyboard.btSpace.setVisible(valorEdt.isCharValid(' '));

    boolean q = valorEdt.isCharValid('q');
    alfaKeyboard.btQ.setVisible(q);
    boolean w = valorEdt.isCharValid('w');
    alfaKeyboard.btW.setVisible(w);
    boolean e = valorEdt.isCharValid('e');
    alfaKeyboard.btE.setVisible(e);
    boolean r = valorEdt.isCharValid('r');
    alfaKeyboard.btR.setVisible(r);
    boolean t = valorEdt.isCharValid('t');
    alfaKeyboard.btT.setVisible(t);
    boolean yTmp = valorEdt.isCharValid('y');
    alfaKeyboard.btY.setVisible(yTmp);
    boolean u = valorEdt.isCharValid('u');
    alfaKeyboard.btU.setVisible(u);
    boolean i = valorEdt.isCharValid('i');
    alfaKeyboard.btI.setVisible(i);
    boolean o = valorEdt.isCharValid('o');
    alfaKeyboard.btO.setVisible(o);
    boolean p = valorEdt.isCharValid('p');
    alfaKeyboard.btP.setVisible(p);
    boolean a = valorEdt.isCharValid('a');
    alfaKeyboard.btA.setVisible(a);
    boolean s = valorEdt.isCharValid('s');
    alfaKeyboard.btS.setVisible(s);
    boolean d = valorEdt.isCharValid('d');
    alfaKeyboard.btD.setVisible(d);
    boolean f = valorEdt.isCharValid('f');
    alfaKeyboard.btF.setVisible(f);
    boolean g = valorEdt.isCharValid('g');
    alfaKeyboard.btG.setVisible(g);
    boolean h = valorEdt.isCharValid('h');
    alfaKeyboard.btH.setVisible(h);
    boolean j = valorEdt.isCharValid('j');
    alfaKeyboard.btJ.setVisible(j);
    boolean k = valorEdt.isCharValid('k');
    alfaKeyboard.btK.setVisible(k);
    boolean l = valorEdt.isCharValid('l');
    alfaKeyboard.btL.setVisible(l);
    boolean z = valorEdt.isCharValid('z');
    alfaKeyboard.btZ.setVisible(z);
    boolean xTmp = valorEdt.isCharValid('x');
    alfaKeyboard.btX.setVisible(xTmp);
    boolean c = valorEdt.isCharValid('c');
    alfaKeyboard.btC.setVisible(c);
    boolean v = valorEdt.isCharValid('v');
    alfaKeyboard.btV.setVisible(v);
    boolean b = valorEdt.isCharValid('b');
    alfaKeyboard.btB.setVisible(b);
    boolean n = valorEdt.isCharValid('n');
    alfaKeyboard.btN.setVisible(n);
    boolean m = valorEdt.isCharValid('m');
    alfaKeyboard.btM.setVisible(m);

    return (q || w || e || r || t || yTmp || u || i || o || p || a || s || d || f || g || h || j
        || k || l || z || xTmp || c || v || b || n || m);
  }

  /**
   * Organiza os botoes que sao visiveis de acordo com os caracteres validos do edit.
   *
   * @return retorna um boolean sinalizando se o teclado numerico deve ou nao aparecer
   */
  protected boolean organizarVisibilidadeTecladoNumerico() {
    boolean virgula = valorEdt.isCharValid(',');
    numericKeyboard.btComma.setVisible(virgula);
    boolean um = valorEdt.isCharValid('1');
    numericKeyboard.bt1.setVisible(um);
    boolean dois = valorEdt.isCharValid('2');
    numericKeyboard.bt2.setVisible(dois);
    boolean tres = valorEdt.isCharValid('3');
    numericKeyboard.bt3.setVisible(tres);
    boolean quatro = valorEdt.isCharValid('4');
    numericKeyboard.bt4.setVisible(quatro);
    boolean cinco = valorEdt.isCharValid('5');
    numericKeyboard.bt5.setVisible(cinco);
    boolean seis = valorEdt.isCharValid('6');
    numericKeyboard.bt6.setVisible(seis);

    boolean sete = valorEdt.isCharValid('7');
    numericKeyboard.bt7.setVisible(sete);
    boolean oito = valorEdt.isCharValid('8');
    numericKeyboard.bt8.setVisible(oito);
    boolean nove = valorEdt.isCharValid('9');
    numericKeyboard.bt9.setVisible(nove);
    boolean zero = valorEdt.isCharValid('0');
    numericKeyboard.bt0.setVisible(zero);
    boolean ponto = valorEdt.isCharValid('.');
    numericKeyboard.btPeriod.setVisible(ponto);

    return (zero || um || dois || tres || quatro || cinco || seis || sete || oito || nove);
  }

  /**
   * seta o valor que o campo será preenchido ao abrir o teclado.
   *
   * @param value
   */
  public void setValue(String value) {
    this.value = value;
    if (value != null) {
      valorEdt.setText(value);
    }
  }

  /**
   * Método que retorna o valor digitado no edit do teclado virtual
   *
   * @return String <b>indica o valor digitado no teclado virtual</b>
   */
  public String getText() {
    return valorEdt.getText();
  }

  /**
   * Método que configura um valor para o edit do teclado virtual
   *
   * @param text <b>novo texto para o teclado virtual</b>
   */
  public void setText(String text) {
    this.valorEdt.setText(text);
  }

  /**
   * Seta os caracteres válidos que podem ser escritos nesse teclado. Caso seja nulo, qualquer
   * caractere pode ser escrito.
   */
  public void setValidChars(String validCharsString) {
    valorEdt.setValidChars(validCharsString);
    boolean tecladoAlfa = organizarVisibilidadeTecladoAlfa();
    if (!tecladoAlfa) {
      swap(numericKeyboard);
      //      numericKeyboard.btABC.setVisible(false);
      alfaKeyboard.btn123.setVisible(false);
      btnAbc.setVisible(false);
      btn123.setVisible(false);
    }
    boolean tecladoNumerico = organizarVisibilidadeTecladoNumerico();
    if (tecladoNumerico && tecladoAlfa) {
      swap(alfaKeyboard);
      //      numericKeyboard.btABC.setVisible(true);
      alfaKeyboard.btn123.setVisible(true);
    }
    if (!tecladoAlfa && !tecladoNumerico) {
      unpop();
    }
  }

  /**
   * Seta o tamanho maximo da String
   *
   * @param maxLength2
   */
  public void setMaxLength(int maxLength) {
    valorEdt.setMaxLength(maxLength);
  }

  public void setMode(byte mode) {
    valorEdt.setMode(mode);
  }

  @Override
  public void setFocus(Control c) {
    if (getFocus() == valorEdt) {
      pos = valorEdt.getCursorPos()[1];
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
        } // Botão CANCELAR
        else if (target == btCancel
            || target == alfaKeyboard.btCancel
            || target == numericKeyboard.btCancel) {
          value = null;
          btnCancelClicked = true;
          unpop();
        } // Botão LIMPAR
        else if (target == btLimpar) {
          btLimpar.setVisible(false);
          valorEdt.setText("");
          valorEdt.requestFocus();
        } // Botão Del
        else if (target == btDel
            || target == alfaKeyboard.btDel
            || target == numericKeyboard.btDel) {
          setFocus(valorEdt);
          valorEdt.setCursorPos(pos, pos);
          valorEdt.onEvent(backspaceEvent);
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
          valorEdt.setCursorPos(pos, pos);
          Button botao = (Button) target;
          String text = botao.getText().trim();
          KeyEvent e = new KeyEvent();
          e.target = valorEdt;
          char tecla = ' ';
          if (text.length() > 0) {
            tecla = text.charAt(0);
          }
          e.key = tecla;
          valorEdt.onEvent(e);
          pos++;
          setFocus(valorEdt);
          valorEdt.setCursorPos(pos, pos);
          valorEdt.repaintNow();
        }

        if (valorEdt.getText() != null && valorEdt.getText().length() > 0) {
          btLimpar.setVisible(true);
        } else {
          btLimpar.setVisible(false);
        }

        break;
    }
  }

  /** Método chamado ao clicar no botão OK */
  private void clickBtOk() {
    value = valorEdt.getText();
    if (value == null) {
      value = "";
    }
    // Caso o usuario não altere o valor que esta na combo, então o teclado
    // tem que simular o comportamento igual como se
    // tivesse clicado no cancelar. Ou seja, ele não deve fazer nada depois.
    if (value.equalsIgnoreCase(valorAnterior)) {
      btnCancelClicked = true;
    }

    if (edt != null) {
      edt.setText(value);
    } else if (mult != null) {
      mult.setText(value);
    }
    unpop();
  }

  public void setCursorPosEnd() {
    final int length = valorEdt.getLength();
    valorEdt.setCursorPos(length, length);
  }

  public void setCursorPos() {
    setFocus(valorEdt);
    int len = valorEdt.getTextFull().length();
    valorEdt.setCursorPos(len, len);
  }

  /**
   * Método que atualiza as mensagens do componente
   *
   * @param strCancelar
   * @param strLimpar
   */
  public static void updateMessages(String strCancelar, String strLimpar) {
    VirtualKeyboard.strCancelar = strCancelar;
    VirtualKeyboard.strLimpar = strLimpar;
  }

  public static void updateImages(
      Image imgOk,
      Image imgCancela,
      Image imgLimpar,
      Image imgBackSpace,
      Image img123,
      Image imgAbc) {
    imagensMudadas = true;
    VirtualKeyboard.imgOk = imgOk;
    VirtualKeyboard.imgCancela = imgCancela;
    VirtualKeyboard.imgLimpar = imgLimpar;
    VirtualKeyboard.imgBackSpace = imgBackSpace;
    VirtualKeyboard.img123 = img123;
    VirtualKeyboard.imgAbc = imgAbc;
  }

  @Override
  public void screenResized() {
    super.screenResized();
    unpop();
  }

  /** Container que servirá de background da MainWindow */
  private class BackGroundContainer extends Container {
    /** Método que pinta o container */
    @Override
    public void onPaint(Graphics g) {
      /*
       * Pintando o background
       */
      if (imagensMudadas) {
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
