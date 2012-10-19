
/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 * *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 * *
 *********************************************************************************/

#include "gfx_ex.h"

#ifdef ANDROID
#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer
#include <android/log.h>
#define debug(...) ((void)__android_log_print(ANDROID_LOG_INFO, "TotalCross", __VA_ARGS__))
#endif

static void checkGlError(const char* op)
{
   GLint error;
   int c=0;
   for (error = glGetError(); error; error = glGetError())
   {
      char* msg = "???";
      switch (error)
      {
         case GL_INVALID_ENUM     : msg = "INVALID ENUM"; break;
         case GL_INVALID_VALUE    : msg = "INVALID VALUE"; break;
         case GL_INVALID_OPERATION: msg = "INVALID OPERATION"; break;
         case GL_OUT_OF_MEMORY    : msg = "OUT OF MEMORY"; break;
      }

      debug("after %s() glError %s\n", op, msg);
      c++;
   }
//   if (!c) debug("after %s() NO ERROR",op);
}

#ifdef ANDROID
static ANativeWindow *window;
static EGLDisplay _display;
static EGLSurface _surface;
static EGLContext _context;
#endif
static void destroyEGL();


#define COORDS_PER_VERTEX 3

int realAppH,appW,appH;
GLfloat ftransp[16], f255[256];
int32 flen;
GLfloat* glcoords;//[flen*3];
GLfloat* glcolors;//[flen*4];
static GLfloat texcoords[16], lrcoords[12];
static GLfloat *pixcoords, *pixEnd;

// http://www.songho.ca/opengl/gl_projectionmatrix.html
//////////// texture
#define TEXTURE_VERTEX_CODE  \
      "attribute vec4 vertexPoint;" \
      "attribute vec2 aTextureCoord;" \
      "uniform mat4 projectionMatrix; " \
      "varying vec2 vTextureCoord;" \
      "void main()" \
      "{" \
      "    gl_Position = vertexPoint * projectionMatrix;" \
      "    vTextureCoord = aTextureCoord;" \
      "}"

#define TEXTURE_FRAGMENT_CODE \
      "precision mediump float;" \
      "varying vec2 vTextureCoord;" \
      "uniform sampler2D sTexture;" \
      "void main() {gl_FragColor = texture2D(sTexture, vTextureCoord);}"

static GLuint textureProgram;
static GLuint texturePoint;
static GLuint textureCoord,textureS;

//////////// points (text)

#define POINTS_VERTEX_CODE \
      "attribute vec4 a_Position; attribute vec4 a_Color; varying vec4 v_Color; " \
      "uniform mat4 projectionMatrix; " \
      "void main() {gl_PointSize = 1.0; v_Color = a_Color; gl_Position = a_Position * projectionMatrix;}"

#define POINTS_FRAGMENT_CODE \
      "precision mediump float;" \
      "varying vec4 v_Color;" \
      "void main() {gl_FragColor = v_Color;}"

static GLuint pointsProgram;
static GLuint pointsPosition;
static GLuint pointsColor;

///////////// line, rect, point

#define LRP_VERTEX_CODE \
      "attribute vec4 a_Position;" \
      "uniform mat4 projectionMatrix;" \
      "void main() {gl_PointSize = 1.0; gl_Position = a_Position*projectionMatrix;}"

#define LRP_FRAGMENT_CODE \
      "precision mediump float;" \
      "uniform vec4 a_Color;" \
      "void main() {gl_FragColor = a_Color;}"

static GLuint lrpProgram;
static GLuint lrpPosition;
static GLuint lrpColor;
static GLubyte rectOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

GLuint loadShader(GLenum shaderType, const char* pSource)
{
   GLint ret=1;
   GLuint shader = glCreateShader(shaderType);
   glShaderSource(shader, 1, &pSource, NULL);
   glCompileShader(shader);

   glGetShaderiv(shader, GL_COMPILE_STATUS, &ret);
   if(!ret)
   {
      glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &ret);
      GLchar buffer[ret];
      glGetShaderInfoLog(shader, ret, &ret, buffer);
      debug("Shader compiler error: %s",buffer);
   }
   return shader;
}

