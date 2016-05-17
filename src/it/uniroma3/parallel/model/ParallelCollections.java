package it.uniroma3.parallel.model;
import java.util.List;

public class ParallelCollections {
	
	String nameFolder;
	List<String> parallelUrl;
	int depth;
	String siteUrl;

	public ParallelCollections(String nameFolder, List<String> parallelUrl, int depth,String siteUrl){
		this.nameFolder=nameFolder;
		this.parallelUrl=parallelUrl;
		this.depth=depth;
		this.siteUrl=siteUrl; 
	}

	public String getNameFolder() {
		return nameFolder;
	}

	public void setNameFolder(String nameFolder) {
		this.nameFolder = nameFolder;
	}

	public List<String> getParallelUrl() {
		return parallelUrl;
	}

	public void setParallelUrl(List<String> parallelUrl) {
		this.parallelUrl = parallelUrl;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}



}