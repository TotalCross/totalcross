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

package totalcross.net.mail;

import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.sys.Convert;
import totalcross.util.Vector;

/**
 * This class represents a MIME style e-mail message.
 * 
 * @since TotalCross 1.13
 */
public class Message extends Part {
  /** The subject of this message */
  public String subject;

  protected Vector vFrom;
  protected Vector[] vRecipients = new Vector[3];
  /** READ-ONLY FIELD! */
  Vector recipients = new Vector();

  Folder folder;
  int msgNumber;
  public String uidl;
  int size;
  int headerSize;

  boolean expunged = false;

  /**
   * Creates a new empty message.
   * 
   * @since TotalCross 1.13
   */
  public Message() {
    vRecipients[RecipientType.TO] = new Vector();
    vRecipients[RecipientType.CC] = new Vector();
    vRecipients[RecipientType.BCC] = new Vector();
  }

  /**
   * Package constructor used by subclasses of Folder to create a message received from the server.
   * 
   * @param folder
   * @param msgNumber
   * @param uidl
   * @param size
   * @param headerSize
   * @since TotalCross 1.13
   */
  Message(Folder folder, int msgNumber, String uidl, int size, int headerSize) {
    this();
    this.msgNumber = msgNumber;
    this.folder = folder;
    this.size = size;
    this.headerSize = headerSize;
    this.uidl = uidl;
  }

  @Override
  public void addHeader(String name, String value) {
    if (name.length() > 7) {
      headers.addHeader(name, value);
    } else if (name.equalsIgnoreCase("From")) {
      throw new IllegalArgumentException();
    } else if (name.equalsIgnoreCase(RecipientType.recipientPrefix[RecipientType.TO])) {
      throw new IllegalArgumentException();
    } else if (name.equalsIgnoreCase(RecipientType.recipientPrefix[RecipientType.CC])) {
      throw new IllegalArgumentException();
    } else if (name.equalsIgnoreCase(RecipientType.recipientPrefix[RecipientType.BCC])) {
      throw new IllegalArgumentException();
    } else if (name.equalsIgnoreCase("Subject")) {
      throw new IllegalArgumentException();
    } else {
      headers.addHeader(name, value);
    }
  }

  /**
   * Add this recipient address to the existing ones of the given type.
   * 
   * @param type
   *           the recipient type
   * @param address
   *           the address
   * @throws IllegalArgumentException
   *            If the given recipient type is not valid
   * @throws NullPointerException
   *            if the given address is null
   * @since TotalCross 1.13
   */
  public void addRecipient(int type, Address address) {
    if (type < RecipientType.TO || type > RecipientType.BCC) {
      throw new IllegalArgumentException();
    }
    recipients.addElement(address.address);
    vRecipients[type].addElement(address);
  }

  /**
   * Add these recipient addresses to the existing ones of the given type.
   * 
   * @param type
   *           the recipient type
   * @param addresses
   *           the addresses
   * @throws IllegalArgumentException
   *            If the given recipient type is not valid
   * @throws NullPointerException
   *            if addresses is null, or has a null value
   * @since TotalCross 1.13
   */
  public void addRecipients(int type, Address[] addresses) {
    if (type < RecipientType.TO || type > RecipientType.BCC) {
      throw new IllegalArgumentException();
    }

    for (int i = 0; i < addresses.length; i++) {
      recipients.addElement(addresses[i].address);
      vRecipients[type].addElement(addresses[i]);
    }
  }

  /**
   * Add these addresses to the existing "From" attribute.
   * 
   * @param addresses
   *           the senders
   * @since TotalCross 1.22
   */
  public void addFrom(Address[] addresses) {
    if (vFrom == null) {
      vFrom = new Vector();
    }
    vFrom.addElements(addresses);
  }

  /**
   * Set the "From" attribute in this Message.
   * 
   * @param address
   *           the sender
   * @since TotalCross 1.22
   */
  public void setFrom(Address address) {
    if (vFrom == null) {
      vFrom = new Vector();
    } else {
      vFrom.removeAllElements();
    }
    vFrom.addElement(address);
  }

