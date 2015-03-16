package fr.dgrandemange.txnmgrworkflow.service;

import fr.dgrandemange.txnmgrworkflow.model.Node;
import fr.dgrandemange.txnmgrworkflow.model.Transition;

/**
 * Describes a factory service dedicated to DOT nodes and edges tooltips
 * 
 * @author dgrandemange
 *
 */
public interface IDOTLabelFactory {
	
	/**
	 * @param node
	 * @return Tooltip for node
	 */
	public String create(Node node);
	
	/**
	 * @param transition
	 * @return Tooltip for transition
	 */
	public String create(Transition transition);
}