static GLint lastProg=-1;
static int32 lastRGB=-1;
static void setCurrentProgram(GLint prog)
{
   if (prog != lastProg)
   {
      lastRGB = -1;
      glUseProgram(lastProg = prog);
   }
}

static GLuint createProgram(char* vertexCode, char* fragmentCode)
{
   GLint ret;
   GLuint p = glCreateProgram();
   glAttachShader(p, loadShader(GL_VERTEX_SHADER, vertexCode));
   glAttachShader(p, loadShader(GL_FRAGMENT_SHADER, fragmentCode));
   glLinkProgram(p);
   //glValidateProgram(p);
   glGetProgramiv(p, GL_LINK_STATUS, &ret);
   if (!ret)
   {
      glGetProgramiv(p, GL_INFO_LOG_LENGTH, &ret);
      GLchar buffer[ret];
      glGetProgramInfoLog(p, ret, &ret, buffer);
      debug("Link error: %s",buffer);
   }
   return p;
}

bool initGLES(ScreenSurface screen); // in iOS, implemented in mainview.m

#ifdef ANDROID
void JNICALL Java_totalcross_Launcher4A_nativeInitSize(JNIEnv *env, jobject this, jobject surface, jint width, jint height) // called only once
{
   ScreenSurfaceEx ex = screen.extension = newX(ScreenSurfaceEx);
   appW = width;
   appH = height;

   window = ANativeWindow_fromSurface(env, surface);
   realAppH = (*env)->CallStaticIntMethod(env, applicationClass, jgetHeight);
}
#endif

static void initPoints()
{
   pointsProgram = createProgram(POINTS_VERTEX_CODE, POINTS_FRAGMENT_CODE);
   setCurrentProgram(lrpProgram);
   pointsColor = glGetAttribLocation(pointsProgram, "a_Color");
   pointsPosition = glGetAttribLocation(pointsProgram, "a_Position"); // get handle to vertex shader's vPosition member
   glEnableVertexAttribArray(pointsColor); // Enable a handle to the colors - since this is the only one used, keep it enabled all the time
   glEnableVertexAttribArray(pointsPosition); // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time
}

void glDrawPixels(int32 n)
{

   setCurrentProgram(pointsProgram);
   glVertexAttribPointer(pointsColor, 4, GL_FLOAT, GL_FALSE, 4 * sizeof(float), glcolors);
   glVertexAttribPointer(pointsPosition, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), glcoords);
   glDrawArrays(GL_POINTS, 0,n);
}

void initTexture()
{
   textureProgram = createProgram(TEXTURE_VERTEX_CODE, TEXTURE_FRAGMENT_CODE);
   setCurrentProgram(textureProgram);
   textureS     = glGetUniformLocation(textureProgram, "sTexture");
   texturePoint = glGetAttribLocation(textureProgram, "vertexPoint");
   textureCoord = glGetAttribLocation(textureProgram, "aTextureCoord");

   glEnableVertexAttribArray(textureCoord);
   glEnableVertexAttribArray(texturePoint);
}

void glLoadTexture(int32* textureId, Pixel *pixels, int32 width, int32 height)
{
   int32 i;
   PixelConv* pf = (PixelConv*)pixels;
   PixelConv* pt = (PixelConv*)xmalloc(width*height*4), *pt0 = pt;
   bool textureAlreadyCreated = *textureId != 0;
   if (!pt)
      return;

   if (!textureAlreadyCreated)
      glGenTextures(1, textureId);
   // OpenGL ES provides support for non-power-of-two textures, provided that the s and t wrap modes are both GL_CLAMP_TO_EDGE.
   glBindTexture(GL_TEXTURE_2D, *textureId);
   if (!textureAlreadyCreated)
   {
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
      glUniform1i(textureS, 0);
   }
   // must invert the pixels from ARGB to RGBA
   for (i = width*height; --i >= 0;pt++,pf++) {pt->a = pf->r; pt->b = pf->g; pt->g = pf->b; pt->r = pf->a;}
   if (textureAlreadyCreated)
      glTexSubImage2D(GL_TEXTURE_2D, 0,0,0,width,height, GL_RGBA,GL_UNSIGNED_BYTE, pt0);
   else
      glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA,GL_UNSIGNED_BYTE, pt0);
   glBindTexture(GL_TEXTURE_2D, 0);
   xfree(pt0);
}

