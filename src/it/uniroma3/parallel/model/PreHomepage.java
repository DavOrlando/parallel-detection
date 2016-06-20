package it.uniroma3.parallel.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jsoup.nodes.Element;

import com.cybozu.labs.langdetect.LangDetectException;

public class PreHomepage extends Page {

	private List<Page> possibleHomepages;

	public PreHomepage(String homepageStringUrl) throws IOException {
		super(homepageStringUrl);
		this.possibleHomepages=new ArrayList<>();
	}

	public List<Page> getPossibleHomepages() {
		return possibleHomepages;
	}

	public void setPossibleHomepages(List<Page> possibleHomepages) {
		this.possibleHomepages = possibleHomepages;
	}

	public HashSet<Element> getAllOutlinks() {
		return getHtmlElements("a");
	}

	public Set<String> getLanguagesOfOutlinks() throws LangDetectException, IOException {
		HashSet<String> languages = new HashSet<>();
		for (Element link : getAllOutlinks()) {
			Page page = new Page(link.absUrl("href"));
			this.possibleHomepages.add(page);
			languages.add(page.getLanguage());
		}
		return languages;
	}
}
