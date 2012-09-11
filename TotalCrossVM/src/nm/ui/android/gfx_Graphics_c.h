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

void checkGlError(const char* op) 
{        
   GLint error;
   for (error = glGetError(); error; error = glGetError()) 
      debug("after %s() glError (0x%x)\n", op, error);
}

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

/*
 * Class:     totalcross_Launcher4A
 * Method:    nativeInitSize
 * Signature: (int,int)V
 */
void JNICALL Java_totalcross_Launcher4A_nativeInitSize(JNIEnv *env, jobject this, jint width, jint height) // called only once
{
   char buf[512];
   ScreenSurfaceEx ex = screen.extension = newX(ScreenSurfaceEx);
   appW = width;
   appH = height;
   if (gProgram != 0)
      glDeleteProgram(gProgram);
//   glDeleteShader(shader);
   
   gProgram = glCreateProgram();
   glAttachShader(gProgram, loadShader(GL_VERTEX_SHADER, vertexShaderCode(buf, width, height)));
   glAttachShader(gProgram, loadShader(GL_FRAGMENT_SHADER, fragmentShaderCode));
   glLinkProgram(gProgram);
   glUseProgram(gProgram);
   
   gColorHandle = glGetAttribLocation(gProgram, "a_Color");
   gPositionHandle = glGetAttribLocation(gProgram, "a_Position"); // get handle to vertex shader's vPosition member
   glEnableVertexAttribArray(gColorHandle); // Enable a handle to the colors - since this is the only one used, keep it enabled all the time
   glEnableVertexAttribArray(gPositionHandle); // Enable a handle to the vertices - since this is the only one used, keep it enabled all the time

   glViewport(0, 0, width, height);
   glEnable(GL_SCISSOR_TEST);
   glDisable(GL_CULL_FACE);
   glDisable(GL_DEPTH_TEST);
   glEnable(GL_BLEND); // enable color alpha channel
   glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
   
   realAppH = (*env)->CallStaticIntMethod(env, applicationClass, jgetHeight);
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
   return true;
}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
   screen->pitch = screen->screenW * screen->bpp / 8;
   screen->pixels = xmalloc(screen->screenW * screen->screenH * 4);
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

////////////////////////////// OPEN GL 2 //////////////////////////////////

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
   coords[2] = 0;

   colors[0] = (GLfloat)pc.r / (GLfloat)255;
   colors[1] = (GLfloat)pc.g / (GLfloat)255;
   colors[2] = (GLfloat)pc.b / (GLfloat)255;
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
   coords[2] = coords[5] = 0;

   colors[0] = colors[4] = (GLfloat)pc.r / (GLfloat)255;
   colors[1] = colors[5] = (GLfloat)pc.g / (GLfloat)255;
   colors[2] = colors[6] = (GLfloat)pc.b / (GLfloat)255;
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
   w--; h--;
   coords[0] = x;
   coords[1] = y;
   coords[2] = 0;
   coords[3] = x;
   coords[4] = y+h;
   coords[5] = 0;
   coords[6] = x+w;
   coords[7] = y+h;
   coords[8] = 0;
   coords[9] = x+w;
   coords[10] = y;
   coords[11] = 0;

   colors[0] = colors[4] = colors[8]  = colors[12] = colors[16] = colors[20] = (GLfloat)pc.r / (GLfloat)255;
   colors[1] = colors[5] = colors[9]  = colors[13] = colors[17] = colors[21] = (GLfloat)pc.g / (GLfloat)255;
   colors[2] = colors[6] = colors[10] = colors[14] = colors[18] = colors[22] = (GLfloat)pc.b / (GLfloat)255;
   colors[3] = colors[7] = colors[11] = colors[15] = colors[19] = colors[23] = 1;
   glVertexAttribPointer(gColorHandle, 4*6, GL_FLOAT, GL_FALSE, 4 * sizeof(float), colors);
   glVertexAttribPointer(gPositionHandle, COORDS_PER_VERTEX, GL_FLOAT, GL_FALSE, COORDS_PER_VERTEX * sizeof(float), coords); // Prepare the triangle coordinate data
   glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_SHORT, rectOrder); // GL_LINES, GL_TRIANGLES, GL_POINTS
}
