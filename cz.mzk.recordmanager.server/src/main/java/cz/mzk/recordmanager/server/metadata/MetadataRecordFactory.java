package cz.mzk.recordmanager.server.metadata;

import cz.mzk.recordmanager.server.dc.DublinCoreParser;
import cz.mzk.recordmanager.server.dc.DublinCoreRecord;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.institutions.*;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.HarvestedRecord.HarvestedRecordUniqueId;
import cz.mzk.recordmanager.server.model.ImportConfiguration;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

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
		String prefix = MetadataUtils.getPrefix(configuration);
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
		case Constants.PREFIX_SFXJIBKKKV:
		case Constants.PREFIX_SFXJIBUZEI:
		case Constants.PREFIX_SFXJIBKKVY:
		case Constants.PREFIX_SFXJIBSVKUL:
		case Constants.PREFIX_SFXJIBNLK:
		case Constants.PREFIX_SFXJIBSVKKL:
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
		case Constants.PREFIX_KKDVY:
		case Constants.PREFIX_KJM:
			return new CosmotronMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_PKJAK:
			return new PkjakMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_MKP:
			return new MkpMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_MKBREC:
		case Constants.PREFIX_MKCK:
		case Constants.PREFIX_MKHNM:
		case Constants.PREFIX_MKMIL:
		case Constants.PREFIX_MKPISEK:
		case Constants.PREFIX_MKSTER:
			return new ClaviusMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_KRAM_CUNI:
		case Constants.PREFIX_KRAM_CUNIFSV:
		case Constants.PREFIX_KRAM_CUNILF1:
		case Constants.PREFIX_KRAM_DIFMOE:
		case Constants.PREFIX_KRAM_DSMO:
		case Constants.PREFIX_KRAM_HMT:
		case Constants.PREFIX_KRAM_LMDA:
		case Constants.PREFIX_KRAM_MENDELU:
		case Constants.PREFIX_KRAM_MJH:
		case Constants.PREFIX_KRAM_MVCHK:
		case Constants.PREFIX_KRAM_NACR:
		case Constants.PREFIX_KRAM_NFA:
		case Constants.PREFIX_KRAM_NM:
		case Constants.PREFIX_KRAM_NULK:
		case Constants.PREFIX_KRAM_PKJAK:
		case Constants.PREFIX_KRAM_UPM:
		case Constants.PREFIX_KRAM_VSE:
		case Constants.PREFIX_KRAM_VSUP:
		case Constants.PREFIX_KRAM_VUGTK:
		case Constants.PREFIX_KRAM_ZCM:
		case Constants.PREFIX_KRAM_ZMP:
		case Constants.PREFIX_KRAM_MZK:
		case Constants.PREFIX_KRAM_KNAV:
		case Constants.PREFIX_KRAM_NLK:
		case Constants.PREFIX_KRAM_SVKUL:
		case Constants.PREFIX_KRAM_CBVK:
		case Constants.PREFIX_KRAM_NTK:
		case Constants.PREFIX_KRAM_MKP:
		case Constants.PREFIX_KRAM_SVKHK:
		case Constants.PREFIX_KRAM_VKOL:
		case Constants.PREFIX_KRAM_UZEI:
		case Constants.PREFIX_KRAM_KFBZ:
		case Constants.PREFIX_KRAM_KKKV:
		case Constants.PREFIX_KRAM_KKPC:
		case Constants.PREFIX_KRAM_KKVY:
		case Constants.PREFIX_KRAM_KVKL:
		case Constants.PREFIX_KRAM_SVKKL:
		case Constants.PREFIX_KRAM_SVKPK:
		case Constants.PREFIX_KRAM_SVKOS:
		case Constants.PREFIX_KRAM_TRE:
		case Constants.PREFIX_KRAM_NKP:
		case Constants.PREFIX_KRAM_ROZHLAS:
		case Constants.PREFIX_KRAM_SNK:
			return init(new KramDefaultMetadataMarcRecord(marcRec, hr));
		case Constants.PREFIX_UZEI:
			return new UzeiMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_RKKA:
			return new RkkaMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_BOOKPORT:
			return new BookportMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_MUNIPRESS:
			return new MunipressMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_MENDELU:
			return new MendeluMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_MKFM:
			return new MkfmMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_ARCHBIB:
			return new ArchbibMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_CZHISTBIB:
			return new CzhistbibMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_CELITEBIB:
			return new CelitebibMarcMetadataRecord(marcRec, hr);
		case Constants.PREFIX_KKPC:
			return new KkpcMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_PALMKNIHY:
			return init(new PalmknihyMetadataMarcRecord(marcRec, hr));
		case Constants.PREFIX_NKC_EBOOK:
			return new NkcEbookMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_ENVI:
		case Constants.PREFIX_GEOL:
			return new CgsMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_CNB:
			return new CnbMetadataMarcRecord(marcRec, hr);
		case Constants.PREFIX_BCBT:
			return new BcbtMetadataMarcRecord(marcRec, hr);
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
		String prefix = MetadataUtils.getPrefix(hr.getHarvestedFrom());
		switch (prefix) {
		case Constants.PREFIX_KRAM_MZK:
		case Constants.PREFIX_KRAM_NKP:
			return init(new KramDefaultMetadataDublinCoreRecord(dcRec, hr));
		case Constants.PREFIX_KRAM_KNAV:
			return init(new KramKnavMetadataDublinCoreRecord(dcRec, hr));
		case Constants.PREFIX_KRAM3_NKP:
			return init(new Kram3NkpMetadataDublinCoreRecord(dcRec, hr));
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

	private MetadataRecord init(MetadataRecord metadataRecord) {
		AutowireCapableBeanFactory factory = appCtx.getAutowireCapableBeanFactory();
		factory.autowireBean(metadataRecord);
		factory.initializeBean(metadataRecord, "metadataRecord");
		return metadataRecord;
	}
}
