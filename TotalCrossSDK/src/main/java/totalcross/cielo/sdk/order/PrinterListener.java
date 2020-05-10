// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.cielo.sdk.order;

public interface PrinterListener {
    public void onPrintSuccess(int printedLines);

    public void onError(Throwable e);

    public void onWithoutPaper();
}
