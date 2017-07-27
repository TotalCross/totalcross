package tc.samples.api.lang.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import tc.samples.api.BaseContainer;
import totalcross.ui.AlignedLabelsContainer;
import totalcross.ui.Button;
import totalcross.ui.Edit;
import totalcross.ui.Label;
import totalcross.ui.dialog.MessageBox;
import totalcross.ui.event.ControlEvent;
import totalcross.ui.event.Event;

public class ReflectionSample extends BaseContainer
{
  // ui
  Button btAdd;
  Edit edName,edAddr,edNumber,edAge;

  // reflection
  Constructor<?> c;
  Field fname,faddr,fnumb;
  Method mage;

  @Override
  public void initUI()
  {
    try
    {
      super.initUI();
      AlignedLabelsContainer alc = new AlignedLabelsContainer(new String[]{"Name: ","Address: ","Addr.Number: ","Age: "});
      int g = fmH/2;
      add(alc,LEFT,TOP+g,FILL,PREFERRED);
      alc.add(edName = new Edit(),LEFT,alc.getLineY(0));
      alc.add(edAddr = new Edit(),LEFT,alc.getLineY(1));
      alc.add(edNumber = new Edit(),LEFT,alc.getLineY(2)); edNumber.setKeyboard(Edit.KBD_NUMERIC);
      alc.add(edAge = new Edit(),LEFT,alc.getLineY(3)); edAge.setKeyboard(Edit.KBD_NUMERIC);
      add(btAdd = new Button("ADD"),CENTER,AFTER+g,PARENTSIZE+50,PREFERRED+g/2);
      add(new Label("Constructed and retrieved using reflection:"),LEFT,AFTER+g);
      addLog(LEFT,AFTER,FILL,FILL,null);
      // get access to Data's fields
      Class<?> data = Class.forName("tc.samples.api.lang.reflection.Data");
      c = data.getConstructor(new Class[]{String.class,String.class,int.class,byte.class});
      fname = data.getField("name");
      faddr = data.getField("address");
      fnumb = data.getField("number");
      mage = data.getMethod("getAge",new Class[0]);
    }
    catch (Throwable e)
    {
      MessageBox.showException(e,true);
    }
  }

  @Override
  public void onEvent(Event e)
  {
    if (e.type == ControlEvent.PRESSED && e.target == btAdd)
    {
      try
      {
        if (edName.getTrimmedLength() == 0 || edAddr.getTrimmedLength() == 0 || edNumber.getTrimmedLength() == 0 || edAge.getTrimmedLength() == 0) {
          new MessageBox("Error","Please fill all fields").popup();
        } else
        {
          // Data d = new Data(name, addr, number, age);
          Object o = c.newInstance(new Object[]{edName.getText(),edAddr.getText(),Integer.valueOf(edNumber.getText()),Byte.valueOf(edAge.getText())});
          // sn = d.name;
          String sn = (String)fname.get(o);
          // sa = d.address;
          String sa = (String)faddr.get(o);
          // in = d.number;
          int in = fnumb.getInt(o);
          // ba = d.getAge();
          byte ba = ((Byte)mage.invoke(o, (Object[])null)).byteValue();
          // show in list
          log("name: "+sn);
          log("address: "+sa);
          log("number: "+in);
          log("age: "+ba);
          log("-------------------");
        }
      }
      catch (Throwable ee)
      {
        MessageBox.showException(ee,true);
      }
    }
  }
}


