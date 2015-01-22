package dk.dtu.dronestreamer;

import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

public class UDPsender {

    private String address;
    private int port;
    private DatagramSocket socketUDP;
    private InetAddress inet;

    private static String DEBUG_TAG = "UDPsender";

    public UDPsender(String ip, int port){
        this.address = ip;
        this.port = port;
        try {
            socketUDP = new DatagramSocket();
            inet = InetAddress.getByName(address);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void sendChunks(byte[] data, int x){
        Log.v(DEBUG_TAG, "Task started");
        //Log.v(DEBUG_TAG, String.format("Data: %s", new String(data)));

        try {
            int len = data.length;
            for (int i = 0; i < len - x + 1; i += x) {
                byte[] load = Arrays.copyOfRange(data, i, i + x);
                socketUDP.send(new DatagramPacket(load, load.length, inet, port));
            }

            if (len % x != 0) {
                byte[] load = Arrays.copyOfRange(data, len - len % x, len);
                socketUDP.send(new DatagramPacket(load, load.length, inet, port));
            }

            Log.w(DEBUG_TAG, "Works fine!");
        } catch (Exception e) {
            Log.w(DEBUG_TAG, "Catched here!");
            e.printStackTrace();
        }
        Log.v(DEBUG_TAG, "Task finished");
    }

    public void sendData(byte[] data) {
        Log.v(DEBUG_TAG, "Task started");
        Log.v(DEBUG_TAG, String.format("Data: %s", new String(data)));

        try {
            DatagramPacket p = new DatagramPacket(data, data.length, inet, port);
            socketUDP.send(p);
            Log.w(DEBUG_TAG, "Works fine!");
        } catch (Exception e) {
            Log.w(DEBUG_TAG, "Catched here!");
            e.printStackTrace();
        }
    }
}
