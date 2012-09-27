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
#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer
#include <android/log.h>

#define debug(...) ((void)__android_log_print(ANDROID_LOG_INFO, "TotalCross", __VA_ARGS__))

static ANativeWindow *window;
static EGLDisplay _display;
static EGLSurface _surface;
static EGLContext _context;
static void destroyEGL();

#define COORDS_PER_VERTEX 3

int realAppH,appW,appH;

static char* vertexShaderCode(char *buf, int w, int h)
{
   // http://www.songho.ca/opengl/gl_projectionmatrix.html
   xstrprintf(buf, "attribute vec4 a_Position; attribute vec4 a_Color; varying vec4 v_Color;"
      "mat4 projectionMatrix = mat4( 2.0/%d.0, 0.0, 0.0, -1.0,"
                           "0.0, -2.0/%d.0, 0.0, 1.0,"
                           "0.0, 0.0, -1.0, 0.0,"
                           "0.0, 0.0, 0.0, 1.0);"
      "void main() {v_Color = a_Color; gl_Position = a_Position * projectionMatrix;}", w,h); // the matrix must be included as a modifier of gl_Position
   return buf;
}

static char* fragmentShaderCode =
   "precision mediump float;"
   "varying vec4 v_Color;"
   "void main() {gl_FragColor = v_Color;}";

GLuint loadShader(GLenum shaderType, const char* pSource)
{
   GLuint shader = glCreateShader(shaderType);
   if (shader)
   {
      glShaderSource(shader, 1, &pSource, NULL);
      glCompileShader(shader);
   }
   return shader;
}

GLuint gProgram;
GLuint gPositionHandle;
GLuint gColorHandle;
static GLushort rectOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

static bool initGLES();

/*
 * Class:     totalcross_Launcher4A
 * Method:    nativeInitSize
 * Signature: (int,int)V
 */
void JNICALL Java_totalcross_Launcher4A_nativeInitSize(JNIEnv *env, jobject this, jobject surface, jint width, jint height) // called only once
{
   ScreenSurfaceEx ex = screen.extension = newX(ScreenSurfaceEx);
   appW = width;
   appH = height;
//   if (gProgram != 0) glDeleteProgram(gProgram); //   glDeleteShader(shader);

   window = ANativeWindow_fromSurface(env, surface);
   realAppH = (*env)->CallStaticIntMethod(env, applicationClass, jgetHeight);
}

GLfloat ftransp[16], f255[256];
static bool initGLES()
{                
   int32 i;
   char buf[512];
   const EGLint attribs[] =
   {
       EGL_SURFACE_TYPE, EGL_WINDOW_BIT, 
       EGL_BLUE_SIZE, 8,
       EGL_GREEN_SIZE, 8,
       EGL_RED_SIZE, 8,        
       EGL_ALPHA_SIZE, 8,
       EGL_NONE
   };
   EGLint context_attribs[] = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE };
//   EGLint window_attribs[] = {EGL_RENDER_BUFFER, EGL_SINGLE_BUFFER, EGL_NONE };
   EGLDisplay display;
   EGLConfig config;
   EGLint numConfigs;
   EGLint format;
   EGLSurface surface;
   EGLContext context;
   EGLint width;
   EGLint height;
   GLfloat ratio;

   if ((display = eglGetDisplay(EGL_DEFAULT_DISPLAY)) == EGL_NO_DISPLAY)    {debug("eglGetDisplay() returned error %d", eglGetError()); return false;}
   if (!eglInitialize(display, 0, 0))                                       {debug("eglInitialize() returned error %d", eglGetError()); return false;}
   if (!eglChooseConfig(display, attribs, &config, 1, &numConfigs))         {debug("eglChooseConfig() returned error %d", eglGetError()); destroyEGL(); return false;}
   if (!eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format)) {debug("eglGetConfigAttrib() returned error %d", eglGetError()); destroyEGL(); return false;}

   ANativeWindow_setBuffersGeometry(window, 0, 0, format);

   if (!(surface = eglCreateWindowSurface(display, config, window, 0/*window_attribs*/)))     {debug("eglCreateWindowSurface() returned error %d", eglGetError()); destroyEGL(); return false;}
   if (!(context = eglCreateContext(display, config, EGL_NO_CONTEXT, context_attribs))) {debug("eglCreateContext() returned error %d", eglGetError()); destroyEGL(); return false;}
   if (!eglMakeCurrent(display, surface, surface, context))                 {debug("eglMakeCurrent() returned error %d", eglGetError()); destroyEGL(); return false;}
   if (!eglQuerySurface(display, surface, EGL_WIDTH, &width) || !eglQuerySurface(display, surface, EGL_HEIGHT, &height)) {debug("eglQuerySurface() returned error %d", eglGetError()); destroyEGL(); return false;}

   _display = display;
   _surface = surface;
   _context = context;
   appW = width;
   appH = height;

   // program for point, line and rectangle
   gProgram = glCreateProgram();
   glAttachShader(gProgram, loadShader(GL_VERTEX_SHADER, vertexShaderCode(buf, width, height)));
   glAttachShader(gProgram, loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode));
   glLinkProgram(gProgram);
   glUseProgram(gProgram);

   gColorHandle = glGetAttribLocation(gProgram, "a_Color");
   gPositionHandle = glGetAttribLocation(gProgram, "a_Position"); // get handle to vertex shader's vPosition member
   glEnableVertexAttribArray(gColorHandle); // Enable a handle to the colors - since this is the only one used, keep it enabled all the time
   glEnableVertexAttribArray(gPositionHandle); // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time

   glPixelStorei(GL_PACK_ALIGNMENT, 1);
   glPixelStorei(GL_UNPACK_ALIGNMENT, 1); 

   glViewport(0, 0, width, height);
   //glEnable(GL_SCISSOR_TEST);
   glDisable(GL_CULL_FACE);
   glDisable(GL_DEPTH_TEST);
   glEnable(GL_BLEND); // enable color alpha channel
   glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

   for (i = 0; i <= 15; i++)
      ftransp[i] = (GLfloat)((i<<4)|0xF) / (GLfloat)255;
   for (i = 0; i <= 255; i++)
      f255[i] = (GLfloat)i/(GLfloat)255;

   return true;
}

