package it.uniroma3.filter;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import it.uniroma3.parallel.filter.LinkTextFilter;

public class LinkTextFilterTest {

	private LinkTextFilter languageSetFilter;

	@Before
	public void setUp() throws Exception {
		this.languageSetFilter = new LinkTextFilter();

	}

	@Test
	public void loadAllName_test() {
		assertNotNull(this.languageSetFilter.getSetForFilter());
		assertFalse(this.languageSetFilter.getSetForFilter().isEmpty());
		assertTrue(this.languageSetFilter.getSetForFilter().contains("italian"));
		assertTrue(this.languageSetFilter.getSetForFilter().contains("italy"));
		assertTrue(this.languageSetFilter.getSetForFilter().contains("italia"));
	}
	
	@Test
	public void valueNotInSet_test(){
		assertFalse(this.languageSetFilter.filter("dsad"));
	}
	
	@Test
	public void valueInSet_test(){
		assertTrue(this.languageSetFilter.filter("italian"));
	}

}
