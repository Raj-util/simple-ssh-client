package fi.jpalomaki.ssh;

/**
 * Unchecked exception thrown upon SSH client errors.
 * 
 * @author jpalomaki
 */
public class SshClientException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    /**
     * Constructs a new {@link SshClientException} with the given message.
     */
    public SshClientException(String message) {
        super(message);
    }
    
    /**
     * Constructs a new {@link SshClientException} with the given message and cause.
     */
    public SshClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
