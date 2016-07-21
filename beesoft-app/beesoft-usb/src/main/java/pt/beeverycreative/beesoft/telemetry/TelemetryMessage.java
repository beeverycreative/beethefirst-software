package pt.beeverycreative.beesoft.telemetry;

/**
 *
 * @author jgrego
 */
public class TelemetryMessage implements Runnable {

    private final String time;
    private final String SerialPrinter;
    private final String Print_Message;
    private final String beeserial;

    public TelemetryMessage(final String time, final String SerialPrinter, final String Print_Message, final String beeserial) {
        this.time = time;
        this.SerialPrinter = SerialPrinter;
        this.Print_Message = Print_Message;
        this.beeserial = beeserial;
    }
    
    @Override
    public void run() {
        Telemetry.getInstance().sendMessage(this);
    }
    
}
