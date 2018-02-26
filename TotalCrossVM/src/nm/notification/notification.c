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



#include "tcvm.h"

#if defined(ANDROID)
#include "android/notification_c.h"
#elif defined (darwin)
Err NmNotify(TCObject title, TCObject text);
#endif

//////////////////////////////////////////////////////////////////////////
TC_API void tnNM_notify_n(NMParams p) // totalcross/notification/NotificationManager public void notify(Notification notification);
{
   TCObject notificationManagerObj = p->obj[0];
   TCObject notificationObj = p->obj[1];
   TCObject titleObj = Notification_title(notificationObj);
   TCObject textObj = Notification_text(notificationObj);
   
#if defined (ANDROID) || defined (darwin)
	NmNotify(titleObj, textObj);
#endif
}
