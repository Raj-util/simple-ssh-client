package fi.jpalomaki.ssh;

/**
 * Simple SSH client interface. Thread-safe.
 * 
 * @author jpalomaki
 */
public interface SshClient {
    
    /**
     * Executes the given command as the given user on the given host.
     * 
     * @param command Command to execute, not <code>null</code> or empty
     * @param userAtHost User at host (and port), not <code>null</code>
     * @return Result of running the command, never <code>null</code>
     * @throws SshClientException In case of errors
     */
    Result executeCommand(String command, UserAtHost userAtHost) throws SshClientException;
}
