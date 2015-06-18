package cz.mzk.recordmanager.server.kramerius.harvest;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemStream;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.model.OAIHarvestConfiguration;
import cz.mzk.recordmanager.server.oai.dao.OAIHarvestConfigurationDAO;
import cz.mzk.recordmanager.server.oai.harvest.OAIHarvesterParams;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer;
import cz.mzk.recordmanager.server.util.HibernateSessionSynchronizer.SessionBinder;

@Component
@StepScope
public class KrameriusItemReader implements ItemReader<List<HarvestedRecord>>,
		ItemStream, StepExecutionListener {

	@Autowired
	private OAIHarvestConfigurationDAO configDao;

	@Autowired
	private KrameriusHarvesterFactory harvesterFactory;

	@Autowired
	private TransactionTemplate template;

	@Autowired
	private HibernateSessionSynchronizer sync;

	@Autowired
	private HibernateSessionSynchronizer hibernateSync;
	
	
	private KrameriusHarvester kHarvester;
	
	private OAIHarvestConfiguration conf;
	
	// configuration
	private Long confId;

	private Date fromDate;

	private Date untilDate;
	
	//state
	private String krameriusStart;
	private Integer start;
	
	private boolean finished = false;
	
	public KrameriusItemReader(Long confId, Date fromDate, Date untilDate, String krameriusStart) {
		super();
		this.confId = confId;
		this.fromDate = fromDate;
		this.untilDate = untilDate;
		this.krameriusStart = krameriusStart;
		if (krameriusStart == null) {
			start=0;
		} else {
			this.start = Integer.valueOf(krameriusStart);
		}
		System.out.println("--------------------------------- confID = " + confId +"--------------------------");
	}
	
	@Override
	public List<HarvestedRecord> read() {
		System.out.println("----- STARTUJE READER --------");
		if (finished) {
			return null;
		}
		
		//get uuids
		List<String> uuids = kHarvester.getUuids(start);
		if (uuids != null) {
			System.out.println("0-0-0-0-0-0- nasly se uuid:" + uuids.toString() + "0-0-0-0-0-0-0-0-0");
		} else {
			System.out.println("0-0-0-0-0-0-  NENASLY se uuid 0-0-0-0-0-0-0-0");
		}
		
		
		//get metadata
		List<HarvestedRecord> records = kHarvester.getRecords(uuids);
		
		for (HarvestedRecord r: records) {
			System.out.println("1-1-1-1--1-1-1-1-1-1-1 zacatek objektu 1-1-1-1-1-1-1-1-1-1-1");
			String s = "nic tu neni - asi doslo k vyjimce";
			try {
				s = new String(r.getRawRecord(), "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(s);
			System.out.println("1-1-1-1--1-1-1-1-1-1-1 konec objektu 1-1-1-1-1-1-1-1-1-1-1");

			
		}
		
		//decide if continue (docNum vs start)
		
		System.out.println("porovnavame start: "+start + " a harvester.numFound():"+ kHarvester.getNumFound());
		if (start<kHarvester.getNumFound()) {
			start=start+20; // melo by se parametrizovat..
			krameriusStart=start.toString();
		} else {
			finished = true; 
		}
		//return metadata
		System.out.println("*********************************** reader konci s krameriusStart = " +krameriusStart+ " a finished je :"+finished+" ***************************");
		return records;
	}
	
	@Override
	public void beforeStep(StepExecution stepExecution) {
		try (SessionBinder sess = hibernateSync.register()) {
			conf = configDao.get(confId);
			OAIHarvesterParams params = new OAIHarvesterParams();
			params.setUrl(conf.getUrl());
//			params.setMetadataPrefix(conf.getMetadataPrefix());
//			params.setGranularity(conf.getGranularity());
//			params.setSet(conf.getSet());
			params.setFrom(fromDate);
			params.setUntil(untilDate);
			kHarvester = harvesterFactory.create(params, confId);
//			processIdentify(conf);
//			conf = configDao.get(confId);
//			params.setGranularity(conf.getGranularity());
//			harvester = harvesterFactory.create(params);
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void open(ExecutionContext ctx)
			throws ItemStreamException {
//		if (ctx.containsKey("krameriusStart")) {
//			krameriusStart = ctx.getString("krameriusStart");
//		}
	}

	@Override
	public void update(ExecutionContext ctx)
			throws ItemStreamException {
//		System.out.println("spoustim update");
//		if (ctx == null) {
//			System.out.println("ctx je null");
//		}
//		if (krameriusStart == null) {
//			System.out.println("kramerius start je null");
//			//krameriusStart = "0"; // kvuli kontextu... u Stringu to zda se nevadi
//		}
//		ctx.putString("pokus", null);
//		ctx.putString("krameriusStart", krameriusStart);
	}

	@Override
	public void close() throws ItemStreamException {
		// TODO Auto-generated method stub

	}


}
