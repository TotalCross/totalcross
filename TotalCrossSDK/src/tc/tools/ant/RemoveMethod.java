package tc.tools.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

public class RemoveMethod extends Task
{
   private String methodName;
   private boolean keepOriginal;
   private List fileSets = new ArrayList();
   
   private static final String[] METHOD_MODIFIERS = new String[] { "abstract", "final", "native", "private", "protected", "public", "static", "synchronized", "strictfp" };
   
   public void setMethodName(String methodName)
   {
      this.methodName = methodName;
   }
   
   public void setKeepOriginal(String keepOriginal)
   {
      this.keepOriginal = Boolean.parseBoolean(keepOriginal);
   }
   
   public void addConfiguredFileSet(FileSet fileSet)
   {
      fileSets.add(fileSet);
   }
   
   public void execute() throws BuildException
   {
      String s = "\\s*((";
      for (int i = 0; i < METHOD_MODIFIERS.length; i++)
      {
         if (i > 0)
            s += "|";
         s += METHOD_MODIFIERS[i];
      }
      s += ")\\s+)*(\\w+(\\[\\])*?)\\s+(" + methodName + ")\\s*\\((\\s*\\w+(\\[\\])*\\s+\\w+\\s*(,\\s*\\w+(\\[\\])*\\s+\\w+\\s*)*)?\\)\\s*(throws\\s+(\\w+\\s*(,\\s*\\w+\\s*)*))?;?";
      
      Pattern regex = Pattern.compile(s);
      
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
               
               process(reader, writer, regex);
               
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
   
   private static void process(Reader reader, Writer writer, Pattern regex) throws IOException, BuildException
   {
      BufferedReader br = new BufferedReader(reader);
      
      int count = 0;
      boolean inMethod = false;
      
      String line;
      while ((line = br.readLine()) != null)
      {
         int i = 0, len = line.length();
         
         if (!inMethod)
         {
            int end = line.indexOf('{');
            if (end == -1)
               end = len;
            
            Matcher m = regex.matcher(line.substring(0, end));
            if (inMethod = m.matches())
            {
               i = m.end();
               if (line.charAt(i - 1) == ';')
                  inMethod = false;
            }
         }
         
         if (inMethod)
         {
            char[] chars = line.toCharArray();
            for (; i < len && inMethod; i++)
            {
               if (chars[i] == '{')
                  count++;
               else if (chars[i] == '}' && --count == 0)
                  inMethod = false;
            }
         }
         
         if (!inMethod)
         {
            if (len == 0)
               writer.write("\r\n");
            else if (i < len)
               writer.write(line.substring(i) + "\r\n");
         }
      }
   }
}
