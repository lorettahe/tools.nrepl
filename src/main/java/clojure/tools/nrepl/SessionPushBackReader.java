package clojure.tools.nrepl;

import java.io.Reader;
import clojure.lang.IFn;
import java.util.concurrent.LinkedBlockingQueue;

public class SessionPushBackReader extends java.io.Reader {
    private final LinkedBlockingQueue inputQueue;
    private final IFn requestInputFn;
    private final IFn doRead;
    
    public SessionPushBackReader(final LinkedBlockingQueue inputQueue, final IFn requestInputFn, final IFn doRead) {
        this.inputQueue = inputQueue;
        this.requestInputFn = requestInputFn;
        this.doRead = doRead;
    }

    @Override
    public void close() {
        inputQueue.clear();
    }
    
    @Override
    public int read(char[] buf, int off, int len) {
        try {
            if(len == 0) {
                return -1;
            }
            final Character firstChar = (Character) requestInputFn.invoke();
            if(firstChar == null || firstChar.equals(-1)) {
                return -1;
            }
            
            buf[off] = firstChar.charValue();
            return (Integer) doRead.invoke(buf, off + 1, len - 1) - off;
        }
        catch (Exception e) {
            return -1;
        }
    }
}
