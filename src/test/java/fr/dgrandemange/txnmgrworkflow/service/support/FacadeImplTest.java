package fr.dgrandemange.txnmgrworkflow.service.support;

import java.io.PrintWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Before;
import org.junit.Test;

import fr.dgrandemange.txnmgrworkflow.helper.GraphHelper;
import fr.dgrandemange.txnmgrworkflow.model.Graph;
import fr.dgrandemange.txnmgrworkflow.model.ParticipantInfo;
import fr.dgrandemange.txnmgrworkflow.service.IContextMgmtInfoPopulator;
import fr.dgrandemange.txnmgrworkflow.service.support.FacadeImpl;

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
					Map<String, List<ParticipantInfo>> txnMgrGroups) {
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
	public void testGetGraphCase6() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase6/20_txnmgr.xml");
		Graph graph = facade.getGraph(resource, ctxMgmtInfoPopulator);
		GraphHelper.dumpGraph(graph, new PrintWriter(System.out));
	}

	@Test
	public void testGetGraphCase7() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase7/sub-app-context__txmgr.xml");
		Graph graph = facade.getGraph(resource, ctxMgmtInfoPopulator);
		GraphHelper.dumpGraph(graph, new PrintWriter(System.out));
	}

	@Test
	public void testGetGraphCase8() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase8/20_txnmgr.xml");
		Graph graph = facade.getGraph(resource, ctxMgmtInfoPopulator);
		GraphHelper.dumpGraph(graph, new PrintWriter(System.out));
	}

	@Test
	public void testGetGraphCase9() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase9/Financial.inc");
		Graph graph = facade.getGraph(resource, ctxMgmtInfoPopulator);
		GraphHelper.dumpGraph(graph, new PrintWriter(System.out));
	}
	
	@Test
	public void testGetGraphCase10() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase8/20_txnmgr.xml");
		Graph graph = facade.getGraph(resource, ctxMgmtInfoPopulator);
		GraphHelper.dumpGraph(graph, new PrintWriter(System.out));
	}
	
	@Test
	public void testGetGraphCase1_entitiesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase1/20_txnmgr.xml");
		facade.getGraphEntityRefsAsSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}

	@Test
	public void testGetGraphCase2_entitiesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase2/20_txnmgr.xml");
		facade.getGraphEntityRefsAsSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}

	@Test
	public void testGetGraphCase3_entitiesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase3/Financial.inc");
		facade.getGraphEntityRefsAsSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}

	@Test
	public void testGetGraphCase4_entitiesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase4/Financial.inc");
		facade.getGraphEntityRefsAsSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}

	@Test
	public void testGetGraphCase5_entitiesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase5/sub-app-context__txmgr.xml");
		facade.getGraphEntityRefsAsSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}

	@Test
	public void testGetGraphCase6_entitiesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase6/20_txnmgr.xml");
		facade.getGraphEntityRefsAsSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}

	@Test
	public void testGetGraphCase7_entitiesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase7/sub-app-context__txmgr.xml");
		facade.getGraphSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}

	@Test
	public void testGetGraphCase8_entitiesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase8/20_txnmgr.xml");
		facade.getGraphEntityRefsAsSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}
	
	@Test
	public void testGetGraphCase9_entitiesAsSubflow() {
		URL resource = FacadeImplTest.class
				.getResource("FacadeImplTest_Res/testParseCase9/Financial.inc");
		facade.getGraphSubFlowMode(resource, ctxMgmtInfoPopulator, graphByEntityRef);
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			System.out.println(entry.getKey());
			GraphHelper.dumpGraph(entry.getValue(), new PrintWriter(System.out));	
		}
	}
}
