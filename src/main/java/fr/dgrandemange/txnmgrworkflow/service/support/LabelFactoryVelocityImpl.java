package fr.dgrandemange.txnmgrworkflow.service.support;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

import fr.dgrandemange.txnmgrworkflow.helper.GraphHelper;
import fr.dgrandemange.txnmgrworkflow.model.DetailedNodeNatureEnum;
import fr.dgrandemange.txnmgrworkflow.model.Node;
import fr.dgrandemange.txnmgrworkflow.model.NodeNatureEnum;
import fr.dgrandemange.txnmgrworkflow.model.ParticipantInfo;
import fr.dgrandemange.txnmgrworkflow.model.Transition;
import fr.dgrandemange.txnmgrworkflow.service.IDOTLabelFactory;

/**
 * Implementation using Velocity engine along pre-defined templates to generate
 * HTML labels
 * 
 * @author dgrandemange
 * 
 */
public class LabelFactoryVelocityImpl implements IDOTLabelFactory {

	private VelocityEngine ve;

	private Template nodeTooltipTemplate;

	protected void init() {
		if (null == ve) {
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty("resource.loader", "class");
			ve.setProperty("class.resource.loader.class",
					"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			nodeTooltipTemplate = ve
					.getTemplate("/fr/dgrandemange/txnmgrworkflow/service/support/template/node_label.vm");
		}
	}

	/* (non-Javadoc)
	 * @see fr.dgrandemange.txnmgrworkflow.service.IDOTLabelFactory#create(fr.dgrandemange.txnmgrworkflow.model.Node)
	 */
	public String create(Node node) {
		init();

		VelocityContext context = new VelocityContext();

		ParticipantInfo pInfo = node.getParticipant();

		putHtmlEncodedValInVelocityContext(context, "nodeTooltipInfo1",
				GraphHelper.getLabelFromNodeData(node));

		if (NodeNatureEnum.INITIAL.equals(node.getNodeNature())) {
			putHtmlEncodedValInVelocityContext(context, "nodeTooltipInfo1",
					"Initial state");
		} else if (NodeNatureEnum.FINAL.equals(node.getNodeNature())) {
			putHtmlEncodedValInVelocityContext(context, "nodeTooltipInfo1",
					"Final state");
		} else if (GraphHelper.isGroup(pInfo)
				&& (!(GraphHelper.isDynaGroup(pInfo)))) {
			putHtmlEncodedValInVelocityContext(context, "nodeTooltipInfo2",
					GraphHelper.getGroupName(pInfo));

			if (GraphHelper.isUndefined(pInfo)) {
				putHtmlEncodedValInVelocityContext(context, "detailedNodeNatureAlt",
						DetailedNodeNatureEnum.undefGroupNature.getAlt());
			} else {
				if (GraphHelper.isSubFlow(pInfo)) {
					putHtmlEncodedValInVelocityContext(context, "detailedNodeNatureAlt",
							DetailedNodeNatureEnum.subflowNature.getAlt());
					context.put("nodeTooltipInfo3", "(subflow)");
				} else {
					putHtmlEncodedValInVelocityContext(context, "detailedNodeNatureAlt",
							DetailedNodeNatureEnum.groupNature.getAlt());
					context.put("nodeTooltipInfo3",
							GraphHelper.getClassName(pInfo));
				}
			}
		} else {
			context.put("nodeTooltipInfo2", null);

			if (GraphHelper.isUndefined(pInfo)) {
				putHtmlEncodedValInVelocityContext(context, "detailedNodeNatureAlt",
						DetailedNodeNatureEnum.undefParticipantNature.getAlt());
			} else {
				putHtmlEncodedValInVelocityContext(context, "detailedNodeNatureAlt",
						DetailedNodeNatureEnum.participantNature.getAlt());
				context.put("nodeTooltipInfo3", GraphHelper.getClassName(pInfo));
			}
		}

		return doMerge(context, nodeTooltipTemplate);
	}

	/* (non-Javadoc)
	 * @see fr.dgrandemange.txnmgrworkflow.service.IDOTLabelFactory#create(fr.dgrandemange.txnmgrworkflow.model.Transition)
	 */
	public String create(Transition t) {
		return String.format("\"%s\"", t.getDesc());
	}

	protected String doMerge(VelocityContext context, Template template) {
		StringWriter sw = new StringWriter();
		template.merge(context, sw);
		return String.format("<\n%s\n>", sw.toString());
	}

	protected void putHtmlEncodedValInVelocityContext(VelocityContext context,
			String key, String val) {
		String escaped = StringEscapeUtils.escapeHtml(val);
		context.put(key, escaped);
	}

	protected void putHtmlEncodedValInVelocityContext(VelocityContext context,
			String key, List<String> strList) {
		if ((strList == null) || (0 == strList.size())) {
			context.put(key, strList);
		} else {
			List<String> encodedList = new ArrayList<String>();
			for (String str : strList) {
				String escaped = StringEscapeUtils.escapeHtml(str);
				encodedList.add(escaped);
			}
			context.put(key, encodedList);
		}
	}

}
