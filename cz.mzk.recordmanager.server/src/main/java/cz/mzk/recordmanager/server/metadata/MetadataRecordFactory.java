package cz.mzk.recordmanager.server.metadata;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import cz.mzk.recordmanager.server.metadata.institutions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import cz.mzk.recordmanager.server.dc.DublinCoreParser;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;

@Component
public class MetadataRecordFactory {

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private DublinCoreParser dcParser;

	@Autowired
	private ApplicationContext appCtx;

	public MetadataRecord getMetadataRecord(HarvestedRecord record) {
		if (record == null) {
			return null;
		}

		ImportConfiguration configuration = record.getHarvestedFrom();
		InputStream is = new ByteArrayInputStream(record.getRawRecord());

		String recordFormat = record.getFormat();

		if (Constants.METADATA_FORMAT_MARC21.equals(recordFormat)
				|| Constants.METADATA_FORMAT_XML_MARC.equals(recordFormat)
				|| Constants.METADATA_FORMAT_MARC_CPK.equals(recordFormat)
				|| Constants.METADATA_FORMAT_OAI_MARCXML_CPK.equals(recordFormat)
				|| Constants.METADATA_FORMAT_MARC21E.equals(recordFormat)) {
			MarcRecord marcRec = marcXmlParser.parseRecord(is);
			return getMetadataRecord(marcRec, configuration);
		}

		if (Constants.METADATA_FORMAT_DUBLIN_CORE.equals(recordFormat)
				|| Constants.METADATA_FORMAT_ESE.equals(recordFormat)) {
			DublinCoreRecord dcRec = dcParser.parseRecord(is);
			return getMetadataRecord(record, dcRec, configuration);
		}

		return null;
	}

	public MetadataRecord getMetadataRecord(MarcRecord marcRec, ImportConfiguration configuration) {
		String prefix = getPrefix(configuration);
		switch (prefix) {
		case Constants.PREFIX_MZK:
			return new MzkMetadataMarcRecord(marcRec);
		case Constants.PREFIX_NKP:
			return new NkpMarcMetadataRecord(marcRec);
		case Constants.PREFIX_TRE:
		case Constants.PREFIX_MKUO:
			return new TreMetadataMarcRecord(marcRec);
		case Constants.PREFIX_MZKNORMS:
			return new MzkNormsMetadataMarcRecord(marcRec);
		case Constants.PREFIX_SFXKNAV:
			return new SfxMetadataMarcRecord(marcRec);
		case Constants.PREFIX_SFXJIBNLK_PERIODICALS:
			return new SfxjibNlkPeriodicalsMetadataMarcRecord(marcRec);
		case Constants.PREFIX_SFXJIBCBVK:
		case Constants.PREFIX_SFXJIBFREE:
		case Constants.PREFIX_SFXJIBKFBZ:
		case Constants.PREFIX_SFXJIBKVKL:
		case Constants.PREFIX_SFXJIBMKP:
		case Constants.PREFIX_SFXJIBMZK:
		case Constants.PREFIX_SFXJIBNKP:
		case Constants.PREFIX_SFXJIBSVKHK:
		case Constants.PREFIX_SFXJIBSVKOS:
		case Constants.PREFIX_SFXJIBVKOL:
			return new SfxJibMetadataMarcRecord(marcRec);
		case Constants.PREFIX_SFXJIBIREL:
			return new SfxJibIrelMetadataMarcRecord(marcRec);
		case Constants.PREFIX_SFXJIBKNAV:
			return new SfxKnavMetadataMarcRecord(marcRec);
		case Constants.PREFIX_SFXTECHLIBNTK:
		case Constants.PREFIX_SFXTECHLIBUOCHB:
		case Constants.PREFIX_SFXTECHLIBVSCHT:
			return new SfxTechlibMetadataMarcRecord(marcRec);
		case Constants.PREFIX_SFXJIBNLK:
			return new SfxjibNlkMetadataMarcRecord(marcRec);
		case Constants.PREFIX_SFXJIBMUNI:
			return new SfxDirectMetadataMarcRecord(marcRec);
		case Constants.PREFIX_CASLIN:
			return new SkatMarcMetadataRecord(marcRec);
		case Constants.PREFIX_AUTH:
			return new AuthMetadataMarcRecord(marcRec);
		case Constants.PREFIX_OSOBNOSTI:
			return new OsobnostiRegionuMetadataMarcRecord(marcRec);
		case Constants.PREFIX_SVKUL:
			return new SvkulMetadataMarcRecord(marcRec);
		case Constants.PREFIX_VKOL:
			return new VkolMarcMetadataRecord(marcRec);
		case Constants.PREFIX_ZAKONY:
			return new ZakonyProLidiMetadataMarcRecord(marcRec);
		case Constants.PREFIX_MKP_EBOOKS:
			return new MkpEbooksMetadataMarcRecord(marcRec);
		case Constants.PREFIX_BMC:
			return new BmcMetadataMarcRecord(marcRec);
		case Constants.PREFIX_UPV:
			MetadataRecord mr = new PatentsMetadataMarcRecord(marcRec);
			init(mr);
			return mr;
		case Constants.PREFIX_OPENLIB:
			return new OpenLibraryMetadataMarcRecord(marcRec);
		case Constants.PREFIX_MESH:
			return new MeshMarcMetadataRecord(marcRec);
		case Constants.PREFIX_LIBRARY:
			return new LibraryMetadataMarcRecord(marcRec);
		case Constants.PREFIX_TDKIV:
			return new TdkivMetadataMarcRecord(marcRec);
		case Constants.PREFIX_AGROVOC:
			return new AgrovocMarcMetadataRecord(marcRec);
		case Constants.PREFIX_KVKL:
		case Constants.PREFIX_CBVK:
		case Constants.PREFIX_SVKKL:
			return new CosmotronMetadataMarcRecord(marcRec);
		default:
			return new MetadataMarcRecord(marcRec);
		}
	}

