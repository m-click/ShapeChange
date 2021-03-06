/**
 * ShapeChange - processing application schemas for geographic information
 *
 * This file is part of ShapeChange. ShapeChange takes a ISO 19109
 * Application Schema from a UML model and translates it into a
 * GML Application Schema or other implementation representations.
 *
 * Additional information about the software can be found at
 * http://shapechange.net/
 *
 * (c) 2002-2013 interactive instruments GmbH, Bonn, Germany
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Contact:
 * interactive instruments GmbH
 * Trierer Strasse 70-72
 * 53115 Bonn
 * Germany
 */

package de.interactive_instruments.ShapeChange;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SystemUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.interactive_instruments.ShapeChange.AIXMSchemaInfos.AIXMSchemaInfo;
import de.interactive_instruments.ShapeChange.Model.PackageInfo;
import de.interactive_instruments.ShapeChange.Model.Stereotypes;
import de.interactive_instruments.ShapeChange.Model.StereotypesCacheSet;
import de.interactive_instruments.ShapeChange.Model.TaggedValues;
import de.interactive_instruments.ShapeChange.Model.TaggedValuesCacheArray;
import de.interactive_instruments.ShapeChange.Model.TaggedValuesCacheMap;
import de.interactive_instruments.ShapeChange.Target.Target;
import de.interactive_instruments.ShapeChange.Target.FeatureCatalogue.FeatureCatalogue;

public class Options {

	//
	// Constants
	//

	/** Parser feature ids. */
	public static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	public static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
	public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	public static final String W3C_RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String W3C_RDFS = "http://www.w3.org/2000/01/rdf-schema#";
	public static final String W3C_OWL = "http://www.w3.org/2002/07/owl#";
	public static final String W3C_OWL2XML = "http://www.w3.org/2006/12/owl2-xml#";
	public static final String DC = "http://purl.org/dc/elements/1.1/";
	public static final String OGC_GEOSPARQL = "http://www.opengis.net/geosparql#";
	public static final String W3C_SKOS = "http://www.w3.org/2004/02/skos/core#";

	public static final String SCRS_NS = "http://www.interactive-instruments.de/ShapeChange/Result";
	public static final String SCAI_NS = "http://www.interactive-instruments.de/ShapeChange/AppInfo";
	public static final String SCHEMATRON_NS = "http://purl.oclc.org/dsdl/schematron";
	public static final String DGIWGSP_NSABR = "gmldgiwgsp";
	public static final String DGIWGSP_NS = "http://www.dgiwg.org/gml/3.2/profiles/spatial/1.0/";

	// TODO move these to each target
	public static final String DEF_NS = "http://www.interactive-instruments.de/ShapeChange/Definitions/0.5";
	public static final String XTRASERVER_NS = "http://www.interactive-instruments.de/namespaces/XtraServer";
	public static final String GDBDOC_NS = "http://www.interactive-instruments.de/namespaces/GdbDoc";

	/* Target Java Classes */
	/**
	 *
	 */
	public static final String TargetXmlSchemaClass = "de.interactive_instruments.ShapeChange.Target.XmlSchema.XmlSchema";
	public static final String TargetFOL2SchematronClass = "de.interactive_instruments.ShapeChange.Target.FOL2Schematron.FOL2Schematron";
	public static final String TargetJsonSchemaClass = "de.interactive_instruments.ShapeChange.Target.JSON.JsonSchema";
	public static final String TargetFeatureCatalogueClass = "de.interactive_instruments.ShapeChange.Target.FeatureCatalogue.FeatureCatalogue";
	public static final String TargetOWLISO19150Class = "de.interactive_instruments.ShapeChange.Target.Ontology.OWLISO19150";
	public static final String TargetRDFClass = "de.interactive_instruments.ShapeChange.Target.Ontology.RDF";
	public static final String TargetSQLClass = "de.interactive_instruments.ShapeChange.Target.SQL.SqlDdl";
	public static final String TargetArcGISWorkspaceClass = "de.interactive_instruments.ShapeChange.Target.ArcGISWorkspace.ArcGISWorkspace";
	public static final String TargetReplicationSchemaClass = "de.interactive_instruments.ShapeChange.Target.ReplicationSchema.ReplicationXmlSchema";
	public static final String TargetApplicationSchemaMetadata = "de.interactive_instruments.ShapeChange.Target.Metadata.ApplicationSchemaMetadata";

	/** XML Schema encoding rules */
	public static final String ISO19136_2007 = "iso19136_2007".toLowerCase();
	public static final String ISO19139_2007 = "iso19139_2007".toLowerCase();
	public static final String ISO19136_2007_SHAPECHANGE_1_0 = "iso19136_2007_ShapeChange_1.0_Extensions"
			.toLowerCase();
	public static final String ISO19136_2007_INSPIRE = "iso19136_2007_INSPIRE_Extensions"
			.toLowerCase();
	public static final String ISO19136_2007_NO_GML = "iso19136_2007_NoGmlBaseTypes"
			.toLowerCase();
	public static final String ISO19150_2014 = "iso19150_2014".toLowerCase();
	public static final String SQL = "sql";
	public static final String SWECOMMON2 = "ogcSweCommon2".toLowerCase();
	public static final String NOT_ENCODED = "notEncoded".toLowerCase();
	public static final String GSIP_ENC = "gsip".toLowerCase();

	/** Well known stereotypes */
	public static final String[] classStereotypes = { "codelist", "enumeration",
			"datatype", "featuretype", "type", "basictype", "interface",
			"union", "abstract", "fachid", "schluesseltabelle", "adeelement",
			"featureconcept", "attributeconcept", "valueconcept",
			"aixmextension" };
	public static final String[] assocStereotypes = { "disjoint" };
	/**
	 * NOTE: stereotype "identifier" is deprecated
	 */
	public static final String[] propertyStereotypes = { "voidable",
			"identifier", "version", "property", "estimated" };
	public static final String[] packageStereotypes = { "application schema",
			"bundle", "leaf" };
	public static final String[] depStereotypes = { "import", "include" };

	/** Carriage Return and Line Feed characters. */
	public static final String CRLF = "\r\n";

	/** Model types. */
	public static final int XMI10 = 1;
	public static final int EA7 = 2;
	public static final int GSIP = 3;
	public static final int GENERIC = 4;

	/** SQL targets. TODO move the relevant targets */
	public static final int NONE = 0;
	// public static final int SFSQL = 1;
	public static final int ORACLE10 = 2;
	public static final int POSTGIS = 3;
	public static final int GDB = 4;

	/** Global vs local declarations. */
	public static final int GLOBAL = 1;
	public static final int LOCAL = 2;

	/** Class categories. */
	public static final int UNKNOWN = -1;
	public static final int FEATURE = 1;
	public static final int CODELIST = 2;
	public static final int ENUMERATION = 3;
	public static final int MIXIN = 4;
	public static final int DATATYPE = 5;
	public static final int OBJECT = 6;
	public static final int GMLOBJECT = 6; // for backwards compatibility
	public static final int BASICTYPE = 7;
	public static final int UNION = 8;
	// 2013-10-14: UNIONDIRECT has been retired, use UNION and
	// ClassInfo.isUnionDirect() instead
	// public static final int UNIONDIRECT = 9;
	public static final int OKSTRAKEY = 11;
	public static final int OKSTRAFID = 12;
	public static final int FEATURECONCEPT = 13;
	public static final int ATTRIBUTECONCEPT = 14;
	public static final int VALUECONCEPT = 15;
	public static final int AIXMEXTENSION = 16;

	/* These constants are used when loading diagrams from the input model */
	public static final String ELEMENT_NAME_KEY_FOR_DIAGRAM_MATCHING = "NAME";
	public static final String IMAGE_INCLUSION_CLASS_REGEX = "NAME";
	public static final String IMAGE_INCLUSION_PACKAGE_REGEX = "NAME";

	/** Constants of the ShapeChangeConfiguration */
	public static final String INPUTELEMENTID = "INPUT";

	/**
	 * Defines the name of the configuration parameter via which a
	 * comma-separated list of package names to exclude from the model loading
	 * can be provided.
	 */
	public static final String PARAM_INPUT_EXCLUDED_PACKAGES = "excludedPackages";

	/**
	 * Defines the name of the input parameter that provides the location of an
	 * excel file with constraints (currently only SBVR rules are supported)
	 * that shall be loaded before postprocessing the input model.
	 */
	public static final String PARAM_CONSTRAINT_EXCEL_FILE = "constraintExcelFile";

	public static final String PARAM_ONLY_DEFERRABLE_OUTPUT_WRITE = "onlyDeferrableOutputWrite";
	public static final String PARAM_USE_STRING_INTERNING = "useStringInterning";
	public static final String PARAM_LANGUAGE = "language"; // TODO document

	/**
	 * If set to “array”, ShapeChange will use a memory optimized implementation
	 * of tagged values when processing the model.Use this option when
	 * processing very large models. ShapeChange can process 100+MB sized models
	 * without problem. However, if processing involves many transformations and
	 * target derivations you may hit a memory limit, which is determined by the
	 * maximum amount of memory you can assign to the Java process in which
	 * ShapeChange is running. On Windows machines that were used for
	 * development, that limit was near 1.1GB.
	 */
	public static final String PARAM_TAGGED_VALUE_IMPL = "taggedValueImplementation";

	/**
	 * (applies to EA7 input only) Defines if global identifiers of model
	 * elements shall be loaded ("true") or not. Default is to not load them.
	 */
	public static final String PARAM_LOAD_GLOBAL_IDENTIFIERS = "loadGlobalIdentifiers";

	/**
	 * Set this input parameter to <code>true</code> if constraints shall only
	 * be loaded for classes and properties from the schemas selected for
	 * processing (and ignoring all constraints from other packages).
	 * <p>
	 * Don't make use of this parameter if one of the classes from the selected
	 * schema packages extends another class from an external package (e.g. an
	 * ISO package) and needs to inherit constraints from that class!
	 * <p>
	 * This parameter is primarily a convenience mechanism to avoid loading and
	 * parsing constraints from external packages (especially ISO packages) that
	 * are irrelevant for processing. So on the one hand this can speed up model
	 * loading. On the other hand, it can prevent messages about constraints
	 * that were parsed from the elements of an external package from appearing
	 * in the log.
	 */
	public static final String PARAM_LOAD_CONSTRAINT_FOR_SEL_SCHEMAS_ONLY = "loadConstraintsForSelectedSchemasOnly";

	// Application schema defaults (namespace and version)
	public String xmlNamespaceDefault = "FIXME";
	public String xmlNamespaceAbbreviationDefault = "FIXME";
	public String appSchemaVersion = "unknown";

	// Name of the configuration file
	public String configFile = null;

	/** GML core namespaces */
	public String GML_NS = "http://www.opengis.net/gml/3.2";
	public static String GMLEXR_NS = "http://www.opengis.net/gml/3.3/exr";

	// Default target version of GML
	// Note: the use of ISO/TS 19139 requires the use of GML 3.2
	// This parameter is deprecated and should not be used
	// TODO remove usages from external targets
	public String gmlVersion = "3.2";

	/**
	 * If set to true, schemas (from the model) will be processed in some stable
	 * (not random) order. To change the processing order of the classes in a
	 * specific Target use the targetParameter "sortedOutput" in the
	 * configuration file.
	 */
	public boolean sortedSchemaOutput = false;

	// Bugfixes:

	// Fix for bugs in Rose
	public boolean roseBugFixDuplicateGlobalDataTypes = true;

	// Fix for bugs in EA
	public boolean eaBugFixWrongID = true;
	public boolean eaBugFixPublicPackagesAreMarkedAsPrivate = true;
	public boolean eaIncludeExtentsions = true;

	// Fix for bugs in ArgoUML
	public boolean argoBugFixMissingDOCTYPE = false;

	// Temporary directory for ShapeChange run
	public static final String DEFAULT_TMP_DIR_PATH = "temp";
	public static final String TMP_DIR_PATH_PARAM = "tmpDirectory";
	protected File tmpDir = null;

	/**
	 * Map of targets to generate from the input model. Keys are the names of
	 * the Java classes which must implement the Target interface and values are
	 * the requested processing modes, one of "enabled", "disabled",
	 * "diagnostics-only".
	 */
	protected HashMap<String, String> fTargets = new HashMap<String, String>();

	/** Hash table for additional parameters */
	protected HashMap<String, String> fParameters = new HashMap<String, String>();

	/**
	 * Hash table for string replacements. Must not be reset, because
	 * replacement information is not contained in the configuration itself!
	 */
	protected HashMap<String, String> fReplace = new HashMap<String, String>();

	/** Hash table for type and element mappings */
	protected HashMap<String, MapEntry> fTypeMap = new HashMap<String, MapEntry>();

	/**
	 * Key: type + "#" + xsdEncodingRule
	 * <p>
	 * Value: MapEntry with:
	 * <ul>
	 * <li>rule: ("direct")</li>
	 * <li>p1: xmlType</li>
	 * <li>(optionally) p2: xmlTypeType+"/"+xmlTypeContent</li>
	 * </ul>
	 */
	protected HashMap<String, MapEntry> fBaseMap = new HashMap<String, MapEntry>();
	protected HashMap<String, MapEntry> fElementMap = new HashMap<String, MapEntry>();
	protected HashMap<String, MapEntry> fAttributeMap = new HashMap<String, MapEntry>();
	protected HashMap<String, MapEntry> fAttributeGroupMap = new HashMap<String, MapEntry>();

	/** Map of type mapping tables for non-XMLSchema targets */
	protected HashMap<String, HashMap<String, MapEntry>> fTargetTypeMap = new HashMap<String, HashMap<String, MapEntry>>();

	/** Hash table for all stereotype and tag aliases */
	protected HashMap<String, String> fStereotypeAliases = new HashMap<String, String>();
	protected HashMap<String, String> fTagAliases = new HashMap<String, String>();

	/** Hash table for all descriptor sources */
	protected HashMap<String, String> fDescriptorSources = new HashMap<String, String>();

