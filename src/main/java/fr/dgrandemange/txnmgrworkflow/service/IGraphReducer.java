package fr.dgrandemange.txnmgrworkflow.service;

import fr.dgrandemange.txnmgrworkflow.model.Graph;

/**
 * Describes a graph reduction service 
 * 
 * @author dgrandemange
 *
 */
public interface IGraphReducer {
	
	/**
	 * @param graph The graph to reduce
	 * @return The resulting graph after reduction 
	 */
	public Graph reduce(Graph graph);
	
}
