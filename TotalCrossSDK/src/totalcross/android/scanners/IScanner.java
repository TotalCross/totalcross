package totalcross.android.scanners;

import android.view.*;

public interface IScanner
{
   public boolean scannerActivate();
   public boolean setBarcodeParam(int barcodeType, boolean enable);
   public String getData();
   public boolean deactivate();
   public boolean checkScanner(KeyEvent event);
}
