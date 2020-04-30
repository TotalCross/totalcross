// Copyright (C) 2000-2013 SuperWaba Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only
 


#include "tcvm.h"
#include "tcz.h"
#include "nativeProcAddressesTC.h"

#if defined (WINCE) || defined (WIN32)
 #include "malloc.h"
 #include "win/startup_c.h"
#elif defined(ANDROID)
 #include "android/startup_c.h"
#else
 #include "posix/startup_c.h"
#endif

void rebootDevice(); // implemented in nm/sys/<plat>/Vm_c.h
bool initGraphicsBeforeSettings(Context currentContext, int16 appTczAttr);
bool initGraphicsAfterSettings(Context currentContext);
void destroyGraphics();

static Context initAll(CharP* args)
{
   Context c = null;
   bool ok = true; // structured this way to make debugging easier
#if defined WIN32 && !defined WINCE
   char appPathTemp[MAX_PATHNAME];
   CharP auxP;
#else
   UNUSED(args);
#endif
#ifndef ANDROID
   getWorkingDir();
#endif
#if defined WIN32 && !defined WINCE
   if ((auxP = xstrstr(*args, " /cmd ")) != null) // check if there's a cmd line
      xstrncpy(appPathTemp, *args, (auxP - *args) * sizeof(char));
   if ((auxP = xstrrchr(appPathTemp, '/')) != null)
   {
      *auxP = 0;
      xstrcpy(appPath, appPathTemp);
      *args += xstrlen(appPathTemp) + 1;
   }
#endif
   ok = ok && initDebug();
   ok = ok && initGlobals();
   ok = ok && initMem();
   if (ok) firstTS = getTimeStamp();
   ok = ok && (c=initContexts()) != null;
   ok = ok && initObjectMemoryManager();
   ok = ok && initClassInfo();
   initNativeProcAddresses();
   if (ok) registerWake(true);
   return ok ? c : null;
}

static void destroyAll() // must be in inverse order of initAll calls
{
   threadDestroyAll(); // first all threads must be destroyed - NOTE: when debugging on win32, this may hang the Visual C++ ide.
   destroyingApplication = true; // now is safe to destroy all objects
   runFinalizers();
   destroyContexts();
   if (isMainWindow) destroyGraphics(); // graphics can't be destroyed before threads are destroyed
   registerWake(false);
   destroyNativeLib();
   if (tcSettings.showMemoryMessagesAtExit != NULL)
      showMemoryMessagesAtExit = *tcSettings.showMemoryMessagesAtExit; // guich@tc114: save in a global var, since tcSettings will no longer be available
   destroyObjectMemoryManager(); // must be before ClassInfo destroy
   destroyNativeProcAddresses();
   destroyClassInfo();
   xmemzero(&tcSettings, sizeof(tcSettings));
   destroyTCZ();
   destroyMem();    
   destroyDebug(); // must be after destroy mem, because mem leaks may be written to the debug
   destroyGlobals();
}

int32 exitProgram(int32 exitcode)
{            
   if (exitcode != 0)
      debug("Exiting: %d", exitcode);
   exitCode = exitcode;
   if (isMainWindow) destroyEvent();
   if (exitCode != 106)
      storeSettings(true);
   destroyAll();
   mainClass = null;
   if (rebootOnExit) // set by Vm.exitAndReboot
      rebootDevice();      
   #ifdef ANDROID
   privateExit(exitcode); // exit from the android vm
   #elif defined(WP8)
   appExit();
   #endif
   return exitCode;
}

/*
 * flsobral@tc115
 * Changed loadLibraries to receive the path to be used, instead of always searching in the vm path.
 * This was originally done to find and load the LitebaseLib.tcz, but I remembered we already set
 * an environment variable with the Litebase installation path.
 * Right now this change is sort of pointless (the function is used only once to check the vm path),
 * but I decided to keep it that way because the function looks more useful/reusable that way.
 */
