// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



import totalcross.io.*;
import totalcross.util.*;
import totalcross.sys.*;

public class GenTestCalls
{
   Hashtable ht = new Hashtable(203);
   private int errorCount;
   static boolean useHIN;

   class TestCase
   {
      public String methodName;
      public String dependsOn;
      public String sourceFile;
      public int hardIndexNumber;

      public int hashCode() // used in the hashtable
      {
         return methodName.hashCode();
      }

      public boolean equals(Object o) // used in the hashtable
      {
         if (o instanceof String)
            return methodName.equals(o);
         if (o instanceof TestCase)
            return methodName.equals(((TestCase)o).methodName);
         return false;
      }

      public String toString() // used in the quick sort
      {
         return useHIN ? (""+hardIndexNumber) : (sourceFile+" - "+methodName);
      }
   }

   public GenTestCalls(String[] args)
   {
      try
      {
         if (args.length == 0)
            println("Format: GenTestCalls <list of folders to search>\nSearch is always recursive into the given folders.\nOnly .c files are scanned.");
         else
         {
            for (int i=0; i < args.length; i++)
               searchIn(args[i]);
            if (ht.size() == 0)
               error("No TESTCASEs found!");
            else
               buildOutput(sortDependents());
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }

   private int find(Vector in, String what, int start) // vector.find is not working
   {
      int depIdx = -1;
      {
         int nn = in.size();
         for (int j=start; j < nn; j++)
            if (in.items[j].equals(what))
            {
               depIdx = j;
               break;
            }
      }
      return depIdx;
   }

   private TestCase[] sortDependents()
   {
      int i,n = ht.size();
      Vector in = ht.getKeys();
      in.qsort(); // sort by filename

      int ndep = countDependencies(in);
      if (ndep == -1) // error?
         return null;
      if (ndep > 0)
      {
         // order the dependencies
         int tries =0;
         boolean changed;
         do
         {
            changed = false;
            if (tries++ > n)
            {
               error("FATAL ERROR! Recursive dependencies!");
               return null;
            }
            for (i = 0; i < n; i++)
            {
               TestCase tc = (TestCase)in.items[i];
               if (tc.dependsOn != null)
               {
                  int depIdx = find(in, tc.dependsOn, i+1);
                  if (depIdx > i) // if the dependency is in a place below us, then we have to move ourself to after it
                  {
                     Object o = in.items[depIdx];
                     in.removeElementAt(depIdx);
                     in.insertElementAt(o,i);
                     changed = true;
                     i--;
                  }
               }
            }
         } while (changed);
      }
      Vector ordered = new Vector(30);
      for (i = 0; i < n; i++)
         if (((TestCase)in.items[i]).hardIndexNumber > 0)
            ordered.addElement(in.items[i]);
      useHIN = true;
      ordered.qsort();
      useHIN = false;

      // now set the order if it was explicitly requested
      for (i = 0; i < ordered.size(); i++)
      {
         TestCase tc = (TestCase)ordered.items[i];
         in.removeElement(tc);
         in.insertElementAt(tc, tc.hardIndexNumber-1);
      }
      // create the returning array
      TestCase tcs[] = new TestCase[n];
      for (i =0; i < n; i++)
         tcs[i] = (TestCase)in.items[i];
      return tcs;
   }

   // verify if all dependencies exist
   private int countDependencies(Vector in)
   {
      boolean allFound = true;
      int c = 0;
      int n = in.size();
      for (int i = 0; i < n; i++)
      {
         TestCase tc = (TestCase)in.items[i];
         if (tc.dependsOn != null)
         {
            c++;
            if (!ht.exists(tc.dependsOn))
            {
               error("Error! "+tc.methodName+" ("+tc.sourceFile+") depends on "+tc.dependsOn+", which was not found!");
               allFound = false;
            }
         }
      }
      return !allFound ? -1 : c;
   }

   private void error(String msg)
   {
      errorCount++;
      println(msg);
   }

   String SWTests_c[] =
   {
      "#include \"tcvm.h\"\n",
      "\n",
      "#define TEST_COUNT ",
      "\n\n",
      "// Function prototypes\n",
      //void testTimeCreate(struct TestSuite *tc, Context currentContext);
      "\n",
      "#ifdef ENABLE_TEST_SUITE\n",
      "\n",
      "void fillTestCaseArray(testFunc *tests)\n",
      "{\n",
      // tests[0] = testTimeCreate;
      "}\n",
      "\n",
      "void startTestSuite(Context currentContext)\n",
      "{\n",
      "   struct TestSuite *tc = createTestSuite();\n",
      "   int i;\n",
      "   int from = 0;\n",
      "   int to = TEST_COUNT-1;\n",
      "   int lastNumberOfFail = 0; //How many tests have failed until now?\n",
      "   int testsResults[TEST_COUNT] = { 0 }; // This flag vector indicates whether a given test had failed\n",
      "   testFunc tests[TEST_COUNT];\n",
      "\n",
      "   xmemzero(tests, sizeof(tests));\n",
      "   fillTestCaseArray(tests);\n",
      "\n",
      "   for (i = from; !tc->abort && i <= to; i++)\n",
      "   {\n",
      "      tc->total++;\n",
      "      currentContext->thrownException = null;\n",
      "      if (tests[i]) tests[i](tc, currentContext);\n",
      "      if (tc->failed != lastNumberOfFail)\n",
      "      {\n",
      "         testsResults[i] = 1;\n",
      "         lastNumberOfFail = tc->failed;\n",
      "      }\n",
      "   }\n",
      "   showTestResults(testsResults);\n",
      "}\n\n",
      "#endif\n\n",
   };

   private void buildOutput(TestCase []tcs) throws Exception
   {
      if (errorCount > 0)
      {
         println("Output not generated due to "+errorCount+" error(s) found");
         return;
      }
      int i,n = tcs.length;
      File f = new File("tc_tests.c",File.CREATE_EMPTY);
      DataStream ds = new DataStream(f);
      // set the size for TEST_COUNT
      SWTests_c[2] += n;
      // write the firxt part
      for (i =0; i <= 4; i++)
         ds.writeBytes(SWTests_c[i].getBytes());
      // write the function prototypes
      char[] spaces =  "                                                             ".toCharArray();
      StringBuffer sb = new StringBuffer(200);
      for (i = 0; i < n; i++)
      {
         sb.setLength(0);
         TestCase tc = tcs[i];
         sb.append("void test_").append(tc.methodName).append("(struct TestSuite *tc, Context currentContext);");
         if (sb.length() < 67) // pad
            sb.append(spaces,0,67-sb.length());
         sb.append("// ").append(formatPath(tc.sourceFile));
         if (tc.dependsOn != null)
            sb.append(" - depends on test").append(tc.dependsOn);
         ds.writeBytes(sb.append('\n').toString().getBytes());
      }
      // write the second part
      for (i =5; i <= 9; i++)
         ds.writeBytes(SWTests_c[i].getBytes());
      // write the function array assignments - tests[0] = testTimeCreate;
      for (i = 0; i < n; i++)
      {
         sb.setLength(0);
         ds.writeBytes(sb.append("   tests[").append(i).append("] = test_").append(tcs[i].methodName).append(";\n").toString().getBytes());
      }
      // write the last part
      for (i =10; i < SWTests_c.length; i++)
         ds.writeBytes(SWTests_c[i].getBytes());
      println("File generated. A total of "+n+" testcases were found.");
      f.close();
   }

   private String formatPath(String s)
   {
      if (s.startsWith(".\\"))
         s = s.substring(2);
      return Convert.replace(s, "\\\\", "\\").replace('\\','/');
   }

   private void searchIn(String folder) throws Exception
   {
      String[] files = new File(folder).listFiles();
      if (files != null)
      {
         for (int i =0; i < files.length; i++)
         {
            String fullPath = folder+'/'+files[i];
            if (files[i].endsWith("/"))
               searchIn(fullPath);
            else
            if (files[i].endsWith("_test.h"))
               processFile(fullPath);
         }
      }
   }

   private void processFile(String file)
   {
      try
      {
         File f = new File(file,File.READ_WRITE);
         LineReader lr = new LineReader(f);
         String line;
         while ((line=lr.readLine()) != null)
         {
            line = line.trim();
            if (line.startsWith("TESTCASE"))
               parseTestCase(line, file);
         }
         f.close();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }

   private void parseTestCase(String line, String file)
   {
      try
      {
         TestCase tc = new TestCase();
         file = (java.io.File.separator.charAt(0) == '/') ? file.replace('\\','/') : file.replace('/','\\');
         if (file.endsWith(java.io.File.separator))
            file = file.substring(0,file.length()-1);
         tc.sourceFile = file;
         int open = line.indexOf('(');
         int close = line.indexOf(')');
         tc.methodName = line.substring(open+1,close);
         // check the # commands
         if ((open=line.indexOf("#DEPENDS",close)) > 0)
            tc.dependsOn = line.substring(open+9,line.lastIndexOf(')'));
         else
         if ((open=line.indexOf('#')) > 0) // does it have a hard index number
            tc.hardIndexNumber = totalcross.sys.Convert.toInt(line.substring(open+1).trim());

         if (ht.exists(tc))
            error("Error! Testcase "+tc.methodName+" duplicated in "+tc.sourceFile+" and "+((TestCase)ht.get(tc)).sourceFile);
         else
            ht.put(tc,tc);
      }
      catch (Exception e)
      {
         println("Invalid line: "+line+". Exception occured: "+e.getMessage());
      }
   }

   private void println(String msg)
   {
      System.out.println(msg);
   }

   public static void main(String args[])
   {
      new GenTestCalls(args);
   }
}
