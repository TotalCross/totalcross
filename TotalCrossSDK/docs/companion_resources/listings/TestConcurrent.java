import totalcross.ui.*;
import totalcross.util.concurrent.*;
public class TestConcurrent extends MainWindow implements Runnable
{
   StringBuffer sb = new StringBuffer(100);
   static int threadCounter;
   static int globalCounter;

   Lock lock = new Lock(); // the Lock object

   public void run()
   {
      int c = threadCounter++;
      synchronized (lock) // *****
      {
         for (int i =0; i < 50; i++)
         {
            sb.setLength(0);
            sb.append(c).append(" -> ").append(globalCounter++)
                                       .append(' ');
            for (int j = 0; j < 100; j++)
               sb.append('a');
            log(sb.toString());
         }
      }
   }
   
   public void initUI()
   {
      add(lb = new ListBox(),LEFT,TOP,FILL,FILL);
      new Thread(this).start();
      new Thread(this).start();
      new Thread(this).start();
   }
   
   ListBox lb;
   void log(String s)
   {
      lb.add(s);
      lb.selectLast();
   }
}