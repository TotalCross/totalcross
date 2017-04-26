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


#if defined (darwin)
#ifdef __cplusplus
extern "C" {
#endif
    
    int iphone_dialNumber(char* number);
    
#ifdef __cplusplus
};
#endif
#endif // darwin


static void dialNumber(CharP number)
{
#ifdef darwin
    iphone_dialNumber(number);
#endif
}

/* static void hangup()
{
}*/
