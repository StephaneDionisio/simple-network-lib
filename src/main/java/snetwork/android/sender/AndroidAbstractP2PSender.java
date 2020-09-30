package snetwork.android.sender;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;
import snetwork.sender.AbstractP2PSender;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;

/**
 * Class that can be used to make an Android device as a sender. It will
 * search peers in the local network (wifi) or in the list of the connected devices (hotspot).
 */
public abstract class AndroidAbstractP2PSender extends AbstractP2PSender {

    /**
     * The owner app.
     */
    private final Application owner;

    /**
     * <i><b>AbstractP2PSender</b></i>
     *
     * <pre> protected AbstractP2PSender(int port) </pre>
     *
     * Constructor of {@link AbstractP2PSender}.
     * @param port    the port used.
     * @param timeout the socket timeout in milliseconds for the search of peers.
     *                timeout <= 0 for no timeout.
     * @param owner the application.
     */
    protected AndroidAbstractP2PSender(int port, int timeout, Application owner) {
        super(port, timeout);
        this.owner = owner;
    }

    @Override
    protected boolean searchPeer() {
        WifiManager wifi = (WifiManager) owner
                .getBaseContext()
                .getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);

        boolean isHotspot = wifi == null || !wifi.isWifiEnabled();
        if(wifi == null)
            throw new RuntimeException("getSystemService(Context.WIFI_SERVICE) returns null.");

        // Broadcast
        if(!isHotspot)
            return super.searchPeer();

        // else /proc/net/arp -> list on connected devices

        ArrayList<InetAddress> addresses = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(new File("/proc/net/arp"))));

            String line;
            // skip line 1
            reader.readLine();

            while ((line = reader.readLine()) != null)
                addresses.add(InetAddress.getByName(line.split(" ")[0]));

        } catch (IOException e) {
            // FileNotFoundException => Not supported by this phone.
            e.printStackTrace();
            return false;
        }

        return searchPeer(addresses);
    }

}
