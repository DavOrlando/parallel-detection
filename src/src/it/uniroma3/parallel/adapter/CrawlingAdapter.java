package src.it.uniroma3.parallel.adapter;

import java.io.IOException;
import java.util.concurrent.locks.Lock;

import it.uniroma3.parallel.model.ParallelPages;

public interface CrawlingAdapter {
	void crawl(ParallelPages groupOfHomepages, Integer integer, int depthT, Lock errorLogLock,
			String nameFolder, Lock productivityLock, Lock timeLock) throws IOException;
}