	/** Known descriptors */
	public static enum Descriptor {
		ALIAS("alias"),
		PRIMARYCODE("primaryCode"),
		DOCUMENTATION("documentation"),
		DEFINITION("definition"),
		DESCRIPTION("description"),
		EXAMPLE("example"),
		LEGALBASIS("legalBasis"),
		DATACAPTURESTATEMENT("dataCaptureStatement"),
		LANGUAGE("language");
		
		private String name = null;		
		Descriptor(String n) {name = n;};
		public String toString() {return name;};
	}

	public static final String DERIVED_DOCUMENTATION_DEFAULT_TEMPLATE = "[[definition]]";
	public static final String DERIVED_DOCUMENTATION_DEFAULT_NOVALUE = "";

	public static final String LF = System.getProperty("line.separator"); 
	public static final String DERIVED_DOCUMENTATION_INSPIRE_TEMPLATE = "-- Name --"+LF+"[[alias]]"+LF+LF+
			"-- Definition --"+LF+"[[definition]]"+LF+LF+
			"-- Description --"+LF+"[[description]]";
	
	/**
	 * Hash table for external namespaces
	 * <p>
	 * key: namespace abbreviation / prefix; value: MapEntry (arg1: namespace,
	 * arg2: location)
	 */
	protected HashMap<String, MapEntry> fNamespaces = new HashMap<String, MapEntry>();

	/** Hash table for packages */
	protected HashMap<String, MapEntry> fPackages = new HashMap<String, MapEntry>();

	/**
	 * Hash table for schema locations
	 * <p>
	 * key: namespace, value: location
	 */
	protected HashMap<String, String> fSchemaLocations = new HashMap<String, String>();

	/** Hash table for schema requirements and conversion rules */
	protected HashSet<String> fAllRules = new HashSet<String>();
	protected HashSet<String> fRulesInEncRule = new HashSet<String>();

	/** Hash table for encoding rule extensions */
	protected HashMap<String, String> fExtendsEncRule = new HashMap<String, String>();

	/** documentation separators */
	protected String extractSeparator = null;
	protected String definitionSeparator = null;
	protected String descriptionSeparator = null;
	protected String nameSeparator = null;

	public String extractSeparator() {
		if (extractSeparator == null)
			extractSeparator = parameter("extractSeparator");
		return extractSeparator;
	}

	public String definitionSeparator() {
		if (definitionSeparator == null)
			definitionSeparator = parameter("definitionSeparator");
		return definitionSeparator;
	}

	public String descriptionSeparator() {
		if (descriptionSeparator == null)
			descriptionSeparator = parameter("descriptionSeparator");
		return descriptionSeparator;
	}

	public String nameSeparator() {
		if (nameSeparator == null)
			nameSeparator = parameter("nameSeparator");
		return nameSeparator;
	}
	
	// determines, if unset class descriptors are inherited from a superclass of the same name
	boolean getDescriptorsFromSupertypesInitialised = false;
	boolean getDescriptorsFromSupertypes = false;
	
	public boolean getDescriptorsFromSupertypes() {
		if (!getDescriptorsFromSupertypesInitialised) {		
			getDescriptorsFromSupertypesInitialised = true;
			String s = parameter("inheritDocumentation");
			// still support deprecated parameter in the FeatureCatalogue target for backward compatibility
			if (s == null || !s.equals("true"))
				s = parameter(FeatureCatalogue.class.getName(), "inheritDocumentation");
			if (s != null && s.equals("true"))
				getDescriptorsFromSupertypes = true;			
		}
		return getDescriptorsFromSupertypes;
	}
	
	// determines, if unset package, class or property descriptors are inherited from a dependency
	boolean getDescriptorsFromDependencyInitialised = false;
	boolean getDescriptorsFromDependency = true;
	
	public boolean getDescriptorsFromDepedency() {
		if (!getDescriptorsFromDependencyInitialised) {		
			getDescriptorsFromDependencyInitialised = true;
			String s = parameter("documentationFromDependency");
			if (s != null && s.equals("false"))
				getDescriptorsFromDependency = false;			
		}
		return getDescriptorsFromDependency;
	}
	
	// /**
	// * List of transformer configurations that directly reference the input
	// * element.
	// */
	// protected List<TransformerConfiguration> transformerConfigurations =
	// null;
	//
	// /**
	// * List of all target configurations that directly reference the input
	// * element.
	// */
	// protected List<TargetConfiguration> inputTargetConfigurations = null;

	/**
	 * True, if constraints shall be created for properties, else false.
	 */
	protected boolean constraintCreationForProperties = true;

	/**
	 * True, if xxxEncodingRule tagged values shall be ignored (because the
	 * model is wrong and needs cleanup), else false.
	 */
	protected boolean ignoreEncodingRuleTaggedValues = false;

	protected boolean useStringInterning = false;
	protected boolean loadGlobalIds = false;
	protected String language = "en";

	/**
	 * Set of class stereotypes for which constraints shall be created; null if
	 * constraints for all classes shall be created.
	 */
	protected HashSet<Integer> classTypesToCreateConstraintsFor = null;

	protected InputConfiguration inputConfig = null;
	protected Map<String, String> dialogParameters = null;
	protected Map<String, String> logParameters = null;
	protected ProcessConfiguration currentProcessConfig = null;
	protected List<TargetConfiguration> inputTargetConfigs = new ArrayList<TargetConfiguration>();
	protected List<TransformerConfiguration> inputTransformerConfigs = new ArrayList<TransformerConfiguration>();
	private String inputId = null;

	/**
	 * Flag used to determine if a reset is invoked during loading (in which
	 * case input as well as dialog and log parameters should be populated) or
	 * before executing a transformation / target process.
	 */
	private boolean resetUponLoadFlag = true;
	private List<TargetConfiguration> targetConfigs = null;

	protected File imageTmpDir = null;

	private Map<String, AIXMSchemaInfo> schemaInfos;

	/**
	 * @return True, if constraints shall be created for properties, else false.
	 */
	public boolean isConstraintCreationForProperties() {
		return this.constraintCreationForProperties;
	}

	/**
	 * Determines if AIXM schema are being processed, which require special
	 * treatment (due to the AIXM extension mechanism and because AIXM feature
	 * types are dynamic features).
	 *
	 * @return <code>true</code> if the input configuration element has
	 *         parameter 'isAIXM' with value 'true' (ignoring case), else
	 *         <code>false</code>.
	 */
	public boolean isAIXM() {
		return this.parameter("isAIXM") != null
				&& this.parameter("isAIXM").equalsIgnoreCase("true");
	}

	public boolean isLoadConstraintsForSelectedSchemasOnly() {

		return this
				.parameter(PARAM_LOAD_CONSTRAINT_FOR_SEL_SCHEMAS_ONLY) != null
				&& this.parameter(PARAM_LOAD_CONSTRAINT_FOR_SEL_SCHEMAS_ONLY)
						.equalsIgnoreCase("true");
	}

	/**
	 * Determines if only deferrable output writing shall be executed. This can
	 * be used to transform the temporary XML with feature catalogue information
	 * in a separate ShapeChange run. That run does no longer need to read the
	 * UML model and can thus be executed using 64bit Java, which supports
	 * bigger heap sizes that may be required to transform huge XML files.
	 *
	 * @return <code>true</code> if the input configuration element has
	 *         parameter {@value #PARAM_ONLY_DEFERRABLE_OUTPUT_WRITE} with value
	 *         'true' (ignoring case), else <code>false</code>.
	 */
	public boolean isOnlyDeferrableOutputWrite() {
		return this.parameter(PARAM_ONLY_DEFERRABLE_OUTPUT_WRITE) != null
				&& this.parameter(PARAM_ONLY_DEFERRABLE_OUTPUT_WRITE)
						.equalsIgnoreCase("true");
	}

	/**
	 * @return True, if xxxEncodingRule tagged values shall be ignored (because
	 *         the model is wrong and needs cleanup), else false.
	 */
	public boolean ignoreEncodingRuleTaggedValues() {
		return this.ignoreEncodingRuleTaggedValues;
	}

	public boolean isClassTypeToCreateConstraintsFor(int classCategory) {
		if (this.classTypesToCreateConstraintsFor == null)
			return true;
		else
			return this.classTypesToCreateConstraintsFor
					.contains(new Integer(classCategory));
	}

	/** A map entry. */
	protected void addTypeMapEntry(String k1, String k2, String s1, String s2) {
		fTypeMap.put(k1 + "#" + k2, new MapEntry(s1, s2));
	}

	protected void addTypeMapEntry(String k1, String k2, String s1, String s2,
			String s3) {
		fTypeMap.put(k1 + "#" + k2, new MapEntry(s1, s2, s3));
	}

	protected void addTypeMapEntry(String k1, String k2, String s1, String s2,
			String s3, String s4) {
		fTypeMap.put(k1 + "#" + k2, new MapEntry(s1, s2, s3, s4));
	}

	public MapEntry typeMapEntry(String k1, String k2) {
		String rule = k2;
		MapEntry me = null;
		while (me == null && rule != null) {
			me = fTypeMap.get(k1 + "#" + rule);
			rule = extendsEncRule(rule);
		}
		return me;
	}

	public void addTargetTypeMapEntry(String cls, String type, String rule,
			String ttype, String param) {
		HashMap<String, MapEntry> fclass = fTargetTypeMap.get(cls);
		if (fclass == null) {
			fclass = new HashMap<String, MapEntry>();
			fTargetTypeMap.put(cls, fclass);
		}
		fclass.put(type + "#" + rule, new MapEntry(rule, ttype, param));
	}

	public MapEntry targetTypeMapEntry(String cls, String type, String rule) {
		HashMap<String, MapEntry> fclass = fTargetTypeMap.get(cls);
		if (fclass == null)
			return null;
		MapEntry me = null;
		while (me == null && rule != null) {
			me = fclass.get(type + "#" + rule);
			rule = extendsEncRule(rule);
		}
		return me;
	}

	protected void addBaseMapEntry(String k1, String k2, String s1, String s2) {
		fBaseMap.put(k1 + "#" + k2, new MapEntry(s1, s2));
	}

	/**
	 * Adds a new MapEntry to fBaseMap.
	 * <p>
	 * The key is k1 + "#" + k2.
	 * <p>
	 * The value is a MapEntry with:
	 * <ul>
	 * <li>rule: s1</li>
	 * <li>p1: s2</li>
	 * <li>p2: s3</li>
	 * </ul>
	 *
	 * @param k1
	 *            type
	 * @param k2
	 *            xsdEncodingRule
	 * @param s1
	 *            ("direct")
	 * @param s2
	 *            xmlType
	 * @param s3
	 *            xmlTypeType+"/"+xmlTypeContent
	 */
	protected void addBaseMapEntry(String k1, String k2, String s1, String s2,
			String s3) {
		fBaseMap.put(k1 + "#" + k2, new MapEntry(s1, s2, s3));
	}

	/**
	 * Tries to find a MapEntry that defines the mapping of a type to its
	 * xmlType, based upon the given encoding rule or any rules it extends.
	 *
	 * @param k1
	 *            type name
	 * @param k2
	 *            encoding rule name
	 * @return MapEntry with rule=("direct"), p1=xmlType and (optionally, can be
	 *         null) p2=xmlTypeType+"/"+xmlTypeContent
	 */
	public MapEntry baseMapEntry(String k1, String k2) {
		String rule = k2;
		MapEntry me = null;
		while (me == null && rule != null) {
			me = fBaseMap.get(k1 + "#" + rule);
			rule = extendsEncRule(rule);
		}
		return me;
	}

	protected void addElementMapEntry(String k1, String k2, String s1,
			String s2) {
		fElementMap.put(k1 + "#" + k2, new MapEntry(s1, s2));
	}

	protected void addElementMapEntry(String k1, String k2, String s1,
			String s2, String s3) {
		fElementMap.put(k1 + "#" + k2, new MapEntry(s1, s2, s3));
	}

	public MapEntry elementMapEntry(String k1, String k2) {
		String rule = k2;
		MapEntry me = null;
		while (me == null && rule != null) {
			me = fElementMap.get(k1 + "#" + rule);
			rule = extendsEncRule(rule);
		}
		return me;
	}

	protected void addAttributeMapEntry(String k1, String k2, String s1) {
		fAttributeMap.put(k1 + "#" + k2, new MapEntry(s1));
	}

	public MapEntry attributeMapEntry(String k1, String k2) {
		String rule = k2;
		MapEntry me = null;
		while (me == null && rule != null) {
			me = fAttributeMap.get(k1 + "#" + rule);
			rule = extendsEncRule(rule);
		}
		return me;
	}

	protected void addAttributeGroupMapEntry(String k1, String k2, String s1) {
		fAttributeGroupMap.put(k1 + "#" + k2, new MapEntry(s1));
	}

	public MapEntry attributeGroupMapEntry(String k1, String k2) {
		String rule = k2;
		MapEntry me = null;
		while (me == null && rule != null) {
			me = fAttributeGroupMap.get(k1 + "#" + rule);
			rule = extendsEncRule(rule);
		}
		return me;
	}

	protected void addTarget(String k1, String k2) {
		fTargets.put(k1, k2);
	}

	public Vector<String> targets() {
		Vector<String> res = new Vector<String>();
		for (String t : fTargets.keySet()) {
			res.add(t);
		}
		return res;
	}

	public String targetMode(String tn) {
		if (tn == null)
			return "disabled";

		String s = fTargets.get(tn);
		if (s == null)
			return "disabled";

		return s;
	}

	public String setTargetMode(String tn, String mode) {
		return fTargets.put(tn, mode);
	}

	/**
	 * @param k1
	 * @return the value of the parameter with the given name, or
	 *         <code>null</code> if the parameter does not exist
	 */
	public String parameter(String k1) {
		return fParameters.get(k1);
	}

	public String parameter(String t, String k1) {
		return fParameters.get(t + "::" + k1);
	}

