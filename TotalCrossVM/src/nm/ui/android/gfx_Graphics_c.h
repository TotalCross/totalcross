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

#ifdef ANDROID
#include <android/native_window.h> // requires ndk r5 or newer
#include <android/native_window_jni.h> // requires ndk r5 or newer
#endif
#ifdef darwin
bool isIpad;
#else
bool isIpad = false;
#endif

bool checkGlError(const char* op, int line)
{
   GLint error;

   //debug("%s (%d)",op,line);

   if (!op)
      return glGetError() != 0;
   else
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
      debug("glError %s at %s (%d)\n", msg, op, line);
      return true;
   }
   return false;
}

#define GL_CHECK_ERROR checkGlError(__FILE__,__LINE__);

#ifdef ANDROID
static void setProjectionMatrix(float w, float h);
static ANativeWindow *window,*lastWindow;
static EGLDisplay _display;
static EGLSurface _surface;
static EGLContext _context;
#endif
static void destroyEGL();
static bool surfaceWillChange;

VoidPs* imgTextures;
int32 realAppH,appW,appH,glShiftY;
float ftransp[16], f255[256];
int32 flen;
float *glXYA;//[flen*2]; x,y
static float texcoords[16], lrcoords[8], shcolors[12],shcoords[8];

// http://www.songho.ca/opengl/gl_projectionmatrix.html
//////////// texture
#define TEXTURE_VERTEX_CODE  \
      "attribute vec4 vertexPoint;" \
      "uniform float alpha;" \
      "uniform mat4 projectionMatrix; " \
      "varying vec2 vTextureCoord;" \
      "varying float vAlpha;" \
      "void main()" \
      "{" \
      "    gl_Position = vec4(vertexPoint.xy,0,1.0) * projectionMatrix;" \
      "    vTextureCoord = vertexPoint.zw;" \
      "    vAlpha = alpha;" \
      "}"

#define TEXTURE_FRAGMENT_CODE \
      "precision mediump float;" \
      "varying vec2 vTextureCoord;" \
      "uniform sampler2D sTexture;" \
      "varying float vAlpha;" \
      "void main()" \
      "{" \
      "   gl_FragColor = texture2D(sTexture, vTextureCoord);" \
      "   gl_FragColor.a *= vAlpha;" \
      "}"

static GLuint textureProgram;
static GLuint texturePoint;
static GLuint textureAlpha;

/////// 

#define TEXT_VERTEX_CODE  \
      "attribute vec4 vertexPoint;" \
      "uniform vec3 rgb;" \
      "uniform mat4 projectionMatrix; " \
      "varying vec2 vTextureCoord;" \
      "varying vec3 v_rgb;" \
      "void main()" \
      "{" \
      "    gl_Position = vec4(vertexPoint.xy,0,1.0) * projectionMatrix;" \
      "    vTextureCoord = vertexPoint.zw;" \
      "    v_rgb = rgb;" \
      "}"

#define TEXT_FRAGMENT_CODE \
      "precision mediump float;" \
      "varying vec2 vTextureCoord;" \
      "uniform sampler2D sTexture;" \
      "varying vec3 v_rgb;" \
      "void main()" \
      "{" \
      "   gl_FragColor = vec4(v_rgb,texture2D(sTexture, vTextureCoord).a);" \
      "}"

static GLuint textProgram;
static GLuint textPoint;
static GLuint textRGB;

//////////// points (text)

#define POINTS_VERTEX_CODE \
      "attribute vec3 a_xya; uniform vec4 a_Color; varying vec4 v_Color;" \
      "uniform mat4 projectionMatrix; " \
      "void main() {gl_PointSize = 1.0; v_Color = vec4(a_Color.x,a_Color.y,a_Color.z, a_xya.z); gl_Position = vec4(a_xya.xy,0,1.0) * projectionMatrix;}"

#define POINTS_FRAGMENT_CODE \
      "precision mediump float;" \
      "varying vec4 v_Color;" \
      "void main() {gl_FragColor = v_Color;}"

static GLuint pointsProgram;
static GLuint pointsXYA;
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

///////////// line, rect, point

