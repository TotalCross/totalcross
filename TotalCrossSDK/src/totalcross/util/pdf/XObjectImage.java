//
// Android PDF Writer
// http://coderesearchlabs.com/androidpdfwriter
//
// by Javier Santo Domingo (j-a-s-d@coderesearchlabs.com)
//

package totalcross.util.pdf;

import totalcross.io.*;
import totalcross.ui.image.*;
import totalcross.util.zip.*;

public class XObjectImage
{

   public static final int BITSPERCOMPONENT_8 = 8;
   public static final String DEVICE_RGB = "/DeviceRGB";

   public static boolean INTERPOLATION = false;
   public static int BITSPERCOMPONENT = BITSPERCOMPONENT_8;
   public static String COLORSPACE = DEVICE_RGB;

   private static int mImageCount = 0;

   private PDFDocument mDocument;
   private IndirectObject mIndirectObject;
   private int mDataSize = 0;
   private int mWidth = -1;
   private int mHeight = -1;
   private String mName = "";
   private String mId = "";
   private String mProcessedImage = "";

   public XObjectImage(PDFDocument document, Image Image) throws IOException
   {
      mDocument = document;
      mProcessedImage = processImage(configureImage(Image));
      mId = Indentifiers.generateId(mProcessedImage);
      mName = "/img" + (++mImageCount);
   }

   public void appendToDocument()
   {
      mIndirectObject = mDocument.newIndirectObject();
      mDocument.includeIndirectObject(mIndirectObject);
      mIndirectObject.addDictionaryContent(" /Type /XObject\n" + " /Subtype /Image\n" + " /Filter [/ASCII85Decode /FlateDecode]\n" + " /Width " + mWidth + "\n"
            + " /Height " + mHeight + "\n" + " /BitsPerComponent " + String.valueOf(BITSPERCOMPONENT) + "\n" + " /Interpolate "
            + String.valueOf(INTERPOLATION) + "\n" + " /ColorSpace " + DEVICE_RGB + "\n" + " /Length " + mProcessedImage.length() + "\n");
      mIndirectObject.addStreamContent(mProcessedImage);
   }

   private Image configureImage(Image image)
   {
      if (image != null)
      {
         mWidth = image.getWidth();
         mHeight = image.getHeight();
         mDataSize = mWidth * mHeight * 3;
      }
      return image;
   }

   private byte[] getImageData(Image image)
   {
      byte[] data = null;
      if (image != null)
      {
         data = new byte[mDataSize];
         int w = mWidth;
         int h = mHeight;
         final int bytesPerPixel = 4;
         byte[] row = new byte[bytesPerPixel * w];
         int offset = 0;
         for (int y = 0; y < h; y++)
         {
            image.getPixelRow(row, y);
            int r = 0;
            for (int x = 0; x < w; x++)
            {
               data[offset++] = row[r++];
               data[offset++] = row[r++];
               data[offset++] = row[r++];
               r++;
            }
            
         }
      }
      return data;
   }

   private boolean deflateImageData(ByteArrayStream baos, byte[] data) throws IOException
   {
      if (data != null)
      {
         ZLib.deflate(new ByteArrayStream(data), baos, ZLib.NO_COMPRESSION);
         return true;
      }
      return false;
   }

   private String encodeImageData(ByteArrayStream baos)
   {
      ByteArrayStream sob = new ByteArrayStream(1024);
      ASCII85Encoder enc85 = new ASCII85Encoder(sob);
      byte[] nl = {(byte)'\n'};
      try
      {
         int i = 0;
         for (byte b : baos.toByteArray())
         {
            enc85.write(b);
            if (i++ == 255)
            {
               sob.writeBytes(nl,0,1);
               i = 0;
            }
         }
         return new String(sob.toByteArray());
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
      return "";
   }

   private String processImage(Image Image) throws IOException
   {
      ByteArrayStream baos = new ByteArrayStream(1024);
      if (deflateImageData(baos, getImageData(Image))) 
         return encodeImageData(baos); 
      return null;
   }

   public String asXObjectReference()
   {
      return mName + " " + mIndirectObject.getIndirectReference();
   }

   public String getName()
   {
      return mName;
   }

   public String getId()
   {
      return mId;
   }

   public int getWidth()
   {
      return mWidth;
   }

   public int getHeight()
   {
      return mHeight;
   }
}
