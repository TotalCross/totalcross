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

import totalcross.io.Stream;
import totalcross.sys.Vm;

public class XmlTokenizer4D {
  // private fields are stripped by the converter
  private int ofsStart;
  private int ofsCur;
  private int ofsEnd;
  private int readPos;
  private int state;
  private int substate;
  private int ixEndTagToSkipTo;
  private byte quote;
  private boolean strictlyXml;
  private boolean resolveCharRef;
  private byte[] endTagToSkipTo;
  byte[] bagRef;

  protected XmlTokenizer4D() {
    substate += ixEndTagToSkipTo + quote + (strictlyXml ? 0 : 1) + (resolveCharRef ? 0 : 1); // remove warnings
    resolveCharRef = true;
    nativeCreate();
  }

  native private void nativeCreate();

  public final void tokenize(byte input[], int offset, int count) throws SyntaxException {
    ofsStart = 0;
    ofsCur = offset;
    ofsEnd = count;
    readPos = offset;
    state = 0;
    foundStartOfInput(input, offset, count);
    tokenizeBytes(input);
    endTokenize(input);
  }

  public final void tokenize(byte input[]) throws SyntaxException {
    tokenize(input, 0, input.length);
  }

  public final void tokenize(Stream input) throws SyntaxException, totalcross.io.IOException {
    byte buffer[] = new byte[1024];
    tokenize(input, buffer, 0, input.readBytes(buffer, 0, buffer.length), 0);
  }

  public final void tokenize(Stream input, byte[] buffer, int start, int end, int pos)
      throws SyntaxException, totalcross.io.IOException {
    ofsStart = start;
    ofsCur = start;
    ofsEnd = end;
    readPos = pos;
    state = 0;
    foundStartOfInput(buffer, 0, ofsEnd);
    while (ofsCur < ofsEnd) {
      tokenizeBytes(buffer); // returns when ofsCur == ofsEnd
      if (ofsEnd == buffer.length) {
        // no more room
        if (ofsStart > 0) { // tidy is still possible
          Vm.arrayCopy(buffer, ofsStart, buffer, 0, ofsEnd - ofsStart);
          readPos += ofsStart;
          ofsCur -= ofsStart;
          ofsStart = 0;
        } else if (((state == 10) || (state == 22)) && (ofsCur > 0)) { // "Data" mode: flush
          foundCharacterData(buffer, 0, ofsCur);
          Vm.arrayCopy(buffer, 0, buffer, 0, ofsEnd - ofsCur);
          readPos += ofsCur;
          ofsCur = ofsStart = 0;
        } else { // nothing else to do than to extend
          byte oldBuffer[] = buffer;
          int newSize = oldBuffer.length * 15 / 10; // guich@510_17: instead of double, grow 50%...
          buffer = new byte[newSize];
          Vm.arrayCopy(oldBuffer, 0, buffer, 0, ofsEnd);
        }
      }
      ofsEnd = ofsCur + input.readBytes(buffer, ofsCur, buffer.length - ofsCur);
    }
    endTokenize(buffer);
  }

  public final int getAbsoluteOffset() {
    return ofsStart + readPos;
  }

  public final boolean isDataCDATA() {
    return (endTagToSkipTo != null);
  }

  public final void setStrictlyXml(boolean toSet) {
    strictlyXml = toSet;
  }

  public final void disableReferenceResolution(boolean disable) {
    resolveCharRef = !disable;
  }

  protected void foundStartOfInput(byte input[], int offset, int count) {
  }

  protected void foundStartTagName(byte input[], int offset, int count) {
  }

  protected void foundEndTagName(byte input[], int offset, int count) {
  }

  protected void foundEndEmptyTag() {
  }

  protected void foundCharacterData(byte input[], int offset, int count) {
  }

  protected void foundCharacter(char charFound) {
  }

  protected void foundAttributeName(byte input[], int offset, int count) {
  }

  protected void foundAttributeValue(byte input[], int offset, int count, byte dlm) {
  }

  protected void foundComment(byte input[], int offset, int count) {
  }

  protected void foundProcessingInstruction(byte input[], int offset, int count) {
  }

  protected void foundDeclaration(byte input[], int offset, int count) {
  }

  protected void foundReference(byte input[], int offset, int count) {
  }

  protected void foundInvalidData(byte input[], int offset, int count) {
  }

  protected void foundEndOfInput(int count) {
  }

  native public static char resolveCharacterReference(byte input[], int offset, int count);

  native protected void setCdataContents(byte input[], int offset, int count);

  native private void tokenizeBytes(byte input[]) throws SyntaxException;

  native private void endTokenize(byte[] input) throws SyntaxException;
}

/* ============================================================================== */
