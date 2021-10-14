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
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

	@Override
	public void write(List<? extends Long> ids) {
		for (Long id : ids) {
			HarvestedRecord hr = harvestedRecordDao.get(id);
			progressLogger.incrementAndLogProgress(hr);

			try {
				MarcRecord rec = marcXmlParser.parseRecord(hr);
				if (rec.getFields("072", 'a').isEmpty()) continue;
				if (!Collections.disjoint(rec.getFields("072", 'a'), KONSPEKT)) continue;
				String periodical = rec.getField("773", 't');
				if (periodical == null || !PERIODICALS.contains(MetadataUtils.normalize(periodical))) continue;
				if (MetadataUtils.normalize(periodical).equals("duha")) {
					Long year = mrFactory.getMetadataRecord(rec).getPublicationYear();
					if (!(year.equals(1989L) || year.equals(1990L))) continue;
				}
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
			printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withQuoteMode(QuoteMode.ALL));
			printer.printRecord(
					hr.getHarvestedFrom().getIdPrefix() + "." + hr.getUniqueId().getRecordId(),
					String.join(",", mr.getFields("072", 'a')),
					String.join(",", mr.getField("773", 't')),
					mr.getDataFields("100").isEmpty() ? "" : mr.getDataFields("100").get(0).getSubfields().stream().map(s -> s.getData()).collect(Collectors.joining(" ")),
					mr.getDataFields("245").isEmpty() ? "" : mr.getDataFields("245").get(0).getSubfields().stream().map(s -> s.toString()).collect(Collectors.joining("")),
					mr.getDataFields("650").isEmpty() ? "" : mr.getDataFields("650").stream().map(df -> df.getSubfields().stream().map(s -> s.toString()).collect(Collectors.joining(""))).collect(Collectors.joining("|")),
					mr.getDataFields("650").isEmpty() ? "" : mr.getDataFields("651").stream().map(df -> df.getSubfields().stream().map(s -> s.toString()).collect(Collectors.joining(""))).collect(Collectors.joining("|")),
					mr.getDataFields("650").isEmpty() ? "" : mr.getDataFields("655").stream().map(df -> df.getSubfields().stream().map(s -> s.toString()).collect(Collectors.joining(""))).collect(Collectors.joining("|")),
					String.join(",", mr.getFields("773", '7')),
					String.join(",", mr.getFields("773", 'x')),
					String.join(",", mr.getFields("773", 'g'))
			);
			printer.flush();
			return writer.toString().trim();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