  /**
   * Returns the "From" attribute. The "From" attribute contains the identity of the person(s) who wished this message
   * to be sent. In certain implementations, this may be different from the entity that actually sent the message.
   * 
   * This method returns null if this attribute is not present in this message. Returns an empty array if this
   * attribute is present, but contains no addresses.
   * 
   * @return array of Address objects
   * @since TotalCross 1.22
   */
  public Address[] getFrom() {
    if (vFrom == null) {
      return null;
    }
    int fromSize = vFrom.size();
    if (fromSize == 0) {
      return new Address[0];
    }
    Address[] addresses = new Address[fromSize];
    vFrom.copyInto(addresses);
    return addresses;
  }

  @Override
  public void writeTo(Stream stream) throws MessagingException {
    try {
      //flsobral@tc123_49: moved SMTP commands from Message.writeTo to SMTPTransport.sendMessage - Message.writeTo now correctly supports writing to any type of stream.
      // FROM
      if (vFrom != null) {
        stream.writeBytes("From: ");
        int fromSize = vFrom.size();
        if (fromSize > 0) {
          Address from = (Address) vFrom.items[0];
          stream.writeBytes(from.toString());
          for (int i = 1; i < fromSize; i++) {
            from = (Address) vFrom.items[i];
            stream.writeBytes(", " + from);
          }
        }
        stream.writeBytes(Convert.CRLF_BYTES);
      }

      // TO, CC and BCC
      if (!vRecipients[RecipientType.TO].isEmpty()) {
        stream.writeBytes("To: " + vRecipients[RecipientType.TO].toString(", "));
        stream.writeBytes(Convert.CRLF_BYTES);
      }
      if (!vRecipients[RecipientType.CC].isEmpty()) {
        stream.writeBytes("Cc: " + vRecipients[RecipientType.CC].toString(", "));
        stream.writeBytes(Convert.CRLF_BYTES);
      }
      if (!vRecipients[RecipientType.BCC].isEmpty()) {
        stream.writeBytes("Bcc: " + vRecipients[RecipientType.BCC].toString(", "));
        stream.writeBytes(Convert.CRLF_BYTES);
      }

      // SUBJECT
      stream.writeBytes("Subject: " + this.subject);
      stream.writeBytes(Convert.CRLF_BYTES);

      // MIME VERSION
      stream.writeBytes("MIME-Version: 1.0");
      stream.writeBytes(Convert.CRLF_BYTES);

      super.writeTo(stream);
    } catch (IOException e) {
      throw new MessagingException(e);
    }
  }

  /**
   * Mark this message as deleted, removing it from the parent folder.
   * 
   * @throws MessagingException
   * @since TotalCross 1.13
   */
  public void delete() throws MessagingException {
    if (folder != null) {
      folder.deleteMessage(this);
    }
  }

  /**
   * Get a new Message suitable for a reply to this message. The new Message will have its attributes and headers set
   * up appropriately. Note that this new message object will be empty, that is, it will not have a "content". These
   * will have to be suitably filled in by the client.
   * 
   * If replyToAll is set, the new Message will be addressed to all recipients of this message. Otherwise, the reply
   * will be addressed to only the sender of this message.
   * 
   * The "Subject" field is filled in with the original subject prefixed with "Re:" (unless it already starts with
   * "Re:").
   * 
   * The reply message will use the same session as this message.
   * 
   * NOTE: The replyToAll feature is NOT supported yet, messages returned by this method will contain only the original
   * sender as recipient.
   * 
   * @param replyToAll
   *           reply should be sent to all recipients of this message
   * @return the reply Message
   * @throws AddressException
   * @since TotalCross 1.13
   */
  public Message reply(boolean replyToAll) throws AddressException {
    Message reply = new Message();

    reply.subject = subject;
    if (!reply.subject.startsWith("Re:")) {
      reply.subject = "Re:" + reply.subject;
    }

    if (vFrom != null && !vFrom.isEmpty()) {
      reply.addRecipient(Message.RecipientType.TO, (Address) vFrom.items[0]);
    }

    reply.setFrom(new Address(MailSession.getDefaultInstance().get(MailSession.SMTP_USER).toString(), null));

    return reply;
  }

