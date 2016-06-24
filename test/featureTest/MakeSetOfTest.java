package featureTest;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.configuration.ConfigurationProperties;

public class MakeSetOfTest {
	private File file;

	@Before
	public void setUp() throws Exception {
		this.file = new File("perTest");
		FileUtils.writeStringToFile(file, "1");
	}

	@Test
	public void fileVuototest() {
		assertEquals(0, ConfigurationProperties.getInstance().makeSetOf("").size());
	}

	@Test
	public void fileConUnaStringatest() {
		assertEquals(1, ConfigurationProperties.getInstance().makeSetOf("perTest").size());
	}

	@Test
	public void fileConDueStringhetest() throws IOException {
		List<String> stringhe = new ArrayList<>();
		stringhe.add("1");
		stringhe.add("2");
		FileUtils.writeLines(file, stringhe);
		assertEquals(2, ConfigurationProperties.getInstance().makeSetOf("perTest").size());
	}

	@Test
	public void fileContenteAltoNumeroDiLineetest() throws IOException {
		assertFalse(ConfigurationProperties.getInstance().makeSetOfAllMultilingualProperties().isEmpty());
	}

	@Test
	public void fileContenteSpagnoloAltoNumeroDiLineetest() throws IOException {
		assertTrue(ConfigurationProperties.getInstance().makeSetOfAllMultilingualProperties().contains("español"));
	}

	@After
	public void after() {
		this.file.delete();
	}

}
