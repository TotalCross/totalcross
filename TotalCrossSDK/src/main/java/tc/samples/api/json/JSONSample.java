package tc.samples.api.json;

import java.util.List;

import tc.samples.api.BaseContainer;
import totalcross.json.JSONFactory;
import totalcross.ui.Label;
import totalcross.ui.dialog.MessageBox;

public class JSONSample extends BaseContainer {
  @Override
  public void initUI() {
    super.initUI();
    try {
      String line = "[{\"name\":\"Mary\",\"date\":61395803160000,\"text\":\"My first post\",\"likes\":1},{\"name\":\"John\",\"date\":61395803820000,\"text\":\"I like TotalCross\",\"likes\":200}]";
      add(new Label("Input line", LEFT, 0, true), LEFT, TOP);
      Label l = new Label(line);
      l.autoSplit = true;
      add(l, LEFT, AFTER, FILL, PREFERRED);
      add(new Label("As array: ", LEFT, 0, true), LEFT, AFTER, FILL, PREFERRED);
      FacebookPost[] posts = JSONFactory.parse(line, FacebookPost[].class);
      for (FacebookPost f : posts) {
        add(new Label(f.getName() + " / " + f.getText() + " / " + f.getLikes() + " / " + f.getDate()), LEFT, AFTER);
      }

      add(new Label("As List: ", LEFT, 0, true), LEFT, AFTER, FILL, PREFERRED);
      List<FacebookPost> posts2 = JSONFactory.asList(line, FacebookPost.class);
      for (FacebookPost f : posts2) {
        add(new Label(f.getName() + " / " + f.getText() + " / " + f.getLikes() + " / " + f.getDate()), LEFT, AFTER);
      }
    } catch (Exception ee) {
      MessageBox.showException(ee, true);
    }
  }
}