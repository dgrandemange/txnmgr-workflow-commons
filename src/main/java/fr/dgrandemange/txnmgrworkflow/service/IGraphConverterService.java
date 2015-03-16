package fr.dgrandemange.txnmgrworkflow.service;

import java.io.PrintWriter;

import fr.dgrandemange.txnmgrworkflow.model.Graph;

/**
 * @author dgrandemange
 *
 */
public interface IGraphConverterService {
	
	/**
	 * @param graph
	 * @param os
	 */
	public void convertGraphToDOT(String name, Graph graph, PrintWriter pw);
}
