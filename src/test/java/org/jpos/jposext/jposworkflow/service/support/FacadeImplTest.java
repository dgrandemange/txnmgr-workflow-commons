package org.jpos.jposext.jposworkflow.service.support;

import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jpos.jposext.jposworkflow.helper.GraphHelper;
import org.jpos.jposext.jposworkflow.model.Graph;
import org.jpos.jposext.jposworkflow.model.ParticipantInfo;
import org.jpos.jposext.jposworkflow.service.IContextMgmtInfoPopulator;
import org.junit.Before;
import org.junit.Test;

/**
 * @author dgrandemange
 * 
 */
public class FacadeImplTest {

	private FacadeImpl facade;
	private IContextMgmtInfoPopulator ctxMgmtInfoPopulator;
	private Map<String, Graph> graphByEntityRef;

	@Before
	public void setUp() {
		facade = new FacadeImpl();
		
		ctxMgmtInfoPopulator = new IContextMgmtInfoPopulator() {

			public void updateReducedGraph(Graph graphInter2) {
			}

			public void processParticipantAnnotations(
					Map<String, List<ParticipantInfo>> jPosTxnMgrGroups) {
			}
		};
		
		graphByEntityRef = new HashMap<String, Graph>();
	}

	@Test
	public void testGetGraphCase1() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase1/20_txnmgr.xml");
		Graph graph = facade.getGraph(resource, ctxMgmtInfoPopulator);
		GraphHelper.dumpGraph(graph, new PrintWriter(System.out));
	}

	@Test
	public void testGetGraphCase2() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase2/20_txnmgr.xml");
		Graph graph = facade.getGraph(resource, ctxMgmtInfoPopulator);
		GraphHelper.dumpGraph(graph, new PrintWriter(System.out));
	}

	@Test
	public void testGetGraphCase3() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase3/Financial.inc");
		Graph graph = facade.getGraph(resource, ctxMgmtInfoPopulator);
		GraphHelper.dumpGraph(graph, new PrintWriter(System.out));
	}

	@Test
	public void testGetGraphCase4() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase4/Financial.inc");
		Graph graph = facade.getGraph(resource, ctxMgmtInfoPopulator);
		GraphHelper.dumpGraph(graph, new PrintWriter(System.out));
	}

	@Test
	public void testGetGraphCase5() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase5/sub-app-context__txmgr.xml");
		Graph graph = facade.getGraph(resource, ctxMgmtInfoPopulator);
		GraphHelper.dumpGraph(graph, new PrintWriter(System.out));
	}

	@Test
	public void testGetGraphCase1_entitesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase1/20_txnmgr.xml");
		Graph graph = facade.getGraphSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}

	@Test
	public void testGetGraphCase2_entitesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase2/20_txnmgr.xml");
		Graph graph = facade.getGraphSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}

	@Test
	public void testGetGraphCase3_entitesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase3/Financial.inc");
		Graph graph = facade.getGraphSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}

	@Test
	public void testGetGraphCase4_entitesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase4/Financial.inc");
		Graph graph = facade.getGraphSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}

	@Test
	public void testGetGraphCase5_entitesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase5/sub-app-context__txmgr.xml");
		Graph graph = facade.getGraphSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}

}
