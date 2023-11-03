package cz.mzk.recordmanager.server.util;

import cz.mzk.recordmanager.server.ClasspathResourceProvider;

import javax.xml.transform.*;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class MODSTransformer {

	private static Transformer transformer;

	public MODSTransformer() throws TransformerConfigurationException {
		TransformerFactory factory = TransformerFactory.newInstance();
		factory.setURIResolver(new XsltURIResolver());
		transformer = factory.newTransformer(new StreamSource(
				new ClasspathResourceProvider().getResource("/xsl/mods/MODS3-6_MARC21slim_XSLT1-0.xsl")));
	}

	public ByteArrayOutputStream transform(InputStream is) throws TransformerException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		StreamResult result = new StreamResult(bos);
		transformer.transform(new StreamSource(is), result);
		return bos;
	}

	private class XsltURIResolver implements URIResolver {

		@Override
		public Source resolve(String href, String base) throws TransformerException {
			try {
				return new StreamSource(new ClasspathResourceProvider().getResource("/xsl/mods/" + href));
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
	}
}
