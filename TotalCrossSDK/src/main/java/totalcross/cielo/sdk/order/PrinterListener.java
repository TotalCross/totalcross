// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.cielo.sdk.order;

public interface PrinterListener {
    public void onPrintSuccess(int printedLines);

    public void onError(Throwable e);

    public void onWithoutPaper();
}
