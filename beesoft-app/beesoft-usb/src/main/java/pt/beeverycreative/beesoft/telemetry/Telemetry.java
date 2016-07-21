package pt.beeverycreative.beesoft.telemetry;

import com.google.gson.Gson;
import com.microsoft.azure.iothub.DeviceClient;
import com.microsoft.azure.iothub.IotHubClientProtocol;
import com.microsoft.azure.iothub.IotHubEventCallback;
import com.microsoft.azure.iothub.IotHubStatusCode;
import com.microsoft.azure.iothub.Message;
import java.io.IOException;
import java.net.URISyntaxException;
import replicatorg.app.Base;

/**
 *
 * @author jgrego
 */
public final class Telemetry {

    private static Telemetry instance = null;
    private final String connectionString = "HostName=IOT-NEW.azure-devices.net;DeviceId=beeconnect-2111500033;SharedAccessKey=hCzl1dgIHNnHIZNBQ47Wlq9bStPoPbp/NNeX4TcV+cc=";
    private final IotHubClientProtocol protocol = IotHubClientProtocol.AMQPS;
    private final DeviceClient client;
    private final EventCallback responseReceivedCallback = new EventCallback();

    private Telemetry() {

        DeviceClient tempClient;

        try {
            tempClient = new DeviceClient(connectionString, protocol);
        } catch (URISyntaxException | IllegalArgumentException ex) {
            tempClient = null;
            Base.writeLog(ex.getClass().getSimpleName() + " while initializing DeviceClient object: " + ex.getMessage(), this.getClass());
        }

        client = tempClient;
        if (client != null) {
            try {
                client.open();
            } catch (IOException | IllegalStateException ex) {
                Base.writeLog(ex.getClass().getSimpleName() + " while opening DeviceClient object: " + ex.getMessage(), this.getClass());
            }
        }

    }
    
    public static synchronized Telemetry getInstance() {
        if (instance == null) {
            instance = new Telemetry();
        }

        return instance;
    }

    public void sendMessage(final TelemetryMessage data) {
        final String msgStr;
        
        if (client != null) {
            Gson gson = new Gson();
            msgStr = gson.toJson(data);
            client.sendEventAsync(new Message(msgStr), responseReceivedCallback, null);
        }
    }

    private class EventCallback implements IotHubEventCallback {

        @Override
        public void execute(IotHubStatusCode status, Object context) {
            System.out.println("IoT Hub responded to message with status: " + status.name());

            if (context != null) {
                synchronized (context) {
                    context.notify();
                }
            }
        }
    }
}
