/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *********************************************************************************/



#include "gfx_ex.h"

int realAppH,appW,appH;

static char* vertexShaderCode(char *buf, int w, int h)
{
   // http://www.songho.ca/opengl/gl_projectionmatrix.html
   xstrprintf(buf, "attribute vec4 vPosition;" 
      "mat4 projectionMatrix = mat4( 2.0/%d.0, 0.0, 0.0, -1.0,"
                           "0.0, -2.0/%d.0, 0.0, 1.0,"
                           "0.0, 0.0, -1.0, 0.0,"
                           "0.0, 0.0, 0.0, 1.0);"
      "void main() {gl_Position = vPosition*projectionMatrix;}", w,h); // the matrix must be included as a modifier of gl_Position
   return buf;
}

static char* fragmentShaderCode =
   "precision mediump float;" 
   "uniform vec4 vColor;" 
   "void main() {gl_FragColor = vColor;}";

GLuint loadShader(GLenum shaderType, const char* pSource) 
{
   GLuint shader = glCreateShader(shaderType);
   if (shader) 
   {
      glShaderSource(shader, 1, &pSource, NULL);
      glCompileShader(shader);
   }
   alert("shader: %d",shader);
   return shader;
}

GLuint gProgram;
GLuint gPositionHandle;
GLuint gColorHandle;

/*
 * Class:     totalcross_Launcher4A
 * Method:    nativeInitSize
 * Signature: (int,int)V
 */
void JNICALL Java_totalcross_Launcher4A_nativeInitSize(JNIEnv *env, jobject this, jint width, jint height) // called only once
{
   char buf[512];
      ScreenSurfaceEx ex = screen.extension = newX(ScreenSurfaceEx);
//   glDeleteProgram(program);
//   glDeleteShader(shader);
   
   gProgram = glCreateProgram();
   glAttachShader(gProgram, loadShader(GL_VERTEX_SHADER, vertexShaderCode(buf, width, height)));
   glAttachShader(gProgram, loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode));
   glLinkProgram(gProgram);
   glUseProgram(gProgram);
   
   alert("@@@@@@@@@ gProgram: %d",gProgram);

   gColorHandle = glGetUniformLocation(gProgram, "vColor");
   alert("@@@@@@@@@ 1");
   gPositionHandle = glGetAttribLocation(gProgram, "vPosition"); // get handle to vertex shader's vPosition member
   glEnableVertexAttribArray(gPositionHandle); // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time
   alert("@@@@@@@@@ 2");

   glViewport(0, 0, width, height);
   alert("@@@@@@@@@ 3");
   glEnable(GL_BLEND); // enable color alpha channel
   alert("@@@@@@@@@ 4");
   glBlendFunc(GL_SRC_ALPHA, GL_DST_ALPHA);
   alert("@@@@@@@@@ 5");
   glEnable(GL_SCISSOR_TEST);
   alert("@@@@@@@@@ 6");

   realAppH = (*env)->CallStaticIntMethod(env, applicationClass, jgetHeight);
   alert("@@@@@@@@@ 7: %d",realAppH);
}

/*
 * Class:     totalcross_Launcher4A
 * Method:    nativeOnSizeChanged
 * Signature: (II)V
 */

void callExecuteProgram(); // on android/startup_c.h

void privateScreenChange(int32 w, int32 h)
{
   UNUSED(w)
   UNUSED(h)
}

bool graphicsStartup(ScreenSurface screen, int16 appTczAttr)
{
   screen->bpp = ANDROID_BPP;
   screen->screenX = screen->screenY = 0;
   screen->screenW = lastW;
   screen->screenH = lastH;
   screen->hRes = ascrHRes;
   screen->vRes = ascrVRes;
   return true;
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
   screen->pitch = screen->screenW * screen->bpp / 8;
   return screen->pixels != null;
}

void graphicsUpdateScreen(Context currentContext, ScreenSurface screen, int32 transitionEffect)
{
   JNIEnv *env = getJNIEnv();
   if (env)
      (*env)->CallStaticVoidMethod(env, applicationClass, jupdateScreen, currentContext->dirtyX1,currentContext->dirtyY1,currentContext->dirtyX2,currentContext->dirtyY2,transitionEffect); // will call Java_totalcross_Launcher4A_nativeOnDraw
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
   if (!isScreenChange)
      xfree(screen->extension);
}

