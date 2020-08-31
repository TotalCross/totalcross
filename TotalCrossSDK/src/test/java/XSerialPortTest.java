import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import static junit.framework.Assert.*;

import totalcross.io.serial.*;

public class XSerialPortTest {
    XSerialPort port = new XSerialPort("my-serial.txt");

    @Test
    public void itShouldFillDataBitsFieldWithValidValue() {
        port.setDataBits(DataBits.DATA_5);
        assertEquals("When `DataBits` with 'define' valid case", 5, port.dataBits());
        port.setDataBits(5);
        assertEquals("When `DataBits` with 'int' valid case", 5, port.dataBits());
        assertThrows(IllegalArgumentException.class, () -> port.setDataBits(10), "When `DataBits` 'int' invalid case (over range)");
        assertThrows(IllegalArgumentException.class, () -> port.setDataBits(3), "When `DataBits` 'int' invalid case (under range)");
    }

    @Test
    public void itShouldFillBaudRateFieldWithValidValue() {
        port.setBaudRate(Direction.OUTPUT, 19200);
        assertEquals("When `BaudRate` 'int' + OUTPUT/OUTPUT valid case", 19200, port.baudRate(Direction.OUTPUT));
        port.setBaudRate(Direction.INPUT, BaudRate.BAUD_38400);
        assertEquals("When `BaudRate` 'define' + INPUT/INPUT valid case", 38400, port.baudRate(Direction.INPUT));
        port.setBaudRate(Direction.OUTPUT, 19200);
        assertEquals("When `BaudRate` 'int' + OUTPUT/INPUT valid case", 9600, port.baudRate(Direction.INPUT));
        port.setBaudRate(Direction.INPUT, BaudRate.BAUD_38400);
        assertEquals("When `BaudRate` 'define' + INPUT/OUTPUT valid case", 9600, port.baudRate(Direction.OUTPUT));
        assertThrows(IllegalArgumentException.class, () -> port.setBaudRate(Direction.OUTPUT, -96000), "When `BaudRate` 'int' invalid case");
    }
 
    @Test
    public void itShouldFillDataTerminalReadyFieldWithValidBoolean() {
        port.setDataTerminalReady(true);
        assertEquals(true, port.isDataTerminalReady());
        port.setDataTerminalReady(false);
        assertEquals(false, port.isDataTerminalReady());
    }

    @Test
    public void itShouldFillFlowControlFieldWithValidValue() {
        port.setFlowControl(1);
        assertEquals("When `FlowControl` 'int' valid case", 1, port.flowControl());
        port.setFlowControl(FlowControl.HARDWARE);
        assertEquals("When `FlowControl` 'define' valid case", 1, port.flowControl());
        assertThrows(IllegalArgumentException.class, () -> port.setFlowControl(3), "When `FlowControl` 'int' invalid case (over range)");
        assertThrows(IllegalArgumentException.class, () -> port.setFlowControl(-1), "When `FlowControl` 'int' invalid case (under range)"); 
    }

    @Test
    public void itShouldFillParityFieldWithValidValue() {
        port.setParity(2);
        assertEquals("When `Parity` 'int' valid case", 2, port.parity());
        port.setParity(Parity.EVEN_PARITY);
        assertEquals("When `Parity` 'define' valid case", 2, port.parity());
        assertThrows(IllegalArgumentException.class, () -> port.setParity(1), "When `Parity` 'int' invalid case (equal to 1)");
        assertThrows(IllegalArgumentException.class, () -> port.setParity(6), "When `Parity` 'int' invalid case (over range)");
        assertThrows(IllegalArgumentException.class, () -> port.setParity(-1), "When `Parity` 'int' invalid case (under range)"); 
    }

    @Test
    public void itShouldFillRequestToSendFieldWithValidBoolean() {
        port.setRequestToSend(true);
        assertEquals(true, port.isRequestToSend());
        port.setRequestToSend(false);
        assertEquals(false, port.isRequestToSend());
    }

    @Test
    public void itShouldFillStopBitsFieldWithValidValue() {
        port.setStopBits(2);
        assertEquals("When `StopBits` 'int' valid case", 2, port.stopBits());
        port.setStopBits(StopBits.TWO_STOP);
        assertEquals("When `StopBits` 'define' valid case", 2, port.stopBits());
        assertThrows(IllegalArgumentException.class, () -> port.setStopBits(4), "When `StopBits` 'int' invalid case (over range)");
        assertThrows(IllegalArgumentException.class, () -> port.setStopBits(-1), "When `StopBits` 'int' invalid case (under range)"); 
    }
}