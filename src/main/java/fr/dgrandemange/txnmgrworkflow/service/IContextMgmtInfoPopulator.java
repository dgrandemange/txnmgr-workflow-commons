package fr.dgrandemange.txnmgrworkflow.service;

import java.util.List;
import java.util.Map;

import fr.dgrandemange.txnmgrworkflow.model.Graph;
import fr.dgrandemange.txnmgrworkflow.model.ParticipantInfo;

/**
 * @author dgrandemange
 *
 */
public interface IContextMgmtInfoPopulator {

	/**
	 * @param txnMgrGroups
	 */
	public void processParticipantAnnotations(
			Map<String, List<ParticipantInfo>> txnMgrGroups);

	/**
	 * @param graphInter2
	 */
	public void updateReducedGraph(Graph graphInter2);
	
	
	
}