#define DOT_VERTEX_CODE \
      "attribute vec4 a_Position;" \
      "uniform mat4 projectionMatrix;" \
      "varying vec2 v_xy;" \
      "void main() {gl_PointSize = 1.0; gl_Position = a_Position*projectionMatrix; v_xy = a_Position.xy;}"

#define DOT_FRAGMENT_CODE \
      "precision mediump float;" \
      "varying vec2 v_xy;" \
      "uniform float isVert;" \
      "uniform vec4 color1;" \
      "uniform vec4 color2;" \
      "void main() {gl_FragColor = mod(isVert > 0.0 ? v_xy.y : v_xy.x, 2.0) >= 1.0 ? color1 : color2;}"

static GLuint dotProgram;
static GLuint dotPosition,dotIsVert;
static GLuint dotColor1,dotColor2;

///////////// shaded rect

#define SHADE_VERTEX_CODE \
      "attribute vec4 a_Position; attribute vec3 a_Color; varying vec4 v_Color;" \
      "uniform mat4 projectionMatrix;" \
      "void main() {gl_PointSize = 1.0; v_Color = vec4(a_Color,1.0); gl_Position = a_Position*projectionMatrix;}"

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
   GLuint shader = glCreateShader(shaderType); GL_CHECK_ERROR
   glShaderSource(shader, 1, &pSource, NULL); GL_CHECK_ERROR
   glCompileShader(shader); GL_CHECK_ERROR

   glGetShaderiv(shader, GL_COMPILE_STATUS, &ret); GL_CHECK_ERROR
   if(!ret)
   {
      glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &ret); GL_CHECK_ERROR
      GLchar buffer[ret];
      glGetShaderInfoLog(shader, ret, &ret, buffer); GL_CHECK_ERROR
      debug("Shader compiler error: %s",buffer);
   }
   return shader;
}

static GLint lastProg=-1;
static Pixel lrpLastRGB = -2, lastTextRGB, lastTextId;
static float lastAlphaMask = -1;
static void setCurrentProgram(GLint prog)
{
   if (prog != lastProg)
   {
      glUseProgram(lastProg = prog); GL_CHECK_ERROR
      if (lastProg != textureProgram && lastProg != textProgram) lrpLastRGB = -2;
   }
}
static void resetGlobals()
{
   lastAlphaMask = lastTextRGB = lastTextId = -1;
}

static GLuint createProgram(char* vertexCode, char* fragmentCode)
{
   GLint ret;
   GLuint p = glCreateProgram();
   glAttachShader(p, loadShader(GL_VERTEX_SHADER, vertexCode)); GL_CHECK_ERROR
   glAttachShader(p, loadShader(GL_FRAGMENT_SHADER, fragmentCode)); GL_CHECK_ERROR
   glLinkProgram(p); GL_CHECK_ERROR
   //glValidateProgram(p);
   glGetProgramiv(p, GL_LINK_STATUS, &ret); GL_CHECK_ERROR
   if (!ret)
   {
      glGetProgramiv(p, GL_INFO_LOG_LENGTH, &ret); GL_CHECK_ERROR
      GLchar buffer[ret];
      glGetProgramInfoLog(p, ret, &ret, buffer); GL_CHECK_ERROR
      debug("Link error: %s",buffer);
   }
   return p;
}

bool initGLES(ScreenSurface screen); // in iOS, implemented in mainview.m
void recreateTextures(bool delTex); // imagePrimitives_c.h

