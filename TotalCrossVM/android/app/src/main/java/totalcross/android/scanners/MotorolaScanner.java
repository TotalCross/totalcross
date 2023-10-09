package totalcross.android.scanners;

import android.content.*;
import android.view.*;

import totalcross.*;
import com.symbol.emdk.*;
import com.symbol.emdk.barcode.*;
import java.util.ArrayList;

public class MotorolaScanner implements IScanner {
  private boolean isActive;
  private String barcode;
  private Scanner scanner;

  @Override
  public boolean scannerActivate() {
    EMDKResults results = EMDKManager.getEMDKManager(Launcher4A.loader.getApplicationContext(),
        new EMDKManager.EMDKListener() {

          @Override
          public void onOpened(EMDKManager emdkManager) {
            BarcodeManager barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
            scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);

            if (!scanner.isEnabled()) {
              try {
                scanner.enable();
              } catch (Exception e) {
                e.printStackTrace();
              }
            }

            scanner.addStatusListener(new Scanner.StatusListener() {

              @Override
              public void onStatus(StatusData statusData) {
                if (statusData.getState() == StatusData.ScannerStates.IDLE) {
                  try {
                    scanner.read();
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                }
              }
            });

            scanner.addDataListener(new Scanner.DataListener() {

              @Override
              public void onData(ScanDataCollection scanDataCollection) {
                ArrayList<ScanDataCollection.ScanData> scanDataList = scanDataCollection.getScanData();
                for (ScanDataCollection.ScanData scanData : scanDataList) {
                  barcode = scanData.getData();
                  Launcher4A.instance._postEvent(Launcher4A.BARCODE_READ, 0, 0, 0, 0, 0);
                }
              }
            });
            try {
              scanner.read();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onClosed() {

          }
        });
    return isActive = (results.statusCode == EMDKResults.STATUS_CODE.SUCCESS);
  }

  @Override
  public boolean setBarcodeParam(int barcodeType, boolean enable) {
    return true;
  }

  @Override
  public String getData() {
    String b = barcode;
    barcode = "";
    return b;
  }

  @Override
  public boolean deactivate() {
    if (scanner.isEnabled()) {
      try {
        scanner.disable();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    return isActive = false;
  }

  @Override
  public boolean checkScanner(KeyEvent event) {
    return false;
  }

  @Override
  public void setParam(String what, String value) {
  }
}
