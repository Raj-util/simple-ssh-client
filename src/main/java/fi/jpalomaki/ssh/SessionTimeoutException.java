package fi.jpalomaki.ssh;

/**
 * Unchecked exception thrown when an SSH session times out.
 */
public final class SessionTimeoutException extends SshClientException {

    private static final long serialVersionUID = 1L;

    public SessionTimeoutException(long timeout) {
        super("Session timeout (" + timeout + " ms) exceeded");
    }
}