void setTimerInterval(int32 t);
int32 desiredglShiftY;
bool setShiftYonNextUpdateScreen;
#ifdef ANDROID
void JNICALL Java_totalcross_Launcher4A_nativeInitSize(JNIEnv *env, jobject this, jobject surface, jint width, jint height) // called only once
{
   if (!screen.extension)
      screen.extension = newX(ScreenSurfaceEx);

   if (surface == null) // passed null when the surface is destroyed
   {
      if (width == -999)
      {
         if (needsPaint != null)
         {
            desiredglShiftY = height == 0 ? 0 : appH - height; // change only after the next screen update, since here we are running in a different thread
            setShiftYonNextUpdateScreen = true;
            *needsPaint = true; // schedule a screen paint to update the shiftY values
            setTimerInterval(1);
         }
      }
      else
      if (width == -998)
         recreateTextures(true); // first we delete the textures before the gl context is invalid
      else
      if (width == -997) // when the screen is turned off and on again, this ensures that the textures will be recreated
         recreateTextures(false); // now we set the changed flag for all textures
      else
         surfaceWillChange = true; // block all screen updates
      return;
   }
   desiredglShiftY = glShiftY = 0;
   setShiftYonNextUpdateScreen = true;
   appW = width;
   appH = height;
   surfaceWillChange = false;
   if (window) // fixed memory leak
      ANativeWindow_release(window);
   window = ANativeWindow_fromSurface(env, surface);
   realAppH = (*env)->CallStaticIntMethod(env, applicationClass, jgetHeight);
   if (lastWindow && lastWindow != window)
   {
      destroyEGL();
      initGLES(&screen);
      recreateTextures(false); // now we set the changed flag for all textures
   }
   lastWindow = window;
}
#endif

static void initPoints()
{
   pointsProgram = createProgram(POINTS_VERTEX_CODE, POINTS_FRAGMENT_CODE);
   setCurrentProgram(pointsProgram);
   pointsColor = glGetUniformLocation(pointsProgram, "a_Color"); GL_CHECK_ERROR
   pointsXYA = glGetAttribLocation(pointsProgram, "a_xya"); GL_CHECK_ERROR // get handle to vertex shader's vPosition member
   glEnableVertexAttribArray(pointsXYA); GL_CHECK_ERROR // Enable a handle to the colors - since this is the only one used, keep it enabled all the time
}

static int pixLastRGB = -1;
void glDrawPixels(int32 n, int32 rgb)
{
   setCurrentProgram(pointsProgram);
   if (pixLastRGB != rgb)
   {
      PixelConv pc;
      pc.pixel = pixLastRGB = rgb;
      glUniform4f(pointsColor, f255[pc.r], f255[pc.g], f255[pc.b], 0); GL_CHECK_ERROR
   }
   glVertexAttribPointer(pointsXYA, 3, GL_FLOAT, GL_FALSE, 0, glXYA); GL_CHECK_ERROR
   glDrawArrays(GL_POINTS, 0,n); GL_CHECK_ERROR
}

void glDrawLines(Context currentContext, TCObject g, int32* x, int32* y, int32 n, int32 tx, int32 ty, Pixel rgb, bool fill)
{
   PixelConv pc;
   setCurrentProgram(lrpProgram);
   pc.pixel = rgb;
   pc.a = 255;
   if (lrpLastRGB != pc.pixel) // prevent color change = performance x2 in galaxy tab2
   {
      lrpLastRGB = pc.pixel;
      glUniform4f(lrpColor, f255[pc.r],f255[pc.g],f255[pc.b],f255[pc.a]); GL_CHECK_ERROR
   }
   if (checkfloatBuffer(currentContext, n))
   {
      int32 i;
      float *glV = glXYA;
      for (i = 0; i < n; i++)
      {
         *glV++ = (float)(*x++ + tx);
         *glV++ = (float)(*y++ + ty);
      }
      glVertexAttribPointer(lrpPosition, 2, GL_FLOAT, GL_FALSE, 0, glXYA); GL_CHECK_ERROR
      glDrawArrays(fill ? GL_TRIANGLE_FAN : GL_LINES, 0, n); GL_CHECK_ERROR
   }
}

static void initShade()
{
   shadeProgram = createProgram(SHADE_VERTEX_CODE, SHADE_FRAGMENT_CODE);
   setCurrentProgram(shadeProgram);
   shadeColor = glGetAttribLocation(shadeProgram, "a_Color"); GL_CHECK_ERROR
   shadePosition = glGetAttribLocation(shadeProgram, "a_Position"); GL_CHECK_ERROR // get handle to vertex shader's vPosition member
   glEnableVertexAttribArray(shadeColor); GL_CHECK_ERROR // Enable a handle to the colors - since this is the only one used, keep it enabled all the time
   glEnableVertexAttribArray(shadePosition); GL_CHECK_ERROR // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time
}

