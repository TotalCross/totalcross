package totalcross.firebase.iid;

/**
 * Use it like https://firebase.google.com/docs/reference/android/com/google/firebase/iid/FirebaseInstanceId
 * 
 * In the enabled device, it will search for the default FirebaseApp name that TotalCross uses and return
 * it's FirebaseInstanceId instance token. If in a device that is not enabled Firebase (be it lack of
 * configuration file or not yet binded with TotalCross), it will return null.
 * 
 * If you are going to deliver a unicast push message, you must retrieve the must recent token and use it.
 * 
 * Currently suported platforms:
 * <ul>
 * 		<li>Android</li>
 * </ul>
 */
public class FirebaseInstanceId {
	private static FirebaseInstanceId instance = new FirebaseInstanceId();
	
	private FirebaseInstanceId() {
		
	}
	
	public static FirebaseInstanceId getInstance() {
		return instance;
	}
	
	public String getToken() {
		return null;
	}
	
	public native String getToken4D();
}
