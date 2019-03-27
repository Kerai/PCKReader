package icedev.pck;

import java.io.*;

public class LERAF extends RandomAccessFile
{
    public LERAF(final File file, final String s) throws IOException {
        super(file, s);
    }
    
    public final char readLEShort() throws IOException {
        final int read = this.read();
        final int read2 = this.read();
        if ((read | read2) < 0) {
            throw new EOFException();
        }
        return (char)(read + (read2 << 8));
    }
    
    public final int readLE3Bytes() throws IOException {
        final int read = this.read();
        final int read2 = this.read();
        final int read3 = this.read();
        if ((read | read2 | read3) < 0) {
            throw new EOFException();
        }
        return read + (read2 << 8) + (read3 << 16);
    }
    
    public final int readLEInt() throws IOException {
        final int read = this.read();
        final int read2 = this.read();
        final int read3 = this.read();
        final int read4 = this.read();
        if ((read | read2 | read3 | read4) < 0) {
            throw new EOFException();
        }
        return read + (read2 << 8) + (read3 << 16) + (read4 << 24);
    }
}