	public MetadataRecord getMetadataRecord(HarvestedRecord hr, DublinCoreRecord dcRec, ImportConfiguration configuration) {
		String prefix = getPrefix(configuration);
		switch (prefix) {
		case Constants.PREFIX_KRAM_MZK:
			return new KramMzkMetadataDublinCoreRecord(dcRec, hr);
		case Constants.PREFIX_KRAM_NKP:
			MetadataRecord mr = new KramNkpMetadataDublinCoreRecord(dcRec, hr);
			init(mr);
			return mr;
		case Constants.PREFIX_KRAM_NTK:
		case Constants.PREFIX_KRAM_KNAV:
			return new KramDefaultMetadataDublinCoreRecord(dcRec, hr);
		case Constants.PREFIX_KRAM3_NKP:
			return new Kram3NkpMetadataDublinCoreRecord(dcRec);
		case Constants.PREFIX_MANUSCRIPTORIUM:
			return new ManuscriptoriumMetadataDublinCoreRecord(dcRec);
		default:
			return getMetadataRecord(dcRec);
		}
	}

	public MetadataRecord getMetadataRecord(HarvestedRecordUniqueId recordId) {
		return getMetadataRecord(harvestedRecordDao.get(recordId));
	}

	public MetadataRecord getMetadataRecord(MarcRecord marcRecord) {
		return new MetadataMarcRecord(marcRecord);
	}

	public MetadataRecord getMetadataRecord(DublinCoreRecord dcRecord) {
		return new MetadataDublinCoreRecord(dcRecord);
	}

	private String getPrefix(ImportConfiguration configuration) {
		if (configuration != null) {
			String prefix;
			prefix = configuration.getIdPrefix();
			return prefix == null ? "" : prefix;
		}
		return "";
	}

	private void init(Object metadataRecord) {
		AutowireCapableBeanFactory factory = appCtx.getAutowireCapableBeanFactory();
		factory.autowireBean(metadataRecord);
		factory.initializeBean(metadataRecord, "metadataRecord");
	}
}
