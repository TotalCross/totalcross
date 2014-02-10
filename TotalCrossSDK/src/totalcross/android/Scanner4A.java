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

package totalcross.android;

import totalcross.*;
import android.content.*;
import com.intermec.aidc.*; 
import totalcross.android.*;
import java.util.concurrent.*;

/**
 * Scanner class for Android.
 */
public class Scanner4A
{ 
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Australian Post barcode type.
    */
   private final static int INTERMEC_AUSTRALIAN_POST = 1;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Aztec barcode type.
    */
   private final static int INTERMEC_AZTEC = 2;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable BPO barcode type.
    */
   private final static int INTERMEC_BPO = 3;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Canada Post barcode type.
    */
   private final static int INTERMEC_CANADA_POST = 4;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Codabar barcode type.
    */
   private final static int INTERMEC_CODABAR = 5;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Codablock A barcode type.
    */
   private final static int INTERMEC_CODABLOCK_A = 6;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Codablock F barcode type.
    */
   private final static int INTERMEC_CODABLOCK_F = 7;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Code 11 barcode type.
    */
   private final static int INTERMEC_CODE_11 = 8;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Code 128 barcode type.
    */
   private final static int INTERMEC_CODE_128 = 9;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Code GS1-128 barcode type.
    */
   private final static int INTERMEC_CODE_GS1_128 = 10;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Code ISBT-128 barcode type.
    */
   private final static int INTERMEC_CODE_ISBT_128 = 11;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Code 39 barcode type.
    */
   private final static int INTERMEC_CODE_39 = 12;
          
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Code 93 barcode type.
    */
   private final static int INTERMEC_CODE_93 = 13;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Data Matrix barcode type.
    */
   private final static int INTERMEC_DATA_MATRIX = 14;
      
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Dutch Post barcode type.
    */
   private final static int INTERMEC_DUTCH_POST = 15;
      
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable EAN/UPC EAN-13 barcode type.
    */
   private final static int INTERMEC_EAN_UPC_EAN_13 = 16;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable EAN/UPC EAN-8 barcode type.
    */
   private final static int INTERMEC_EAN_UPC_EAN_8 = 17;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable EAN/UPC UPCA barcode type.
    */
   private final static int INTERMEC_EAN_UPC_UPCA = 18;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable EAN/UPC UPC-E barcode type.
    */
   private final static int INTERMEC_EAN_UPC_UPC_E = 19;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable EAN/UPC UPC-E1 barcode type.
    */
   private final static int INTERMEC_EAN_UPC_UPC_E1 = 20;
            
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable GS1 Composite barcode type.
    */
   private final static int INTERMEC_GS1_COMPOSITE = 21;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable GS1 Composite C barcode type.
    */
   private final static int INTERMEC_GS1_COMPOSITE_C = 22;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable GS1 DataBar Expanded barcode type.
    */
   private final static int INTERMEC_GS1_DATA_BAR_EXPANDED = 23;            
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable GS1 DataBar Limited barcode type.
    */
   private final static int INTERMEC_GS1_DATA_BAR_LIMITED = 24;            
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable GS1 DataBar Omnidirectional barcode type.
    */
   private final static int INTERMEC_GS1_OMINI_DIRECTIONAL = 25;            
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable HanXin barcode type.
    */
   private final static int INTERMEC_HAN_XIN = 26;            
     
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Infomail barcode type.
    */
   private final static int INTERMEC_INFOMAIL = 27;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Intelligent Mail barcode type.
    */
   private final static int INTERMEC_INTELLIGENT_MAIL = 28;

   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Interleaved 2 of 5 barcode type.
    */
   private final static int INTERMEC_INTERLEAVED_2_OF_5 = 29;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Japan Post barcode type.
    */
   private final static int INTERMEC_JAPAN_POST = 30;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Matrix 2 of 5 barcode type.
    */
   private final static int INTERMEC_MATRIX_2_OF_5 = 31;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Maxicode barcode type.
    */
   private final static int INTERMEC_MAXICODE = 32;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Micro PDF 417 barcode type.
    */
   private final static int INTERMEC_MICRO_PDF_417 = 33;

   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable MSI barcode type.
    */
   private final static int INTERMEC_MSI = 34;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable PDF 417 barcode type.
    */
   private final static int INTERMEC_PDF_417 = 35;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Planet barcode type.
    */
   private final static int INTERMEC_PLANET = 36;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Plessey barcode type.
    */
   private final static int INTERMEC_PLESSEY = 37;

   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Postnet barcode type.
    */
   private final static int INTERMEC_POSTNET = 38;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable QR Code barcode type.
    */
   private final static int INTERMEC_QR_CODE = 39;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Standard 2 of 5 barcode type.
    */
   private final static int INTERMEC_STANDARD_2_OF_5 = 40;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Sweden Post barcode type.
    */
   private final static int INTERMEC_SWEDEN_POST = 41;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable Telepen barcode type.
    */
   private final static int INTERMEC_TELEPEN = 42;
   
