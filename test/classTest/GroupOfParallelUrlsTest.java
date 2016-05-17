package classTest;

import static org.junit.Assert.*;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.model.GroupOfParallelUrls;

public class GroupOfParallelUrlsTest {

	private GroupOfParallelUrls pp;
	private URL url;
	private URL url2;
	private URL url3;

	@Before
	public void setUp() throws Exception {
		this.pp = new GroupOfParallelUrls();
		this.url = new URL("http://localhost:8080/testMinimale/homeIt.html");
		this.url2 = new URL("http://test2");
		this.url3 = new URL("http://test3");
	}

	@Test
	public void testAddOneURL() {
		pp.addURL(url);
		assertEquals(1, pp.getParallelUrls().size());
	}

	@Test
	public void testGroupOfFiveEntryPoints_WithoutURL() {
		assertEquals(0, pp.getGroupOfEntryPoints(1).size());
	}

	@Test
	public void testGroupOfFiveEntryPoints_WithOneURL() {
		pp.addURL(url);
		assertEquals(1, pp.getGroupOfEntryPoints(1).size());
	}

	@Test
	public void testGroupOfFiveEntryPoints_WithTwoURL() {
		pp.addURL(url);
		pp.addURL(url2);
		assertEquals(2, pp.getGroupOfEntryPoints(1).size());
	}

	@Test
	public void testGroupOfFiveEntryPoints_WithTwoURLAndMaxNumberIs2() {
		pp.addURL(url);
		pp.addURL(url2);
		assertEquals(1, pp.getGroupOfEntryPoints(2).size());
	}

	@Test
	public void testGroupOfFiveEntryPoints_WithThreeURL() {
		pp.addURL(url);
		pp.addURL(url2);
		pp.addURL(url3);
		assertEquals(3, pp.getGroupOfEntryPoints(1).size());
	}

	@Test
	public void testGroupOfFiveEntryPoints_WithThreeURLAndMaxNumberIs2() {
		pp.addURL(url);
		pp.addURL(url2);
		pp.addURL(url3);
		assertEquals(2, pp.getGroupOfEntryPoints(2).size());
	}
}