void glDeleteTexture(int32* textureId)
{
   glDeleteTextures(1,textureId);
   *textureId = 0;
}

void glDrawTexture(int32 textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 imgW, int32 imgH)
{
   GLfloat* coords = texcoords;
   setCurrentProgram(textureProgram);
   glBindTexture(GL_TEXTURE_2D, textureId);

   // destination coordinates
   coords[0] = coords[6] = dstX;
   coords[1] = coords[3] = dstY+h;
   coords[2] = coords[4] = dstX+w;
   coords[5] = coords[7] = dstY;
   glVertexAttribPointer(texturePoint, 2, GL_FLOAT, false, 0, coords);

   // source coordinates
   GLfloat left = (float)x/(float)imgW,top=(float)y/(float)imgH,right=(float)(x+w)/(float)imgW,bottom=(float)(y+h)/(float)imgH; // 0,0,1,1
   coords[ 8] = coords[14] = left;
   coords[ 9] = coords[11] = bottom;
   coords[10] = coords[12] = right;
   coords[13] = coords[15] = top;
   glVertexAttribPointer(textureCoord, 2, GL_FLOAT, false, 0, &coords[8]);

   glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
   glBindTexture(GL_TEXTURE_2D, 0);
}

void initLineRectPoint()
{
   lrpProgram = createProgram(LRP_VERTEX_CODE, LRP_FRAGMENT_CODE);
   setCurrentProgram(lrpProgram);
   lrpColor = glGetUniformLocation(lrpProgram, "a_Color");
   lrpPosition = glGetAttribLocation(lrpProgram, "a_Position"); // get handle to vertex shader's vPosition member
   glEnableVertexAttribArray(lrpPosition); // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time
}

void flushPixels()
{
   if (pixcoords != glcoords)
   {
      int n = (pixcoords-glcoords)/3,i;
      PixelConv pc;
      GLfloat* coords = lrcoords;
      setCurrentProgram(lrpProgram);
      pixcoords = glcoords;
      glVertexAttribPointer(lrpPosition, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords);
      int32 lastRGBA = ~*((int32*)&pixcoords[2]);
      for (i = 0; i < n; i++, pixcoords += 3)
      {
         int32 x = pixcoords[0];
         int32 y = pixcoords[1];
         int32 rgba = *((int32*)&pixcoords[2]);
         coords[0] = coords[3]  = x;
         coords[1] = coords[10] = y;
         coords[4] = coords[7]  = y+1;
         coords[6] = coords[9]  = x+1;

         if (lastRGBA != rgba) // prevent color change = performance x2 in galaxy tab2
         {
            pc.pixel = lastRGBA = rgba;
            glUniform4f(lrpColor, f255[pc.r],f255[pc.g],f255[pc.b],f255[pc.a]);
         }
         pixcoords[2] = 0;
         glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, rectOrder);
      }
      pixcoords = glcoords;
   }
}

void glDrawPixelA(int32 x, int32 y, int32 rgb, int32 a)
{
   PixelConv pc;
   pc.pixel = rgb;
   pc.a = a;
   pixcoords[0] = x;
   pixcoords[1] = y;
   *((int32*)&pixcoords[2]) = pc.pixel;
   pixcoords += 3;
   if (pixcoords == pixEnd)
      flushPixels();
}

void glDrawPixel(int32 x, int32 y, int32 rgb)
{
   PixelConv pc;
   pc.pixel = rgb;
   pc.a = 0xFF;
   pixcoords[0] = x;
   pixcoords[1] = y;
   *((int32*)&pixcoords[2]) = pc.pixel;
   pixcoords += 3;
   if (pixcoords == pixEnd)
      flushPixels();
}

