
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

int realAppH,appW,appH;
GLfloat ftransp[16], f255[256];
int32 flen;
GLfloat* glcoords;//[flen*2]; x,y
GLfloat* glcolors;//[flen];   alpha
static GLfloat texcoords[16], lrcoords[8], shcolors[24],shcoords[8];
static int32 *pixcoords, *pixcolors, *pixEnd;

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
      "attribute vec4 a_Position; uniform vec4 a_Color; varying vec4 v_Color; attribute float alpha;" \
      "uniform mat4 projectionMatrix; " \
      "void main() {gl_PointSize = 1.0; v_Color = vec4(a_Color.x,a_Color.y,a_Color.z,alpha); gl_Position = a_Position * projectionMatrix;}"

#define POINTS_FRAGMENT_CODE \
      "precision mediump float;" \
      "varying vec4 v_Color;" \
      "void main() {gl_FragColor = v_Color;}"

static GLuint pointsProgram;
static GLuint pointsPosition;
static GLuint pointsColor;
static GLuint pointsAlpha;

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

///////////// shaded rect

#define SHADE_VERTEX_CODE \
      "attribute vec4 a_Position; attribute vec4 a_Color; varying vec4 v_Color;" \
      "uniform mat4 projectionMatrix;" \
      "void main() {gl_PointSize = 1.0; v_Color = a_Color; gl_Position = a_Position*projectionMatrix;}"

#define SHADE_FRAGMENT_CODE \
      "precision mediump float;" \
      "varying vec4 v_Color;" \
      "void main() {gl_FragColor = v_Color;}"

static GLuint shadeProgram;
static GLuint shadePosition;
static GLuint shadeColor;


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
   pointsColor = glGetUniformLocation(pointsProgram, "a_Color");
   pointsAlpha = glGetAttribLocation(pointsProgram, "alpha");
   pointsPosition = glGetAttribLocation(pointsProgram, "a_Position"); // get handle to vertex shader's vPosition member
   glEnableVertexAttribArray(pointsAlpha); // Enable a handle to the colors - since this is the only one used, keep it enabled all the time
   glEnableVertexAttribArray(pointsPosition); // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time
}

static int pixLastRGB = -1;
void glDrawPixels(int32 n, int32 rgb)
{
   setCurrentProgram(pointsProgram);
   if (pixLastRGB != rgb)
   {
      PixelConv pc;
      pc.pixel = pixLastRGB = rgb;
      glUniform4f(pointsColor, f255[pc.r], f255[pc.g], f255[pc.b], 0);
   }
   glVertexAttribPointer(pointsAlpha, 1, GL_FLOAT, GL_FALSE, 0, glcolors);
   glVertexAttribPointer(pointsPosition, 2, GL_FLOAT, GL_FALSE, 0, glcoords);
   glDrawArrays(GL_POINTS, 0,n);
}

static void initShade()
{
   shadeProgram = createProgram(SHADE_VERTEX_CODE, SHADE_FRAGMENT_CODE);
   setCurrentProgram(shadeProgram);
   shadeColor = glGetAttribLocation(shadeProgram, "a_Color");
   shadePosition = glGetAttribLocation(shadeProgram, "a_Position"); // get handle to vertex shader's vPosition member
   glEnableVertexAttribArray(shadeColor); // Enable a handle to the colors - since this is the only one used, keep it enabled all the time
   glEnableVertexAttribArray(shadePosition); // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time
}

