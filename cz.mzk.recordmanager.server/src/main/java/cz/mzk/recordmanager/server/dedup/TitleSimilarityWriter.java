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
	
	String tablename;
	
	public TitleSimilarityWriter(String tablename) {
		this.tablename = tablename;
	}
	
	@Override
	public void write(List<? extends List<Set<Long>>> items) throws Exception {
		
		List<String> commands = new ArrayList<>();
		
		for (List<Set<Long>> item: items) {
			for (Set<Long> idSet: item) {
				if (idSet.size() > 1) {
					String[] ids = idSet.stream().map(e -> e.toString()).toArray(String[]::new);
					StringBuilder builder = new StringBuilder();
					builder.append("INSERT INTO ");
					builder.append(tablename);
					builder.append(" VALUES (nextval('tmp_table_id_seq'),");
					builder.append("('");
					builder.append(String.join(",", ids));
					builder.append("'))");
					commands.add(builder.toString());
				}
			}
		}
		
		for (String command: commands) {
			jdbcTemplate.execute(command);
		}

	}

}
