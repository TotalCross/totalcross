/**
 * Copyright (c) 2001, Sergey A. Samokhodkin
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, 
 * are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, 
 * this list of conditions and the following disclaimer. 
 * - Redistributions in binary form 
 * must reproduce the above copyright notice, this list of conditions and the following 
 * disclaimer in the documentation and/or other materials provided with the distribution.
 * - Neither the name of jregex nor the names of its contributors may be used 
 * to endorse or promote products derived from this software without specific prior 
 * written permission. 
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 * IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY 
 * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * @version 1.2_01
 */

package totalcross.util.regex;

import totalcross.io.*;
import totalcross.util.*;

/**
 * The Tokenizer class suggests a methods to break a text into tokens using 
 * occurences of a pattern as delimiters.
 * There are two ways to obtain a text tokenizer for some pattern:<pre>
 * Pattern p=new Pattern("\\s+"); //any number of space characters
 * String text="blah blah blah";
 * //by factory method
 * RETokenizer tok1=p.tokenizer(text);
 * //or by constructor
 * RETokenizer tok2=new RETokenizer(p,text);
 * </pre>
 * Now the one way is to use the tokenizer as a token enumeration/iterator:<pre>
 * while(tok1.hasMore()) System.out.println(tok1.nextToken());
 * </pre>
 * and another way is to split it into a String array:<pre> 
 * String[] arr=tok2.split();
 * for(int i=0;i<tok2.length;i++) System.out.println(arr[i]);
 * </pre>
 * @see        Pattern#tokenizer(java.lang.String)
 */

public class RETokenizer {
   private Matcher matcher;
   private boolean checked;
   private boolean hasToken;
   private String token;
   private boolean endReached=false;
   private boolean emptyTokensEnabnled=false;
   
   public RETokenizer(Pattern pattern,String text){
      this(pattern.matcher(text),false);
   }
   
   public RETokenizer(Pattern pattern,char[] chars,int off,int len){
      this(pattern.matcher(chars,off,len),false);
   }
   
   public RETokenizer(Pattern pattern,CharStream r,int len) throws IOException{
      this(pattern.matcher(r,len),false);
   }
   
   public RETokenizer(Matcher m, boolean emptyEnabled){
      matcher=m;
      emptyTokensEnabnled=emptyEnabled;
   }
   
   public void setEmptyEnabled(boolean b){
      emptyTokensEnabnled=b;
   }
   
   public boolean isEmptyEnabled(){
      return emptyTokensEnabnled;
   }
   
   public boolean hasMore(){
      if(!checked) check();
      return hasToken;
   }
   
   public String nextToken() throws ElementNotFoundException{
      if(!checked) check();
      if(!hasToken) throw new ElementNotFoundException("");
      checked=false;
      return token;
   }
   
   public String[] split() throws ElementNotFoundException{
      return collect(this);
   }
   
   public void reset(){
      matcher.setPosition(0);
   }
   
   private static final String[] collect(RETokenizer tok) throws ElementNotFoundException{
      java.util.ArrayList<String> ll = new java.util.ArrayList<String>(50);//<String> ll = new java.util.LinkedList<String>();
      while (tok.hasMore())
         ll.add(tok.nextToken());
      return (String[])ll.toArray(new String[ll.size()]);
   }
   
   private void check(){
      final boolean emptyOk=this.emptyTokensEnabnled;
      checked=true;
      if(endReached){
         hasToken=false;
         return;
      }
      Matcher m=matcher;
      boolean hasMatch=false;
      while(m.find()){
         if(m.start()>0){
            hasMatch=true;
            break;
         }
         else if(m.end()>0){
            if(emptyOk){
               hasMatch=true;
               break;
            }
            else m.setTarget(m,MatchResult.SUFFIX);
         }
      }
      if(!hasMatch){
         endReached=true;
         if(m.length(MatchResult.TARGET)==0 && !emptyOk){
            hasToken=false;
         }
         else{
            hasToken=true;
            token=m.target();
         }
         return;
      }
//System.out.println(m.target()+": "+m.groupv());
//System.out.println("prefix: "+m.prefix());
//System.out.println("suffix: "+m.suffix());
      hasToken=true;
      token=m.prefix();
      m.setTarget(m,MatchResult.SUFFIX);
      //m.setTarget(m.suffix());
   }
   
   public boolean hasMoreElements(){
      return hasMore();
   }
   
  /**
   * @return a next token as a String
 * @throws ElementNotFoundException 
   */
   public Object nextElement() throws ElementNotFoundException{
      return nextToken();
   }
   
   /*
   public static void main(String[] args){
      RETokenizer rt=new RETokenizer(new Pattern("/").matcher("/a//b/c/"),false);
      while(rt.hasMore()){
         System.out.println("<"+rt.nextToken()+">");
      }
   }
   */
}