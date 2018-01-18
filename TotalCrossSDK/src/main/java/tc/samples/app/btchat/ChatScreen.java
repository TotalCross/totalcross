package tc.samples.app.btchat;

import totalcross.io.DataStream;
import totalcross.io.IOException;
import totalcross.io.Stream;
import totalcross.sys.Vm;
import totalcross.ui.Button;
import totalcross.ui.Container;
import totalcross.ui.Edit;
import totalcross.ui.ListBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;
import totalcross.ui.event.KeyEvent;

public class ChatScreen extends Container {
  DataStream connection;

  ChatScreen(Stream connection) {
    this.connection = new DataStream(connection);
  }

  ListBox chatArea;
  Edit msgField;
  Button btSend;

  @Override
  public void initUI() {
    btSend = new Button(" Send! ");

    add(btSend, RIGHT, BOTTOM, PREFERRED, PREFERRED + fmH);

    msgField = new Edit();
    add(msgField, LEFT, SAME, FIT, PREFERRED);

    chatArea = new ListBox();
    add(chatArea, LEFT, TOP, FILL, FIT, msgField);

    msgField.requestFocus();
    new Thread(new Listener()).start();
  }

  @Override
  public void onEvent(Event event) {
    if (event.type == KeyEvent.SPECIAL_KEY_PRESS) {
      btSend.simulatePress();
      btSend.postPressedEvent();
    } else if (event.type == ControlEvent.PRESSED && event.target == btSend) {
      String msg = msgField.getText();
      if (msg != null && msg.trim().length() > 0) {
        try {
          connection.writeString(msg);
          // send message
          chatArea.add(msg);
          chatArea.selectLast();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      msgField.clear();
    }
  }

  class Listener implements Runnable {
    @Override
    public void run() {
      try {
        while (true) {
          String s = connection.readString();
          if (s != null) {
            chatArea.add(s);
            chatArea.selectLast();
          }
          Vm.sleep(50);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
