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

bool graphicsStartup(ScreenSurface screen, int16 appTczAttr)
{
   GetWidthAndHeight(&lastW, &lastH);
   screen->bpp = 32;
   screen->screenX = screen->screenY = 0;
   screen->screenW = lastW;
   screen->screenH = lastH;
   screen->hRes = ascrHRes;
   screen->vRes = ascrVRes;
   SetupDX();
   return true;
}

void graphicsDestroy(ScreenSurface screen, bool isScreenChange)
{
   xfree(screen->pixels);
   ReleaseDX();
}

void graphicsUpdateScreen(Context currentContext, ScreenSurface screen)
{

}

void privateScreenChange(int32 w, int32 h)
{

}

bool graphicsCreateScreenSurface(ScreenSurface screen)
{
   screen->pitch = screen->screenW * screen->bpp / 8;
   screen->pixels = xmalloc(screen->screenW * screen->screenH << 2);
   return screen->pixels != null;
}