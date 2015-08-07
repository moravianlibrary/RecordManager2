package cz.mzk.recordmanager.server.dedup;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

public class TitleSimilarityWriter implements ItemWriter<List<Set<Long>>> {

	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Override
	public void write(List<? extends List<Set<Long>>> items) throws Exception {
		
		List<String> commands = new ArrayList<>();
		
		for (List<Set<Long>> item: items) {
			for (Set<Long> idSet: item) {
				if (idSet.size() > 1) {
					String[] ids = idSet.stream().map(e -> e.toString()).toArray(String[]::new);
					commands.add("INSERT INTO tmp_similarity_ids (id_array) VALUES ('" + String.join(",", ids) + "');");
				}
			}
		}
		
		for (String command: commands) {
			jdbcTemplate.execute(command);
		}

	}

}
