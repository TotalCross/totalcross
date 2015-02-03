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



#ifndef DEMO_H
#define DEMO_H

/// Called at startup to see how much time we have
int32 checkDemo();
/// Updates the demo time
bool updateDemoTime();
/// Gets the virtual machine compilation date in format YYYYMMDD, xored by 12341234 (defined below)
int32 getCompilationDate();
#define COMPILATION_MASK 12341234

#endif
