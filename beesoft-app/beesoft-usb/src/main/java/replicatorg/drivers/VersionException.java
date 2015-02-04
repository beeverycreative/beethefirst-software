/**
 * 
 */
package replicatorg.drivers;

/**
 * @author phooky
 * 
 * 
 * RT: sorry phooky
 *
 */
public class VersionException extends RuntimeException {

    private Version detected;
    String message = "default message";

    public VersionException(Version detected) {
        this.detected = detected;
    }

    public VersionException() {
    }

    public VersionException(String message) {
        this.message = message;
    }

    public Version getDetected() {
        return detected;
    }

    public String getMessage() {
        return "Generic Version Exception on version " + getDetected().toString();
    }
}
