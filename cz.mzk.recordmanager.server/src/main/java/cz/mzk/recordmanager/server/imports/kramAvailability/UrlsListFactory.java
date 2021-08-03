package cz.mzk.recordmanager.server.imports.kramAvailability;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class UrlsListFactory {

	public static List<String> getUrls(String type) {
		switch (type) {
		case "titles":
			return Arrays.asList(
					"%s/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,document_type,issn&q=level:0&rows=%d&start=%d&wt=xml",
					"%s/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,document_type&q=level:1+document_type:monographunit&rows=%d&start=%d&wt=xml"
			);
		case "pages":
			return Arrays.asList(
					"%s/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,parent_pid,details,document_type&q=fedora.model:periodicalvolume&rows=%d&start=%d&wt=xml",
					"%s/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,parent_pid,details,document_type,rok,issn&q=fedora.model:periodicalitem&rows=%d&start=%d&wt=xml",
					"%s/search?fl=dostupnost,dnnt,PID,level,dnnt-labels,parent_pid,details,document_type,title,rok,rels_ext_index&q=fedora.model:page+model_path:*periodicalitem/page&rows=%d&start=%d&wt=xml"
			);
		}
		return Collections.emptyList();
	}

}
