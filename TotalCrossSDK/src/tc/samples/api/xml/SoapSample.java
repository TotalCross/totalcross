/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/


/** Sample that can be renamed to JWS:
 <pre>
public class TestHandler
{
   public String testParameters(String testString, int testInt, double testDouble, boolean testBoolean)
   {
      return "Test("+testString+","+testInt+","+testDouble+","+testBoolean+") arrived!";
   }

   public int somaInt(int x, int y)
   {
      return x+y;
   }

   public double somaDouble(double x, double y)
   {
      return x+y;
   }

   public boolean returnBoolean(boolean b)
   {
      return b;
   }

   public int sumIntArray(int[] array, String name)
   {
      int soma = 0;
      int len = array.length;
      for (int i=0; i<len; i++)
      {
         soma += array[i];
      }
      return soma;
   }

   public double sumDoubleArray(double[] array, String name)
   {
      double soma = 0;
      int len = array.length;
      for (int i=0; i<len; i++)
      {
         soma += array[i];
      }
      return soma;
   }

   public String[] returnStringArray(String[] s)
   {
      return s;
   }

   public int[] returnIntArray()
   {
      int[] array = {1,2,3,4,5};
      return array;
   }

   public double[] returnDoubleArray()
   {
      double[] array = {1.1, 2.2, 3.3, 4.4, 5.5};
      return array;
   }
}
 </pre>
 */

package tc.samples.api.xml;

import tc.samples.api.*;

import totalcross.ui.*;
import totalcross.ui.event.*;
import totalcross.ui.gfx.*;
import totalcross.xml.soap.*;

public class SoapSample extends BaseContainer
{
   ComboBox cbTest;
   Button btOk;
   Edit edRemote;

   int selectedServer;

   private String[] tests = 
   {
      "All tests",
      "parameters",
      "sum int",
      "sum double",
      "boolean return",
      "sum int array",
      "sum double array",
      "return string array",
//      "return int array",     - does not work
//      "return double array"   - in our server
   };

   public void initUI()
   {
      super.initUI();
      add(new Label("URI:"), LEFT+2, TOP+4);
      add(edRemote = new Edit(""), AFTER+2,SAME-2);
      edRemote.setText("http://www.superwaba.net/SoapSampleWS/services/TestHandler");
      
      add(new Label("Select test: "), LEFT+2, AFTER+2);

      add(cbTest = new ComboBox(tests),AFTER, SAME);

      add(btOk = new Button(" GO! "));
      btOk.setBackColor(Color.ORANGE);
      btOk.setRect(AFTER+fmH,SAME, FILL, PREFERRED);

      addLog(LEFT,AFTER+2,FILL,FILL,null);
   }

   public void executeTest(int test)
   {
      if (test == -1)
      {
         for (int i=0; i < tests.length-1; i++)
            executeTest(i);
      }
      else
      {
         log("Executing test #"+test+": "+tests[test+1]);
         switch (test)
         {
            case 0: testParameters(); break;        // String testParameters(String testString, int testInt, double testDouble, boolean testBoolean)
            case 1: testSomaInt(); break;           // int somaInt(int x, int y)
            case 2: testSomaDouble(); break;        // double somaDouble(double x, double y)
            case 3: testReturnBoolean(); break;     // boolean returnBoolean(boolean b)
            case 4: testSumIntArray(); break;       // int sumIntArray(int[] array, String name)
            case 5: testSumDoubleArray(); break;    // double sumDoubleArray(double[] array, String name)
            case 6: testReturnStringArray(); break; // string[] returnStringArray(string[] s)
            //case 7: testReturnIntArray(); break;    // int[] returnIntArray()
            //case 8: testReturnDoubleArray(); break; // double[] returnDoubleArray()
         }
      }
   }

   public void onEvent(Event e)
   {
      switch (e.type)
      {
         case ControlEvent.PRESSED:
            if (e.target == btOk && cbTest.getSelectedIndex() >= 0)
            {
               lblog.removeAll();
               executeTest(cbTest.getSelectedIndex()-1);
            }
            break;
      }
   }

