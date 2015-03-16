package fr.dgrandemange.txnmgrworkflow.service;

import java.util.List;
import java.util.Map;

import fr.dgrandemange.txnmgrworkflow.model.Graph;
import fr.dgrandemange.txnmgrworkflow.model.ParticipantInfo;


/**
 * Interface de transformation de groupes de participants
 * 
 * @author dgrandemange
 *
 */
public interface ITxnMgrGroupsConverter {
	
	/**
	 * @param groups
	 * @return Un graphe
	 */
	Graph toGraph(Map<String, List<ParticipantInfo>> groups);
	
}
