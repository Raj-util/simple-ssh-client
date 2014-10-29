package fi.jpalomaki.ssh.util;

import java.io.IOException;
import java.io.OutputStream;

/**
 * An {@link OutputStream} decorator that limits the number of bytes written
 * through it, either by raising an exception or by ignoring the excess bytes.
 *
 * @author jpalomaki
 */
public final class BoundedOutputStream extends OutputStream {

    private final long maxBytes;
    private long bytesWritten = 0L;
    private final OutputStream sink;
    private boolean failOnMaxBytesExceeded;

    public BoundedOutputStream(long maxBytes, OutputStream sink, boolean failOnMaxBytesExceeded) {
        Assert.isTrue(maxBytes > 0L, "Max bytes must be > 0");
        Assert.notNull(sink, "Sink must not be null");
        this.maxBytes = maxBytes;
        this.sink = sink;
        this.failOnMaxBytesExceeded = failOnMaxBytesExceeded;
    }

    @Override
    public void write(int b) throws IOException {
        if (bytesWritten < maxBytes) {
            sink.write(b);
            bytesWritten++;
        } else {
            if (failOnMaxBytesExceeded) {
                throw new IOException("Exceeded max bytes: " + maxBytes);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        sink.flush();
    }

    @Override
    public void close() throws IOException {
        sink.close();
    }
}
