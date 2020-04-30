// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.ui.html;

import totalcross.util.ElementNotFoundException;
import totalcross.util.IntHashtable;

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
  public static final String escape(String s) {
    if (amp == null) {
      amp = "&";
    }
    StringBuffer sb = new StringBuffer(s.length() * 12 / 10);
    for (int i = 0, n = s.length(); i < n; i++) {
      char c = s.charAt(i);
      switch (c) {
      case 0:
        break; //flsobral@tc120_58: ignore character of value 0, because it is not a valid value.
      case 39:
        sb.append(amp).append("#39;");
        break; // '''
      case 60:
        sb.append(amp).append("lt;");
        break; // '<'
      case 62:
        sb.append(amp).append("gt;");
        break; // '>'
      case 173:
        sb.append(amp).append("shy;");
        break; // '­'
      case 34:
        sb.append(amp).append("quot;");
        break; // '"'
      case 38:
        sb.append(amp).append("amp;");
        break; // '&'
      case 161:
        sb.append(amp).append("iexcl;");
        break; // '¡'
      case 166:
        sb.append(amp).append("brvbar;");
        break; // '¦'
      case 168:
        sb.append(amp).append("uml;");
        break; // '¨'
      case 175:
        sb.append(amp).append("macr;");
        break; // '¯'
      case 180:
        sb.append(amp).append("acute;");
        break; // '´'
      case 184:
        sb.append(amp).append("cedil;");
        break; // '¸'
      case 191:
        sb.append(amp).append("iquest;");
        break; // '¿'
      case 177:
        sb.append(amp).append("plusmn;");
        break; // '±'
      case 171:
        sb.append(amp).append("laquo;");
        break; // '«'
      case 187:
        sb.append(amp).append("raquo;");
        break; // '»'
      case 215:
        sb.append(amp).append("times;");
        break; // '×'
      case 247:
        sb.append(amp).append("divide;");
        break; // '÷'
      case 162:
        sb.append(amp).append("cent;");
        break; // '¢'
      case 163:
        sb.append(amp).append("pound;");
        break; // '£'
      case 164:
        sb.append(amp).append("curren;");
        break; // '¤'
      case 165:
        sb.append(amp).append("yen;");
        break; // '¥'
      case 167:
        sb.append(amp).append("sect;");
        break; // '§'
      case 169:
        sb.append(amp).append("copy;");
        break; // '©'
      case 172:
        sb.append(amp).append("not;");
        break; // '¬'
      case 174:
        sb.append(amp).append("reg;");
        break; // '®'
      case 176:
        sb.append(amp).append("deg;");
        break; // '°'
      case 181:
        sb.append(amp).append("micro;");
        break; // 'µ'
      case 182:
        sb.append(amp).append("para;");
        break; // '¶'
      case 183:
        sb.append(amp).append("middot;");
        break; // '·'
      case 8364:
        sb.append(amp).append("euro;");
        break; // ''
      case 188:
        sb.append(amp).append("frac14;");
        break; // '¼'
      case 189:
        sb.append(amp).append("frac12;");
        break; // '½'
      case 190:
        sb.append(amp).append("frac34;");
        break; // '¾'
      case 185:
        sb.append(amp).append("sup1;");
        break; // '¹'
      case 178:
        sb.append(amp).append("sup2;");
        break; // '²'
      case 179:
        sb.append(amp).append("sup3;");
        break; // '³'
      case 225:
        sb.append(amp).append("aacute;");
        break; // 'á'
      case 193:
        sb.append(amp).append("Aacute;");
        break; // 'Á'
      case 226:
        sb.append(amp).append("acirc;");
        break; // 'â'
      case 194:
        sb.append(amp).append("Acirc;");
        break; // 'Â'
      case 224:
        sb.append(amp).append("agrave;");
        break; // 'à'
      case 192:
        sb.append(amp).append("Agrave;");
        break; // 'À'
      case 229:
        sb.append(amp).append("aring;");
        break; // 'å'
      case 197:
        sb.append(amp).append("Aring;");
        break; // 'Å'
      case 227:
        sb.append(amp).append("atilde;");
        break; // 'ã'
      case 195:
        sb.append(amp).append("Atilde;");
        break; // 'Ã'
      case 228:
        sb.append(amp).append("auml;");
        break; // 'ä'
      case 196:
        sb.append(amp).append("Auml;");
        break; // 'Ä'
      case 170:
        sb.append(amp).append("ordf;");
        break; // 'ª'
      case 230:
        sb.append(amp).append("aelig;");
        break; // 'æ'
      case 198:
        sb.append(amp).append("AElig;");
        break; // 'Æ'
      case 231:
        sb.append(amp).append("ccedil;");
        break; // 'ç'
      case 199:
        sb.append(amp).append("Ccedil;");
        break; // 'Ç'
      case 208:
        sb.append(amp).append("ETH;");
        break; // 'Ð'
      case 240:
        sb.append(amp).append("eth;");
        break; // 'ð'
      case 233:
        sb.append(amp).append("eacute;");
        break; // 'é'
      case 201:
        sb.append(amp).append("Eacute;");
        break; // 'É'
      case 234:
        sb.append(amp).append("ecirc;");
        break; // 'ê'
      case 202:
        sb.append(amp).append("Ecirc;");
        break; // 'Ê'
      case 232:
        sb.append(amp).append("egrave;");
        break; // 'è'
      case 200:
        sb.append(amp).append("Egrave;");
        break; // 'È'
      case 235:
        sb.append(amp).append("euml;");
        break; // 'ë'
      case 203:
        sb.append(amp).append("Euml;");
        break; // 'Ë'
      case 237:
        sb.append(amp).append("iacute;");
        break; // 'í'
      case 205:
        sb.append(amp).append("Iacute;");
        break; // 'Í'
      case 238:
        sb.append(amp).append("icirc;");
        break; // 'î'
      case 206:
        sb.append(amp).append("Icirc;");
        break; // 'Î'
      case 236:
        sb.append(amp).append("igrave;");
        break; // 'ì'
      case 204:
        sb.append(amp).append("Igrave;");
        break; // 'Ì'
      case 239:
        sb.append(amp).append("iuml;");
        break; // 'ï'
      case 207:
        sb.append(amp).append("Iuml;");
        break; // 'Ï'
      case 241:
        sb.append(amp).append("ntilde;");
        break; // 'ñ'
      case 209:
        sb.append(amp).append("Ntilde;");
        break; // 'Ñ'
      case 243:
        sb.append(amp).append("oacute;");
        break; // 'ó'
      case 211:
        sb.append(amp).append("Oacute;");
        break; // 'Ó'
      case 244:
        sb.append(amp).append("ocirc;");
        break; // 'ô'
      case 212:
        sb.append(amp).append("Ocirc;");
        break; // 'Ô'
      case 242:
        sb.append(amp).append("ograve;");
        break; // 'ò'
      case 210:
        sb.append(amp).append("Ograve;");
        break; // 'Ò'
      case 186:
        sb.append(amp).append("ordm;");
        break; // 'º'
      case 248:
        sb.append(amp).append("oslash;");
        break; // 'ø'
      case 216:
        sb.append(amp).append("Oslash;");
        break; // 'Ø'
      case 245:
        sb.append(amp).append("otilde;");
        break; // 'õ'
      case 213:
        sb.append(amp).append("Otilde;");
        break; // 'Õ'
      case 246:
        sb.append(amp).append("ouml;");
        break; // 'ö'
      case 214:
        sb.append(amp).append("Ouml;");
        break; // 'Ö'
      case 223:
        sb.append(amp).append("szlig;");
        break; // 'ß'
      case 254:
        sb.append(amp).append("thorn;");
        break; // 'þ'
      case 222:
        sb.append(amp).append("THORN;");
        break; // 'Þ'
      case 250:
        sb.append(amp).append("uacute;");
        break; // 'ú'
      case 218:
        sb.append(amp).append("Uacute;");
        break; // 'Ú'
      case 251:
        sb.append(amp).append("ucirc;");
        break; // 'û'
      case 219:
        sb.append(amp).append("Ucirc;");
        break; // 'Û'
      case 249:
        sb.append(amp).append("ugrave;");
        break; // 'ù'
      case 217:
        sb.append(amp).append("Ugrave;");
        break; // 'Ù'
      case 252:
        sb.append(amp).append("uuml;");
        break; // 'ü'
      case 220:
        sb.append(amp).append("Uuml;");
        break; // 'Ü'
      case 253:
        sb.append(amp).append("yacute;");
        break; // 'ý'
      case 221:
        sb.append(amp).append("Yacute;");
        break; // 'Ý'
      case 255:
        sb.append(amp).append("yuml;");
        break; // 'ÿ'
      default:
        sb.append(c);
        break;
      }
    }
    return sb.toString();
  }

  private static IntHashtable ht;
  static {
    ht = new IntHashtable(235); // 10 collisions
    ht.put("#39", 39); // '''
    ht.put("lt", 60); // '<'
    ht.put("gt", 62); // '>'
    ht.put("shy", 173); // '­'
    ht.put("quot", 34); // '"'
    ht.put("amp", 38); // '&'
    ht.put("iexcl", 161); // '¡'
    ht.put("brvbar", 166); // '¦'
    ht.put("uml", 168); // '¨'
    ht.put("macr", 175); // '¯'
    ht.put("acute", 180); // '´'
    ht.put("cedil", 184); // '¸'
    ht.put("iquest", 191); // '¿'
    ht.put("plusmn", 177); // '±'
    ht.put("laquo", 171); // '«'
    ht.put("raquo", 187); // '»'
    ht.put("times", 215); // '×'
    ht.put("divide", 247); // '÷'
    ht.put("cent", 162); // '¢'
    ht.put("pound", 163); // '£'
    ht.put("curren", 164); // '¤'
    ht.put("yen", 165); // '¥'
    ht.put("sect", 167); // '§'
    ht.put("copy", 169); // '©'
    ht.put("not", 172); // '¬'
    ht.put("reg", 174); // '®'
    ht.put("deg", 176); // '°'
    ht.put("micro", 181); // 'µ'
    ht.put("para", 182); // '¶'
    ht.put("middot", 183); // '·'
    ht.put("euro", 8364); // ''
    ht.put("frac14", 188); // '¼'
    ht.put("frac12", 189); // '½'
    ht.put("frac34", 190); // '¾'
    ht.put("sup1", 185); // '¹'
    ht.put("sup2", 178); // '²'
    ht.put("sup3", 179); // '³'
    ht.put("aacute", 225); // 'á'
    ht.put("Aacute", 193); // 'Á'
    ht.put("acirc", 226); // 'â'
    ht.put("Acirc", 194); // 'Â'
    ht.put("agrave", 224); // 'à'
    ht.put("Agrave", 192); // 'À'
    ht.put("aring", 229); // 'å'
    ht.put("Aring", 197); // 'Å'
    ht.put("atilde", 227); // 'ã'
    ht.put("Atilde", 195); // 'Ã'
    ht.put("auml", 228); // 'ä'
    ht.put("Auml", 196); // 'Ä'
    ht.put("ordf", 170); // 'ª'
    ht.put("aelig", 230); // 'æ'
    ht.put("AElig", 198); // 'Æ'
    ht.put("ccedil", 231); // 'ç'
    ht.put("Ccedil", 199); // 'Ç'
    ht.put("ETH", 208); // 'Ð'
    ht.put("eth", 240); // 'ð'
    ht.put("eacute", 233); // 'é'
    ht.put("Eacute", 201); // 'É'
    ht.put("ecirc", 234); // 'ê'
    ht.put("Ecirc", 202); // 'Ê'
    ht.put("egrave", 232); // 'è'
    ht.put("Egrave", 200); // 'È'
    ht.put("euml", 235); // 'ë'
    ht.put("Euml", 203); // 'Ë'
    ht.put("iacute", 237); // 'í'
    ht.put("Iacute", 205); // 'Í'
    ht.put("icirc", 238); // 'î'
    ht.put("Icirc", 206); // 'Î'
    ht.put("igrave", 236); // 'ì'
    ht.put("Igrave", 204); // 'Ì'
    ht.put("iuml", 239); // 'ï'
    ht.put("Iuml", 207); // 'Ï'
    ht.put("ntilde", 241); // 'ñ'
    ht.put("Ntilde", 209); // 'Ñ'
    ht.put("oacute", 243); // 'ó'
    ht.put("Oacute", 211); // 'Ó'
    ht.put("ocirc", 244); // 'ô'
    ht.put("Ocirc", 212); // 'Ô'
    ht.put("ograve", 242); // 'ò'
    ht.put("Ograve", 210); // 'Ò'
    ht.put("ordm", 186); // 'º'
    ht.put("oslash", 248); // 'ø'
    ht.put("Oslash", 216); // 'Ø'
    ht.put("otilde", 245); // 'õ'
    ht.put("Otilde", 213); // 'Õ'
    ht.put("ouml", 246); // 'ö'
    ht.put("Ouml", 214); // 'Ö'
    ht.put("szlig", 223); // 'ß'
    ht.put("thorn", 254); // 'þ'
    ht.put("THORN", 222); // 'Þ'
    ht.put("uacute", 250); // 'ú'
    ht.put("Uacute", 218); // 'Ú'
    ht.put("ucirc", 251); // 'û'
    ht.put("Ucirc", 219); // 'Û'
    ht.put("ugrave", 249); // 'ù'
    ht.put("Ugrave", 217); // 'Ù'
    ht.put("uuml", 252); // 'ü'
    ht.put("Uuml", 220); // 'Ü'
    ht.put("yacute", 253); // 'ý'
    ht.put("Yacute", 221); // 'Ý'
    ht.put("yuml", 255); // 'ÿ'
  }

  /** Converts escaped sequences into special chars. Note that the space IS converted if it appears escaped. */
  public static final String unescape(String escaped) {
    //totalcross.sys.Vm.debug("coll: "+ht.collisions);
    char[] chars = escaped.toCharArray();
    StringBuffer sb = new StringBuffer(chars.length);
    int last = 0;
    int i = 0;
    while (true) {
      int s0 = escaped.indexOf('&', i);
      if (s0 == -1) {
        break;
      }
      int sf = escaped.indexOf(';', s0);
      if (sf == -1) {
        break;
      }
      int len = sf - s0 - 1;
      if (2 <= len && len <= 6) // is this a valid symbol?
      {
        // compute the hash to avoid creating a string
        int hash = 0;
        for (int j = s0 + 1; j < sf; j++) {
          hash = (hash << 5) - hash + chars[j];
        }
        //String token = escaped.substring(s0+1,sf); int hash = token.hashCode();
        try {
          int to = ht.get(hash);
          if (s0 > last) {
            sb.append(chars, last, s0 - last);
          }
          sb.append((char) to);
          last = sf + 1;
        } catch (ElementNotFoundException e) {
        }
        i = sf + 1;
      } else {
        i++;
      }
    }
    if (i < chars.length) {
      sb.append(chars, i, chars.length - i);
    }
    return sb.toString();
  }
}