void glFillShadedRect(TCObject g, int32 x, int32 y, int32 w, int32 h, PixelConv c1, PixelConv c2, bool horiz)
{
   setCurrentProgram(shadeProgram);
   glVertexAttribPointer(shadeColor, 3, GL_FLOAT, GL_FALSE, 0, shcolors); GL_CHECK_ERROR
   glVertexAttribPointer(shadePosition, 2, GL_FLOAT, GL_FALSE, 0, shcoords); GL_CHECK_ERROR

   shcoords[0] = shcoords[2] = x;
   shcoords[1] = shcoords[7] = y;
   shcoords[3] = shcoords[5] = y+h;
   shcoords[4] = shcoords[6] = x+w;

   if (!horiz)
   {
      shcolors[0] = shcolors[9]  = f255[c2.r]; // upper left + upper right
      shcolors[1] = shcolors[10] = f255[c2.g];
      shcolors[2] = shcolors[11] = f255[c2.b];

      shcolors[3] = shcolors[6]  = f255[c1.r]; // lower left + lower right
      shcolors[4] = shcolors[7]  = f255[c1.g];
      shcolors[5] = shcolors[8]  = f255[c1.b];
   }
   else
   {
      shcolors[0] = shcolors[3]  = f255[c2.r];  // upper left + lower left
      shcolors[1] = shcolors[4]  = f255[c2.g];
      shcolors[2] = shcolors[5]  = f255[c2.b];

      shcolors[6] = shcolors[9]  = f255[c1.r]; // lower right + upper right
      shcolors[7] = shcolors[10] = f255[c1.g];
      shcolors[8] = shcolors[11] = f255[c1.b];
   }

   glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, rectOrder); GL_CHECK_ERROR
}

void initTexture()
{
   // images
   textureProgram = createProgram(TEXTURE_VERTEX_CODE, TEXTURE_FRAGMENT_CODE);
   setCurrentProgram(textureProgram);
   texturePoint = glGetAttribLocation(textureProgram, "vertexPoint"); GL_CHECK_ERROR
   textureAlpha = glGetUniformLocation(textureProgram, "alpha"); GL_CHECK_ERROR

   glEnableVertexAttribArray(texturePoint); GL_CHECK_ERROR

   // text char
   textProgram = createProgram(TEXT_VERTEX_CODE, TEXT_FRAGMENT_CODE);
   setCurrentProgram(textProgram);
   textPoint = glGetAttribLocation(textProgram, "vertexPoint"); GL_CHECK_ERROR
   textRGB   = glGetUniformLocation(textProgram, "rgb"); GL_CHECK_ERROR

   glEnableVertexAttribArray(textPoint); GL_CHECK_ERROR
}

void glLoadTexture(Context currentContext, TCObject img, int32* textureId, Pixel *pixels, int32 width, int32 height, bool updateList, bool onlyAlpha)
{
   int32 i;
   PixelConv* pf = (PixelConv*)pixels, *pt, *pt0;
   bool textureAlreadyCreated = *textureId != 0;
   bool err;
   if (onlyAlpha)
      pt = pt0 = (PixelConv*)pixels;
   else
   {
      pt0 = pt = (PixelConv*)xmalloc(width*height*4);
      if (!pt)
      {
         throwException(currentContext, OutOfMemoryError, "Out of bitmap memory for image with %dx%d",width,height);
         return;
      }
   }
   if (!textureAlreadyCreated)
   {
      glGenTextures(1, (GLuint*)textureId); err = GL_CHECK_ERROR
      if (err)
      {
         throwException(currentContext, OutOfMemoryError, "Cannot bind texture for image with %dx%d",width,height);
         return;
      }
   }
   // OpenGL ES provides support for non-power-of-two textures, provided that the s and t wrap modes are both GL_CLAMP_TO_EDGE.
   glBindTexture(GL_TEXTURE_2D, *textureId); GL_CHECK_ERROR
   if (!textureAlreadyCreated)
   {
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR); GL_CHECK_ERROR
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR); GL_CHECK_ERROR
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE); GL_CHECK_ERROR
      glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE); GL_CHECK_ERROR
   }
   // must invert the pixels from ARGB to RGBA
   if (!onlyAlpha)
      for (i = width*height; --i >= 0;pt++,pf++) {pt->a = pf->r; pt->b = pf->g; pt->g = pf->b; pt->r = pf->a;}
   if (textureAlreadyCreated)
   {
      glTexSubImage2D(GL_TEXTURE_2D, 0,0,0,width,height, onlyAlpha ? GL_ALPHA : GL_RGBA,GL_UNSIGNED_BYTE, pt0); GL_CHECK_ERROR
      glBindTexture(GL_TEXTURE_2D, 0); GL_CHECK_ERROR
   }
   else
   {
      glTexImage2D(GL_TEXTURE_2D, 0, onlyAlpha ? GL_ALPHA : GL_RGBA, width, height, 0, onlyAlpha ? GL_ALPHA : GL_RGBA,GL_UNSIGNED_BYTE, pt0); err = GL_CHECK_ERROR
      if (err)
      {
         glDeleteTextures(1,(GLuint*)textureId); GL_CHECK_ERROR
         *textureId = 0;
         throwException(currentContext, OutOfMemoryError, "Out of texture memory for image with %dx%d",width,height);
      }
      else
      if (updateList && !VoidPsContains(imgTextures, img)) // dont add duplicate
         imgTextures = VoidPsAdd(imgTextures, img, null);
      glBindTexture(GL_TEXTURE_2D, 0); GL_CHECK_ERROR
   }
   if (!onlyAlpha) xfree(pt0);
}

