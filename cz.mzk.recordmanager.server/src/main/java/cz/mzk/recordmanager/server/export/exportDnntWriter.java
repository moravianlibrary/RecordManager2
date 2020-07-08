package cz.mzk.recordmanager.server.export;

import cz.mzk.recordmanager.server.marc.MarcXmlParser;
import cz.mzk.recordmanager.server.metadata.MetadataRecordFactory;
import cz.mzk.recordmanager.server.model.HarvestedRecord;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import cz.mzk.recordmanager.server.util.Constants;
import cz.mzk.recordmanager.server.util.ProgressLogger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang3.StringUtils;
import org.marc4j.marc.DataField;
import org.marc4j.marc.Record;
import org.marc4j.marc.Subfield;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class exportDnntWriter implements ItemWriter<String>, StepExecutionListener {

	private static final Logger logger = LoggerFactory.getLogger(exportDnntWriter.class);

	@Autowired
	private MarcXmlParser marcXmlParser;

	@Autowired
	private MetadataRecordFactory metadataRecordFactory;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	private final ProgressLogger progressLogger;

	private final String fileName;

	private CSVPrinter csvPrinter;

	public exportDnntWriter(String fileName) {
		progressLogger = new ProgressLogger(logger, 10000);
		this.fileName = fileName;
	}

	private Map<String, String> caslin = new HashMap<>();

	@Override
	public void write(List<? extends String> recordIds) throws Exception {
		if (caslin.isEmpty()) {

			BufferedReader br = new BufferedReader(new FileReader("/home/tomas/skc"));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				try {
					String id = "SKC01-" + StringUtils.leftPad(line, 9, '0');
					HarvestedRecord hr = harvestedRecordDao.findByIdAndHarvestConfiguration(id, 316L);
					if (hr == null) continue;
					for (DataField df : marcXmlParser.parseRecord(hr).getDataFields("996")) {
						if (df.getSubfield('e') != null && df.getSubfield('e').getData().equals("BOA001") && df.getSubfield('w') != null) {
							HarvestedRecord hrMzk = harvestedRecordDao.findByHarvestConfAndRaw001Id(300L, df.getSubfield('w').getData());
							if (hrMzk == null) continue;
							caslin.put(hrMzk.getUniqueId().getRecordId(), id);
						}
					}
				} catch (Exception ignore) {
				}
			}
		}
//		try (BufferedReader br = new BufferedReader(new FileReader("/home/tomas/Downloads/map_2007_ids.csv"))) {
//			for (String line; (line = br.readLine()) != null; ) {
		for (String recordId : recordIds) {
			progressLogger.incrementAndLogProgress();
//				HarvestedRecord hr = harvestedRecordDao.findByIdAndHarvestConfiguration(
//						"MZK01-" + StringUtils.leftPad(line.trim(), 9, '0'), 300L);
			HarvestedRecord hr = harvestedRecordDao.findByIdAndHarvestConfiguration(recordId, 300L);
			if (hr == null) continue;
			Record rec;
			try {
				rec = marcXmlParser.parseUnderlyingRecord(hr);
				Set<String> cnbs = new HashSet<>();
				List<String> isbns = new ArrayList<>();
				List<String> issns = new ArrayList<>();
				String caslinSysNo = "";
				String fmt = "";
				String author = "";
				String date = "";
				String title = "";
				String place260 = "";
				String publisher260 = "";
				String publicationYear260 = "";
				String place264 = "";
				String publisher264 = "";
				String publicationYear264 = "";
				for (DataField df : rec.getDataFields()) {
					if (df.getTag().equals("015") && df.getSubfield('a') != null) {
						cnbs.add(df.getSubfield('a').getData());
					}
					if (df.getTag().equals("020") && df.getSubfield('a') != null) {
						StringBuilder result = new StringBuilder(df.getSubfield('a').getData());
						for (Subfield sf : df.getSubfields('q')) {
							if (!result.toString().isEmpty()) result.append(" ");
							result.append(sf.getData());
						}
						isbns.add(result.toString());
					}
					if (df.getTag().equals("022") && df.getSubfield('a') != null) {
						issns.add(df.getSubfield('a').getData());
					}
					if (df.getTag().equals("100") && df.getSubfield('a') != null) {
						author = df.getSubfield('a').getData();
					}
					if (df.getTag().equals("100") && df.getSubfield('d') != null) {
						date = df.getSubfield('d').getData();
					}
					if (df.getTag().equals("245")) {
						title = df.getSubfields().stream().map(Subfield::getData).collect(Collectors.joining(" "));
						title = CleaningUtils.replaceAll(title, Pattern.compile(" +"), " ");
					}
					if (df.getTag().equals("260")) {
						if (df.getSubfield('a') != null) {
							place260 = df.getSubfield('a').getData();
							if (place260.substring(place260.length() -1).equals(":")) {
								place260 += " ";
							}
						}
						if (df.getSubfield('b') != null) {
							publisher260 = df.getSubfield('b').getData();
						}
						if (df.getSubfield('c') != null) {
							publicationYear260 = df.getSubfield('c').getData();
						}
					}
					if (df.getTag().equals("264")) {
						if (df.getSubfield('a') != null) {
							place264 = df.getSubfield('a').getData();
							if (place264.substring(place264.length() -1).equals(":")) place264 += " ";
						}
						if (df.getSubfield('b') != null) {
							publisher264 = df.getSubfield('b').getData();
						}
						if (df.getSubfield('c') != null) {
							publicationYear264 = df.getSubfield('c').getData();
						}
					}
					if (df.getTag().equals("990") && df.getSubfield('a') != null) {
						fmt = df.getSubfield('a').getData();
					}
				}
				if (!Arrays.asList("BK").contains(fmt)) continue;
				for (HarvestedRecord otherHr : harvestedRecordDao.getByDedupRecordWithDeleted(hr.getDedupRecord())) {
					if (!Arrays.asList("nkp", "caslin").contains(otherHr.getHarvestedFrom().getIdPrefix()))
						continue;
					try {
						rec = marcXmlParser.parseUnderlyingRecord(otherHr);
					} catch (Exception ignore) {
						continue;
					}
					for (DataField df : rec.getDataFields()) {
						if (cnbs.isEmpty() && df.getTag().equals("015") && df.getSubfield('a') != null) {
							cnbs.add(df.getSubfield('a').getData());
						}
					}
					if (otherHr.getHarvestedFrom().getIdPrefix().equals(Constants.PREFIX_CASLIN)) {
						caslinSysNo = otherHr.getUniqueId().getRecordId().substring(6);
					}
				}
				if (caslinSysNo.isEmpty()) {
					caslinSysNo = caslin.get(hr.getUniqueId().getRecordId()) == null ? "" : hr.getUniqueId().getRecordId().substring(6);
				}
				List<String> isxn = new ArrayList<>();
				if (!isbns.isEmpty()) isxn.add(String.join("|", isbns));
				if (!issns.isEmpty()) isxn.add(String.join("|", issns));
				csvPrinter.printRecord(caslinSysNo, hr.getUniqueId().getRecordId(), fmt, String.join("|", cnbs),
						String.join("/", isxn), author, date, title,
						(place260 + publisher260).isEmpty() ? place264 + publisher264 : place260 + publisher260,
						publicationYear260.isEmpty() ? publicationYear264 : publicationYear260);
			} catch (Exception e) {
				logger.error("Unable to parse input " + hr.getUniqueId().getRecordId());
				continue;
			}
		}
		csvPrinter.flush();
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try {
			BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName));
			csvPrinter = new CSVPrinter(writer, CSVFormat.DEFAULT.withDelimiter(';').withQuoteMode(QuoteMode.ALL)
					.withHeader("sysNoSKC", "sysNo", "FMT", "čČNB", "ISBN/ISSN", "Autor", "ŽivData", "Název", "MístoVyd",
							"RokVydani"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		try {
			csvPrinter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
