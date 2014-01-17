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
import org.jpos.jposext.jposworkflow.service.ITxnMgrConfigParser;

/**
 * Facade implementation
 * 
 * @author dgrandemange
 * 
 */
public class FacadeImpl implements IFacade {

	public static final String ROOT_KEY = "<root>";

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.jposworkflow.service.support.IFacade#getGraph(java.net
	 * .URL, org.jpos.jposext.jposworkflow.service.IContextMgmtInfoPopulator)
	 */
	public Graph getGraph(URL selectedUrl,
			IContextMgmtInfoPopulator ctxMgmtInfoPopulator) {
		Graph res = null;

		Map<String, Graph> graphBySubflowName = new HashMap<String, Graph>();

		TxnMgrConfigParserSubflowEltImpl txnMgrConfigParserImpl = new TxnMgrConfigParserSubflowEltImpl();
		txnMgrConfigParserImpl.setGraphByEntityRef(graphBySubflowName);
		txnMgrConfigParserImpl.setSubFlowMode(false);

		TxnMgrGroupsConverterImpl converter = new TxnMgrGroupsConverterImpl();
		GraphReducerImpl reducer = new GraphReducerImpl();

		Map<String, List<ParticipantInfo>> jPosTxnMgrGroups = txnMgrConfigParserImpl
				.parse(selectedUrl);

		ctxMgmtInfoPopulator.processParticipantAnnotations(jPosTxnMgrGroups);
		Graph graphInter1 = converter.toGraph(jPosTxnMgrGroups);
		Graph graphInter2 = reducer.reduce(graphInter1);

		// GraphHelper.dumpGraph(graphInter2, new PrintWriter(System.out));
		ctxMgmtInfoPopulator.updateReducedGraph(graphInter2);

		res = graphInter2;

		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.jposworkflow.service.support.IFacade#getGraphSubFlowMode
	 * (java.net.URL,
	 * org.jpos.jposext.jposworkflow.service.IContextMgmtInfoPopulator,
	 * java.util.Map)
	 */
	public Graph getGraphSubFlowMode(URL selectedUrl,
			IContextMgmtInfoPopulator ctxMgmtInfoPopulator,
			Map<String, Graph> graphBySubflowName) {
		
		TxnMgrConfigParserSubflowEltImpl txnMgrConfigParserImpl = new TxnMgrConfigParserSubflowEltImpl();
		txnMgrConfigParserImpl.setGraphByEntityRef(graphBySubflowName);
		txnMgrConfigParserImpl.setSubFlowMode(true);

		return getGraphGenericSubFlowMode(txnMgrConfigParserImpl, selectedUrl,
				ctxMgmtInfoPopulator, graphBySubflowName);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.jpos.jposext.jposworkflow.service.IFacade#getGraphEntityRefsAsSubFlowMode
	 * (java.net.URL,
	 * org.jpos.jposext.jposworkflow.service.IContextMgmtInfoPopulator,
	 * java.util.Map)
	 */
	public Graph getGraphEntityRefsAsSubFlowMode(URL selectedUrl,
			IContextMgmtInfoPopulator ctxMgmtInfoPopulator,
			Map<String, Graph> graphBySubflowName) {
		
		TxnMgrConfigParserImpl txnMgrConfigParserImpl = new TxnMgrConfigParserImpl();
		txnMgrConfigParserImpl.setGraphByEntityRef(graphBySubflowName);
		txnMgrConfigParserImpl.setSubFlowMode(true);
		txnMgrConfigParserImpl.useXmlDocType(selectedUrl);

		return getGraphGenericSubFlowMode(txnMgrConfigParserImpl, selectedUrl,
				ctxMgmtInfoPopulator, graphBySubflowName);
	}

	protected Graph getGraphGenericSubFlowMode(
			ITxnMgrConfigParser txnMgrConfigParser, URL selectedUrl,
			IContextMgmtInfoPopulator ctxMgmtInfoPopulator,
			Map<String, Graph> graphBySubflowName) {
		Graph res = null;

		TxnMgrGroupsConverterImpl converter = new TxnMgrGroupsConverterImpl();
		GraphReducerImpl reducer = new GraphReducerImpl();

		Map<String, EntityRefInfo> entityRefs = new HashMap<String, EntityRefInfo>();
		Map<String, List<String>> listSubflowsInterDependencies = txnMgrConfigParser
				.listSubflowsInterDependencies(selectedUrl, entityRefs);

		List<EntityRefInfo> entityRefsTopologicalSort = txnMgrConfigParser
				.sortEntityRefsTopologicalOrder(entityRefs,
						listSubflowsInterDependencies);
		entityRefsTopologicalSort.add(new EntityRefInfo(ROOT_KEY, selectedUrl));

		for (EntityRefInfo entityRef : entityRefsTopologicalSort) {

			URL entityRefURL = entityRef.getUrl();

			Map<String, List<ParticipantInfo>> jPosTxnMgrGroups = txnMgrConfigParser
					.parse(entityRefURL);

			ctxMgmtInfoPopulator
					.processParticipantAnnotations(jPosTxnMgrGroups);
			Graph graphInter1 = converter.toGraph(jPosTxnMgrGroups);
			Graph graphInter2 = reducer.reduce(graphInter1);

			String entityRefName = entityRef.getName();
			graphBySubflowName.put(entityRefName, graphInter2);

			// GraphHelper.dumpGraph(graphInter2, new PrintWriter(System.out));
		}

		Map<String, List<Node>> unresolvedNodesBySubflowName = new HashMap<String, List<Node>>();
		for (Entry<String, Graph> entry : graphBySubflowName.entrySet()) {
			Graph currGraph = entry.getValue();
			List<Node> unresolvedNodes = GraphHelper
					.lookForUnresolvedNodes(currGraph);
			unresolvedNodesBySubflowName.put(entry.getKey(), unresolvedNodes);
		}

		for (Entry<String, List<Node>> entry : unresolvedNodesBySubflowName
				.entrySet()) {
			List<String> currentFlowDependencies = listSubflowsInterDependencies
					.get(entry.getKey());
			for (Node unresolved : entry.getValue()) {
				String unResolvedGroupName = unresolved.getParticipant()
						.getGroupName();

				Graph referencedGraph = graphBySubflowName
						.get(unResolvedGroupName);

				if (null != referencedGraph) {
					if (!(currentFlowDependencies.contains(unResolvedGroupName))) {
						currentFlowDependencies.add(unResolvedGroupName);
					}

					// Replace unresolved participant info with a subflow
					// info
					SubFlowInfo subFlowInfo = new SubFlowInfo(
							unResolvedGroupName, referencedGraph,
							new HashMap<String, SelectCriterion>());
					unresolved.setParticipant(subFlowInfo);
				}
			}
		}

		List<EntityRefInfo> finalSubflowsTopologicalSort = txnMgrConfigParser
				.sortEntityRefsTopologicalOrder(entityRefs,
						listSubflowsInterDependencies);
		finalSubflowsTopologicalSort.add(new EntityRefInfo(ROOT_KEY,
				selectedUrl));

		for (EntityRefInfo entityRef : finalSubflowsTopologicalSort) {
			ctxMgmtInfoPopulator.updateReducedGraph(graphBySubflowName
					.get(entityRef.getName()));
		}

		res = graphBySubflowName.get(ROOT_KEY);

		return res;
	}
}