void glDeleteTexture(TCObject img, int32* textureId, bool updateList)
{
   if (textureId != null && textureId[0] != 0)
   {
      glDeleteTextures(1,(GLuint*)textureId); GL_CHECK_ERROR
      *textureId = 0;
   }
   if (updateList && VoidPsContains(imgTextures, img))
      imgTextures = VoidPsRemove(imgTextures, img, null);
}

void glDrawTexture(int32* textureId, int32 x, int32 y, int32 w, int32 h, int32 dstX, int32 dstY, int32 dstW, int32 dstH, int32 imgW, int32 imgH, PixelConv* color, int32 alphaMask)
{
   bool isDrawText = color != null;
   if (textureId[0] == 0) return;

   float* coords = texcoords;

   setCurrentProgram(isDrawText ? textProgram : textureProgram);
   if (lastTextId != *textureId) // the bound texture is per graphics card, not by per program
      glBindTexture(GL_TEXTURE_2D, lastTextId = *textureId); GL_CHECK_ERROR

   float left = (float)x/(float)imgW,top=(float)y/(float)imgH,right=(float)(x+w)/(float)imgW,bottom=(float)(y+h)/(float)imgH; // 0,0,1,1

   // coordinates
   coords[0 ] = coords[12] = dstX;
   coords[1 ] = coords[5 ] = isDrawText ? dstY+dstH : dstY+h;
   coords[2 ] = coords[14] = left;
   coords[3 ] = coords[7 ] = bottom;
   coords[4 ] = coords[8 ] = isDrawText ? dstX+dstW : dstX+w;
   coords[6 ] = coords[10] = right;
   coords[9 ] = coords[13] = dstY;
   coords[11] = coords[15] = top;
   
   glVertexAttribPointer(isDrawText ? textPoint : texturePoint, 4, GL_FLOAT, false, 0, coords); GL_CHECK_ERROR

   if (!isDrawText && lastAlphaMask != alphaMask) // prevent color change = performance x2 in galaxy tab2
   {
      lastAlphaMask = alphaMask;
      glUniform1f(textureAlpha, f255[alphaMask]);
   }
   if (isDrawText && lastTextRGB != color->pixel) // prevent color change = performance x2 in galaxy tab2
   {
      lastTextRGB = color->pixel;
      glUniform3f(textRGB, f255[color->r],f255[color->g],f255[color->b]); GL_CHECK_ERROR
   }
   glDrawArrays(GL_TRIANGLE_FAN, 0, 4); GL_CHECK_ERROR
}

