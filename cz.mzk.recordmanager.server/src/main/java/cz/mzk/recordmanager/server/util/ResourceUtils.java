package cz.mzk.recordmanager.server.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import com.google.common.io.CharStreams;

public class ResourceUtils {

	public static String asString(String resource) throws IOException {
		InputStream is = ResourceUtils.class.getClassLoader().getResourceAsStream(resource);
		if (is == null) {
			throw new IllegalArgumentException(String.format("Resource %s not found", resource));
		}
		try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8)) {
			return CharStreams.toString(isr);
		}
	}

}
