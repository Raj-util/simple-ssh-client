package fi.jpalomaki.ssh;

import fi.jpalomaki.ssh.util.Assert;

/**
 * Abstraction for a remote user, host and port. Immutable.
 * 
 * Think <code>-p &lt;port&gt; &lt;user&gt;@&lt;host&gt;</code>.
 * 
 * @author jpalomaki
 */
public final class UserAtHost {
    
    public final String user;
    public final String host;
    public final int port;
    
    /**
     * Constructs a new {@link UserAtHost} with default port (22).
     */
    public UserAtHost(String user, String host) {
        this(user, host, 22);
    }
    
    /**
     * Constructs a new {@link UserAtHost} with the given parameters.
     * 
     * @param user User, not <code>null</code> or empty
     * @param host Host, not <code>null</code> or empty
     * @param port Port, integer between 1 and 65535 (inclusive)
     */
    public UserAtHost(String user, String host, int port) {
        Assert.hasText(user, "User must not be null or empty");
        Assert.hasText(host, "Host must not be null or empty");
        Assert.isTrue(port >= 1 && port <= 65535, "Port must be >= 1 and <= 65535");
        this.user = user;
        this.host = host;
        this.port = port;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String hashCode = Integer.toHexString(hashCode());
        String instance = getClass().getSimpleName() + "@" + hashCode;
        return String.format("%s [user=%s,host=%s,port=%s]", instance, user, host, port);
    }
}
