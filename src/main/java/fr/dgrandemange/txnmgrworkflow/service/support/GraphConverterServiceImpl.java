package fr.dgrandemange.txnmgrworkflow.service.support;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.dgrandemange.txnmgrworkflow.helper.GraphHelper;
import fr.dgrandemange.txnmgrworkflow.model.Graph;
import fr.dgrandemange.txnmgrworkflow.model.Node;
import fr.dgrandemange.txnmgrworkflow.model.NodeNatureEnum;
import fr.dgrandemange.txnmgrworkflow.model.Transition;
import fr.dgrandemange.txnmgrworkflow.service.IDOTLabelFactory;
import fr.dgrandemange.txnmgrworkflow.service.IGraphConverterService;

/**
 * @author dgrandemange
 *
 */
public class GraphConverterServiceImpl implements IGraphConverterService {

	private IDOTLabelFactory labelFactory;
	
	private IDOTLabelFactory toolTipFactory;
	
	
	/* (non-Javadoc)
	 * @see fr.dgrandemange.txnmgrworkflow.service.IGraphConverterService#convertGraphToDOT(java.lang.String, fr.dgrandemange.txnmgrworkflow.model.Graph, java.io.PrintWriter)
	 */	
	public void convertGraphToDOT(String name, Graph graph, PrintWriter pw) {
		pw.println(String.format("digraph \"%s\" {", name));
		
		pw.println("labeljust=\"l\";");		
		pw.println("labelloc=\"t\";");		
		pw.println(String.format("label=\"%s\";", name));		
		
		Map<String, Node> nodes = new HashMap<String, Node>();

		for (Transition t : graph.getLstTransitions()) {
			Node srcNode = t.getSource();
			Node destNode = t.getTarget();

			nodes.put(srcNode.getId(), srcNode);
			nodes.put(destNode.getId(), destNode);
		}

		pw.println("node [shape=\"box\" peripheries=\"1\" style=\"filled\" color=\"#000000\" fillcolor=\"#FFFFCE\" fontname=\"Arial\" fontsize=\"10\"]");
		for (Entry<String, Node> entry : nodes.entrySet()) {
			Node node = entry.getValue();
						
			String tooltip = "\"\"";
			if (null != toolTipFactory) {
				tooltip = toolTipFactory.create(node);
			}

			if (NodeNatureEnum.INITIAL.equals(node.getNodeNature())) {
				pw
						.println(String
								.format(
										"%s [label=\"\" shape=\"circle\" peripheries=\"1\" style=\"filled\" color=\"#000000\" fillcolor=\"#000000\" tooltip=%s]",
										node.getId(), tooltip));
			} else if (NodeNatureEnum.FINAL.equals(node.getNodeNature())) {
				pw
						.println(String
								.format(
										"%s [label=\"\" shape=\"circle\" peripheries=\"2\" style=\"filled\" color=\"#000000\" fillcolor=\"#000000\" tooltip=%s]",
										node.getId(), tooltip));
			} else {
				String nodeLabel = String.format("\"%s\"", GraphHelper.getLabelFromNodeData(node));
				if (null != labelFactory) {
					nodeLabel = labelFactory.create(node);
				}
				
				if (GraphHelper.isSubFlow(node)) {
					pw.println(String.format("%s [label=%s href=\"%s\" tooltip=%s]", node.getId(),
							nodeLabel, GraphHelper.getGroupName(node), tooltip));
				}
				else {
					pw.println(String.format("%s [label=%s tooltip=%s]", node.getId(),
							nodeLabel, tooltip));
				}
			}
		}

		pw.println("edge [fontname=\"Arial\" fontsize=\"8\" dir=\"forward\" arrowhead=\"normal\"]");
		for (Transition t : graph.getLstTransitions()) {
			Node srcNode = t.getSource();
			Node destNode = t.getTarget();
			
			String edgeLabel = String.format("\"%s\"", t.getDesc());
			if (null != labelFactory) {
				edgeLabel = labelFactory.create(t);
			}			
			
			String tooltip = "\"\"";
			if (null != toolTipFactory) {
				tooltip = toolTipFactory.create(t);
			}

			pw
					.println(String
							.format(
									"%s -> %s [label=%s tooltip=%s]",
									srcNode.getId(), destNode.getId(), edgeLabel, tooltip));
		}

		pw.println(String.format("}", name));
		pw.flush();
	}

	/**
	 * @param toolTipFactory the toolTipFactory to set
	 */
	public void setToolTipFactory(IDOTLabelFactory toolTipFactory) {
		this.toolTipFactory = toolTipFactory;
	}

	/**
	 * @param labelFactory the labelFactory to set
	 */
	public void setLabelFactory(IDOTLabelFactory labelFactory) {
		this.labelFactory = labelFactory;
	}

}
