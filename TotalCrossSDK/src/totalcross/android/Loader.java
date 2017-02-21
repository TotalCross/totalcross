/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/

package totalcross.android;

import totalcross.*;
import totalcross.android.compat.*;
import totalcross.android.firebase.FirebaseUtils;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.pm.PackageManager.*;
import android.content.res.*;
import android.database.*;
import android.graphics.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.provider.*;
import android.speech.*;
import android.util.*;
import android.view.*;
import android.view.ViewGroup.*;
import android.view.inputmethod.*;
import android.widget.*;
import com.google.android.gms.ads.*;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.zxing.integration.android.*;
import com.intermec.aidc.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Loader extends Activity implements BarcodeReadListener
{
   public static boolean IS_EMULATOR = android.os.Build.MODEL.toLowerCase().indexOf("sdk") >= 0;
   public Handler achandler;
   private boolean runningVM;
   private static final int TAKE_PHOTO = 1234324330;
   private static final int JUST_QUIT = 1234324331;
   private static final int MAP_RETURN = 1234324332;
   private static final int ZXING_RETURN = 1234324333;
   private static final int EXTCAMERA_RETURN = 1234324334;
   private static final int SELECT_PICTURE = 1234324335;
   private static final int CAMERA_PIC_REQUEST = 1337;
   private static final int TEXT_TO_SPEECH = 1234324336;
   private static boolean onMainLoop;
   public static boolean isFullScreen;
   
   private static boolean onCreateCalled; //
   /** Called when the activity is first created. */
   public void onCreate(Bundle savedInstanceState)
   {
      super.onCreate(savedInstanceState);
      try
      {
         AndroidUtils.initialize(this);
         if (isSingleApk() && onCreateCalled) // bypass bug that will cause a new instance each time the app is minimized and called again (2282)
         {
            System.exit(2);
            return;
         }
         onCreateCalled = true;
         AndroidUtils.checkInstall();
         runVM();
      }
      catch (Throwable e)
      {
         String stack = Log.getStackTraceString(e);
         AndroidUtils.debug(stack);
         AndroidUtils.error("An exception was issued when launching the program. Please inform this stack trace to your software's vendor:\n\n"+stack,true);
      }
   }
   
   public void onRestart()
   {
      super.onRestart();
   }
   
   public String getImagePath(Uri uri) 
   {
      try
      {
         String[] projection = { MediaStore.Images.Media.DATA };
         Cursor cursor = managedQuery(uri, projection, null, null, null);
         if (cursor == null) return null;
         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
         String s=cursor.getString(column_index);
//         cursor.close(); - cant close cursors! or the vm will stall on second try
         return s;
      }
      catch (Throwable t)
      {
         AndroidUtils.handleException(t,false);
         return null;
      }
   }
   
   protected void onActivityResult(int requestCode, int resultCode, Intent data)
   {
      switch (requestCode)
      {
         case TEXT_TO_SPEECH:
            Launcher4A.soundResult = resultCode == RESULT_OK ? data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS).get(0) : null;
            Launcher4A.callingSound = false;
            break;
            
         case SELECT_PICTURE:
            if (resultCode == RESULT_OK)
            {
               Uri selectedImageUri = data.getData();
               String selectedImagePath = getImagePath(selectedImageUri);
               if (selectedImagePath == null)
                  resultCode = RESULT_OK+1;
               else
                  AndroidUtils.copyFile(selectedImagePath,imageFN,false);
            }
            Launcher4A.pictureTaken(resultCode != RESULT_OK ? 1 : 0);
            break;
         case Level5.BT_MAKE_DISCOVERABLE:
            Level5.getInstance().setResponse(resultCode != Activity.RESULT_CANCELED,null);
            break;
         case JUST_QUIT:
            finish();
            break;
         case TAKE_PHOTO:
            Launcher4A.pictureTaken(resultCode != RESULT_OK ? 1 : 0);
            break;
         case CAMERA_PIC_REQUEST:
            Launcher4A.pictureTaken(resultCode != RESULT_OK ? 1 : 0);
            break;
         case MAP_RETURN:
            Launcher4A.showingMap = false;
            break;
         case IntentIntegrator.REQUEST_CODE:
         {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            Launcher4A.zxingResult = result.getContents();
            Launcher4A.callingZXing = false;
         } break;
         case ZXING_RETURN:
            Launcher4A.zxingResult = resultCode == RESULT_OK ? data.getStringExtra("SCAN_RESULT") : null;
            Launcher4A.callingZXing = false;
            break;
         case EXTCAMERA_RETURN:
         {
             Uri capturedImageURI = data.<Uri>getParcelableExtra(MediaStore.EXTRA_OUTPUT);
            if (capturedImageURI == null)
            {
               AndroidUtils.debug("capturedImageURI is null!");
               Launcher4A.pictureTaken(0);
               break;
            }
            String[] projection = {MediaStore.Images.Media.DATA, BaseColumns._ID, MediaStore.Images.Media.DATE_ADDED}; 
            Cursor cursor = managedQuery(capturedImageURI, projection, null, null, null);
            
            String capturedImageFilePath = null;
            if (cursor.moveToFirst())
               capturedImageFilePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
            if (capturedImageFilePath == null || !AndroidUtils.copyFile(capturedImageFilePath,imageFN,cameraType == CAMERA_NATIVE_NOCOPY))
               resultCode = RESULT_OK+1; // error
            else
            {
               long date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
               autoRotatePhoto(imageFN);
               if (cameraType == CAMERA_NATIVE_NOCOPY) // if the file was deleted, delete from database too
                  try
                  { 
                     getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, BaseColumns._ID + "=" + cursor.getString(cursor.getColumnIndexOrThrow(BaseColumns._ID)), null);
                     try {new File(capturedImageFilePath).delete();} catch (Exception e) {} // on android 2.3 the code above does not work, so we just ensure that we delete the file
                     removeLastImageFromGallery(date);
                  } catch (Exception e) {AndroidUtils.handleException(e,false);}
            }
            Launcher4A.pictureTaken(resultCode != RESULT_OK ? 1 : 0);
            break;
         }
      }
   }

   private void removeLastImageFromGallery(long orig)
   {
      try
      {
         final String[] imageColumns = { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED };
         final String imageOrderBy = MediaStore.Images.Media._ID+" DESC";
         Cursor imageCursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, imageColumns, null, null, imageOrderBy);
         if (imageCursor.moveToFirst())
         {
            long last = imageCursor.getLong(imageCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED));
            int id = imageCursor.getInt(imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
            long dif = Math.abs(orig-last);
            if (dif < 1000) // 1 second - usually is less than 10ms
               getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, MediaStore.Images.Media._ID + "=?", new String[]{ Long.toString(id) } );
         }
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e, false);
      }
  }
   
   public static void autoRotatePhoto(String imagePath)
   {
      try
      {
         File f = new File(imagePath);
         ExifInterface exif = new ExifInterface(f.getPath());
         int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
         AndroidUtils.debug(imagePath+" -> "+orientation);
   
         int angle = 0;
         switch (orientation)
         {
            case ExifInterface.ORIENTATION_ROTATE_90: angle  = 90;  break;
            case ExifInterface.ORIENTATION_ROTATE_180: angle = 180; break;
            case ExifInterface.ORIENTATION_ROTATE_270: angle = 270; break;
            default: return;
         }
   
         Matrix mat = new Matrix();
         mat.postRotate(angle);
         BitmapFactory.Options options = new BitmapFactory.Options();
         options.inSampleSize = 2;
   
         Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
         Bitmap bitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
         FileOutputStream out = new FileOutputStream(f);
         bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
         out.close();
         AndroidUtils.debug("auto-rotated "+imagePath);
      }
      catch (Exception e)
      {
         AndroidUtils.handleException(e, false);
      }
   }

   private static final int SHOW_SATELLITE_PHOTOS = 1;
   public static final int USE_WAZE = 2;

   private void callRoute(double latI, double lonI, double latF, double lonF, String coord, int flags)
   {
      try
      {
         if ((flags & USE_WAZE) != 0)
         {
            try
            {
               String url = "waze://?ll="+latI+","+lonI+"&navigate=yes";
               Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
               Launcher4A.showingMap = false; // note: waze runs as a separate app, so we just return directly from here
               startActivity(intent);
               return;
            }
            catch ( ActivityNotFoundException ex)
            {
               AndroidUtils.debug("Waze not found, using default app");
               callGoogleMap(latI, lonI, (flags & SHOW_SATELLITE_PHOTOS) != 0);
               return;
            }
         }
         
         Intent intent = new Intent(this, Class.forName(totalcrossPKG+".RouteViewer"));
         intent.putExtra("latI",latI);
         intent.putExtra("lonI",lonI);
         intent.putExtra("latF",latF);
         intent.putExtra("lonF",lonF);
         intent.putExtra("coord",coord);
         intent.putExtra("sat",(flags & SHOW_SATELLITE_PHOTOS) != 0);
         startActivityForResult(intent, MAP_RETURN);
      }
      catch (Throwable e)
      {
         AndroidUtils.handleException(e,false);
      }
   }

   private void callGoogleMap(double lat, double lon, boolean sat)
   {
      try
      {
         Intent intent = new Intent(this, Class.forName(totalcrossPKG+".MapViewer"));
         intent.putExtra("lat",lat);
         intent.putExtra("lon",lon);
         intent.putExtra("sat",sat);
         startActivityForResult(intent, MAP_RETURN);
      }
      catch (Throwable e)
      {
         AndroidUtils.handleException(e,false);
      }
   }

   private void callGoogleMap(String items, boolean sat)
   {
      try
      {
         Intent intent = new Intent(this, Class.forName(totalcrossPKG+".MapViewer"));
         intent.putExtra("items",items);
         intent.putExtra("sat",sat);
         startActivityForResult(intent, MAP_RETURN);
      }
      catch (Throwable e)
      {
         AndroidUtils.handleException(e,false);
      }
   }

   private String imageFN;
   //private static final int CAMERA_CUSTOM = 0;
   private static final int CAMERA_NATIVE = 1;
   private static final int CAMERA_NATIVE_NOCOPY = 2;
   private static final int FROM_GALLERY = 3;
   private static String[] cameraTypes = {"CUSTOM","NATIVE","NATIVE_NOCOPY","GALLERY","UNDEFINED"};
   private int cameraType;
   private void captureCamera(String s, int quality, int width, int height, boolean allowRotation, int cameraType)
   {
      try
      {
         imageFN = s;
         this.cameraType = cameraType;
         String deviceId = Build.MANUFACTURER.replaceAll("\\P{ASCII}", " ") + " " + Build.MODEL.replaceAll("\\P{ASCII}", " ");
         AndroidUtils.debug("Taking photo "+width+"x"+height+" from "+cameraTypes[0 <= cameraType && cameraType <= 3 ? cameraType : 4]);
         if (cameraType == FROM_GALLERY)
         {
            Intent i = new Intent();
            i.setType("image/*");
            i.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(i, SELECT_PICTURE);
         }
         else
         if (cameraType == CAMERA_NATIVE || cameraType == CAMERA_NATIVE_NOCOPY)
         {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "tctemp.jpg");
            values.put (MediaStore.Images.Media.IS_PRIVATE, 1);
            Uri capturedImageURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);  
            intent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageURI);  
            startActivityForResult(intent, EXTCAMERA_RETURN);
         }
         else
         if ("SK GT-7340".equals(deviceId))
         {
            Uri outputFileUri = Uri.fromFile(new File(s));
            Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, outputFileUri);
            startActivityForResult(intent, CAMERA_PIC_REQUEST);
         }
         else
         {
            Intent intent = new Intent(this, Class.forName(totalcrossPKG+".CameraViewer"));
            intent.putExtra("file",s);
            intent.putExtra("quality",quality);
            intent.putExtra("width",width);
            intent.putExtra("height",height);
            intent.putExtra("allowRotation", allowRotation);
            startActivityForResult(intent, TAKE_PHOTO);
            Launcher4A.instance.nativeInitSize(null,-998,0);
         }
         AndroidUtils.debug("Launched photo");
      }
      catch (Throwable e)
      {
         AndroidUtils.handleException(e,false);
         Launcher4A.pictureTaken(1);
      }
   }
   
   private void dialNumber(String number)
   {
      startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:"+number)));
   }

   public static final int DIAL = 1;
   public static final int CAMERA = 2;
   public static final int TITLE = 3;
   public static final int EXEC = 4;
   public static final int LEVEL5 = 5;
   public static final int MAP = 6;
   public static final int FULLSCREEN = 7;
   public static final int ROUTE = 8;
   public static final int ZXING_SCAN = 9;
   public static final int MAPITEMS = 10;
   public static final int ADS_FUNC = 11;
   public static final int TOTEXT = 12;
   public static final int ORIENTATION = 13;
   
   public static String tcz;
   private String totalcrossPKG = "totalcross.android";
   
   public boolean isSingleApk()
   {
      return !AndroidUtils.pinfo.sharedUserId.equals("totalcross.app.sharedid");
   }
   
	private boolean myFirebasePackageName(String packageNameCandidate) {
		String pkgName = getApplicationContext().getPackageName();
		return pkgName.equals(packageNameCandidate);
	}
   
	private void initializeFirebase() {
		AssetManager assetManager = getResources().getAssets();
		
		try (InputStream stream = assetManager.open("google-services.json")) {
			JSONObject obj = (JSONObject) new JSONParser().parse(new InputStreamReader(stream));
			
			JSONObject projectInfo = (JSONObject)obj.get("project_info");
			String gcm_defaultSenderId = (String)projectInfo.get("project_number");
			String firebase_database_url = (String)projectInfo.get("firebase_url");
			String storage_bucket = (String)projectInfo.get("storage_bucket");
			
			JSONArray clients = (JSONArray)obj.get("client");
			boolean foundFirebaseClient = false; 
			String package_name = null;
			String mobilesdk_app_id = null;
			String current_api_key = null;
			
			for (Object clientObj: clients) {
				JSONObject client = (JSONObject) clientObj;
				JSONObject clientInfo = (JSONObject)client.get("client_info");
				JSONObject android_client_info = (JSONObject) clientInfo.get("android_client_info");
				package_name = (String) android_client_info.get("package_name");
				
				if (myFirebasePackageName(package_name)) {
					foundFirebaseClient = true;
					mobilesdk_app_id = (String) clientInfo.get("mobilesdk_app_id");
					JSONArray api_key = (JSONArray) client.get("api_key");
					JSONObject api_key0 = (JSONObject) api_key.get(0);
					current_api_key = (String) api_key0.get("current_key");
					break;
				}
			}
			
			if (foundFirebaseClient) {
				FirebaseOptions.Builder builder = new FirebaseOptions.Builder();
				builder.setGcmSenderId(gcm_defaultSenderId);//<string name="gcm_defaultSenderId" translatable="false">462748528174</string>
				builder.setStorageBucket(storage_bucket);//<string name="google_storage_bucket" translatable="false">totalcrossfirebaseteste.appspot.com</string>
				builder.setDatabaseUrl(firebase_database_url);//<string name="firebase_database_url" translatable="false">https://totalcrossfirebaseteste.firebaseio.com</string>
				builder.setApplicationId(mobilesdk_app_id);//<string name="google_app_id" translatable="false">1:462748528174:android:d1696eef73864aa2</string>
				builder.setApiKey(current_api_key);//<string name="google_api_key" translatable="false">AIzaSyCiU3EE9ckkvlzvyC8_dc7Z9MiC8NGgfHI</string>
				
				FirebaseApp.initializeApp(getApplicationContext(), builder.build());
			} else {
				AndroidUtils.debug("Could not initialize Firebase, can't find your package in google-services.json");
			}
		} catch (ParseException | IOException e) {
			AndroidUtils.debug("Could not initialize Firebase, probably 'google-services.json' isn't deployed or it isn't a valid json for google services");
			AndroidUtils.handleException(e,false);
		}
   }
   
   private void runVM()
   {
      if (runningVM) return;
      runningVM = true;
	  initializeFirebase();
      Hashtable<String,String> ht = AndroidUtils.readVMParameters();
      String tczname = tcz = ht.get("tczname");
      boolean isSingleAPK = false;
      if (tczname == null)
      {
         // this is a single apk. get the app name from the package
         String sharedId = AndroidUtils.pinfo.sharedUserId;
         if (sharedId.equals("totalcross.app.sharedid")) // is it the default shared id?
            AndroidUtils.error("Launching parameters not found",true);
         else
         {
            tczname = sharedId.substring(sharedId.lastIndexOf('.')+1);
            totalcrossPKG = "totalcross."+tczname;
            ht.put("apppath", AndroidUtils.pinfo.applicationInfo.dataDir);
            isSingleAPK = true;
            Bundle extras = getIntent().getExtras();
            if (extras != null && extras.containsKey("cmdline"))
               ht.put("cmdline",extras.getString("cmdline"));
         }
      }
      String appPath = ht.get("apppath");
      String fc = ht.get("fullscreen");
      isFullScreen = fc != null && fc.equalsIgnoreCase("true"); // used without /p
      
      // now getting from extra meta data
      try
      {
         if ("fullscreen:1".equals(getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA).metaData.getString("isFullScreen")))
            isFullScreen = true;
      }
      catch (NameNotFoundException e)
      {
         e.printStackTrace();
      }
      
      
      setTitle(tczname);
      if (isFullScreen)
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
      
      // start the vm
      achandler = new EventHandler();
      String cmdline = ht.get("cmdline");
      
      mainView = new Launcher4A(this, tczname, appPath, cmdline, isSingleAPK);
      mainLayout = new RelativeLayout(this);
      mainLayout.addView(mainView);
      setContentView(mainLayout); 
      onMainLoop = true;
   }

   RelativeLayout mainLayout;
   public static View mainView;
   public static AdView adView;
   
   class EventHandler extends Handler 
   {
      public void handleMessage(Message msg) 
      {
         Bundle b = msg.getData();
         switch (b.getInt("type"))
         {
            case LEVEL5:
               Level5.getInstance().processMessage(b);
               break;
            case DIAL:
               String nr = b.getString("dial.number");
               dialNumber(nr);
               break;
            case CAMERA:
               captureCamera(b.getString("showCamera.fileName"),b.getInt("showCamera.quality"),b.getInt("showCamera.width")
                                                               ,b.getInt("showCamera.height"),b.getBoolean("showCamera.allowRotation"),b.getInt("showCamera.cameraType"));
			   break;
            case TITLE:
               setTitle(b.getString("setDeviceTitle.title"));
               break;
            case EXEC:
               intentExec(b.getString("command"), b.getString("args"), b.getInt("launchCode"), b.getBoolean("wait"));
               break;
            case MAPITEMS:
               callGoogleMap(b.getString("items"), b.getBoolean("sat"));
               break;
            case MAP:
               callGoogleMap(b.getDouble("lat"), b.getDouble("lon"), b.getBoolean("sat"));
               break;
            case ROUTE:
               callRoute(b.getDouble("latI"), b.getDouble("lonI"),b.getDouble("latF"), b.getDouble("lonF"), b.getString("coord"), b.getInt("flags"));
               break;
            case FULLSCREEN:
            {
               boolean setAndHide = b.getBoolean("fullScreen");
               InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
               Window w = getWindow();
               if (setAndHide)
               {
                  try // for galaxy tab2 bar
                  {
                     java.lang.reflect.Method m = View.class.getMethod("setSystemUiVisibility", new Class[]{Integer.class});
                     final int SYSTEM_UI_FLAG_HIDE_NAVIGATION = 2;
                     m.invoke(Launcher4A.instance, new Integer(SYSTEM_UI_FLAG_HIDE_NAVIGATION));
                  }
                  catch (Exception e) {}
                  imm.hideSoftInputFromWindow(Launcher4A.instance.getWindowToken(), 0, Launcher4A.instance.siprecv);
                  w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                  w.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
               }
               else
               {
                  imm.showSoftInput(Launcher4A.instance, 0);
                  w.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                  w.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
               }
               break;
            }
            case ZXING_SCAN:
            {
               String cmd = b.getString("zxing.mode");
               StringTokenizer st = new StringTokenizer(cmd,"&");
               String mode = "SCAN_MODE";
               String scanmsg = "";
               while (st.hasMoreTokens())
               {
                  String s = st.nextToken();
                  int i = s.indexOf('=');
                  if (i == -1) continue;
                  String s1 = s.substring(0,i);
                  String s2 = s.substring(i+1);
                  if (s1.equalsIgnoreCase("mode"))
                     mode = s2;
                  else
                  if (s1.equalsIgnoreCase("msg"))
                     scanmsg = s2;
               }
               
               IntentIntegrator integrator = new IntentIntegrator(Loader.this);
               if (mode.equalsIgnoreCase("1D"))
               {
                  integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
               }
               else if (mode.equalsIgnoreCase("2D"))
               {
                  integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
               }
               else
               {
                  integrator.setDesiredBarcodeFormats(null);
               }
               integrator.setPrompt(scanmsg);
               integrator.setResultDisplayDuration(1000);
               integrator.autoWide();  // Wide scanning rectangle, may work better for 1D barcodes
               integrator.setCameraId(0);  // Use a specific camera of the device
               integrator.initiateScan();
               break;
            }               
            case TOTEXT:
               String title = b.getString("title");
               promptSpeechInput(title.isEmpty() ? null : title);
               break;
            case ADS_FUNC:
               adsFunc(b);
               break;
            case ORIENTATION:
               int o = b.getInt("orientation");
               if ((o & 3) != 3)
               {
                  boolean isPort = (o & ORIENTATION_PORTRAIT) != 0;
                  boolean isInv  = (o & ORIENTATION_INVERTED) != 0;
                  setRequestedOrientation(isPort ? (isInv ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT: ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) : (isInv ? ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE));
               }
               break;
         }
      }
   }
   private static final int ORIENTATION_PORTRAIT = 1;
   //private static final int ORIENTATION_LANDSCAPE = 2;
   private static final int ORIENTATION_INVERTED = 4;
   
   public enum Size
   {
      ADMOB_BANNER,
      ADMOB_FULL,
      ADMOB_LARGE,
      ADMOB_LEADER,
      ADMOB_MEDIUM,
      ADMOB_SKY,
      ADMOB_SMART,
   };
   
   public enum Position
   {
      BOTTOM,
      TOP
   };
   
   private AdSize toAdSize(int i)
   {
      switch (Size.values()[i])
      {
         case ADMOB_BANNER:  return AdSize.BANNER; 
         case ADMOB_FULL:    return AdSize.FULL_BANNER;
         case ADMOB_LARGE:   return AdSize.LARGE_BANNER;
         case ADMOB_LEADER:  return AdSize.LEADERBOARD;
         case ADMOB_MEDIUM:  return AdSize.MEDIUM_RECTANGLE;
         case ADMOB_SKY:     return AdSize.WIDE_SKYSCRAPER;
         case ADMOB_SMART:   return AdSize.SMART_BANNER;
      }
      return null;
   }

   private void configureAd(String id)
   {
      if (adView != null)
         return;
      
      adView = new AdView(this);
      adView.setAdUnitId(id);
      adView.setAdSize(defaultAdSize);
      adView.setVisibility(adIsVisible ? View.VISIBLE : View.INVISIBLE);
      adView.loadAd(new AdRequest.Builder().build());

      RelativeLayout.LayoutParams adParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
      adParams.addRule(adAtBottom ? RelativeLayout.ALIGN_PARENT_BOTTOM : RelativeLayout.ALIGN_PARENT_TOP);
      mainLayout.addView(adView, adParams);
      
      adView.setAdListener(new AdListener()
      {
         boolean firstAd = true;
         public void onAdLoaded()
         {
            AndroidUtils.debug("onAdLoaded");
            if (firstAd && adIsVisible)
            {
               firstAd = false;
               adView.setVisibility(View.GONE);
               adView.setVisibility(View.VISIBLE);
            }
         }
         public void onAdFailedToLoad(int errorCode)
         {
            AndroidUtils.debug("onAdFailedToLoad: "+errorCode);
         }
         public void onAdOpened()
         {
            AndroidUtils.debug("onAdOpened");
         }
         public void onAdClosed()
         {
            AndroidUtils.debug("onAdClosed");
         }
         public void onAdLeftApplication()
         {
            AndroidUtils.debug("onAdLeftApplication");
         }
      });
   }

   private AdSize defaultAdSize = AdSize.SMART_BANNER;
   private boolean adAtBottom = true;
   private boolean adIsVisible;

   // Banner: 640x100, Full: 936x120, Large: 640x200, Leader: 1456x180, Medium: 600x500, Sky: 320x1200, Smart: 720x100
   private void adsFunc(Bundle b)
   {
      int i = b.getInt("int");
      String s = b.getString("str");
      int ret = 0;
      
      switch (b.getInt("func"))
      {
         case Launcher4A.GET_WH:
            AdSize as = toAdSize(i);
            ret = as.getHeightInPixels(this) * 1000000 + as.getWidthInPixels(this);
            break;
         case Launcher4A.SET_SIZE:
               defaultAdSize = toAdSize(i);
               if (adView != null)
                  adView.setAdSize(toAdSize(i));
            break;         
         case Launcher4A.SET_POSITION:
            adAtBottom = Position.values()[i] == Position.BOTTOM;
            break;         
         case Launcher4A.SET_VISIBLE:
            adIsVisible = i == 1;
            if (adView != null)
               adView.setVisibility(adIsVisible ? View.VISIBLE : View.INVISIBLE);
            break;         
         case Launcher4A.IS_VISIBLE: 
            ret = adView != null && adView.isShown() ? 1 : 0; 
            break;
         case Launcher4A.CONFIGURE: // must be last step! 
            configureAd(s); 
            break;         
      }
      Launcher4A.adsRet = ret;
   }
   
   // Vm.exec("url","http://www.google.com/search?hl=en&source=hp&q=abraham+lincoln",0,false): launches a url
   // Vm.exec("totalcross.app.UIGadgets",null,0,false): launches another TotalCross' application
   // Vm.exec("viewer","file:///sdcard/G3Assets/541.jpg", 0, true);
   // Vm.exec("/sdcard/
   private void intentExec(String command, String args, int launchCode, boolean wait)
   {
      try
      {
         if (command.equals("***REGISTER PUSH TOKEN***")) // start firebase service
         {
            FirebaseUtils.setToken(this,args);
         }
         else
         if (command.equalsIgnoreCase("broadcast"))
         {
            Intent intent = new Intent();
            if (launchCode != 0)
               intent.addFlags(launchCode);
            intent.setAction(args);
            sendBroadcast(intent);
         }
         else
         if (command.equalsIgnoreCase("cmd"))
         {
            java.lang.Process process = Runtime.getRuntime().exec(args);
            if (wait)
               process.waitFor();
         }
         else
         if (command.equalsIgnoreCase("kingsoft"))
         {
            File f = new File(args);
            if (f.exists()) 
            {
               Intent intent = new Intent();
               intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
               intent.setAction(android.content.Intent.ACTION_VIEW);
               intent.setClassName("cn.wps.moffice_eng", "cn.wps.moffice.documentmanager.PreStartActivity");
               Uri uri = Uri.fromFile(f);
               intent.setData(uri);
               startActivity(intent);
            }
         }
         else
         if (command.equalsIgnoreCase("viewer"))
         {
            String argl = args.toLowerCase();
            if (android.os.Build.VERSION.SDK_INT >= 8 && AndroidUtils.isImage(argl))
            {
               Intent intent = new Intent(this, Class.forName(totalcrossPKG+".TouchImageViewer"));
               intent.putExtra("file",args);
               if (!wait)
                  startActivityForResult(intent, JUST_QUIT);
               else
                  startActivity(intent);
               return;
            }
            if (argl.endsWith(".pdf"))
            {
               File pdfFile = new File(args);
               if(pdfFile.exists()) 
               {
                   Uri path = Uri.fromFile(pdfFile); 
                   Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                   pdfIntent.setDataAndType(path, "application/pdf");
                   pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   try
                   {
                       startActivity(pdfIntent);
                   }
                   catch (ActivityNotFoundException e)
                   {
                      AndroidUtils.debug("THERE'S NO PDF READER TO OPEN "+args);
                      e.printStackTrace(); 
                   }
               }
            }
            else
            {
               Intent intent;
               if (argl.indexOf("youtu.be") >= 0 || argl.indexOf("youtube") >= 0)
                  intent = new Intent(Intent.ACTION_VIEW, Uri.parse(args));
               else
               {
                  intent = new Intent(this, Class.forName(totalcrossPKG+".WebViewer"));
                  intent.putExtra("url",args);
               }
               if (!wait)
                  startActivityForResult(intent, JUST_QUIT);
               else
                  startActivity(intent);
               return;
            }
         }
         else
         if (command.equalsIgnoreCase("url"))
         {
            if (args != null)
            {
               Intent i = new Intent(Intent.ACTION_VIEW);
               i.setData(Uri.parse(args));
               startActivity(i);
            }
         }
         else
         if (command.toLowerCase().endsWith(".apk"))
         {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            i.setDataAndType(Uri.fromFile(new File(command)), "application/vnd.android.package-archive");
            startActivity(i);
         }
         else
         {
            Intent i = new Intent();
            i.setClassName(command,command+"."+args);
            boolean isService = args.equalsIgnoreCase("TCService");
            AndroidUtils.debug("*** Vm.exec "+command+" . "+args+": "+isService);
            if (isService)
               startService(i);
            else
               startActivity(i);
         }
      }
      catch (Throwable e)
      {
         AndroidUtils.handleException(e,false);
      }
      if (!wait)
         finish();
   }
   
   public void onConfigurationChanged(Configuration config)
   {
      super.onConfigurationChanged(config);
      Launcher4A.hardwareKeyboardIsVisible = config.hardKeyboardHidden == Configuration.HARDKEYBOARDHIDDEN_NO || config.keyboard == Configuration.KEYBOARD_QWERTY; // motorola titanium returns HARDKEYBOARDHIDDEN_YES but KEYBOARD_QWERTY. In soft inputs, it returns KEYBOARD_NOKEYS
   }

   protected void onSaveInstanceState(Bundle outState) 
   {
      outState.clear();
   }
   
   protected void onDestroy()
   {
      if (runningVM) // guich@tc126_60: call app 1, home, call app 2: onDestroy is called
         quitVM();
      super.onDestroy(); 
   }
   
   protected void onPause()
   {
      if (runningVM)
         Launcher4A.sendCloseSIPEvent();
      Launcher4A.appPaused = true;
      if (onMainLoop)
         Launcher4A.appPaused();
      super.onPause();
      if (isFinishing() && runningVM) // guich@tc126_60: stop the vm if finishing is true, since onDestroy is not guaranteed to be called
         quitVM();                    // call app 1, exit, call app 2: onPause is called but onDestroy not
   }
   
   private void quitVM()
   {
      runningVM = onMainLoop = false;
      Launcher4A.stopVM();
      while (!Launcher4A.canQuit)
         try {Thread.sleep(100);} catch (Exception e) {}
      Launcher4A.closeTCZs();
      //Level5.getInstance().destroy();
      android.os.Process.killProcess(android.os.Process.myPid());
      // with these two lines, the application may have problems when then stub tries to load another vm instance.
      //try {Thread.sleep(1000);} catch (Exception e) {} // let the app take time to exit
      // System.exit(0); // make sure all threads will stop. also ensures that one app is not called as the app launched previously
   }
   
   protected void onResume()
   {
      if (onMainLoop)
         Launcher4A.appResumed();
      Launcher4A.appPaused = false;
      super.onResume();
   }

   public String strBarcodeData;    
   public static Semaphore semaphore = new Semaphore(1);

   public void barcodeRead(BarcodeReadEvent aBarcodeReadEvent)
   {
      try
	   {
         semaphore.acquire();		 
      }
	   catch (InterruptedException exception) {}
	   strBarcodeData = aBarcodeReadEvent.getBarcodeData();
	   semaphore.release();

      Launcher4A.instance._postEvent(Launcher4A.BARCODE_READ, 0, 0, 0, 0, 0);
   }

   public void onNewIntent(Intent i) 
   {
      AndroidUtils.debug("on new intent "+i);
      super.onNewIntent(i);
   }

   public void promptSpeechInput(String caption) // http://stackoverflow.com/questions/16228817/android-speech-recognition-app-without-pop-up
   {
      Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
      intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
      intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2000);
      //intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
      intent.putExtra(RecognizerIntent.EXTRA_PROMPT, caption);
      try
      {
         startActivityForResult(intent, TEXT_TO_SPEECH);
      }
      catch (ActivityNotFoundException a)
      {
         Toast.makeText(getApplicationContext(), "Text to Speech is not supported", Toast.LENGTH_SHORT).show();
      }
   }
}
