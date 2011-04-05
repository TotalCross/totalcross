package tc.tools.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.Vector;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

public class BBPreComp extends Task
{
   private List fileSets = new ArrayList();
   private String defines;
   private boolean keepOriginal;
   
   public void setDefines(String defines)
   {
      this.defines = defines;
   }
   
   public void setKeepOriginal(String keepOriginal)
   {
      this.keepOriginal = Boolean.parseBoolean(keepOriginal);
   }
   
   public void execute() throws BuildException
   {
      Project project = getProject();
      try
      {
         Iterator it = fileSets.iterator();
         while (it.hasNext())
         {
            FileSet fileSet = (FileSet)it.next();
            File fileSetDir = fileSet.getDir(project);
            String[] files = fileSet.getDirectoryScanner(project).getIncludedFiles();

            for (int i = 0; i < files.length; i++)
            {
               File src = new File(fileSetDir, files[i] + ".original");
               File dst = new File(fileSetDir, files[i]);
               
               if (src.exists() && !src.delete())
                  throw new BuildException("Could not delete file: " + src.getCanonicalPath());
               dst.renameTo(src);
               
               FileReader reader = new FileReader(src);
               FileWriter writer = new FileWriter(dst);
               
               process(reader, writer, tokenizeDefines(defines));
               
               reader.close();
               writer.close();
               
               if (!keepOriginal)
                  src.delete();
            }
         }
      }
      catch (IOException ex)
      {
         throw new BuildException(ex);
      }
   }
   
   public void addConfiguredFileSet(FileSet fileSet)
   {
      fileSets.add(fileSet);
   }
   
   private void process(Reader reader, Writer writer, Hashtable ht) throws IOException, BuildException
   {
      BufferedReader br = new BufferedReader(reader);
      Stack stmts = new Stack();
      Stack process = new Stack();
      process.push("Y");
      
      String line;
      while ((line = br.readLine()) != null)
      {
         String s = line.trim();
         if (s.startsWith("//$"))
         {
            try
            {
               String[] stmt = tokenize(s.substring(3), ',');
               if (stmt.length == 0)
                  throw new BuildException("Invalid format");
               
               if (stmt[0].equalsIgnoreCase("IF") || stmt[0].equalsIgnoreCase("IFNOT"))
               {
                  String p;
                  if ("Y".equals(process.peek()))
                  {
                     boolean res = checkIfStatement(stmt, ht);
                     if (stmt[0].equalsIgnoreCase("IFNOT")) // IFNOT has inverse meaning
                        res = !res;
                     p = res ? "Y" : "N";
                  }
                  else
                     p = "I";
                  
                  stmts.push("IF");
                  process.push(p);
               }
               else if (stmt[0].equalsIgnoreCase("ELSE"))
               {
                  if (!"IF".equals(stmts.pop()))
                     throw new BuildException("IF statement is missing");
                  
                  stmts.push("ELSE");
                  if (!"I".equals(process.peek()))
                     process.push("Y".equals(process.pop()) ? "N" : "Y");
               }
               else if (stmt[0].equalsIgnoreCase("END"))
               {
                  String last = (String)stmts.pop();
                  if (!"IF".equals(last) && !"ELSE".equals(last))
                     throw new BuildException("IF and/or ELSE statements are missing");
                     
                  process.pop();
               }
               else
                  throw new BuildException("Unknown tag \"" + stmt[0] + "\"");
            }
            catch (BuildException ex)
            {
               throw new BuildException("Invalid statement; " + ex.getMessage() + ": " + s, ex);
            }
         }
         else if ("Y".equals(process.peek()))
            writer.write(line + "\r\n");
      }
      
      if (!stmts.isEmpty())
         throw new BuildException("END statement is missing");
   }
   
   private boolean checkIfStatement(String[] stmt, Hashtable ht) throws BuildException
   {
      if (stmt.length == 2) // simple IFDEF
         return ht.containsKey(stmt[1]);
      else if (stmt.length == 4) // IF with parameters
      {
         String s1 = (String)ht.get(stmt[1]);
         String s2 = stmt[3];
         Double d1 = stringToDouble(s1);
         Double d2 = stringToDouble(s2);
         boolean isNumberOp = d1 != null && d2 != null;
            
         if (stmt[2].equals("=="))
            return isNumberOp ? d1.doubleValue() == d2.doubleValue() : s1.equals(s2);
         else if (stmt[2].equals("!="))
            return isNumberOp ? d1.doubleValue() != d2.doubleValue() : !s1.equals(s2);
         else if (stmt[2].equals("<"))
            return isNumberOp ? d1.doubleValue() < d2.doubleValue() : s1.compareTo(s2) < 0;
         else if (stmt[2].equals("<="))
            return isNumberOp ? d1.doubleValue() <= d2.doubleValue() : s1.compareTo(s2) <= 0;
         else if (stmt[2].equals(">"))
            return isNumberOp ? d1.doubleValue() > d2.doubleValue() : s1.compareTo(s2) > 0;
         else if (stmt[2].equals(">="))
            return isNumberOp ? d1.doubleValue() >= d2.doubleValue() : s1.compareTo(s2) >= 0;
         else
            throw new BuildException("Invalid operator \"" + stmt[2] + "\"");
      }
      else
         throw new BuildException("Invalid syntax");
   }
   
   private Double stringToDouble(String s)
   {
      try
      {
         return Double.valueOf(s == null ? 0.0 : Double.parseDouble(s));
      }
      catch (NumberFormatException ex)
      {
         return null;
      }
   }
   
   private Hashtable tokenizeDefines(String defines) throws BuildException
   {
      Hashtable ht = new Hashtable();
      
      if (defines != null)
      {
         String[] defs = tokenize(defines, ',');
         for (int i = defs.length; --i >= 0;)
         {
            String[] vals = tokenize(defs[i], '=');
            if (vals.length == 1)
               ht.put(vals[0], "");
            else if (vals.length == 2)
               ht.put(vals[0], vals[1]);
            else
               throw new BuildException("Invalid defines format: " + defines);
         }
      }
      
      return ht;
   }
   
   private String[] tokenize(String src, char delim)
   {
      Vector tokens = new Vector();
      boolean escape = false;
      
      src = src.trim() + delim;
      char[] chars = src.toCharArray();
      StringBuffer buf = new StringBuffer(chars.length);
      
      for (int i = 0; i < chars.length; i++)
      {
         char c = chars[i];
         if (c == '\\' && !escape)
            escape = true;
         else
         {
            if (c == delim && !escape)
            {
               String s = buf.toString().trim();
               buf.setLength(0);
               
               if (!s.isEmpty())
                  tokens.add(s);
            }
            else
               buf.append(c);
            
            escape = false;
         }
      }
      
      String[] result = new String[tokens.size()];
      tokens.copyInto(result);
      
      return result;
   }
}
