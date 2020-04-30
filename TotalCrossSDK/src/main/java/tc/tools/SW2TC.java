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
package tc.tools;

import totalcross.io.File;
import totalcross.sys.Time;
import totalcross.util.Hashtable;
import totalcross.util.Vector;

public class SW2TC {
  Hashtable htImports;
  Vector vClassNamesSource, vClassNamesTarget, vFullClassNamesSource, vFullClassNamesTarget;

  String[] tofrom = {
      // target class                   , target package                      , source class                     , source package                                   ,
      "Address", "totalcross.pim.palm.builtin", "Address", "superwaba.ext.palm.io.builtin", "AddressBook",
      "totalcross.pim.addressbook", "AddressBook", "superwaba.ext.xplat.io.pimal.addressbook", "AddressField",
      "totalcross.pim.addressbook", "AddressField", "superwaba.ext.xplat.io.pimal.addressbook",
      "AddressNotSupportedHandler", "totalcross.pim.addressbook", "AddressNotSupportedHandler",
      "superwaba.ext.xplat.io.pimal.addressbook", "AddressNSHNote", "totalcross.pim.addressbook", "AddressNSHNote",
      "superwaba.ext.xplat.io.pimal.addressbook", "AddressRecord", "totalcross.pim.addressbook", "AddressRecord",
      "superwaba.ext.xplat.io.pimal.addressbook", "Adler32", "totalcross.util.zip", "Adler32",
      "superwaba.ext.xplat.util.zip", "AnimatedButton", "totalcross.game", "AnimatedButton", "superwaba.ext.xplat.game",
      "AnimatedSprite", "totalcross.game", "AnimatedSprite", "superwaba.ext.xplat.game", "Animation", "totalcross.game",
      "Animation", "superwaba.ext.xplat.game", "AnimationEvent", "totalcross.game", "AnimationEvent",
      "superwaba.ext.xplat.game", "AppExitException", "totalcross.sys", "AppExitException", "waba.sys",
      "ArithmeticException", "totalcross.lang", "ArithmeticException", "waba.lang", "ArrayIndexOutOfBoundsException",
      "totalcross.lang", "ArrayIndexOutOfBoundsException", "waba.lang", "AssertionFailedError", "totalcross.unit",
      "AssertionFailedError", "superwaba.ext.xplat.unit", "AttributeList", "totalcross.xml", "AttributeList",
      "superwaba.ext.xplat.xml", "Base64", "totalcross.net", "Base64", "superwaba.ext.xplat.io.http",
      "CompressedByteArrayStream", "totalcross.io", "BigByteArrayStream", "superwaba.ext.xplat.io", "BufferedFile",
      "totalcross.io", "BufferedFile", "superwaba.ext.xplat.io", "Button", "totalcross.ui", "Button", "waba.ui",
      "Button", "totalcross.ui.html.ui", "Button", "superwaba.ext.xplat.html.ui", "ByteArrayStream", "totalcross.io",
      "ByteArrayStream", "waba.io", "ByteString", "totalcross.net", "ByteString", "superwaba.ext.xplat.io.http",
      "CalculatorBox", "totalcross.ui.dialog", "Calculator", "waba.ui", "CalendarBox", "totalcross.ui.dialog",
      "Calendar", "waba.ui", "CharacterConverter", "totalcross.sys", "CharacterConverter", "waba.sys", "Check",
      "totalcross.ui", "Check", "waba.ui", "Check", "totalcross.ui.html.ui", "Check", "superwaba.ext.xplat.html.ui",
      "Checksum", "totalcross.util.zip", "Checksum", "superwaba.ext.xplat.util.zip.checksum", "ControlBox",
      "totalcross.ui.dialog", "ChoicesDialog", "superwaba.ext.xplat.ui", "Class", "totalcross.lang", "Class",
      "waba.lang", "ClassCastException", "totalcross.lang", "ClassCastException", "waba.lang", "ClassNotFoundException",
      "totalcross.lang", "ClassNotFoundException", "waba.lang", "Color", "totalcross.ui.gfx", "Color", "waba.fx",
      "ColorList", "totalcross.ui", "ColorList", "superwaba.ext.xplat.ui", "ComboBox", "totalcross.ui", "ComboBox",
      "waba.ui", "ComboBox", "totalcross.ui.html.ui", "ComboBox", "superwaba.ext.xplat.html.ui", "CompareWindow",
      "totalcross.unit", "CompareWindow", "superwaba.ext.xplat.unit", "CompressedHttpClient", "totalcross.xml.rpc",
      "CompressedHttpClient", "superwaba.ext.xplat.webservice", "Conduit", "totalcross.io.sync", "Conduit",
      "superwaba.ext.xplat.sync", "Constant", "totalcross.pim.ce.builtin", "Constant", "superwaba.ext.ce.io.builtin",
      "Container", "totalcross.ui", "Container", "waba.ui", "ContentHandler", "totalcross.xml", "ContentHandler",
      "superwaba.ext.xplat.xml", "Control", "totalcross.ui", "Control", "waba.ui", "ControlEvent",
      "totalcross.ui.event", "ControlEvent", "waba.ui", "Controllable", "totalcross.ui.html.ui", "Controllable",
      "superwaba.ext.xplat.html.ui", "ControllableSelectable", "totalcross.ui.html.ui", "ControllableSelectable",
      "superwaba.ext.xplat.html.ui", "ControlTile", "totalcross.ui.html", "ControlTile", "superwaba.ext.xplat.html",
      "Convert", "totalcross.sys", "Convert", "waba.sys", "Coord", "totalcross.ui.gfx", "Coord", "waba.fx", "CRC32",
      "totalcross.util.zip", "CRC32", "superwaba.ext.xplat.util.zip.checksum", "DataStream", "totalcross.io",
      "DataStream", "waba.io", "Date", "totalcross.util", "Date", "waba.util", "DateBook", "totalcross.pim.datebook",
      "DateBook", "superwaba.ext.xplat.io.pimal.datebook", "Datebook", "totalcross.pim.palm.builtin", "Datebook",
      "superwaba.ext.palm.io.builtin", "DateField", "totalcross.pim.datebook", "DateField",
      "superwaba.ext.xplat.io.pimal.datebook", "DateNotSupportedHandler", "totalcross.pim.datebook",
      "DateNotSupportedHandler", "superwaba.ext.xplat.io.pimal.datebook", "DateNSHNote", "totalcross.pim.datebook",
      "DateNSHNote", "superwaba.ext.xplat.io.pimal.datebook", "DateRecord", "totalcross.pim.datebook", "DateRecord",
      "superwaba.ext.xplat.io.pimal.datebook", "DocProperties", "totalcross.ui.html", "DocProperties",
      "superwaba.ext.xplat.html", "Document", "totalcross.ui.html", "Document", "superwaba.ext.xplat.html", "DumpXml",
      "totalcross.xml", "DumpXml", "superwaba.ext.xplat.xml", "Edit", "totalcross.ui", "Edit", "waba.ui", "Edit",
      "totalcross.ui.html.ui", "Edit", "superwaba.ext.xplat.html.ui", "ElementNotFoundException", "totalcross.util",
      "ElementNotFoundError", "waba.util", "Error", "totalcross.lang", "Error", "waba.lang", "EscapeHtml",
      "totalcross.ui.html", "EscapeHtml", "superwaba.ext.xplat.util", "Event", "totalcross.ui.event", "Event",
      "waba.ui", "Exception", "totalcross.lang", "Exception", "waba.lang", "File", "totalcross.io", "File", "waba.io",
      "Font", "totalcross.ui.font", "Font", "waba.fx", "FontMetrics", "totalcross.ui.font", "FontMetrics", "waba.fx",
      "Form", "totalcross.ui.html", "Form", "superwaba.ext.xplat.html", "FTP", "totalcross.net", "FTP",
      "superwaba.ext.xplat.io", "FTPConnectionClosedException", "totalcross.net", "FTPConnectionClosedException",
      "superwaba.ext.xplat.io", "GameEngine", "totalcross.game", "GameEngine", "superwaba.ext.xplat.game",
      "GameEngineException", "totalcross.game", "GameEngineException", "superwaba.ext.xplat.game",
      "GameEngineMainWindow", "totalcross.game", "GameEngineMainWindow", "superwaba.ext.xplat.game", "GfxSurface",
      "totalcross.ui.gfx", "ISurface", "waba.fx", "Image", "totalcross.ui.image", "GifImage",
      "superwaba.ext.xplat.fx.gif", "GPRS", "totalcross.net", "GPRS", "superwaba.ext.ce.io.gprs", "GPS",
      "totalcross.io.device.gps", "GPS", "superwaba.ext.xplat.io.gps", "Graphics", "totalcross.ui.gfx", "Graphics",
      "waba.fx", "Grid", "totalcross.ui", "Grid", "waba.ui", "GridEvent", "totalcross.ui.event", "GridEvent", "waba.ui",
      "Hashtable", "totalcross.util", "Hashtable", "waba.util", "Hidden", "totalcross.ui.html.ui", "Hidden",
      "superwaba.ext.xplat.html.ui", "HighScoreEntry", "totalcross.game", "HighScoreEntry", "superwaba.ext.xplat.game",
      "HighScores", "totalcross.game", "HighScores", "superwaba.ext.xplat.game", "Hr", "totalcross.ui.html", "Hr",
      "superwaba.ext.xplat.html", "HtmlContainer", "totalcross.ui.html", "HtmlContainer", "superwaba.ext.xplat.html",
      "HtmlReader", "totalcross.ui.html", "HtmlReader", "superwaba.ext.xplat.html", "HttpStream", "totalcross.net",
      "HttpStream", "superwaba.ext.xplat.io.http", "IAppointment", "totalcross.pim.ce.builtin", "IAppointment",
      "superwaba.ext.ce.io.builtin", "IAppointments", "totalcross.pim.ce.builtin", "IAppointments",
      "superwaba.ext.ce.io.builtin", "IContact", "totalcross.pim.ce.builtin", "IContact", "superwaba.ext.ce.io.builtin",
      "IContacts", "totalcross.pim.ce.builtin", "IContacts", "superwaba.ext.ce.io.builtin", "IDate",
      "totalcross.pim.ce.builtin", "IDate", "superwaba.ext.ce.io.builtin", "IExtended", "totalcross.pim.ce.builtin",
      "IExtended", "superwaba.ext.ce.io.builtin", "SpecialKeys", "totalcross.sys", "IKeys", "waba.ui",
      "IllegalAccessException", "totalcross.lang", "IllegalAccessException", "waba.lang", "Image",
      "totalcross.ui.image", "Image", "waba.fx", "ImageComparisionTest", "totalcross.unit", "ImageComparisionTest",
      "superwaba.ext.xplat.unit", "ImageException", "totalcross.ui.image", "ImageError", "waba.fx", "ImageControl",
      "totalcross.ui", "ImageScroller", "superwaba.ext.xplat.ui", "ImageTester", "totalcross.unit", "ImageTester",
      "superwaba.ext.xplat.unit", "Img", "totalcross.ui.html", "Img", "superwaba.ext.xplat.html",
      "IndexOutOfBoundsException", "totalcross.lang", "IndexOutOfBoundsException", "waba.lang", "Input",
      "totalcross.ui.html", "Input", "superwaba.ext.xplat.html", "InputBox", "totalcross.ui.dialog", "InputDialog",
      "waba.ui", "InstantiationException", "totalcross.lang", "InstantiationException", "waba.lang", "IntHashtable",
      "totalcross.util", "IntHashtable", "waba.util", "IntVector", "totalcross.util", "IntVector", "waba.util",
      "IObject", "totalcross.pim.ce.builtin", "IObject", "superwaba.ext.ce.io.builtin", "IObjects",
      "totalcross.pim.ce.builtin", "IObjects", "superwaba.ext.ce.io.builtin", "IOException", "totalcross.io", "IOError",
      "waba.io", "IPOutlookItemCollection", "totalcross.pim.ce.builtin", "IPOutlookItemCollection",
      "superwaba.ext.ce.io.builtin", "IRecipient", "totalcross.pim.ce.builtin", "IRecipient",
      "superwaba.ext.ce.io.builtin", "IRecipients", "totalcross.pim.ce.builtin", "IRecipients",
      "superwaba.ext.ce.io.builtin", "IRecurrencePattern", "totalcross.pim.ce.builtin", "IRecurrencePattern",
      "superwaba.ext.ce.io.builtin", "ITask", "totalcross.pim.ce.builtin", "ITask", "superwaba.ext.ce.io.builtin",
      "ITasks", "totalcross.pim.ce.builtin", "ITasks", "superwaba.ext.ce.io.builtin", "JavaBridge", "totalcross",
      "JavaBridge", "waba.applet", "Image", "totalcross.ui.image", "JpegImage", "superwaba.ext.xplat.fx.jpeg",
      "KeyboardBox", "totalcross.ui.dialog", "Keyboard", "waba.ui", "KeyEvent", "totalcross.ui.event", "KeyEvent",
      "waba.ui", "Keypad", "totalcross.ui", "Keypad", "waba.ui", "Label", "totalcross.ui", "Label", "waba.ui",
      "Launcher", "totalcross", "Applet", "waba.applet", "LayoutContext", "totalcross.ui.html", "LayoutContext",
      "superwaba.ext.xplat.html", "LineReader", "totalcross.io", "LineReader", "superwaba.ext.xplat.io", "ListBox",
      "totalcross.ui", "ListBox", "waba.ui", "ListBox", "totalcross.ui.html.ui", "ListBox",
      "superwaba.ext.xplat.html.ui", "Mail", "totalcross.pim.palm.builtin", "Mail", "superwaba.ext.palm.io.builtin",
      "MainWindow", "totalcross.ui", "MainWindow", "waba.ui", "Math", "totalcross.lang", "Math", "waba.lang", "MD5",
      "totalcross.net.ssl.crypto", "MD5", "superwaba.ext.xplat.util.crypto", "Memo", "totalcross.pim.palm.builtin",
      "Memo", "superwaba.ext.palm.io.builtin", "MemoBook", "totalcross.pim.memobook", "MemoBook",
      "superwaba.ext.xplat.io.pimal.memobook", "MemoField", "totalcross.pim.memobook", "MemoField",
      "superwaba.ext.xplat.io.pimal.memobook", "MemoNotSupportedHandler", "totalcross.pim.memobook",
      "MemoNotSupportedHandler", "superwaba.ext.xplat.io.pimal.memobook", "MemoRecord", "totalcross.pim.memobook",
      "MemoRecord", "superwaba.ext.xplat.io.pimal.memobook", "MenuBar", "totalcross.ui", "MenuBar", "waba.ui",
      "MenuItem", "totalcross.ui", "MenuItem", "waba.ui", "MessageBox", "totalcross.ui.dialog", "MessageBox", "waba.ui",
      "MultiEdit", "totalcross.ui", "MultiEdit", "superwaba.ext.xplat.ui", "MultiEdit", "totalcross.ui.html.ui",
      "MultiEdit", "superwaba.ext.xplat.html.ui", "MultiEditMenu", "totalcross.ui", "MultiEditMenu",
      "superwaba.ext.xplat.ui", "MultiListBox", "totalcross.ui.html.ui", "MultiListBox", "superwaba.ext.xplat.html.ui",
      "NamedEntitiesDereferencer", "totalcross.ui.html", "NamedEntitiesDereferencer", "superwaba.ext.xplat.html",
      "NoClassDefFoundError", "totalcross.lang", "NoClassDefFoundError", "waba.lang", "NotSupportedByDeviceException",
      "totalcross.pim", "NotSupportedByDeviceException", "superwaba.ext.xplat.io.pimal", "NotSupportedHandlerNote",
      "totalcross.pim", "NotSupportedHandlerNote", "superwaba.ext.xplat.io.pimal", "NullPointerException",
      "totalcross.lang", "NullPointerException", "waba.lang", "Object", "totalcross.lang", "Object", "waba.lang",
      "ObjectPDBFile", "totalcross.io", "ObjectCatalog", "superwaba.ext.xplat.io", "Options", "totalcross.game",
      "Options", "superwaba.ext.xplat.game", "OutOfMemoryError", "totalcross.lang", "OutOfMemoryError", "waba.lang",
      "Packet", "totalcross.io.device.gps.garmin", "Packet", "superwaba.ext.xplat.io.gps.garmin", "Palette",
      "totalcross.ui.gfx", "Palette", "waba.fx", "PalmAddressBook", "totalcross.pim.palm.builtin.pimal",
      "PalmAddressBook", "superwaba.ext.palm.io.builtin.pimal", "PalmAddressRecord",
      "totalcross.pim.palm.builtin.pimal", "PalmAddressRecord", "superwaba.ext.palm.io.builtin.pimal", "PalmDateBook",
      "totalcross.pim.palm.builtin.pimal", "PalmDateBook", "superwaba.ext.palm.io.builtin.pimal", "PalmDateRecord",
      "totalcross.pim.palm.builtin.pimal", "PalmDateRecord", "superwaba.ext.palm.io.builtin.pimal", "PalmMemoBook",
      "totalcross.pim.palm.builtin.pimal", "PalmMemoBook", "superwaba.ext.palm.io.builtin.pimal", "PalmMemoRecord",
      "totalcross.pim.palm.builtin.pimal", "PalmMemoRecord", "superwaba.ext.palm.io.builtin.pimal", "PalmPIMFactory",
      "totalcross.pim.palm.builtin.pimal", "PalmPIMFactory", "superwaba.ext.palm.io.builtin.pimal", "PalmToDoBook",
      "totalcross.pim.palm.builtin.pimal", "PalmToDoBook", "superwaba.ext.palm.io.builtin.pimal", "PalmToDoRecord",
      "totalcross.pim.palm.builtin.pimal", "PalmToDoRecord", "superwaba.ext.palm.io.builtin.pimal", "PDBFile",
      "totalcross.io", "Catalog", "waba.io", "PDBFile", "totalcross.io", "CatalogSearch",
      "superwaba.ext.xplat.io.search", "PenEvent", "totalcross.ui.event", "PenEvent", "waba.ui", "PIMFactory",
      "totalcross.pim", "PIMFactory", "superwaba.ext.xplat.io.pimal", "Image", "totalcross.ui.image", "PngImage",
      "superwaba.ext.xplat.fx.png", "PocketPCAddressBook", "totalcross.pim.ce.builtin.pimal", "PocketPCAddressBook",
      "superwaba.ext.ce.io.builtin.pimal", "PocketPCAddressRecord", "totalcross.pim.ce.builtin.pimal",
      "PocketPCAddressRecord", "superwaba.ext.ce.io.builtin.pimal", "PocketPCDateBook",
      "totalcross.pim.ce.builtin.pimal", "PocketPCDateBook", "superwaba.ext.ce.io.builtin.pimal", "PocketPCDateRecord",
      "totalcross.pim.ce.builtin.pimal", "PocketPCDateRecord", "superwaba.ext.ce.io.builtin.pimal",
      "PocketPCPIMFactory", "totalcross.pim.ce.builtin.pimal", "PocketPCPIMFactory",
      "superwaba.ext.ce.io.builtin.pimal", "PocketPCRecord", "totalcross.pim.ce.builtin.pimal", "PocketPCRecord",
      "superwaba.ext.ce.io.builtin.pimal", "PocketPCToDoBook", "totalcross.pim.ce.builtin.pimal", "PocketPCToDoBook",
      "superwaba.ext.ce.io.builtin.pimal", "PocketPCToDoRecord", "totalcross.pim.ce.builtin.pimal",
      "PocketPCToDoRecord", "superwaba.ext.ce.io.builtin.pimal", "ComboBoxDropDown", "totalcross.ui", "PopList",
      "waba.ui", "MenuBarDropDown", "totalcross.ui", "PopupMenu", "waba.ui", "PortConnector", "totalcross.io.device",
      "SerialPort", "waba.io", "ProgressBar", "totalcross.ui", "ProgressBar", "waba.ui", "Properties",
      "totalcross.util", "Properties", "superwaba.ext.xplat.util.props", "PushButtonGroup", "totalcross.ui",
      "PushButtonGroup", "waba.ui", "Queue", "totalcross", "Queue", "waba.applet", "Radio", "totalcross.ui", "Radio",
      "waba.ui", "Radio", "totalcross.ui.html.ui", "Radio", "superwaba.ext.xplat.html.ui", "RadioGroupController",
      "totalcross.ui", "RadioGroup", "waba.ui", "Random", "totalcross.util", "Random", "waba.util", "RecordList",
      "totalcross.pim", "RecordList", "superwaba.ext.xplat.io.pimal", "Rect", "totalcross.ui.gfx", "Rect", "waba.fx",
      "RemoteFile", "totalcross.io.sync", "RemoteFile", "superwaba.ext.xplat.sync", "RemotePDBFile",
      "totalcross.io.sync", "RemoteCatalog", "superwaba.ext.xplat.sync", "RemotePDBRecord", "totalcross.io.sync",
      "RemoteCatalogRecord", "superwaba.ext.xplat.sync", "Reset", "totalcross.ui.html.ui", "Reset",
      "superwaba.ext.xplat.html.ui", "ResizeRecord", "totalcross.io", "ResizeRecord", "waba.io", "ResizeRecord",
      "totalcross.io", "ResizeStream", "waba.io", "RootTile", "totalcross.ui.html", "RootTile",
      "superwaba.ext.xplat.html", "Route", "totalcross.io.device.gps.garmin", "Route",
      "superwaba.ext.xplat.io.gps.garmin", "RuntimeException", "totalcross.lang", "RuntimeException", "waba.lang",
      "ScanEvent", "totalcross.io.device.scanner", "ScanEvent", "superwaba.ext.xplat.io.scanner", "Scanner",
      "totalcross.io.device.scanner", "Scanner", "superwaba.ext.xplat.io.scanner", "ScrollBar", "totalcross.ui",
      "ScrollBar", "waba.ui", "Select", "totalcross.ui.html", "Select", "superwaba.ext.xplat.html", "Settings",
      "totalcross.sys", "Settings", "waba.sys", "SHA1", "totalcross.net.ssl.crypto", "SHA1",
      "superwaba.ext.xplat.util.crypto", "SMTP", "totalcross.net", "SMTP", "superwaba.ext.xplat.io", "SOAP",
      "totalcross.xml.soap", "SOAP", "superwaba.ext.xplat.webservice", "Socket", "totalcross.net", "Socket", "waba.io",
      "Sound", "totalcross.ui.media", "Sound", "waba.fx", "SoundClip", "totalcross.ui.media", "SoundClip", "waba.fx",
      "SoundClipEvent", "totalcross.ui.media", "SoundClipEvent", "waba.fx", "SpinList", "totalcross.ui", "SpinList",
      "superwaba.ext.xplat.ui", "Sprite", "totalcross.game", "Sprite", "superwaba.ext.xplat.game", "StandardHttpClient",
      "totalcross.xml.rpc", "StandardHttpClient", "superwaba.ext.xplat.webservice", "Storable", "totalcross.io",
      "Storable", "superwaba.ext.xplat.io", "Stream", "totalcross.io", "Stream", "waba.io", "String", "totalcross.lang",
      "String", "waba.lang", "StringBuffer", "totalcross.lang", "StringBuffer", "waba.lang", "StringExt",
      "totalcross.pim.ce.builtin", "StringExt", "superwaba.ext.ce.io.builtin", "Style", "totalcross.ui.html", "Style",
      "superwaba.ext.xplat.html", "Submit", "totalcross.ui.html.ui", "Submit", "superwaba.ext.xplat.html.ui", "SWEvent",
      "totalcross", "SWEvent", "waba.applet", "SWEventThread", "totalcross", "SWEventThread", "waba.applet",
      "SyntaxException", "totalcross.xml", "SyntaxException", "superwaba.ext.xplat.xml", "Table", "totalcross.ui.html",
      "Table", "superwaba.ext.xplat.html", "TabbedContainer", "totalcross.ui", "TabPanel", "waba.ui", "TagDereferencer",
      "totalcross.ui.html", "TagDereferencer", "superwaba.ext.xplat.html", "TEA", "totalcross.net.ssl.crypto", "TEA",
      "superwaba.ext.xplat.util.crypto", "TestCase", "totalcross.unit", "TestCase", "superwaba.ext.xplat.unit",
      "TestSuite", "totalcross.unit", "TestSuite", "superwaba.ext.xplat.unit", "TextArea", "totalcross.ui.html",
      "TextArea", "superwaba.ext.xplat.html", "TextRenderer", "totalcross.game", "TextRenderer",
      "superwaba.ext.xplat.game", "TextSpan", "totalcross.ui.html", "TextSpan", "superwaba.ext.xplat.html", "Thread",
      "totalcross.lang", "Thread", "waba.sys", "Throwable", "totalcross.lang", "Throwable", "waba.lang", "Tile",
      "totalcross.ui.html", "Tile", "superwaba.ext.xplat.html", "TileFactory", "totalcross.ui.html", "TileFactory",
      "superwaba.ext.xplat.html", "Time", "totalcross.sys", "Time", "waba.sys", "TimerEvent", "totalcross.ui.event",
      "Timer", "waba.ui", "ToDo", "totalcross.pim.palm.builtin", "ToDo", "superwaba.ext.palm.io.builtin", "ToDoBook",
      "totalcross.pim.todobook", "ToDoBook", "superwaba.ext.xplat.io.pimal.todobook", "ToDoField",
      "totalcross.pim.todobook", "ToDoField", "superwaba.ext.xplat.io.pimal.todobook", "ToDoNotSupportedHandler",
      "totalcross.pim.todobook", "ToDoNotSupportedHandler", "superwaba.ext.xplat.io.pimal.todobook", "ToDoNSHNote",
      "totalcross.pim.todobook", "ToDoNSHNote", "superwaba.ext.xplat.io.pimal.todobook", "ToDoRecord",
      "totalcross.pim.todobook", "ToDoRecord", "superwaba.ext.xplat.io.pimal.todobook", "ToolTip", "totalcross.ui",
      "ToolTip", "waba.ui", "UffUserFont", "totalcross", "UffUserFont", "waba.applet", "UIColors", "totalcross.ui",
      "UIColors", "waba.ui", "UIRobot", "totalcross.unit", "UIRobot", "superwaba.ext.xplat.unit", "URI",
      "totalcross.net", "URI", "superwaba.ext.xplat.io.http", "UserFont", "totalcross", "UserFont", "waba.applet",
      "UTF8CharacterConverter", "totalcross.sys", "UTF8CharacterConverter", "waba.sys", "VCalField", "totalcross.pim",
      "VCalField", "superwaba.ext.xplat.io.pimal", "VCalRecord", "totalcross.pim", "VCalRecord",
      "superwaba.ext.xplat.io.pimal", "VCardField", "totalcross.pim", "VCardField", "superwaba.ext.xplat.io.pimal",
      "VCardRecord", "totalcross.pim", "VCardRecord", "superwaba.ext.xplat.io.pimal", "Vector", "totalcross.util",
      "Vector", "waba.util", "VersitField", "totalcross.pim", "VersitField", "superwaba.ext.xplat.io.pimal",
      "VersitRecord", "totalcross.pim", "VersitRecord", "superwaba.ext.xplat.io.pimal", "Vm", "totalcross.sys", "Vm",
      "waba.sys", "Waypoint", "totalcross.io.device.gps.garmin", "Waypoint", "superwaba.ext.xplat.io.gps.garmin",
      "Welcome", "totalcross.ui", "Welcome", "waba.ui", "WinCanvas", "totalcross", "WinCanvas", "waba.applet", "Window",
      "totalcross.ui", "Window", "waba.ui", "WinTimer", "totalcross", "WinTimer", "waba.applet", "XmlReadable",
      "totalcross.xml", "XmlReadable", "superwaba.ext.xplat.xml", "XmlReadablePDBFile", "totalcross.xml",
      "XmlReadableCatalog", "superwaba.ext.xplat.xml", "XmlReadablePort", "totalcross.xml", "XmlReadablePort",
      "superwaba.ext.xplat.xml", "XmlReadableSocket", "totalcross.xml", "XmlReadableSocket", "superwaba.ext.xplat.xml",
      "XmlReadableString", "totalcross.xml", "XmlReadableString", "superwaba.ext.xplat.xml", "XmlReader",
      "totalcross.xml", "XmlReader", "superwaba.ext.xplat.xml", "XmlRpcClient", "totalcross.xml.rpc", "XmlRpcClient",
      "superwaba.ext.xplat.webservice", "XmlRpcContentHandler", "totalcross.xml.rpc", "XmlRpcContentHandler",
      "superwaba.ext.xplat.webservice", "XmlRpcException", "totalcross.xml.rpc", "XmlRpcException",
      "superwaba.ext.xplat.webservice", "XmlRpcValue", "totalcross.xml.rpc", "XmlRpcValue",
      "superwaba.ext.xplat.webservice", "XmlTokenizer", "totalcross.xml", "XmlTokenizer", "superwaba.ext.xplat.xml",
      "XmlWriter", "totalcross.xml.rpc", "XmlWriter", "superwaba.ext.xplat.webservice", "ZLib", "totalcross.util.zip",
      "ZLib", "superwaba.ext.xplat.zlib", "ZLibException", "totalcross.util.zip", "ZLibException",
      "superwaba.ext.xplat.zlib", };

