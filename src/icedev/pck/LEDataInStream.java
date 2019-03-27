package icedev.pck;

import java.io.*;

public class LEDataInStream extends DataInputStream
{
    public LEDataInStream(final InputStream inputStream) {
        super(inputStream);
    }
    
    public final int readShortLE() throws IOException {
        final int read = this.in.read();
        final int read2 = this.in.read();
        if ((read | read2) < 0) {
            throw new EOFException();
        }
        return read + (read2 << 8);
    }
    
    public void skipAll(int i) throws IOException {
        while (i > 0) {
            final int n = (int)this.in.skip(i);
            if (n < 0) {
                return;
            }
            i -= n;
        }
    }
    
    public final int readIntLE() throws IOException {
        final int b1 = this.in.read();
        final int b2 = this.in.read();
        final int b3 = this.in.read();
        final int b4 = this.in.read();
        if ((b1 | b2 | b3 | b4) < 0) {
            throw new EOFException();
        }
        return b1 + (b2 << 8) + (b3 << 16) + (b4 << 24);
    }
}
