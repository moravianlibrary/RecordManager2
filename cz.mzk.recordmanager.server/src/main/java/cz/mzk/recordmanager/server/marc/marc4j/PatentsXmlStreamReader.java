package cz.mzk.recordmanager.server.marc.marc4j;

import com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl;
import cz.mzk.recordmanager.server.ClasspathResourceProvider;
import cz.mzk.recordmanager.server.marc.marc4j.upv.CPCClasification;
import cz.mzk.recordmanager.server.marc.marc4j.upv.FurtherCPC;
import cz.mzk.recordmanager.server.marc.marc4j.upv.MainCPC;
import cz.mzk.recordmanager.server.marc.marc4j.upv.author.*;
import cz.mzk.recordmanager.server.scripting.MappingResolver;
import cz.mzk.recordmanager.server.scripting.ResourceMappingResolver;
import cz.mzk.recordmanager.server.util.CleaningUtils;
import cz.mzk.recordmanager.server.util.RecordUtils;
import cz.mzk.recordmanager.server.util.constants.EVersionConstants;
import org.apache.commons.lang3.StringUtils;
import org.marc4j.MarcReader;
import org.marc4j.marc.DataField;
import org.marc4j.marc.MarcFactory;
import org.marc4j.marc.Record;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatentsXmlStreamReader implements MarcReader {


	private final MappingResolver propertyResolver;

	private Record record;

	private final MarcFactory factory;

	private final Document doc;

	private static final String TEXT_LEADER = "-----nam--22--------450-";
	private static final String TEXT_008_PART1 = "------e";
	private static final String TEXT_008_PART2 = "--------sj----------";
	private static final String TEXT_013b = "Česká republika";
	private static final String TEXT_0242 = "MPT";
	private static final String TEXT_260a = "Praha :";
	private static final String TEXT_260b = "Úřad průmyslového vlastnictví,";
	private static final String TEXT_300a_UTILITY_MODEL = "1 užitný vzor";
	private static final String TEXT_300a_APPLICATION = "1 přihláška vynálezu";
	private static final String TEXT_300a_PATENT = "1 patentový spis";
	private static final String TEXT_500a_PUBLICATION_A3 = "Datum zveřejnění přihlášky: %s";
	private static final String TEXT_500a_PUBLICATION_B6 = "Datum udělení patentu: %s";
	private static final String TEXT_500a_PUBLICATION_U1 = "Datum zápisu užitného vzoru: %s";
	private static final String TEXT_500a_APPLICATION = "Datum přihlášení: %s";
	private static final String TEXT_500a_APPL_NUMBER = "Číslo přihlášky: %s";
	private static final String TEXT_500a_DOC_NUMBER = "Číslo dokumentu: %s";
	private static final String TEXT_655a_UTILITY_MODEL = "užitné vzory";
	private static final String TEXT_655a_APPLICATION = "přihlášky vynálezu";
	private static final String TEXT_655a_PATENT = "patentové spisy";
	private static final String TEXT_0722 = "Konspekt";

	private static final String DATE_STRING_005 = "yyyyMMddHHmmss'.0'";


	private static final String PATENTS_MAP = "patents.map";

	private static final Pattern WHITE_SPACES_PATTERN = Pattern.compile("\\s+");
	private static final Pattern DASH_PATTERN = Pattern.compile("-");
	private static final Pattern SPLIT_072 = Pattern.compile(" - ");

	private static final Pattern A3_PATTERN = Pattern.compile("St36_CZ_(\\d{4})-(\\d+)_A3");
	private static final String A3_URL = "https://isdv.upv.cz/doc/FullFiles/Applications/%s/PPVCZ%s_%sA3.pdf";
	private static final Pattern B6_PATTERN = Pattern.compile("St36_CZ_(\\d*)_B6");
	private static final String B6_URL = "https://isdv.upv.cz/doc/FullFiles/Patents/FullDocuments/%s/%s.pdf";
	private static final Pattern U1_PATTERN = Pattern.compile("St36_CZ_(\\d*)_U1");
	private static final String U1_URL = "https://isdv.upv.cz/doc/FullFiles/UtilityModels/FullDocuments/FDUM%s/uv%s.pdf";

	private static final String PATENT_TYPE_A3 = "A3";
	private static final String PATENT_TYPE_B6 = "B6";
	private static final String PATENT_TYPE_U1 = "U1";

	private static final String DATE_ORIGIN_FORMAT = "yyyyMMdd";
	private static final String DATE_OUTPUT_FORMAT = "d. M. yyyy";

	private static final SimpleDateFormat SDF_ORIGIN = new SimpleDateFormat(DATE_ORIGIN_FORMAT);
	private static final SimpleDateFormat SDF_OUTPUT = new SimpleDateFormat(DATE_OUTPUT_FORMAT);

	private String kindCode = null;
	private String docNumber = null;

	/**
	 * Constructs an instance with the specified input stream.
	 */
	public PatentsXmlStreamReader(InputStream input) {
		factory = MarcFactoryImpl.newInstance();
		propertyResolver = new ResourceMappingResolver(new ClasspathResourceProvider());

		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactoryImpl.newInstance().newDocumentBuilder();
			doc = builder.parse(input);
			doc.getDocumentElement().normalize();
			parse();
		} catch (SAXException | ParserConfigurationException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns true if the iteration has more records, false otherwise.
	 */
	public boolean hasNext() {
		return record != null;
	}

	public Record next() {
		Record localRecord = record;
		record = null;
		return RecordUtils.sortFields(localRecord);
	}


	/**
	 * Returns the next record in the iteration.
	 *
	 * @return Record - the record object
	 */
	public Record parse() {
		record = factory.newRecord();
		addIdentifier();
		addFields();
		addField245();
		addDocNumber();
		addApplicationIdentification();
		addField024();
		addField072and653();
		addAbstract();
		addAuthors();
		addUrl();

		return RecordUtils.sortFields(record);
	}

	private void addField500aDate(String text, String date) {
		try {
			record.addVariableField(factory.newDataField("500", ' ', ' ', "a",
					String.format(text, SDF_OUTPUT.format(SDF_ORIGIN.parse(date)))));
		} catch (ParseException ignore) {
		}
	}

	private void add500aApplDocNumber(String data, String patentType) {
		if (patentType.equals(PATENT_TYPE_B6)) {
			record.addVariableField(factory.newDataField("500", ' ', ' ', "a", String.format(TEXT_500a_APPL_NUMBER, data)));
		}
	}

	private void addField024() {
		JAXBContext jaxbContext;
		try {
			Node node = doc.getElementsByTagName("pat:MainCPC").item(0);
			jaxbContext = JAXBContext.newInstance(MainCPC.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			MainCPC mainCpc = (MainCPC) unmarshaller.unmarshal(node);
			record.addVariableField(factory.newDataField("024", '7', ' ', "a",
					mainCpc.getCpcClasification().get024a(), "2", TEXT_0242));
		} catch (JAXBException | NullPointerException ignore) {
		}
		try {
			Node node = doc.getElementsByTagName("pat:FurtherCPC").item(0);
			jaxbContext = JAXBContext.newInstance(FurtherCPC.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			FurtherCPC FurtherCPC = (FurtherCPC) unmarshaller.unmarshal(node);
			for (CPCClasification classifications : FurtherCPC.getCpcClasifications()) {
				record.addVariableField(factory.newDataField("024", '7', ' ', "a",
						classifications.get024a(), "2", TEXT_0242));
			}
		} catch (JAXBException | NullPointerException ignore) {
		}
	}

	private void addIdentifier() {
		Node node = doc.getElementsByTagName("pat:PatentPublicationIdentification").item(0);
		if (node == null) return;
		NodeList childs = node.getChildNodes();
		if (childs.getLength() > 0) {
			String officeCode = null;
			for (int i = 0; i < childs.getLength(); i++) {
				Node detail = childs.item(i);

				switch (detail.getNodeName()) {
					case "com:IPOfficeCode":
						officeCode = detail.getTextContent();
						break;
					case "pat:PublicationNumber":
						this.docNumber = detail.getTextContent();
						break;
					case "com:PatentDocumentKindCode":
						this.kindCode = detail.getTextContent();
						break;
					case "com:PublicationDate":
						String dateStr = CleaningUtils.replaceAll(detail.getTextContent(), DASH_PATTERN, "");
						addField008(dateStr);
						addField260(dateStr);
						addField013(this.docNumber, this.kindCode, dateStr);
						switch (this.kindCode) {
							case PATENT_TYPE_A3:
								addField500aDate(TEXT_500a_PUBLICATION_A3, dateStr);
								break;
							case PATENT_TYPE_B6:
								addField500aDate(TEXT_500a_PUBLICATION_B6, dateStr);
								break;
							case PATENT_TYPE_U1:
								addField500aDate(TEXT_500a_PUBLICATION_U1, dateStr);
								break;
							default:
								break;
						}
						break;
				}
			}
			record.addVariableField(factory.newControlField("001", String.format("St36_%s_%s_%s", officeCode, this.docNumber, this.kindCode)));
		}
	}

	private void addApplicationIdentification() {
		Node node = doc.getElementsByTagName("pat:ApplicationIdentification").item(0);
		if (node == null) return;
		add500aApplDocNumber(this.docNumber, this.kindCode);
		NodeList childs = node.getChildNodes();
		if (childs.getLength() > 0) {
			for (int i = 0; i < childs.getLength(); i++) {
				Node detail = childs.item(i);
				if (detail.getNodeName().equals("pat:FilingDate")) {
					String dateStr = CleaningUtils.replaceAll(detail.getTextContent(), DASH_PATTERN, "");
					addField500aDate(TEXT_500a_APPLICATION, dateStr);
				}
			}
		}
	}

	private void addDocNumber() {
		record.addVariableField(factory.newDataField("500", ' ', ' ', "a",
				String.format(TEXT_500a_DOC_NUMBER, this.docNumber)));
	}

	private void addField008(String date) {
		if (date != null) {
			String s008 = TEXT_008_PART1 + date + TEXT_008_PART2;
			record.addVariableField(factory.newControlField("008", s008));
		}
	}

	private void addFields() {
		record.setLeader(factory.newLeader(TEXT_LEADER));

		SimpleDateFormat sdf = new SimpleDateFormat(DATE_STRING_005);
		record.addVariableField(factory.newControlField("005", sdf.format(new Date())));

		String f001 = record.getControlNumber();
		if (f001 != null) {
			String text300 = "";
			String text655 = "";
			if (kindCode.equals(PATENT_TYPE_A3)) {
				text300 = TEXT_300a_APPLICATION;
				text655 = TEXT_655a_APPLICATION;
			} else if (kindCode.equals(PATENT_TYPE_U1)) {
				text300 = TEXT_300a_UTILITY_MODEL;
				text655 = TEXT_655a_UTILITY_MODEL;
			} else if (kindCode.equals(PATENT_TYPE_B6)) {
				text300 = TEXT_300a_PATENT;
				text655 = TEXT_655a_PATENT;
			}
			record.addVariableField(factory.newDataField("300", ' ', ' ', "a", text300));

			record.addVariableField(factory.newDataField("655", '4', ' ', "a", text655));
		}
	}

	private void addField260(String date) {
		if (date != null && date.length() >= 4) {
			DataField df = factory.newDataField("260", ' ', ' ');
			df.addSubfield(factory.newSubfield('a', TEXT_260a));
			df.addSubfield(factory.newSubfield('b', TEXT_260b));
			df.addSubfield(factory.newSubfield('c', date.substring(0, 4)));
			record.addVariableField(df);
		}
	}

	private DataField createField072(String text) {
		String temp[] = SPLIT_072.split(text);
		DataField df = factory.newDataField("072", '7', ' ');
		df.addSubfield(factory.newSubfield('a', temp[0].split(" ")[1]));
		df.addSubfield(factory.newSubfield('x', temp[1]));
		df.addSubfield(factory.newSubfield('2', TEXT_0722));
		df.addSubfield(factory.newSubfield('9', temp[0].split(" ")[0]));

		return df;
	}

	private void addUrl() {
		String url = null;
		Matcher matcher = A3_PATTERN.matcher(record.getControlNumber());
		if (matcher.matches()) {
			url = String.format(A3_URL, matcher.group(1), matcher.group(1), StringUtils.leftPad(matcher.group(2), 4, '0'));
		}

		matcher = B6_PATTERN.matcher(record.getControlNumber());
		if (matcher.matches()) {
			String dir;
			if (matcher.group(1).length() <= 3) dir = "0";
			else {
				dir = matcher.group(1).substring(0, matcher.group(1).length() - 3);
			}
			url = String.format(B6_URL, dir, matcher.group(1));
		}

		matcher = U1_PATTERN.matcher(record.getControlNumber());
		if (matcher.matches()) {
			String dir;
			if (matcher.group(1).length() <= 3) dir = "0000";
			else {
				dir = StringUtils.leftPad(matcher.group(1).substring(0, matcher.group(1).length() - 3), 4, '0');
			}
			url = String.format(U1_URL, dir, StringUtils.leftPad(matcher.group(1), 6, '0'));
		}

		if (url != null) {
			DataField df = factory.newDataField("856", '4', ' ', "u", url,
					"y", EVersionConstants.FULLTEXT_LINK);
			record.addVariableField(df);
		}
	}

	private void addField013(String docNumber, String patentType, String date) {
		record.addVariableField(factory.newDataField("013", '#', '#',
				"a", "CZ " + docNumber + ' ' + patentType, "b", TEXT_013b,
				"c", patentType, "d", date));
	}

	private void addField245() {
		NodeList nodeList = doc.getElementsByTagName("pat:InventionTitle");
		if (nodeList.getLength() > 0) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node detail = nodeList.item(i);
				switch (detail.getAttributes().getNamedItem("com:languageCode").getNodeValue()) {
					case "cs":
						record.addVariableField(factory.newDataField("245", ' ', ' ', "a", detail.getTextContent()));
						break;
					case "en":
						record.addVariableField(factory.newDataField("246", '2', ' ', "a", detail.getTextContent()));
						break;
				}
			}
		}
	}

	private void addField072and653() {
		JAXBContext jaxbContext;
		try {
			Node node = doc.getElementsByTagName("pat:MainCPC").item(0);
			jaxbContext = JAXBContext.newInstance(MainCPC.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			MainCPC mainCpc = (MainCPC) unmarshaller.unmarshal(node);

			List<String> get = propertyResolver.resolve(PATENTS_MAP).get(mainCpc.getCpcClasification().get072Key());
			if (get != null) {
				String[] temp = get.get(0).split("\\|");
				if (temp.length == 2) {
					record.addVariableField(factory.newDataField("653", ' ', ' ', "a", temp[0]));
					record.addVariableField(createField072(temp[1]));
				}
			}
		} catch (JAXBException | IOException | NullPointerException ignore) {
		}
	}

	private void addAbstract() {
		Node node = doc.getElementsByTagName("pat:Abstract").item(0);
		if (node == null) return;
		if (node.getAttributes().getNamedItem("com:languageCode").getNodeValue().equals("cs")) {
			record.addVariableField(factory.newDataField("520", '3', ' ', "a",
					CleaningUtils.replaceAll(node.getTextContent().trim(), WHITE_SPACES_PATTERN, " ")));
		}
	}

	private String tagFieldAuthor = "100";
	private String tagFieldCorporate = "110";

	private void addAuthors() {
		JAXBContext jaxbContext;
		try {
			Node node = doc.getElementsByTagName("pat:ApplicantBag").item(0);
			jaxbContext = JAXBContext.newInstance(ApplicantBag.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			ApplicantBag applicantBag = (ApplicantBag) unmarshaller.unmarshal(node);
			for (Applicant applicant : applicantBag.getApplicants()) {
				addAuthor(applicant.getContact().getName(), "pta");
			}
		} catch (JAXBException | NullPointerException ignore) {
		}
		try {
			Node node = doc.getElementsByTagName("pat:InventorBag").item(0);
			jaxbContext = JAXBContext.newInstance(InventorBag.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			InventorBag inventorBag = (InventorBag) unmarshaller.unmarshal(node);
			for (Inventor inventor : inventorBag.getInventors()) {
				addAuthor(inventor.getContact().getName(), "inv");
			}
		} catch (JAXBException | NullPointerException ignore) {
		}
		try {
			Node node = doc.getElementsByTagName("pat:RegisteredPractitionerBag").item(0);
			jaxbContext = JAXBContext.newInstance(RegisteredPractitionerBag.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			RegisteredPractitionerBag registeredPractitionerBag = (RegisteredPractitionerBag) unmarshaller.unmarshal(node);
			for (RegisteredPractitioner registeredPractitioner : registeredPractitionerBag.getRegisteredPractitioners()) {
				addAuthor(registeredPractitioner.getContact().getName(), "pth");
			}
		} catch (JAXBException | NullPointerException ignore) {
		}
	}

	private void addAuthor(Name name, String sfu) {
		if (name.getOrganizationName() != null) {
			record.addVariableField(factory.newDataField(tagFieldCorporate, '1', ' ', "a",
					CleaningUtils.replaceAll(name.getOrganizationName()
							.getOrganizationStandardName(), WHITE_SPACES_PATTERN, " "), "u", sfu));
			tagFieldCorporate = "710";
		}
		if (name.getPersonName() != null) {
			record.addVariableField(factory.newDataField(tagFieldAuthor, '1', ' ', "a",
					name.getPersonName().getPersonStructuredName().getNameForMarc(), "u", sfu));
			tagFieldAuthor = "700";
		}
	}
}