   private void testParameters()
   {
      try
      {
         SOAP s = createSoap("testParameters");
         s.setParam(totalcross.ui.html.EscapeHtml.escape("hoje eh"),"testString");
         s.setParam(26,"testInt");
         s.setParam(7.4,"testDouble");
         s.setParam(true,"testBoolean");
         s.execute();
         log("Answer: " + s.getAnswer());
      }
      catch (SOAPException /*XmlRpcException*/ e)
      {
         log("*** Error! "+e.getMessage());
         e.printStackTrace();
      }
   }

   private void testSomaInt()  //int somaInt(int x, int y)
   {
      try
      {
         SOAP s = createSoap("somaInt");
         s.setParam(2,"x");
         s.setParam(3,"y");
         s.execute();
         log("Answer: " + s.getAnswer());
      }
      catch (SOAPException /*XmlRpcException*/ e)
      {
         log("*** Error! "+e.getMessage());
         e.printStackTrace();
      }
   }

   private void testSomaDouble()  //double somaDouble(double x, double y)
   {
      try
      {
         SOAP s = createSoap("somaDouble");
         s.setParam(3.3,"x");
         s.setParam(6.2,"y");
         s.execute();
         log("Answer: " + s.getAnswer());
      }
      catch (SOAPException /*XmlRpcException*/ e)
      {
         log("*** Error! "+e.getMessage());
         e.printStackTrace();
      }
   }

   private void testReturnBoolean()  //boolean returnBoolean(boolean b)
   {
      try
      {
         SOAP s = createSoap("returnBoolean");
         s.setParam(true,"b");
         s.execute();
         log("Answer: " + s.getAnswer());
      }
      catch (SOAPException /*XmlRpcException*/ e)
      {
         log("*** Error! "+e.getMessage());
         e.printStackTrace();
      }
   }

   private void testSumIntArray()  //int sumIntArray(int[] array, String name)
   {
      try
      {
         SOAP s = createSoap("sumIntArray");
         int[] array = {1,2,3,4,5};
         s.setParam(array, "array");
         s.setParam("teste","nome","string");
         s.execute();
         log("Answer: " + s.getAnswer());
      }
      catch (SOAPException /*XmlRpcException*/ e)
      {
         log("*** Error! "+e.getMessage());
         e.printStackTrace();
      }
   }

   private void testSumDoubleArray()  //double sumDoubleArray(double[] array, String name)
   {
      try
      {
         SOAP s = createSoap("sumDoubleArray");
         double[] array = {1.1, 2.2, 3.3, 4.4, 5.5};
         s.setParam(array, "array");
         s.setParam("teste","nome","string");
         s.execute();
         log("Answer: " + s.getAnswer());
      }
      catch (SOAPException /*XmlRpcException*/ e)
      {
         log("*** Error! "+e.getMessage());
         e.printStackTrace();
      }
   }

   private void testReturnStringArray() //string[] returnStringArray(string[] s)
   {
      try
      {
         SOAP s = createSoap("returnStringArray");
         String[] sArray = {"teste1","teste2","teste3"};
         s.setParam(sArray,"s");
         s.execute();
         log("Answer: ");
         sArray = (String[])s.getAnswer();
         int len = sArray.length;
         for (int i=0; i<len; i++)
            log(sArray[i]);
      }
      catch (SOAPException /*XmlRpcException*/ e)
      {
         log("*** Error! "+e.getMessage());
         e.printStackTrace();
      }
   }

/* does not work in our server
   private void testReturnIntArray() //int[] returnIntArray()
   {
      try
      {
         SOAP s = createSoap("returnIntArray");
         s.execute();
         log("Answer: ");
         int[] response = (int[])s.getAnswer();
         int len = response.length;
         for (int i=0; i<len; i++)
            log(Convert.toString(response[i]));
      }
      catch (SOAPException e)
      {
         log("*** Error! "+e.getMessage());
         e.printStackTrace();
      }
   }

   private void testReturnDoubleArray() //double[] returnDoubleArray()
   {
      try
      {
         SOAP s = createSoap("returnDoubleArray");
         s.execute();
         log("Answer: ");
         double[] response = (double[])s.getAnswer();
         int len = response.length;
         for (int i=0; i<len; i++)
            log(Convert.toString(response[i]));
      }
      catch (SOAPException e)
      {
         log("*** Error! "+e.getMessage());
         e.printStackTrace();
      }
   }*/

   private SOAP createSoap(String method)
   {
      SOAP s = new SOAP(method, edRemote.getText()); //"http://localhost:8080/axis/TestHandler.jws"
      return s;
   }
}
