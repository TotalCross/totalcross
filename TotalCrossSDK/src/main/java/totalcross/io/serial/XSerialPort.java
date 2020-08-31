package totalcross.io.serial;

import java.io.IOException;
import java.util.function.Function;

import totalcross.io.XStream;

public class XSerialPort extends XStream {
	
	private int baudRate;
	private boolean breakEnabled;
	private int dataBits;
	private boolean dataTerminalReady;
	private int flowControl;
	private int parity;
	private boolean requestToSend;
	private int stopBits;
	
	public XSerialPort(String portName) {
		
	}
	
	public String portName() {
		return null;
	}
	
	public XSerialPort setPortName(String portName) {
		return this;
	}
	
	public XSerialPort open(int openMode) {
		return this;
	}
	
	/**
	* throws error on BOTH
	*/
	public int baudRate(int direction) {
		if(direction == Direction.BOTH)
			throw new IllegalArgumentException("Argument direction must be INPUT (0) or OUPUT (1)");
		return this.baudRate;
	}
	
	/**
	* baudRate must be positive
	*
	* @param direction
	* @param baudRate
	* @return
	*/
	public XSerialPort setBaudRate(int direction, int baudRate) {
		if(baudRate <= 0)
			throw new IllegalArgumentException("Argument baudRate must be greater than 0"); 
		this.baudRate = baudRate;
		return this;
	}
	
	public boolean	isBreakEnabled() {
		return this.breakEnabled;
	}
	
	public XSerialPort setBreakEnabled(boolean breakEnabled) {
		this.breakEnabled = breakEnabled;
		return this;
	}
	
	public int dataBits() {
		return this.dataBits;
	}
	
	public XSerialPort setDataBits(int dataBits) {
		if(dataBits <= 5 && dataBits >= 8)
			throw new IllegalArgumentException("Argument dataBits must be 5,6,7 or 8");
		this.dataBits = dataBits;
		return this;
	}
	
	public boolean	isDataTerminalReady() {
		return false;
	}
	
	public XSerialPort setDataTerminalReady(boolean set) {
		return this;
	}
	
	public int	flowControl() {
		return 0;
	}
	
	public XSerialPort	setFlowControl(int flowControl) {
		return this;
	}
	
	public int	parity() {
		return 0;
	}
	
	public XSerialPort	setParity(int parity) {
		return this;
	}
	
	public boolean	isRequestToSend() {
		return false;
	}
	
	public XSerialPort	setRequestToSend(boolean set) {
		return this;
	}
	
	public int	stopBits() {
		return 0;
	}
	
	public XSerialPort	setStopBits(int stopBits) {
		return this;
	}
	
	@Override
	public void close() throws IOException {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public CharSequence readLine() throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public char[] readLine(long maxSize) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public long read(byte[] buf, long start, long count) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public long write(byte[] buf, long start, long count) throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
}