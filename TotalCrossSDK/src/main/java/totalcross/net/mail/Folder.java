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

import totalcross.util.Hashtable;

/**
 * Folder is an abstract class that represents a folder for mail messages. Subclasses implement protocol specific
 * Folders.
 * 
 * @since TotalCross 1.13
 */
public abstract class Folder {
  protected Store store;
  protected Hashtable messagesByUidl;
  protected Hashtable messagesByNumber;
  protected Hashtable expungedMessages;
  protected int messageCount = -1;
  protected int deletedMessageCount;

  public int HEADER_BUFFER_SIZE = 4000;
  protected byte[] msgHeaderBuffer;

  /**
   * Constructor that takes a Store object.
   * 
   * @param store
   *           the Store that holds this folder
   * @since TotalCross 1.13
   */
  protected Folder(Store store) {
    this.store = store;
  }

  /**
   * Open this Folder. This method is valid only on Folders that can contain Messages and that are closed.
   * 
   * @throws MessagingException
   * @since TotalCross 1.13
   */
  public abstract void open() throws MessagingException;

  /**
   * Get total number of messages in this Folder.
   * 
   * This method can be invoked on a closed folder. However, note that for some folder implementations, getting the
   * total message count can be an expensive operation involving actually opening the folder. In such cases, a provider
   * can choose not to support this functionality in the closed state, in which case this method must return -1.
   * 
   * Clients invoking this method on a closed folder must be aware that this is a potentially expensive operation.
   * Clients must also be prepared to handle a return value of -1 in this case.
   * 
   * @return total number of messages. -1 may be returned by certain implementations if this method is invoked on a
   *         closed folder.
   * @since TotalCross 1.13
   */
  public int getMessageCount() throws MessagingException {
    return messageCount;
  }

  public int getDeleteMessageCount() throws MessagingException {
    return deletedMessageCount;
  }

  /**
   * Get all Message objects from this Folder. Returns an empty array if the folder is empty. Clients can use Message
   * objects (instead of sequence numbers) as references to the messages within a folder; this method supplies the
   * Message objects to the client. Folder implementations are expected to provide light-weight Message objects, which
   * get filled on demand.
   * 
   * This implementation invokes uses getMessage() to get Message objects from 1 till the message count.
   * 
   * @return array of Message objects, empty array if folder is empty.
   * @throws MessagingException
   * @since TotalCross 1.13
   */
  public Message[] getMessages() throws MessagingException {
    if (messageCount == -1) {
      return null;
    }

    Message[] msgRet = new Message[messageCount];

    for (int i = 1; i <= messageCount; i++) {
      msgRet[i - 1] = getMessage(i);
    }

    return msgRet;
  }

  /**
   * Get the Message objects for message numbers ranging from start through end, both start and end inclusive. Note
   * that message numbers start at 1, not 0.
   * 
   * Message objects are light-weight references to the actual message that get filled up on demand. Hence Folder
   * implementations are expected to provide light-weight Message objects.
   * 
   * This implementation uses getMessage(index) to obtain the required Message objects. Note that the returned array
   * must contain (end-start+1) Message objects.
   * 
   * @param start
   * @param end
   * @return The Message array
   * @throws MessagingException
   * @since TotalCross 1.13
   */
  public Message[] getMessages(int start, int end) throws MessagingException {
    if (messageCount == -1) {
      return null;
    }
    if (start < 1 || end > messageCount) {
      throw new IndexOutOfBoundsException();
    }

    Message[] msgRet = new Message[end - start + 1];

    for (int i = start; i <= end; i++) {
      msgRet[i - start] = getMessage(i);
    }

    return msgRet;
  }

  /**
   * Get the Message object corresponding to the given message number. A Message object's message number is the
   * relative position of this Message in its Folder. Messages are numbered starting at 1 through the total number of
   * message in the folder. Note that the message number for a particular Message can change during a session if other
   * messages in the Folder are deleted and the Folder is expunged.
   * 
   * Message objects are light-weight references to the actual message that get filled up on demand. Hence Folder
   * implementations are expected to provide light-weight Message objects.
   * 
   * Unlike Folder objects, repeated calls to getMessage with the same message number will return the same Message
   * object, as long as no messages in this folder have been expunged.
   * 
   * Since message numbers can change within a session if the folder is expunged, clients are advised not to use
   * message numbers as references to messages. Use Message objects instead.
   * 
   * @param msgnum
   *           the message number
   * @return the Message object
   * @throws MessagingException
   * @since TotalCross 1.13
   */
  public abstract Message getMessage(int msgnum) throws MessagingException;

  public abstract Message getMessage(String uidl) throws MessagingException;

  /**
   * Reset this session, undoing any operations performed over messages on this folder during this session.
   * @since TotalCross 1.13
   */
  public abstract void reset() throws MessagingException;

  /**
   * Close this Folder. This method is valid only on open Folders.
   * 
   * @param b
   * @since TotalCross 1.13
   */
  public void close(boolean b) {
    if (messageCount == -1) {
      return;
    }
    messageCount = -1;
  }

  abstract void deleteMessage(Message message) throws MessagingException;
}
