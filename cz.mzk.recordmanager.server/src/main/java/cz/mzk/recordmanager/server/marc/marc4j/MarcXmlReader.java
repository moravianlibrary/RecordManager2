package cz.mzk.recordmanager.server.marc.marc4j;


import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

import org.marc4j.MarcException;
import org.marc4j.MarcReader;
import org.marc4j.RecordStack;
import org.marc4j.marc.Record;
import org.xml.sax.InputSource;

/**
 * customized implementation of {@link MarcXmlReader}
 * difference is in used {@link MarcXmlParserThread}
 *
 */
public class MarcXmlReader implements MarcReader{
    private final RecordStack queue;

    /**
     * Constructs an instance with the specified input stream.
     *
     * @param input the input stream
     */
    public MarcXmlReader(final InputStream input) {
        this(new InputSource(input));
    }

    /**
     * Constructs an instance with the specified input source.
     *
     * @param input the input source
     */
    public MarcXmlReader(final InputSource input) {
        this.queue = new RecordStack();
        final MarcXmlParserThread producer = new MarcXmlParserThread(queue, input);
        producer.start();
    }

    /**
     * Constructs an instance with the specified input stream and stylesheet location. The stylesheet is used to
     * transform the source file and should produce valid MARC XML records. The result is then used to create
     * <code>Record</code> objects.
     *
     * @param input the input stream
     * @param stylesheetUrl the stylesheet location
     */
    public MarcXmlReader(final InputStream input, final String stylesheetUrl) {
        this(new InputSource(input), new StreamSource(stylesheetUrl));
    }

    /**
     * Constructs an instance with the specified input stream and stylesheet source. The stylesheet is used to transform
     * the source file and should produce valid MARCXML records. The result is then used to create <code>Record</code>
     * objects.
     *
     * @param input the input stream
     * @param stylesheet the stylesheet source
     */
    public MarcXmlReader(final InputStream input, final Source stylesheet) {
        this(new InputSource(input), stylesheet);
    }

    /**
     * Constructs an instance with the specified input source and stylesheet source. The stylesheet is used to transform
     * the source file and should produce valid MARCXML records. The result is then used to create <code>Record</code>
     * objects.
     *
     * @param input the input source
     * @param stylesheet the stylesheet source
     */
    public MarcXmlReader(final InputSource input, final Source stylesheet) {
        this.queue = new RecordStack();
        final MarcXmlParserThread producer = new MarcXmlParserThread(queue, input);
        final TransformerFactory factory = TransformerFactory.newInstance();
        final SAXTransformerFactory stf = (SAXTransformerFactory) factory;
        TransformerHandler th = null;
        try {
            th = stf.newTransformerHandler(stylesheet);
        } catch (final TransformerConfigurationException e) {
            throw new MarcException("Error creating TransformerHandler", e);
        }
        producer.setTransformerHandler(th);
        producer.start();
    }

    /**
     * Constructs an instance with the specified input stream and transformer handler. The
     * {@link javax.xml.transform.sax.TransformerHandler}&nbsp;is used to transform the source file and should produce
     * valid MARCXML records. The result is then used to create <code>Record</code> objects. A
     * <code>TransformerHandler</code> can be obtained from a <code>SAXTransformerFactory</code> with either a
     * {@link javax.xml.transform.Source}&nbsp;or {@link javax.xml.transform.Templates}&nbsp;object.
     *
     * @param input the input stream
     * @param th the transformation content handler
     */
    public MarcXmlReader(final InputStream input, final TransformerHandler th) {
        this(new InputSource(input), th);
    }

    /**
     * Constructs an instance with the specified input source and transformer handler. The
     * {@link javax.xml.transform.sax.TransformerHandler}&nbsp;is used to transform the source file and should produce
     * valid MARCXML records. The result is then used to create <code>Record</code> objects. A
     * <code>TransformerHandler</code> can be obtained from a <code>SAXTransformerFactory</code> with either a
     * {@link javax.xml.transform.Source}&nbsp;or {@link javax.xml.transform.Templates}&nbsp;object.
     *
     * @param input the input source
     * @param th the transformation content handler
     */
    public MarcXmlReader(final InputSource input, final TransformerHandler th) {
        this.queue = new RecordStack();
        final MarcXmlParserThread producer = new MarcXmlParserThread(queue, input);
        producer.setTransformerHandler(th);
        producer.start();
    }

    /**
     * Returns true if the iteration has more records, false otherwise.
     *
     * @return boolean - true if the iteration has more records, false otherwise
     */
    public boolean hasNext() {
        return queue.hasNext();
    }

    /**
     * Returns the next record in the iteration.
     *
     * @return Record - the record object
     */
    public Record next() {
        return queue.pop();
    }

}