void glFillShadedRect(int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz)
{     
   if (pixcolors != (int32*)glcolors) flushPixels();
   setCurrentProgram(shadeProgram);
   glVertexAttribPointer(shadeColor, 4, GL_FLOAT, GL_FALSE, 0, shcolors);
   glVertexAttribPointer(shadePosition, 2, GL_FLOAT, GL_FALSE, 0, shcoords);
   
   shcoords[0] = shcoords[2] = x;
   shcoords[1] = shcoords[7] = y;
   shcoords[3] = shcoords[5] = y+h;
   shcoords[4] = shcoords[6] = x+w;

   shcolors[3] = shcolors[7] = shcolors[11] = shcolors[15] = shcolors[19] = shcolors[23] = 1; 
   if (!horiz)
   {
      shcolors[0] = shcolors[12] = shcolors[20] = f255[c1.r];
      shcolors[1] = shcolors[13] = shcolors[21] = f255[c1.g];
      shcolors[2] = shcolors[14] = shcolors[22] = f255[c1.b];
      
      shcolors[4] = shcolors[8]  = shcolors[16] = f255[c2.r];
      shcolors[5] = shcolors[9]  = shcolors[17] = f255[c2.g];
      shcolors[6] = shcolors[10] = shcolors[18] = f255[c2.b];
   }
   else
   {
      shcolors[0] = shcolors[4] = shcolors[16] = shcolors[20] = f255[c2.r];
      shcolors[1] = shcolors[5] = shcolors[17] = shcolors[21] = f255[c2.g];
      shcolors[2] = shcolors[6] = shcolors[18] = shcolors[22] = f255[c2.b];
      
      shcolors[8]  = shcolors[12] = f255[c1.r];
      shcolors[9]  = shcolors[13] = f255[c1.g];
      shcolors[10] = shcolors[14] = f255[c1.b];
   }
    
   glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, rectOrder);
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
   if (pixcolors != (int32*)glcolors) flushPixels();
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
   lrpPosition = glGetAttribLocation(lrpProgram, "a_Position");
   glEnableVertexAttribArray(lrpPosition);
}

#define IS_PIXEL (1<<31)
#define IS_DIAGONAL  (1<<30)

void flushPixels()
{
   if (pixcolors != (int32*)glcolors)
   {
      int32 n = pixcolors-(int32*)glcolors, i;
      PixelConv pc;
      GLfloat* coords = lrcoords;
      setCurrentProgram(lrpProgram);
      pixcoords = (int32*)glcoords;
      pixcolors = (int32*)glcolors;
      glVertexAttribPointer(lrpPosition, 2, GL_FLOAT, GL_FALSE, 0, coords);
      int32 lastRGBA = ~*pixcolors;
      int32 x,y,w,h,x2,y2;
      for (i = 0; i < n; i++)
      {  
         // color
         int32 rgba = *pixcolors++;
         if (lastRGBA != rgba) // prevent color change = performance x2 in galaxy tab2
         {
            pc.pixel = lastRGBA = rgba;
            glUniform4f(lrpColor, f255[pc.r],f255[pc.g],f255[pc.b],f255[pc.a]);
         }
         // coord
         x = *pixcoords++;
         y = *pixcoords++;
         if (x & IS_DIAGONAL)
         {                    
            x2 = *pixcoords++;
            y2 = *pixcoords++;
            coords[0] = x & ~IS_DIAGONAL;
            coords[1] = y;
            coords[2] = x2;
            coords[3] = y2;
            glDrawArrays(GL_LINES, 0,2);
         }
         else
         {
            if (x & IS_PIXEL)
            {
               x &= ~IS_PIXEL;
               w = h = 1;
            }
            else
            {
               w = *pixcoords++;
               h = *pixcoords++;
            }
            coords[0] = coords[2] = x;
            coords[1] = coords[7] = y;
            coords[3] = coords[5] = y+h;
            coords[4] = coords[6] = x+w;

            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, rectOrder);
         }
      }
      pixcoords = (int32*)glcoords;
      pixcolors = (int32*)glcolors;
   }
}

static void add2pipe(int32 x, int32 y, int32 w, int32 h, int32 rgb, int32 a)
{
   bool isPixel = (x & IS_PIXEL) != 0;
   if ((pixcoords+(isPixel ? 2 : 4)) > pixEnd)
      flushPixels();
   *pixcoords++ = x;
   *pixcoords++ = y;
   if (!isPixel)
   {
      *pixcoords++ = w;
      *pixcoords++ = h;
   }
   PixelConv pc;
   pc.pixel = rgb;
   pc.a = a;
   *pixcolors++ = pc.pixel;
}

