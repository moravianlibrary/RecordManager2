package cz.mzk.recordmanager.server.marc.marc4j;

import javax.xml.transform.sax.TransformerHandler;

import org.marc4j.MarcException;
import org.marc4j.MarcXmlParser;
import org.marc4j.RecordStack;
import org.xml.sax.InputSource;

/**
 * customized implementation of MarcXmlParserThread,
 * only difference is in used {@link MarcXmlHandler}
 *
 */
public class MarcXmlParserThread extends Thread {

    private RecordStack queue;

    private InputSource input;

    private TransformerHandler th = null;

    /**
     * Creates a new instance and registers the <code>RecordQueue</code>.
     * 
     * @param queue the record queue
     */
    public MarcXmlParserThread(RecordStack queue) {
        this.queue = queue;
    }

    /**
     * Creates a new instance and registers the <code>RecordQueue</code> and the
     * <code>InputStream</code>.
     * 
     * @param queue the record queue
     * @param input the input stream
     */
    public MarcXmlParserThread(RecordStack queue, InputSource input) {
        this.queue = queue;
        this.input = input;
    }

    /**
     * Returns the content handler to transform the source to MARCXML.
     * 
     * @return TransformerHandler - the transformation content handler
     */
    public TransformerHandler getTransformerHandler() {
        return th;
    }

    /**
     * Sets the content handler to transform the source to MARCXML.
     * 
     * @param th - the transformation content handler
     */
    public void setTransformerHandler(TransformerHandler th) {
        this.th = th;
    }

    /**
     * Returns the input stream.
     * 
     * @return InputSource - the input source
     */
    public InputSource getInputSource() {
        return input;
    }

    /**
     * Sets the input stream.
     * 
     * @param input the input stream
     */
    public void setInputSource(InputSource input) {
        this.input = input;
    }

    /**
     * Creates a new <code>MarcXmlHandler</code> instance, registers the
     * <code>RecordQueue</code> and sends the <code>InputStream</code> to the
     * <code>MarcXmlParser</code> parser.
     */
    public void run() {
        try {
            MarcXmlHandler handler = new MarcXmlHandler(queue);
            MarcXmlParser parser = new MarcXmlParser(handler);

            if (th == null) {
                parser.parse(input);
            } else {
                parser.parse(input, th);
            }
        } catch (MarcException me) {
            queue.passException(me);
        } finally {
            queue.end();
        }
    }

}
