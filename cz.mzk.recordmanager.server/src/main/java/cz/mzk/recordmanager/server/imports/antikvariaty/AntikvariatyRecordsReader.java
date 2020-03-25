package cz.mzk.recordmanager.server.imports.antikvariaty;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ListIterator;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import cz.mzk.recordmanager.server.model.AntikvariatyRecord;
import cz.mzk.recordmanager.server.model.DownloadImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.DownloadImportConfigurationDAO;
import cz.mzk.recordmanager.server.util.HttpClient;
import cz.mzk.recordmanager.server.util.MetadataUtils;

public class AntikvariatyRecordsReader implements ItemReader<AntikvariatyRecord> {

	private Long configId;

	private String filename;

	private final static int EFFECTIVE_TITLE_LENGTH = 255;
	
	@Autowired 
	private HttpClient httpClient;

	@Autowired
	private DownloadImportConfigurationDAO configDao;

	private StaxEventItemReader<AntikvariatyRecord> reader;

	public AntikvariatyRecordsReader(final Long configId, final String filename) {
		this.configId = configId;
		this.filename = filename;
	}

	@Override
	public AntikvariatyRecord read() throws Exception {
		if (reader == null) {
			initializeReader();
		}
		AntikvariatyRecord item = reader.read();
		if (item != null) {
			fixCatalogueIds(item);
			shortenTitle(item);
		}
		return item;
	}
	
	protected void fixCatalogueIds(AntikvariatyRecord item) {
		if (item.getCatalogueIds() == null) {
			return;
		}
		ListIterator<String> iter = item.getCatalogueIds().listIterator();
		while (iter.hasNext()) {
			String text = iter.next();
			int pos = text.indexOf(')');
			if (pos > 0) {
				String id = text.substring(++pos);
				if (id.isEmpty()) {
					iter.remove();
				} else {
					iter.set(id);
				}
			} else {
				iter.remove();
			}
		}
		// unique ids
		item.setCatalogueIds(new ArrayList<String>(new HashSet<>(item.getCatalogueIds())));
	}

	protected void shortenTitle(AntikvariatyRecord item){
		item.setTitle(MetadataUtils.normalizeAndShorten(item.getTitle(), EFFECTIVE_TITLE_LENGTH));
	}

	protected void initializeReader() throws IOException, RuntimeException {
		InputStream is;
		if (filename == null) {
			DownloadImportConfiguration config = configDao.get(configId);
			if (config == null) {
				throw new IllegalArgumentException(String.format("Configuration with id=%s not found.", configId));
			}
			String url = config.getUrl();
			if (url == null || url.isEmpty()) {
				throw new IllegalArgumentException(
						String.format("Missing url in DownloadImportConfiguration with id=%s.", configId));
			}
			is = httpClient.executeGet(url);
		} else {
			is = new FileInputStream(filename);
		}
		try {
			reader = new StaxEventItemReader<>();
			reader.setResource(new InputStreamResource(is));
			reader.setFragmentRootElementName("record");
			Jaxb2Marshaller unmarshaller = new Jaxb2Marshaller();
			unmarshaller.setClassesToBeBound(AntikvariatyRecord.class);
			unmarshaller.afterPropertiesSet();
			reader.setUnmarshaller(unmarshaller);
			reader.setSaveState(false);
			reader.open(null);
			reader.afterPropertiesSet();
		} catch (Exception ex) {
			throw new RuntimeException("StaxEventItemReader can not be created", ex);
		}
	}

}
