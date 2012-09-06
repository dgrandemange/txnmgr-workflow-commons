package org.jpos.jposext.jposworkflow.service;

import org.jpos.jposext.jposworkflow.model.Node;
import org.jpos.jposext.jposworkflow.model.Transition;

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
