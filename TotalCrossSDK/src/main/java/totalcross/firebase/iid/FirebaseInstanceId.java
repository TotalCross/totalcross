package totalcross.firebase.iid;

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