	/** This returns the names of all parms whose names match a regex pattern */
	public String[] parameterNamesByRegex(String t, String regex) {
		HashSet<String> pnames = new HashSet<String>();
		int lt2 = t.length() + 2;
		for (Entry<String, String> e : fParameters.entrySet()) {
			String key = e.getKey();
			if (key.startsWith(t + "::")) {
				if (Pattern.matches(regex, key.substring(lt2)))
					pnames.add(key.substring(lt2));
			}
		}
		return pnames.toArray(new String[0]);
	}

	public void setParameter(String k1, String s1) {
		String s = replaceValue(s1);
		if (s != null)
			fParameters.put(k1, s);
		else
			fParameters.put(k1, s1);
	}

	public void setParameter(String t, String k1, String s1) {
		String s = replaceValue(s1);
		if (s != null)
			fParameters.put(t + "::" + k1, s);
		else
			fParameters.put(t + "::" + k1, s1);
	}

	public String replaceValue(String k1) {
		return fReplace.get(k1);
	}

	public void setReplaceValue(String k1, String s1) {
		fReplace.put(k1, s1);
	}

	/**
	 * Adds a stereotype alias mapping.
	 *
	 * @param alias
	 *            - the stereotype alias (in lower case)
	 * @param wellknown
	 *            - the wellknown stereotype (in lower case) to which the alias
	 *            maps
	 */
	protected void addStereotypeAlias(String alias, String wellknown) {
		fStereotypeAliases.put(alias, wellknown);
	}

	/**
	 * Retrieves the wellknown stereotype to which the given alias maps, or
	 * <code>null</code> if no such mapping exists. The alias will automatically
	 * be converte to lower case to look up the mapping (the according key
	 * values in the stereotype map have also been converted to lower case).
	 *
	 * @param alias
	 *            stereotype for which a mapping to a wellknown stereotype is
	 *            being looked up
	 * @return the wellknown stereotype to which the alias maps, or
	 *         <code>null</code> if no such mapping exists
	 */
	public String stereotypeAlias(String alias) {
		return fStereotypeAliases.get(alias.toLowerCase());
	}

	/**
	 * Adds a tag alias mapping.
	 * 
	 * @param alias
	 *            - the tag alias (in lower case)
	 * @param wellknown
	 *            - the wellknown tag (in lower case) to which the alias
	 *            maps
	 */
	protected void addTagAlias(String alias, String wellknown) {
		fTagAliases.put(alias, wellknown);
	}

	/**
	 * Retrieves the wellknown tag to which the given alias maps, or
	 * <code>null</code> if no such mapping exists. The alias will automatically
	 * be converte to lower case to look up the mapping (the according key
	 * values in the tag map have also been converted to lower case).
	 * 
	 * @param alias
	 *            tag for which a mapping to a wellknown tag is
	 *            being looked up
	 * @return the wellknown tag to which the alias maps, or
	 *         <code>null</code> if no such mapping exists
	 */
	public String tagAlias(String alias) {
		return fTagAliases.get(alias.toLowerCase());
	}

	/**
	 * Adds a descriptor-source mapping.
	 * 
	 * @param descriptor
	 *            - the descriptor (in lower case)
	 * @param source
	 *            - the source (in lower case) 
	 */
	protected void addDescriptorSource(String descriptor, String source) {
		fDescriptorSources.put(descriptor, source);
	}

	/**
	 * Retrieves the wellknown stereotype to which the given alias maps, or
	 * <code>null</code> if no such mapping exists. The alias will automatically
	 * be converte to lower case to look up the mapping (the according key
	 * values in the stereotype map have also been converted to lower case).
	 * 
	 * @param descriptor
	 *            - the descriptor (in lower case)
	 * @return the source where the descriptor is represented in the model or
	 *         <code>null</code> if no such mapping exists
	 */
	public String descriptorSource(String descriptor) {
		return fDescriptorSources.get(descriptor.toLowerCase());
	}

	/**
	 * @param k1
	 *            namespace abbreviation / prefix
	 * @param s1
	 *            namespace
	 * @param s2
	 *            location
	 */
	protected void addNamespace(String k1, String s1, String s2) {
		fNamespaces.put(k1, new MapEntry(s1, s2));
	}

	protected void addRule(String rule) {
		fAllRules.add(rule.toLowerCase());
	}

	public boolean hasRule(String rule) {
		return fAllRules.contains(rule.toLowerCase());
	}

	protected void addRule(String rule, String encRule) {
		fRulesInEncRule.add(rule.toLowerCase() + "#" + encRule.toLowerCase());
	}

	public boolean hasRule(String rule, String encRule) {
		boolean res = false;
		while (!res && encRule != null) {
			res = fRulesInEncRule
					.contains(rule.toLowerCase() + "#" + encRule.toLowerCase());
			encRule = extendsEncRule(encRule);
		}
		return res;
	}

	public boolean matchesEncRule(String encRule, String baseRule) {
		while (encRule != null) {
			if (encRule.equalsIgnoreCase(baseRule))
				return true;
			encRule = extendsEncRule(encRule);
		}
		return false;
	}

	protected void addExtendsEncRule(String rule1, String rule2) {
		fExtendsEncRule.put(rule1.toLowerCase(), rule2.toLowerCase());
	}

	protected String extendsEncRule(String rule1) {
		return fExtendsEncRule.get(rule1.toLowerCase());
	}

	protected void addPackage(String k1, String s1, String s2, String s3,
			String s4) {
		fPackages.put(k1, new MapEntry(s1, s2, s3, s4));
	}

	/**
	 * @param k1
	 *            namespace
	 * @param s1
	 *            location
	 */
	public void addSchemaLocation(String k1, String s1) {
		// This will overwrite any previously existing value for the given key
		// k1. Order of schema location additions thus is important.
		fSchemaLocations.put(k1, s1);
	}

	/**
	 * @param k1
	 *            nsabr (namespace abbreviation / prefix)
	 * @return the MapEntry belonging to the nsabr (with namespace as 'rule' and
	 *         location as 'p1') - or <code>null</code> if the nsabr is unknown
	 */
	protected MapEntry namespace(String k1) {
		MapEntry me = fNamespaces.get(k1);
		return me;
	}

	/**
	 * Gets the namespace abbreviation / prefix for a given namespace as
	 * declared via the configuration, or <code>null</code> if the configuration
	 * does not contain information about the namespace.
	 *
	 * @param ns
	 * @return
	 */
	public String nsabrForNamespace(String ns) {

		for (String nsabr : fNamespaces.keySet()) {
			MapEntry me = fNamespaces.get(nsabr);
			if (me.rule.equals(ns)) {
				return nsabr;
			}
		}
		return null;
	}

	/**
	 * @param k1
	 *            nsabr (namespace abbreviation / prefix)
	 * @return the full namespace
	 */
	public String fullNamespace(String k1) {
		MapEntry me = fNamespaces.get(k1);
		if (me != null) {
			return me.rule;
		}
		return null;
	}

	public String nsOfPackage(String k1) {
		MapEntry me = fPackages.get(k1);
		if (me != null) {
			return me.rule;
		}
		return null;
	}

	public String nsabrOfPackage(String k1) {
		MapEntry me = fPackages.get(k1);
		if (me != null) {
			return me.p1;
		}
		return null;
	}

	public String xsdOfPackage(String k1) {
		MapEntry me = fPackages.get(k1);
		if (me != null) {
			return me.p2;
		}
		return null;
	}

	public String versionOfPackage(String k1) {
		MapEntry me = fPackages.get(k1);
		if (me != null) {
			return me.p3;
		}
		return null;
	}

	/**
	 * @param k1
	 *            - namespace
	 * @return schema location, if defined, else <code>null</code>
	 */
	public String schemaLocationOfNamespace(String k1) {
		String loc = fSchemaLocations.get(k1);
		/*
		 * note, schema location may be omitted / null; example: DGIWG spatial
		 * profile is not available online
		 */
		return loc;
	}

	public String nameOfTarget(int targetId) {

		for (TargetIdentification ti : TargetIdentification.values()) {

			if (ti.getId() == targetId) {
				return ti.getName();
			}
		}

		return "Unknown (" + targetId + ")";

		// switch (targetId) {
		// case 0:
		// return "-reserved-";
		// case 1:
		// return "XML Schema";
		// case 2:
		// return "-reserved-";
		// case 3:
		// return "RDF";
		// case 4:
		// return "Definitions";
		// case 5:
		// return "Excel Mapping";
		// case 6:
		// return "KML XSLT";
		// case 7:
		// return "JSON Schema";
		// case 8:
		// return "Code List Dictionary";
		// case 9:
		// return "Feature Catalogue";
		// case 10:
		// // FIXME shouldn't this be something like "SQL DDL"?
		// return "Decoder";
		// case 13:
		// return "Replication XML Schema";
		//
		// // FIXME hevan: it seems that not all targets in ShapeChange are in
		// this switch-statement
		//
		// case 401:
		// return "Objektartenkatalog";
		// case 402:
		// return "AAA-Profil (3AP)";
		// case 404:
		// return "AAA-Modellart (3AM)";
		// }
		//
		// return "Unknown (" + targetId + ")";
	}

