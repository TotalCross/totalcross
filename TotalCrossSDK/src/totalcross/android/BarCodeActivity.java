package totalcross.android;

import android.app.Activity;
import android.os.Bundle;
import com.intermec.aidc.*; 
import totalcross.*;
 
 public class BarCodeActivity extends Activity implements BarcodeReadListener
 { 
     private com.intermec.aidc.BarcodeReader bcr;
     private com.intermec.aidc.VirtualWedge wedg;
     String strDeviceId, strBarcodeData, strSymbologyId;    

     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
             
         // Make sure the BarcodeReader dependent service is connected and
         // register a callback to service connect and disconnect events.
         AidcManager.connectService(this, new AidcManager.IServiceListener() 
         {
             public void onConnect()
             {
                 // The dependent service is connected and it is ready
                 // to receive barcode requests.
                doBarcodReader();
             }
 
             public void onDisconnect()
             {
             }

         });
     }

     public void doBarcodReader()
     {
          try
          { 
              //disable virtual wedge
              wedg = new VirtualWedge();
              wedg.setEnable(false);  
                          
              //set barcode reader object for internal scanner
              bcr = new BarcodeReader();

              //add barcode reader listener
              bcr.addBarcodeReadListener(this);

          }
          catch (BarcodeReaderException bcrexp)
          {
              int errCode = bcrexp.getErrorCode();
              String errMessage = bcrexp.getErrorMessage();
          }
          catch (SymbologyException sym)
          {
              String errMessage = sym.getErrorMessage();
          }
          catch (SymbologyOptionsException symOp)
          {
              String errMessage = symOp.getErrorMessage();
          }
          catch (VirtualWedgeException exception)
          {
             String errMessage = exception.getErrorMessage();
          }
     }

     public void barcodeRead(BarcodeReadEvent aBarcodeReadEvent)
     {
          strDeviceId =  aBarcodeReadEvent.getDeviceId();
          strBarcodeData =  aBarcodeReadEvent.getBarcodeData();
          strSymbologyId = aBarcodeReadEvent.getSymbolgyId();

          //update data to edit fields
          runOnUiThread(new Runnable() {

              public void run() {
                   AndroidUtils.debug(strDeviceId);
                   AndroidUtils.debug(strBarcodeData);
                   AndroidUtils.debug(strSymbologyId);
              }
          });
     }

     public void onDestroy()
     {
         super.onDestroy();

         if(bcr != null)
         {
             try
             { 
                bcr.removeBarcodeReadListener(this);
             }
             catch (BarcodeReaderException exception)
             {
                
             }
             bcr.close();
             bcr = null;
         }
                        
         wedg = null;
         //disconnect from data collection service
         AidcManager.disconnectService();
     }
 }