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
			return getMetadataRecord(record, marcRec, configuration);
		}

		if (Constants.METADATA_FORMAT_DUBLIN_CORE.equals(recordFormat)
				|| Constants.METADATA_FORMAT_ESE.equals(recordFormat)) {
			DublinCoreRecord dcRec = dcParser.parseRecord(is);
			return getMetadataRecord(record, dcRec);
		}

		return null;
	}

	public MetadataRecord getMetadataRecord(MarcRecord marcRec, ImportConfiguration configuration) {
		return getMetadataRecord(null, marcRec, configuration);
	}

	public MetadataRecord getMetadataRecord(HarvestedRecord hr, MarcRecord marcRec, ImportConfiguration configuration) {
		String prefix = getPrefix(configuration);
		switch (prefix) {
		case Constants.PREFIX_MZK:
			return new MzkMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_NKP:
			return new NkpMarcMetadataRecord(marcRec, hr);
		case Constants.PREFIX_TRE:
		case Constants.PREFIX_MKUO:
			return new TreMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_MZKNORMS:
			return new MzkNormsMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_SFXKNAV:
			return new SfxMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_SFXJIBNLK_PERIODICALS:
			return new SfxjibNlkPeriodicalsMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_SFXJIBCBVK:
		case Constants.PREFIX_SFXJIBFREE:
		case Constants.PREFIX_SFXJIBKFBZ:
		case Constants.PREFIX_SFXJIBKVKL:
		case Constants.PREFIX_SFXJIBMKP:
		case Constants.PREFIX_SFXJIBMZK:
		case Constants.PREFIX_SFXJIBNKP:
		case Constants.PREFIX_SFXJIBSVKHK:
		case Constants.PREFIX_SFXJIBSVKOS:
		case Constants.PREFIX_SFXJIBSVKPK:
		case Constants.PREFIX_SFXJIBVKOL:
		case Constants.PREFIX_SFXJIBMKHK:
			return new SfxJibMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_SFXJIBIREL:
			return new SfxJibIrelMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_SFXJIBKIV:
			return new SfxJibKivMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_SFXJIBMUS:
			return new SfxJibMusMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_SFXJIBTECH:
			return new SfxJibTechMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_SFXJIBKNAV:
			return new SfxKnavMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_SFXTECHLIBNTK:
		case Constants.PREFIX_SFXTECHLIBUOCHB:
		case Constants.PREFIX_SFXTECHLIBVSCHT:
			return new SfxTechlibMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_SFXJIBNLK:
			return new SfxjibNlkMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_SFXJIBMUNI:
			return new SfxDirectMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_CASLIN:
			return new SkatMarcMetadataRecord(marcRec, hr);
		case Constants.PREFIX_AUTH:
			return init(new AuthMetadataMarcRecord(marcRec, hr));
		case Constants.PREFIX_OSOBNOSTI:
			return new OsobnostiRegionuMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_SVKUL:
			return new SvkulMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_VKOL:
			return new VkolMarcMetadataRecord(marcRec, hr);
		case Constants.PREFIX_ZAKONY:
			return new ZakonyProLidiMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_MKP_EBOOKS:
			return new MkpEbooksMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_BMC:
			return new BmcMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_UPV:
			return init(new PatentsMetadataMarcRecord(marcRec, hr));
		case Constants.PREFIX_OPENLIB:
			return new OpenLibraryMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_MESH:
			return new MeshMarcMetadataRecord(marcRec, hr);
		case Constants.PREFIX_LIBRARY:
			return init(new LibraryMetadataMarcRecord(marcRec, hr));
		case Constants.PREFIX_TDKIV:
			return new TdkivMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_AGROVOC:
			return new AgrovocMarcMetadataRecord(marcRec, hr);
		case Constants.PREFIX_KVKL:
		case Constants.PREFIX_CBVK:
		case Constants.PREFIX_SVKKL:
		case Constants.PREFIX_UPOL:
			return new CosmotronMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_PKJAK:
			return new PkjakMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_KKKV:
			return new KkkvMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_MKP:
			return new MkpMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_CMUZ:
		case Constants.PREFIX_KKVY:
		case Constants.PREFIX_KNEP:
		case Constants.PREFIX_MKBREC:
		case Constants.PREFIX_MKCK:
		case Constants.PREFIX_MKHK:
		case Constants.PREFIX_MKHNM:
		case Constants.PREFIX_MKHOD:
		case Constants.PREFIX_MKKL:
		case Constants.PREFIX_MKMIL:
		case Constants.PREFIX_MKML:
		case Constants.PREFIX_MKNB:
		case Constants.PREFIX_MKOR:
		case Constants.PREFIX_MKPEL:
		case Constants.PREFIX_MKPISEK:
		case Constants.PREFIX_MKSTER:
		case Constants.PREFIX_MKTRI:
		case Constants.PREFIX_MKZN:
		case Constants.PREFIX_VFU:
			return new ClaviusMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_KRAM_KNAV:
			return new KramKnavMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_KRAM_MZK:
			return init(new KramMzkMetadataMarcRecord(marcRec, hr));
		case Constants.PREFIX_KRAM_NLK:
			return new KramNlkMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_KRAM_SVKUL:
			return new KramSvkulMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_KRAM_CBVK:
			return new KramCbvkMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_KRAM_NTK:
			return new KramNtkMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_KRAM_MKP:
			return new KramMkpMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_KRAM_SVKHK:
			return new KramSvkhkMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_KRAM_VKOL:
			return new KramVkolMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_UZEI:
			return new UzeiMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_RKKA:
			return new RkkaMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_BOOKPORT:
			return new BookportMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_MUNIPRESS:
			return new MunipressMetadataMarcRecord(marcRec, hr);
		default:
			return new MetadataMarcRecord(marcRec, hr);
		}
	}

	public MetadataRecord getMetadataRecord(HarvestedRecord hr, MarcRecord mr) {
		return getMetadataRecord(hr, mr, hr.getHarvestedFrom());
	}

	public MetadataRecord getMetadataRecord(MarcRecord marcRecord) {
		return new MetadataMarcRecord(marcRecord);
	}

	public MetadataRecord getMetadataRecord(HarvestedRecord hr, DublinCoreRecord dcRec) {
		String prefix = getPrefix(hr.getHarvestedFrom());
		switch (prefix) {
		case Constants.PREFIX_KRAM_MZK:
			return new KramMzkMetadataDublinCoreRecord(dcRec, hr);
		case Constants.PREFIX_KRAM_NKP:
			MetadataRecord mr = new KramNkpMetadataDublinCoreRecord(dcRec, hr);
			init(mr);
			return mr;
		case Constants.PREFIX_KRAM_KNAV:
			return new KramKnavMetadataDublinCoreRecord(dcRec, hr);
		case Constants.PREFIX_KRAM3_NKP:
			return new Kram3NkpMetadataDublinCoreRecord(dcRec, hr);
		case Constants.PREFIX_MANUSCRIPTORIUM:
			return new ManuscriptoriumMetadataDublinCoreRecord(dcRec, hr);
		default:
			return new MetadataDublinCoreRecord(dcRec, hr);
		}
	}

	public MetadataRecord getMetadataRecord(HarvestedRecordUniqueId recordId) {
		return getMetadataRecord(harvestedRecordDao.get(recordId));
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

	private MetadataRecord init(MetadataRecord metadataRecord) {
		AutowireCapableBeanFactory factory = appCtx.getAutowireCapableBeanFactory();
		factory.autowireBean(metadataRecord);
		factory.initializeBean(metadataRecord, "metadataRecord");
		return metadataRecord;
	}
}
