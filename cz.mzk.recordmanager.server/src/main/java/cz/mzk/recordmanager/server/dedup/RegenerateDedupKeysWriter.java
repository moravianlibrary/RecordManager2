package cz.mzk.recordmanager.server.dedup;

import cz.mzk.recordmanager.server.marc.InvalidMarcException;
import cz.mzk.recordmanager.server.marc.MarcRecord;
import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.FileUtils;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.Range;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

@Component
@StepScope
public class RegenerateDedupKeysWriter implements ItemWriter<Long> {

	private static Logger logger = LoggerFactory.getLogger(RegenerateDedupKeysWriter.class);

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Autowired
	protected DelegatingDedupKeysParser dedupKeysParser;

	@Autowired
	protected SessionFactory sessionFactory;

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private MetadataRecordFactory mrFactory;

	private ProgressLogger progressLogger = new ProgressLogger(logger, 10000);

	private static final List<String> KONSPEKT = FileUtils.openFile("/list/konspekt.txt");
	private static final List<String> PERIODICALS = FileUtils.openFile("/list/periodicals.txt")
			.stream().map(p -> MetadataUtils.normalize(p)).collect(Collectors.toList());

	private static final Map<String, List<Long>> map = new HashMap<>();

	static {
		map.put(MetadataUtils.normalize("Haló noviny"), new ArrayList<>(LongStream.rangeClosed(2011L, 2020L).boxed().collect(Collectors.toList())));
		map.get(MetadataUtils.normalize("Haló noviny")).add(1991L);
		map.put(MetadataUtils.normalize("Hospodářské noviny"), new ArrayList<>(LongStream.rangeClosed(2011L, 2020L).boxed().collect(Collectors.toList())));
		map.put(MetadataUtils.normalize("Lidové noviny"), new ArrayList<>(LongStream.rangeClosed(2011L, 2020L).boxed().collect(Collectors.toList())));
		map.get(MetadataUtils.normalize("Lidové noviny")).addAll(Arrays.asList(1989L,1990L));
		map.put(MetadataUtils.normalize("Literární noviny"), new ArrayList<>(LongStream.rangeClosed(2011L, 2020L).boxed().collect(Collectors.toList())));
		map.put(MetadataUtils.normalize("Mladá fronta Dnes"), new ArrayList<>(LongStream.rangeClosed(1990L, 2020L).boxed().collect(Collectors.toList())));
		map.put(MetadataUtils.normalize("Právo"), new ArrayList<>(LongStream.rangeClosed(2012L, 2020L).boxed().collect(Collectors.toList())));
		map.put(MetadataUtils.normalize("Universitas"), new ArrayList<>(LongStream.rangeClosed(1989L, 1995L).boxed().collect(Collectors.toList())));
		map.get(MetadataUtils.normalize("Universitas")).addAll(Arrays.asList(2017L,2018L));
		map.put(MetadataUtils.normalize("Kritická příloha Revolver Revue"), new ArrayList<>(LongStream.rangeClosed(1995L, 1996L).boxed().collect(Collectors.toList())));
		map.put(MetadataUtils.normalize("Interview"), new ArrayList<>(LongStream.rangeClosed(2011L, 2020L).boxed().collect(Collectors.toList())));
		map.put(MetadataUtils.normalize("Tvary"), new ArrayList<>(LongStream.rangeClosed(1995L, 2005L).boxed().collect(Collectors.toList())));
		map.put(MetadataUtils.normalize("Perspektivy"), Arrays.asList(1992L,1994L,1995L,1999L));
		map.put(MetadataUtils.normalize("Xantypa"), new ArrayList<>(LongStream.rangeClosed(1995L, 2003L).boxed().collect(Collectors.toList())));
		map.get(MetadataUtils.normalize("Xantypa")).add(2005L);
	}

	private static final Map<String, List<Long>> anl = new HashMap<>();