void glDrawLine(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb)
{
   // The Samsung Galaxy Tab 2 (4.0.4) has a bug in opengl for drawing horizontal/vertical lines: it draws at wrong coordinates, and incomplete sometimes. so we use fillrect, which always work
   if (x1 == x2)
      glFillRect(min32(x1,x2),min32(y1,y2),1,abs32(y2-y1),rgb);
   else
   if (y1 == y2)
      glFillRect(min32(x1,x2),min32(y1,y2),abs32(x2-x1),1,rgb);
   else
   {
      GLfloat* coords = lrcoords;
      PixelConv pc;
      pc.pixel = rgb;
      coords[0] = x1;
      coords[1] = y1;
      coords[3] = x2;
      coords[4] = y2;

      setCurrentProgram(lrpProgram);

      if (lastRGB != rgb)
      {
         glUniform4f(lrpColor, f255[pc.r], f255[pc.g], f255[pc.b], 1); // Set color for drawing the line
         lastRGB = rgb;
      }
      glVertexAttribPointer(lrpPosition, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords); // Prepare the triangle coordinate data
      glDrawArrays(GL_LINES, 0,2);
   }
}

void glFillRect(int32 x, int32 y, int32 w, int32 h, int32 rgb)
{
   GLfloat* coords = lrcoords;
   PixelConv pc;
   pc.pixel = rgb;
   coords[0] = coords[3]  = x;
   coords[1] = coords[10] = y;
   coords[4] = coords[7]  = y+h;
   coords[6] = coords[9]  = x+w;

   setCurrentProgram(lrpProgram);
   if (lastRGB != rgb)
   {
      glUniform4f(lrpColor, f255[pc.r], f255[pc.g], f255[pc.b], 1); // Set color for drawing the line
      lastRGB = rgb;
   }
   glVertexAttribPointer(lrpPosition, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords);
   glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, rectOrder);
}

static void setProjectionMatrix(GLfloat w, GLfloat h)
{
   GLfloat mat[16] =
   {
      2.0/w, 0.0, 0.0, -1.0,
      0.0, -2.0/h, 0.0, 1.0,
      0.0, 0.0, -1.0, 0.0,
      0.0, 0.0, 0.0, 1.0
   };
   setCurrentProgram(textureProgram); glUniformMatrix4fv(glGetUniformLocation(textureProgram, "projectionMatrix"), 1, 0, mat);
   setCurrentProgram(lrpProgram);     glUniformMatrix4fv(glGetUniformLocation(lrpProgram    , "projectionMatrix"), 1, 0, mat);
   setCurrentProgram(pointsProgram);  glUniformMatrix4fv(glGetUniformLocation(pointsProgram , "projectionMatrix"), 1, 0, mat);
}


struct{ GLubyte r, g, b, a; } glpixel;

int32 glGetPixel(int32 x, int32 y)
{
   flushPixels();
   glReadPixels(x, appH-y-1, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, &glpixel);
   return (((int32)glpixel.r) << 16) | (((int32)glpixel.g) << 8) | (int32)glpixel.b;
}

/////////////////////////////////////////////////////////////////////////
bool checkGLfloatBuffer(Context c, int32 n)
{
   if (n > flen)
   {
      xfree(glcoords);
      xfree(glcolors);
      flen = n*3/2;
      pixcoords = glcoords = (GLfloat*)xmalloc(sizeof(GLfloat)*flen*3);
      glcolors = (GLfloat*)xmalloc(sizeof(GLfloat)*flen*4);
      pixEnd = pixcoords + flen*3;
      if (!glcoords || !glcolors)
      {
         throwException(c, OutOfMemoryError, "Cannot allocate buffer for drawPixels");
         xfree(glcoords);
         xfree(glcolors);
         flen = 0;
         return false;
      }
   }
   return true;
}

bool setupGL(int width, int height)
{
    int i;
    appW = width;
    appH = height;

    initTexture();
    initLineRectPoint();
    initPoints();
    setProjectionMatrix(appW,appH);

    glPixelStorei(GL_PACK_ALIGNMENT, 1);
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

    glDisable(GL_CULL_FACE);
    glDisable(GL_DEPTH_TEST);
    glEnable(GL_BLEND); // enable color alpha channel
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

    for (i = 0; i <= 15; i++)
        ftransp[i] = (GLfloat)((i<<4)|0xF) / (GLfloat)255;
    for (i = 0; i <= 255; i++)
        f255[i] = (GLfloat)i/(GLfloat)255;

    glViewport(0, 0, width, height);

    return checkGLfloatBuffer(mainContext,1000);
}