void initLineRectPoint()
{
   lrpProgram = createProgram(LRP_VERTEX_CODE, LRP_FRAGMENT_CODE);
   setCurrentProgram(lrpProgram);
   lrpColor = glGetUniformLocation(lrpProgram, "a_Color"); GL_CHECK_ERROR
   lrpPosition = glGetAttribLocation(lrpProgram, "a_Position"); GL_CHECK_ERROR
   glEnableVertexAttribArray(lrpPosition); GL_CHECK_ERROR

   dotProgram = createProgram(DOT_VERTEX_CODE, DOT_FRAGMENT_CODE);
   setCurrentProgram(dotProgram);
   dotColor1 = glGetUniformLocation(dotProgram, "color1"); GL_CHECK_ERROR
   dotColor2 = glGetUniformLocation(dotProgram, "color2"); GL_CHECK_ERROR
   dotPosition = glGetAttribLocation(dotProgram, "a_Position"); GL_CHECK_ERROR
   dotIsVert = glGetUniformLocation(dotProgram, "isVert"); GL_CHECK_ERROR
   glEnableVertexAttribArray(dotPosition); GL_CHECK_ERROR
}

void glSetLineWidth(int32 w)
{
   setCurrentProgram(lrpProgram);
   glLineWidth(w); GL_CHECK_ERROR
}

typedef enum
{
   SIMPLE,
   DOTS,
   DIAGONAL
}  LRPType;

void drawLRP(int32 x, int32 y, int32 w, int32 h, int32 rgb, int32 rgb2, int32 a, LRPType type)
{
   float* coords = lrcoords;
   setCurrentProgram(type == DOTS ? dotProgram : lrpProgram);
   glVertexAttribPointer(type == DOTS ? dotPosition : lrpPosition, 2, GL_FLOAT, GL_FALSE, 0, coords); GL_CHECK_ERROR
   PixelConv pc;
   pc.pixel = rgb;
   pc.a = a;
   if (type == DOTS)
   {
      glUniform1f(dotIsVert, x == w ? 1.0 : 0.0); GL_CHECK_ERROR
      glUniform4f(dotColor1, f255[pc.r],f255[pc.g],f255[pc.b],1); GL_CHECK_ERROR
      pc.pixel = rgb2;
      glUniform4f(dotColor2, f255[pc.r],f255[pc.g],f255[pc.b],1); GL_CHECK_ERROR
   }
   else
   if (lrpLastRGB != pc.pixel) // prevent color change = performance x2 in galaxy tab2
   {
      lrpLastRGB = pc.pixel;
      glUniform4f(lrpColor, f255[pc.r],f255[pc.g],f255[pc.b],f255[pc.a]); GL_CHECK_ERROR
   }
   if (type == DIAGONAL || type == DOTS)
   {
      coords[0] = x;
      coords[1] = y;
      coords[2] = w;  // x2
      coords[3] = h; // y2
      glDrawArrays(GL_LINES, 0,2); GL_CHECK_ERROR
   }
   else
   {
      coords[0] = coords[2] = x;
      coords[1] = coords[7] = y;
      coords[3] = coords[5] = y+h;
      coords[4] = coords[6] = x+w;
      glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, rectOrder); GL_CHECK_ERROR
   }
}

void glDrawPixel(int32 x, int32 y, int32 rgb, int32 a)
{
   drawLRP(x,y,1,1,rgb,-1,a, SIMPLE);
}

void glDrawThickLine(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb, int32 a)
{
   drawLRP(x1,y1,x2,y2,rgb,-1,a, DIAGONAL);
}

void glDrawDots(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb1, int32 rgb2)
{
   int32 extra = x1 == x2 ? 1 : 0;
   drawLRP(x1+extra, y1, x2+extra, y2-extra, rgb1, rgb2, 255, DOTS);
}

void glDrawLine(int32 x1, int32 y1, int32 x2, int32 y2, int32 rgb, int32 a)
{
   // The Samsung Galaxy Tab 2 (4.0.4) has a bug in opengl for drawing horizontal/vertical lines: it draws at wrong coordinates, and incomplete sometimes. so we use fillrect, which always work
   if (x1 == x2)
      drawLRP(min32(x1,x2),min32(y1,y2),1,abs32(y2-y1), rgb,-1,a, SIMPLE);
   else
   if (y1 == y2)
      drawLRP(min32(x1,x2),min32(y1,y2),abs32(x2-x1),1, rgb,-1,a, SIMPLE);
   else
      drawLRP(x1,y1,x2,y2,rgb,-1,a, DIAGONAL);
}

