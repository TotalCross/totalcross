package totalcross.ui;

public interface Presenter<T extends Control> {

  T getView();
}
