package cz.mzk.recordmanager.server.dedup.clustering;

import cz.mzk.recordmanager.server.util.StringUtils;

public class TitleClusterable implements Clusterable {

	protected int MINIMAL_TITILE_MATCH_PERCENTAGE = 70;
	
	protected int TITLE_PREFIX_BOUNDARY = 8;
	
	private Long id;
	
	private String title;
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	@Override
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	
	
	@Override
	public int computeSimilarityPercentage(Clusterable other) {
		if (other == null || !(other instanceof TitleClusterable)) {
			return 0;
		}
		
		TitleClusterable otherTitleClusterable = (TitleClusterable) other;
		return StringUtils.simmilarTitleMatchPercentage(
					this.getTitle(), //
					otherTitleClusterable.getTitle(), //
					MINIMAL_TITILE_MATCH_PERCENTAGE, //
					TITLE_PREFIX_BOUNDARY);
	}

}
