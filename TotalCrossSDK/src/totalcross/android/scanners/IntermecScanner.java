/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.android.scanners;

import android.view.*;
import com.intermec.aidc.*;
import java.util.concurrent.*;

import totalcross.*;
import totalcross.android.*;

/**
 * Scanner class for Android.
 */
public class IntermecScanner implements IScanner
{ 
   private final static int INTERMEC_AUSTRALIAN_POST = 1;
   private final static int INTERMEC_AZTEC = 2;
   private final static int INTERMEC_BPO = 3;
   private final static int INTERMEC_CANADA_POST = 4;
   private final static int INTERMEC_CODABAR = 5;
   private final static int INTERMEC_CODABLOCK_A = 6;
   private final static int INTERMEC_CODABLOCK_F = 7;
   private final static int INTERMEC_CODE_11 = 8;
   private final static int INTERMEC_CODE_128 = 9;
   private final static int INTERMEC_CODE_GS1_128 = 10;
   private final static int INTERMEC_CODE_ISBT_128 = 11;
   private final static int INTERMEC_CODE_39 = 12;
   private final static int INTERMEC_CODE_93 = 13;
   private final static int INTERMEC_DATA_MATRIX = 14;
   private final static int INTERMEC_DUTCH_POST = 15;
   private final static int INTERMEC_EAN_UPC_EAN_13 = 16;
   private final static int INTERMEC_EAN_UPC_EAN_8 = 17;
   private final static int INTERMEC_EAN_UPC_UPCA = 18;
   private final static int INTERMEC_EAN_UPC_UPC_E = 19;
   private final static int INTERMEC_EAN_UPC_UPC_E1 = 20;
   private final static int INTERMEC_GS1_COMPOSITE = 21;
   private final static int INTERMEC_GS1_COMPOSITE_C = 22;
   private final static int INTERMEC_GS1_DATA_BAR_EXPANDED = 23;            
   private final static int INTERMEC_GS1_DATA_BAR_LIMITED = 24;            
   private final static int INTERMEC_GS1_OMINI_DIRECTIONAL = 25;            
   private final static int INTERMEC_HAN_XIN = 26;            
   private final static int INTERMEC_INFOMAIL = 27;
   private final static int INTERMEC_INTELLIGENT_MAIL = 28;
   private final static int INTERMEC_INTERLEAVED_2_OF_5 = 29;
   private final static int INTERMEC_JAPAN_POST = 30;
   private final static int INTERMEC_MATRIX_2_OF_5 = 31;
   private final static int INTERMEC_MAXICODE = 32;
   private final static int INTERMEC_MICRO_PDF_417 = 33;
   private final static int INTERMEC_MSI = 34;
   private final static int INTERMEC_PDF_417 = 35;
   private final static int INTERMEC_PLANET = 36;
   private final static int INTERMEC_PLESSEY = 37;
   private final static int INTERMEC_POSTNET = 38;
   private final static int INTERMEC_QR_CODE = 39;
   private final static int INTERMEC_STANDARD_2_OF_5 = 40;
   private final static int INTERMEC_SWEDEN_POST = 41;
   private final static int INTERMEC_TELEPEN = 42;
   private final static int INTERMEC_TLC_39 = 43;
   
   private BarcodeReader bcr;
   private VirtualWedge wedg;
   private boolean isOk;
   private Semaphore semaphore = new Semaphore(1);
   
   public boolean scannerActivate()
   {
      // Make sure the BarcodeReader dependent service is connected and
      // register a callback to service connect and disconnect events.
      AidcManager.connectService(Launcher4A.loader, new AidcManager.IServiceListener() 
      {
         public void onConnect()
         {  		    
			   try
            {
			      semaphore.acquire();				   
		      }		           
            catch (InterruptedException exception) {}
			   attachBarcodeReader(); // The dependent service is connected and it is ready to receive barcode requests.
			   semaphore.release();				
		   }

         public void onDisconnect() {}

      });
      return isOk;
   }
   
