/*********************************************************************************
 *  TotalCross Software Development Kit                                          *
 *  Copyright (C) 2000-2012 SuperWaba Ltda.                                      *
 *  All Rights Reserved                                                          *
 *                                                                               *
 *  This library and virtual machine is distributed in the hope that it will     *
 *  be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of    *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         *
 *                                                                               *
 *  This file is covered by the GNU LESSER GENERAL PUBLIC LICENSE VERSION 3.0    *
 *  A copy of this license is located in file license.txt at the root of this    *
 *  SDK or can be downloaded here:                                               *
 *  http://www.gnu.org/licenses/lgpl-3.0.txt                                     *
 *                                                                               *
 *********************************************************************************/



package totalcross.xml.soap;

public class SOAPException extends Exception
{
	private Throwable cause;

	public SOAPException(String string)
	{
		super(string);
	}

	public SOAPException(Throwable cause)
	{
		super(cause == null ? null : cause.getMessage());
		this.cause = cause;
	}

	public void printStackTrace()
	{
		if (cause != null)
			cause.printStackTrace();
		else
			super.printStackTrace();
	}
	
	public Throwable getCause()
	{
		return cause;
	}
}
