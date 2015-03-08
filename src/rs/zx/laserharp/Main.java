package rs.zx.laserharp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.MidiChannel;

import gnu.io.CommPortIdentifier; 
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent; 
import gnu.io.SerialPortEventListener; 

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.ResourceBundle;


public class Main extends Application implements SerialPortEventListener {
	SerialPort serialPort;

	private static final String portName = "COM3";
	
	private BufferedReader input;
	private OutputStream output;
	
	private static final int TIME_OUT = 2000;
	private static final int DATA_RATE = 9600;
	private static final int SLEEP_CONST = 100;
	
	private int offset = 21;
	
	@FXML private Pane n0;
	@FXML private Pane n1;
	@FXML private Pane n2;
	@FXML private Pane n3;
	@FXML private Pane n4;
	@FXML private Pane n5;
	@FXML private Pane n6;
	
	@FXML private Pane s0;
	@FXML private Pane s1;
	@FXML private Pane s2;
	@FXML private Pane s3;
	@FXML private Pane s4;
	@FXML private Pane s5;
	@FXML private Pane s6;
	
	private Pane arrPane[];
	private Pane arrScale[];
	
	public void begin() {
        System.setProperty("gnu.io.rxtx.SerialPorts", "COM3");

		CommPortIdentifier portId = null;
		Enumeration<?> portEnum = CommPortIdentifier.getPortIdentifiers();

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
				
				final MidiChannel[] channels = synth.getChannels();
				
				String cmd = input.readLine();
				
				final int note = Character.digit(cmd.charAt(0), 10);
				
				if(cmd.charAt(1) == '1') {
					channels[0].noteOn(39+offset+2*note, 127);
					blink(note, 1);
					Thread.sleep(SLEEP_CONST);				
					
				} else if(cmd.charAt(1) == '0') { 
					channels[0].noteOff(39+offset+2*note);
					blink(note, 0);
				}				

			} catch (Exception e) {
				System.out.println(e);
			}
		}
	}
	
	public void setScale(String scale) {
		final int sc = Character.digit(scale.charAt(1), 10);
		
		arrScale[offset/7].setEffect(new Glow(0.0));
		
		offset = 7*sc;
		arrScale[sc].setEffect(new Glow(1.0));		
	}
	
	@FXML public void hoverin(MouseEvent e) {
		if(!((Node) e.getSource()).getId().equals("s" + offset/7)) 
			((Node) e.getSource()).setEffect(new Glow(1.0));
    }

    @FXML public void hoverout(MouseEvent e) {
    	if(!((Node) e.getSource()).getId().equals("s" + offset/7)) 
    		((Node) e.getSource()).setEffect(new Glow(0.0));
    }
    
    @FXML public void changeScale(MouseEvent e) {
    	setScale(((Node) e.getSource()).getId());
    }
    
    public void blink(int note, int cmd) {
    	Platform.runLater(() -> {
    		arrPane[note].setEffect(new Glow(cmd));
    	}); 
    }
    
	@Override
	public void start(Stage primaryStage) {
		begin();
		
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("gf.fxml"));
			loader.setController(this);
			
			Parent root = (Parent) loader.load();
			
			Scene scene = new Scene(root,600,330);
		
			primaryStage.setScene(scene);
			primaryStage.show();
			
			
			arrPane = new Pane[] {n0, n1, n2, n3, n4, n5, n6};
			arrScale = new Pane[] {s0, s1, s2, s3, s4, s5, s6};

			s3.setEffect(new Glow(1.0));
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void main(String[] args) {		
		launch(args);		
	}
}