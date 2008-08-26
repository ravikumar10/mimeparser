package tests.rules;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import configuration.Configuration;

import analize.Analyser;
import analize.Rule;

import mail.MimeMessage;
import mail.exceptions.ParseException;
import mail.util.SharedFileInputStream;
import junit.framework.TestCase;

/**
 * Tests if rules according to size of message
 * are doing correctly
 * @author zbychu
 *
 */
public class MaxPartSizeRuleTest extends TestCase {
	
	// ########### explanation of rule ###############
	// drops all messages that contains part with size
	// bigger than 10 KB
	// analyses parts in first degree - without headers
	// if message is not mime/multipart analyses the size
	// of the message - without headers
	// 
	// how accurate is this filter is determined by how big
	// is buffer to which we're reading data (now 512 bytes)
	// and how this buffer grows (now +256 bytes)
	//
	// ####### end of explanation of rule ############
	
	private static String rule = "rule_part_max_size#7#4#true#true";
	
	private String[] messages = new String[]{"test_multipart.eml", 
			"multipart_mixed_aware.eml", "text_and_pdf.eml"};
	
	public void testMaxPartSizeRule() {
		
		String resourceDirName = "resources";
		List<Rule> rules = new ArrayList<Rule>();
		rules.add(Configuration.parseRuleLine(rule));
		Analyser analizer = new Analyser(rules);
		
		for (int i = 0; i < messages.length; i++) {
			String message = messages[i];
			
			SharedFileInputStream is = null;
			try {
				is = new SharedFileInputStream(resourceDirName + "/" + message);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			// parsing messages
			MimeMessage mm = null;
			try {
				mm = new MimeMessage(is);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			assertNotNull(mm);
			
			System.out.println("Analazing message: " + message);
			
			analizer.setMessage(mm);
			analizer.analize();
			
			if (i == 0) {
				assertEquals(0, analizer.getDroppingRules().size());
				continue;
			}
			
			assertNotSame(0, analizer.getDroppingRules().size());
		}
		
	}
	
}
