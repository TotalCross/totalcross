// Copyright (C) 2003-2004 Pierre G. Richard
// Copyright (C) 2004-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.xml;

import totalcross.sys.Convert;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

/**
 * This class describes the attributes attached to a start-tag. Tags are case-insensitive.
 * <P>
 * The <code>AttributeListIterator</code> class provides an iterator over the components of each attribute in this </code>AttributeList</code>
 * instance:
 * <UL>
 * <LI>the attribute name
 * <LI>the unquoted value
 * <LI>the quote, if one exists
 * </UL>
 */
public class AttributeList extends Hashtable {
  Filter filter;

  /** 
   * Set to true if you want the get/set methods to be case insensitive. 
   */
  public boolean caseInsensitive; // guich@tc113_29

  /**
   * Constructs an empty <code>attributeList</code> with 5 free positions. 
   */
  public AttributeList() {
    super(5);
  }

  /**
   * Clones the given <code>attributeList</code>.
   * 
   * @param source The <code>attributeList</code> to be cloned.
   */
  public AttributeList(AttributeList source) {
    super(source.size());
    Vector keys = source.getKeys();
    Vector values = source.getValues();
    int max = keys.size();
    for (int i = 0; i < max; ++i) {
      put(getKey((String) keys.items[i]), values.items[i]);
    }
  }

  private String getKey(String k) // guich@tc113_29
  {
    return caseInsensitive ? k.toLowerCase() : k; // guich@tc113_12: added toLowerCase
  }

  /**
   * Adds a new attribute to this AttributeList
   *
   * @param attrName The name of the attribute.
   * @param attrValue The unquoted value of the attribute.
   * @param dlm Delimiter that started the attribute value (<code>'</code> or <code>"</code>), or <code>'\0'</code> if none.
   */
  public final void addAttribute(String attrName, String attrValue, byte dlm) {
    attrName = getKey(attrName); // guich@tc113_12: convert toLowerCase
    if ((filter == null) || filter.acceptAttribute(attrName, attrValue, dlm)) {
      put(attrName, new AttributeValue(attrValue, dlm));
    }
  }

  /**
   * Gets the attribute value for a given name.
   *
   * @param name The attribute name.
   * @return The value, or <code>null</code> if it wasn't specified.
   */
  public final String getAttributeValue(String name) {
    AttributeValue value = (AttributeValue) get(getKey(name)); // guich@tc113_12: convert toLowerCase
    if (value != null) {
      return value.value;
    }
    return null;
  }

  /**
   * Gets the attribute value for a given name as an integer.
   *
   * @param name The attribute name.
   * @param defaultValue The default value if there is no value for the attribute name.
   * @return The value, or the default value if it wasn't specified or there's a problem in the number conversion.
   */
  public final int getAttributeValueAsInt(String name, int defaultValue) {
    AttributeValue value = (AttributeValue) get(getKey(name)); // guich@tc113_12: convert toLowerCase
    try {
      return Convert.toInt(value.value);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  /**
   * Sets an <code>AttributeList.Filter</code> to filter the attribute entered in this <code>AttributeList</code>.
   *
   * @param filter The <code>AttributeList.Filter</code> to set, or <code>null</code> if the current <code>AttributeList</code> filter must be 
   * removed.
   * @return The previous <code>AttributeList.Filter</code> or <code>null</code> if none was set.
   */
  Filter setFilter(Filter filter) {
    Filter old = this.filter;
    this.filter = filter;
    return old;
  }

  /** 
   * Interface to filter each attribute before entering it in the <code>AttributeList</code>.
   */
  public interface Filter {
    /**
     * Call back to check if an attribute must be entered.
     *
     * @param attrName The name of the attribute.
     * @param attrValue The unquoted value of the attribute.
     * @param dlm Delimiter that started the attribute value (<code>'</code> or <code>"</code>), or <code>'\0'</code> if none.
     * @return <code>true</code> if the attribute can be entered; <code>false</code>, otherwise.
     */
    boolean acceptAttribute(String attrName, String attrValue, byte dlm);
  }

  /** 
   * Class to iterate over each attribute of an <code>AttributeList</code> 
   */
  public class Iterator {
    private int current;
    private String currentName;
    private AttributeValue currentValue;
    private Vector keys;
    private static final String singleQuote = "\'";
    private static final String doubleQuote = "\"";
    private static final String noQuote = "";

    /**
     * Constructs an <code>Iterator</code> over each attribute in the outer <code>AttributeList</code>.
     */
    public Iterator() {
      this.keys = getKeys();
    }

    /**
     * Makes current the next attribute in this <code>AttributeList</code>.
     *
     * @return <code>true</code> if the next attribute was activated, or <code>false</code> if there are no more attribute in this list.
     */
    public final boolean next() {
      if (current < size()) {
        currentName = (String) keys.items[current++];
        currentValue = (AttributeValue) get(currentName);
        return true;
      } else {
        currentName = null;
        currentValue = null;
        return false;
      }
    }

    /**
     * Gets the name of the current attribute.
     *
     * @return The name of the current attribute, or <code>null</code> if no attribute is actually current.
     */
    public final String getAttributeName() {
      return currentName;
    }

    /**
     * Gets the unquoted value of the current attribute.
     *
     * @return The unquoted value of the current attribute, or <code>null</code> if no attribute is actually current.
     */
    public final String getAttributeValue() {
      if (currentValue != null) {
        return currentValue.value;
      }
      return null;
    }

    /**
     * Gets the quote surrounding the value of the current attribute.
     *
     * @return <code>\'</code>, <code>\"</code>, or <code>\0</code> (the latter when no quote were surrounding the attribute value) or 
     * <code>null</code> if no attribute is actually current.
     */
    public final String getValueDelimiter() {
      if (currentValue != null) {
        switch (currentValue.dlm) {
        case (byte) '\'':
          return singleQuote;
        case (byte) '\"':
          return doubleQuote;
        default:
          return noQuote;
        }
      }
      return null;
    }

    /**
     * Gets the attribute as a string, the value being surrounded by single or double quotes if these were specified in the start-tag.
     *
     * @return The string a described above.
     */
    public final String getAttributeAsString() {
      if (currentValue != null) {
        String dlm = getValueDelimiter();
        return currentName + "=" + dlm + currentValue.value + dlm;
      }
      return null;
    }
  }

  /** Private class to store Attribute values with their delimiter */
  private static class AttributeValue {
    private String value;
    private byte dlm;

    /**
     * Constructor
     *
     * @param value unquoted value for this AttributeValue
     * @param delemiter single quote, double quote or 0
     */
    AttributeValue(String value, byte dlm) {
      this.value = value;
      this.dlm = dlm;
    }
  }
}
