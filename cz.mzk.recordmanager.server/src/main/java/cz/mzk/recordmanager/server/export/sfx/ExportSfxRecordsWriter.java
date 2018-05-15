package cz.mzk.recordmanager.server.export.sfx;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemWriter;

public class ExportSfxRecordsWriter implements ItemWriter<String>,
		StepExecutionListener {

	private String filename;
	private FileWriter file;

	public ExportSfxRecordsWriter(String filename) {
		this.filename = filename;
	}

	@Override
	public void beforeStep(StepExecution stepExecution) {
		try {
			file = new FileWriter(filename, false);
			file.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			file.write("<institutional_holdings>\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		try {
			file.write("</institutional_holdings>\n");
			file.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void write(List<? extends String> items) throws Exception {
		for (String string : items) {
			file.write(string);
		}
	}

}
