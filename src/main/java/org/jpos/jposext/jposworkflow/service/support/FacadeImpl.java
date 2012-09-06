package org.jpos.jposext.jposworkflow.service.support;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jpos.jposext.jposworkflow.helper.GraphHelper;
import org.jpos.jposext.jposworkflow.model.EntityRefInfo;
import org.jpos.jposext.jposworkflow.model.Graph;
import org.jpos.jposext.jposworkflow.model.Node;
import org.jpos.jposext.jposworkflow.model.ParticipantInfo;
import org.jpos.jposext.jposworkflow.model.SelectCriterion;
import org.jpos.jposext.jposworkflow.model.SubFlowInfo;
import org.jpos.jposext.jposworkflow.service.IContextMgmtInfoPopulator;
import org.jpos.jposext.jposworkflow.service.IFacade;

/**
 * Facade implementation
 * 
 * @author dgrandemange
 *
 */
public class FacadeImpl implements IFacade {
	
	public static final String ROOT_KEY = "<root>";
	
	/* (non-Javadoc)
	 * @see org.jpos.jposext.jposworkflow.service.support.IFacade#getGraph(java.net.URL, org.jpos.jposext.jposworkflow.service.IContextMgmtInfoPopulator)
	 */
	public Graph getGraph(URL selectedUrl, IContextMgmtInfoPopulator ctxMgmtInfoPopulator) {
		Graph res = null;
		
		Map<String, Graph> graphByEntityRef = new HashMap<String, Graph>();
		
		TxnMgrConfigParserImpl txnMgrConfigParserImpl = new TxnMgrConfigParserImpl();
		txnMgrConfigParserImpl.setGraphByEntityRef(graphByEntityRef);
		TxnMgrGroupsConverterImpl converter = new TxnMgrGroupsConverterImpl();
		GraphReducerImpl reducer = new GraphReducerImpl();

		txnMgrConfigParserImpl.setExpanded(true);

		Map<String, List<ParticipantInfo>> jPosTxnMgrGroups = txnMgrConfigParserImpl
				.parse(selectedUrl);

		ctxMgmtInfoPopulator
				.processParticipantAnnotations(jPosTxnMgrGroups);
		Graph graphInter1 = converter.toGraph(jPosTxnMgrGroups);
		Graph graphInter2 = reducer.reduce(graphInter1);

		// GraphHelper.dumpGraph(graphInter2, new PrintWriter(System.out));
		ctxMgmtInfoPopulator.updateReducedGraph(graphInter2);
		
		res = graphInter2;
		
		return res;
	}	
	
	/* (non-Javadoc)
	 * @see org.jpos.jposext.jposworkflow.service.support.IFacade#getGraphSubFlowMode(java.net.URL, org.jpos.jposext.jposworkflow.service.IContextMgmtInfoPopulator, java.util.Map)
	 */
	public Graph getGraphSubFlowMode(URL selectedUrl, IContextMgmtInfoPopulator ctxMgmtInfoPopulator, Map<String, Graph> graphByEntityRef) {
		Graph res = null;
		
		TxnMgrConfigParserImpl txnMgrConfigParserImpl = new TxnMgrConfigParserImpl();
		txnMgrConfigParserImpl.setGraphByEntityRef(graphByEntityRef);
		TxnMgrGroupsConverterImpl converter = new TxnMgrGroupsConverterImpl();
		GraphReducerImpl reducer = new GraphReducerImpl();

		txnMgrConfigParserImpl.useXmlDocType(selectedUrl);

		Map<String, EntityRefInfo> entityRefs = new HashMap<String, EntityRefInfo>();
		Map<String, List<String>> listEntityRefsInterDependencies = txnMgrConfigParserImpl.listEntityRefsInterDependencies(selectedUrl, entityRefs);
		
		List<EntityRefInfo> entityRefsTopologicalSort = txnMgrConfigParserImpl.sortEntityRefsTopologicalOrder(entityRefs, listEntityRefsInterDependencies); 
		entityRefsTopologicalSort.add(new EntityRefInfo(ROOT_KEY,
				selectedUrl));

		for (EntityRefInfo entityRef : entityRefsTopologicalSort) {

			URL entityRefURL = entityRef.getUrl();

			Map<String, List<ParticipantInfo>> jPosTxnMgrGroups = txnMgrConfigParserImpl
					.parse(entityRefURL);

			ctxMgmtInfoPopulator
					.processParticipantAnnotations(jPosTxnMgrGroups);
			Graph graphInter1 = converter.toGraph(jPosTxnMgrGroups);
			Graph graphInter2 = reducer.reduce(graphInter1);

			String entityRefName = entityRef.getName();
			graphByEntityRef.put(entityRefName, graphInter2);

//				GraphHelper.dumpGraph(graphInter2, new PrintWriter(System.out));
		}

		Map<String, List<Node>> unresolvedNodesByEntityRefName = new HashMap<String, List<Node>>();
		for (Entry<String, Graph> entry : graphByEntityRef.entrySet()) {
			Graph currGraph = entry.getValue();
			List<Node> unresolvedNodes = GraphHelper
					.lookForUnresolvedNodes(currGraph);
			unresolvedNodesByEntityRefName.put(entry.getKey(),
					unresolvedNodes);
		}

		for (Entry<String, List<Node>> entry : unresolvedNodesByEntityRefName
				.entrySet()) {
			List<String> currentFlowDependencies = listEntityRefsInterDependencies
					.get(entry.getKey());
			for (Node unresolved : entry.getValue()) {
				String unResolvedGroupName = unresolved
						.getParticipant().getGroupName();
				
				Graph referencedGraph = graphByEntityRef.get(unResolvedGroupName);
				
				if (null != referencedGraph) {
					if (!(currentFlowDependencies.contains(unResolvedGroupName))) {
						currentFlowDependencies.add(unResolvedGroupName);
					}

					// Replace unresolved participant info with a subflow
					// info
					SubFlowInfo subFlowInfo = new SubFlowInfo(unResolvedGroupName,
							referencedGraph,
							new HashMap<String, SelectCriterion>());
					unresolved.setParticipant(subFlowInfo);
				}
			}
		}
		
		List<EntityRefInfo> finalEntityRefsTopologicalSort = txnMgrConfigParserImpl.sortEntityRefsTopologicalOrder(entityRefs, listEntityRefsInterDependencies); 
		finalEntityRefsTopologicalSort.add(new EntityRefInfo(ROOT_KEY,
				selectedUrl));

		for (EntityRefInfo entityRef : finalEntityRefsTopologicalSort) {				
			ctxMgmtInfoPopulator.updateReducedGraph(graphByEntityRef.get(entityRef.getName()));				
		}			

		res = graphByEntityRef.get(ROOT_KEY);
		
		return res;
	}	
	
}
