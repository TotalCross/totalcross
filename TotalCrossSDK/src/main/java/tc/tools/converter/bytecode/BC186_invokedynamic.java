package tc.tools.converter.bytecode;

public class BC186_invokedynamic extends MethodCall {
   public BC186_invokedynamic() {
     super(readUInt16(pc + 1));
     pcInc = 5;
   }
 
   @Override
   public void exec() {
   }
 }
