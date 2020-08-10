package totalcross.android.cielo;

import java.util.HashMap;

import cielo.printer.client.PrinterAttributes;
import cielo.sdk.order.PrinterListener;
import cielo.sdk.printer.PrinterManager;

import totalcross.Launcher4A;
import totalcross.AndroidUtils;

public class PrinterManager4A {

  public static void cieloPrintManagerPrintText(String textToPrint, String printerAttributes) {
      HashMap<String, Integer> attributes = null;
      
      if (printerAttributes != null) {
          attributes =  new HashMap<>();
          
          String[] elements = printerAttributes.split("&");
          for(String s1: elements) {
              String[] keyValue = s1.split("=");
              try {
                  attributes.put(keyValue[0], Integer.valueOf(keyValue[1]));
              } catch (NumberFormatException e) {
                  // just ignore
              }
          }
      }
      
      PrinterManager printerManager = new PrinterManager(Launcher4A.loader.getApplicationContext());
      PrinterListener listener = new PrinterListener() {
          @Override
          public void onWithoutPaper() {
              AndroidUtils.debug("printer without paper");
          }

          @Override
          public void onPrintSuccess() {
              AndroidUtils.debug("print success!");
          }

          @Override
          public void onError(Throwable throwable) {
              AndroidUtils.debug(
                  String.format("printer error -> %s", throwable.getMessage()));
          }
      };
      
      printerManager.printText(textToPrint, attributes, listener);
  }
}
