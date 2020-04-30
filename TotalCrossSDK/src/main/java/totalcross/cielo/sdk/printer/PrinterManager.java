// Copyright (C) 2019-2020 TotalCross Global Mobile Platform Ltda.
//
// TotalCross Software Development Kit
//
// This library and virtual machine is distributed in the hope that it will
// be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
// SPDX-License-Identifier: LGPL-2.1-only

package totalcross.cielo.sdk.printer;

import java.util.Map;

import com.totalcross.annotations.ReplacedByNativeOnDeploy;

import totalcross.cielo.sdk.order.PrinterListener;
import totalcross.sys.Vm;

/**
 * Usage example:
 * 
 * <pre>
 * HashMap<String, Integer> printerAttributes = new HashMap<>();
 * 
 * printerAttributes.put(PrinterAttributes.KEY_ALIGN, PrinterAttributes.VAL_ALIGN_CENTER);
 * printerAttributes.put(PrinterAttributes.KEY_TYPEFACE, 1);
 * printerAttributes.put(PrinterAttributes.KEY_TEXT_SIZE, 20);
 * 
 * String textToPrint = "TEXT TO PRINT";
 * PrinterManager.getInstance()
 *               .printText(textToPrint, printerAttributes);
 * </pre>
 */
public class PrinterManager {

    private static PrinterManager instance;

    private PrinterManager() {
    }

    public static PrinterManager getInstance() {
        if (instance == null) {
            instance = new PrinterManager();
        }
        return instance;
    }

    static class PrinterListenerInternal {
        PrinterListener printerListener;

        PrinterListenerInternal(PrinterListener printerListener) {
            this.printerListener = printerListener;
        }
    }

    public void printText(String textToPrint, Map<String, Integer> printerAttributes) {
        printText(textToPrint, printerAttributes, null);
    }

    private void printText(String textToPrint, Map<String, Integer> printerAttributes,
            PrinterListener printerListener) {
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, Integer> entry : printerAttributes.entrySet()) {
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            sb.append('&');
        }
        sb.deleteCharAt(sb.length() - 1);

        internalPrintText(
                textToPrint,
                sb.toString(),
                printerListener == null ? null : new PrinterListenerInternal(printerListener));
    }

    @ReplacedByNativeOnDeploy
    private void internalPrintText(String textToPrint, String printerAttributes,
            PrinterListenerInternal printerListener) {
        Vm.debug("PRINT ONLY AVAILABLE ON CIELO DEVICE");
    }
}
