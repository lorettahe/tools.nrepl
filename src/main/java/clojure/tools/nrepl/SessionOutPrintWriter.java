package clojure.tools.nrepl;

import java.io.Writer;
import java.io.PrintWriter;
import clojure.tools.nrepl.StdOutBuffer;
import clojure.lang.IFn;

public class SessionOutPrintWriter extends java.io.Writer {
    private final StdOutBuffer buf;
    private final IFn outLimit;
    private final IFn flushFn;
    
    public SessionOutPrintWriter(final StdOutBuffer buf, final IFn outLimit, final IFn flushFn) {
        this.buf = buf;
        this.outLimit = outLimit;
        this.flushFn = flushFn;
    }

    @Override
    public void close() {
        flush();
    }

    @Override
    public void write (int x) {
        synchronized(buf){
            buf.append((char) x);
            maybeFlush();
        }
    }

    @Override
    public void write(String x) {
        synchronized(buf) {
            buf.append(x);
            maybeFlush ();
        }
    }
    
    @Override
    public void write(String x, int off, int len) {
        synchronized(buf) {
            // The CharSequence overload of append takes an *end* idx, not length
            buf.append(x, off, off + len);
            maybeFlush();
        }
    }

    @Override
    public void write(char[] x, int off, int len) {
        synchronized(buf) {
            buf.append(x, off, len);
            maybeFlush();
        }
    }

    private void maybeFlush() {
        try {
            if((Integer) outLimit.invoke() <= buf.length()) {
                flush();
            }
        }
        catch (Exception e) {}
    }

    @Override
    public void flush() {
        String text;
        synchronized(buf) {
            text = buf.toString();
            buf.setLength(0);
        
        if(text.length() > 0) {
            try {
                flushFn.invoke(text);
            }
            catch (Exception e) {}
        }}
    }
}
