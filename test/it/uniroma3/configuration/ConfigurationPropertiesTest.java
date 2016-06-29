package it.uniroma3.configuration;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.configuration.ConfigurationProperties;

public class ConfigurationPropertiesTest {
	private static final String OPTION = "option";
	private static final String ANCHOR = "a";
	private static final String PARAGRAPH = "p";
	private static final String ES = "es";
	private static final String EN = "en";
	private static final int _500 = 500;
	private static final String ENGLISH = "english";
	private static final String ESPAÑOL = "español";
	private static final String CREATO_PER_TEST = "creatoPerTest";
	private static final String VUOTO = "vuoto";
	private File file;
	private List<String> lineeDiTesto;

	@SuppressWarnings("deprecation")
	@Before
	public void setUp() throws Exception {
		this.lineeDiTesto = new ArrayList<String>();
		this.file = new File(CREATO_PER_TEST);
		lineeDiTesto.add(ENGLISH);
		FileUtils.writeLines(file, lineeDiTesto);
	}

	@Test
	public void nessunFile_test() {
		try {
			File file = new File(VUOTO);
			FileUtils.writeStringToFile(file, "");
			assertEquals(0, ConfigurationProperties.getInstance().makeSetOfStringByFile(VUOTO).size());
		} catch (IOException e) {
			e.printStackTrace();
			fail();
		}
	}

	@Test
	public void fileConUnaStringa_test() {
		assertEquals(1, ConfigurationProperties.getInstance().makeSetOfStringByFile(CREATO_PER_TEST).size());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void fileConDueStringhe_test() throws IOException {
		lineeDiTesto.add(ESPAÑOL);
		FileUtils.writeLines(file, lineeDiTesto);
		assertEquals(2, ConfigurationProperties.getInstance().makeSetOfStringByFile(CREATO_PER_TEST).size());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void fileConDueStringheUguali_test() throws IOException {
		lineeDiTesto.add(ENGLISH);
		FileUtils.writeLines(file, lineeDiTesto);
		assertEquals(1, ConfigurationProperties.getInstance().makeSetOfStringByFile(CREATO_PER_TEST).size());
	}

	@Test
	public void caricamentoFilePerCrawler_test() throws IOException {
		Set<String> multilingualProperties = ConfigurationProperties.getInstance().makeSetOfAllMultilingualProperties();
		assertFalse(multilingualProperties.isEmpty());
		assertTrue(multilingualProperties.size() > _500);
		assertTrue(multilingualProperties.contains(ESPAÑOL));
		assertTrue(multilingualProperties.contains(ENGLISH));
		assertTrue(multilingualProperties.contains(ES));
		assertTrue(multilingualProperties.contains(EN));
	}

	@Test
	public void caricamentoTagNames_test() throws IOException {
		List<String> stringOfTagName = ConfigurationProperties.getInstance().getStringOfTagName();
		assertFalse(stringOfTagName.isEmpty());
		assertEquals(3,stringOfTagName.size());
		assertEquals(PARAGRAPH, stringOfTagName.get(0));
		assertEquals(ANCHOR, stringOfTagName.get(1));
		assertEquals(OPTION, stringOfTagName.get(2));
	}

	@After
	public void after() {
		this.file.delete();
	}

}
