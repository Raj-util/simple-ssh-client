package fi.jpalomaki.ssh;

import static fi.jpalomaki.ssh.TimeUnit.DAY;
import static fi.jpalomaki.ssh.TimeUnit.HOUR;
import static fi.jpalomaki.ssh.TimeUnit.MILLISECOND;
import static fi.jpalomaki.ssh.TimeUnit.MINUTE;
import static fi.jpalomaki.ssh.TimeUnit.SECOND;
import static java.util.Arrays.asList;

/**
 * Unchecked exception thrown when an SSH session times out.
 */
public final class SessionTimeoutException extends SshClientException {

    private static final long serialVersionUID = 1L;

    public SessionTimeoutException(long timeout) {
        super("Session timeout (" + humanReadable(timeout) + ") exceeded");
    }

    private static String humanReadable(long millis) {
        if (millis < 0L) {
            throw new IllegalArgumentException("Duration must be >= 0 ms");
        }
        StringBuilder result = new StringBuilder();
        for (TimeUnit unit : asList(DAY, HOUR, MINUTE, SECOND, MILLISECOND)) {
            long count = millis / unit.asMillis;
            if (count > 0L) {
                millis -= (count * unit.asMillis);
                if (result.length() > 0) {
                    result.append(" ");
                }
                result.append(count + unit.suffix);
            }
        }
        return result.length() > 0 ? result.toString() : "0 ms";
    }
}

enum TimeUnit {

    MILLISECOND(1, "ms"),
    SECOND(1000 * MILLISECOND.asMillis, "s"),
    MINUTE(60 * SECOND.asMillis, "m"),
    HOUR(60 * MINUTE.asMillis, "h"),
    DAY(24 * HOUR.asMillis, "d");

    public final long asMillis;
    public final String suffix;

    private TimeUnit(long asMillis, String suffix) {
        this.asMillis = asMillis;
        this.suffix = suffix;
    }
}