  private SW2TC(String[] args) {
    try {
      if (args.length == 0 || !new File(args[0]).isDir()) {
        System.out.println(
            "Format: SW2TC <folder>\nThe given folder is read recursively and all java files are patched. Caution: no backup is created; the original file is overwritten.");
      } else {
        setupHashtables();
        crawlTree(args[0]);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void crawlTree(String folder) throws Exception {
    String[] files = new File(folder).listFiles();
    if (files != null) {
      for (int i = 0; i < files.length; i++) {
        String f = files[i];
        if (f.endsWith("/")) {
          crawlTree(folder + "/" + f);
        } else if (f.toLowerCase().endsWith(".java")) {
          readFile(folder + "/" + f);
        }
      }
    }
  }

  private byte[] buff = new byte[65000];

  private void readFile(String name) throws Exception {
    File f = new File(name, File.READ_WRITE);
    System.out.println("Processing " + name);
    int size = f.getSize();
    if (buff.length < size) {
      buff = new byte[size];
    }
    f.readBytes(buff, 0, size);
    String wholefile = new String(buff, 0, size);
    wholefile = processFile(wholefile);
    if (wholefile != null) {
      Time time = f.getTime(File.TIME_CREATED); // keep file time
      f.delete();
      f = new File(name, File.CREATE);
      byte[] newbytes = wholefile.getBytes();
      f.writeBytes(newbytes, 0, newbytes.length);
      f.setTime(File.TIME_CREATED, time);
      f.close();
    } else {
      f.close();
    }
  }

  private String delims = "abcdefghijklmnopqrtsuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_$";

  private String processFile(String str) {
    int pos = 0;
    boolean changed = false;
    // first, process the imports
    while (true) {
      pos = str.indexOf("import ", pos);
      if (pos == -1) {
        break;
      }
      int end = str.indexOf(';', pos + 1);
      if (end != -1) {
        String imp = str.substring(pos, end + 1);
        String to = (String) htImports.get(normalizeImport(imp));
        if (to != null) {
          str = replace(str, imp, to, pos);
          changed = true;
        }
      }
      pos++;
    }
    pos = 0;
    // then, process the full class names
    int n = vFullClassNamesSource.size();
    for (int i = 0; i < n; i++) {
      String source = (String) vFullClassNamesSource.items[i];
      String target = (String) vFullClassNamesTarget.items[i];
      while (true) {
        pos = str.indexOf(source, pos);
        if (pos == -1) {
          break;
        }
        char first = str.charAt(pos - 1);
        char last = str.charAt(pos + source.length());
        if (delims.indexOf(first) == -1 && delims.indexOf(last) == -1) // whole word?
        {
          str = replace(str, source, target, pos);
          changed = true;
          pos += target.length();
        } else {
          pos += source.length();
        }
      }
    }
    pos = 0;
    // now, process the short class names
    n = vClassNamesSource.size();
    for (int i = 0; i < n; i++) {
      String source = (String) vClassNamesSource.items[i];
      String target = (String) vClassNamesTarget.items[i];
      while (true) {
        pos = str.indexOf(source, pos);
        if (pos == -1) {
          break;
        }
        char first = str.charAt(pos - 1);
        char last = str.charAt(pos + source.length());
        if (delims.indexOf(first) == -1 && delims.indexOf(last) == -1) // whole word?
        {
          str = replace(str, source, target, pos);
          changed = true;
          pos += target.length();
        } else {
          pos += source.length();
        }
      }
    }

    return changed ? str : null;
  }

  private String normalizeImport(String imp) // remove spaces/tabs before/after the package name
  {
    String pack = imp.substring(7, imp.length() - 1);
    return "import " + pack.trim() + ";";
  }

  private String replace(String str, String from, String to, int pos) {
    String part1 = str.substring(0, pos);
    int end = pos + from.length();
    String part2 = str.substring(end);
    return part1 + to + part2;
  }

  private void setupHashtables() {
    htImports = new Hashtable(0xFFF);
    vClassNamesSource = new Vector(0xFF);
    vClassNamesTarget = new Vector(0xFF);
    vFullClassNamesSource = new Vector(0xFF);
    vFullClassNamesTarget = new Vector(0xFF);
    for (int i = 0; i < tofrom.length; i += 4) {
      String targetClass = tofrom[i];
      String targetPackg = tofrom[i + 1];
      String sourceClass = tofrom[i + 2];
      String sourcePackg = tofrom[i + 3];
      // imports
      String source = "import " + sourcePackg + ".*;";
      String target = "import " + targetPackg + ".*;";
      htImports.put(source, target);
      String fullSource = sourcePackg + "." + sourceClass;
      String fullTarget = targetPackg + "." + targetClass;
      // class name changes
      vFullClassNamesSource.addElement(fullSource);
      vFullClassNamesTarget.addElement(fullTarget);
      if (!sourceClass.equals(targetClass)) {
        vClassNamesSource.addElement(sourceClass);
        vClassNamesTarget.addElement(targetClass);
      }
    }
    // waba.fx moved to many different packages
    htImports.put("import waba.fx.*;",
        "import totalcross.ui.font.*;\r\nimport totalcross.ui.gfx.*;\r\nimport totalcross.ui.image.*;\r\nimport totalcross.ui.media.*;");
    htImports.put("import waba.ui.*;",
        "import totalcross.ui.*;\r\nimport totalcross.ui.event.*;\r\nimport totalcross.ui.dialog.*;");
    htImports.put("import superwaba.ext.xplat.io.*;",
        "import totalcross.io.*;\r\nimport totalcross.net.*;\r\nimport totalcross.io.device.*;");
    htImports.put("import totalcross.io.*;",
        "import totalcross.io.*;\r\nimport totalcross.net.*;\r\nimport totalcross.io.device.*;");
    htImports.put("import superwaba.ext.xplat.webservice.*;",
        "import totalcross.xml.rpc.*;\r\nimport totalcross.xml.soap.*;");
    String[] sourceTarget = {
        //  source                             target
        "popupBlockingModal", "popup", "popupModal", "popupNonBlocking", "wasDateValid",
        "/* NO LONGER SUPPORTED wasDateValid - catch the InvalidDateException instead */", "getPanel", "getContainer",
        "setPanel", "setContainer", "useOnTabThePanelsColor", "useOnTabTheContainerColor", "createBmp",
        "createPng /* bas.count() has the # of bytes written */", "getChoices", "getControl", // CustomDialog
        "totalcross.JavaBridge.showMsgs", "totalcross.sys.Settings.showDesktopMessages", "JavaBridge.showMsgs",
        "Settings.showDesktopMessages", "onlyShowImage",
        "/* NO LONGER SUPPORTED: onlyShowImage - use ImageControl instead */", "getSelectedMenuItem",
        "getSelectedIndex", "saveBehind", "/* NO LONGER NEEDED: saveBehind*/", "dontSaveBehind",
        "/* NO LONGER NEEDED: dontSaveBehind*/", "createGraphics", "getGraphics", "setDoubleBuffer",
        "/* NO LONGER NEEDED: setDoubleBuffer*/", "getOffScreen", "/* NO LONGER SUPPORTED: getOffScreen*/", ".equ",
        "/*.equ*/", // c1.equ == c2.equ -> c1 == c2 (because color have only one instance per rgb)
        "Palette", "/* NO LONGER NEEDED: Palette - simply remove all palette manipulations */", "clearScreen()",
        "/* NO LONGER SUPPORTED: clearScreen - please change it to fillRect(0,0,width,height) */", "applyPalette",
        "/* NO LONGER SUPPORTED: applyPalette - there's a single system palette */", "collisionDetect",
        "/* NO LONGER SUPPORTED: collisionDetect - use a rectangle as a region instead */", "setDrawDots",
        "drawDots = ", "setCapitaliseMode", "capitalise = ", "setNumberOfRows", "rowCount = ", "getTextWidth",
        "stringWidth", "getCharWidth", "charWidth", "totalcross.sys.Settings.setUIStyle", "setUIStyle",
        "Settings.setUIStyle", "setUIStyle", "Settings.useExceptions = true;", "//Settings. useExceptions = true;",
        "Settings.useExceptions = false;", "//Settings.useExceptions = false;", "listCatalogs", "listPDBs", "copyArray",
        "arrayCopy", "getCopy", "toByteArray", "drawCursor", "fillCursor", // 1. drawCursor -> fillCursor            please keep
        "drawCursorOutline", "drawCursor", // 2. drawCursorOutline -> drawCursor     this order!
        "PDBFile.READ_ONLY", "PDBFile.READ_WRITE", "PDBFile.WRITE_ONLY", "PDBFile.READ_WRITE", "File.READ_ONLY",
        "File.READ_WRITE", "File.WRITE_ONLY", "File.READ_WRITE", "nextFloat", "nextDouble", "Convert.toIntBitwise",
        "Convert.doubleToIntBits", "Convert.convertToFloat", "(float)Convert.convertToDouble", "Convert.toFloatBitwise",
        "(float)Convert.intBitsToDouble", "Convert.toFloat", "(float)Convert.toDouble", "setDrawOp", "drawOp = ",
        "getColor", "getRGB", "Vm.actionEqualsMenu", "Settings.actionInvokesMenu", "Vm.setTimeStamp", "Vm.setTime",
        "Vm.interceptSystemKeys", "Vm.interceptSpecialKeys /* read the javadocs */", "Vm.getSystemKeysPressed",
        "Vm.isKeyDown /* read the javadocs */", "ControlEvent.TIMER", "TimerEvent.TRIGGERED", "JOG_UP",
        "/* NO LONGER SUPPORTED: JOG_UP */", "JOG_DOWN", "/* NO LONGER SUPPORTED: JOG_DOWN */", "JOG_PUSH",
        "/* NO LONGER SUPPORTED: JOG_PUSH */", "JOG_RELEASE", "/* NO LONGER SUPPORTED: JOG_RELEASE */",
        "JOG_PUSHREPEAT", "/* NO LONGER SUPPORTED: JOG_PUSHREPEAT */", "JOG_PUSHUP",
        "/* NO LONGER SUPPORTED: JOG_PUSHUP */", "JOG_PUSHDOWN", "/* NO LONGER SUPPORTED: JOG_PUSHDOWN */", "JOG_BACK",
        "/* NO LONGER SUPPORTED: JOG_BACK */", "Settings.appCreatorId", "Settings.applicationId", "Catalog.READ_ONLY",
        "Catalog.READ_WRITE", "mask", "getMask()", "getClientRect()",
        "/* getClientRect(): replace by setRect(LEFT,TOP,FILL,FILL) */", "Socket.disconnect()",
        "ConnectionManager.close()", "Vm.TWEAK_PRINT_STACK", "/* NO LONGER NEEDED: Vm.TWEAK_PRINT_STACK */",
        "translateTo", "/* translateTo - replace by translate */", "uiPalm", "(Settings.uiStyle == Settings.PalmOS)",
        "uiCE", "(Settings.uiStyle == Settings.WinCE)", "uiVista", "(Settings.uiStyle == Settings.Vista)", "uiFlat",
        "(Settings.uiStyle == Settings.Flat)", "loadBehind(true)", "loadBehind() /*extra parameter has been removed!*/",
        "loadBehind(false)", "loadBehind() /*extra parameter has been removed!*/", "writeIntLE",
        "writeIntLE /*use DataStreamLE.writeInt instead*/", "writeShortLE",
        "writeShortLE /*- use DataStreamLE.writeShort instead*/", "readIntLE",
        "readIntLE /*- use DataStreamLE.readInt instead*/", "readShortLE",
        "readShortLE /*- use DataStreamLE.readShort instead*/", "onAdd", "onAddAgain", "onRemove", "onRemove",
        "getSelectedRow", "getSelectedIndex", "setSelected", "setSelectedIndex", "getSelected", "getSelectedIndex",
        "ControlEvent.WINDOW_MOVED", "/* NO LONGER SUPPORTED: ControlEvent.WINDOW_MOVED */", "rand", "between",
        "onStart", "initUI", "Label.commonVGap", "/* NO LONGER NEEDED: Label.commonVGap */", "IntHashtable.INVALID",
        "/* NO LONGER SUPPORTED: IntHashtable.INVALID. Catch the ElementNotFoundException */", "enableHorizScroll",
        "enableHorizontalScroll", "setTransparentColor(null)", "transparentColor = -1", "setTransparentColor",
        "transparentColor = ", "getSelectedCaption", "getSelectedItem", "!totalcross.sys.Settings.onDevice",
        "totalcross.sys.Settings.onJavaSE", "totalcross.sys.Settings.onDevice", "!totalcross.sys.Settings.onJavaSE",
        "!Settings.onDevice", "Settings.onJavaSE", "Settings.onDevice", "!Settings.onJavaSE", "getDeviceFreeMemory",
        "getFreeMemory", "setDeviceAutoOff(0)", "setAutoOff(false)", "setDeviceAutoOff",
        "setAutoOff(true);// not needed", "isOpen",
        "/* NO LONGER SUPPORTED: isOpen - an exception is thrown if an error happens */", "transparentPixel",
        "transparentColor", "g.setForeColor", "g.foreColor = ", "g.setBackColor", "g.backColor = ", "getChecked",
        "isChecked", "fill3dRect", "fillVistaRect", "defaultFont",
        "/* defaultFont: use setDefaultFont or getDefaultFont */", "setNumberKeys", "setSymbolKeys",
        "Button.createArrowButton", "/* Button.createArrowButton - use ArrowButton class instead. */", "extraArrowSize",
        "/* extraArrowSize - no longer supported. size is now computed based on button's width/height */",
        "File.isAvailable()", "/* File.isAvailable() - no longer needed */", "getJulianDay", "getGregorianDay",
        "getRandom", "nextInt", "popupPop", "popup", "onBoundsChanged()", "onBoundsChanged(boolean screenChanged)",
        "brighter()", "Color.brighter()", "darker()", "Color.darker()", "new Font", "Font.getFont", "new Color",
        "Color.getRGB", "setSelectedLine", "setSelectedIndex", "getSelectedLine", "getSelectedIndex", "skip",
        "skipBytes", "getCount", "size", "clicked", "setSelectedItem", };
    for (int i = 0; i < sourceTarget.length;) {
      vFullClassNamesSource.addElement(sourceTarget[i++]);
      vFullClassNamesTarget.addElement(sourceTarget[i++]);
    }
  }

  public static void main(String args[]) {
    new SW2TC(args);
  }
}
