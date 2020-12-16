// Copyright (C) 2000-2013 SuperWaba Ltda.
// Copyright (C) 2014-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.xml;

import java.io.UnsupportedEncodingException;
import totalcross.io.Stream;
import totalcross.sys.Vm;

/**
 * Dumb Xml class used only to dump to the Debug Console the contents of a XML.
 * It implements each of the methods that are called by the XmlTokenizer class.
 * You can run this to understand how to handle a XML.
 * 
 * Here's a sample:
 * <pre>
 * File f = new File("data.xml",File.READ_WRITE);
 * new totalcross.xml.DumpXML(f);
 * f.close();
 * </pre> 
 * Then you'll learn how each of the methods of the XmlTokenizer interface are called, and you can then
 * create your own logic to handle the element calls.
 */

public class DumpXml extends XmlTokenizer {
  /** Must call tokenize by yourself. */
  public DumpXml() {
  }

  public DumpXml(Stream stream) throws SyntaxException, totalcross.io.IOException, UnsupportedEncodingException {
    tokenize(stream);
  }

  @Override
  public void foundStartOfInput(char buffer[], int offset, int count) {
    Vm.debug("Start: " + new String(buffer, offset, count));
  }

  @Override
  public void foundStartTagName(char buffer[], int offset, int count) {
    Vm.debug("StartTagName: " + new String(buffer, offset, count));
  }

  @Override
  public void foundEndTagName(char buffer[], int offset, int count) {
    Vm.debug("EndTagName: " + new String(buffer, offset, count));
  }

  @Override
  public void foundEndEmptyTag() {
    Vm.debug("EndEmptyTag");
  }

  @Override
  public void foundCharacterData(char buffer[], int offset, int count) {
    Vm.debug("Content: " + new String(buffer, offset, count));
  }

  @Override
  public void foundCharacter(char charFound) {
    Vm.debug("Content Ref: '" + charFound + "'");
  }

  @Override
  public void foundAttributeName(char buffer[], int offset, int count) {
    Vm.debug("AttributeName: " + new String(buffer, offset, count));
  }

  @Override
  public void foundAttributeValue(char buffer[], int offset, int count, char dlm) {
    Vm.debug("AttributeValue: " + new String(buffer, offset, count));
  }

  @Override
  public void foundEndOfInput(int count) {
    Vm.debug("Ended: " + count + " bytes parsed.");
  }
}
