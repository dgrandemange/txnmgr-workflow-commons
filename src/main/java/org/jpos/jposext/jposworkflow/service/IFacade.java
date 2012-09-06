package org.jpos.jposext.jposworkflow.service;

import java.net.URL;
import java.util.Map;

import org.jpos.jposext.jposworkflow.model.Graph;

/**
 * @author dgrandemange
 *
 */
public interface IFacade {

	/**
	 * Generates a graph from a jPos transaction manager XML configuration<br/>
	 * Any entity refs are resolved to produce a full expanded graph (no subflows)<br/>
	 * 
	 * @param selectedUrl [IN] Should point to jPos transaction manager XML configuration file
	 * @param ctxMgmtInfoPopulator	[IN] An IContextMgmtInfoPopulator implementation
	 * @return resulting graph
	 */
	public Graph getGraph(URL selectedUrl,
			IContextMgmtInfoPopulator ctxMgmtInfoPopulator);

	/**
	 * Generates a graph from a jPos transaction manager XML configuration<br/>
	 * XML entity refs are NOT expanded. Each are considered a subflow and produces 
	 * its own graph that can be further retrieved in graphByEntityRef paramater<br/>
	 * 
	 * @param selectedUrl [IN] Should point to jPos transaction manager XML configuration file
	 * @param ctxMgmtInfoPopulator [IN] An IContextMgmtInfoPopulator implementation
	 * @param graphByEntityRef [IN/OUT] Generated graphs including root graph and subflows 
	 * @return resulting graph
	 */
	public Graph getGraphSubFlowMode(URL selectedUrl,
			IContextMgmtInfoPopulator ctxMgmtInfoPopulator,
			Map<String, Graph> graphByEntityRef);

}