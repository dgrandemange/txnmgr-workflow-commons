package fr.dgrandemange.txnmgrworkflow.service.support;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Attribute;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import fr.dgrandemange.txnmgrworkflow.io.EnclosedInputStream;
import fr.dgrandemange.txnmgrworkflow.model.EntityRefInfo;
import fr.dgrandemange.txnmgrworkflow.model.Graph;
import fr.dgrandemange.txnmgrworkflow.model.ParticipantInfo;
import fr.dgrandemange.txnmgrworkflow.model.SelectCriterion;
import fr.dgrandemange.txnmgrworkflow.model.SubFlowInfo;
import fr.dgrandemange.txnmgrworkflow.model.Wrapper;
import fr.dgrandemange.txnmgrworkflow.service.ITxnMgrConfigParser;

/**
 * JDOM based implementation of a transaction manager XML config parser
 * 
 * @author dgrandemange
 * 
 */
public class TxnMgrConfigParserSubflowEltImpl implements ITxnMgrConfigParser {

	public static final String DEFAULT_GROUP = "";
	public static final String TXN_MGR_CONFIG__ROOT_ELEMENT = "txnmgr";
	public static final String TXN_MGR_SUBCONFIG__ENTRYPOINTGROUP__NAMEEXTRACTIONREGEXP = "^(.*/)*([^/].*)\\.[^\\.]*$";

	public class EntityResolverImpl implements EntityResolver {

		private String base;

		public EntityResolverImpl(String base) {
			this.base = base;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
		 * java.lang.String)
		 */
		public InputSource resolveEntity(String publicId, String systemId)
				throws SAXException, IOException {

			try {
				String entityUrl = fixSystemId(systemId);

				if (null != entityUrl) {
					InputStream is = new URL(entityUrl).openStream();
					InputSource inputSource = new InputSource(is);
					inputSource.setSystemId(entityUrl);
					return inputSource;
				} else {
					return null;
				}
			} catch (Exception e) {
				return null;
			}
		}

		public String fixSystemId(String systemId)
				throws MalformedURLException, UnsupportedEncodingException {
			String entityUrl = null;

			// If system ID was a relative path, me must remove the
			// current dir prefixing the system id
			File currentDir = new File(System.getProperty("user.dir"));
			String sCurrentDirURL = currentDir.toURI().toURL().toString();

			File systemIdFile = new File(new URL(systemId).getFile());
			String sSystemIdFile = systemIdFile.toURI().toURL().toString();
			int lastIndex = sSystemIdFile.lastIndexOf(sCurrentDirURL);

			if (-1 == lastIndex) {
				// We try an URL decode of sSystemIdFile
				sSystemIdFile = URLDecoder.decode(sSystemIdFile, "UTF-8");
				lastIndex = sSystemIdFile.lastIndexOf(sCurrentDirURL);
			}

			if (-1 < lastIndex) {
				entityUrl = base
						+ sSystemIdFile.substring(sCurrentDirURL.length());

			}
			return entityUrl;
		}
	}

	private Map<String, Graph> graphByEntityRef;

	private DocType defaultDocType;

