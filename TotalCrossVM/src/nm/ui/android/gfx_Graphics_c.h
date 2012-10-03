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

static void checkGlError(const char* op) 
{
   GLint error;
   for (error = glGetError(); error; error = glGetError()) 
      if (op)
      debug("after %s() glError (0x%x)\n", op, error);
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
      "uniform vec4 uTextureColor;" \
      "attribute vec4 vertexPoint;" \
      "attribute vec2 aTextureCoord;" \
      "uniform mat4 projectionMatrix; " \
      "varying vec2 vTextureCoord;" \
      "varying vec4 vTextureColor;" \
      "void main()" \
      "{" \
      "    gl_Position = vertexPoint * projectionMatrix;" \
      "    vTextureCoord = aTextureCoord;" \
      "    vTextureColor = uTextureColor;" \
      "}"                         
     
#define TEXTURE_FRAGMENT_CODE \
      "precision mediump float;" \
      "varying vec2 vTextureCoord;" \
      "varying vec4 vTextureColor;" \
      "uniform sampler2D sTexture;" \
      "void main() {gl_FragColor = texture2D(sTexture, vTextureCoord) * vTextureColor;}"

static GLuint textureProgram;
static GLuint textureColor,texturePoint;
static GLuint textureCoord,textureId;

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

static GLushort rectOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

GLuint loadShader(GLenum shaderType, const char* pSource)
{
   GLuint shader = glCreateShader(shaderType);
   glShaderSource(shader, 1, &pSource, NULL);
   glCompileShader(shader);
   return shader;
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
//   if (pointsProgram != 0) glDeleteProgram(pointsProgram); //   glDeleteShader(shader);

   window = ANativeWindow_fromSurface(env, surface);
   realAppH = (*env)->CallStaticIntMethod(env, applicationClass, jgetHeight);
}

static void initPoints()
{
   pointsProgram = glCreateProgram();
   glAttachShader(pointsProgram, loadShader(GL_VERTEX_SHADER, POINTS_VERTEX_CODE));
   glAttachShader(pointsProgram, loadShader(GL_FRAGMENT_SHADER, POINTS_FRAGMENT_CODE));
   glLinkProgram(pointsProgram);
   glUseProgram(pointsProgram);

   pointsColor = glGetAttribLocation(pointsProgram, "a_Color");
   pointsPosition = glGetAttribLocation(pointsProgram, "a_Position"); // get handle to vertex shader's vPosition member
   glEnableVertexAttribArray(pointsColor); // Enable a handle to the colors - since this is the only one used, keep it enabled all the time
   glEnableVertexAttribArray(pointsPosition); // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time
}

void glDrawPixels(Context c, int32 n)
{
   glUseProgram(pointsProgram);
   glVertexAttribPointer(pointsColor, 4 * n, GL_FLOAT, GL_FALSE, 4 * sizeof(float), c->glcolors);
   glVertexAttribPointer(pointsPosition, COORDS_PER_VERTEX * n, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), c->glcoords);
   glDrawArrays(GL_POINTS, 0,n);
}

void initTexture()
{  
   GLfloat textureVerts[8] = { 0, 1,  1, 1,  1, 0,  0, 0 };
   debug("init texture");
   
   textureProgram = glCreateProgram();
   glAttachShader(textureProgram, loadShader(GL_VERTEX_SHADER, TEXTURE_VERTEX_CODE));
   glAttachShader(textureProgram, loadShader(GL_FRAGMENT_SHADER, TEXTURE_FRAGMENT_CODE));
   glLinkProgram(textureProgram);
   glUseProgram(textureProgram);
   
   glActiveTexture(GL_TEXTURE0);

   textureColor = glGetUniformLocation(textureProgram, "uTextureColor");
   textureId    = glGetUniformLocation(textureProgram, "sTexture");
   texturePoint = glGetAttribLocation(textureProgram, "vertexPoint");
   textureCoord = glGetAttribLocation(textureProgram, "aTextureCoord");

   glEnableVertexAttribArray(textureCoord);
   glVertexAttribPointer(textureCoord, 2, GL_FLOAT, false, 0, textureVerts);

   glEnableVertexAttribArray(texturePoint);
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
   glUseProgram(pointsProgram);
   glVertexAttribPointer(pointsColor, 4, GL_FLOAT, GL_FALSE, 4 * sizeof(float), colors);
   glVertexAttribPointer(pointsPosition, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords); // Prepare the triangle coordinate data
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
   glUseProgram(pointsProgram);
   glVertexAttribPointer(pointsColor, 4*2, GL_FLOAT, GL_FALSE, 4 * sizeof(float), colors);
   glVertexAttribPointer(pointsPosition, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords); // Prepare the triangle coordinate data
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
   glUseProgram(pointsProgram);
   glVertexAttribPointer(pointsColor, 4*6, GL_FLOAT, GL_FALSE, 4 * sizeof(float), colors);
   glVertexAttribPointer(pointsPosition, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords); // Prepare the triangle coordinate data
   glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, rectOrder); // GL_LINES, GL_TRIANGLES, GL_POINTS
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
   glUseProgram(textureProgram); glUniformMatrix4fv(glGetUniformLocation(textureProgram, "projectionMatrix"), 1, 0, mat);
   glUseProgram(pointsProgram);  glUniformMatrix4fv(glGetUniformLocation(pointsProgram , "projectionMatrix"), 1, 0, mat);
}

/*
void prepare(String s)
{
   Text t = new Text();
   android.graphics.Rect bounds = new android.graphics.Rect();
   paint.getTextBounds(s, 0, s.length(), bounds);
   t.textW = bounds.width();
   t.textH = bounds.height()+20;
   
   Bitmap image = Bitmap.createBitmap(t.textW+1, t.textH+1, Bitmap.Config.ARGB_8888);
   new Canvas(image).drawText(s, 0, t.textH-20, paint);

   int[] texture = new int[1];
   glGenTextures(1, texture, 0);
   t.texture = texture[0];

   // OpenGL ES provides support for non-power-of-two textures, including its associated mipmaps, provided that
   // the s and t wrap modes are both GL_CLAMP_TO_EDGE.
   glBindTexture(GL_TEXTURE_2D, t.texture);
   glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
   glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
   glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
   glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
   GLUtils.texImage2D(GL_TEXTURE_2D, 0, image, 0);
   glUniform1i(textureId, 0);

   return t;
}

float[] unitQuadVerts = new float[8];

public void draw(Text text, int x, int y) throws Exception
{
   glUseProgram(textureProgram);
   
   unitQuadVerts[0] = unitQuadVerts[6] = x; 
   unitQuadVerts[1] = unitQuadVerts[3] = y;
   unitQuadVerts[2] = unitQuadVerts[4] = x+text.textW;
   unitQuadVerts[5] = unitQuadVerts[7] = y-text.textH;
   vertexBuf.put(unitQuadVerts); vertexBuf.rewind();
   
   glVertexAttribPointer(texturePoint, 2, GL_FLOAT, false, 0, vertexBuf);

   glBindTexture(GL_TEXTURE_2D, text.texture);
   glUniform4f(textureColor, 0,1,0,1);
   glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
}*/

////////////////////////////// OPEN GL 2 //////////////////////////////////
void glLoadTexture(int32* textureId, Pixel *pixels, int32 width, int32 height)
{
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

    return;
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
