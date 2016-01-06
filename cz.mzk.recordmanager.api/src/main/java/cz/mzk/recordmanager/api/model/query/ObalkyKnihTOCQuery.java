package cz.mzk.recordmanager.api.model.query;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

public class ObalkyKnihTOCQuery implements Serializable {

	private static final long serialVersionUID = 1L;

	private LogicalOperator logicalOperator = LogicalOperator.OR;

	private Collection<Long> isbns;

	private Collection<String> nbns;

	private Collection<String> eans;

	private Collection<String> oclcs;

	public LogicalOperator getLogicalOperator() {
		return logicalOperator;
	}

	public void setLogicalOperator(LogicalOperator logicalOperator) {
		this.logicalOperator = logicalOperator;
	}

	public Collection<Long> getIsbns() {
		return isbns;
	}

	public void setIsbns(Collection<Long> isbns) {
		this.isbns = isbns;
	}

	public void setIsbn(Long isbn) {
		this.isbns = Collections.singletonList(isbn);
	}

	public Collection<String> getNbns() {
		return nbns;
	}

	public void setNbns(Collection<String> nbns) {
		this.nbns = nbns;
	}

	public void setNbn(String nbn) {
		this.nbns = Collections.singletonList(nbn);
	}

	public Collection<String> getEans() {
		return eans;
	}

	public void setEans(Collection<String> eans) {
		this.eans = eans;
	}

	public void setEan(String ean) {
		this.eans = Collections.singletonList(ean);
	}

	public Collection<String> getOclcs() {
		return oclcs;
	}

	public void setOclcs(Collection<String> oclcs) {
		this.oclcs = oclcs;
	}

	public void setOclc(String oclc) {
		this.oclcs = Collections.singletonList(oclc);
	}

}
