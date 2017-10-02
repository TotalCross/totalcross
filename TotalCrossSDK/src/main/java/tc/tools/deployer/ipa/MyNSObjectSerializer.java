package tc.tools.deployer.ipa;

import java.io.UnsupportedEncodingException;

import com.dd.plist.NSObject;

public abstract class MyNSObjectSerializer {
  public static String toXMLPropertyList(NSObject o) {
    return o.toXMLPropertyList().replace("\r\n", "\n");
  }

  public static byte[] toXMLPropertyListBytesUTF8(NSObject o) throws UnsupportedEncodingException {
    return toXMLPropertyList(o).replace(".0</real>", "</real>").getBytes("UTF-8");
  }
}