   public boolean setBarcodeParam(int barcodeType, boolean enable)
   {
      if (bcr == null)
         return false;
      
      try
	   {	
         semaphore.acquire();	
	   }
	   catch (InterruptedException exception) {}
		switch (barcodeType)
		{
         case INTERMEC_AUSTRALIAN_POST:
            bcr.symbology.australianPost.setEnable(enable);
            break;
         case INTERMEC_AZTEC:
            bcr.symbology.aztec.setEnable(enable);
            break;
         case INTERMEC_BPO:
            bcr.symbology.bpo.setEnable(enable);
            break;
         case INTERMEC_CANADA_POST:
            bcr.symbology.canadaPost.setEnable(enable);
            break;
         case INTERMEC_CODABAR:
            bcr.symbology.codabar.setEnable(enable);
            break;
         case INTERMEC_CODABLOCK_A:
            bcr.symbology.codablockA.setEnable(enable);
            break;
         case INTERMEC_CODABLOCK_F:
            bcr.symbology.codablockF.setEnable(enable);
            break;
         case INTERMEC_CODE_11:
            bcr.symbology.code11.setEnable(enable);
            break;
         case INTERMEC_CODE_128:
            bcr.symbology.code128.setEnable(enable);
            break;        
         case INTERMEC_CODE_GS1_128:
            bcr.symbology.code128.setGS1_128Enable(enable);
            break;
         case INTERMEC_CODE_ISBT_128:
            bcr.symbology.code128.setISBT128Enable(enable);
            break;
         case INTERMEC_CODE_39:    
            bcr.symbology.code39.setEnable(enable);
            break;   
         case INTERMEC_CODE_93:
            bcr.symbology.code93.setEnable(enable);
            break;   
         case INTERMEC_DATA_MATRIX:
            bcr.symbology.datamatrix.setEnable(enable);
            break;
         case INTERMEC_DUTCH_POST:
            bcr.symbology.dutchPost.setEnable(enable);
            break;
         case INTERMEC_EAN_UPC_EAN_13:
            bcr.symbology.eanUpc.setEan13Enable(enable);
            break;
         case INTERMEC_EAN_UPC_EAN_8:
            bcr.symbology.eanUpc.setEan8Enable(enable);
            break;
         case INTERMEC_EAN_UPC_UPCA:
            bcr.symbology.eanUpc.setUPCAEnable(enable);
            break;
         case INTERMEC_EAN_UPC_UPC_E:
            bcr.symbology.eanUpc.setUPCEEnable(enable);
            break;
         case INTERMEC_EAN_UPC_UPC_E1:
            bcr.symbology.eanUpc.setUPCE1Enable(enable);
            break;            
         case INTERMEC_GS1_COMPOSITE:       
            bcr.symbology.gs1Composite.setEnable(enable);
            break;      
         case INTERMEC_GS1_COMPOSITE_C:
            bcr.symbology.gs1Composite.setGS1CompositeCEnable(enable);
            break;
         case INTERMEC_GS1_DATA_BAR_EXPANDED:       
            bcr.symbology.gs1DataBarExpanded.setEnable(enable);
            break;   
         case INTERMEC_GS1_DATA_BAR_LIMITED:    
            bcr.symbology.gs1DataBarLimited.setEnable(enable);
            break;   
         case INTERMEC_GS1_OMINI_DIRECTIONAL:       
            bcr.symbology.gs1DataBarOmniDirectional.setEnable(enable);
            break;   
         case INTERMEC_HAN_XIN:    
            bcr.symbology.hanXin.setEnable(enable);
            break;
         case INTERMEC_INFOMAIL:    
            bcr.symbology.infomail.setEnable(enable);
            break;
         case INTERMEC_INTELLIGENT_MAIL:    
            bcr.symbology.intelligentMail.setEnable(enable);
            break;
         case INTERMEC_INTERLEAVED_2_OF_5:    
            bcr.symbology.interleaved2Of5.setEnable(enable);
            break;
         case INTERMEC_JAPAN_POST:       
            bcr.symbology.japanPost.setEnable(enable);
            break;
         case INTERMEC_MATRIX_2_OF_5:       
            bcr.symbology.matrix2Of5.setEnable(enable);
            break;
         case INTERMEC_MAXICODE:       
            bcr.symbology.maxicode.setEnable(enable);
            break;
         case INTERMEC_MICRO_PDF_417:       
            bcr.symbology.microPdf417.setEnable(enable);
            break;
         case INTERMEC_MSI:       
            bcr.symbology.msi.setEnable(enable);
            break;
         case INTERMEC_PDF_417:    
            bcr.symbology.pdf417.setEnable(enable);
            break;
         case INTERMEC_PLANET:       
            bcr.symbology.planet.setEnable(enable);
            break;
         case INTERMEC_PLESSEY:       
            bcr.symbology.plessey.setEnable(enable);
            break;
         case INTERMEC_POSTNET:    
            bcr.symbology.postnet.setEnable(enable);
            break;
         case INTERMEC_QR_CODE: 
            bcr.symbology.qrCode.setEnable(enable);
            break;
         case INTERMEC_STANDARD_2_OF_5:    
            bcr.symbology.standard2Of5.setEnable(enable);
            break;
         case INTERMEC_SWEDEN_POST:       
            bcr.symbology.swedenPost.setEnable(enable);
            break;
         case INTERMEC_TELEPEN:
            bcr.symbology.telepen.setEnable(enable);
            break;
         case INTERMEC_TLC_39:
            bcr.symbology.tlc39.setEnable(enable);
            break;
      }
      semaphore.release();
      return true;
   }
   
