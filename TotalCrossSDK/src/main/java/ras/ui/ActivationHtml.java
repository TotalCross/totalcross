package ras.ui;

import totalcross.io.IOException;
import totalcross.sys.Convert;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.Control;
import totalcross.ui.Edit;
import totalcross.ui.ScrollBar;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.gfx.Color;
import totalcross.ui.html.Document;
import totalcross.ui.html.HtmlContainer;
import totalcross.util.Hashtable;
import totalcross.xml.SyntaxException;
import totalcross.xml.XmlReadableByteArray;

public class ActivationHtml extends Container {
  public static final int ACTIVATION_START = 0;
  public static final int ACTIVATION_SUCCESS = 1;
  public static final int ACTIVATION_ERROR = 2;
  public static final int ACTIVATION_NOINTERNET = 3;

  private static String[] activationHtmls = { "activation.html", "activation_success.html", "activation_error.html",
      "activation_nointernet.html" };

  private HtmlContainer htmlCnr;
  private Document doc;

  public int type;

  private static Hashtable userDefinedParams;

  private ActivationHtml(byte[] source) throws IOException, SyntaxException {
    doc = new Document(new XmlReadableByteArray(source));
    userDefinedParams = new Hashtable(30);
  }

  public static ActivationHtml getInstance(int type) {
    byte[] source = Vm.getFile(activationHtmls[type]);
    if (source != null) {
      try {
        ActivationHtml instance = new ActivationHtml(source);
        instance.type = type;
        return instance;
      } catch (Exception e) {
        Vm.debug("Failed to load HTML!");
        MessageBox.showException(e, true);
      }
    }
    return null;
  }

  @Override
  public void initUI() {
    int scrollBarExtraSize = ScrollBar.extraSize;
    int buttonCommonGap = Button.commonGap;
    ScrollBar.extraSize = 4;
    Button.commonGap = 2;
    add(htmlCnr = new HtmlContainer(), 0, 0, FILL, FILL);
    htmlCnr.setBackForeColors(Color.WHITE, Color.BLACK);
    htmlCnr.setDocument(doc);
    htmlCnr.focusTraversable = false;
    ScrollBar.extraSize = scrollBarExtraSize;
    Button.commonGap = buttonCommonGap;
    setFocus();
  }

  protected void setFocus() {
    // search for the topmost edit
    int minY = 100000;
    Control c = null;
    Control[] cc = doc.getBagChildren();
    if (cc != null) {
      for (int i = cc.length; --i >= 0;) {
        if (cc[i] instanceof Edit) {
          if (cc[i].getY() < minY) {
            minY = cc[i].getY();
            c = cc[i];
          }
        }
      }
    }
    if (c != null) {
      c.requestFocus();
    } else {
      htmlCnr.requestFocus();
    }
  }

  @Override
  public void onEvent(Event event) {
    if (event.type == ControlEvent.PRESSED && event.target == htmlCnr) {
      String link = htmlCnr.pressedLink;
      if (type == ACTIVATION_START) {
        saveUserParams(link.substring(link.indexOf('?') + 1));
      }
      postPressedEvent();
    }
  }

  private void saveUserParams(String link) {
    String[] fields = Convert.tokenizeString(link, '&');
    for (int i = 0; i < fields.length; i++) {
      String[] fieldAndValue = Convert.tokenizeString(fields[i], '=');
      if (!"SubmitActivation".equals(fieldAndValue[0])) {
        userDefinedParams.put(fieldAndValue[0], fieldAndValue.length == 1 ? "" : fieldAndValue[1]);
      }
    }
  }

  public static Hashtable getUserDefinedParams() {
    return userDefinedParams;
  }

}
