package io.left.hellomesh;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashSet;

import io.left.rightmesh.android.AndroidMeshManager;
import io.left.rightmesh.android.MeshService;
import io.left.rightmesh.id.MeshId;
import io.left.rightmesh.mesh.MeshManager;
import io.left.rightmesh.mesh.MeshManager.PeerChangedEvent;
import io.left.rightmesh.mesh.MeshManager.RightMeshEvent;
import io.left.rightmesh.mesh.MeshStateListener;
import io.left.rightmesh.util.MeshUtility;
import io.left.rightmesh.util.RightMeshException;

import static io.left.rightmesh.mesh.MeshManager.DATA_RECEIVED;
import static io.left.rightmesh.mesh.MeshManager.PEER_CHANGED;
import static io.left.rightmesh.mesh.MeshManager.REMOVED;

public class MainActivity extends Activity implements MeshStateListener {
    // TODO: this port must match the port assigned, on developer.rightmesh.io, to your key
    private static final int HELLO_PORT = 9876;

    // MeshManager instance - interface to the mesh network.
    AndroidMeshManager meshManager = null;

    // Set to keep track of peers connected to the mesh.
    HashSet<MeshId> users = new HashSet<>();

    /**
     * Called when app first opens, initializes {@link AndroidMeshManager} reference (which will
     * start the {@link MeshService} if it isn't already running.
     *
     * @param savedInstanceState passed from operating system
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: when testing, we suggest using rightmesh-library-dev in app:build.gradle,
        // and specifying a pattern as the third argument to this call. This will isolate
        // your devices so they won't try to connect to the network of the developer sitting
        // beside you :D
        meshManager = AndroidMeshManager.getInstance(
                MainActivity.this,
                MainActivity.this);
    }

    /**
     * Called when activity is on screen.
     */
    @Override
    protected void onResume() {
        try {
            super.onResume();
            meshManager.resume();
        } catch (RightMeshException.RightMeshServiceDisconnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when the app is being closed (not just navigated away from). Shuts down
     * the {@link AndroidMeshManager} instance.
     */
    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();
            meshManager.stop();
        } catch (RightMeshException.RightMeshServiceDisconnectedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called by the {@link MeshService} when the mesh state changes. Initializes mesh connection
     * on first call.
     *
     * @param uuid our own user id on first detecting
     * @param state state which indicates SUCCESS or an error code
     */
    @Override
    public void meshStateChanged(MeshId uuid, int state) {
        if (state == MeshStateListener.SUCCESS) {
            try {
                // Binds this app to MESH_PORT.
                // This app will now receive all events generated on that port.
                meshManager.bind(HELLO_PORT);

                // Subscribes handlers to receive events from the mesh.
                meshManager.on(DATA_RECEIVED, this::handleDataReceived);
                meshManager.on(PEER_CHANGED, this::handlePeerChanged);

                // Enable buttons now that mesh is connected.
                Button btnConfigure = findViewById(R.id.btnConfigure);
                Button btnSend = findViewById(R.id.btnHello);
                btnConfigure.setEnabled(true);
                btnSend.setEnabled(true);
            } catch (RightMeshException e) {
                String status = "Error initializing the library" + e.toString();
                Toast.makeText(getApplicationContext(), status, Toast.LENGTH_SHORT).show();
                TextView txtStatus = findViewById(R.id.txtStatus);
                txtStatus.setText(status);
                return;
            }
        }

        // Update display on successful calls (i.e. not FAILURE or DISABLED).
        if (state == MeshStateListener.SUCCESS || state == MeshStateListener.RESUME) {
            updateStatus();
        }
    }

    /**
     * Update the {@link TextView} with a list of all peers.
     */
    private void updateStatus() {
        StringBuilder status = new StringBuilder("uuid: " + meshManager.getUuid().toString() + "\npeers:\n");
        for (MeshId user : users) {
            status.append(user.toString()).append("\n");
        }
        TextView txtStatus = findViewById(R.id.txtStatus);
        txtStatus.setText(status.toString());
    }

    /**
     * Handles incoming data events from the mesh - toasts the contents of the data.
     *
     * @param e event object from mesh
     */
    private void handleDataReceived(RightMeshEvent e) {
        final MeshManager.DataReceivedEvent event = (MeshManager.DataReceivedEvent) e;

        runOnUiThread(() -> {
            // Toast data contents.
            String message = new String(event.data);
            Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();

            // Play a notification.
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone r = RingtoneManager.getRingtone(MainActivity.this, notification);
            r.play();
        });
    }

    /**
     * Handles peer update events from the mesh - maintains a list of peers and updates the display.
     *
     * @param e event object from mesh
     */
    private void handlePeerChanged(RightMeshEvent e) {
        // Update peer list.
        PeerChangedEvent event = (PeerChangedEvent) e;
        if (event.state != REMOVED && !users.contains(event.peerUuid)) {
            users.add(event.peerUuid);
        } else if (event.state == REMOVED){
            users.remove(event.peerUuid);
        }

        // Update display.
        runOnUiThread(this::updateStatus);
    }

    /**
     * Sends "hello" to all known peers.
     *
     * @param v calling view
     */
    public void sendHello(View v) throws RightMeshException {
        for(MeshId receiver : users) {
            String msg = "Hello to: " + receiver + " from" + meshManager.getUuid();
            MeshUtility.Log(this.getClass().getCanonicalName(), "MSG: " + msg);
            byte[] testData = msg.getBytes();
            meshManager.sendDataReliable(receiver, HELLO_PORT, testData);
        }
    }

    /**
     * Open mesh settings screen.
     *
     * @param v calling view
     */
    public void configure(View v)
    {
        try {
            meshManager.showSettingsActivity();
        } catch(RightMeshException ex) {
            MeshUtility.Log(this.getClass().getCanonicalName(), "Service not connected");
        }
    }
}