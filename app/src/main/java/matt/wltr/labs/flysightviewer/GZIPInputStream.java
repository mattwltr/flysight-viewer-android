package matt.wltr.labs.flysightviewer;

import java.io.IOException;
import java.io.InputStream;

public class GZIPInputStream extends java.util.zip.GZIPInputStream {

    private long compressedLength;

    public GZIPInputStream(InputStream in, long compressedLength) throws IOException {
        super(in);
        this.compressedLength = compressedLength;
    }

    public long getCompressedLength() {
        return compressedLength;
    }
}