	private boolean subflowMode;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * fr.dgrandemange.txnmgrworkflow.service.ITxnMgrConfigParser#parse(java.
	 * net.URL)
	 */
	public Map<String, List<ParticipantInfo>> parse(URL url) {
		Map<String, List<ParticipantInfo>> groups = new HashMap<String, List<ParticipantInfo>>();

		EntityResolver entityResolver = getEntityResolver(url);

		// Check url query for a subflow param
		String subflowName = null;
		String urlQuery = url.getQuery();
		if (urlQuery != null) {
			StringTokenizer tokenizer = new StringTokenizer(urlQuery, "&");
			while (tokenizer.hasMoreElements()) {
				String nextToken = tokenizer.nextToken();
				Pattern pattern = Pattern.compile("subflow=(.*)");
				Matcher matcher = pattern.matcher(nextToken);
				if (matcher.matches()) {
					subflowName = matcher.group(1);
					break;
				}
			}
		}

		// Remove any query part from url
		URL urlNoQuery;
		try {
			urlNoQuery = new URL(url.getProtocol(), url.getHost(),
					url.getPort(), url.getPath());
		} catch (MalformedURLException e) {
			// Too bad ...
			throw new RuntimeException(e);
		}

		Document doc = getDocument(urlNoQuery, entityResolver, defaultDocType);

		if (null != doc) {

			Wrapper txnMgrEltWrapper = new Wrapper(null);

			if (subflowName == null) {
				Pattern pattern = Pattern
						.compile(TXN_MGR_SUBCONFIG__ENTRYPOINTGROUP__NAMEEXTRACTIONREGEXP);
				Matcher matcher = pattern.matcher(url.getPath());
				if (matcher.matches()) {
					subflowName = matcher.group(2);
				}
				
				findFirstTxnMgrEltAvailable(doc.getRootElement(),
						txnMgrEltWrapper);
			} else {
				findSubflowEltByName(doc.getRootElement(), subflowName,
						txnMgrEltWrapper);
			}

			if (txnMgrEltWrapper.getWrapped() != null) {
				Element txMgrElt = (Element) txnMgrEltWrapper.getWrapped();
				initParticipants(txMgrElt, groups, subflowName);
			}
		}

		return groups;
	}

	@SuppressWarnings("unchecked")
	private void findFirstTxnMgrEltAvailable(Element elt, Wrapper eltWrapper) {
		if (eltWrapper.getWrapped() != null) {
			// A txmgrElt has already been found, no need to get any further
			return;
		}

		if ("txnmgr".equalsIgnoreCase(elt.getName())) {
			eltWrapper.setWrapped(elt);
		} else {
			List<Element> children = elt.getChildren();
			for (Element child : children) {
				findFirstTxnMgrEltAvailable(child, eltWrapper);
				if (eltWrapper.getWrapped() != null) {
					// A matching element has already been found, no need to get
					// any
					// further
					break;
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void findSubflowEltByName(Element elt, String subflowName,
			Wrapper eltWrapper) {

		if (eltWrapper.getWrapped() != null) {
			// A txmgrElt has already been found, no need to get any further
			return;
		}

		if ("subflow".equalsIgnoreCase(elt.getName())) {
			Element childGroupElt = elt.getChild("group", elt.getNamespace());
			if (childGroupElt != null) {
				if (subflowName.equals(childGroupElt.getAttributeValue("name"))) {
					eltWrapper.setWrapped(elt);
				}
			}
		} else {
			List<Element> children = elt.getChildren();
			for (Element child : children) {
				findSubflowEltByName(child, subflowName, eltWrapper);
				if (eltWrapper.getWrapped() != null) {
					// A matching element has already been found, no need to get
					// any
					// further
					break;
				}
			}
		}
	}

	protected EntityResolverImpl getEntityResolver(URL url) {
		URL resolvedUrl = url;

		String sResolvedUrl = resolvedUrl.toString();

		int lastSep;
		int lastSep1 = sResolvedUrl.lastIndexOf('/');
		int lastSep2 = sResolvedUrl.lastIndexOf('\\');
		lastSep = (lastSep1 > lastSep2) ? lastSep1 : lastSep2;
		final String base = sResolvedUrl.substring(0, lastSep + 1);

		EntityResolverImpl entityResolver = new EntityResolverImpl(base);
		return entityResolver;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.dgrandemange.txnmgrworkflow.service.ITxnMgrConfigParser#
	 * entityRefsTopologicalSort(java.net.URL)
	 */
	public List<EntityRefInfo> entityRefsTopologicalSort(URL url) {
		Map<String, EntityRefInfo> entityRefs = new HashMap<String, EntityRefInfo>();

		Map<String, List<String>> entityDeps = listSubflowsInterDependencies(
				url, entityRefs);

		return sortEntityRefsTopologicalOrder(entityRefs, entityDeps);
	}

	public List<EntityRefInfo> sortEntityRefsTopologicalOrder(
			Map<String, EntityRefInfo> entityRefs,
			Map<String, List<String>> entityDeps) {
		List<String> sortedEntities = new ArrayList<String>();
		sortEntities("", entityDeps, sortedEntities);

		List<EntityRefInfo> res = new ArrayList<EntityRefInfo>();
		for (String entity : sortedEntities) {
			res.add(entityRefs.get(entity));
		}

		return res;
	}

	public Map<String, List<String>> listSubflowsInterDependencies(URL url,
			Map<String, EntityRefInfo> entityRefs) {
		Map<String, List<String>> entityDeps = new HashMap<String, List<String>>();

		EntityResolverImpl entityResolver = getEntityResolver(url);

		listSubflowsInterDependencies(url, null, entityResolver, entityRefs,
				entityDeps, "", true);
		return entityDeps;
	}

	protected void sortEntities(String entity,
			Map<String, List<String>> entityDeps, List<String> sortedEntities) {
		if (sortedEntities.contains(entity)) {
			return;
		}

		List<String> currEntityDeps = entityDeps.get(entity);

		for (String entityDep : currEntityDeps) {
			sortEntities(entityDep, entityDeps, sortedEntities);
		}

		if (!("".equals(entity))) {
			sortedEntities.add(entity);
		}
	}

	protected void listSubflowsInterDependencies(URL url, DocType docType,
			EntityResolverImpl entityResolver,
			Map<String, EntityRefInfo> entityRefs,
			Map<String, List<String>> entityDeps, String currentEntityName,
			boolean rootDocument) {
		List<String> currentEntityDeps = entityDeps.get(currentEntityName);

		if (null == currentEntityDeps) {
			currentEntityDeps = new ArrayList<String>();
			entityDeps.put(currentEntityName, currentEntityDeps);
		}

		Document doc = getDocument(url, entityResolver, docType);
		if (rootDocument) {
			docType = doc.getDocType();
		}

		Element config = doc.getRootElement();

		listSubflowsInterDependencies(url, docType, entityResolver, entityRefs,
				entityDeps, currentEntityDeps, config);
	}

	protected void listSubflowsInterDependencies(URL url, DocType docType,
			EntityResolverImpl entityResolver,
			Map<String, EntityRefInfo> entityRefs,
			Map<String, List<String>> entityDeps,
			List<String> currentEntityDeps, Element config) {
		// Looking for subflow elements
		for (Object o : config.getChildren()) {
			Element childElt = (Element) o;
			if ("subflow".equals((childElt).getName())) {
				Element subflowElt = childElt;
				// Search first child element of type 'group'
				Element firstChildGroup = subflowElt.getChild("group",
						subflowElt.getNamespace());
				if (null != firstChildGroup) {
					String subFlowName = firstChildGroup
							.getAttributeValue("name");
					currentEntityDeps.add(subFlowName);

					if (!entityDeps.containsKey(subFlowName)) {
						entityDeps.put(subFlowName, new ArrayList<String>());
					}

					if (!(entityRefs.containsKey(subFlowName))) {
						try {
							String subFlowUrlFile = String
									.format("%s?subflow=%s", url.getPath(),
											subFlowName);

							URL subflowUrl = new URL(url.getProtocol(),
									url.getHost(), url.getPort(),
									subFlowUrlFile);

							EntityRefInfo entityRefInfo = new EntityRefInfo(
									subFlowName, subflowUrl);
							entityRefs.put(subFlowName, entityRefInfo);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			} else {
				listSubflowsInterDependencies(url, docType, entityResolver,
						entityRefs, entityDeps, currentEntityDeps, childElt);
			}
		}
	}

	protected Document getDocument(URL url, EntityResolver entityResolver,
			DocType inheritedDocType) {
		SAXBuilder builder = new SAXBuilder();

		builder.setValidation(false);
		builder.setExpandEntities(true);

		builder.setFeature("http://xml.org/sax/features/namespaces", true);
		builder.setFeature("http://apache.org/xml/features/xinclude", true);

		builder.setEntityResolver(entityResolver);

		boolean JDOMExceptionOnFirstTry = false;

		Document doc = null;

		URL resolvedUrl = url;

		InputStream is = null;
		try {
			is = resolvedUrl.openStream();
			doc = builder.build(is);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (JDOMException e) {
			JDOMExceptionOnFirstTry = true;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					// We've done our best
				}
			}
		}

		if (JDOMExceptionOnFirstTry) {
			try {
				String docTypePart = "";
				if (null != inheritedDocType) {
					docTypePart = String.format("<!DOCTYPE %s [\n%s\n]>\n",
							inheritedDocType.getElementName(),
							inheritedDocType.getInternalSubset());
				}

				String prefix = String.format("%s<%s>", docTypePart,
						TXN_MGR_CONFIG__ROOT_ELEMENT);

				String suffix = String.format("</%s>",
						TXN_MGR_CONFIG__ROOT_ELEMENT);

				is = new EnclosedInputStream(prefix.getBytes(),
						removeNamespacePrefix(resolvedUrl.openStream()),
						suffix.getBytes());
				doc = builder.build(is);
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} catch (JDOMException e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (Exception e) {
						// We've done our best
					}
				}
			}
		}

		return doc;
	}

	private InputStream removeNamespacePrefix(InputStream rawIs)
			throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		int read;
		byte[] buf = new byte[256];
		while ((read = rawIs.read(buf)) > 0) {
			bos.write(buf, 0, read);
		}
		bos.flush();
		bos.close();

		ByteArrayOutputStream bosPatched = new ByteArrayOutputStream();
		PrintWriter pw = new PrintWriter(bosPatched);
		ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
		BufferedReader reader = new BufferedReader(new InputStreamReader(bis));
		String line;
		while ((line = reader.readLine()) != null) {
			String regexp = "<([/]{0,1})([^> ]*:)([^<]*)";
			String patchedLine = line.replaceAll(regexp, "<$1$3");
			pw.println(patchedLine);
		}
		pw.flush();
		pw.close();

		ByteArrayInputStream bisPatched = new ByteArrayInputStream(
				bosPatched.toByteArray());

		return bisPatched;
	}

	protected void initParticipants(Element config,
			Map<String, List<ParticipantInfo>> groups,
			String entryPointGroupName) {
		boolean entryPointIsAGroup = false;

		groups.put(DEFAULT_GROUP, initGroup(config, DEFAULT_GROUP));

		List<ParticipantInfo> defaultGroup = groups.get(DEFAULT_GROUP);

		if (0 == defaultGroup.size()) {
			if (null != entryPointGroupName) {
				// Try to match a group which is named the same as the
				// entryPointGroupName parameter
				@SuppressWarnings("rawtypes")
				Iterator iter = config.getChildren("group",
						config.getNamespace()).iterator();
				while (iter.hasNext()) {
					Element e = (Element) iter.next();
					String name = e.getAttributeValue("name");
					if (entryPointGroupName.equals(name)) {
						entryPointIsAGroup = true;
						@SuppressWarnings("rawtypes")
						Iterator iterParticipant = e.getChildren("participant",
								e.getNamespace()).iterator();
						while (iterParticipant.hasNext()) {
							defaultGroup.add(getParticipantInfo(
									(Element) iterParticipant.next(),
									entryPointGroupName));
						}
						break;
					}
				}
			}
		}

		lookForGroups(config, groups, entryPointGroupName, entryPointIsAGroup);

		// Looking for subflows
		List<Element> subflowEltChilds = config.getChildren("subflow",
				config.getNamespace());

		for (Element subflowEltChild : subflowEltChilds) {
			if (!subflowMode) {
				lookForGroups(subflowEltChild, groups, entryPointGroupName,
						entryPointIsAGroup);
			} else {
				Element childGroupElt = subflowEltChild.getChild("group",
						subflowEltChild.getNamespace());
				if (childGroupElt != null) {
					String subflowName = childGroupElt
							.getAttributeValue("name");

					Graph subFlowGraph = null;
					if (null != graphByEntityRef) {
						subFlowGraph = graphByEntityRef.get(subflowName);
					}

					SubFlowInfo pInfo = new SubFlowInfo(subflowName,
							subFlowGraph,
							new HashMap<String, SelectCriterion>());

					List<String> guaranteedCtxAttributes = new ArrayList<String>();
					pInfo.setGuaranteedCtxAttributes(guaranteedCtxAttributes);

					List<String> optionalCtxAttributes = new ArrayList<String>();
					pInfo.setOptionalCtxAttributes(optionalCtxAttributes);

					List<ParticipantInfo> group = new ArrayList<ParticipantInfo>();
					group.add(pInfo);

					groups.put(subflowName, group);
				}
			}
		}
	}

	protected void lookForGroups(Element config,
			Map<String, List<ParticipantInfo>> groups,
			String entryPointGroupName, boolean entryPointIsAGroup) {
		// Looking for groups
		@SuppressWarnings("rawtypes")
		Iterator iter = config.getChildren("group", config.getNamespace())
				.iterator();
		while (iter.hasNext()) {
			Element e = (Element) iter.next();
			String name = e.getAttributeValue("name");

			if (entryPointIsAGroup) {
				if (entryPointGroupName.equals(name)) {
					continue;
				}
			}

			if (name == null)
				throw new RuntimeException("missing group name");
			if (groups.get(name) != null) {
				throw new RuntimeException("Group '" + name
						+ "' already defined");
			}
			groups.put(name, initGroup(e, name));
		}
	}

	protected List<ParticipantInfo> initGroup(Element e, String groupName) {
		List<ParticipantInfo> group = new ArrayList<ParticipantInfo>();
		@SuppressWarnings("rawtypes")
		Iterator iter = e.getChildren("participant", e.getNamespace())
				.iterator();
		while (iter.hasNext()) {
			group.add(getParticipantInfo((Element) iter.next(), groupName));
		}
		return group;
	}

	protected ParticipantInfo getParticipantInfo(Element e, String groupName) {
		ParticipantInfo pInfo = new ParticipantInfo();
		pInfo.setGroupName(groupName);
		pInfo.setClazz((String) e.getAttributeValue("class"));
		Map<String, SelectCriterion> selectCriteria = new HashMap<String, SelectCriterion>();
		pInfo.setSelectCriteria(selectCriteria);
		ElementFilter elementsFilter = new ElementFilter("property");
		for (@SuppressWarnings("rawtypes")
		Iterator descendants = e.getDescendants(elementsFilter); descendants
				.hasNext();) {
			Element propertyElt = (Element) descendants.next();
			Attribute attribute = propertyElt.getAttribute("selectCriterion");
			if (attribute == null) {
				attribute = propertyElt.getAttribute("transition");
			}
			if (attribute != null) {
				SelectCriterion criterion = new SelectCriterion(
						propertyElt.getAttributeValue("name"),
						propertyElt.getAttributeValue("value"),
						attribute.getValue());
				selectCriteria.put(criterion.getName(), criterion);
			}
		}

		return pInfo;
	}

	public void setGraphByEntityRef(Map<String, Graph> graphByEntityRef) {
		this.graphByEntityRef = graphByEntityRef;
	}

	/**
	 * @return the defaultDocType
	 */
	public DocType getDefaultDocType() {
		return defaultDocType;
	}

	/**
	 * @param defaultDocType
	 *            the defaultDocType to set
	 */
	public void setDefaultDocType(DocType defaultDocType) {
		this.defaultDocType = defaultDocType;
	}

	/**
	 * @param subflowMode
	 */
	public void setSubFlowMode(boolean subflowMode) {
		this.subflowMode = subflowMode;
	}

}
