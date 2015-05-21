package tc.samples.like.fb;

import totalcross.ui.image.*;

public class FBImages
{
   public static Image chat, content, friends, group, menu, message, news, online, search;
   
   public static void load(int fmH) throws Exception
   {
      chat    = new Image("img/chat.png").smoothScaledFixedAspectRatio(fmH,true);
      content = new Image("img/content.png").smoothScaledFixedAspectRatio(fmH,true);
      friends = new Image("img/friends.png").smoothScaledFixedAspectRatio(fmH,true);
      group   = new Image("img/group.png").smoothScaledFixedAspectRatio(fmH,true);
      menu    = new Image("img/menu.png").smoothScaledFixedAspectRatio(fmH,true);
      message = new Image("img/message.png").smoothScaledFixedAspectRatio(fmH,true);
      news    = new Image("img/news.png").smoothScaledFixedAspectRatio(fmH,true);
      online  = new Image("img/online.png").smoothScaledFixedAspectRatio(fmH,true);
      search  = new Image("img/search.png").smoothScaledFixedAspectRatio(fmH,true);
   }
}