   public String getData()
   {
      String strBarcodeData;
	   Semaphore loaderSemaphore = Loader.semaphore;
	  
	   try
	   {
         loaderSemaphore.acquire();		 
      }
	   catch (InterruptedException exception) {}
      strBarcodeData = Launcher4A.loader.strBarcodeData;
	   loaderSemaphore.release();
	  
	   return strBarcodeData;
   }
   
   public boolean deactivate()
   {
      if (bcr == null) return true;
      boolean ret = true;
      
      try
      { 
         if (bcr != null)
         {           	 
             bcr.removeBarcodeReadListener(Launcher4A.loader);             
             bcr.setScannerEnable(false);
             bcr.close();
         }
      }
	   catch (Exception exception)
      {
         String message = exception.getMessage();
         AndroidUtils.debug(message != null? message : "Could not deactivate scanner.");     
         ret = false;		 
      }
      
      try
      {	  
         if (wedg != null)
         {
            wedg.setEnable(true);
            wedg = null;
         }
	   }
      catch (Exception exception)
      {
         String message = exception.getMessage();
         AndroidUtils.debug(message != null? message : "Could not deactivate scanner.");        
         ret = false;   
	   }        
         
      try
		{
		   AidcManager.disconnectService(); //disconnect from data collection service
      }
		catch (Exception exception) 
		{
		   String message = exception.getMessage();
         AndroidUtils.debug(message != null? message : "Could not deactivate scanner.");
         ret = false; 
		}
		
      bcr = null;
      return ret;  
   }
   
   private void attachBarcodeReader()
   {
      isOk = true;
      
      try
      {             
		   if ((wedg = new VirtualWedge()).isEnabled())
            wedg.setEnable(false); // disable virtual wedge  
	   }
      catch (Exception exception)
      {
	      String message = exception.getMessage();
         AndroidUtils.debug(message != null? message : "Could not activate scanner.");		 
		   wedg = null;
		   isOk = false;
      }
      
      try
      {                        
         if (!(bcr = new BarcodeReader()).isScannerEnabled())
		      bcr.setScannerEnable(true); // set barcode reader object for internal scanner
        
         bcr.addBarcodeReadListener(Launcher4A.loader); // add barcode reader listener
      }
      catch (Exception exception)
      {
	      String message = exception.getMessage();
         AndroidUtils.debug(message != null? message : "Could not activate scanner.");
		   bcr = null;
		   isOk = false;
      }
   }

   public boolean checkScanner(KeyEvent event)
   {
      return false;
   }
}