void glDrawPixel(int32 x, int32 y, int32 rgb, int32 a)
{   
   add2pipe(x|IS_PIXEL,y,1,1,rgb,a);
}

void glDrawLine(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb, int32 a)
{
   // The Samsung Galaxy Tab 2 (4.0.4) has a bug in opengl for drawing horizontal/vertical lines: it draws at wrong coordinates, and incomplete sometimes. so we use fillrect, which always work
   if (x1 == x2)
      add2pipe(min32(x1,x2),min32(y1,y2),1,abs32(y2-y1),rgb,a);
   else
   if (y1 == y2)
      add2pipe(min32(x1,x2),min32(y1,y2),abs32(x2-x1),1,rgb,a);
   else              
      add2pipe(x1|IS_DIAGONAL,y1,x2,y2,rgb,a);
}

void glFillRect(int32 x, int32 y, int32 w, int32 h, int32 rgb, int32 a)
{
   add2pipe(x,y,w,h,rgb,a);
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
   setCurrentProgram(shadeProgram);   glUniformMatrix4fv(glGetUniformLocation(shadeProgram  , "projectionMatrix"), 1, 0, mat);
}


typedef union
{
   struct{ GLubyte r, g, b, a; };
   Pixel pixel;
} glpixel;

int32 glGetPixel(int32 x, int32 y)
{                
   glpixel gp;
   if (pixcolors != (int32*)glcolors) flushPixels();
   glReadPixels(x, appH-y-1, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, &gp);
   return (((int32)gp.r) << 16) | (((int32)gp.g) << 8) | (int32)gp.b;
}

void glGetPixels(Pixel* dstPixels,int32 srcX,int32 srcY,int32 width,int32 height,int32 pitch)
{          
   Pixel* p;
   PixelConv pc;
   glpixel gp;
   int32 i;
   if (pixcolors != (int32*)glcolors) flushPixels();
   for (; height-- > 0; srcY++,dstPixels += pitch)
   {
      glReadPixels(srcX, appH-srcY-1, width, 1, GL_RGBA, GL_UNSIGNED_BYTE, dstPixels);
      p = dstPixels;
      for (i = 0; i < width; i++)
      {
         gp.pixel = *p;
         pc.a = gp.a;
         pc.r = gp.r;
         pc.g = gp.g;
         pc.b = gp.b;
         *p++ = pc.pixel;       
      }
   }
}

void glSetClip(int32 x1, int32 y1, int32 x2, int32 y2)
{  
   if (x1 == 0 && y1 == 0 && x2 == appW && y2 == appH) // set clip to whole screen disables it
      glClearClip();
   else
   {                             
      glEnable(GL_SCISSOR_TEST);
      glScissor(x1,appH-y2,x2-x1,y2-y1);
   }
}

void glClearClip()
{
   glDisable(GL_SCISSOR_TEST);
}   

/////////////////////////////////////////////////////////////////////////
bool checkGLfloatBuffer(Context c, int32 n)
{
   if (n > flen)
   {
      xfree(glcoords);
      xfree(glcolors);
      flen = n*3/2;
      int len = flen*2;
      glcoords = (GLfloat*)xmalloc(sizeof(GLfloat)*len); pixcoords = (int32*)glcoords;
      glcolors = (GLfloat*)xmalloc(sizeof(GLfloat)*flen); pixcolors = (int32*)glcolors;
      pixEnd = pixcoords + len;
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
    initShade();
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

    return checkGLfloatBuffer(mainContext,10000);
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
   screen->pixels = (uint8*)1;
   return screen->pixels != null;
}

void graphicsUpdateScreenIOS(ScreenSurface screen);
void graphicsUpdateScreen(Context currentContext, ScreenSurface screen)
{
   if (pixcolors != (int32*)glcolors) flushPixels();
#ifdef ANDROID
   eglSwapBuffers(_display, _surface);
#else
   graphicsUpdateScreenIOS(screen);
#endif
   glClearColor(0,0,0,0);
   glClear(GL_COLOR_BUFFER_BIT);
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