static bool loadLibraries(Context currentContext, CharP path, bool first)
{
   Err err;
   volatile Heap h;
   TCHARPs *files=null,*head;
   char fname[MAX_PATHNAME];
   TCHAR searchPath[MAX_PATHNAME];
   int32 count=0,len;

   h = heapCreate();
   IF_HEAP_ERROR(h)
   {
      heapDestroy(h);
      return false;
   }
#ifdef ANDROID // guich@tc1681 - for Android, search TCZs only inside the apk 
   JNIEnv* env = getJNIEnv();
   jstring jret = (*env)->CallStaticObjectMethod(env, applicationClass, jlistTCZs);
   int jlen = (*env)->GetStringUTFLength(env, jret);
   CharP ret = heapAlloc(h,jlen+1), p = ret;
   jstring2CharP(jret, ret); // tcz files are comma-separated
   (*env)->DeleteLocalRef(env, jret);
   while (1)
   {
      CharP sep = strchr(p,',');
      if (sep)
         *sep = 0;
      if (xstrstr(p,"lib.tcz") || xstrstr(p,"Lib.tcz"))
         if (tczLoad(currentContext, p) == null)
            break;
      if (!sep) // reached end
         break;
      p = sep+1;
   }
   err = jlen > 0 ? NO_ERROR : 1;
#else
   CharP2TCHARPBuf(path, searchPath);
   err = listFiles(searchPath,0,&files,&count,h, LF_RECURSIVE);
   if (err == NO_ERROR && count > 0)
   {
      head = files;
      do
      {
         TCHARP2CharPBuf(files->value, fname);
         len = xstrlen(fname);
         CharPToLower(fname);
         if (strEq(&fname[len-7],"lib.tcz"))
         {
            TCHARP2CharPBuf(files->value, fname); // restore original case
            if (tczLoad(currentContext, fname) == null)
            {
               err = !NO_ERROR; // remove mem leak
               break;
            }
         }
         files = files->next;
      } while (files != head);
   }
#endif   
   heapDestroy(h);
   if (err != NO_ERROR && first)
      alert("Problem when loading TotalCross libraries (%X)",err);

   return err == NO_ERROR;
}

static void checkFullScreenPlatform() // guich@tc120_59
{
   if (*tcSettings.fullScreenPlatformsPtr)
   {
      char plat[100];
      String2CharPBuf(*tcSettings.fullScreenPlatformsPtr, plat);
      if (xstrstr(plat, platform) == null)    // if the platform is not inside, set to false
         *tcSettings.isFullScreenPtr = false;
   }
}

#define ISNOTSIGNED 0
#define ISFREE 1
#define ISFAILED 2
#define ISWILLACTIVATE 3
#define ISACTIVATED 4
#define ISNORAS 5

TC_API int32 startProgram(Context currentContext)
{
   TCClass c;
   int32 retc;
   
   // load libraries must be before checking the activation, because the tcz may have been splitted
   if (!loadLibraries(currentContext, vmPath, true))
      return exitProgram(115);
   
#if defined (WIN32) && !(defined (WINCE) || defined(WP8)) //flsobral@tc115_64: on Win32, automatically load LitebaseLib.tcz if Litebase is installed and allowed.
   {
      TCHAR litebasePath[MAX_PATHNAME];
      if (GetEnvironmentVariable(TEXT("LITEBASE_HOME"), litebasePath, MAX_PATHNAME) != 0)
      {
         tcscat(litebasePath, TEXT("/dist/lib/LitebaseLib.tcz")); //flsobral@tc120_18: fixed path of LitebaseLib.tcz on Win32. Applications should now able to run from anywhere, as long as the Litebase and TotalCross home paths are set.
         tczLoad(currentContext, litebasePath);
      }
   }
#endif
   // 3. Load the main class (also calls its static initializer)
   c = loadClass(currentContext, mainClassName, true); // some fields of totalcross.sys.Settings may be set by the programmer at the static initializer, called now
   if (c == null)
   {
      if (currentContext->thrownException != null)
         showUnhandledException(currentContext,true);
      else
         alert("Class not found or corrupted: %s",mainClassName);
      return exitProgram(116);
   }
   else
   if (currentContext->thrownException == null && keepRunning)
   {
      checkFullScreenPlatform();
      if (*tcSettings.isFullScreenPtr) // Settings.isFullScreen is set at the static initializer
         setFullScreen();
      // 4. Retrieve user settings
      retrieveSettingsChangedAtStaticInitializer(currentContext);
      // 5. create an instance and call the constructor
      mainClass = createObject(currentContext, mainClassName); // keep it locked
   }
   if (currentContext->thrownException == null && mainClass != null) // no unhandled exception was thrown?
   {
      // 6. call appStarting
      Method mainMtd = getMethod(OBJ_CLASS(mainClass), true, "appStarting", 1, J_INT, J_BOOLEAN);
      if (!mainMtd) return exitProgram(117);
      if (isMainWindow) waitUntilStarted();
      executeMethod(currentContext, mainMtd, mainClass, -1);
      // 7. call the main event loop
      if (isMainWindow) mainEventLoop(currentContext); // in the near future, MainClass apps will also receive events.
      // 8. call appEnding
      executeMethod(currentContext, getMethod(OBJ_CLASS(mainClass), true, "appEnding", 0), mainClass);
   }

   return exitProgram(exitCode);
}

