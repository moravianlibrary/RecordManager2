package cz.mzk.recordmanager.server.adresar;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

import org.hibernate.SessionFactory;
import org.marc4j.MarcWriter;
import org.marc4j.MarcXmlWriter;
import org.marc4j.marc.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.marc.ISOCharConvertor;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcRecordImpl;
import cz.mzk.recordmanager.server.model.AdresarKnihoven;
import cz.mzk.recordmanager.server.oai.dao.AdresarKnihovenDAO;

public class AdresarRecordsWriter implements ItemWriter<List<Record>> {
	
	private static Logger logger = LoggerFactory.getLogger(AdresarRecordsWriter.class);
	
	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	public AdresarKnihovenDAO adresarDao;
	
	@Override
	public void write(List<? extends List<Record>> items) throws Exception {
		try {
			writeInner(items);
		} finally {
			sessionFactory.getCurrentSession().flush();
			sessionFactory.getCurrentSession().clear();
		}
	}

	protected void writeInner(List<? extends List<Record>> items) throws Exception {
		for (List<Record> records : items) {
			for (Record currentRecord : records) {
				try {
					if (currentRecord == null) {
						continue;
					}
					MarcRecord marc = new MarcRecordImpl(currentRecord);
					String recordId = marc.getControlField("SYS");
					AdresarKnihoven ak = adresarDao.findByRecordId(recordId);
					if (ak == null) {
						ak = new AdresarKnihoven(recordId);
						ak.setFormat("marc21-xml");
					}
					ak.setUpdated(new Date());
					ByteArrayOutputStream outStream = new ByteArrayOutputStream();
					MarcWriter marcWriter = new MarcXmlWriter(outStream, true);
					marcWriter.setConverter(ISOCharConvertor.INSTANCE);
					marcWriter.write(currentRecord);
					marcWriter.close();
					ak.setRawRecord(outStream.toByteArray());

					adresarDao.persist(ak);
				} catch (Exception e) {
					logger.warn("Error occured in processing record");
					throw e;
				}
			}
		}
	}

}
