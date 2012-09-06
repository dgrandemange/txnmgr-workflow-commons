package org.jpos.jposext.jposworkflow.helper;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jpos.jposext.jposworkflow.model.Graph;
import org.jpos.jposext.jposworkflow.model.Node;
import org.jpos.jposext.jposworkflow.model.NodeNatureEnum;
import org.jpos.jposext.jposworkflow.model.ParticipantInfo;
import org.jpos.jposext.jposworkflow.model.SelectCriterion;
import org.jpos.jposext.jposworkflow.model.SubFlowInfo;
import org.jpos.jposext.jposworkflow.model.Transition;

/**
 * @author dgrandemange
 * 
 */
public class GraphHelper {

	public static Graph getCopy(Graph toCopy) {
		if (null == toCopy) {
			return null;
		}

		Map<String, Node> clonedNodes = new HashMap<String, Node>();

		Node clonedInitialNode = getCopy(toCopy.getInitialNode());
		clonedNodes.put(clonedInitialNode.getId(), clonedInitialNode);

		Node clonedFinalNode = getCopy(toCopy.getFinalNode());
		clonedNodes.put(clonedFinalNode.getId(), clonedFinalNode);

		ArrayList<Transition> clonedLstTransitions = new ArrayList<Transition>();
		for (Transition t : toCopy.getLstTransitions()) {
			Node source;
			Node target;

			String sourceNodeId = t.getSource().getId();
			source = clonedNodes.get(sourceNodeId);
			if (null == source) {
				source = getCopy(t.getSource());
				clonedNodes.put(source.getId(), source);
			}

			String targetNodeId = t.getTarget().getId();
			target = clonedNodes.get(targetNodeId);
			if (null == target) {
				target = getCopy(t.getTarget());
				clonedNodes.put(target.getId(), target);
			}

			Transition transitionCopy = new Transition(t.getId(), t.getName(),
					source, target);
			transitionCopy.setDesc(t.getDesc());
			// transitionCopy.setGuaranteedCtxAttributes(new
			// ArrayList<String>());
			// transitionCopy.setOptionalCtxAttributes(new ArrayList<String>());
			// transitionCopy.setAttributesAdded(new ArrayList<String>());

			clonedLstTransitions.add(transitionCopy);
		}

		Graph copy = new Graph(clonedInitialNode, clonedFinalNode,
				clonedLstTransitions);

		GraphHelper.recomputeNodesTransitions(copy);

		return copy;

	}

	public static Node getCopy(Node toCopy) {
		if (null == toCopy) {
			return null;
		}

		Node copy = null;

		copy = new Node(toCopy.getId());
		copy.setNodeNature(toCopy.getNodeNature());
		copy.setType(toCopy.getType());

		if (null != toCopy.getParticipant()) {
			copy.setParticipant(GraphHelper.getCopy(toCopy.getParticipant()));
		}

		return copy;
	}

	public static ParticipantInfo getCopy(ParticipantInfo toCopy) {
		if (null == toCopy) {
			return null;
		}

		ParticipantInfo copy;
		if (toCopy instanceof SubFlowInfo) {
			SubFlowInfo subFlowInfoCopy = new SubFlowInfo();
			copy = subFlowInfoCopy;
			subFlowInfoCopy.setSubFlowGraph(((SubFlowInfo) toCopy)
					.getSubFlowGraph());
		} else {
			copy = new ParticipantInfo();
		}

		copy.setClazz(toCopy.getClazz());
		copy.setGroupName(toCopy.getGroupName());

		Map<String, SelectCriterion> selectCriteria = toCopy
				.getSelectCriteria();
		if (null != selectCriteria) {
			Map<String, SelectCriterion> cloneSelectCriteria = new HashMap<String, SelectCriterion>();

			for (Entry<String, SelectCriterion> entryCriterion : selectCriteria
					.entrySet()) {
				SelectCriterion criterion = entryCriterion.getValue();
				SelectCriterion cloneCriterion = new SelectCriterion(
						criterion.getName(), criterion.getValue(),
						criterion.getDesc());
				cloneSelectCriteria
						.put(entryCriterion.getKey(), cloneCriterion);
			}

			copy.setSelectCriteria(cloneSelectCriteria);
		}

		if (null != toCopy.getUpdCtxAttrByTransId()) {
			copy.setUpdCtxAttrByTransId(new HashMap<String, String[]>(toCopy
					.getUpdCtxAttrByTransId()));
		} else {
			copy.setUpdCtxAttrByTransId(null);
		}

		copy.setGuaranteedCtxAttributes(new ArrayList<String>());
		copy.setOptionalCtxAttributes(new ArrayList<String>());

		return copy;
	}