   /**
    * To be used with Intermec android scanners, in the <code>setBarcodeParam()</code> method, to enable or disable TLC 39 barcode type.
    */
   private final static int INTERMEC_TLC_39 = 43;
   
   private static BarcodeReader bcr;
   private static VirtualWedge wedg;
   private static boolean isOk;
   private static Semaphore semaphore = new Semaphore(1);
   
   static boolean scannerActivate()
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
			doBarcodReader(); // The dependent service is connected and it is ready to receive barcode requests.
			semaphore.release();				
		   }

         public void onDisconnect() {}

      });
      return isOk;
   }
   
   static boolean setBarcodeParam(int barcodeType, boolean enable)
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
   
   static String getData()
   {
      String strBarcodeData;
	  Semaphore loaderSemaphore = Launcher4A.loader.semaphore;
	  
	  try
	   {
         loaderSemaphore.acquire();		 
      }
	   catch (InterruptedException exception) {}
	  
       strBarcodeData = Launcher4A.loader.strBarcodeData;
	   loaderSemaphore.release();
	  
	  return strBarcodeData;
   }
   
   static boolean deactivate()
   {
      try
      { 
         if (bcr != null)
         {           	 
             bcr.removeBarcodeReadListener(Launcher4A.loader);             
             bcr.setScannerEnable(false);
             bcr.close();
             bcr = null;
         }
      }
	  catch (BarcodeReaderException exception)
      {
         String message = exception.getMessage();
         AndroidUtils.debug(message != null? message : "Could not deactivate scanner.");     
         return false;		 
      }
      
      try
      {	  
         if (wedg != null)
         {
            wedg.setEnable(true);
            wedg = null;
         }
	  }
      catch (VirtualWedgeException exception)
      {
         String message = exception.getMessage();
         AndroidUtils.debug(message != null? message : "Could not deactivate scanner.");        
         return false;
	  }        
         //disconnect from data collection service
         try
		 {
		    AidcManager.disconnectService();
         }
		 catch (Exception exception) 
		 {
		    return false;
		 }
		 return true;  
   }
   
   public static void doBarcodReader()
   {
      isOk = true;
      try
      {    
         //disable virtual wedge
		 if ((wedg = new VirtualWedge()).isEnabled())
            wedg.setEnable(false);  
		 else
		    wedg = null;
	  }
      catch (VirtualWedgeException exception)
      {
	     String message = exception.getMessage();
         AndroidUtils.debug(message != null? message : "Could not activate scanner.");		 
		 wedg = null;
		 isOk = false;
      }
      try
      {                
         //set barcode reader object for internal scanner
         if (!(bcr = new BarcodeReader()).isScannerEnabled())
		    bcr.setScannerEnable(true);

         //add barcode reader listener
         bcr.addBarcodeReadListener(Launcher4A.loader);
      }
      catch (BarcodeReaderException exception)
      {
	     String message = exception.getMessage();
         AndroidUtils.debug(message != null? message : "Could not activate scanner.");
		 bcr = null;
		 isOk = false;
      }
   }
}