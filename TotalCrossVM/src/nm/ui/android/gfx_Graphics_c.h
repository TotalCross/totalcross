
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
#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer
#include <android/log.h>

#define debug(...) ((void)__android_log_print(ANDROID_LOG_INFO, "TotalCross", __VA_ARGS__))

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

static ANativeWindow *window;
static EGLDisplay _display;
static EGLSurface _surface;
static EGLContext _context;
static void destroyEGL();

#define COORDS_PER_VERTEX 3

int realAppH,appW,appH;
GLfloat ftransp[16], f255[256];

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
      "void main() {v_Color = a_Color; gl_Position = a_Position * projectionMatrix;}"

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
      "void main() {gl_Position = a_Position*projectionMatrix;}"

#define LRP_FRAGMENT_CODE \
      "precision mediump float;" \
      "uniform vec4 a_Color;" \
      "void main() {gl_FragColor = a_Color;}"

static GLuint lrpProgram;
static GLuint lrpPosition;
static GLuint lrpColor;
static GLushort rectOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

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
static void setCurrentProgram(GLint prog)
{
   if (prog != lastProg)
      glUseProgram(lastProg = prog);
}

static GLuint createProgram(char* vertexCode, char* fragmentCode)
{
   GLint ret;
   GLuint p = glCreateProgram();
   glAttachShader(p, loadShader(GL_VERTEX_SHADER, vertexCode));
   glAttachShader(p, loadShader(GL_FRAGMENT_SHADER, fragmentCode));
   glLinkProgram(p);
   glValidateProgram(p);
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

   window = ANativeWindow_fromSurface(env, surface);
   realAppH = (*env)->CallStaticIntMethod(env, applicationClass, jgetHeight);
}

static void initPoints()
{  
   pointsProgram = createProgram(POINTS_VERTEX_CODE, POINTS_FRAGMENT_CODE);
   setCurrentProgram(lrpProgram);
   pointsColor = glGetAttribLocation(pointsProgram, "a_Color");
   pointsPosition = glGetAttribLocation(pointsProgram, "a_Position"); // get handle to vertex shader's vPosition member
   glEnableVertexAttribArray(pointsColor); // Enable a handle to the colors - since this is the only one used, keep it enabled all the time
   glEnableVertexAttribArray(pointsPosition); // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time
}

void glDrawPixels(Context c, int32 n)
{
   setCurrentProgram(pointsProgram);
   glVertexAttribPointer(pointsColor, 4, GL_FLOAT, GL_FALSE, 4 * sizeof(float), c->glcolors);
   glVertexAttribPointer(pointsPosition, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), c->glcoords);
   glDrawArrays(GL_POINTS, 0,n);
}

static GLfloat textureVerts[8] = { 0, 1,  1, 1,  1, 0,  0, 0 };
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

void glDrawTexture(Context c, int32 textureId, int32 x, int32 y, int32 w, int32 h)
{
   GLfloat* coords = c->glcoords;
   setCurrentProgram(textureProgram);

   coords[0] = coords[6] = x;
   coords[1] = coords[3] = y+h;
   coords[2] = coords[4] = x+w;
   coords[5] = coords[7] = y;
   glBindTexture(GL_TEXTURE_2D, textureId);
   glVertexAttribPointer(texturePoint, 2, GL_FLOAT, false, 0, coords);
   glVertexAttribPointer(textureCoord, 2, GL_FLOAT, false, 0, textureVerts);
   glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
   glBindTexture(GL_TEXTURE_2D, 0);
   coords[2] = coords[5] = 0; 
}

void initLineRectPoint()
{ 
   lrpProgram = createProgram(LRP_VERTEX_CODE, LRP_FRAGMENT_CODE);
   setCurrentProgram(lrpProgram);
   lrpColor = glGetUniformLocation(lrpProgram, "a_Color");
   lrpPosition = glGetAttribLocation(lrpProgram, "a_Position"); // get handle to vertex shader's vPosition member
   glEnableVertexAttribArray(lrpPosition); // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time
}

void glDrawPixel(Context c, int32 x, int32 y, int32 rgb)
{
   GLfloat* coords = c->glcoords;
   PixelConv pc;
   pc.pixel = rgb;
   coords[0] = x;
   coords[1] = y;

   setCurrentProgram(lrpProgram);
   glUniform4f(lrpColor, f255[pc.r], f255[pc.g], f255[pc.b], 1); // Set color for drawing the line - nopt to cache color
   glVertexAttribPointer(lrpPosition, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords); // Prepare the triangle coordinate data
   glDrawArrays(GL_POINTS, 0,1);
}

void glDrawLine(Context c, int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb)
{
   GLfloat* coords = c->glcoords;
   PixelConv pc;
   pc.pixel = rgb;
   coords[0] = x1;
   coords[1] = y1;
   coords[3] = x2;
   coords[4] = y2;

   setCurrentProgram(lrpProgram);
   glUniform4f(lrpColor, f255[pc.r], f255[pc.g], f255[pc.b], 1); // Set color for drawing the line
   glVertexAttribPointer(lrpPosition, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords); // Prepare the triangle coordinate data
   glDrawArrays(GL_LINES, 0,2);
}

void glFillRect(Context c, int32 x, int32 y, int32 w, int32 h, int32 rgb)
{
   GLfloat* coords = c->glcoords;
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

   setCurrentProgram(lrpProgram);
   glUniform4f(lrpColor, f255[pc.r], f255[pc.g], f255[pc.b], 1);
   glVertexAttribPointer(lrpPosition, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords);
   glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, rectOrder);
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
   glReadPixels(x, appH-y-1, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, &glpixel);
   return (((int32)glpixel.r) << 16) | (((int32)glpixel.g) << 8) | (int32)glpixel.b;
}

/////////////////////////////////////////////////////////////////////////

static bool initGLES()
{
   int32 i;
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

   initTexture();
   initLineRectPoint();
   initPoints();
   setProjectionMatrix(appW,appH);

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
}


/*
 * Class:     totalcross_Launcher4A
 * Method:    nativeOnSizeChanged
 * Signature: (II)V
 */


void privateScreenChange(int32 w, int32 h)
{
   setProjectionMatrix(w,h);
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