// copy next arg that is a path
static CharP nextArgPath(CharP buffer, CharP path)
{
   while (*buffer == ' ')
      buffer++;
   while (*buffer != '\0' && *buffer != ' ')
      *path++ = *buffer++;
   *path = '\0';
   return buffer;
}

void closeDebug();

static void loadExceptionClasses(Context currentContext)
{
   CharP* exceptions = throwableAsCharP;
   int32 n = (int32)ThrowableCount - 1;
   for (exceptions++; --n >= 0; )
      loadClass(currentContext, *exceptions++, false);
   lockClass = loadClass(currentContext, "totalcross.util.concurrent.Lock", false);
}

#if defined(ANDROID) || !defined(ENABLE_TEST_SUITE) // running in android without the test_suite macro defined must skip the test suite
#define ALLOW_TEST_SUITE false
#else
#define ALLOW_TEST_SUITE true
#endif

TC_API int32 startVM(CharP argsOriginal, Context* cOut)
{
   CharP cmdline;
   TCZFile loadedTCZ;
   char tcbase[11];
   char args[256];
   char argsLower[256];
   CharP tczName;
   int32 argsOriginalLen = argsOriginal ? xstrlen(argsOriginal) : 0;
   CharP c;
   Context currentContext;
   TCObject name;

#if defined(WINCE)
 #if _WIN32_WCE >= 300 // splitted because HPC211 must be just ignored.
    if (isWakeUpCall(argsOriginal))
      return 109;
 #endif
#elif defined WIN32
   if (argsOriginalLen > 0 && xstrstr(argsOriginal, "/scr"))
   {
      argsOriginal = parseScreenBounds(argsOriginal, &defScrX, &defScrY, &defScrW, &defScrH);
      if (!argsOriginal)
         return 110;
      argsOriginalLen = xstrlen(argsOriginal);
   }
#endif

   xstrcpy(tcbase, "TCBase.tcz"); // copy to a temp buffer, it may be truncated in tczLoad
   xstrncpy(args, argsOriginal, min32(sizeof(args)-1, argsOriginalLen));
   tczName = args;

   *cOut = currentContext = initAll(&tczName);
   if (currentContext == null)
      return 100;

   if (!tczLoad(currentContext, tcbase))
   {
      alert("TCBase not found or corrupted. Please reinstall TotalCross");
      return 101;
   }
   xstrcpy(tcbase, "TCUI.tcz");
   if (!tczLoad(currentContext, tcbase))
   {
      alert("TCUI not found or corrupted. Please reinstall TotalCross");
      return 103;
   }
   
   initException(); // load exceptions

   xstrcpy(argsLower, args);
   CharPToLower(argsLower);
   if (xstrstr(argsLower, "launcher") && 0) // if executing the default Launcher, instead of creating a launcher for an application, run the testsuite
      xstrcpy(args, " /cmd -testsuite");

   cmdline = xstrstr(args, " /cmd "); // check if there's a cmd line
   if (cmdline) // if yes, split the cmdline and the tczName
   {
      *cmdline = 0;
      cmdline += 6;
   }

   if (cmdline || !*args) // parse the command line
   {
      bool loop = true;
      if (ALLOW_TEST_SUITE && (!*args || xstrstr(cmdline,"-testsuite")))
      {
         #ifdef ENABLE_TEST_SUITE
          initSettings(currentContext, "", null);
          retrieveSettings(currentContext, "TestSuite");
          if (!initGraphicsBeforeSettings(currentContext,0) || !initGraphicsAfterSettings(currentContext))
          {
             alert("Could not start graphics. Out of memory or problem with the fonts?");
             return exitProgram(103);
          }
          waitUntilStarted();
          imageClass = loadClass(currentContext, "totalcross.ui.image.Image", false);
          mainContext->OutOfMemoryErrorObj = createObject(currentContext, "java.lang.OutOfMemoryError"); // now its safe to initialize the OutOfMemoryErrorObj for the main context
          gcContext->OutOfMemoryErrorObj = createObject(currentContext, "java.lang.OutOfMemoryError");
          lifeContext->OutOfMemoryErrorObj   = createObject(currentContext, "java.lang.OutOfMemoryError");
          loadExceptionClasses(currentContext); // guich@tc112_18

          startTestSuite(currentContext); // run the testsuite before Graphics test to be able to test for Font and FontMetrics
          destroyGraphics();
         #else
          alert("The test suite is not\nlinked with the VM!\nExiting...");
         #endif
          return exitProgram(-1);
      }
      c = cmdline;
      if (cmdline) 
      while (loop)
      {
         switch (*cmdline++)
         {
            case 0:
               cmdline--;
               loop = false;
               break;
            case '-': // possible options
            {
               switch (toLower(*cmdline++))
               {
                  case 't':
                  {
                     traceOn = true;
                     goto jumpArgument;
                  }
                  break;
                  case 'p': // required on systems that can't determine the executable directory of the current process
                  {
                     closeDebug(); // close debug file before an appPath change, not terrible :-(
                     cmdline = nextArgPath(cmdline, appPath);
                     goto jumpArgument;
                  }
                  break;
jumpArgument:
                  c = cmdline;
                  default:
                     break;
               }
            }
            case ' ':
            default:
               break;
         }
      }
      while (*c == ' ' && *c != 0)
         c++;
      if (commandLine != null && c != null)
         xstrcpy(commandLine, c);
   }

#if defined(ENABLE_TRACE) && (defined(WINCE) || defined(ANDROID)) && !defined(DEBUG)
   traceOn = true;
#endif

#if defined(darwin) || defined(ANDROID) || defined WP8
   strcat(tczName, ".tcz");
#endif
   mainContext->OutOfMemoryErrorObj = createObject(currentContext, "java.lang.OutOfMemoryError"); // now its safe to initialize the OutOfMemoryErrorObj for the main context
   gcContext->OutOfMemoryErrorObj   = createObject(currentContext, "java.lang.OutOfMemoryError");
   lifeContext->OutOfMemoryErrorObj   = createObject(currentContext, "java.lang.OutOfMemoryError");
   imageClass = loadClass(currentContext, "totalcross.ui.image.Image", false);
   loadExceptionClasses(currentContext); // guich@tc112_18
   voidTYPE    = getStaticFieldObject(currentContext,loadClass(currentContext, "java.lang.Void",      false), "TYPE");
   booleanTYPE = getStaticFieldObject(currentContext,loadClass(currentContext, "java.lang.Boolean",   false), "TYPE");
   byteTYPE    = getStaticFieldObject(currentContext,loadClass(currentContext, "java.lang.Byte",      false), "TYPE");
   shortTYPE   = getStaticFieldObject(currentContext,loadClass(currentContext, "java.lang.Short",     false), "TYPE");
   intTYPE     = getStaticFieldObject(currentContext,loadClass(currentContext, "java.lang.Integer",   false), "TYPE");
   longTYPE    = getStaticFieldObject(currentContext,loadClass(currentContext, "java.lang.Long",      false), "TYPE");
   floatTYPE   = getStaticFieldObject(currentContext,loadClass(currentContext, "java.lang.Float",     false), "TYPE");
   doubleTYPE  = getStaticFieldObject(currentContext,loadClass(currentContext, "java.lang.Double",    false), "TYPE");
   charTYPE    = getStaticFieldObject(currentContext,loadClass(currentContext, "java.lang.Character", false), "TYPE");
   // used in convert
   charConverterPtr = getStaticFieldObject(currentContext, loadClass(currentContext, "totalcross.sys.Convert", false), "charConverter");
   ISO88591CharacterConverter = loadClass(currentContext, "totalcross.sys.CharacterConverter", false);
   UTF8CharacterConverter = loadClass(currentContext, "totalcross.sys.UTF8CharacterConverter", false);

   cloneable = loadClass(currentContext, "java.lang.Cloneable", false); // Loads the Cloneable interface.

   // Create a Java thread for the main context and call it "TC Event Thread"
   mainContext->threadObj = createObjectWithoutCallingDefaultConstructor(currentContext, "java.lang.Thread");
   name = createStringObjectFromCharP(currentContext, "TC Event Thread", -1);
   executeMethod(currentContext, getMethod(OBJ_CLASS(mainContext->threadObj), false, CONSTRUCTOR_NAME, 1, "java.lang.String"), mainContext->threadObj, name);
   setObjectLock(name, UNLOCKED);

   // Create a Java thread for the GC context and call it "Finalizer"
   gcContext->threadObj = createObjectWithoutCallingDefaultConstructor(currentContext, "java.lang.Thread");
   name = createStringObjectFromCharP(currentContext, "Finalizer", -1);
   executeMethod(currentContext, getMethod(OBJ_CLASS(gcContext->threadObj), false, CONSTRUCTOR_NAME, 1, "java.lang.String"), gcContext->threadObj, name);
   setObjectLock(name, UNLOCKED);

   if ((loadedTCZ = tczLoad(currentContext, tczName)) == null)
      return exitProgram(104);
   else
   {
      CharP mainClassName = loadedTCZ->header->names[0];
#if !defined(ANDROID) && !defined (PALMOS) && !defined (darwin) || defined (THEOS) // we load libraries in the application's path too (guich@tc139: all platforms except palm)
      loadLibraries(currentContext, appPath, false);
#endif      
      // 0. Initialize tcSettings structure
      if (!initSettings(currentContext, mainClassName, loadedTCZ))
         return exitProgram(105); // used at exit!
      else
      {
#if defined (WIN32) || (WINCE)
         // 0.5. Only one instance of the application allowed?
         if (!*tcSettings.multipleInstances && checkIfRunning())
            return exitProgram(106);
#endif
         // 1. Initialize the graphics
         isMainWindow = (loadedTCZ->header->attr & ATTR_HAS_MAINWINDOW) != 0;
         if (isMainWindow && !initEvent())
            return exitProgram(102);

         if (isMainWindow && (!initGraphicsBeforeSettings(currentContext,loadedTCZ->header->attr) || !keepRunning))
            return exitProgram(107);
         else
         {
            // 2. Retrieve the settings
            if (!retrieveSettings(currentContext, mainClassName) || !keepRunning) // Settings must always be initialized before the application
               return exitProgram(108); // used at exit!
            else
            if (isMainWindow && (!initGraphicsAfterSettings(currentContext) || !keepRunning))
               return exitProgram(109);
         }
      }
   }
   return 0; // sucessfull startup
}


/*
    Since the tcvm is a library, you must use a loader to call it.
    The loader's name must be the same of the TCZ file that stores the classes.
    Optionally, you may pass as arguments:
      -t trace the program
      -testsuite: runs the test suite and quits.
*/
TC_API int32 executeProgram(CharP argsOriginal)
{
   Context c;
   int32 rc = startVM(argsOriginal, &c);

   if (rc == 0)
      rc = startProgram(c);
   return rc;
}
