// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only



void createTempFileName(char* dest, char* ext);
static bool takingPicture;
static int32 code;
/*
 * Class:     totalcross_Launcher4A
 * Method:    pictureTaken
 * Signature: (I)V
 */
void JNICALL Java_totalcross_Launcher4A_pictureTaken(JNIEnv *env, jclass _class, jint jcode)
{                                     
   code = jcode;
   takingPicture = false;         
}

static TCObject Camera_getNativeResolutions(Context currentContext)
{
   JNIEnv *env = getJNIEnv();
   TCObject ret = null;
   
   jmethodID method = (*env)->GetStaticMethodID(env, applicationClass, "requestCameraPermission", "()I");
   jint result = (*env)->CallStaticIntMethod(env, applicationClass, method);
   if (result > 0) {
       jstring src = (*env)->CallStaticObjectMethod(env, applicationClass, jgetNativeResolutions); 
       if (src != null) {
          const char *str = (*env)->GetStringUTFChars(env, src, 0);
          if (str) {
             ret = createStringObjectFromCharP(currentContext,(CharP)str,-1);
             (*env)->ReleaseStringUTFChars(env, src, str);
          }
          (*env)->DeleteLocalRef(env, src); // guich@tc125_1
       }
   }
   return ret;
}

static void cameraClick(NMParams p)
{
   TCObject obj = p->obj[0];
   TCObject defaultFN = Camera_defaultFileName(obj);                      
   int32 width = Camera_resolutionWidth(obj);
   int32 height = Camera_resolutionHeight(obj);
   int32 quality = Camera_stillQuality(obj);
   bool allowRotation = Camera_allowRotation(obj);
   int cameraType = Camera_cameraType(obj);
   JNIEnv *env = getJNIEnv();      
   bool isPhoto = Camera_captureMode(obj) == 0;
   if (env)                      
   {
	   jmethodID method = (*env)->GetStaticMethodID(env, applicationClass, "requestCameraPermission", "()I");
	   jint result = (*env)->CallStaticIntMethod(env, applicationClass, method);
	   if (result <= 0) {
		   p->retO = null;
		   return;
	   }
	   
      char fileName[MAX_PATHNAME];
      JChar jfn[MAX_PATHNAME];
      jstring s;
      int32 len;
      if (defaultFN == null)
         createTempFileName(fileName, isPhoto ? ".jpg" : ".3gp");
      else
      {
         JCharP2CharPBuf(String_charsStart(defaultFN),String_charsLen(defaultFN),fileName);
         if (xstrchr(fileName,'/') == null && xstrchr(fileName,'\\') == null) // no path specified:
         {
            char temp[MAX_PATHNAME];
            xstrcpy(temp,getAppPath());
            xstrcat(temp,"/");
            xstrcat(temp, fileName);
            xstrcpy(fileName,temp);
         }
      }                                     
      len = xstrlen(fileName);
      if (!isPhoto && len > 4 && fileName[len-3] != '3') // is a movie and filename is .jpg? make it 3gp
      {
         fileName[len-3] = '3';
         fileName[len-2] = 'g';
         fileName[len-1] = 'p';
      }
      CharP2JCharPBuf(fileName,len,jfn,true);
      s = (*env)->NewString(env,jfn,len);
      (*env)->CallStaticVoidMethod(env, applicationClass, jshowCamera, s,quality,width,height, (jboolean)allowRotation,cameraType); 
      for (takingPicture = true; takingPicture;) // block vm until the picture is taken
         Sleep(250);
      switch (code)
      {
         case 0: 
            p->retO = createStringObjectFromCharP(p->currentContext, fileName, xstrlen(fileName)); 
            setObjectLock(p->retO,UNLOCKED);
            break;
         case 1: p->retO = null; break; // cancelled
      }
      (*env)->DeleteLocalRef(env, s);
   }
}
