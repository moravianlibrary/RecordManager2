package cz.mzk.recordmanager.server.model;

import cz.mzk.recordmanager.server.util.MetadataUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = BLTopicKey.TABLE_NAME)
public class BLTopicKey extends AbstractDomainObject {

	public static final String TABLE_NAME = "bl_topic_key";

	@Column(name = "topic_key")
	private String topicKey = "";

	public static BLTopicKey create(final String value) {
		BLTopicKey newTopicKey = new BLTopicKey();
		newTopicKey.setBLTopicKeyStr(value);
		return newTopicKey;
	}

	public String getBLTopicKeyStr() {
		return topicKey;
	}

	public void setBLTopicKeyStr(String topicKey) {
		this.topicKey = MetadataUtils.normalizeAndShorten(topicKey, 20);
	}

	@Override
	public String toString() {
		return "Topic key [topicKey=" + topicKey + ']';
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		if (!super.equals(o)) return false;

		BLTopicKey blTopicKey = (BLTopicKey) o;

		return topicKey.equals(blTopicKey.topicKey);
	}

	@Override
	public int hashCode() {
		int result = super.hashCode();
		result = 31 * result + topicKey.hashCode();
		return result;
	}
}
