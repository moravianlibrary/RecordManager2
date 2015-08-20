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
		OTHER_BRAILL(10L), 
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
		OTHER_UNSPECIFIED(100L);
		
		private Long numValue;
		
		private HarvestedRecordFormatEnum(Long numValue) {
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
