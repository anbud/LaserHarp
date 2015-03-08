package rs.zx.laserharp;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 
import java.util.Enumeration;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;


public class Init implements SerialPortEventListener {
	SerialPort serialPort;

	private static final String portName = "COM3";
	
	private BufferedReader input;
	private OutputStream output;
	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 9600;

	public void initialize() {
        System.setProperty("gnu.io.rxtx.SerialPorts", "COM3");

		CommPortIdentifier portId = null;
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		while (portEnum.hasMoreElements()) {
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			
			if (currPortId.getName().equals(portName))
				portId = currPortId;
		}
		if (portId == null) {
			System.out.println("Could not find COM port.");
			return;
		}

		try {
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public synchronized void close() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				Synthesizer synth = MidiSystem.getSynthesizer();
				synth.open();
				MidiChannel[] channels = synth.getChannels();
				
				String cmd = input.readLine();
				System.out.println("note " + cmd.charAt(0) + " " + (cmd.charAt(1) == '1' ? "on" : "off"));
				
				if(cmd.charAt(1) == '1') {
					channels[0].noteOn(60+2*Character.digit(cmd.charAt(0), 10), 127);
					Thread.sleep(200);
				} else if(cmd.charAt(1) == '0') {
					channels[0].noteOff(60+2*Character.digit(cmd.charAt(0), 10));
				}

			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		new Init().initialize();
		
		new Thread() {
			public void run() {
				try {
					Thread.sleep(1000000);
				} catch (InterruptedException ie) {
					
				}
			}
		}.start();
		
		System.out.println("Started");
	}
}
