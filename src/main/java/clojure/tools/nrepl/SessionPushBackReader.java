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
        if(len == 0) {
            return -1;
        }

        Object firstChar;
        try {
            firstChar = requestInputFn.invoke();
            if (firstChar == null) {
                return -1;
            }
        }
        catch (Exception e) {
            System.out.println("Errored in requestInput");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return -1;
        }
        
        if(firstChar instanceof Number && ((Number)firstChar).equals(-1)) {
            return -1;
        }
        
        buf[off] = (Character)firstChar;
        Object readResult;
        try {
            readResult = doRead.invoke(buf, off + 1, len - 1);
        }
        catch (Exception e) {
            System.out.println("Error in doRead");
            System.out.println(e.getMessage());
            e.printStackTrace();
            return -1;
        }
        return (int)(((Long) readResult) - Long.valueOf(off));
    }
}
