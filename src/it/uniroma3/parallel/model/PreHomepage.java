package it.uniroma3.parallel.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;

import com.cybozu.labs.langdetect.LangDetectException;

public class PreHomepage extends Page {

	private Set<String> languages;
	private HashSet<Element> outlinks;
	private List<Page> possibleHomepages;

	public HashSet<Element> getAllOutlinks() {
		if (outlinks == null)
			this.outlinks = getHtmlElements("a");
		return outlinks;
	}

	public Set<String> getLanguages() throws LangDetectException, IOException {
		if (languages == null) {
			this.languages = new HashSet<>();
			for (Element link : outlinks) {
				Page page = new Page(link.absUrl("href"));
				this.possibleHomepages.add(page);
				this.languages.add(page.getLanguage());
			}
		}
		return languages;
	}
}