void glFillRect(int32 x, int32 y, int32 w, int32 h, int32 rgb, int32 a)
{
   drawLRP(x,y,w,h,rgb,-1,a, SIMPLE);
}

typedef union
{
   struct{ GLubyte r, g, b, a; };
   Pixel pixel;
} glpixel;

int32 glGetPixel(int32 x, int32 y)
{
   glpixel gp;
   glReadPixels(x, appH-y-1, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, &gp); GL_CHECK_ERROR
   return (((int32)gp.r) << 16) | (((int32)gp.g) << 8) | (int32)gp.b;
}

void glGetPixels(Pixel* dstPixels,int32 srcX,int32 srcY,int32 width,int32 height,int32 pitch)
{
   #define GL_BGRA 0x80E1 // BGRA is 20x faster than RGBA on devices that supports it
   PixelConv* p;
   glpixel gp;
   int32 i;
   GLint ext_format, ext_type;
   glGetIntegerv(GL_IMPLEMENTATION_COLOR_READ_FORMAT, &ext_format);
   glGetIntegerv(GL_IMPLEMENTATION_COLOR_READ_TYPE, &ext_type);
   if (ext_format == GL_BGRA && ext_type == GL_UNSIGNED_BYTE)
      for (; height-- > 0; srcY++,dstPixels += pitch)
      {
         glReadPixels(srcX, appH-srcY-1, width, 1, GL_BGRA, GL_UNSIGNED_BYTE, dstPixels); GL_CHECK_ERROR
         p = (PixelConv*)dstPixels;
         for (i = 0; i < width; i++,p++)
         {
            gp.pixel = p->pixel;
            p->a = 255;//gp.a; - with this, the transition effect causes a fade-out when finished in UIGadgets
            p->r = gp.b;
            p->g = gp.g;
            p->b = gp.r;
         }
      }
   else
      for (; height-- > 0; srcY++,dstPixels += pitch)
      {
         glReadPixels(srcX, appH-srcY-1, width, 1, GL_RGBA, GL_UNSIGNED_BYTE, dstPixels); GL_CHECK_ERROR
         p = (PixelConv*)dstPixels;
         for (i = 0; i < width; i++,p++)
         {
            gp.pixel = p->pixel;
            p->a = 255;//gp.a; - with this, the transition effect causes a fade-out when finished in UIGadgets
            p->r = gp.r;
            p->g = gp.g;
            p->b = gp.b;
         }
      }
}

void flushAll()
{
   glFlush(); GL_CHECK_ERROR
}

static void setProjectionMatrix(float w, float h)
{
   double t = -glShiftY, b = h-glShiftY; // f = 1, n = -1
   float mat[] =
   {
      2.0/w,    0.0,   0.0, -1.0,
      0.0,    2.0/(t-b),   0.0,  -(t+b)/(t-b),
      0.0,      0.0,  -1.0,  0.0,
      0.0,      0.0,   0.0,  1.0
   };
   
   setCurrentProgram(textProgram);    glUniformMatrix4fv(glGetUniformLocation(textProgram,    "projectionMatrix"), 1, 0, mat); GL_CHECK_ERROR
   setCurrentProgram(textureProgram); glUniformMatrix4fv(glGetUniformLocation(textureProgram, "projectionMatrix"), 1, 0, mat); GL_CHECK_ERROR
   setCurrentProgram(lrpProgram);     glUniformMatrix4fv(glGetUniformLocation(lrpProgram    , "projectionMatrix"), 1, 0, mat); GL_CHECK_ERROR
   setCurrentProgram(dotProgram);     glUniformMatrix4fv(glGetUniformLocation(dotProgram    , "projectionMatrix"), 1, 0, mat); GL_CHECK_ERROR
   setCurrentProgram(pointsProgram);  glUniformMatrix4fv(glGetUniformLocation(pointsProgram , "projectionMatrix"), 1, 0, mat); GL_CHECK_ERROR
   setCurrentProgram(shadeProgram);   glUniformMatrix4fv(glGetUniformLocation(shadeProgram  , "projectionMatrix"), 1, 0, mat); GL_CHECK_ERROR
#ifdef darwin
    int fw,fh;
    glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_WIDTH, &fw);
    glGetRenderbufferParameteriv(GL_RENDERBUFFER, GL_RENDERBUFFER_HEIGHT, &fh);
    glViewport(0, 0, fw, fh); GL_CHECK_ERROR
