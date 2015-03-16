package fr.dgrandemange.txnmgrworkflow.service.support;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fr.dgrandemange.txnmgrworkflow.helper.GraphHelper;
import fr.dgrandemange.txnmgrworkflow.model.EntityRefInfo;
import fr.dgrandemange.txnmgrworkflow.model.Graph;
import fr.dgrandemange.txnmgrworkflow.model.Node;
import fr.dgrandemange.txnmgrworkflow.model.ParticipantInfo;
import fr.dgrandemange.txnmgrworkflow.model.SelectCriterion;
import fr.dgrandemange.txnmgrworkflow.model.SubFlowInfo;
import fr.dgrandemange.txnmgrworkflow.service.IContextMgmtInfoPopulator;
import fr.dgrandemange.txnmgrworkflow.service.IFacade;
import fr.dgrandemange.txnmgrworkflow.service.ITxnMgrConfigParser;

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
	 * fr.dgrandemange.txnmgrworkflow.service.support.IFacade#getGraph(java.net
	 * .URL, fr.dgrandemange.txnmgrworkflow.service.IContextMgmtInfoPopulator)
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

		Map<String, List<ParticipantInfo>> txnMgrGroups = txnMgrConfigParserImpl
				.parse(selectedUrl);

		ctxMgmtInfoPopulator.processParticipantAnnotations(txnMgrGroups);
		Graph graphInter1 = converter.toGraph(txnMgrGroups);
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
	 * fr.dgrandemange.txnmgrworkflow.service.support.IFacade#getGraphSubFlowMode
	 * (java.net.URL,
	 * fr.dgrandemange.txnmgrworkflow.service.IContextMgmtInfoPopulator,
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
	 * fr.dgrandemange.txnmgrworkflow.service.IFacade#getGraphEntityRefsAsSubFlowMode
	 * (java.net.URL,
	 * fr.dgrandemange.txnmgrworkflow.service.IContextMgmtInfoPopulator,
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

			Map<String, List<ParticipantInfo>> txnMgrGroups = txnMgrConfigParser
					.parse(entityRefURL);

			ctxMgmtInfoPopulator
					.processParticipantAnnotations(txnMgrGroups);
			Graph graphInter1 = converter.toGraph(txnMgrGroups);
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
