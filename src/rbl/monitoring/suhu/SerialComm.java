package rbl.monitoring.suhu;

import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.TooManyListenersException;
import javax.swing.JOptionPane;

/**
 *
 * @author Puradidjaja
 */
public class SerialComm implements SerialPortEventListener {

    GrafikFrame grafikFrame = null;
    private Enumeration ports = null;
    private HashMap portMap = new HashMap();
    private CommPortIdentifier portIdentifier = null;
    private SerialPort serialPort = null;
    private InputStream serialInput = null;
    private boolean serialConnected = false;
    String statusSerialPort = "";
    int dataADC = 0;
    double suhu = 0;

    public SerialComm(GrafikFrame grafikFrame) {
        this.grafikFrame = grafikFrame;

    }

    public void cekSerialPort() {
        grafikFrame.listPortComboBox.removeAllItems();
        ports = CommPortIdentifier.getPortIdentifiers();
        while (ports.hasMoreElements()) {
            CommPortIdentifier curPort = (CommPortIdentifier) ports.nextElement();
            if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                grafikFrame.listPortComboBox.addItem(curPort.getName());
                portMap.put(curPort.getName(), curPort);
            }
        }
    }

    final public boolean getConnected() {
        return serialConnected;
    }

    public void setConnected(boolean serialConnected) {
        this.serialConnected = serialConnected;
    }

    public void connect() {
        String selectedPort = (String) grafikFrame.listPortComboBox.getSelectedItem();
        portIdentifier = (CommPortIdentifier) portMap.get(selectedPort);
        CommPort commPort = null;
        try {
            commPort = portIdentifier.open("ControlPanel", 2000);
            serialPort = (SerialPort) commPort;
            setConnected(true);
            setSerialPortParameters();
            statusSerialPort = selectedPort + " opened successfully.";
            JOptionPane.showMessageDialog(null, statusSerialPort);
            grafikFrame.statusLabel.setText("Serial Port Status: Connected");
        } catch (PortInUseException e) {
            statusSerialPort = selectedPort + " is in use. (" + e.toString() + ")";
            JOptionPane.showMessageDialog(null, statusSerialPort);
        } catch (Exception e) {
            statusSerialPort = "Failed to open " + selectedPort + "(" + e.toString() + ")";
            JOptionPane.showMessageDialog(null, statusSerialPort);
        }
    }

    public void disconnect() {
        try {
            serialPort.removeEventListener();
            serialPort.close();
            serialInput.close();
            setConnected(false);
            grafikFrame.serialInputTextField.setText("");
            grafikFrame.suhuLabel.setText("0.00 °C");
            statusSerialPort = "PORT closed successfully";
            JOptionPane.showMessageDialog(null, statusSerialPort);
            grafikFrame.statusLabel.setText("Serial Port Status: Disconnect");
        } catch (Exception e) {
            statusSerialPort = "Failed to close " + serialPort.getName() + "(" + e.toString() + ")";
            JOptionPane.showMessageDialog(null, statusSerialPort);
        }
    }

    public boolean initIOStream() {
        boolean successful = false;
        try {
            serialInput = serialPort.getInputStream();
            successful = true;
            return successful;
        } catch (IOException e) {
            statusSerialPort = "I/O Streams failed to open. (" + e.toString() + ")";
            JOptionPane.showMessageDialog(null, statusSerialPort);
            return successful;
        }
    }

    public void initListener() {
        try {
            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);            
        } catch (TooManyListenersException e) {
            JOptionPane.showMessageDialog(null, e.toString());
        }
    }

    private void setSerialPortParameters() throws IOException {    
        int baudRate = 115200;   
        try {           
            serialPort.setSerialPortParams(baudRate,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,
                                           SerialPort.PARITY_NONE);
            serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        } catch (UnsupportedCommOperationException ex) {        
            throw new IOException("Unsupported serial port parameter");           
        }
        
    }

    public double getSuhu() {
        return suhu;
    }

    public void setSuhu(double suhu) {
        this.suhu = suhu;
    }

    public void serialEvent(SerialPortEvent evt) {
        char dataSerial = 0;       
        int dataDigital;

        if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try {               
                dataSerial = (char) serialInput.read();
                if (dataSerial != 0) {
                    dataDigital = (int) dataSerial; 
                    grafikFrame.serialInputTextField.setText(String.valueOf(dataDigital));
                    suhu = (double) dataDigital * 500.0 / 255.0;
                    NumberFormat n = NumberFormat.getInstance();
                    n.setMaximumFractionDigits(2);
                    grafikFrame.suhuLabel.setText(n.format(suhu) + " °C");
                }

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, ex.toString());
            }
        }
    }
}
