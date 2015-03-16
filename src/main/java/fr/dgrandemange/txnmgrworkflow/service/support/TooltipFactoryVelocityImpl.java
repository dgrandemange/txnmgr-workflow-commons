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
 * HTML tooltips
 * 
 * @author dgrandemange
 * 
 */
public class TooltipFactoryVelocityImpl implements IDOTLabelFactory {

	private VelocityEngine ve;

	private Template nodeTooltipTemplate;

	private Template edgeTooltipTemplate;

	protected void init() {
		if (null == ve) {
			VelocityEngine ve = new VelocityEngine();
			ve.setProperty("resource.loader", "class");
			ve.setProperty("class.resource.loader.class",
					"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
			nodeTooltipTemplate = ve
					.getTemplate("/fr/dgrandemange/txnmgrworkflow/service/support/template/node_tooltip.vm");
			edgeTooltipTemplate = ve
					.getTemplate("/fr/dgrandemange/txnmgrworkflow/service/support/template/edge_tooltip.vm");
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
				putHtmlEncodedValInVelocityContext(context, "nodeNature",
						DetailedNodeNatureEnum.undefGroupNature.name());
			} else {
				if (GraphHelper.isSubFlow(pInfo)) {
					putHtmlEncodedValInVelocityContext(context, "nodeNature",
							DetailedNodeNatureEnum.subflowNature.name());
					context.put("nodeTooltipInfo3", "(subflow)");
				} else {
					putHtmlEncodedValInVelocityContext(context, "nodeNature",
							DetailedNodeNatureEnum.groupNature.name());
					context.put("nodeTooltipInfo3",
							GraphHelper.getClassName(pInfo));
				}
			}
		} else {
			context.put("nodeTooltipInfo2", null);

			if (GraphHelper.isUndefined(pInfo)) {
				putHtmlEncodedValInVelocityContext(context, "nodeNature",
						DetailedNodeNatureEnum.undefParticipantNature.name());
			} else {
				putHtmlEncodedValInVelocityContext(context, "nodeNature",
						DetailedNodeNatureEnum.participantNature.name());
				context.put("nodeTooltipInfo3", GraphHelper.getClassName(pInfo));
			}
		}

		if (null != pInfo) {
			putHtmlEncodedValInVelocityContext(context, "guaranteedAttributes",
					pInfo.getGuaranteedCtxAttributes());
			putHtmlEncodedValInVelocityContext(context,
					"nonGuaranteedAttributes", pInfo.getOptionalCtxAttributes());
		} else {
			context.put("guaranteedAttributes", null);
			context.put("nonGuaranteedAttributes", null);
		}

		return doMerge(context, nodeTooltipTemplate);
	}

	/* (non-Javadoc)
	 * @see fr.dgrandemange.txnmgrworkflow.service.IDOTLabelFactory#create(fr.dgrandemange.txnmgrworkflow.model.Transition)
	 */
	public String create(Transition t) {
		init();

		VelocityContext context = new VelocityContext();

		context.put("sourceIsSubflow", false);
		ParticipantInfo pInfo = t.getSource().getParticipant();
		if (null != pInfo) {
			context.put("sourceIsSubflow", GraphHelper.isSubFlow(pInfo));
		}

		putHtmlEncodedValInVelocityContext(context, "guaranteedAttributes",
				t.getGuaranteedCtxAttributes());
		putHtmlEncodedValInVelocityContext(context, "nonGuaranteedAttributes",
				t.getOptionalCtxAttributes());

		String transitionName = t.getName();
		if (!("".equals(transitionName))) {
			putHtmlEncodedValInVelocityContext(context, "transitionName",
					transitionName);
		} else {
			context.put("transitionName", null);
		}

		String transitionDesc = t.getDesc();
		if ((null != transitionDesc) && (transitionDesc.trim().length() > 0)) {
			putHtmlEncodedValInVelocityContext(context, "transitionDesc",
					transitionDesc);
		} else {
			context.put("transitionDesc", null);
		}

		putHtmlEncodedValInVelocityContext(context, "attributesAdded",
				t.getAttributesAdded());

		return doMerge(context, edgeTooltipTemplate);
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
