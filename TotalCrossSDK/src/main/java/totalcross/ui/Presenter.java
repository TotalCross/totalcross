package totalcross.ui;

import totalcross.ui.Control;

public interface Presenter<T extends Control> {

  T getView();
}
