package totalcross.firebase;

import totalcross.firebase.iid.FirebaseInstanceIdService;
import totalcross.ui.MainWindow;
import totalcross.ui.event.TimerEvent;
import totalcross.ui.event.TimerListener;
import totalcross.util.concurrent.Lock;

/**
 * For internal use only. Please, do not mess here. Leave this be.
 */
public class FirebaseManager {
  static FirebaseManager instance = new FirebaseManager();
  FirebaseInstanceIdService registeredService;
  private boolean firstTime = true;

  Lock runningLock = new Lock();

  private class DelayedRun implements TimerListener {
    private static final int DELAY_EXCEPTION_PENALTY = 10;
    private static final int DELAY_MAX = 60;
    boolean running = false;

    TimerEvent event = new TimerEvent();
    int delay;

    public void init() {
      delay = 1;

      synchronized (runningLock) {
        if (running) {
          return;
        } else {
          running = true;
        }
      }
      addTimer();
    }

    void addTimer() {
      MainWindow.getMainWindow().addTimer(event, delay * 1000);
    }

    @Override
    public void timerTriggered(TimerEvent e) {
      if (e == event) {
        doStuff();
      }
    }

    void doStuff() {
      try {
        registeredService.onTokenRefresh();

        synchronized (runningLock) {
          running = false;
        }
      } catch (Exception e) {
        e.printStackTrace();
        addPenalty(DELAY_EXCEPTION_PENALTY);

        addTimer();
      }
    }

    void addPenalty(int penalty) {
      if (delay + penalty > DELAY_MAX) {
        delay = DELAY_MAX;
      } else {
        delay += penalty;
      }
    }

    @Override
    public String toString() {
      return "TotalCross/Firebase delayed runner";
    }
  }

  DelayedRun delayedRunner = new DelayedRun();

  private FirebaseManager() {
    MainWindow.getMainWindow().addTimerListener(delayedRunner);
  }

  public static FirebaseManager getInstance() {
    return instance;
  }

  public void registerFirebaseInstanceIdService(FirebaseInstanceIdService service) {
    synchronized (runningLock) {
      if (firstTime) {
        registeredService = service;
        firstTime = false;
      } else {
        throw new UnsupportedOperationException(
            "One should implement MainWindow.initFirebaseInstanceIdService() to register a firebase instanceIdService");
      }
    }
  }

  public void tokenReceived() {
    if (registeredService != null) {
      delayedRunner.init();
    }
  }
}
