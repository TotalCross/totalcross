package totalcross.json;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
import java.util.Iterator;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

/**
 * Converts a Property file data into JSONObject and back.
 * @author JSON.org
 * @version 2014-05-03
 */
public class Property {
  /**
   * Converts a property file object into a JSONObject. The property file object is a table of name value pairs.
   * @param properties java.util.Properties
   * @return JSONObject
   * @throws JSONException
   */
  public static JSONObject toJSONObject(/*java.util.Properties*/Hashtable properties) throws JSONException {
    JSONObject jo = new JSONObject();
    if (properties != null && properties.size() > 0) {
      Vector v = properties.getKeys();//Enumeration enumProperties = properties.propertyNames();
      for (int i = 0, n = v.size(); i < n; i++) {
        String name = (String) v.items[i];//enumProperties.nextElement();
        jo.put(name, properties.get(name));
      }
    }
    return jo;
  }

  /**
   * Converts the JSONObject into a property file object.
   * @param jo JSONObject
   * @return java.util.Properties
   * @throws JSONException
   */
  public static /*Properties*/Hashtable toProperties(JSONObject jo) throws JSONException {
    /*Properties*/Hashtable properties = new Hashtable(jo.length());
    if (jo != null) {
      Iterator<String> keys = jo.keys();
      while (keys.hasNext()) {
        String name = keys.next();
        properties.put(name, jo.getString(name));
      }
    }
    return properties;
  }
}
