package tc.samples.like.fb;

import totalcross.ui.image.*;

public class FBImages
{
   public static Image chat, content, friends, group, menu, message, news, online, search;
   public static Image comment, like, share;
   
   public static void load(int fmH) throws Exception
   {
      int top = fmH*3/2;
      group   = new Image("img/group.png").smoothScaledFixedAspectRatio(fmH,true);
      message = new Image("img/message.png").smoothScaledFixedAspectRatio(fmH,true);

      content = new Image("img/content.png").smoothScaledFixedAspectRatio(top,true);
      friends = new Image("img/friends.png").smoothScaledFixedAspectRatio(top,true);
      chat    = new Image("img/chat.png").smoothScaledFixedAspectRatio(top,true);
      news    = new Image("img/news.png").smoothScaledFixedAspectRatio(top,true);
      menu    = new Image("img/menu.png").smoothScaledFixedAspectRatio(top,true);
      
      online  = new Image("img/online.png").smoothScaledFixedAspectRatio(top,true);
      search  = new Image("img/search.png").smoothScaledFixedAspectRatio(top,true);
      
      comment = new Image("img/comment.png").smoothScaledFixedAspectRatio(fmH,true);
      like    = new Image("img/like.png").smoothScaledFixedAspectRatio(fmH,true);
      share   = new Image("img/share.png").smoothScaledFixedAspectRatio(fmH,true);
   }
}
