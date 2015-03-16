package fr.dgrandemange.txnmgrworkflow.service;

import java.net.URL;
import java.util.List;
import java.util.Map;

import fr.dgrandemange.txnmgrworkflow.model.EntityRefInfo;
import fr.dgrandemange.txnmgrworkflow.model.ParticipantInfo;

/**
 * transaction manager configuration parser interface
 * 
 * @author dgrandemange
 * 
 */
public interface ITxnMgrConfigParser {

	/**
	 * @param url
	 * @return
	 */
	public Map<String, List<ParticipantInfo>> parse(URL url);

	/**
	 * @param url
	 *            transaction manager XML configuration URL
	 * @return List of entities referenced by the XML configuration; entities
	 *         are sorted using a topological sorting algorithm
	 */
	public List<EntityRefInfo> entityRefsTopologicalSort(URL url);

	/**
	 * @param selectedUrl
	 * @param entityRefs
	 * @return
	 */
	public Map<String, List<String>> listSubflowsInterDependencies(
			URL selectedUrl, Map<String, EntityRefInfo> entityRefs);

	/**
	 * @param entityRefs
	 * @param listSubflowsInterDependencies
	 * @return
	 */
	public List<EntityRefInfo> sortEntityRefsTopologicalOrder(
			Map<String, EntityRefInfo> entityRefs,
			Map<String, List<String>> listSubflowsInterDependencies);

}