#ifdef ANDROID
bool initGLES(ScreenSurface screen)
{
   int32 i;
   const EGLint attribs[] =
   {
       EGL_SURFACE_TYPE, EGL_WINDOW_BIT,
       EGL_BLUE_SIZE, 8,
       EGL_GREEN_SIZE, 8,
       EGL_RED_SIZE, 8,
       EGL_ALPHA_SIZE, 8,
       EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
       EGL_NONE
   };
   EGLint context_attribs[] = { EGL_CONTEXT_CLIENT_VERSION, 2, EGL_NONE };
   EGLDisplay display;
   EGLConfig config;
   EGLint numConfigs;
   EGLint format;
   EGLSurface surface;
   EGLContext context;
   EGLint width;
   EGLint height;

   if ((display = eglGetDisplay(EGL_DEFAULT_DISPLAY)) == EGL_NO_DISPLAY)    {debug("eglGetDisplay() returned error %d", eglGetError()); return false;}
   if (!eglInitialize(display, 0, 0))                                       {debug("eglInitialize() returned error %d", eglGetError()); return false;}
   if (!eglChooseConfig(display, attribs, &config, 1, &numConfigs))         {debug("eglChooseConfig() returned error %d", eglGetError()); destroyEGL(); return false;}
   if (!eglGetConfigAttrib(display, config, EGL_NATIVE_VISUAL_ID, &format)) {debug("eglGetConfigAttrib() returned error %d", eglGetError()); destroyEGL(); return false;}

   ANativeWindow_setBuffersGeometry(window, 0, 0, format);

   if (!(surface = eglCreateWindowSurface(display, config, window, 0)))     {debug("eglCreateWindowSurface() returned error %d", eglGetError()); destroyEGL(); return false;}
   if (!(context = eglCreateContext(display, config, EGL_NO_CONTEXT, context_attribs))) {debug("eglCreateContext() returned error %d", eglGetError()); destroyEGL(); return false;}
   if (!eglMakeCurrent(display, surface, surface, context))                 {debug("eglMakeCurrent() returned error %d", eglGetError()); destroyEGL(); return false;}
   if (!eglQuerySurface(display, surface, EGL_WIDTH, &width) || !eglQuerySurface(display, surface, EGL_HEIGHT, &height)) {debug("eglQuerySurface() returned error %d", eglGetError()); destroyEGL(); return false;}

   _display = display;
   _surface = surface;
   _context = context;
   return setupGL(width,height);
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
   xfree(glcoords);
   xfree(glcolors);
}
#endif

void privateScreenChange(int32 w, int32 h)
{

   setProjectionMatrix(w,h);
}

bool graphicsStartup(ScreenSurface screen, int16 appTczAttr)
{
   screen->bpp = 32;
   screen->screenX = screen->screenY = 0;
   screen->screenW = lastW;
   screen->screenH = lastH;
   screen->hRes = ascrHRes;
   screen->vRes = ascrVRes;
   return initGLES(screen);
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
#ifndef ANDROID
   screen->extension = deviceCtx;
#endif
   screen->pitch = screen->screenW * screen->bpp / 8;
   screen->pixels = xmalloc(screen->screenW * screen->screenH * 4);
   return screen->pixels != null;
}

void graphicsUpdateScreenIOS(ScreenSurface screen, int32 transitionEffect);
void graphicsUpdateScreen(Context currentContext, ScreenSurface screen, int32 transitionEffect)
{
   flushPixels();
#ifdef ANDROID
   eglSwapBuffers(_display, _surface);
#else
   graphicsUpdateScreenIOS(screen, transitionEffect);
#endif
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
#ifdef ANDROID
   if (!isScreenChange)
   {
       destroyEGL();
      xfree(screen->extension);
   }
#else
   if (isScreenChange)
       screen->extension = NULL;
   else
   {
      if (screen->extension)
          free(screen->extension);
      deviceCtx = screen->extension = NULL;
   }
#endif
}
