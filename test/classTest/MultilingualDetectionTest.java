//package classTest;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.Calendar;
//import java.util.List;
//import java.util.concurrent.locks.ReentrantLock;
//
//import org.apache.commons.io.FileUtils;
//import org.junit.Before;
//import org.junit.Test;
///*siti su cui lanciare il processo
//String ssite="http://nato.int";
//String ssite="http://ferrari.com";
//String ssite="http://lohmann-stahl.de/";
//String ssite="http://www.archos.com";
//String ssite="http://www.ferrari.com";
//String ssite="http://www.lg.com";
//String ssite="http://www.toyota.com";
//String ssite="http://www.beringtime.de/";
//String ssite="http://www.opera.com/";
//String ssite="http://www.kongregate.com/";
//String ssite="http://www.fairmont.com";
//String ssite="http://www.speedtest.net/fr/";
//String ssite="http://www.vmware.com/";
//String ssite="https://www.articulate.com/";
//String ssite="http://worldtimeserver.com/";
//String ssite="http://www.box.com";
//String ssite="http://www.uc3m.es/Home";
//http://www.bulthaup.com/
//*/
//
//import com.cybozu.labs.langdetect.LangDetectException;
//
//import it.uniroma3.parallel.detection.MultilingualDetection;
//
//
//public class MultilingualDetectionTest {
//	
//	private static final String HREFLANG_DETECTION = "www.ferrari.com";
//	private static final String HOMEPAGE_DETECTION = "www.toyota.com";
//	private static final String PREHOMEPAGE_DETECTION = "www.bulthaup.com";
//	private MultilingualDetection multiDetection;
//
//	@Before
//	public void setUp() throws Exception {
//		this.multiDetection = new MultilingualDetection();
//	}
//
//	@Test
//	public void multilingualDetectionPrehomepage_test_real_site() throws IOException, InterruptedException, LangDetectException {
//		multiDetection.multilingualDetection(PREHOMEPAGE_DETECTION, 2, new ReentrantLock(), new ReentrantLock(),
//				new ReentrantLock(), new ReentrantLock());
//	}
//
//	@Test
//	public void multilingualDetectionHomepage_test_real_site() throws IOException, InterruptedException, LangDetectException {
//		multiDetection.multilingualDetection("it.flightaware.com", 2, new ReentrantLock(), new ReentrantLock(),
//				new ReentrantLock(), new ReentrantLock());
//	}
//	
//	@Test
//	public void multilingualDetectionHreflang_test_real_site() throws IOException, InterruptedException, LangDetectException {
//		multiDetection.multilingualDetection(HREFLANG_DETECTION, 2, new ReentrantLock(), new ReentrantLock(),
//				new ReentrantLock(), new ReentrantLock());
//	}
//	
//	@Test
//	public void exp1_test() throws IOException, InterruptedException, LangDetectException {
//		long time_start = Calendar.getInstance().getTimeInMillis();
//		File f = new File("exp.txt");
//		List<String> siti = FileUtils.readLines(f);
//		for (String sito : siti)
//		this.multiDetection.multilingualDetection(sito, 2, new
//		ReentrantLock(), new ReentrantLock(), new ReentrantLock(),
//		new ReentrantLock());
//		long time_finish = Calendar.getInstance().getTimeInMillis();
//		System.out.println(time_finish-time_start);
//	}
//}