static void destroyEGL()
{
    eglMakeCurrent(_display, EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT);
    eglDestroyContext(_display, _context);
    eglDestroySurface(_display, _surface);
    eglTerminate(_display);

    _display = EGL_NO_DISPLAY;
    _surface = EGL_NO_SURFACE;
    _context = EGL_NO_CONTEXT;

    return;
}


/*
 * Class:     totalcross_Launcher4A
 * Method:    nativeOnSizeChanged
 * Signature: (II)V
 */


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
   return initGLES();
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
   screen->pitch = screen->screenW * screen->bpp / 8;
   screen->pixels = xmalloc(screen->screenW * screen->screenH * 4);
   return screen->pixels != null;
}

void graphicsUpdateScreen(Context currentContext, ScreenSurface screen, int32 transitionEffect)
{                         
   eglSwapBuffers(_display, _surface);
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
   if (!isScreenChange)
   {
      destroyEGL();
      xfree(screen->extension);
   }
}

////////////////////////////// OPEN GL 2 //////////////////////////////////
struct{ GLubyte r, g, b, a; } glpixel;

int32 glGetPixel(int32 x, int32 y)
{  
   glReadPixels(x, appH-y-1, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, &glpixel);
   return (((int32)glpixel.r) << 16) | (((int32)glpixel.g) << 8) | (int32)glpixel.b;
}
void glDrawPixels(Context c, int32 n)
{
   glVertexAttribPointer(gColorHandle, 4 * n, GL_FLOAT, GL_FALSE, 4 * sizeof(float), c->glcolors);
   glVertexAttribPointer(gPositionHandle, COORDS_PER_VERTEX * n, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), c->glcoords);
   glDrawArrays(GL_POINTS, 0,n);
}

void glDrawPixel(Context c, int32 x, int32 y, int32 rgb)
{
   GLfloat* coords = c->glcoords;
   GLfloat* colors = c->glcolors;
   PixelConv pc;
   pc.pixel = rgb;
   coords[0] = x;
   coords[1] = y;

   colors[0] = f255[pc.r];
   colors[1] = f255[pc.g];
   colors[2] = f255[pc.b];
   colors[3] = 1;
   glVertexAttribPointer(gColorHandle, 4, GL_FLOAT, GL_FALSE, 4 * sizeof(float), colors);
   glVertexAttribPointer(gPositionHandle, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords); // Prepare the triangle coordinate data
   glDrawArrays(GL_POINTS, 0,1);
}

void glDrawLine(Context c, int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb)
{           
   GLfloat* coords = c->glcoords;
   GLfloat* colors = c->glcolors;
   PixelConv pc;
   pc.pixel = rgb;
   coords[0] = x1;
   coords[1] = y1;
   coords[3] = x2;
   coords[4] = y2;

   colors[0] = colors[4] = f255[pc.r];
   colors[1] = colors[5] = f255[pc.g];
   colors[2] = colors[6] = f255[pc.b];
   colors[3] = colors[7] = 1;
   glVertexAttribPointer(gColorHandle, 4*2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), colors);
   glVertexAttribPointer(gPositionHandle, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords); // Prepare the triangle coordinate data
   glDrawArrays(GL_LINES, 0,2);
}

void glFillRect(Context c, int32 x, int32 y, int32 w, int32 h, int32 rgb)
{
   GLfloat* coords = c->glcoords;
   GLfloat* colors = c->glcolors;
   PixelConv pc;
   pc.pixel = rgb;
   coords[0] = x;
   coords[1] = y;
   coords[3] = x;
   coords[4] = y+h;
   coords[6] = x+w;
   coords[7] = y+h;
   coords[9] = x+w;
   coords[10] = y;

   colors[0] = colors[4] = colors[8]  = colors[12] = colors[16] = colors[20] = f255[pc.r];
   colors[1] = colors[5] = colors[9]  = colors[13] = colors[17] = colors[21] = f255[pc.g];
   colors[2] = colors[6] = colors[10] = colors[14] = colors[18] = colors[22] = f255[pc.b];
   colors[3] = colors[7] = colors[11] = colors[15] = colors[19] = colors[23] = 1;
   glVertexAttribPointer(gColorHandle, 4*6, GL_FLOAT, GL_FALSE, 4 * sizeof(float), colors);
   glVertexAttribPointer(gPositionHandle, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords); // Prepare the triangle coordinate data
   glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, rectOrder); // GL_LINES, GL_TRIANGLES, GL_POINTS
}
