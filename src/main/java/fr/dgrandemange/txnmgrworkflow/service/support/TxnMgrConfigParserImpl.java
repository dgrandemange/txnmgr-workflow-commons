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
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Attribute;
import org.jdom.DocType;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.EntityRef;
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
public class TxnMgrConfigParserImpl implements ITxnMgrConfigParser {

	private static final String REGEXP_PATERN__DTD_EXTENSION = "^.*\\.[dD][tT][dD]$";
	public static final String DEFAULT_GROUP = "";
	public static final String TXN_MGR_CONFIG__ROOT_ELEMENT = "txnmgr";
	public static final String TXN_MGR_SUBCONFIG__ENTRYPOINTGROUP__NAMEEXTRACTIONREGEXP = "^(.*/)*([^/].*)\\.[^\\.]*$";

	public static class EntityResolverImpl implements EntityResolver {

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

		Document doc = getDocument(url, entityResolver, defaultDocType);

		if (null != doc) {
			String pathLastToken = null;
			Pattern pattern = Pattern
					.compile(TXN_MGR_SUBCONFIG__ENTRYPOINTGROUP__NAMEEXTRACTIONREGEXP);
			Matcher matcher = pattern.matcher(url.getPath());
			if (matcher.matches()) {
				pathLastToken = matcher.group(2);
			}

			Wrapper txnMgrEltWrapper = new Wrapper(null);
			findFirstTxnMgrEltAvailable(doc.getRootElement(), txnMgrEltWrapper);

			if (txnMgrEltWrapper.getWrapped() != null) {
				Element txMgrElt = (Element) txnMgrEltWrapper.getWrapped();
				initParticipants(txMgrElt, groups, pathLastToken);
			}
		}

		return groups;
	}

	@SuppressWarnings("unchecked")
	private void findFirstTxnMgrEltAvailable(Element rootElt,
			Wrapper txnMgrEltWrapper) {
		if (txnMgrEltWrapper.getWrapped() != null) {
			// A txmgrElt has already been found, no need to get any further
			return;
		}

		if ("txnmgr".equalsIgnoreCase(rootElt.getName())) {
			txnMgrEltWrapper.setWrapped(rootElt);
		} else {
			List<Element> children = rootElt.getChildren();
			for (Element child : children) {
				findFirstTxnMgrEltAvailable(child, txnMgrEltWrapper);
				if (txnMgrEltWrapper.getWrapped() != null) {
					// A txmgrElt has already been found, no need to get any
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

		listEntitiesInterDependencies(url, null, entityResolver, entityRefs,
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

	protected void listEntitiesInterDependencies(URL url, DocType docType,
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

		listEntitiesInterDependencies(docType, entityResolver, entityRefs,
				entityDeps, currentEntityDeps, config);
	}

	protected void listEntitiesInterDependencies(DocType docType,
			EntityResolverImpl entityResolver,
			Map<String, EntityRefInfo> entityRefs,
			Map<String, List<String>> entityDeps,
			List<String> currentEntityDeps, Element config) {

		// Looking for entity references

		Pattern pattern = Pattern
				.compile("<\\!ENTITY\\s*([a-zA-Z0-9_]*)\\s*.*\\s*SYSTEM\\s*\"(.*)\".*>");

		String docTypeInternalSubset;
		if (docType != null) {
			docTypeInternalSubset = docType.getInternalSubset();
		} else {
			docTypeInternalSubset = "";
		}
		Scanner scanner = new Scanner(docTypeInternalSubset);

		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			line = line.trim();
			Matcher matcher = pattern.matcher(line);
			if (matcher.matches()) {
				String ERname = matcher.group(1);
				String ERsystemID = matcher.group(2);
				boolean refersDTD;
				if (ERsystemID == null) {
					refersDTD = false;
				} else {
					refersDTD = ERsystemID
							.matches(REGEXP_PATERN__DTD_EXTENSION);
				}

				if ((ERsystemID != null) && (!refersDTD)) {
					currentEntityDeps.add(ERname);
					if (!(entityRefs.containsKey(ERname))) {
						try {
							String fixedSystemId = entityResolver
									.fixSystemId(ERsystemID);
							listEntitiesInterDependencies(
									new URL(fixedSystemId), null,
									entityResolver, entityRefs, entityDeps,
									ERname, false);
							EntityRefInfo entityRefInfo = new EntityRefInfo(
									ERname, new URL(fixedSystemId));
							entityRefs.put(ERname, entityRefInfo);
						} catch (Exception e) {
							throw new RuntimeException(e);
						}
					}
				}
			}
		}

		@SuppressWarnings("rawtypes")
		List content = config.getContent();
		@SuppressWarnings("rawtypes")
		Iterator iterEntityRef = content.listIterator();
		while (iterEntityRef.hasNext()) {
			Object o = iterEntityRef.next();
			if (o instanceof Element) {
				listEntitiesInterDependencies(null, entityResolver,
						entityRefs, entityDeps, currentEntityDeps, (Element) o);
			}
		}
	}

	public void useXmlDocType(URL url) {
		Document doc = getDocument(url, null, null);
		DocType docType = doc.getDocType();
		if (null != docType) {
			this.defaultDocType = (DocType) docType.clone();
		}
	}

	protected Document getDocument(URL url, EntityResolver entityResolver,
			DocType inheritedDocType) {
		SAXBuilder builder = new SAXBuilder();

		builder.setValidation(false);
		builder.setExpandEntities(!subflowMode);

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

		// Looking for entity references
		@SuppressWarnings("rawtypes")
		List content = config.getContent();
		@SuppressWarnings("rawtypes")
		Iterator iterEentityRef = content.listIterator();
		while (iterEentityRef.hasNext()) {
			Object o = iterEentityRef.next();
			if (o instanceof EntityRef) {
				EntityRef entityRef = (EntityRef) o;
				String ERname = entityRef.getName();

				Graph subFlowGraph = null;
				if (null != graphByEntityRef) {
					subFlowGraph = graphByEntityRef.get(ERname);
				}

				SubFlowInfo pInfo = new SubFlowInfo(ERname, subFlowGraph,
						new HashMap<String, SelectCriterion>());

				List<String> guaranteedCtxAttributes = new ArrayList<String>();
				pInfo.setGuaranteedCtxAttributes(guaranteedCtxAttributes);

				List<String> optionalCtxAttributes = new ArrayList<String>();
				pInfo.setOptionalCtxAttributes(optionalCtxAttributes);

				List<ParticipantInfo> group = new ArrayList<ParticipantInfo>();
				group.add(pInfo);

				groups.put(ERname, group);

			} else if (o instanceof Element) {
				// TODO voir si qqchose à faire ici
			}
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
