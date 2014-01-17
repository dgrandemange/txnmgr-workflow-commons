package org.jpos.jposext.jposworkflow.service;

import java.net.URL;
import java.util.List;
import java.util.Map;

import org.jpos.jposext.jposworkflow.model.EntityRefInfo;
import org.jpos.jposext.jposworkflow.model.ParticipantInfo;

/**
 * jPos transaction manager congiguration parser interface
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