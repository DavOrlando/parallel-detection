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

import it.uniroma3.parallel.utils.FetchManager;

public class MakeSetOfTest {
	private static final int NUMBER_OF_LANGUAGES = 859;
	private File file;

	@Before
	public void setUp() throws Exception {
		this.file = new File("perTest");
		FileUtils.writeStringToFile(file, "1");
	}

	@Test
	public void fileVuototest() {
		assertEquals(0, FetchManager.getInstance().makeSetOf("").size());
	}

	@Test
	public void fileConUnaStringatest() {
		assertEquals(1, FetchManager.getInstance().makeSetOf("perTest").size());
	}

	@Test
	public void fileConDueStringhetest() throws IOException {
		List<String> stringhe = new ArrayList<>();
		stringhe.add("1");
		stringhe.add("2");
		FileUtils.writeLines(file, stringhe);
		assertEquals(2, FetchManager.getInstance().makeSetOf("perTest").size());
	}
	
	@Test
	public void fileContenteAltoNumeroDiLineetest() throws IOException {
		assertEquals(NUMBER_OF_LANGUAGES, FetchManager.getInstance().makeSetOfAllMultilingualProperties().size());
	}


	@After
	public void after() {
		this.file.delete();
	}

}