	public void loadConfiguration() throws ShapeChangeAbortException {

		InputStream configStream = null;
		if (configFile == null) {
			// load minimal configuration, if no configuration file has been
			// provided
			configFile = "/config/minimal.xml";
			configStream = getClass().getResourceAsStream(configFile);
			if (configStream == null) {
				configFile = "src/main/resources" + configFile;
				File file = new File(configFile);
				if (file.exists())
					try {
						configStream = new FileInputStream(file);
					} catch (FileNotFoundException e1) {
						throw new ShapeChangeAbortException(
								"Minimal configuration file not found: "
										+ configFile);
					}
				else {
					URL url;
					String configURL = "http://shapechange.net/resources/config/minimal.xml";
					try {
						url = new URL(configURL);
						configStream = url.openStream();
					} catch (MalformedURLException e) {
						throw new ShapeChangeAbortException(
								"Minimal configuration file not accessible from: "
										+ configURL + " (malformed URL)");
					} catch (IOException e) {
						throw new ShapeChangeAbortException(
								"Minimal configuration file not accessible from: "
										+ configURL + " (IO error)");
					}
				}
			}
		} else {
			File file = new File(configFile);
			if (file == null || !file.exists()) {
				try {
					configStream = (new URL(configFile)).openStream();
				} catch (MalformedURLException e) {
					throw new ShapeChangeAbortException(
							"No configuration file found at " + configFile
									+ " (malformed URL)");
				} catch (IOException e) {
					throw new ShapeChangeAbortException(
							"No configuration file found at " + configFile
									+ " (IO exception)");
				}
			} else {
				try {
					configStream = new FileInputStream(file);
				} catch (FileNotFoundException e) {
					throw new ShapeChangeAbortException(
							"No configuration file found at " + configFile);
				}
			}
			if (configStream == null) {
				throw new ShapeChangeAbortException(
						"No configuration file found at " + configFile);
			}
		}

		DocumentBuilder builder = null;
		ShapeChangeErrorHandler handler = null;
		try {
			System.setProperty("javax.xml.parsers.DocumentBuilderFactory",
					"org.apache.xerces.jaxp.DocumentBuilderFactoryImpl");
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setNamespaceAware(true);
			factory.setValidating(true);
			factory.setFeature(
					"http://apache.org/xml/features/validation/schema", true);
			factory.setIgnoringElementContentWhitespace(true);
			factory.setIgnoringComments(true);
			factory.setXIncludeAware(true);
			factory.setFeature(
					"http://apache.org/xml/features/xinclude/fixup-base-uris",
					false);
			builder = factory.newDocumentBuilder();
			handler = new ShapeChangeErrorHandler();
			builder.setErrorHandler(handler);
		} catch (FactoryConfigurationError e) {
			throw new ShapeChangeAbortException(
					"Unable to get a document builder factory.");
		} catch (ParserConfigurationException e) {
			throw new ShapeChangeAbortException(
					"XML Parser was unable to be configured.");
		}

		// parse file
		try {
			Document document = builder.parse(configStream);
			if (handler.errorsFound()) {
				throw new ShapeChangeAbortException(
						"Invalid configuration file.");
			}

			// parse input element specific content
			NodeList nl = document.getElementsByTagName("input");
			Element inputElement = (Element) nl.item(0);
			if (inputElement.hasAttribute("id")) {
				inputId = inputElement.getAttribute("id").trim();
				if (inputId.length() == 0) {
					inputId = null;
				}
			} else {
				inputId = Options.INPUTELEMENTID;
			}

			Map<String, String> inputParameters = new HashMap<String, String>();
			nl = inputElement.getElementsByTagName("parameter");
			for (int j = 0; j < nl.getLength(); j++) {
				Element e = (Element) nl.item(j);
				String key = e.getAttribute("name");
				String val = e.getAttribute("value");
				inputParameters.put(key, val);
			}

			Map<String, String> stereotypeAliases = new HashMap<String, String>();
			nl = inputElement.getElementsByTagName("StereotypeAlias");
			for (int j = 0; j < nl.getLength(); j++) {
				Element e = (Element) nl.item(j);
				String key = e.getAttribute("alias");
				String val = e.getAttribute("wellknown");

				// case shall be ignored
				key = key.toLowerCase();
				val = val.toLowerCase();

				stereotypeAliases.put(key, val);
			}

			Map<String, String> tagAliases = new HashMap<String, String>();
			nl = inputElement.getElementsByTagName("TagAlias");
			for (int j = 0; j < nl.getLength(); j++) {
				Element e = (Element) nl.item(j);
				String key = e.getAttribute("alias");
				String val = e.getAttribute("wellknown");

				// case not to be ignored for tagged values at the moment
				// key = key.toLowerCase();
				// val = val.toLowerCase();

				tagAliases.put(key, val);
			}

			Map<String, String> descriptorSources = new HashMap<String, String>();
			nl = inputElement.getElementsByTagName("DescriptorSource");
			for (int j = 0; j < nl.getLength(); j++) {
				Element e = (Element) nl.item(j);
				String key = e.getAttribute("descriptor");
				String val = e.getAttribute("source");

				// case shall be ignored for descriptor and source
				key = key.toLowerCase();
				val = val.toLowerCase();
				
				if (val.equals("sc:extract")) {
					String s = e.getAttribute("token");
					val += "#"+(s==null ? "" : s);
				} else if (val.equals("tag")) {
					String s = e.getAttribute("tag");
					val += "#"+(s==null ? "" : s);
				}
				
				descriptorSources.put(key, val);
			}

			Map<String, PackageInfoConfiguration> packageInfos = parsePackageInfos(
					inputElement);

			this.inputConfig = new InputConfiguration(inputId, inputParameters,
					stereotypeAliases, tagAliases, descriptorSources, packageInfos);

			// parse dialog specific parameters
			nl = document.getElementsByTagName("dialog");
			if (nl != null && nl.getLength() != 0) {
				for (int k = 0; k < nl.getLength(); k++) {
					Node node = nl.item(k);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element dialogElement = (Element) node;
						this.dialogParameters = parseParameters(dialogElement,
								"parameter");
					}
				}
			}

			// parse log specific parameters
			nl = document.getElementsByTagName("log");
			if (nl != null && nl.getLength() != 0) {
				for (int k = 0; k < nl.getLength(); k++) {
					Node node = nl.item(k);
					if (node.getNodeType() == Node.ELEMENT_NODE) {
						Element logElement = (Element) node;
						this.logParameters = parseParameters(logElement,
								"parameter");
					}
				}
			}

			// add standard rules, just so that configured rules can be
			// validated
			addStandardRules();

			// Load transformer configurations (if any are provided in the
			// configuration file)
			Map<String, TransformerConfiguration> transformerConfigs = parseTransformerConfigurations(
					document);

			// Load target configurations
			this.targetConfigs = parseTargetConfigurations(document);

			this.resetFields();
			// this.resetUponLoadFlag = false;

			// TBD discuss if there's a better way to support rule and
			// requirements matching for the input model
			// TBD apparently the matching requires all
			// applicable encoding rules to be known up front
			for (TargetConfiguration tgtConfig : targetConfigs) {

				// System.out.println(tgtConfig);
				String className = tgtConfig.getClassName();
				String mode = tgtConfig.getProcessMode().name();

				// set targets and their mode; if a target occurs multiple
				// times, keep the enabled one(s)
				if (!this.fTargets.containsKey(className)) {
					addTarget(className, mode);

				} else {
					if (this.fTargets.get(className)
							.equals(ProcessMode.disabled)) {
						// set targets and their mode; if a target occurs
						// multiple times, keep the enabled one(s)
						addTarget(className, mode);
					}
				}

				// ensure that we have all the rules from all non-disabled
				// targets
				// we repeat this for the same target (if it is not disabled) to
				// ensure that we get the union of all encoding rules
				if (!tgtConfig.getProcessMode().equals(ProcessMode.disabled)) {
					for (ProcessRuleSet prs : tgtConfig.getRuleSets()
							.values()) {
						String nam = prs.getName();
						String ext = prs.getExtendedRuleSetName();
						addExtendsEncRule(nam, ext);

						if (prs.hasAdditionalRules()) {
							for (String rule : prs.getAdditionalRules()) {
								addRule(rule, nam);
							}
						}
					}
				}

				/*
				 * looks like we also need parameters like defaultEncodingRule
				 * !!! IF THERE ARE DIFFERENT DEFAULT ENCODING RULES FOR
				 * DIFFERENT TARGETS (WITH SAME CLASS) THIS WONT WORK!!!
				 */
				for (String paramName : tgtConfig.getParameters().keySet()) {
					setParameter(className, paramName,
							tgtConfig.getParameters().get(paramName));
				}

				// in order for the input model load not to produce warnings,
				// we also need to load the map entries
				if (tgtConfig instanceof TargetXmlSchemaConfiguration) {

					TargetXmlSchemaConfiguration config = (TargetXmlSchemaConfiguration) tgtConfig;

					// add xml schema namespace information
					for (XmlNamespace xns : config.getXmlNamespaces()) {
						addNamespace(xns.getNsabr(), xns.getNs(),
								xns.getLocation());
						// if (xns.getLocation() != null) {
						addSchemaLocation(xns.getNs(), xns.getLocation());
						// }
					}

					// add xsd map entries
					for (XsdMapEntry xsdme : config.getXsdMapEntries()) {

						for (String xsdEncodingRule : xsdme
								.getEncodingRules()) {

							String type = xsdme.getType();
							String xmlPropertyType = xsdme.getXmlPropertyType();
							String xmlElement = xsdme.getXmlElement();
							String xmlTypeContent = xsdme.getXmlTypeContent();
							String xmlTypeNilReason = xsdme
									.getXmlTypeNilReason();
							String xmlType = xsdme.getXmlType();
							String xmlTypeType = xsdme.getXmlTypeType();
							String xmlAttribute = xsdme.getXmlAttribute();
							String xmlAttributeGroup = xsdme
									.getXmlAttributeGroup();

							if (xmlPropertyType != null) {
								if (xmlPropertyType.equals("_P_")
										&& xmlElement != null) {
									addTypeMapEntry(type, xsdEncodingRule,
											"propertyType", xmlElement);
								} else if (xmlPropertyType.equals("_MP_")
										&& xmlElement != null) {
									addTypeMapEntry(type, xsdEncodingRule,
											"metadataPropertyType", xmlElement);
								} else {
									addTypeMapEntry(type, xsdEncodingRule,
											"direct", xmlPropertyType,
											xmlTypeType + "/" + xmlTypeContent,
											xmlTypeNilReason);
								}
							}
							if (xmlElement != null) {
								addElementMapEntry(type, xsdEncodingRule,
										"direct", xmlElement);
							}
							if (xmlType != null) {
								addBaseMapEntry(type, xsdEncodingRule, "direct",
										xmlType,
										xmlTypeType + "/" + xmlTypeContent);
							}
							if (xmlAttribute != null) {
								addAttributeMapEntry(type, xsdEncodingRule,
										xmlAttribute);
							}
							if (xmlAttributeGroup != null) {
								addAttributeGroupMapEntry(type, xsdEncodingRule,
										xmlAttributeGroup);
							}
						}
					}
				} else {

					// add map entries for Target (no need to do this for
					// transformers)
					for (ProcessMapEntry pme : tgtConfig.getMapEntries()) {
						addTargetTypeMapEntry(tgtConfig.getClassName(),
								pme.getType(), pme.getRule(),
								pme.getTargetType(), pme.getParam());
					}
				}
			}

			// create "tree"
			for (TargetConfiguration tgtConfig : targetConfigs) {
				for (String inputIdref : tgtConfig.getInputIds()) {
					if (inputIdref.equals(getInputId())) {
						this.inputTargetConfigs.add(tgtConfig);
					} else {
						transformerConfigs.get(inputIdref).addTarget(tgtConfig);
					}
				}
			}

			for (TransformerConfiguration trfConfig : transformerConfigs
					.values()) {
				String inputIdref = trfConfig.getInputId();
				if (inputIdref.equals(getInputId())) {
					this.inputTransformerConfigs.add(trfConfig);
				} else {
					transformerConfigs.get(inputIdref)
							.addTransformer(trfConfig);
				}
			}

			// Determine constraint creation handling parameters:
			String classTypesToCreateConstraintsFor = parameter(
					"classTypesToCreateConstraintsFor");
			if (classTypesToCreateConstraintsFor != null) {
				classTypesToCreateConstraintsFor = classTypesToCreateConstraintsFor
						.trim();
				if (classTypesToCreateConstraintsFor.length() > 0) {
					String[] stereotypes = classTypesToCreateConstraintsFor
							.split("\\W*,\\W*");
					this.classTypesToCreateConstraintsFor = new HashSet<Integer>();
					for (String stereotype : stereotypes) {
						String sForCons = stereotype.toLowerCase();
						if (sForCons.equals("enumeration")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(ENUMERATION));
						} else if (sForCons.equals("codelist")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(CODELIST));
						} else if (sForCons.equals("schluesseltabelle")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(OKSTRAKEY));
						} else if (sForCons.equals("fachid")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(OKSTRAFID));
						} else if (sForCons.equals("datatype")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(DATATYPE));
						} else if (sForCons.equals("union")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(UNION));
						} else if (sForCons.equals("featureconcept")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(FEATURECONCEPT));
						} else if (sForCons.equals("attributeconcept")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(ATTRIBUTECONCEPT));
						} else if (sForCons.equals("valueconcept")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(VALUECONCEPT));
						} else if (sForCons.equals("interface")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(MIXIN));
						} else if (sForCons.equals("basictype")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(BASICTYPE));
						} else if (sForCons.equals("adeelement")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(FEATURE));
						} else if (sForCons.equals("featuretype")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(FEATURE));
						} else if (sForCons.equals("type")) {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(OBJECT));
						} else {
							this.classTypesToCreateConstraintsFor
									.add(new Integer(UNKNOWN));
						}
					}
				}
			}

			String constraintCreationForProperties = parameter(
					"constraintCreationForProperties");
			if (constraintCreationForProperties != null) {
				if (constraintCreationForProperties.trim()
						.equalsIgnoreCase("false")) {
					this.constraintCreationForProperties = false;
				}
			}

			/*
			 * TODO add documentation
			 */
			String ignoreEncodingRuleTaggedValues = parameter(
					"ignoreEncodingRuleTaggedValues");

			if (ignoreEncodingRuleTaggedValues != null) {
				if (ignoreEncodingRuleTaggedValues.trim()
						.equalsIgnoreCase("true")) {
					this.ignoreEncodingRuleTaggedValues = true;
				}
			}

			String useStringInterning_value = parameter(
					PARAM_USE_STRING_INTERNING);

			if (useStringInterning_value != null && useStringInterning_value
					.trim().equalsIgnoreCase("true")) {
				this.useStringInterning = true;
			}

			String loadGlobalIds_value = this
					.parameter(PARAM_LOAD_GLOBAL_IDENTIFIERS);

			if (loadGlobalIds_value != null
					&& loadGlobalIds_value.trim().equalsIgnoreCase("true")) {
				this.loadGlobalIds = true;
			}

			String language_value = inputConfig.getParameters().get(PARAM_LANGUAGE);

			if (language_value != null && !language_value.trim().isEmpty()) {
				this.language = language_value.trim().toLowerCase();
			}

			
		} catch (SAXException e) {
			String m = e.getMessage();
			if (m != null) {
				throw new ShapeChangeAbortException(
						"Error while loading configuration file: "
								+ System.getProperty("line.separator") + m);
			} else {
				e.printStackTrace(System.err);
				throw new ShapeChangeAbortException(
						"Error while loading configuration file: "
								+ System.getProperty("line.separator")
								+ System.err);
			}
		} catch (IOException e) {
			String m = e.getMessage();
			if (m != null) {
				throw new ShapeChangeAbortException(
						"Error while loading configuration file: "
								+ System.getProperty("line.separator") + m);
			} else {
				e.printStackTrace(System.err);
				throw new ShapeChangeAbortException(
						"Error while loading configuration file: "
								+ System.getProperty("line.separator")
								+ System.err);
			}
		}

		MapEntry nsme = namespace("gml");
		if (nsme != null) {
			GML_NS = nsme.rule;
		}
	}

	/**
	 *
	 */
	public void resetFields() {

		// reset fields

		fTargets = new HashMap<String, String>();
		fParameters = new HashMap<String, String>();
		fTypeMap = new HashMap<String, MapEntry>();
		fBaseMap = new HashMap<String, MapEntry>();
		fElementMap = new HashMap<String, MapEntry>();
		fAttributeMap = new HashMap<String, MapEntry>();
		fAttributeGroupMap = new HashMap<String, MapEntry>();
		fTargetTypeMap = new HashMap<String, HashMap<String, MapEntry>>();
		fStereotypeAliases = new HashMap<String, String>();
		fTagAliases = new HashMap<String, String>();
		fDescriptorSources = new HashMap<String, String>();
		fNamespaces = new HashMap<String, MapEntry>();
		fPackages = new HashMap<String, MapEntry>();
		fSchemaLocations = new HashMap<String, String>();
		fAllRules = new HashSet<String>();
		fRulesInEncRule = new HashSet<String>();
		fExtendsEncRule = new HashMap<String, String>();

		// repopulate fields

		// set standard parameters first
		setStandardParameters();

		addStandardRules();

		if (resetUponLoadFlag) {
			// add all parameters from input
			for (String key : inputConfig.getParameters().keySet()) {
				setParameter(key, inputConfig.getParameters().get(key));
			}

			// add all stereotype aliases
			for (String key : inputConfig.getStereotypeAliases().keySet()) {
				addStereotypeAlias(key, inputConfig.getStereotypeAliases().get(key));
			}

			// add all tag aliases
			for (String key : inputConfig.getTagAliases().keySet()) {
				addTagAlias(key, inputConfig.getTagAliases().get(key));
			}

			// add all descriptor sources
			for (String key : inputConfig.getDescriptorSources().keySet()) {
				addDescriptorSource(key, inputConfig.getDescriptorSources().get(key));
			}

			// add all package infos
			for (PackageInfoConfiguration pic : inputConfig.getPackageInfos()
					.values()) {
				addPackage(pic.getPackageName(), pic.getNs(), pic.getNsabr(),
						pic.getXsdDocument(), pic.getVersion());
				// ensure that the package information is also added to the
				// schema location map
				addSchemaLocation(pic.getNs(), pic.getXsdDocument());
			}

			// add all dialog parameters
			if (dialogParameters != null) {
				for (String key : dialogParameters.keySet()) {
					setParameter(key, dialogParameters.get(key));
				}
			}

			// add all log parameters
			if (logParameters != null) {
				for (String key : logParameters.keySet()) {
					setParameter(key, logParameters.get(key));
				}
			}
		}

		if (currentProcessConfig != null) {

			this.gmlVersion = currentProcessConfig.getGmlVersion();

			// no need to set all the fields for transformers, because they
			// retrieve them directly

			// initialise fields for common targets and xml schema targets
			if (currentProcessConfig instanceof TargetConfiguration) {
				// add all parameters from current process configuration (only
				// required
				// for targets)
				for (String name : currentProcessConfig.getParameters()
						.keySet()) {
					setParameter(currentProcessConfig.getClassName(), name,
							currentProcessConfig.getParameters().get(name));
				}

				// add target mode
				addTarget(currentProcessConfig.getClassName(),
						currentProcessConfig.getProcessMode().name());

				// add encoding rules
				for (ProcessRuleSet prs : currentProcessConfig.getRuleSets()
						.values()) {
					String nam = prs.getName();
					String ext = prs.getExtendedRuleSetName();
					addExtendsEncRule(nam, ext);
					if (prs.hasAdditionalRules()) {
						for (String rule : prs.getAdditionalRules()) {
							addRule(rule, nam);
						}
					}
				}
			}

			if (currentProcessConfig.getClassName()
					.equals(TargetFOL2SchematronClass)
					&& currentProcessConfig.getParameters()
							.get("defaultEncodingRule") != null) {
				/*
				 * The execution of the FOL2Schematron target only generates
				 * Schematron, not XML Schema. We want the derivation of
				 * Schematron code to take into account the default encoding
				 * rule that we configured in the FOL2Schematron target. Because
				 * Schematron generation depends on how the XML Schema of model
				 * elements looks like, we must take the XSD encoding into
				 * account. We have a standard value for the XML Schema default
				 * encoding rule, and encoding rule checks for 'xsd' depend upon
				 * that specific parameter. However, the parameter is bound to
				 * the XMLSchema target class name. Thus if we have a
				 * defaultEncodingRule set in the FOL2Schematron target we must
				 * overwrite that parameter for the XML Schema target.
				 */
				this.setParameter(TargetXmlSchemaClass, "defaultEncodingRule",
						currentProcessConfig.getParameters()
								.get("defaultEncodingRule"));
			}

			if (currentProcessConfig instanceof TargetXmlSchemaConfiguration) {

				TargetXmlSchemaConfiguration config = (TargetXmlSchemaConfiguration) currentProcessConfig;

				// add xml schema namespace information
				for (XmlNamespace xns : config.getXmlNamespaces()) {
					addNamespace(xns.getNsabr(), xns.getNs(),
							xns.getLocation());
					// if (xns.getLocation() != null) {
					addSchemaLocation(xns.getNs(), xns.getLocation());
					// }
				}

				// add xsd map entries
				for (XsdMapEntry xsdme : config.getXsdMapEntries()) {

					for (String xsdEncodingRule : xsdme.getEncodingRules()) {

						String type = xsdme.getType();
						String xmlPropertyType = xsdme.getXmlPropertyType();
						String xmlElement = xsdme.getXmlElement();
						String xmlTypeContent = xsdme.getXmlTypeContent();
						String xmlTypeNilReason = xsdme.getXmlTypeNilReason();
						String xmlType = xsdme.getXmlType();
						String xmlTypeType = xsdme.getXmlTypeType();
						String xmlAttribute = xsdme.getXmlAttribute();
						String xmlAttributeGroup = xsdme.getXmlAttributeGroup();

						if (xmlPropertyType != null) {
							if (xmlPropertyType.equals("_P_")
									&& xmlElement != null) {
								addTypeMapEntry(type, xsdEncodingRule,
										"propertyType", xmlElement);
							} else if (xmlPropertyType.equals("_MP_")
									&& xmlElement != null) {
								addTypeMapEntry(type, xsdEncodingRule,
										"metadataPropertyType", xmlElement);
							} else {
								addTypeMapEntry(type, xsdEncodingRule, "direct",
										xmlPropertyType,
										xmlTypeType + "/" + xmlTypeContent,
										xmlTypeNilReason);
							}
						}
						if (xmlElement != null) {
							addElementMapEntry(type, xsdEncodingRule, "direct",
									xmlElement);
						}
						if (xmlType != null) {
							addBaseMapEntry(type, xsdEncodingRule, "direct",
									xmlType,
									xmlTypeType + "/" + xmlTypeContent);
						}
						if (xmlAttribute != null) {
							addAttributeMapEntry(type, xsdEncodingRule,
									xmlAttribute);
						}
						if (xmlAttributeGroup != null) {
							addAttributeGroupMapEntry(type, xsdEncodingRule,
									xmlAttributeGroup);
						}
					}
				}
			} else {

				// add map entries for Target (no need to do this for
				// transformers)
				for (ProcessMapEntry pme : currentProcessConfig
						.getMapEntries()) {
					addTargetTypeMapEntry(currentProcessConfig.getClassName(),
							pme.getType(), pme.getRule(), pme.getTargetType(),
							pme.getParam());
				}

				// TODO store namespace info
			}
		}

		MapEntry nsme = namespace("gml");
		if (nsme != null) {
			GML_NS = nsme.rule;
		}

	}

	private void setStandardParameters() {
		setParameter("reportLevel", "INFO");
		setParameter("xsltFile", "src/main/resources/xslt/result.xsl");
		setParameter("appSchemaName", "");
		setParameter("appSchemaNameRegex", "");
		setParameter("appSchemaNamespaceRegex", "");
		setParameter("publicOnly", "true");
		setParameter("inputFile",
				"http://shapechange.net/resources/test/test.xmi");
		setParameter("inputModelType", "XMI10");
		setParameter("logFile", "log.xml");
		setParameter("representTaggedValues", "");
		setParameter("addTaggedValues", "");
		setParameter("extractSeparator", "--IMPROBABLE--DUMMY--SEPARATOR--");
		setParameter("definitionSeparator", "-- Definition --");
		setParameter("descriptionSeparator", "-- Description --");
		setParameter("nameSeparator", "-- Name --");
		setParameter("outputDirectory", SystemUtils.getUserDir().getPath());
		setParameter("sortedSchemaOutput", "true");
		setParameter("sortedOutput", "true");
		setParameter("oclConstraintTypeRegex", "(OCL|Invariant)");
		setParameter("folConstraintTypeRegex", "(SBVR)");
		setParameter(Options.TargetXmlSchemaClass, "defaultEncodingRule",
				Options.ISO19136_2007);
		setParameter(Options.TargetXmlSchemaClass, "gmlVersion", "3.2");
		setParameter(Options.TargetOWLISO19150Class, "defaultEncodingRule",
				Options.ISO19150_2014);
		setParameter(Options.TargetSQLClass, "defaultEncodingRule",
				Options.SQL);
	}

	private Map<String, PackageInfoConfiguration> parsePackageInfos(
			Element inputElement) {

		Map<String, PackageInfoConfiguration> result = new HashMap<String, PackageInfoConfiguration>();

		NodeList nl = inputElement.getElementsByTagName("PackageInfo");

		for (int j = 0; j < nl.getLength(); j++) {

			Element e = (Element) nl.item(j);

			String name = e.getAttribute("packageName");
			String nsabr = e.getAttribute("nsabr");
			String ns = e.getAttribute("ns");
			String xsdDocument = e.getAttribute("xsdDocument");
			String version = e.getAttribute("version");

			PackageInfoConfiguration pic = new PackageInfoConfiguration(name,
					nsabr, ns, xsdDocument, version);

			result.put(name, pic);
		}

		return result;
	}

	private List<TargetConfiguration> parseTargetConfigurations(
			Document configurationDocument) throws ShapeChangeAbortException {

		List<TargetConfiguration> tgtConfigs = new ArrayList<TargetConfiguration>();

		NodeList tgtsNl = configurationDocument.getElementsByTagName("targets");

		for (int i = 0; i < tgtsNl.getLength(); i++) {
			Node tgtsN = tgtsNl.item(i);
			NodeList tgtNl = tgtsN.getChildNodes();

			// look for all Target/TargetXmlSchema elements in the "targets"
			// Node
			for (int j = 0; j < tgtNl.getLength(); j++) {
				Node tgtN = tgtNl.item(j);

				if (tgtN.getNodeType() == Node.ELEMENT_NODE) {
					Element tgtE = (Element) tgtN;

					// parse content of target element

					// get name of config element for decision which type of
					// TargetConfiguration to instantiate later on
					String tgtType = tgtE.getNodeName();

					// get class name
					String tgtConfigName = tgtE.getAttribute("class");

					// get mode
					ProcessMode tgtMode = this.parseMode(tgtE);

					Map<String, String> processParameters = parseParameters(
							tgtE, "targetParameter");

					// now look up all ProcessRuleSet elements, if there are
					// any
					Map<String, ProcessRuleSet> processRuleSets = parseRuleSets(
							tgtE, "EncodingRule", true);

					// get the target inputs - can be null, then set it to
					// global input element
					Set<String> tgtConfigInputs;
					if (tgtE.hasAttribute("inputs")) {
						String[] inputs = tgtE.getAttribute("inputs")
								.split("\\s");
						tgtConfigInputs = new HashSet<String>(
								Arrays.asList(inputs));
					} else {
						tgtConfigInputs = new HashSet<String>();
						tgtConfigInputs.add(getInputId());
					}

					Element advancedProcessConfigurations = parseAdvancedProcessConfigurations(
							tgtE);

					TargetConfiguration tgtConfig;
					if (tgtType.equals("Target")) {
						// now look up all ProcessMapEntry elements, if there
						// are any
						List<ProcessMapEntry> processMapEntries = parseProcessMapEntries(
								tgtE, "MapEntry");

						// also parse namespaces, if there are any
						List<Namespace> namespaces = parseNamespaces(tgtE);

						// create target config and add it to list
						tgtConfig = new TargetConfiguration(tgtConfigName,
								tgtMode, processParameters, processRuleSets,
								processMapEntries, tgtConfigInputs, namespaces,
								advancedProcessConfigurations);

					} else if (tgtType.equals("TargetOwl")) {

						// now look up all ProcessMapEntry elements, if there
						// are any
						List<ProcessMapEntry> processMapEntries = parseProcessMapEntries(
								tgtE, "MapEntry");

						// also parse namespaces, if there are any
						List<Namespace> namespaces = parseNamespaces(tgtE);

						// also parse stereotype mappings, if there are any
						Map<String, String> stereotypeMappings = parseStereotypeMappings(
								tgtE);

						// create target owl config and add it to list
						tgtConfig = new TargetOwlConfiguration(tgtConfigName,
								tgtMode, processParameters, processRuleSets,
								processMapEntries, tgtConfigInputs, namespaces,
								stereotypeMappings,
								advancedProcessConfigurations);
					} else {
						// We're dealing with a TargetXmlSchema element

						List<XsdMapEntry> xsdMapEntries = parseXsdMapEntries(
								tgtE);

						List<XmlNamespace> xmlNamespaces = parseXmlNamespaces(
								tgtE);

						tgtConfig = new TargetXmlSchemaConfiguration(
								tgtConfigName, tgtMode, processParameters,
								processRuleSets, null, xsdMapEntries,
								xmlNamespaces, tgtConfigInputs,
								advancedProcessConfigurations);
					}
					tgtConfigs.add(tgtConfig);
				}
			}
		}
		return tgtConfigs;
	}

	private Element parseAdvancedProcessConfigurations(Element processElement) {

		NodeList apcNl = processElement
				.getElementsByTagName("advancedProcessConfigurations");
		Node apcN;
		Element apcE;

		if (apcNl != null && apcNl.getLength() != 0) {

			for (int k = 0; k < apcNl.getLength(); k++) {

				apcN = apcNl.item(k);
				if (apcN.getNodeType() == Node.ELEMENT_NODE) {

					apcE = (Element) apcN;

					/*
					 * there can only be one 'advancedProcessConfigurations'
					 * element
					 */
					return apcE;
				}
			}
		}

		// no 'advancedProcessConfigurations' element found
		return null;
	}

	private Map<String, String> parseStereotypeMappings(Element targetElement) {

		Map<String, String> result = new HashMap<String, String>();

		NodeList smNl = targetElement.getElementsByTagName("StereotypeMapping");
		Node smN;
		Element smE;

		if (smNl != null && smNl.getLength() != 0) {

			for (int k = 0; k < smNl.getLength(); k++) {

				smN = smNl.item(k);
				if (smN.getNodeType() == Node.ELEMENT_NODE) {

					smE = (Element) smN;

					String wellknownStereotype = smE.getAttribute("wellknown");
					String mapsTo = smE.getAttribute("mapsTo");

					result.put(wellknownStereotype.toLowerCase(), mapsTo);
				}
			}
		}
		return result;
	}

	private List<Namespace> parseNamespaces(Element targetElement) {

		List<Namespace> result = new ArrayList<Namespace>();

		NodeList namespacesNl = targetElement.getElementsByTagName("Namespace");
		Node namespaceN;
		Element namespaceE;

		if (namespacesNl != null && namespacesNl.getLength() != 0) {

			for (int k = 0; k < namespacesNl.getLength(); k++) {

				namespaceN = namespacesNl.item(k);
				if (namespaceN.getNodeType() == Node.ELEMENT_NODE) {

					namespaceE = (Element) namespaceN;

					String nsabr = namespaceE.getAttribute("nsabr");
					String ns = namespaceE.getAttribute("ns");
					String location = namespaceE.hasAttribute("location")
							? location = namespaceE.getAttribute("location")
							: null;

					result.add(new Namespace(nsabr, ns, location));
				}
			}
		}
		return result;
	}

	private Map<String, String> parseParameters(Element processElement,
			String parameterElementTagName) throws ShapeChangeAbortException {

		Map<String, String> result = new HashMap<String, String>();
		NodeList parametersNl = processElement
				.getElementsByTagName(parameterElementTagName);
		Node parameterN;
		Element parameterE;

		if (parametersNl != null && parametersNl.getLength() != 0) {

			for (int k = 0; k < parametersNl.getLength(); k++) {

				parameterN = parametersNl.item(k);
				if (parameterN.getNodeType() == Node.ELEMENT_NODE) {

					parameterE = (Element) parameterN;

					result.put(parameterE.getAttribute("name"),
							parameterE.getAttribute("value"));
				}

			}
		}

		String s = result.get("gmlVersion");
		if (s != null) {
			if (s.equals("3.3") || s.equals("3.2") || s.equals("3.1")
					|| s.equals("2.1"))
				gmlVersion = s;
			else {
				throw new ShapeChangeAbortException(
						"Unknown value for gmlVersion: " + s);
			}
		}

		return result;
	}

	private List<XmlNamespace> parseXmlNamespaces(
			Element targetXmlSchemaElement) {

		List<XmlNamespace> result = new ArrayList<XmlNamespace>();

		NodeList xmlNamespacesNl = targetXmlSchemaElement
				.getElementsByTagName("XmlNamespace");
		Node xmlNamespaceN;
		Element xmlNamespaceE;

		if (xmlNamespacesNl != null && xmlNamespacesNl.getLength() != 0) {

			for (int k = 0; k < xmlNamespacesNl.getLength(); k++) {

				xmlNamespaceN = xmlNamespacesNl.item(k);
				if (xmlNamespaceN.getNodeType() == Node.ELEMENT_NODE) {

					xmlNamespaceE = (Element) xmlNamespaceN;

					String nsabr = xmlNamespaceE.getAttribute("nsabr");
					String ns = xmlNamespaceE.getAttribute("ns");
					String location = xmlNamespaceE.hasAttribute("location")
							? location = xmlNamespaceE.getAttribute("location")
							: null;
					String packageName = xmlNamespaceE
							.hasAttribute("packageName")
									? xmlNamespaceE.getAttribute("packageName")
									: null;

					result.add(
							new XmlNamespace(nsabr, ns, location, packageName));
				}
			}
		}
		return result;
	}

	/**
	 * Parses the given TargetXmlSchema element and returns a map of all
	 * XsdMapEntries it contains.
	 *
	 * @param targetXmlSchemaElement
	 *            The TargetXmlSchema element from the ShapeChangeConfiguration
	 * @return map of the XsdMapEntries contained in the TargetXmlSchema element
	 *         (key: XsdMapEntry type)
	 */
	private List<XsdMapEntry> parseXsdMapEntries(
			Element targetXmlSchemaElement) {

		List<XsdMapEntry> result = new ArrayList<XsdMapEntry>();

		NodeList xsdMapEntriesNl = targetXmlSchemaElement
				.getElementsByTagName("XsdMapEntry");
		Node xsdMapEntryN;
		Element xsdMapEntryE;

		if (xsdMapEntriesNl != null && xsdMapEntriesNl.getLength() != 0) {

			for (int k = 0; k < xsdMapEntriesNl.getLength(); k++) {

				xsdMapEntryN = xsdMapEntriesNl.item(k);
				if (xsdMapEntryN.getNodeType() == Node.ELEMENT_NODE) {

					xsdMapEntryE = (Element) xsdMapEntryN;

					String type = xsdMapEntryE.getAttribute("type");

					List<String> encodingRules = new ArrayList<String>();
					String encodingRulesValue = xsdMapEntryE
							.getAttribute("xsdEncodingRules");
					if (encodingRulesValue != null
							&& encodingRulesValue.trim().length() > 0) {
						encodingRulesValue = encodingRulesValue.toLowerCase();
						encodingRules = new ArrayList<String>(
								Arrays.asList(encodingRulesValue.split("\\s")));
					}

					String xmlType = xsdMapEntryE.hasAttribute("xmlType")
							? xsdMapEntryE.getAttribute("xmlType") : null;

					String xmlTypeContent = "";
					if (xsdMapEntryE.hasAttribute("xmlTypeContent")) {
						xmlTypeContent = xsdMapEntryE
								.getAttribute("xmlTypeContent").trim();
					}
					if (xmlTypeContent.length() == 0) {
						xmlTypeContent = "complex";
					}

					String xmlTypeType = "";
					if (xsdMapEntryE.hasAttribute("xmlTypeType")) {
						xmlTypeType = xsdMapEntryE.getAttribute("xmlTypeType")
								.trim();
					}
					if (xmlTypeType.length() == 0) {
						xmlTypeType = "complex";
					}

					String xmlTypeNilReason = "";
					if (xsdMapEntryE.hasAttribute("xmlTypeNilReason")) {
						xmlTypeNilReason = xsdMapEntryE
								.getAttribute("xmlTypeNilReason").trim();
					}
					if (xmlTypeNilReason.length() == 0) {
						if (xmlTypeType.equals("simple"))
							xmlTypeNilReason = "false";
						else
							xmlTypeNilReason = "true";
					}

					String xmlElement = xsdMapEntryE.hasAttribute("xmlElement")
							? xsdMapEntryE.getAttribute("xmlElement") : null;

					String xmlPropertyType = xsdMapEntryE
							.hasAttribute("xmlPropertyType") ? xsdMapEntryE
									.getAttribute("xmlPropertyType") : null;

					String xmlAttribute = xsdMapEntryE
							.hasAttribute("xmlAttribute")
									? xsdMapEntryE.getAttribute("xmlAttribute")
									: null;

					String xmlAttributeGroup = xsdMapEntryE
							.hasAttribute("xmlAttributeGroup") ? xsdMapEntryE
									.getAttribute("xmlAttributeGroup") : null;

					String nsabr = xsdMapEntryE.hasAttribute("nsabr")
							? xsdMapEntryE.getAttribute("nsabr") : null;

					result.add(new XsdMapEntry(type, encodingRules, xmlType,
							xmlTypeContent, xmlTypeType, xmlTypeNilReason,
							xmlElement, xmlPropertyType, xmlAttribute,
							xmlAttributeGroup, nsabr));
				}
			}
		}
		return result;
	}

	/**
	 * Parses the given process element and returns a map of all map entries it
	 * contains.
	 *
	 * @param processElement
	 *            A Transformer or Target element from the
	 *            ShapeChangeConfiguration
	 * @param string
	 * @return map of the map entries contained in the process element (key:
	 *         ProcessMapEntry type)
	 */
	private List<ProcessMapEntry> parseProcessMapEntries(Element processElement,
			String mapEntryElementTagName) {

		List<ProcessMapEntry> result = new ArrayList<ProcessMapEntry>();

		NodeList processMapEntriesNl = processElement
				.getElementsByTagName(mapEntryElementTagName);
		Node processMapEntryN;
		Element processMapEntryE;

		if (processMapEntriesNl != null
				&& processMapEntriesNl.getLength() != 0) {

			for (int k = 0; k < processMapEntriesNl.getLength(); k++) {

				processMapEntryN = processMapEntriesNl.item(k);
				if (processMapEntryN.getNodeType() == Node.ELEMENT_NODE) {

					processMapEntryE = (Element) processMapEntryN;

					String type = processMapEntryE.getAttribute("type");
					String rule = processMapEntryE.getAttribute("rule");
					String targetType = processMapEntryE
							.hasAttribute("targetType")
									? processMapEntryE
											.getAttribute("targetType")
									: null;
					String param = processMapEntryE.hasAttribute("param")
							? processMapEntryE.getAttribute("param") : null;

					result.add(
							new ProcessMapEntry(type, rule, targetType, param));
				}
			}
		}
		return result;
	}

	/**
	 * Parses all transformer configuration aspects available in the given
	 * ShapeChangeConfiguration document.
	 *
	 * @param configurationDocument
	 * @throws ShapeChangeAbortException
	 *
	 */
	private Map<String, TransformerConfiguration> parseTransformerConfigurations(
			Document configurationDocument) throws ShapeChangeAbortException {

		Map<String, TransformerConfiguration> trfConfigs = new HashMap<String, TransformerConfiguration>();

		NodeList trfsNl = configurationDocument
				.getElementsByTagName("transformers");

		for (int i = 0; i < trfsNl.getLength(); i++) {
			Node trfsN = trfsNl.item(i);
			NodeList trfNl = trfsN.getChildNodes();

			// look for all Transformer elements in the "transformers" Node
			for (int j = 0; j < trfNl.getLength(); j++) {
				Node trfN = trfNl.item(j);

				if (trfN.getNodeType() == Node.ELEMENT_NODE) {
					Element trfE = (Element) trfN;

					// parse content of Transformer element

					// get transformer id
					String trfConfigId = trfE.getAttribute("id");

					// get transformer class name
					String trfConfigName = trfE.getAttribute("class");

					// get transformer mode
					ProcessMode trfMode = parseMode(trfE);

					Map<String, String> processParameters = parseParameters(
							trfE, "ProcessParameter");

					// now look up all ProcessRuleSet elements, if there are
					// any
					Map<String, ProcessRuleSet> processRuleSets = parseRuleSets(
							trfE, "ProcessRuleSet", false);

					// now look up all ProcessMapEntry elements, if there
					// are any
					List<ProcessMapEntry> processMapEntries = parseProcessMapEntries(
							trfE, "ProcessMapEntry");

					// parse tagged values, if any are defined
					List<TaggedValueConfigurationEntry> taggedValues = parseTaggedValues(
							trfE);

					// get the transformer input - can be null, then set it to
					// global input element
					String trfConfigInput;
					if (trfE.hasAttribute("input")) {
						trfConfigInput = trfE.getAttribute("input");
					} else {
						trfConfigInput = getInputId();
					}

					if (trfConfigInput.equals(trfConfigId)) {
						throw new ShapeChangeAbortException(
								"Attributes input and id are equal in a transformer configuration element (class: "
										+ trfConfigName
										+ ") which is not allowed.");
					}

					Element advancedProcessConfigurations = parseAdvancedProcessConfigurations(
							trfE);

					// create transformer config and add it to list
					TransformerConfiguration trfConfig = new TransformerConfiguration(
							trfConfigId, trfConfigName, trfMode,
							processParameters, processRuleSets,
							processMapEntries, taggedValues, trfConfigInput,
							advancedProcessConfigurations);

					trfConfigs.put(trfConfig.getId(), trfConfig);
				}
			}
		}
		return trfConfigs;

	}

	/**
	 * @param trfE
	 *            "Transformer" element from the configuration
	 * @return list of tagged values defined for the transformer or
	 *         <code>null</code> if no tagged values are defined for it.
	 */
	private List<TaggedValueConfigurationEntry> parseTaggedValues(
			Element trfE) {

		List<TaggedValueConfigurationEntry> result = new ArrayList<TaggedValueConfigurationEntry>();

		Element directTaggedValuesInTrf = null;

		/*
		 * identify taggedValues element that is direct child of the Transformer
		 * element - can be null
		 */
		NodeList children = trfE.getChildNodes();
		if (children != null && children.getLength() != 0) {

			for (int k = 0; k < children.getLength(); k++) {

				Node n = children.item(k);
				if (n.getNodeType() == Node.ELEMENT_NODE
						&& n.getNodeName().equals("taggedValues")) {
					directTaggedValuesInTrf = (Element) n;
					break;
				}
			}
		}

		if (directTaggedValuesInTrf != null) {

			NodeList taggedValuesNl = directTaggedValuesInTrf
					.getElementsByTagName("TaggedValue");
			Node taggedValueN;
			Element taggedValueE;

			if (taggedValuesNl != null && taggedValuesNl.getLength() > 0) {

				for (int k = 0; k < taggedValuesNl.getLength(); k++) {

					taggedValueN = taggedValuesNl.item(k);
					if (taggedValueN.getNodeType() == Node.ELEMENT_NODE) {

						taggedValueE = (Element) taggedValueN;

						String value = null;

						if (taggedValueE.hasAttribute("value")) {
							value = taggedValueE.getAttribute("value");
						}

						Pattern modelElementNamePattern = null;

						if (taggedValueE.hasAttribute("modelElementName")) {
							String modelElementName = taggedValueE
									.getAttribute("modelElementName");
							modelElementNamePattern = Pattern
									.compile(modelElementName);
						}

						Pattern modelElementStereotypePattern = null;

						if (taggedValueE
								.hasAttribute("modelElementStereotype")) {

							String modelElementStereotype = taggedValueE
									.getAttribute("modelElementStereotype");
							modelElementStereotypePattern = Pattern
									.compile(modelElementStereotype);
						}

						Pattern applicationSchemaNamePattern = null;

						if (taggedValueE
								.hasAttribute("applicationSchemaName")) {

							String applicationSchemaName = taggedValueE
									.getAttribute("applicationSchemaName");
							applicationSchemaNamePattern = Pattern
									.compile(applicationSchemaName);
						}

						result.add(new TaggedValueConfigurationEntry(
								taggedValueE.getAttribute("name"), value,
								modelElementStereotypePattern,
								modelElementNamePattern,
								applicationSchemaNamePattern));

					}
				}
			}
		}

		if (result.isEmpty())
			return null;
		else
			return result;
	}

	private ProcessMode parseMode(Element processElement) {

		ProcessMode result = ProcessMode.enabled;

		if (processElement.hasAttribute("mode")) {
			String mode = processElement.getAttribute("mode");
			if (mode.equalsIgnoreCase("diagnostics-only")) {
				mode = "diagnosticsonly";
			}
			result = ProcessMode.fromString(mode);
		}

		return result;
	}

	private Map<String, ProcessRuleSet> parseRuleSets(Element processElement,
			String ruleSetElementTagName, boolean checkRuleExistence)
					throws ShapeChangeAbortException {

		Map<String, ProcessRuleSet> result = new HashMap<String, ProcessRuleSet>();

		NodeList processRuleSetsNl = processElement
				.getElementsByTagName(ruleSetElementTagName);
		Node processRuleSetN;
		Element processRuleSetE;
		Set<String> ruleSetRules;

		if (processRuleSetsNl != null && processRuleSetsNl.getLength() != 0) {

			for (int k = 0; k < processRuleSetsNl.getLength(); k++) {

				processRuleSetN = processRuleSetsNl.item(k);

				if (processRuleSetN.getNodeType() == Node.ELEMENT_NODE) {

					// parse rule set
					processRuleSetE = (Element) processRuleSetN;

					// parse name and extends
					String processRuleSetName = processRuleSetE
							.getAttribute("name");
					// String processRuleSetName = processRuleSetE
					// .getAttribute("name").toLowerCase();
					String extendedRuleName = processRuleSetE
							.getAttribute("extends");
					if (extendedRuleName != null
							&& extendedRuleName.trim().length() == 0) {
						extendedRuleName = "*";
					}
					// extendedRuleName = extendedRuleName.toLowerCase();

					// parse additional rules for rule set, if
					// there are any
					ruleSetRules = null;
					NodeList processRuleSetRulesNl = processRuleSetE
							.getElementsByTagName("rule");
					if (processRuleSetRulesNl != null
							&& processRuleSetRulesNl.getLength() != 0) {

						for (int l = 0; l < processRuleSetRulesNl
								.getLength(); l++) {
							Node processRuleSetRuleN = processRuleSetRulesNl
									.item(l);
							if (processRuleSetRuleN
									.getNodeType() == Node.ELEMENT_NODE) {
								Element processRuleSetRuleE = (Element) processRuleSetRuleN;
								if (ruleSetRules == null)
									ruleSetRules = new HashSet<String>();
								String ruleName = processRuleSetRuleE
										.getAttribute("name");

								// we do not need to check rules for
								// transformers here
								if (checkRuleExistence) {
									if (hasRule(ruleName))
										ruleSetRules.add(ruleName);
									else
										System.err.println(
												"Warning while loading configuration file: Rule '"
														+ ruleName
														+ "' is unknown, but referenced from the configuration. This is only a problem, if "
														+ "you know that the conversion rule is used in an encoding rule of your configuration.");
								} else {
									ruleSetRules.add(ruleName);
								}
							}
						}
					}

					result.put(processRuleSetName,
							new ProcessRuleSet(processRuleSetName,
									extendedRuleName, ruleSetRules));

				}
			}
		}
		return result;
	}

	private void addStandardRules() {
		/*
		 * mandatory rules
		 */
		addRule("req-xsd-pkg-xsdDocument-unique");
		addRule("req-xsd-cls-name-unique");
		addRule("req-xsd-cls-ncname");
		addRule("req-xsd-prop-data-type");
		addRule("req-xsd-prop-value-type-exists");
		addRule("req-xsd-prop-ncname");
		addRule("rule-xsd-pkg-contained-packages");
		addRule("rule-xsd-pkg-dependencies");
		addRule("rule-xsd-cls-union-as-choice");
		addRule("rule-xsd-cls-unknown-as-object");
		addRule("rule-xsd-cls-sequence");
		addRule("rule-xsd-cls-object-element");
		addRule("rule-xsd-cls-type");
		addRule("rule-xsd-cls-property-type");
		addRule("rule-xsd-cls-local-properties");
		/*
		 * Associate these with a core encoding rule
		 */
		addRule("req-xsd-pkg-xsdDocument-unique", "*");
		addRule("req-xsd-cls-name-unique", "*");
		addRule("req-xsd-cls-ncname", "*");
		addRule("req-xsd-prop-data-type", "*");
		addRule("req-xsd-prop-value-type-exists", "*");
		addRule("req-xsd-prop-ncname", "*");
		addRule("rule-xsd-pkg-contained-packages", "*");
		addRule("rule-xsd-pkg-dependencies", "*");
		addRule("rule-xsd-cls-unknown-as-object", "*");
		addRule("rule-xsd-cls-object-element", "*");
		addRule("rule-xsd-cls-type", "*");
		addRule("rule-xsd-cls-property-type", "*");
		addRule("rule-xsd-cls-local-properties", "*");
		addRule("rule-xsd-cls-union-as-choice", "*");
		addRule("rule-xsd-cls-sequence", "*");
		/*
		 * GML 3.2 / ISO 19136:2007 rules
		 */
		addRule("req-xsd-cls-generalization-consistent");
		addRule("rule-xsd-all-naming-gml");
		addRule("rule-xsd-cls-global-enumeration");
		addRule("rule-xsd-cls-codelist-asDictionary");
		addRule("rule-xsd-cls-noPropertyType");
		addRule("rule-xsd-cls-byValuePropertyType");
		addRule("rule-xsd-cls-standard-gml-property-types");
		addRule("rule-xsd-pkg-gmlProfileSchema");
		addRule("rule-xsd-prop-defaultCodeSpace");
		addRule("rule-xsd-prop-inlineOrByReference");
		addRule("rule-xsd-prop-reverseProperty");
		addRule("rule-xsd-prop-targetElement");
		/*
		 * add the iso19136_2007 encoding rule and extend the core encoding rule
		 */
		addExtendsEncRule("iso19136_2007", "*");
		addRule("req-xsd-cls-generalization-consistent", "iso19136_2007");
		addRule("rule-xsd-all-naming-gml", "iso19136_2007");
		addRule("rule-xsd-cls-global-enumeration", "iso19136_2007");
		addRule("rule-xsd-cls-codelist-asDictionary", "iso19136_2007");
		addRule("rule-xsd-cls-standard-gml-property-types", "iso19136_2007");
		addRule("rule-xsd-cls-noPropertyType", "iso19136_2007");
		addRule("rule-xsd-cls-byValuePropertyType", "iso19136_2007");
		addRule("rule-xsd-pkg-gmlProfileSchema", "iso19136_2007");
		addRule("rule-xsd-prop-targetElement", "iso19136_2007");
		addRule("rule-xsd-prop-reverseProperty", "iso19136_2007");
		addRule("rule-xsd-prop-defaultCodeSpace", "iso19136_2007");
		addRule("rule-xsd-prop-inlineOrByReference", "iso19136_2007");
		/*
		 * additional GML 3.3 rules
		 */
		addRule("rule-xsd-cls-codelist-asDictionaryGml33");
		addRule("rule-xsd-rel-association-classes");
		/*
		 * add the gml33 encoding rule and extend the core encoding rule
		 */
		addExtendsEncRule("gml33", "*");
		addRule("req-xsd-cls-generalization-consistent", "gml33");
		addRule("rule-xsd-all-naming-gml", "gml33");
		addRule("rule-xsd-cls-global-enumeration", "gml33");
		addRule("rule-xsd-cls-codelist-asDictionaryGml33", "gml33");
		addRule("rule-xsd-cls-standard-gml-property-types", "gml33");
		addRule("rule-xsd-cls-noPropertyType", "gml33");
		addRule("rule-xsd-cls-byValuePropertyType", "gml33");
		addRule("rule-xsd-pkg-gmlProfileSchema", "gml33");
		addRule("rule-xsd-prop-targetElement", "gml33");
		addRule("rule-xsd-prop-reverseProperty", "gml33");
		addRule("rule-xsd-prop-defaultCodeSpace", "gml33");
		addRule("rule-xsd-prop-inlineOrByReference", "gml33");
		addRule("rule-xsd-rel-association-classes", "gml33");
		/*
		 * ISO/TS 19139:2007 rules
		 */
		addRule("rule-xsd-all-naming-19139");
		addRule("rule-xsd-cls-standard-19139-property-types");
		addRule("rule-xsd-cls-enum-object-element");
		addRule("rule-xsd-cls-enum-property-type");
		/*
		 * add the iso19139_2007 encoding rule and extend the core encoding rule
		 */
		addExtendsEncRule("iso19139_2007", "*");
		addRule("rule-xsd-cls-enum-object-element", "iso19139_2007");
		addRule("rule-xsd-cls-enum-property-type", "iso19139_2007");
		addRule("rule-xsd-cls-global-enumeration", "iso19139_2007");
		addRule("rule-xsd-cls-standard-19139-property-types", "iso19139_2007");
		addRule("rule-xsd-all-naming-19139", "iso19139_2007");
		/*
		 * SWE Common Data Model 2.0 rules
		 */
		addRule("rule-xsd-all-naming-swe");
		addRule("rule-xsd-prop-xsdAsAttribute");
		addRule("rule-xsd-prop-soft-typed");
		addRule("rule-xsd-cls-union-as-group-property-type");
		addRule("rule-xsd-cls-standard-swe-property-types");
		addRule("rule-xsd-prop-initialValue");
		/*
		 * add the ogcSweCommon2 encoding rule and extend the core encoding rule
		 */
		addExtendsEncRule("ogcSweCommon2", "*");
		addRule("req-xsd-cls-generalization-consistent", "ogcSweCommon2");
		addRule("rule-xsd-all-naming-swe", "ogcSweCommon2");
		addRule("rule-xsd-cls-global-enumeration", "ogcSweCommon2");
		addRule("rule-xsd-cls-codelist-asDictionary", "ogcSweCommon2");
		addRule("rule-xsd-cls-standard-swe-property-types", "ogcSweCommon2");
		addRule("rule-xsd-cls-noPropertyType", "ogcSweCommon2");
		addRule("rule-xsd-cls-byValuePropertyType", "ogcSweCommon2");
		addRule("rule-xsd-pkg-gmlProfileSchema", "ogcSweCommon2");
		addRule("rule-xsd-prop-targetElement", "ogcSweCommon2");
		addRule("rule-xsd-prop-reverseProperty", "ogcSweCommon2");
		addRule("rule-xsd-prop-defaultCodeSpace", "ogcSweCommon2");
		addRule("rule-xsd-prop-inlineOrByReference", "ogcSweCommon2");
		addRule("rule-xsd-prop-xsdAsAttribute", "ogcSweCommon2");
		addRule("rule-xsd-prop-soft-typed", "ogcSweCommon2");
		addRule("rule-xsd-cls-union-as-group-property-type", "ogcSweCommon2");
		addRule("rule-xsd-prop-initialValue", "ogcSweCommon2");

		/*
		 * additional GML 2.1 rules
		 */
		addRule("rule-xsd-all-gml21");
		addRule("rule-xsd-cls-codelist-anonymous-xlink");
		/*
		 * add the gml21 encoding rule and extend the core encoding rule
		 */
		addExtendsEncRule("gml21", "iso19136_2007");
		addRule("rule-xsd-all-gml21", "gml21");
		addRule("rule-xsd-cls-codelist-anonymous-xlink", "gml21");

		/*
		 * addExtendsEncRule("gsip","*");
		 * addRule("req-xsd-cls-mixin-supertypes","gsip");
		 * addRule("req-xsd-cls-codelist-no-supertypes","gsip");
		 * addRule("rule-xsd-cls-union-asCharacterString","gsip");
		 * addRule("rule-xsd-cls-union-asGroup","gsip");
		 * addRule("rule-xsd-cls-enum-supertypes","gsip");
		 * addRule("rule-xsd-cls-enum-subtypes","gsip");
		 * addRule("rule-xsd-cls-basictype","gsip");
		 * addRule("rule-xsd-cls-union-direct","gsip");
		 * addRule("rule-xsd-cls-codelist-constraints","gsip");
		 * addRule("rule-xsd-cls-mixin-classes-as-group","gsip");
		 * addRule("rule-xsd-cls-mixin-classes","gsip");
		 * addRule("rule-xsd-prop-exclude-derived","gsip");
		 * addRule("rule-xsd-prop-length-size-pattern","gsip");
		 * addRule("rule-xsd-prop-xsdAsAttribute","gsip");
		 * addRule("rule-xsd-prop-nillable","gsip");
		 * addRule("rule-xsd-prop-nilReasonAllowed","gsip");
		 * addRule("rule-xsd-prop-initialValue","gsip");
		 * addRule("rule-xsd-prop-att-map-entry","gsip");
		 * addRule("rule-xsd-pkg-schematron","gsip");
		 * addRule("rule-xsd-all-tagged-values","gsip");
		 */

		/*
		 * non-standard extensions - requirements
		 */
		addRule("req-all-all-documentation");
		addRule("req-all-prop-sequenceNumber");
		addRule("req-xsd-pkg-targetNamespace");
		addRule("req-xsd-pkg-xmlns");
		addRule("req-xsd-pkg-namespace-schema-only");
		addRule("rec-xsd-pkg-version");
		addRule("req-xsd-pkg-xsdDocument");
		addRule("req-xsd-pkg-dependencies");
		addRule("req-xsd-cls-codelist-asDictionary-true");
		addRule("req-xsd-cls-codelist-extensibility-values");
		addRule("req-xsd-cls-codelist-extensibility-vocabulary");
		addRule("req-xsd-cls-codelist-no-supertypes");
		addRule("req-xsd-cls-datatype-noPropertyType");
		addRule("req-xsd-cls-enum-no-supertypes");
		addRule("req-xsd-cls-mixin-supertypes");
		addRule("req-xsd-cls-mixin-supertypes-overrule");
		addRule("req-xsd-cls-objecttype-byValuePropertyType");
		addRule("req-xsd-cls-objecttype-noPropertyType");
		addRule("req-xsd-cls-suppress-no-properties");
		addRule("req-xsd-cls-suppress-subtype");
		addRule("req-xsd-cls-suppress-supertype");
		addRule("req-xsd-prop-codelist-obligation");
		/*
		 * non-standard extensions - conversion rules
		 */
		addRule("rule-xsd-all-notEncoded");
		addRule("rule-xsd-cls-adeelement");
		addRule("rule-xsd-cls-basictype");
		addRule("rule-xsd-cls-codelist-constraints");
		addRule("rule-xsd-cls-enum-subtypes");
		addRule("rule-xsd-cls-enum-supertypes");
		addRule("rule-xsd-cls-mixin-classes-as-group");
		addRule("rule-xsd-cls-mixin-classes");
		addRule("rule-xsd-cls-mixin-classes-non-mixin-supertypes");
		addRule("rule-xsd-cls-no-abstract-classes");
		addRule("rule-xsd-cls-no-base-class");
		addRule("rule-xsd-cls-no-gml-types");
		addRule("rule-xsd-cls-okstra-fid");
		addRule("rule-xsd-cls-okstra-lifecycle");
		addRule("rule-xsd-cls-okstra-schluesseltabelle");
		addRule("rule-xsd-cls-suppress");
		addRule("rule-xsd-cls-union-asCharacterString");
		addRule("rule-xsd-cls-union-asGroup");
		addRule("rule-xsd-cls-union-direct");
		addRule("rule-xsd-cls-union-direct-optionality");
		addRule("rule-xsd-prop-att-map-entry");
		addRule("rule-xsd-prop-exclude-derived");
		addRule("rule-xsd-prop-length-size-pattern");
		addRule("rule-xsd-prop-nillable");
		addRule("rule-xsd-prop-nilReasonAllowed");
		addRule("rule-xsd-prop-gmlArrayProperty");
		addRule("rule-xsd-prop-gmlListProperty");
		addRule("rule-xsd-prop-qualified-associations");
		addRule("rule-xsd-all-no-documentation");
		addRule("rule-xsd-cls-local-enumeration");
		addRule("rule-xsd-cls-local-basictype");
		addRule("rule-xsd-pkg-dgiwgsp");
		addRule("rule-xsd-pkg-schematron");
		addRule("rule-xsd-all-tagged-values");
		addRule("rule-xsd-cls-adehook");

		// AIXM specific rules
		addRule("rule-all-cls-aixmDatatype");
		// further rules of a more general nature
		addRule("rule-all-prop-uomAsAttribute");

		/*
		 * JSON encoding rules
		 */
		addExtendsEncRule("geoservices", "*");
		addExtendsEncRule("geoservices_extended", "*");

		/*
		 * RDF encoding rules
		 */
		addRule("rule-rdf-prop-parent");

		/*
		 * SQL encoding rules
		 */

		addRule("rule-sql-cls-feature-types");
		addRule("rule-sql-cls-object-types");
		addRule("rule-sql-cls-references-to-external-types");
		addRule("rule-sql-all-associativetables");
		addRule("rule-sql-prop-exclude-derived");
		addRule("rule-sql-cls-data-types");
		addRule("rule-sql-prop-check-constraints-for-enumerations");

		// declare rule sets
		addExtendsEncRule(SQL, "*");
		addRule("rule-sql-cls-feature-types", SQL);

		/*
		 * OWL encoding rules
		 */
		addExtendsEncRule("iso19150_2014", "*");

		// declare optional rules
		addRule("rule-owl-pkg-singleOntologyPerSchema");
		addRule("rule-owl-pkg-pathInOntologyName");
		addRule("rule-owl-all-constraints");
		addRule("rule-owl-cls-geosparql-features");
		addRule("rule-owl-cls-19150-2-features");
		addRule("rule-owl-cls-codelist-external");

		addRule("rule-owl-prop-suppress-cardinality-restrictions");
		addRule("rule-owl-prop-suppress-allValuesFrom-restrictions");
		addRule("rule-owl-prop-voidable-as-minCardinality0");
		addRule("rule-owl-all-suppress-dc-source");
		addRule("rule-owl-prop-suppress-asociation-names");
		addRule("rule-owl-pkg-app-schema-code");

		/*
		 * ArcGIS workspace encoding rules
		 */
		addRule("rule-arcgis-prop-initialValueByAlias");
		/*
		 * extend core encoding rule with optional ArcGIS workspace rules
		 */
		addRule("rule-arcgis-prop-initialValueByAlias", "*");

		/*
		 * Replication schema encoding rules
		 */
		addRule("rule-rep-prop-optional");
		addRule("rule-rep-prop-exclude-derived");
		addRule("rule-rep-cls-generate-objectidentifier");
		addRule("rule-rep-prop-maxLength-from-size");
		
		/*
		 * Application schema metadata rules
		 */
		addRule("rule-asm-all-identify-profiles");

		// // ==================
		// // Transformer rules
		// // ==================
		//
		// /*
		// * Flattener rules
		// */
		// addRule("rule-trf-all-removeType");
		// addRule("rule-trf-prop-flatten-ONINAs");
		// addRule("rule-trf-prop-optionality");
		// addRule("rule-trf-cls-flatten-inheritance");
		// addRule("rule-trf-prop-flatten-multiplicity");
		// addRule("rule-trf-prop-flatten-types");
		// addRule("rule-trf-all-flatten-name");
		// addRule("rule-trf-all-flatten-constraints");
		// addRule("rule-trf-prop-remove-name-and-alias-component");
		// addRule("rule-trf-prop-flatten-homogeneousgeometries");
		//
		// /*
		// * Profiler rules
		// */
		// addRule("rule-trf-profiling-preprocessing-profilesValueConsistencyCheck");
		// addRule("rule-trf-profiling-preprocessing-modelConsistencyCheck");
		// addRule("rule-trf-profiling-postprocessing-removeEmptyPackages");

	}

	/** Normalize a stereotype fetched from the model. */
	public String normalizeStereotype(String stereotype) {
		// Map stereotype alias to well-known stereotype
		String s = stereotypeAlias(stereotype.trim());
		if (s != null)
			return s;
		return stereotype;
	};

	/** Normalize a tag fetched from the model. */
	public String normalizeTag(String tag) {
		// Map tag alias to well-known tag
		String s = tagAlias(tag.trim());
		if (s != null)
			return s;
		return tag;
	};

	/*
	 * Only process schemas in a namespace and name that matches a user-selected
	 * pattern
	 */
	public boolean skipSchema(Target target, PackageInfo pi) {
		String name = pi.name();
		String ns = pi.targetNamespace();

		// only process schemas with a given name
		String schemaFilter;
		if (target == null)
			schemaFilter = parameter("appSchemaName");
		else
			schemaFilter = parameter(target.getClass().getName(),
					"appSchemaName");
		if (schemaFilter != null && schemaFilter.length() > 0
				&& !schemaFilter.equals(name))
			return true;

		// only process schemas with a name that matches a user-selected pattern
		String appSchemaNameRegex;
		if (target == null)
			appSchemaNameRegex = parameter("appSchemaNameRegex");
		else
			appSchemaNameRegex = parameter(target.getClass().getName(),
					"appSchemaNameRegex");
		if (appSchemaNameRegex != null && appSchemaNameRegex != null
				&& appSchemaNameRegex.length() > 0
				&& !name.matches(appSchemaNameRegex))
			return true;

		// only process schemas in a namespace that matches a user-selected
		// pattern
		String appSchemaNamespaceRegex;
		if (target == null)
			appSchemaNamespaceRegex = parameter("appSchemaNamespaceRegex");
		else
			appSchemaNamespaceRegex = parameter(target.getClass().getName(),
					"appSchemaNamespaceRegex");
		if (appSchemaNamespaceRegex != null
				&& appSchemaNamespaceRegex.length() > 0
				&& !ns.matches(appSchemaNamespaceRegex))
			return true;

		return false;
	}

	public String targetClassName(String rule) {
		String[] ra = rule.toLowerCase().split("-", 4);
		if (ra.length != 4)
			return null;

		if (ra[1].equals("xsd"))
			return Options.TargetXmlSchemaClass;
		else if (ra[1].equals("json"))
			return Options.TargetJsonSchemaClass;
		else if (ra[1].equals("fc"))
			return Options.TargetFeatureCatalogueClass;
		else if (ra[1].equals("rdf"))
			return Options.TargetRDFClass;
		else if (ra[1].equals("sql"))
			return Options.TargetSQLClass;
		else if (ra[1].equals("owl"))
			return Options.TargetOWLISO19150Class;
		else if (ra[1].equals("arcgis"))
			return Options.TargetArcGISWorkspaceClass;
		else if (ra[1].equals("rep"))
			return Options.TargetReplicationSchemaClass;
		else if (ra[1].equals("asm"))
			return Options.TargetApplicationSchemaMetadata;

		return null;
	}

	public Options() {
		setStandardParameters();
	}

	/**
	 * @return the inputTargetConfigs
	 */
	public List<TargetConfiguration> getInputTargetConfigs() {
		return inputTargetConfigs;
	}

	/**
	 * @return the inputTransformerConfigs
	 */
	public List<TransformerConfiguration> getInputTransformerConfigs() {
		return inputTransformerConfigs;
	}

	/**
	 * @param currentProcessConfig
	 *            the currentProcessConfig to set
	 */
	public void setCurrentProcessConfig(
			ProcessConfiguration currentProcessConfig) {
		this.currentProcessConfig = currentProcessConfig;
	}

	/**
	 * @return the currentProcessConfig
	 */
	public ProcessConfiguration getCurrentProcessConfig() {
		return currentProcessConfig;
	}

	public List<TargetConfiguration> getTargetConfigurations() {
		return this.targetConfigs;
	}

	/**
	 * @return the value of the 'id' attribute of the 'input' configuration
	 *         element (or the default value as defined by the
	 *         {@link #INPUTELEMENTID} field), if the attribute was not set in
	 *         the configuration.
	 */
	public String getInputId() {
		return inputId;
	}

	/**
	 * @return the temporary directory for the ShapeChange run; will be created
	 *         if it does not already exist
	 */
	public File getTmpDir() {

		if (tmpDir == null) {

			String tmpDirPath = this.parameter(TMP_DIR_PATH_PARAM);
			if (tmpDirPath == null) {
				tmpDirPath = DEFAULT_TMP_DIR_PATH;
			}

			tmpDir = new File(tmpDirPath);
		}

		if (!tmpDir.exists()) {
			try {
				FileUtils.forceMkdir(tmpDir);
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}

		return tmpDir;
	}

	/**
	 * @return the directory in which images (i.e. diagrams) from the input
	 *         model can be stored; if it did not exist yet, it will be created
	 */
	public File imageTmpDir() {

		if (imageTmpDir == null) {

			imageTmpDir = new File(getTmpDir(), "images");
		}

		if (!imageTmpDir.exists()) {
			try {
				FileUtils.forceMkdir(imageTmpDir);
			} catch (IOException e) {
				e.printStackTrace(System.err);
			}
		}

		return imageTmpDir;
	}

	/**
	 * @return the excludedPackages for the model (may be empty but not
	 *         <code>null</code>).
	 */
	public Set<String> getExcludedPackages() {

		Set<String> excludedPackages = new HashSet<String>();

		String value = parameter(PARAM_INPUT_EXCLUDED_PACKAGES);

		if (value != null) {

			value = value.trim();

			if (value.length() > 0) {

				String[] values = value.split(",");

				for (String v : values) {
					excludedPackages.add(v.trim());
				}
			}
		}

		return excludedPackages;
	}

	/**
	 * Store AIXM schema infos for global use.
	 *
	 * @param schemaInfos
	 */
	public void setAIXMSchemaInfos(Map<String, AIXMSchemaInfo> schemaInfos) {
		this.schemaInfos = schemaInfos;
	}

	/**
	 * Retrieve AIXM schema infos that have been stored previously.
	 *
	 * @return map with schema infos (key: info object id; value: AIXMSchemaInfo
	 *         for the object)
	 */
	public Map<String, AIXMSchemaInfo> getAIXMSchemaInfos() {
		return schemaInfos;
	}

	/**
	 * Determine if global identifiers of model elements shall be loaded or not.
	 * This depends upon the setting of the input element parameter
	 * {@value #PARAM_LOAD_GLOBAL_IDENTIFIERS}.
	 *
	 * @return <code>true</code> if global identifiers shall be loaded, else
	 *         <code>false</code>
	 */
	public boolean isLoadGlobalIdentifiers() {

		return this.loadGlobalIds;
	}
	
	/**
	 * 
	 * @return RFC 5646 language code of the primary language to use in targets and transformers
	 */
	public String language() {

		return this.language;
	}
	


	public boolean useStringInterning() {

		return this.useStringInterning;
	}
	
	/**
	 * Depending upon whether or not string interning shall be used during
	 * processing, this method interns the given string.
	 *
	 * @param string
	 * @return
	 */
	public String internalize(String string) {
		
		if(string == null) {
			return null;
		} else if (useStringInterning) {
			return string.intern();
		} else {
			return string;
		}
	}

	/**
	 * Depending upon whether or not string interning shall be used during
	 * processing, this method interns the given string array or an 
	 * internized copy.
	 *
	 * @param array
	 * @return
	 */
	public String[] internalize(String[] array) {
		if (useStringInterning) {
			String[] result = new String[array.length];
			int i = 0;
			for (String s : array) 
				result[i++] = s.intern();
			return result;
		} else {
			return array;
		}
	}

	/**
	 * Depending on the tagged value implementation we want (map: default;
	 * array: better memory footprint for large models) this returns the one
	 * selected in the configuration.
	 */
	private boolean useTaggedValuesArray() {
		String tvImpl = parameter(PARAM_TAGGED_VALUE_IMPL);
		return (tvImpl != null && tvImpl.equalsIgnoreCase("array"));
	}

	public TaggedValues taggedValueFactory() {
		TaggedValues result;
		if (useTaggedValuesArray()) {
			result = new TaggedValuesCacheArray(this);
		} else {
			result = new TaggedValuesCacheMap(this);
		}
		return result;
	}

	public TaggedValues taggedValueFactory(int size) {
		TaggedValues result;
		if (useTaggedValuesArray()) {
			result = size < 0 ? new TaggedValuesCacheArray(this)
					: new TaggedValuesCacheArray(size, this);
		} else {
			result = size < 0 ? new TaggedValuesCacheMap(this)
					: new TaggedValuesCacheMap(size, this);
		}
		return result;
	}

	/**
	 * @param original
	 * @param tagList
	 * @return can be empty but not <code>null</code>
	 */
	public TaggedValues taggedValueFactory(TaggedValues original,
			String tagList) {
		TaggedValues result;
		if (useTaggedValuesArray()) {
			result = new TaggedValuesCacheArray(original, tagList, this);
		} else {
			result = new TaggedValuesCacheMap(original, tagList, this);
		}
		return result;
	}

	/**
	 * @param original
	 * @return can be empty but not <code>null</code>
	 */
	public TaggedValues taggedValueFactory(TaggedValues original) {
		TaggedValues result;
		if (useTaggedValuesArray()) {
			result = new TaggedValuesCacheArray(original, this);
		} else {
			result = new TaggedValuesCacheMap(original, this);
		}
		return result;
	}

	public Stereotypes stereotypesFactory() {
		return new StereotypesCacheSet(this);
	}

	public Stereotypes stereotypesFactory(Stereotypes stereotypes) {
		return new StereotypesCacheSet(stereotypes, this);
	}
}