	static {
		anl.put(MetadataUtils.normalize("Listy filologické"), Collections.singletonList(2020L));
		anl.put(MetadataUtils.normalize("Regenerace"), new ArrayList<>(LongStream.rangeClosed(1993L, 2006L).boxed().collect(Collectors.toList())));
		anl.get(MetadataUtils.normalize("Regenerace")).addAll(LongStream.rangeClosed(2017L, 2020L).boxed().collect(Collectors.toList()));
		anl.put(MetadataUtils.normalize("Svět a divadlo"), Collections.singletonList(2020L));
		anl.put(MetadataUtils.normalize("XB-1"), Collections.singletonList(2017L));
	}

	private static final Map<String, List<Long>> nkp = new HashMap<>();

	static {
		nkp.put(MetadataUtils.normalize("Biblio"), new ArrayList<>(LongStream.rangeClosed(2013L, 2020L).boxed().collect(Collectors.toList())));
	}

	@Override
	public void write(List<? extends Long> ids) {
		for (Long id : ids) {
			HarvestedRecord hr = harvestedRecordDao.get(id);
			progressLogger.incrementAndLogProgress(hr);
			try {
				MarcRecord rec = marcXmlParser.parseRecord(hr);
//				if (rec.getFields("072", 'a').isEmpty()) continue;
//				if (!Collections.disjoint(rec.getFields("072", 'a'), KONSPEKT)) continue;
				String periodical = rec.getField("773", 't');
				if (periodical == null || !map.containsKey(MetadataUtils.normalize(periodical))) continue;
				if (!map.get(MetadataUtils.normalize(periodical)).contains(mrFactory.getMetadataRecord(rec).getPublicationYear()))
					continue;
//				System.out.println(hr.getId());
				System.out.println(getMappingAsCsv(hr, rec));
			} catch (InvalidMarcException ime) {
				logger.warn("Invalid Marc in record: {}", hr.getId());
			} catch (Exception e) {
				logger.warn("Skipping record {} due to error: {}", hr.getId(), e);
			}
		}
		sessionFactory.getCurrentSession().flush();
		sessionFactory.getCurrentSession().clear();
	}

	public String getMappingAsCsv(HarvestedRecord hr, MarcRecord mr) {
		StringWriter writer = new StringWriter();
		CSVPrinter printer;
		try {
			printer = new CSVPrinter(writer, CSVFormat.EXCEL.withQuoteMode(QuoteMode.ALL));
			printer.printRecord(
					hr.getHarvestedFrom().getIdPrefix() + "." + hr.getUniqueId().getRecordId(),
					String.join(",", mr.getFields("072", 'a')),
					String.join(",", mr.getField("773", 't')),
					mr.getDataFields("100").isEmpty() ? "" : mr.getDataFields("100").get(0).getSubfields().stream().map(s -> s.getData()).collect(Collectors.joining(" ")),
					mr.getDataFields("245").isEmpty() ? "" : mr.getDataFields("245").get(0).getSubfields().stream().map(s -> s.toString()).collect(Collectors.joining("")),
					mr.getDataFields("650").isEmpty() ? "" : mr.getDataFields("650").stream().map(df -> df.getSubfields().stream().map(s -> s.toString()).collect(Collectors.joining(""))).collect(Collectors.joining("|")),
//					mr.getDataFields("651").isEmpty() ? "" : mr.getDataFields("651").stream().map(df -> df.getSubfields().stream().map(s -> s.toString()).collect(Collectors.joining(""))).collect(Collectors.joining("|")),
					mr.getDataFields("655").isEmpty() ? "" : mr.getDataFields("655").stream().map(df -> df.getSubfields().stream().map(s -> s.toString()).collect(Collectors.joining(""))).collect(Collectors.joining("|")),
					String.join(",", mr.getFields("773", 'g')),
					String.join(",", mr.getFields("773", 'x'))
//					String.join(",", mr.getFields("773", '7'))
			);
			printer.flush();
			return writer.toString().trim();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
