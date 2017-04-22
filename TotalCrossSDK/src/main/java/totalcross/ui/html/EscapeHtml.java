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



package totalcross.ui.html;

import totalcross.util.*;

/** A class that can be used to convert escape characters in a Html string.
 * @since SuperWaba 5.71
 */
public class EscapeHtml // guich@571_10
{
   /** When passing the escaped string to a SOAP service, you must change this to 
    * <pre>
    * EscapeHtml.amp = "&amp;";
    * </pre>
    * and then set back to null when done.
    * @since TotalCross 1.14
    */
   public static String amp; // guich@tc114_10

   /** Converts special chars into escaped sequences. Note that the space is NOT converted. */
   public static final String escape(String s)
   {
      if (amp == null)
         amp = "&";
      StringBuffer sb = new StringBuffer(s.length()*12/10);
      for (int i = 0, n = s.length(); i < n; i++)
      {
         char c = s.charAt(i);
         switch (c)
         {
            case 0   : break; //flsobral@tc120_58: ignore character of value 0, because it is not a valid value.
            case 39  : sb.append(amp).append("#39;"); break;      // '''
            case 60  : sb.append(amp).append("lt;"); break;       // '<'
            case 62  : sb.append(amp).append("gt;"); break;       // '>'
            case 173 : sb.append(amp).append("shy;"); break;      // '�'
            case 34  : sb.append(amp).append("quot;"); break;     // '"'
            case 38  : sb.append(amp).append("amp;"); break;      // '&'
            case 161 : sb.append(amp).append("iexcl;"); break;    // '�'
            case 166 : sb.append(amp).append("brvbar;"); break;   // '�'
            case 168 : sb.append(amp).append("uml;"); break;      // '�'
            case 175 : sb.append(amp).append("macr;"); break;     // '�'
            case 180 : sb.append(amp).append("acute;"); break;    // '�'
            case 184 : sb.append(amp).append("cedil;"); break;    // '�'
            case 191 : sb.append(amp).append("iquest;"); break;   // '�'
            case 177 : sb.append(amp).append("plusmn;"); break;   // '�'
            case 171 : sb.append(amp).append("laquo;"); break;    // '�'
            case 187 : sb.append(amp).append("raquo;"); break;    // '�'
            case 215 : sb.append(amp).append("times;"); break;    // '�'
            case 247 : sb.append(amp).append("divide;"); break;   // '�'
            case 162 : sb.append(amp).append("cent;"); break;     // '�'
            case 163 : sb.append(amp).append("pound;"); break;    // '�'
            case 164 : sb.append(amp).append("curren;"); break;   // '�'
            case 165 : sb.append(amp).append("yen;"); break;      // '�'
            case 167 : sb.append(amp).append("sect;"); break;     // '�'
            case 169 : sb.append(amp).append("copy;"); break;     // '�'
            case 172 : sb.append(amp).append("not;"); break;      // '�'
            case 174 : sb.append(amp).append("reg;"); break;      // '�'
            case 176 : sb.append(amp).append("deg;"); break;      // '�'
            case 181 : sb.append(amp).append("micro;"); break;    // '�'
            case 182 : sb.append(amp).append("para;"); break;     // '�'
            case 183 : sb.append(amp).append("middot;"); break;   // '�'
            case 8364: sb.append(amp).append("euro;"); break;     // '�'
            case 188 : sb.append(amp).append("frac14;"); break;   // '�'
            case 189 : sb.append(amp).append("frac12;"); break;   // '�'
            case 190 : sb.append(amp).append("frac34;"); break;   // '�'
            case 185 : sb.append(amp).append("sup1;"); break;     // '�'
            case 178 : sb.append(amp).append("sup2;"); break;     // '�'
            case 179 : sb.append(amp).append("sup3;"); break;     // '�'
            case 225 : sb.append(amp).append("aacute;"); break;   // '�'
            case 193 : sb.append(amp).append("Aacute;"); break;   // '�'
            case 226 : sb.append(amp).append("acirc;"); break;    // '�'
            case 194 : sb.append(amp).append("Acirc;"); break;    // '�'
            case 224 : sb.append(amp).append("agrave;"); break;   // '�'
            case 192 : sb.append(amp).append("Agrave;"); break;   // '�'
            case 229 : sb.append(amp).append("aring;"); break;    // '�'
            case 197 : sb.append(amp).append("Aring;"); break;    // '�'
            case 227 : sb.append(amp).append("atilde;"); break;   // '�'
            case 195 : sb.append(amp).append("Atilde;"); break;   // '�'
            case 228 : sb.append(amp).append("auml;"); break;     // '�'
            case 196 : sb.append(amp).append("Auml;"); break;     // '�'
            case 170 : sb.append(amp).append("ordf;"); break;     // '�'
            case 230 : sb.append(amp).append("aelig;"); break;    // '�'
            case 198 : sb.append(amp).append("AElig;"); break;    // '�'
            case 231 : sb.append(amp).append("ccedil;"); break;   // '�'
            case 199 : sb.append(amp).append("Ccedil;"); break;   // '�'
            case 208 : sb.append(amp).append("ETH;"); break;      // '�'
            case 240 : sb.append(amp).append("eth;"); break;      // '�'
            case 233 : sb.append(amp).append("eacute;"); break;   // '�'
            case 201 : sb.append(amp).append("Eacute;"); break;   // '�'
            case 234 : sb.append(amp).append("ecirc;"); break;    // '�'
            case 202 : sb.append(amp).append("Ecirc;"); break;    // '�'
            case 232 : sb.append(amp).append("egrave;"); break;   // '�'
            case 200 : sb.append(amp).append("Egrave;"); break;   // '�'
            case 235 : sb.append(amp).append("euml;"); break;     // '�'
            case 203 : sb.append(amp).append("Euml;"); break;     // '�'
            case 237 : sb.append(amp).append("iacute;"); break;   // '�'
            case 205 : sb.append(amp).append("Iacute;"); break;   // '�'
            case 238 : sb.append(amp).append("icirc;"); break;    // '�'
            case 206 : sb.append(amp).append("Icirc;"); break;    // '�'
            case 236 : sb.append(amp).append("igrave;"); break;   // '�'
            case 204 : sb.append(amp).append("Igrave;"); break;   // '�'
            case 239 : sb.append(amp).append("iuml;"); break;     // '�'
            case 207 : sb.append(amp).append("Iuml;"); break;     // '�'
            case 241 : sb.append(amp).append("ntilde;"); break;   // '�'
            case 209 : sb.append(amp).append("Ntilde;"); break;   // '�'
            case 243 : sb.append(amp).append("oacute;"); break;   // '�'
            case 211 : sb.append(amp).append("Oacute;"); break;   // '�'
            case 244 : sb.append(amp).append("ocirc;"); break;    // '�'
            case 212 : sb.append(amp).append("Ocirc;"); break;    // '�'
            case 242 : sb.append(amp).append("ograve;"); break;   // '�'
            case 210 : sb.append(amp).append("Ograve;"); break;   // '�'
            case 186 : sb.append(amp).append("ordm;"); break;     // '�'
            case 248 : sb.append(amp).append("oslash;"); break;   // '�'
            case 216 : sb.append(amp).append("Oslash;"); break;   // '�'
            case 245 : sb.append(amp).append("otilde;"); break;   // '�'
            case 213 : sb.append(amp).append("Otilde;"); break;   // '�'
            case 246 : sb.append(amp).append("ouml;"); break;     // '�'
            case 214 : sb.append(amp).append("Ouml;"); break;     // '�'
            case 223 : sb.append(amp).append("szlig;"); break;    // '�'
            case 254 : sb.append(amp).append("thorn;"); break;    // '�'
            case 222 : sb.append(amp).append("THORN;"); break;    // '�'
            case 250 : sb.append(amp).append("uacute;"); break;   // '�'
            case 218 : sb.append(amp).append("Uacute;"); break;   // '�'
            case 251 : sb.append(amp).append("ucirc;"); break;    // '�'
            case 219 : sb.append(amp).append("Ucirc;"); break;    // '�'
            case 249 : sb.append(amp).append("ugrave;"); break;   // '�'
            case 217 : sb.append(amp).append("Ugrave;"); break;   // '�'
            case 252 : sb.append(amp).append("uuml;"); break;     // '�'
            case 220 : sb.append(amp).append("Uuml;"); break;     // '�'
            case 253 : sb.append(amp).append("yacute;"); break;   // '�'
            case 221 : sb.append(amp).append("Yacute;"); break;   // '�'
            case 255 : sb.append(amp).append("yuml;"); break;     // '�'
            default  : sb.append(c); break;
         }
      }
      return sb.toString();
   }

