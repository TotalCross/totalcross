//
// Android PDF Writer
// http://coderesearchlabs.com/androidpdfwriter
//
// by Javier Santo Domingo (j-a-s-d@coderesearchlabs.com)
//

package totalcross.util.pdf;

public class Header extends Base
{

   private String mVersion;
   private String mRenderedHeader;

   public Header()
   {
      clear();
   }

   public void setVersion(int Major, int Minor)
   {
      mVersion = Integer.toString(Major) + "." + Integer.toString(Minor);
      render();
   }

   public int getPDFStringSize()
   {
      return mRenderedHeader.length();
   }

   private void render()
   {
      mRenderedHeader = "%PDF-" + mVersion + "\n%©»ªµ\n";
   }

   public String toPDFString()
   {
      return mRenderedHeader;
   }

   public void clear()
   {
      setVersion(1, 4);
   }

}