#else
    glViewport(0, 0, w, h); GL_CHECK_ERROR
#endif
}

/////////////////////////////////////////////////////////////////////////
bool checkfloatBuffer(Context c, int32 n)
{
   if (n > flen)
   {
      xfree(glXYA);
      flen = n*3/2;
      int len = flen*3;
      glXYA = (float*)xmalloc(sizeof(float)*len);
      if (!glXYA)
      {
         throwException(c, OutOfMemoryError, "Cannot allocate buffer for drawPixels");
         flen = 0;
         return false;
      }
   }
   return true;
}

bool setupGL(int width, int height)
{
    int i;
    pixLastRGB = -1;
    appW = width;
    appH = height;

    initTexture();
    initLineRectPoint();
    initPoints();
    initShade();
    setProjectionMatrix(appW,appH);

    glPixelStorei(GL_PACK_ALIGNMENT, 1); GL_CHECK_ERROR
    glPixelStorei(GL_UNPACK_ALIGNMENT, 1); GL_CHECK_ERROR

    glDisable(GL_CULL_FACE); GL_CHECK_ERROR
    glDisable(GL_DEPTH_TEST); GL_CHECK_ERROR
    glEnable(GL_BLEND); GL_CHECK_ERROR // enable color alpha channel
    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA); GL_CHECK_ERROR

    for (i = 0; i < 14; i++)
        ftransp[i+1] = (float)(i<<4) / (float)255; // make it lighter. since ftransp[0] is never used, shift it to [1]
    ftransp[15] = 1;
    for (i = 0; i <= 255; i++)
        f255[i] = (float)i/(float)255;
    return checkfloatBuffer(mainContext,1000);
}

#ifdef ANDROID
bool initGLES(ScreenSurface /*screen*/unused)
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
}
#endif

void graphicsIOSdoRotate();
void privateScreenChange(int32 w, int32 h)
{
#ifdef darwin
   graphicsIOSdoRotate();
#else
    appW = w;
    appH = h;
#endif
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

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
#ifdef ANDROID
   if (!isScreenChange)
   {
      destroyEGL();
      xfree(screen->extension);
      xfree(glXYA);
   }
#else
   if (isScreenChange)
       screen->extension = NULL;
   else
   {
      if (screen->extension)
         free(screen->extension);
      deviceCtx = screen->extension = NULL;
      xfree(glXYA);
   }
#endif
}

void setTimerInterval(int32 t);
void setShiftYgl(int32 shiftY)
{
   if (setShiftYonNextUpdateScreen && needsPaint != null)
   {
      setShiftYonNextUpdateScreen = false;
#ifdef ANDROID
      glShiftY = max32(0,desiredglShiftY - shiftY); // guich: under 0 occurs sometimes when the keyboard is closed and the desired shift y is 0. it was resulting in a negative value.
#else
      glShiftY = -shiftY;
#endif
      setProjectionMatrix(appW,appH);
      screen.shiftY = shiftY;
      *needsPaint = true; // now that the shifts has been set, schedule another window update to paint at the given location
      setTimerInterval(1); // needed, dont remove!
   }
}
void graphicsUpdateScreenIOS();
void graphicsUpdateScreen(Context currentContext, ScreenSurface screen)
{
   if (surfaceWillChange) return;
#ifdef ANDROID
   eglSwapBuffers(_display, _surface); // requires API LEVEL 9 (2.3 and up)
#else
   graphicsUpdateScreenIOS();
#endif
   // erase buffer with keyboard's background color
   PixelConv gray;
   gray.pixel = shiftScreenColorP ? *shiftScreenColorP : 0xFFFFFF;
   glClearColor(f255[gray.r],f255[gray.g],f255[gray.b],1); GL_CHECK_ERROR
   glClear(GL_COLOR_BUFFER_BIT); GL_CHECK_ERROR
   resetGlobals();
}
