package cz.mzk.recordmanager.server.bibliolinker.keys;

import cz.mzk.recordmanager.server.metadata.MetadataRecord;
import cz.mzk.recordmanager.server.model.*;
import cz.mzk.recordmanager.server.oai.dao.HarvestedRecordDAO;
import cz.mzk.recordmanager.server.util.MetadataUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Abstract BiblioLinkerKeyParser implementation
 * <p>
 * This implementation solves problem with repeated creation of biblio linker keys
 */
public abstract class HashingBiblioLinkerKeyParser implements BiblioLinkerKeysParser {

	private final static int EFFECTIVE_TITLE_LENGTH = 255;
	private final static int EFFECTIVE_AUTHOR_LENGTH = 200;
	private final static int EFFECTIVE_LENGTH_200 = 200;

	@Autowired
	private HarvestedRecordDAO harvestedRecordDao;

	@Override
	public HarvestedRecord parse(HarvestedRecord record,
			MetadataRecord metadataRecord) throws BiblioLinkerKeyParserException {

		if (!record.getHarvestedFrom().isGenerateBiblioLinkerKeys()) return record;

		boolean biblioLinkerKeysChanged = false;
		boolean oaiTimestampChanged;

		BiblioLinkerKeysencapsulator encapsulator = new BiblioLinkerKeysencapsulator();

		List<BLTitle> blTitles = new ArrayList<>();
		for (BLTitle blTitle : metadataRecord.getBLTitle()) {
			blTitle.setBLTitleStr(MetadataUtils.normalizeAndShorten(
					blTitle.getBLTitleStr(),
					EFFECTIVE_TITLE_LENGTH));
			if (blTitle.getBLTitleStr().isEmpty()) continue;
			if (!blTitles.contains(blTitle)) {
				blTitles.add(blTitle);
			}
		}
		encapsulator.setBlTitles(blTitles);
		List<BlCommonTitle> blCommonTitles = new ArrayList<>();
		for (BlCommonTitle blCommonTitle : metadataRecord.getBiblioLinkerCommonTitle()) {
			blCommonTitle.setBlCommonTitleStr(MetadataUtils.normalizeAndShorten(
					blCommonTitle.getBlCommonTitleStr(),
					EFFECTIVE_TITLE_LENGTH));
			if (blCommonTitle.getBlCommonTitleStr().isEmpty()) continue;
			if (!blCommonTitles.contains(blCommonTitle)) {
				blCommonTitles.add(blCommonTitle);
			}
		}
		encapsulator.setBlCommonTitle(blCommonTitles);

		encapsulator.setBlAuthor(MetadataUtils.normalizeAndShorten(metadataRecord.getBiblioLinkerAuthor(), EFFECTIVE_AUTHOR_LENGTH));
		encapsulator.setBlPublisher(MetadataUtils.normalizeAndShorten(metadataRecord.getBiblioLinkerPublisher(), EFFECTIVE_LENGTH_200));
		encapsulator.setBlSeries(MetadataUtils.normalizeAndShorten(metadataRecord.getBiblioLinkerSeries(), EFFECTIVE_LENGTH_200));
		encapsulator.setBlTopicKey(metadataRecord.getBiblioLinkerTopicKey());
		encapsulator.setBlEntities(metadataRecord.getBiblioLinkerEntity());
		encapsulator.setBlLanguages(metadataRecord.getBiblioLinkerLanguages());
		String computedHash = computeHashValue(encapsulator);
		String oldHash = record.getBiblioLinkerKeysHash();
		String temporalHash = record.getTemporalBiblioLinkerHash() == null
				? "0000000000000000000000000000000000000000" : record.getTemporalBiblioLinkerHash();

		// decide whether keys changed and should be updated in database
		// if temporal hash matches current hash, keys won't be updated
		// this prevents errors during processing one record multiple times
		// during one batch
		if ((!temporalHash.equals(computedHash)) &&
				(oldHash == null || oldHash.isEmpty() || !computedHash.equals(oldHash))) {
			// keys changed, updated in database
			biblioLinkerKeysChanged = true;

			// drop old keys
			harvestedRecordDao.dropBilioLinkerKeys(record);

			// assign new keys
			record.setBlTitles(encapsulator.getBlTitles());
			record.setBlAuthor(encapsulator.getBlAuthor());
			record.setBlPublisher(encapsulator.getBlPublisher());
			record.setBlSeries(encapsulator.getBlSeries());
			record.setBlCommonTitle(encapsulator.getBlCommonTitle());
			record.setBlTopicKey(encapsulator.getBlTopicKey());
			record.setBlEntity(encapsulator.getBlEntities());
			record.setBlLanguages(encapsulator.getBlLanguages());
			record.setTemporalBiblioLinkerHash(computedHash);
		}
		record.setBiblioLinkerKeysHash(computedHash);

		oaiTimestampChanged = record.getOaiTimestamp() != null && record.getTemporalOldOaiTimestamp() != null
				&& !record.getOaiTimestamp().equals(record.getTemporalOldOaiTimestamp());

		// decide whether record should be bl
		if (biblioLinkerKeysChanged) {
			// new record or change in keys
			record.setNextBiblioLinkerFlag(true);
			record.setNextBiblioLinkerSimilarFlag(true);
		} else {
			// key are equal
			if (oaiTimestampChanged) {
				// neither keys neither oai timestamp changed, 
				// don't bl
				record.setNextBiblioLinkerFlag(false);
				record.setNextBiblioLinkerSimilarFlag(false);
			} else {
				// keys are same but timestamp changed
				// keep previsous bl flag
				// this may happen during repeated harvesting before 
			}
		}
		return record;
	}