   private static IntHashtable ht;
   static
   {
      ht = new IntHashtable(235); // 10 collisions
      ht.put("#39"    ,39   );   // '''
      ht.put("lt"     ,60   );   // '<'
      ht.put("gt"     ,62   );   // '>'
      ht.put("shy"    ,173  );   // '�'
      ht.put("quot"   ,34   );   // '"'
      ht.put("amp"    ,38   );   // '&'
      ht.put("iexcl"  ,161  );   // '�'
      ht.put("brvbar" ,166  );   // '�'
      ht.put("uml"    ,168  );   // '�'
      ht.put("macr"   ,175  );   // '�'
      ht.put("acute"  ,180  );   // '�'
      ht.put("cedil"  ,184  );   // '�'
      ht.put("iquest" ,191  );   // '�'
      ht.put("plusmn" ,177  );   // '�'
      ht.put("laquo"  ,171  );   // '�'
      ht.put("raquo"  ,187  );   // '�'
      ht.put("times"  ,215  );   // '�'
      ht.put("divide" ,247  );   // '�'
      ht.put("cent"   ,162  );   // '�'
      ht.put("pound"  ,163  );   // '�'
      ht.put("curren" ,164  );   // '�'
      ht.put("yen"    ,165  );   // '�'
      ht.put("sect"   ,167  );   // '�'
      ht.put("copy"   ,169  );   // '�'
      ht.put("not"    ,172  );   // '�'
      ht.put("reg"    ,174  );   // '�'
      ht.put("deg"    ,176  );   // '�'
      ht.put("micro"  ,181  );   // '�'
      ht.put("para"   ,182  );   // '�'
      ht.put("middot" ,183  );   // '�'
      ht.put("euro"   ,8364 );   // '�'
      ht.put("frac14" ,188  );   // '�'
      ht.put("frac12" ,189  );   // '�'
      ht.put("frac34" ,190  );   // '�'
      ht.put("sup1"   ,185  );   // '�'
      ht.put("sup2"   ,178  );   // '�'
      ht.put("sup3"   ,179  );   // '�'
      ht.put("aacute" ,225  );   // '�'
      ht.put("Aacute" ,193  );   // '�'
      ht.put("acirc"  ,226  );   // '�'
      ht.put("Acirc"  ,194  );   // '�'
      ht.put("agrave" ,224  );   // '�'
      ht.put("Agrave" ,192  );   // '�'
      ht.put("aring"  ,229  );   // '�'
      ht.put("Aring"  ,197  );   // '�'
      ht.put("atilde" ,227  );   // '�'
      ht.put("Atilde" ,195  );   // '�'
      ht.put("auml"   ,228  );   // '�'
      ht.put("Auml"   ,196  );   // '�'
      ht.put("ordf"   ,170  );   // '�'
      ht.put("aelig"  ,230  );   // '�'
      ht.put("AElig"  ,198  );   // '�'
      ht.put("ccedil" ,231  );   // '�'
      ht.put("Ccedil" ,199  );   // '�'
      ht.put("ETH"    ,208  );   // '�'
      ht.put("eth"    ,240  );   // '�'
      ht.put("eacute" ,233  );   // '�'
      ht.put("Eacute" ,201  );   // '�'
      ht.put("ecirc"  ,234  );   // '�'
      ht.put("Ecirc"  ,202  );   // '�'
      ht.put("egrave" ,232  );   // '�'
      ht.put("Egrave" ,200  );   // '�'
      ht.put("euml"   ,235  );   // '�'
      ht.put("Euml"   ,203  );   // '�'
      ht.put("iacute" ,237  );   // '�'
      ht.put("Iacute" ,205  );   // '�'
      ht.put("icirc"  ,238  );   // '�'
      ht.put("Icirc"  ,206  );   // '�'
      ht.put("igrave" ,236  );   // '�'
      ht.put("Igrave" ,204  );   // '�'
      ht.put("iuml"   ,239  );   // '�'
      ht.put("Iuml"   ,207  );   // '�'
      ht.put("ntilde" ,241  );   // '�'
      ht.put("Ntilde" ,209  );   // '�'
      ht.put("oacute" ,243  );   // '�'
      ht.put("Oacute" ,211  );   // '�'
      ht.put("ocirc"  ,244  );   // '�'
      ht.put("Ocirc"  ,212  );   // '�'
      ht.put("ograve" ,242  );   // '�'
      ht.put("Ograve" ,210  );   // '�'
      ht.put("ordm"   ,186  );   // '�'
      ht.put("oslash" ,248  );   // '�'
      ht.put("Oslash" ,216  );   // '�'
      ht.put("otilde" ,245  );   // '�'
      ht.put("Otilde" ,213  );   // '�'
      ht.put("ouml"   ,246  );   // '�'
      ht.put("Ouml"   ,214  );   // '�'
      ht.put("szlig"  ,223  );   // '�'
      ht.put("thorn"  ,254  );   // '�'
      ht.put("THORN"  ,222  );   // '�'
      ht.put("uacute" ,250  );   // '�'
      ht.put("Uacute" ,218  );   // '�'
      ht.put("ucirc"  ,251  );   // '�'
      ht.put("Ucirc"  ,219  );   // '�'
      ht.put("ugrave" ,249  );   // '�'
      ht.put("Ugrave" ,217  );   // '�'
      ht.put("uuml"   ,252  );   // '�'
      ht.put("Uuml"   ,220  );   // '�'
      ht.put("yacute" ,253  );   // '�'
      ht.put("Yacute" ,221  );   // '�'
      ht.put("yuml"   ,255  );   // '�'
   }
   /** Converts escaped sequences into special chars. Note that the space IS converted if it appears escaped. */
   public static final String unescape(String escaped)
   {
      //totalcross.sys.Vm.debug("coll: "+ht.collisions);
      char []chars = escaped.toCharArray();
      StringBuffer sb = new StringBuffer(chars.length);
      int last =0;
      int i = 0;
      while (true)
      {
         int s0 = escaped.indexOf('&',i);
         if (s0 == -1)
            break;
         int sf = escaped.indexOf(';',s0);
         if (sf == -1)
            break;
         int len = sf-s0-1;
         if (2 <= len && len <= 6) // is this a valid symbol?
         {
            // compute the hash to avoid creating a string
            int hash = 0;
            for (int j = s0+1; j < sf; j++)
               hash = (hash<<5) - hash + chars[j];
            //String token = escaped.substring(s0+1,sf); int hash = token.hashCode();
            try
            {
               int to = ht.get(hash);
               if (s0 > last) sb.append(chars,last,s0-last);
               sb.append((char)to);
               last = sf+1;
            } catch (ElementNotFoundException e) {}
            i = sf+1;
         }
         else i++;
      }
      if (i < chars.length)
         sb.append(chars,i,chars.length-i);
      return sb.toString();
   }
}