  public interface RecipientType {
    static final int TO = 0;
    static final int CC = 1;
    static final int BCC = 2;

    static final String[] recipientPrefix = { "To", "Cc", "Bcc" };
  }

  private static final String HEADER_SUBJECT = "\r\nSubject: ";
  private static final String HEADER_FROM = "\r\nFrom: ";
  private static final String HEADER_TO = "\r\nTo:";
  //private static final String HEADER_CC = "\r\nCc: ";
  //private static final String HEADER_BCC = "\r\nBcc: ";
  private static final String HEADER_CONTENT_TYPE = "\r\nContent-type: ";

  void parseHeader(String header) throws IOException, AddressException {
    subject = getAttribute(HEADER_SUBJECT, header);
    String szFrom = getAttribute(HEADER_FROM, header);
    if (szFrom != null) {
      int start = szFrom.indexOf('<');
      if (start != -1) {
        String address = szFrom.substring(start + 1, szFrom.indexOf('>'));
        String personal = null;
        if (start > 0) {
          personal = szFrom.substring(0, start - 1);
        }
        setFrom(new Address(address, personal));
      } else {
        setFrom(new Address(szFrom, null));
      }
    }
    String szTo = getAttribute(HEADER_TO, header);
    if (szTo != null) {
      String[] tokens = Convert.tokenizeString(szTo, ',');
      parseRecipient(RecipientType.TO, tokens);
    }
    // Cc and Bcc are not supported yet!
    // String szCc = getAttribute(HEADER_CC, header);
    // String szBcc = getAttribute(HEADER_BCC, header);

    mimeType = getAttribute(HEADER_CONTENT_TYPE, header);
    if (mimeType == null) {
      mimeType = PLAIN;
    }
  }

  void parseContent(String content) throws IOException {
    //if (mimeType == null || mimeType.equals(Part.PLAIN))
    this.content = content;
  }

  private String getAttribute(String attribute, String source) {
    int start = source.indexOf(attribute);
    if (start == -1) {
      return null;
    }
    start += attribute.length();
    int end = source.indexOf(Convert.CRLF, start);
    if (end == -1) {
      return null;
    }
    String result = source.substring(start, end);
    if (result.startsWith("=?iso-8859-1?Q?")) {
      int endEncoded = result.indexOf("?=");
      int resultLen = result.length();
      String encoded = result.substring(15, endEncoded);
      StringBuffer sb = new StringBuffer(resultLen);
      start = 0;
      while ((end = encoded.indexOf('=', start)) != -1) {
        if (start != end) {
          sb.append(encoded.substring(start, end));
        }
        start = end + 3;
        sb.append(new String(Convert.hexStringToBytes(encoded.substring(end + 1, start))));
      }
      sb.append(encoded.substring(start));
      encoded = sb.toString().replace('_', ' ');
      if (endEncoded + 2 < resultLen) {
        encoded += result.substring(endEncoded + 2);
      }
      result = encoded;
    }
    return result;
  }

  private void parseRecipient(int type, String[] tokens) throws AddressException {
    int len = tokens.length;
    for (int i = 0; i < len; i++) {
      if (tokens[i].length() > 0) {
        int start = tokens[i].indexOf('<');
        if (start != -1) {
          String address = tokens[i].substring(start + 1, tokens[i].indexOf('>'));
          String personal = address;
          if (start > 1) {
            personal = tokens[i].substring(1, start - 1);
          }
          addRecipient(type, new Address(address, personal));
        } else {
          String address = tokens[i].substring(1);
          addRecipient(type, new Address(address, null));
        }
      }
    }
  }
}
