package fr.dgrandemange.txnmgrworkflow.service.support;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.TestCase;

import org.fest.assertions.Assertions;
import org.junit.Before;
import org.junit.Test;

import fr.dgrandemange.txnmgrworkflow.service.support.TxnMgrConfigParserImpl;

/**
 * @author dgrandemange
 * 
 */
public class TestTxnMgrConfigParserImplTest extends TestCase {

	TxnMgrConfigParserImpl parserImpl;
	TxnMgrConfigParserImpl.EntityResolverImpl entityResolver;

	@Before
	public void setUp() {
		parserImpl = new TxnMgrConfigParserImpl();
	}

	@Test
	public void testEntityResolver_FixSystemId() throws MalformedURLException,
			UnsupportedEncodingException {
		File currentDir = new File(System.getProperty("user.dir"));
		String sCurrentDirURL = currentDir.toURI().toURL().toString();

		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(bos);
		pw.println("[DocType: <!DOCTYPE SYSTEM [");
		pw.println("  <!ENTITY AuthorizationSubFlow SYSTEM \"file://"
				+ sCurrentDirURL + "/AuthorizationSubFlow.inc\">");
		pw.println("  <!ENTITY HolderNumberManagement SYSTEM \"file://"
				+ sCurrentDirURL + "/HolderNumberManagement.inc\">");
		pw.println("]>]");
		pw.flush();
		pw.close();

		String docType = bos.toString();

		Pattern pattern = Pattern
				.compile("<\\!ENTITY\\s*([a-zA-Z0-9_]*)\\s*.*\\s*SYSTEM\\s*\"(.*)\".*>");

		TxnMgrConfigParserImpl.EntityResolverImpl entityResolver = new TxnMgrConfigParserImpl.EntityResolverImpl(
				"/dummybase/");
		Scanner scanner = new Scanner(docType);
		int entityRank = 0;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			line = line.trim();
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				String entityName = matcher.group(1);
				String systemId = matcher.group(2);
				String fixedSystemId = entityResolver.fixSystemId(systemId);

				System.out.println(String.format(
						"entityName=%s, systemId=%s, fixedSytemId=%s",
						entityName, systemId, fixedSystemId));
				switch (entityRank) {
				case 0:
					Assertions.assertThat(fixedSystemId).isEqualTo(
							"/dummybase/AuthorizationSubFlow.inc");
					break;
				case 1:
					Assertions.assertThat(fixedSystemId).isEqualTo(
							"/dummybase/HolderNumberManagement.inc");
					break;
				}
				entityRank++;
			}
		}

	}

}