	public static void recomputeNodesTransitions(Graph graph) {
		List<Transition> lstTransitions = graph.getLstTransitions();

		// First reset nodes source/dest transitions list
		for (Transition t : lstTransitions) {
			t.getSource().setLstTransitionsAsDest(new ArrayList<Transition>());
			t.getSource()
					.setLstTransitionsAsSource(new ArrayList<Transition>());
			t.getTarget().setLstTransitionsAsDest(new ArrayList<Transition>());
			t.getTarget()
					.setLstTransitionsAsSource(new ArrayList<Transition>());
		}

		// Then recompute them
		for (Transition t : lstTransitions) {
			List<Transition> lstTransitionsAsSource = t.getSource()
					.getLstTransitionsAsSource();
			if (!lstTransitionsAsSource.contains(t)) {
				lstTransitionsAsSource.add(t);
			}

			List<Transition> lstTransitionsAsDest = t.getTarget()
					.getLstTransitionsAsDest();
			if (!lstTransitionsAsDest.contains(t)) {
				lstTransitionsAsDest.add(t);
			}
		}
	}

	public static void dumpGraph(Graph graph, PrintWriter pw) {
		pw.println("==========");
		for (Transition t : graph.getLstTransitions()) {
			Node source = t.getSource();
			ParticipantInfo srcParticipant = source.getParticipant();
			String srcGroupName = (null != srcParticipant) ? srcParticipant
					.getGroupName() : null;

			Node target = t.getTarget();
			ParticipantInfo tgtParticipant = target.getParticipant();
			String tgtGroupName = (null != tgtParticipant) ? tgtParticipant
					.getGroupName() : null;

			pw.println(String.format(
					"Tx [%s] / Source [%s] (%s) -- %s --> Target [%s] (%s)",
					t.getId(), source.getId(), srcGroupName, t.getDesc(),
					target.getId(), tgtGroupName));
		}
		pw.println("==========");
		pw.flush();
	}

	public static List<Node> lookForUnresolvedNodes(Graph currGraph) {
		List<Node> res = new ArrayList<Node>();
		HashSet<Node> inter = new HashSet<Node>();
		
		for (Transition t : currGraph.getLstTransitions()) {
			Node current = t.getSource();
			if (NodeNatureEnum.COMMON == current.getNodeNature()) {
				ParticipantInfo pInfo = current.getParticipant();				
				if ((null != pInfo) && (ParticipantInfo.UNDEFINED_CLAZZ.equals(pInfo.getClazz()))) {
					inter.add(current);
				}
			}
		}
		
		res.addAll(inter);
		
		return res;
	}

	public static String getLabelFromParticipantInfo(ParticipantInfo pInfo) {
		String label;
		
		if (null != pInfo) {
			if (isSubFlow(pInfo)) {
				label = pInfo.getGroupName();
			} else {
				String simpleClassName;
				String className = pInfo.getClazz();
				if (null != className) {
					int idx = className.lastIndexOf(".");					
					if ((idx > -1) && (idx + 1 < className.length())) {
						simpleClassName = className.substring(idx + 1);
					} else {
						simpleClassName = className;
					}
				}
				else {
					simpleClassName="";
				}
				
				if (isUndefined(pInfo)) {
					label = String.format("%s:%s", getGroupName(pInfo),
							simpleClassName);
				} else {
					label = simpleClassName;
				}
			}
		}
		else {
			label="";
		}
		
		return label;
	}

	public static String getLabelFromNodeData(Node node) {
		if (NodeNatureEnum.COMMON.equals(node.getNodeNature())) {
			return getLabelFromParticipantInfo(node.getParticipant());
		} else if (NodeNatureEnum.INITIAL.equals(node.getNodeNature())) {
			return "Inital state";
		} else if (NodeNatureEnum.FINAL.equals(node.getNodeNature())) {
			return "Final state";
		} else {
			return "";
		}
	}

	public static String getGroupName(Node node) {
		return getGroupName(node.getParticipant());
	}
	
	public static String getGroupName(ParticipantInfo pInfo) {
		String res = "";

		if (null != pInfo) {
			String groupName = pInfo.getGroupName();
			if (null != groupName) {
				res = groupName.trim();
			}
		}
		return res;
	}

	public static String getClassName(ParticipantInfo pInfo) {
		String res = "";
		if (null != pInfo) {
			String className = pInfo.getClazz();
			if (null != className) {
				res = className.trim();
			}
		}
		return res;
	}
	
	public static boolean isGroup(ParticipantInfo pInfo) {
		boolean res = false;

		if (null != pInfo) {
			String groupName = pInfo.getGroupName();
			if (null != groupName) {
				res = !("".equals(groupName.trim()));
			}
		}
		return res;
	}

	public static boolean isDynaGroup(ParticipantInfo pInfo) {
		boolean res = false;

		if (null != pInfo) {
			String groupName = pInfo.getGroupName();
			if (null != groupName) {
				res = groupName.matches(String.format("^%s_[0-9]*$",
						ParticipantInfo.DYNAGROUP_PREFIXE));
			}
		}
		return res;
	}
	
	public static boolean isSubFlow(Node node) {
		return isSubFlow(node.getParticipant());
	}	
	
	public static boolean isSubFlow(ParticipantInfo pInfo) {
		boolean res = false;

		if (null != pInfo) {
			return (pInfo instanceof SubFlowInfo);
		}
		return res;
	}
	
	public static boolean isUndefined(ParticipantInfo pInfo) {
		boolean res = false;

		if (null != pInfo) {
			res = ParticipantInfo.UNDEFINED_CLAZZ.equals(pInfo.getClazz());
		}
		return res;
	}
	
}
