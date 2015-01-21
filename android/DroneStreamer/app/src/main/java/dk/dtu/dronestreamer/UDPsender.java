package dk.dtu.dronestreamer;

import android.os.AsyncTask;
import android.util.Log;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class UDPsender extends AsyncTask<byte[], Void, String> {

    private String address = "192.168.1.5";
    private int port = 8082;

    private static String DEBUG_TAG = "UDPsender";

    @Override
    protected String doInBackground(byte[]... data) {
        Log.v(DEBUG_TAG, "Task started");
        Log.v(DEBUG_TAG, String.format("Data: %s", data[0].toString()));
        try {
            DatagramSocket socketUDP = new DatagramSocket();
            InetAddress inet = InetAddress.getByName(address);
            DatagramPacket p = new DatagramPacket(data[0], data[0].length, inet, port);
            socketUDP.send(p);
            android.util.Log.w(DEBUG_TAG, "Works fine!");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            android.util.Log.w(DEBUG_TAG, "Catched here!");
            e.printStackTrace();
        }
        Log.v(DEBUG_TAG, "Task finished");
        return "Executed";
    }

    @Override
    protected void onPostExecute(String result) {
    }

    @Override
    protected void onPreExecute() {
    }

    @Override
    protected void onProgressUpdate(Void... values) {
    }
}
