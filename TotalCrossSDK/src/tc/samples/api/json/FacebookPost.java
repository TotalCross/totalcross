package tc.samples.api.json;

public class FacebookPost
{
   private String name;
   private long date;
   private String text;
   private int likes;
   
   public String getName()
   {
      return name;
   }

   public void setName(String name)
   {
      this.name = name;
   }

   public long getDate()
   {
      return date;
   }

   public void setDate(long date)
   {
      this.date = date;
   }

   public String getText()
   {
      return text;
   }

   public void setText(String text)
   {
      this.text = text;
   }

   public int getLikes()
   {
      return likes;
   }

   public void setLikes(int likes)
   {
      this.likes = likes;
   }

   public FacebookPost()
   {
   }

   public FacebookPost(String name, long date, String text, int likes)
   {
      this.name = name;
      this.date = date;
      this.text = text;
      this.likes = likes;
   }

}
