package classTest;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import org.junit.Before;
import org.junit.Test;
/*siti su cui lanciare il processo
String ssite="http://nato.int";
String ssite="http://ferrari.com";
String ssite="http://lohmann-stahl.de/";
String ssite="http://www.archos.com";
String ssite="http://www.ferrari.com";
String ssite="http://www.lg.com";
String ssite="http://www.toyota.com";
String ssite="http://www.beringtime.de/";
String ssite="http://www.opera.com/";
String ssite="http://www.kongregate.com/";
String ssite="http://www.fairmont.com";
String ssite="http://www.speedtest.net/fr/";
String ssite="http://www.vmware.com/";
String ssite="https://www.articulate.com/";
String ssite="http://worldtimeserver.com/";
String ssite="http://www.box.com";
String ssite="http://www.uc3m.es/Home";
http://www.bulthaup.com/
*/

import com.cybozu.labs.langdetect.LangDetectException;

import it.uniroma3.parallel.detection.MultilingualDetection;

// File f = new File("seedTot2.txt");
// List<String> siti = FileUtils.readLines(f);
// for (String sito : siti)
// MultilingualDetection.multilingualDetection(sito, 2, new
// ReentrantLock(), new ReentrantLock(), new ReentrantLock(),
// new ReentrantLock());
public class MultilingualDetectionTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void multilingualDetectionPrehomepage_test() throws IOException, InterruptedException, LangDetectException {
		MultilingualDetection.multilingualDetection("www.bulthaup.com", 2, new ReentrantLock(), new ReentrantLock(),
				new ReentrantLock(), new ReentrantLock());
	}

	@Test
	public void multilingualDetectionHomepage_test() throws IOException, InterruptedException, LangDetectException {
		MultilingualDetection.multilingualDetection("www.toyota.com", 2, new ReentrantLock(), new ReentrantLock(),
				new ReentrantLock(), new ReentrantLock());
	}
	
	@Test
	public void multilingualDetectionHreflang_test() throws IOException, InterruptedException, LangDetectException {
		MultilingualDetection.multilingualDetection("www.ferrari.com", 2, new ReentrantLock(), new ReentrantLock(),
				new ReentrantLock(), new ReentrantLock());
	}
}
