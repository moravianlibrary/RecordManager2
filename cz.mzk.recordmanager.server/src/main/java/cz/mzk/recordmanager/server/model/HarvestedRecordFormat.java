package cz.mzk.recordmanager.server.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name=HarvestedRecordFormat.TABLE_NAME)
public class HarvestedRecordFormat {
	
	public static final String TABLE_NAME = "harvested_record_format";
	public static final String LINK_TABLE_NAME = "harvested_record_format_link";
	
	public enum HarvestedRecordFormatEnum {
		BOOKS(1L), 
		PERIODICALS(2L), 
		ARTICLES(3L), 
		MAPS(4L), 
		MUSICAL_SCORES(5L), 
		VISUAL_DOCUMENTS(6L), 
		OTHER_MICROFORMS(8L),
		OTHER_BRAILLE(10L), 
		ELECTRONIC_SOURCE(11L), 
		AUDIO_DOCUMENTS(12L), 
		AUDIO_CD(13L), 
		AUDIO_DVD(14L), 
		AUDIO_LP(15L), 
		AUDIO_CASSETTE(16L), 
		AUDIO_OTHER(17L), 
		VIDEO_DOCUMENTS(18L),
		VIDEO_BLURAY(19L),
		VIDEO_VHS(20L),
		VIDEO_DVD(21L),
		VIDEO_CD(22L),
		VIDEO_OTHER(23L),
		OTHER_KIT(24L),
		OTHER_OBJECT(25L),
		OTHER_MIX_DOCUMENT(26L),
		NORMS(27L),
		OTHER_PERSON(28L),
		LEGISLATIVE_GOVERNMENT_ORDERS(29L), // nařízení vlády
		LEGISLATIVE_REGULATIONS(30L), // nařízení
		LEGISLATIVE_COMMUNICATION(31L), // sdělení
		LEGISLATIVE_LAWS(32L), // zákony
		LEGISLATIVE_LAWS_TEXT(33L), // úplné znění zákona
		LEGISLATIVE_FINDING(34L), // nálezy ústavního soudu
		LEGISLATIVE_CONSTITUTIONAL_LAWS(35L), // ústavní zákony
		LEGISLATIVE_DECISIONS(36L), // rozhodnutí
		LEGISLATIVE_DECREES(37L), // dekrety
		LEGISLATIVE_EDICTS(38L), // výnosy
		LEGISLATIVE_RESOLUTIONS(39L), // usnesení
		LEGISLATIVE_MEASURES(40L), // opatření
		LEGISLATIVE_DIRECTIVES(41L), // směrnice
		LEGISLATIVE_TREATIES(42L), // smlouvy
		LEGISLATIVE_EDITORIAL(43L), //redakční oznámení
		LEGISLATIVE_RULES(44L), // pravidla
		LEGISLATIVE_ORDERS(45L), // rozkazy
		LEGISLATIVE_PROCEDURES(46L), // postupy
		LEGISLATIVE_STATUTES(47L), // stanovy
		LEGISLATIVE_CONVENTIONS(48L), // úmluvy
		LEGISLATIVE_PRINCIPLES(49L), // zásady
		LEGISLATIVE_AGREEMENTS(50L), // dohody
		LEGISLATIVE_GUIDELINES(51L), // pokyny
		LEGISLATIVE_ORDINANCES(52L), // vyhlášky
		LEGISLATIVE_SENATE_MEASURES(53L), // zákonná opatření senátu
		LEGISLATIVE_CONDITIONS(54L), // podmínky
		LEGISLATIVE_OTHERS(55L),
		LEGISLATIVE_NOTICE(56L), // oznámení
		LEGISLATIVE_CODE(57L), // řád
		PATENTS(58L),
		OTHER_COMPUTER_CARRIER(59L),
		OTHER_OTHER(60L),
		PATENTS_UTILITY_MODELS(61L),
		PATENTS_PATENT_APPLICATIONS(62L),
		PATENTS_PATENTS(63L),
		OTHER_DICTIONARY_ENTRY(64L),
		OTHER_UNSPECIFIED(100L);

		private Long numValue;
		
		HarvestedRecordFormatEnum(Long numValue) {
			this.numValue = numValue;
		}
		
		public Long getNumValue() {
			return numValue;
		}
		
		public static HarvestedRecordFormatEnum stringToHarvestedRecordFormatEnum(String input) {
			try {
				return HarvestedRecordFormatEnum.valueOf(input.toUpperCase());
			} catch (IllegalArgumentException iax) {
				return HarvestedRecordFormatEnum.OTHER_UNSPECIFIED;
			}
		}
	}
	
	@Id
	@Column(name="id")
	private Long id;
	
	@Column(name="name")
	private String name;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	} 
}
