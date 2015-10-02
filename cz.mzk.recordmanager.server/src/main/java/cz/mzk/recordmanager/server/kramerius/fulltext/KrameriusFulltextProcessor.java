package cz.mzk.recordmanager.server.kramerius.fulltext;

import java.util.Arrays;
import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;

import cz.mzk.recordmanager.server.model.FulltextMonography;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.KrameriusConfigurationDAO;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;


public class KrameriusFulltextProcessor implements ItemProcessor<HarvestedRecord, HarvestedRecord>, StepExecutionListener{

	@Autowired
	KrameriusFulltexter kf;
	
	@Autowired
	private KrameriusConfigurationDAO configDao;
	
	@Autowired
	private HibernateSessionSynchronizer sync;

	@Autowired
	private HibernateSessionSynchronizer hibernateSync;
	
	// configuration
	private Long confId;
	
	
	public KrameriusFulltextProcessor(Long confId) {
		super();
		this.confId = confId;
	}
		
	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder sess = hibernateSync.register()) {
			kf.setAuthToken(configDao.get(confId).getAuthToken());
			kf.setKramApiUrl(configDao.get(confId).getUrl());
			kf.setDownloadPrivateFulltexts(configDao.get(confId).isDownloadPrivateFulltexts());
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		return null;
	}

	/* proccessor
	 *  dostane uuid dokumentu
	 *  stahne seznam stranek
	 *  vrati objekty fulltextu*/	
	@Override
	public HarvestedRecord process(HarvestedRecord item) throws Exception {
		System.out.println("************ zpracovavam v processoru Harvested Record: " +item.toString() + " unikatni identifikator: "+item.getUniqueId() +"***************");

		System.out.println("------- nacitam stranky -------");

		String rootUuid = item.getUniqueId().getRecordId();
		List<FulltextMonography> pages = kf.getFulltextObjects(rootUuid);
		
		//result.add(item.getUniqueId().getRecordId());
		item.setFulltextMonography(pages);
		
		/* test nacteni*/
		List<FulltextMonography> pagess = item.getFulltextMonography();
		for (FulltextMonography p: pagess) {
			String ocr = null;
			if (p.getFulltext() != null) {
				ocr = new String(p.getFulltext(), "UTF-8");
			}
			System.out.println("-*-*-*-*-*-*-*-*-*-*");
			System.out.println("-*-* page UUID: "+ p.getUuidPage() + "*-*-");
			System.out.println("-*-* OCR start *-*- ");
			if (ocr != null) {
				System.out.println(ocr);
			}
			System.out.println("-*-* OCR end *-*- ");
			System.out.println("-*-*-*-*-*-*-*-*-*-*");

			
		}
		
		return item;
	}

}