	/**
	 * Compute SHA1 hash of biblio linker keys from given {@link BiblioLinkerKeysencapsulator}
	 *
	 * @param encapsulator {@link BiblioLinkerKeysencapsulator}
	 * @return Hash as String
	 */
	protected String computeHashValue(final BiblioLinkerKeysencapsulator encapsulator) {

		try {
			// change of hash function also requires changes in database row
			MessageDigest md = MessageDigest.getInstance("SHA-1");

			if (encapsulator.getBlAuthor() != null) {
				md.update(encapsulator.getBlAuthor().getBytes());
			}

			if (encapsulator.getBlPublisher() != null) {
				md.update(encapsulator.getBlPublisher().getBytes());
			}

			if (encapsulator.getBlSeries() != null) {
				md.update(encapsulator.getBlSeries().getBytes());
			}

			for (BLTitle blTitle : encapsulator.getBlTitles()) {
				md.update(blTitle.getBLTitleStr().getBytes("utf-8"));
			}

			for (BlCommonTitle blCommonTitle : encapsulator.getBlCommonTitle()) {
				md.update(blCommonTitle.getBlCommonTitleStr().getBytes("utf-8"));
			}

			for (BLEntity blEntity : encapsulator.getBlEntities()) {
				md.update(blEntity.getBLEntityStr().getBytes("utf-8"));
			}

			for (BLTopicKey blTopicKey : encapsulator.getBlTopicKey()) {
				md.update(blTopicKey.getBLTopicKeyStr().getBytes("utf-8"));
			}

			for (BLLanguage blLanguage : encapsulator.getBlLanguages()) {
				md.update(blLanguage.getBLLanguageStr().getBytes("utf-8"));
			}

			byte[] hash = md.digest();
			StringBuilder sb = new StringBuilder();
			for (byte b : hash) {
				sb.append(String.format("%02x", b));
			}

			return sb.toString();
		} catch (NoSuchAlgorithmException e) {
			// should never be thrown, SHA-1 is required by Java specification
		} catch (UnsupportedEncodingException uee) {
			throw new BiblioLinkerKeyParserException("Uncoding problems in hash computation", uee);
		}
		return "";
	}

	protected class BiblioLinkerKeysencapsulator {
		List<BLTitle> blTitles = new ArrayList<>();
		List<BlCommonTitle> blCommonTitle = new ArrayList<>();
		List<BLEntity> blEntities = new ArrayList<>();
		List<BLTopicKey> blTopicKey = new ArrayList<>();
		List<BLLanguage> blLanguages = new ArrayList<>();

		String blAuthor;
		String blPublisher;
		String blSeries;

		public String getBlAuthor() {
			return blAuthor;
		}

		public void setBlAuthor(String blAuthor) {
			this.blAuthor = blAuthor;
		}

		public String getBlPublisher() {
			return blPublisher;
		}

		public void setBlPublisher(String blPublisher) {
			this.blPublisher = blPublisher;
		}

		public List<BLTitle> getBlTitles() {
			return blTitles;
		}

		public void setBlTitles(List<BLTitle> blTitles) {
			this.blTitles = blTitles;
		}

		public String getBlSeries() {
			return blSeries;
		}

		public void setBlSeries(String blSeries) {
			this.blSeries = blSeries;
		}

		public List<BlCommonTitle> getBlCommonTitle() {
			return blCommonTitle;
		}

		public void setBlCommonTitle(List<BlCommonTitle> blCommonTitle) {
			this.blCommonTitle = blCommonTitle;
		}

		public List<BLEntity> getBlEntities() {
			return blEntities;
		}

		public void setBlEntities(List<BLEntity> blEntities) {
			this.blEntities = blEntities;
		}

		public List<BLTopicKey> getBlTopicKey() {
			return blTopicKey;
		}

		public void setBlTopicKey(List<BLTopicKey> blTopicKey) {
			this.blTopicKey = blTopicKey;
		}

		public List<BLLanguage> getBlLanguages() {
			return blLanguages;
		}

		public void setBlLanguages(List<BLLanguage> blLanguages) {
			this.blLanguages = blLanguages;
		}
	}

}
