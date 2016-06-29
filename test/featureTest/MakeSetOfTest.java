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
		File file = new File(VUOTO);
		assertEquals(0, ConfigurationProperties.getInstance().makeSetOfStringByFile(VUOTO).size());
	}

	@Test
	public void fileConUnaStringa_test() {
		assertEquals(1, ConfigurationProperties.getInstance().makeSetOfStringByFile(CREATO_PER_TEST).size());
	}
	
	@SuppressWarnings("deprecation")
	@Test
	public void fileConDueStringhe_test() throws IOException {
		lineeDiTesto.add(ESPAÑOL);
		FileUtils.writeLines(file,lineeDiTesto);
		assertEquals(2, ConfigurationProperties.getInstance().makeSetOfStringByFile(CREATO_PER_TEST).size());
	}

	@SuppressWarnings("deprecation")
	@Test
	public void fileConDueStringheUguali_test() throws IOException {
		lineeDiTesto.add(ENGLISH);
		FileUtils.writeLines(file,lineeDiTesto);
		assertEquals(1, ConfigurationProperties.getInstance().makeSetOfStringByFile(CREATO_PER_TEST).size());
	}

	@Test
	public void caricamentoFilePerCrawler_test() throws IOException {
		assertFalse(ConfigurationProperties.getInstance().makeSetOfAllMultilingualProperties().isEmpty());
		assertTrue(ConfigurationProperties.getInstance().makeSetOfAllMultilingualProperties().size() > _500);
		assertTrue(ConfigurationProperties.getInstance().makeSetOfAllMultilingualProperties().contains(ESPAÑOL));
		assertTrue(ConfigurationProperties.getInstance().makeSetOfAllMultilingualProperties().contains(ENGLISH));
		assertTrue(ConfigurationProperties.getInstance().makeSetOfAllMultilingualProperties().contains(ES));
		assertTrue(ConfigurationProperties.getInstance().makeSetOfAllMultilingualProperties().contains(EN));
	}


	@After
	public void after() {
		this.file.delete();
	}

}
