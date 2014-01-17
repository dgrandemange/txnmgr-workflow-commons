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
	 * Generates a graph from a transaction manager XML configuration<br/>
	 * Graph is full expanded (no subflows)<br/>
	 * 
	 * @param selectedUrl [IN] Should point to jPos transaction manager XML configuration file
	 * @param ctxMgmtInfoPopulator	[IN] An IContextMgmtInfoPopulator implementation
	 * @return resulting graph
	 */
	public Graph getGraph(URL selectedUrl,
			IContextMgmtInfoPopulator ctxMgmtInfoPopulator);


	/**
	 * Generates a graph from a transaction manager XML configuration<br/>
	 * Each &lt;subflow&gt; element is considered a subflow and produces 
	 * its own graph that can be further retrieved in graphByEntityRef parameter<br/>
	 * 
	 * @param selectedUrl [IN] Should point to a transaction manager XML configuration file
	 * @param ctxMgmtInfoPopulator [IN] An IContextMgmtInfoPopulator implementation
	 * @param graphBySubflowName [IN/OUT] Generated graphs including root graph and subflows 
	 * @return resulting graph
	 */
	public Graph getGraphSubFlowMode(URL selectedUrl,
			IContextMgmtInfoPopulator ctxMgmtInfoPopulator,
			Map<String, Graph> graphBySubflowName);
	
	/**
	 * Generates a graph from a transaction manager XML configuration<br/>
	 * XML entity references are NOT expanded.<br>
	 * Each entity reference is considered a subflow and produces 
	 * its own graph that can be further retrieved in graphByEntityRef parameter<br/>
	 * 
	 * @param selectedUrl [IN] Should point to a transaction manager XML configuration file
	 * @param ctxMgmtInfoPopulator [IN] An IContextMgmtInfoPopulator implementation
	 * @param graphBySubflowName [IN/OUT] Generated graphs including root graph and subflows 
	 * @return resulting graph
	 */
	public Graph getGraphEntityRefsAsSubFlowMode(URL selectedUrl,
			IContextMgmtInfoPopulator ctxMgmtInfoPopulator,
			Map<String, Graph> graphBySubflowName);
}