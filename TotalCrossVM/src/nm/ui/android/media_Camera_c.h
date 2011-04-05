/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2011 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



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

static void cameraClick(NMParams p)
{
   Object obj = p->obj[0];
   Object defaultFN = Camera_defaultFileName(obj);
   JNIEnv *env = getJNIEnv();
   bool isPhoto = Camera_captureMode(obj) == 0;
   if (env)                      
   {
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
      (*env)->CallStaticVoidMethod(env, applicationClass, jshowCamera, s); 
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
   }
}
