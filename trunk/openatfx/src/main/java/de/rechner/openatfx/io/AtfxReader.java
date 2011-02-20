package de.rechner.openatfx.io;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.asam.ods.AIDName;
import org.asam.ods.AIDNameValueSeqUnitId;
import org.asam.ods.AoException;
import org.asam.ods.AoSession;
import org.asam.ods.ApplElemAccess;
import org.asam.ods.ApplicationAttribute;
import org.asam.ods.ApplicationElement;
import org.asam.ods.ApplicationRelation;
import org.asam.ods.ApplicationStructure;
import org.asam.ods.BaseAttribute;
import org.asam.ods.BaseElement;
import org.asam.ods.BaseRelation;
import org.asam.ods.Blob;
import org.asam.ods.DataType;
import org.asam.ods.ElemId;
import org.asam.ods.EnumerationDefinition;
import org.asam.ods.ErrorCode;
import org.asam.ods.InstanceElement;
import org.asam.ods.NameValueUnit;
import org.asam.ods.RelationRange;
import org.asam.ods.SetType;
import org.asam.ods.SeverityFlag;
import org.asam.ods.TS_Union;
import org.asam.ods.TS_Value;
import org.asam.ods.T_ExternalReference;
import org.asam.ods.T_LONGLONG;
import org.omg.CORBA.ORB;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.rechner.openatfx.AoServiceFactory;
import de.rechner.openatfx.util.ODSHelper;


/**
 * Object for reading ATFX files.
 * 
 * @author Christian Rechner
 */
public class AtfxReader {

    private static final Log LOG = LogFactory.getLog(AtfxReader.class);

    /** The singleton instance */
    private static AtfxReader instance;

    /** cached model information for faster parsing */
    private final Map<String, Map<String, ApplicationAttribute>> applAttrs;
    private final Map<String, Map<String, ApplicationRelation>> applRels;
    private ApplicationAttribute applAttrLocalColumnValues;

    /**
     * Non visible constructor.
     */
    private AtfxReader() {
        this.applAttrs = new HashMap<String, Map<String, ApplicationAttribute>>();
        this.applRels = new HashMap<String, Map<String, ApplicationRelation>>();
    }

    /**
     * Returns the ASAM ODS aoSession object for a ATFX file.
     * 
     * @param orb The ORB.
     * @param atfxFile The ATFX file.
     * @return The aoSession object.
     * @throws AoException Error getting aoSession.
     */
    public synchronized AoSession createSessionForATFX(ORB orb, File atfxFile) throws AoException {
        long start = System.currentTimeMillis();
        this.applAttrs.clear();
        this.applRels.clear();
        this.applAttrLocalColumnValues = null;

        InputStream in = null;
        try {
            // open XML file
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            in = new BufferedInputStream(new FileInputStream(atfxFile));
            XMLStreamReader reader = inputFactory.createXMLStreamReader(in);

            // parse start element 'atfx_file'
            reader.nextTag();
            reader.nextTag();

            // parse 'documentation'
            Map<String, String> documentation = new HashMap<String, String>();
            if (reader.getLocalName().equals(AtfxTagConstants.DOCUMENTATION)) {
                documentation.putAll(parseDocumentation(reader));
                reader.nextTag();
            }

            // parse 'base_model_version'
            String baseModelVersion = "";
            if (reader.getLocalName().equals(AtfxTagConstants.BASE_MODEL_VERSION)) {
                baseModelVersion = reader.getElementText();
                reader.nextTag();
            } else {
                throw new AoException(ErrorCode.AO_UNKNOWN_ERROR, SeverityFlag.ERROR, 0, "Expected tag '"
                        + AtfxTagConstants.BASE_MODEL_VERSION + "'");
            }

            // create AoSession object
            AoSession aoSession = AoServiceFactory.getInstance().newEmptyAoSession(orb, atfxFile, baseModelVersion);

            // parse 'files'
            Map<String, String> files = new HashMap<String, String>();
            if (reader.getLocalName().equals(AtfxTagConstants.FILES)) {
                files.putAll(parseFiles(reader));
                reader.nextTag();
            }

            // parse 'application_model'
            if (reader.getLocalName().equals(AtfxTagConstants.APPL_MODEL)) {
                parseApplicationModel(aoSession.getApplicationStructure(), reader);
                reader.nextTag();
            }

            // parse 'instance_data'
            if (reader.getLocalName().equals(AtfxTagConstants.INSTANCE_DATA)) {
                parseInstanceElements(aoSession, reader, files);
                reader.nextTag();
            }

            LOG.info("Read ATFX in " + (System.currentTimeMillis() - start) + "ms: " + atfxFile.getAbsolutePath());
            return aoSession;
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new AoException(ErrorCode.AO_UNKNOWN_ERROR, SeverityFlag.ERROR, 0, e.getMessage());
        } catch (XMLStreamException e) {
            LOG.error(e.getMessage(), e);
            throw new AoException(ErrorCode.AO_UNKNOWN_ERROR, SeverityFlag.ERROR, 0, e.getMessage());
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Parse the 'documentation' part of the ATFX file.
     * 
     * @param reader The XML stream reader.
     * @return Map containing the key value pairs of the documentation.
     * @throws XMLStreamException Error parsing XML.
     */
    private Map<String, String> parseDocumentation(XMLStreamReader reader) throws XMLStreamException {
        reader.nextTag();
        Map<String, String> map = new HashMap<String, String>();
        while (!(reader.isEndElement() && reader.getLocalName().equals(AtfxTagConstants.DOCUMENTATION))) {
            if (reader.isStartElement()) {
                map.put(reader.getLocalName(), reader.getElementText());
            }
            reader.nextTag();
        }
        return map;
    }

    /***************************************************************************************
     * methods for parsing the component files declaration
     ***************************************************************************************/

    /**
     * Parse the 'files' part of the ATFX file.
     * 
     * @param reader The XML stream reader.
     * @return Map containing the key value pairs of the component files.
     * @throws XMLStreamException Error parsing XML.
     */
    private Map<String, String> parseFiles(XMLStreamReader reader) throws XMLStreamException {
        Map<String, String> map = new HashMap<String, String>();
        // 'files'
        while (!(reader.isEndElement() && reader.getLocalName().equals(AtfxTagConstants.FILES))) {
            if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.COMPONENT)) {
                map.putAll(parseComponent(reader));
            }
            reader.next();
        }
        return map;
    }

    /**
     * Parse one 'component' part.
     * 
     * @param reader The XML stream reader.
     * @return Map containing the key value pairs of the component files.
     * @throws XMLStreamException Error parsing XML.
     */
    private Map<String, String> parseComponent(XMLStreamReader reader) throws XMLStreamException {
        Map<String, String> map = new HashMap<String, String>();
        String identifier = "";
        String filename = "";
        // 'component'
        while (!(reader.isEndElement() && reader.getLocalName().equals(AtfxTagConstants.COMPONENT))) {
            // 'identifier'
            if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.COMPONENT_IDENTIFIER)) {
                identifier = reader.getElementText();
            }
            // 'filename'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.COMPONENT_FILENAME)) {
                filename = reader.getElementText();
            }
            reader.next();
        }
        map.put(identifier, filename);
        return map;
    }

    /***************************************************************************************
     * methods for parsing the application model
     ***************************************************************************************/

    /**
     * Parse the application model.
     * 
     * @param as The application structure.
     * @param reader The XML stream reader.
     * @throws XMLStreamException Error parsing XML.
     * @throws AoException Error writing application structure.
     */
    private void parseApplicationModel(ApplicationStructure as, XMLStreamReader reader) throws XMLStreamException,
            AoException {
        Map<ApplicationRelation, String> applRelElem2Map = new HashMap<ApplicationRelation, String>();
        while (!(reader.isEndElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_MODEL))) {
            // 'application_enumeration'
            if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ENUM)) {
                parseEnumerationDefinition(as, reader);
            }
            // 'application_element'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ELEM)) {
                applRelElem2Map.putAll(parseApplicationElement(as, reader));
            }
            reader.next();
        }

        // set the elem2 of all application relations (this has to be done after parsing all elements)
        for (ApplicationRelation rel : applRelElem2Map.keySet()) {
            rel.setElem2(as.getElementByName(applRelElem2Map.get(rel)));
        }
    }

    /**
     * Parse an enumeration definition.
     * 
     * @param as The application structure.
     * @param reader The XML stream reader.
     * @throws XMLStreamException Error parsing XML.
     * @throws AoException Error writing application structure.
     */
    private void parseEnumerationDefinition(ApplicationStructure as, XMLStreamReader reader) throws XMLStreamException,
            AoException {
        // 'name'
        reader.nextTag();
        if (!reader.getLocalName().equals(AtfxTagConstants.APPL_ENUM_NAME)) {
            throw new AoException(ErrorCode.AO_UNKNOWN_ERROR, SeverityFlag.ERROR, 0, "Expected enumeration name");
        }
        EnumerationDefinition enumDef = as.createEnumerationDefinition(reader.getElementText());
        // items
        while (!(reader.isEndElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ENUM))) {
            // 'item'
            if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ENUM_ITEM)) {
                parseEnumerationItem(enumDef, reader);
            }
            reader.next();
        }
    }

    /**
     * Parse an enumeration item.
     * 
     * @param enumDef The enumeration definition.
     * @param reader The XML stream reader.
     * @throws XMLStreamException Error parsing XML.
     * @throws AoException Error writing to enumeration definition.
     */
    private void parseEnumerationItem(EnumerationDefinition enumDef, XMLStreamReader reader) throws XMLStreamException,
            AoException {
        while (!(reader.isEndElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ENUM_ITEM))) {
            // 'name'
            if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ENUM_NAME)) {
                enumDef.addItem(reader.getElementText());
            }
            reader.next();
        }
    }

    /**
     * Parse an application element.
     * 
     * @param as The application structure.
     * @param reader The XML stream reader.
     * @return Map containing the elem2 ae name for the relations.
     * @throws XMLStreamException Error parsing XML.
     * @throws AoException Error writing to application model.
     */
    private Map<ApplicationRelation, String> parseApplicationElement(ApplicationStructure as, XMLStreamReader reader)
            throws XMLStreamException, AoException {
        // 'name'
        reader.nextTag();
        if (!reader.getLocalName().equals(AtfxTagConstants.APPL_ELEM_NAME)) {
            throw new AoException(ErrorCode.AO_UNKNOWN_ERROR, SeverityFlag.ERROR, 0,
                                  "Expected application element 'name'");
        }
        String aeName = reader.getElementText();
        // 'basetype'
        reader.nextTag();
        if (!reader.getLocalName().equals(AtfxTagConstants.APPL_ELEM_BASETYPE)) {
            throw new AoException(ErrorCode.AO_UNKNOWN_ERROR, SeverityFlag.ERROR, 0,
                                  "Expected application element 'basetype'");
        }
        String basetype = reader.getElementText();

        // create application element
        BaseElement be = as.getSession().getBaseStructure().getElementByType(basetype);
        ApplicationElement applElem = as.createElement(be);
        applElem.setName(aeName);

        // cache base attributes and base relations
        Map<String, BaseAttribute> baseAttrMap = new HashMap<String, BaseAttribute>();
        Map<String, BaseRelation> baseRelMap = new HashMap<String, BaseRelation>();
        for (BaseAttribute baseAttr : applElem.getBaseElement().getAttributes("*")) {
            baseAttrMap.put(baseAttr.getName(), baseAttr);
        }
        for (BaseRelation baseRel : applElem.getBaseElement().getAllRelations()) {
            baseRelMap.put(baseRel.getRelationName(), baseRel);
        }

        // add to global map
        this.applAttrs.put(aeName, new HashMap<String, ApplicationAttribute>());
        this.applRels.put(aeName, new HashMap<String, ApplicationRelation>());

        // attributes and relations
        Map<ApplicationRelation, String> applRelElem2Map = new HashMap<ApplicationRelation, String>();
        while (!(reader.isEndElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ELEM))) {
            // 'application_attribute'
            if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ATTR)) {
                parseApplicationAttribute(applElem, reader, baseAttrMap);
            }
            // 'relation_attribute'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_REL)) {
                applRelElem2Map.putAll(parseApplicationRelation(applElem, reader, baseRelMap));
            }
            reader.next();
        }

        return applRelElem2Map;
    }

    /**
     * Parse an application attribute.
     * 
     * @param applElem The application element.
     * @param reader The XML stream reader.
     * @param baseAttrMap Map containing all base attributes of the application element.
     * @throws XMLStreamException Error parsing XML.
     * @throws AoException Error writing to application model.
     */
    private void parseApplicationAttribute(ApplicationElement applElem, XMLStreamReader reader,
            Map<String, BaseAttribute> baseAttrMap) throws XMLStreamException, AoException {
        String aaNameStr = "";
        String baseAttrStr = "";
        String dataTypeStr = "";
        String lengthStr = "";
        String obligatoryStr = "";
        String uniqueStr = "";
        String autogeneratedStr = "";
        String enumtypeStr = "";
        String unitStr = "";
        while (!(reader.isEndElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ATTR))) {
            // 'name'
            if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ATTR_NAME)) {
                aaNameStr = reader.getElementText();
            }
            // 'base_attribute'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ATTR_BASEATTR)) {
                baseAttrStr = reader.getElementText();
            }
            // 'datatype'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ATTR_DATATYPE)) {
                dataTypeStr = reader.getElementText();
            }
            // 'length'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ATTR_LENGTH)) {
                lengthStr = reader.getElementText();
            }
            // 'obligatory'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ATTR_OBLIGATORY)) {
                obligatoryStr = reader.getElementText();
            }
            // 'unique'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ATTR_UNIQUE)) {
                uniqueStr = reader.getElementText();
            }
            // 'autogenerated'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ATTR_AUTOGENERATED)) {
                autogeneratedStr = reader.getElementText();
            }
            // 'enumeration_type'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ATTR_ENUMTYPE)) {
                enumtypeStr = reader.getElementText();
            }
            // 'unit'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_ATTR_UNIT)) {
                unitStr = reader.getElementText();
            }
            reader.next();
        }

        // check if base attribute already exists (obligatory base attributes are generated automatically)
        ApplicationAttribute aa = null;
        BaseAttribute baseAttr = null;
        if (baseAttrStr != null && baseAttrStr.length() > 0) {
            baseAttr = baseAttrMap.get(baseAttrStr);
            if (baseAttr == null) {
                throw new AoException(ErrorCode.AO_NOT_FOUND, SeverityFlag.ERROR, 0, "Base attribute '" + baseAttrStr
                        + "' not found");
            }
            if (baseAttr.isObligatory()) {
                aa = applElem.getAttributeByBaseName(baseAttrStr);
            }
        }
        if (aa == null) {
            aa = applElem.createAttribute();
        }
        aa.setName(aaNameStr);

        // base attribute?
        if (baseAttrStr != null && baseAttrStr.length() > 0) {
            aa.setBaseAttribute(baseAttr);
        }
        // datatype & obligatory
        else {
            DataType datatype = ODSHelper.string2dataType(dataTypeStr);
            aa.setDataType(datatype);
            aa.setIsObligatory(Boolean.valueOf(obligatoryStr));
        }
        // length
        if (lengthStr != null && lengthStr.length() > 0) {
            aa.setLength(AtfxParseUtil.parseLong(lengthStr));
        }
        // unique
        if (uniqueStr != null && uniqueStr.length() > 0) {
            aa.setIsUnique(AtfxParseUtil.parseBoolean(uniqueStr));
        }
        // autogenerated
        if (autogeneratedStr != null && autogeneratedStr.length() > 0) {
            aa.setIsAutogenerated(AtfxParseUtil.parseBoolean(autogeneratedStr));
        }
        // enumeration
        if (enumtypeStr != null && enumtypeStr.length() > 0) {
            EnumerationDefinition enumDef = applElem.getApplicationStructure().getEnumerationDefinition(enumtypeStr);
            aa.setEnumerationDefinition(enumDef);
        }
        // unit
        if (unitStr != null && unitStr.length() > 0) {
            aa.setUnit(AtfxParseUtil.parseLongLong(unitStr));
        }

        // add to global map
        this.applAttrs.get(applElem.getName()).put(aaNameStr, aa);
        if (baseAttrStr.equals("value") && applElem.getBaseElement().getType().equals("AoLocalColumn")) {
            this.applAttrLocalColumnValues = aa;
        }
    }

    /**
     * Parse an application relation.
     * 
     * @param applElem The application element
     * @param reader The XML stream reader.
     * @param baseRelMap Map containing all base relations. of the application element.
     * @return Map containing the elem2 ae name for the relations.
     * @throws XMLStreamException Error parsing XML.
     * @throws AoException Error writing to application model.
     */
    private Map<ApplicationRelation, String> parseApplicationRelation(ApplicationElement applElem,
            XMLStreamReader reader, Map<String, BaseRelation> baseRelMap) throws XMLStreamException, AoException {
        String elem2Name = "";
        String relName = "";
        String inverseRelName = "";
        String brName = "";
        String minStr = "";
        String maxStr = "";
        while (!(reader.isEndElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_REL))) {
            // 'ref_to'
            if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_REL_REFTO)) {
                elem2Name = reader.getElementText();
            }
            // 'name'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_REL_NAME)) {
                relName = reader.getElementText();
            }
            // 'inverse_name'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_REL_INVNAME)) {
                inverseRelName = reader.getElementText();
            }
            // 'base_relation'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_REL_BASEREL)) {
                brName = reader.getElementText();
            }
            // 'min_occurs'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_REL_MIN)) {
                minStr = reader.getElementText();
            }
            // 'max_occurs'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.APPL_REL_MAX)) {
                maxStr = reader.getElementText();
            }
            reader.next();
        }

        ApplicationStructure as = applElem.getApplicationStructure();
        ApplicationRelation rel = as.createRelation();
        RelationRange relRange = new RelationRange();
        relRange.min = ODSHelper.string2relRange(minStr);
        relRange.max = ODSHelper.string2relRange(maxStr);

        rel.setElem1(applElem);
        rel.setRelationName(relName);
        rel.setInverseRelationName(inverseRelName);
        rel.setRelationRange(relRange);
        if (brName != null && brName.length() > 0) {
            BaseRelation baseRel = baseRelMap.get(brName);
            if (baseRel == null) {
                throw new AoException(ErrorCode.AO_NOT_FOUND, SeverityFlag.ERROR, 0, "BaseRelation '" + brName
                        + "' not found'");
            }
            rel.setBaseRelation(baseRel);
        }

        // add to global map
        this.applRels.get(applElem.getName()).put(relName, rel);

        // return the information of the ref to application element
        Map<ApplicationRelation, String> applRelElem2Map = new HashMap<ApplicationRelation, String>();
        applRelElem2Map.put(rel, elem2Name);
        return applRelElem2Map;
    }

    /***************************************************************************************
     * methods for parsing instance elements
     ***************************************************************************************/

    /**
     * Read the instance elements from the instance data XML element.
     * <p>
     * Also the relations are parsed and set.
     * 
     * @param aoSession The session.
     * @param reader The XML stream reader.
     * @param componentMap The mapping between external file identifier and file name.
     * @throws XMLStreamException Error parsing XML.
     * @throws AoException Error writing to application model.
     */
    private void parseInstanceElements(AoSession aoSession, XMLStreamReader reader, Map<String, String> componentMap)
            throws XMLStreamException, AoException {
        ApplicationStructure as = aoSession.getApplicationStructure();
        Map<ElemId, Map<ApplicationRelation, T_LONGLONG[]>> relMap = new HashMap<ElemId, Map<ApplicationRelation, T_LONGLONG[]>>();

        // parse instances
        reader.next();
        while (!(reader.isEndElement() && reader.getLocalName().equals(AtfxTagConstants.INSTANCE_DATA))) {
            if (reader.isStartElement()) {
                parseInstanceElement(as, reader, componentMap);
            }
            reader.next();
        }

        // create relations
        ApplElemAccess applElemAccess = aoSession.getApplElemAccess();
        for (ElemId elemId : relMap.keySet()) {
            for (ApplicationRelation applRel : relMap.get(elemId).keySet()) {
                T_LONGLONG[] relIids = relMap.get(elemId).get(applRel);
                applElemAccess.setRelInst(elemId, applRel.getRelationName(), relIids, SetType.APPEND);
            }
        }
    }

    /**
     * Read the all attributes,relations and security information from the instance element XML element.
     * 
     * @param as The applications structure.
     * @param reader The XML stream reader.
     * @param componentMap The mapping between external file identifier and file name.
     * @return
     * @throws XMLStreamException Error parsing XML.
     * @throws AoException Error writing to application model.
     */
    private Map<ElemId, Map<ApplicationRelation, T_LONGLONG[]>> parseInstanceElement(ApplicationStructure as,
            XMLStreamReader reader, Map<String, String> componentMap) throws XMLStreamException, AoException {
        // 'name'
        String aeName = reader.getLocalName();
        ApplicationElement applElem = as.getElementByName(aeName);

        // read attributes
        List<AIDNameValueSeqUnitId> applAttrValues = new ArrayList<AIDNameValueSeqUnitId>();
        List<NameValueUnit> instAttrValues = new ArrayList<NameValueUnit>();
        Map<ApplicationRelation, T_LONGLONG[]> instRelMap = new HashMap<ApplicationRelation, T_LONGLONG[]>();
        while (!(reader.isEndElement() && reader.getLocalName().equals(aeName))) {

            // application attribute
            if (reader.isStartElement() && (getApplAttr(aeName, reader.getLocalName()) != null)) {
                ApplicationAttribute aa = getApplAttr(aeName, reader.getLocalName());
                AIDNameValueSeqUnitId applAttrValue = new AIDNameValueSeqUnitId();
                applAttrValue.unitId = ODSHelper.asODSLongLong(0);
                applAttrValue.attr = new AIDName();
                applAttrValue.attr.aid = applElem.getId();
                applAttrValue.attr.aaName = reader.getLocalName();
                applAttrValue.values = ODSHelper.tsValue2tsValueSeq(parseAttributeContent(aa, reader));
                applAttrValues.add(applAttrValue);
            }

            // application relation
            else if (reader.isStartElement() && (getApplRel(aeName, reader.getLocalName()) != null)) {
                // only read the non inverse relations for performance reasons!
                ApplicationRelation applRel = getApplRel(aeName, reader.getLocalName());
                short relMax = applRel.getRelationRange().max;
                short invMax = applRel.getInverseRelationRange().max;
                if ((invMax == -1) || (relMax == 1 && invMax == 1)) {
                    String textContent = reader.getElementText();
                    if (textContent.length() > 0) {
                        T_LONGLONG[] relInstIids = AtfxParseUtil.parseLongLongSeq(textContent);
                        instRelMap.put(applRel, relInstIids);
                    }
                }
            }

            // instance attribute
            else if (reader.isStartElement() && (reader.getLocalName().equals(AtfxTagConstants.INST_ATTR))) {
            }

            // ACLA
            else if (reader.isStartElement() && (reader.getLocalName().equals(AtfxTagConstants.SECURITY_ACLA))) {
            }

            // ACLI
            else if (reader.isStartElement() && (reader.getLocalName().equals(AtfxTagConstants.SECURITY_ACLI))) {
            }

            // values of 'LocalColumn'
            else if (reader.isStartElement() && isLocalColumnValue(aeName, reader.getLocalName())) {
            }

            reader.next();
        }

        // create instance element
        ApplElemAccess aea = as.getSession().getApplElemAccess();
        ElemId elemId = aea.insertInstances(applAttrValues.toArray(new AIDNameValueSeqUnitId[0]))[0];

        // set instance attributes
        if (!instAttrValues.isEmpty()) {
            InstanceElement ie = applElem.getInstanceById(elemId.iid);
            for (NameValueUnit nvu : instAttrValues) {
                ie.addInstanceAttribute(nvu);
            }
        }

        // parse measurement values
        // if (valuesElement != null) {
        // InstanceElement localColumnIe = applElem.getInstanceById(elemId.iid);
        // parseMeasurementData(componentMap, localColumnIe, valuesElement);
        // }

        // create relation map
        Map<ElemId, Map<ApplicationRelation, T_LONGLONG[]>> retMap = new HashMap<ElemId, Map<ApplicationRelation, T_LONGLONG[]>>();
        retMap.put(new ElemId(applElem.getId(), elemId.iid), instRelMap);

        return retMap;
    }

    private ApplicationAttribute getApplAttr(String aeName, String name) {
        Map<String, ApplicationAttribute> attrMap = this.applAttrs.get(aeName);
        if (attrMap != null) {
            return attrMap.get(name);
        }
        return null;
    }

    private ApplicationRelation getApplRel(String aeName, String name) {
        Map<String, ApplicationRelation> relMap = this.applRels.get(aeName);
        if (relMap != null) {
            return relMap.get(name);
        }
        return null;
    }

    private boolean isLocalColumnValue(String aeName, String name) throws AoException {
        if (this.applAttrLocalColumnValues != null) {
            if (this.applAttrLocalColumnValues.getName().equals(name)
                    && this.applAttrLocalColumnValues.getApplicationElement().getName().equals(aeName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Read the instance attributes from the instance attributes XML element.
     * 
     * @param instElemsNode The instance attributes XML element.
     * @return List of values.
     */
    private List<NameValueUnit> parseInstanceAttributes(Element instElemsNode) throws AoException {
        List<NameValueUnit> instAttrs = new ArrayList<NameValueUnit>();

        NodeList nodeList = instElemsNode.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String nodeName = node.getNodeName();
            NameValueUnit nvu = new NameValueUnit();
            nvu.unit = "";
            nvu.valName = ((Element) node).getAttribute(AtfxTagConstants.INST_ATTR_NAME);
            nvu.value = new TS_Value();
            nvu.value.u = new TS_Union();

            String textContent = node.getTextContent();
            // DT_STRING
            if (nodeName.equals(AtfxTagConstants.INST_ATTR_ASCIISTRING)) {
                nvu.value.u.stringVal(textContent);
                nvu.value.flag = (textContent == null || textContent.length() < 1) ? (short) 0 : (short) 15;
            }
            // DT_FLOAT
            else if (nodeName.equals(AtfxTagConstants.INST_ATTR_FLOAT32)) {
                if (textContent.trim().length() > 0) {
                    nvu.value.u.floatVal(AtfxParseUtil.parseFloat(textContent));
                    nvu.value.flag = (short) 15;
                } else {
                    nvu.value.u.floatVal(0);
                    nvu.value.flag = (short) 0;
                }
            }
            // DT_DOUBLE
            else if (nodeName.equals(AtfxTagConstants.INST_ATTR_FLOAT64)) {
                if (textContent.trim().length() > 0) {
                    nvu.value.u.doubleVal(AtfxParseUtil.parseDouble(textContent));
                    nvu.value.flag = (short) 15;
                } else {
                    nvu.value.u.doubleVal(0);
                    nvu.value.flag = (short) 0;
                }
            }
            // DT_BYTE
            else if (nodeName.equals(AtfxTagConstants.INST_ATTR_INT8)) {
                if (textContent.trim().length() > 0) {
                    nvu.value.u.byteVal(AtfxParseUtil.parseByte(textContent));
                    nvu.value.flag = (short) 15;
                } else {
                    nvu.value.u.byteVal((byte) 0);
                    nvu.value.flag = (short) 0;
                }
            }
            // DT_SHORT
            else if (nodeName.equals(AtfxTagConstants.INST_ATTR_INT16)) {
                if (textContent.trim().length() > 0) {
                    nvu.value.u.shortVal(AtfxParseUtil.parseShort(textContent));
                    nvu.value.flag = (short) 15;
                } else {
                    nvu.value.u.shortVal((short) 0);
                    nvu.value.flag = (short) 0;
                }
            }
            // DT_LONG
            else if (nodeName.equals(AtfxTagConstants.INST_ATTR_INT32)) {
                if (textContent.trim().length() > 0) {
                    nvu.value.u.longVal(AtfxParseUtil.parseLong(textContent));
                    nvu.value.flag = (short) 15;
                } else {
                    nvu.value.u.longVal(0);
                    nvu.value.flag = (short) 0;
                }
            }
            // DT_LONGLONG
            else if (nodeName.equals(AtfxTagConstants.INST_ATTR_INT64)) {
                if (textContent.trim().length() > 0) {
                    nvu.value.u.longlongVal(AtfxParseUtil.parseLongLong(textContent));
                    nvu.value.flag = (short) 15;
                } else {
                    nvu.value.u.longlongVal(ODSHelper.asODSLongLong(0));
                    nvu.value.flag = (short) 0;
                }
            }
            // DT_DATE
            else if (nodeName.equals(AtfxTagConstants.INST_ATTR_TIME)) {
                if (textContent.trim().length() > 0) {
                    nvu.value.u.dateVal(textContent.trim());
                    nvu.value.flag = (short) 15;
                } else {
                    nvu.value.u.dateVal("");
                    nvu.value.flag = (short) 0;
                }
            }

            instAttrs.add(nvu);
        }

        return instAttrs;
    }

    /**
     * Parse the inline measurement data found in the XML element of the 'values' application attribute of an instance
     * of LocalColumn.
     * 
     * @param localColumnIe The LocalColumn instance element.
     * @param attrElem The XML element.
     * @throws AoException
     */
    private void parseMeasurementData(Map<String, String> componentMap, InstanceElement localColumnIe, Element attrElem)
            throws AoException {
        NodeList nodeList = attrElem.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String nodeName = node.getNodeName();
            // external component
            if (nodeName.equals(AtfxTagConstants.COMPONENT)) {
                parseComponent(localColumnIe, (Element) node);
            }
            // explicit values
            else {
                NameValueUnit nvu = new NameValueUnit();
                nvu.valName = localColumnIe.getApplicationElement().getAttributeByBaseName("values").getName();
                nvu.unit = "";
                nvu.value = new TS_Value();
                nvu.value.flag = (short) 15;
                nvu.value.u = new TS_Union();

                // DT_BLOB
                if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_BLOB)) {
                    // nvu.value.u.booleanSeq(AtfxParseUtil.parseBooleanSeq(node.getTextContent()));
                }
                // DT_BOOLEAN
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_BOOLEAN)) {
                    nvu.value.u.booleanSeq(AtfxParseUtil.parseBooleanSeq(node.getTextContent()));
                }
                // DT_BYTESTR
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_BYTEFIELD)) {
                    // nvu.value.u.byteSeq(AtfxParseUtil.parseByteSeq(node.getTextContent()));
                }
                // DT_COMPLEX
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_COMPLEX32)) {
                    nvu.value.u.complexSeq(AtfxParseUtil.parseComplexSeq(node.getTextContent()));
                }
                // DT_DCOMPLEX
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_COMPLEX64)) {
                    nvu.value.u.dcomplexSeq(AtfxParseUtil.parseDComplexSeq(node.getTextContent()));
                }
                // DT_EXTERNALREFERENCE
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_EXTERNALREFERENCE)) {
                    // nvu.value.u.extRefSeq(parseExtRefs((Element) node));
                }
                // DT_BYTE
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_INT8)) {
                    nvu.value.u.byteSeq(AtfxParseUtil.parseByteSeq(node.getTextContent()));
                }
                // DT_SHORT
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_INT16)) {
                    nvu.value.u.shortSeq(AtfxParseUtil.parseShortSeq(node.getTextContent()));
                }
                // DT_LONG
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_INT32)) {
                    nvu.value.u.longSeq(AtfxParseUtil.parseLongSeq(node.getTextContent()));
                }
                // DT_LONGLONG
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_INT64)) {
                    nvu.value.u.longlongSeq(AtfxParseUtil.parseLongLongSeq(node.getTextContent()));
                }
                // DT_FLOAT
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_FLOAT32)) {
                    nvu.value.u.floatSeq(AtfxParseUtil.parseFloatSeq(node.getTextContent()));
                }
                // DT_DOUBLE
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_FLOAT64)) {
                    nvu.value.u.doubleSeq(AtfxParseUtil.parseDoubleSeq(node.getTextContent()));
                }
                // DT_DATE
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_TIMESTRING)) {
                    // nvu.value.u.dateSeq(parseStringSeq((Element) node));
                }
                // DT_STRING
                else if (nodeName.equals(AtfxTagConstants.VALUES_ATTR_UTF8STRING)) {
                    // nvu.value.u.stringSeq(parseStringSeq((Element) node));
                }
                // not supported
                else {
                    throw new AoException(ErrorCode.AO_INVALID_DATATYPE, SeverityFlag.ERROR, 0,
                                          "Unsupported values datatype: " + nodeName);
                }

                // set values
                localColumnIe.setValue(nvu);
            }
        }
    }

    private void parseComponent(InstanceElement localColumnIe, Element attrElem) throws AoException {
        NodeList nodeList = attrElem.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            String nodeName = node.getNodeName();

            // external component configuration
            if (nodeName.equals(AtfxTagConstants.COMPONENT_IDENTIFIER)) {

            } else if (nodeName.equals(AtfxTagConstants.COMPONENT_DATATYPE)) {

            } else if (nodeName.equals(AtfxTagConstants.COMPONENT_LENGTH)) {

            }
        }

        // insert external component
        ApplicationStructure as = localColumnIe.getApplicationElement().getApplicationStructure();
        ApplicationElement aeLc = as.getElementsByBaseType("AoExternalComponent")[0];
        ApplicationRelation relLc2ExtComp = localColumnIe.getApplicationElement()
                                                         .getRelationsByBaseName("external_component")[0];
        InstanceElement ieExtComp = aeLc.createInstance("ExtComp");
        localColumnIe.createRelation(relLc2ExtComp, ieExtComp);
    }

    /***************************************************************************************
     * methods for parsing attribute values
     ***************************************************************************************/

    /**
     * @param aa
     * @param reader
     * @return
     * @throws XMLStreamException
     * @throws AoException
     */
    private TS_Value parseAttributeContent(ApplicationAttribute aa, XMLStreamReader reader) throws XMLStreamException,
            AoException {
        DataType dataType = aa.getDataType();
        TS_Value tsValue = ODSHelper.createEmptyTS_Value(dataType);
        tsValue.flag = 15;
        tsValue.u = new TS_Union();
        // DT_BLOB
        if (dataType == DataType.DT_BLOB) {
            tsValue.u.blobVal(parseBlob(aa, reader));
        }
        // DT_BOOLEAN
        else if (dataType == DataType.DT_BOOLEAN) {
            tsValue.u.booleanVal(AtfxParseUtil.parseBoolean(reader.getElementText()));
        }
        // DT_BYTE
        else if (dataType == DataType.DT_BYTE) {
            tsValue.u.byteVal(AtfxParseUtil.parseByte(reader.getElementText()));
        }
        // DT_BYTESTR
        else if (dataType == DataType.DT_BYTESTR) {
            tsValue.u.bytestrVal(AtfxParseUtil.parseByteSeq(reader.getElementText()));
        }
        // DT_COMPLEX
        else if (dataType == DataType.DT_COMPLEX) {
            tsValue.u.complexVal(AtfxParseUtil.parseComplex(reader.getElementText()));
        }
        // DT_DATE
        else if (dataType == DataType.DT_DATE) {
            tsValue.u.dateVal(reader.getElementText());
        }
        // DT_COMPLEX
        else if (dataType == DataType.DT_DCOMPLEX) {
            tsValue.u.dcomplexVal(AtfxParseUtil.parseDComplex(reader.getElementText()));
        }
        // DT_DOUBLE
        else if (dataType == DataType.DT_DOUBLE) {
            tsValue.u.doubleVal(AtfxParseUtil.parseDouble(reader.getElementText()));
        }
        // DT_ENUM
        else if (dataType == DataType.DT_ENUM) {
            EnumerationDefinition ed = aa.getEnumerationDefinition();
            tsValue.u.enumVal(ed.getItem(reader.getElementText()));
        }
        // DT_EXTERNALREFERENCE
        else if (dataType == DataType.DT_EXTERNALREFERENCE) {
            T_ExternalReference[] extRefs = parseExtRefs(aa.getName(), reader);
            if (extRefs.length > 1) {
                throw new AoException(ErrorCode.AO_INVALID_LENGTH, SeverityFlag.ERROR, 0,
                                      "Multiple references for datatype DT_EXTERNALREFERENCE FOUND");
            }
            tsValue.u.extRefVal(extRefs[0]);
        }
        // DT_FLOAT
        else if (dataType == DataType.DT_FLOAT) {
            tsValue.u.floatVal(AtfxParseUtil.parseFloat(reader.getElementText()));
        }
        // DT_ID
        else if (dataType == DataType.DT_ID) {
            throw new AoException(ErrorCode.AO_IMPLEMENTATION_PROBLEM, SeverityFlag.ERROR, 0,
                                  "DataType 'DT_ID' not supported for application attribute");
        }
        // DT_LONG
        else if (dataType == DataType.DT_LONG) {
            tsValue.u.longVal(AtfxParseUtil.parseLong(reader.getElementText()));
        }
        // DT_LONGLONG
        else if (dataType == DataType.DT_LONGLONG) {
            tsValue.u.longlongVal(AtfxParseUtil.parseLongLong(reader.getElementText()));
        }
        // DT_SHORT
        else if (dataType == DataType.DT_SHORT) {
            tsValue.u.shortVal(AtfxParseUtil.parseShort(reader.getElementText()));
        }
        // DT_STRING
        else if (dataType == DataType.DT_STRING) {
            tsValue.u.stringVal(reader.getElementText());
        }
        // DS_BOOLEAN
        else if (dataType == DataType.DS_BOOLEAN) {
            tsValue.u.booleanSeq(AtfxParseUtil.parseBooleanSeq(reader.getElementText()));
        }
        // DS_BYTE
        else if (dataType == DataType.DS_BYTE) {
            tsValue.u.byteSeq(AtfxParseUtil.parseByteSeq(reader.getElementText()));
        }
        // DS_BYTESTR
        else if (dataType == DataType.DS_BYTESTR) {
            throw new AoException(ErrorCode.AO_IMPLEMENTATION_PROBLEM, SeverityFlag.ERROR, 0,
                                  "DataType 'DS_BYTESTR' not supported for application attribute");
        }
        // DS_COMPLEX
        else if (dataType == DataType.DS_COMPLEX) {
            tsValue.u.complexSeq(AtfxParseUtil.parseComplexSeq(reader.getElementText()));
        }
        // DS_DATE
        else if (dataType == DataType.DS_DATE) {
            tsValue.u.dateSeq(parseStringSeq(aa.getName(), reader));
        }
        // DS_DCOMPLEX
        else if (dataType == DataType.DS_DCOMPLEX) {
            tsValue.u.dcomplexSeq(AtfxParseUtil.parseDComplexSeq(reader.getElementText()));
        }
        // DS_DOUBLE
        else if (dataType == DataType.DS_DOUBLE) {
            tsValue.u.doubleSeq(AtfxParseUtil.parseDoubleSeq(reader.getElementText()));
        }
        // DS_ENUM
        else if (dataType == DataType.DS_ENUM) {
            String[] enumValues = parseStringSeq(aa.getName(), reader);
            EnumerationDefinition ed = aa.getEnumerationDefinition();
            int[] enumItems = new int[enumValues.length];
            for (int i = 0; i < enumItems.length; i++) {
                enumItems[i] = ed.getItem(enumValues[i]);
            }
            tsValue.u.enumSeq(enumItems);
        }
        // DS_EXTERNALREFERENCE
        else if (dataType == DataType.DS_EXTERNALREFERENCE) {
            tsValue.u.extRefSeq(parseExtRefs(aa.getName(), reader));
        }
        // DS_FLOAT
        else if (dataType == DataType.DS_FLOAT) {
            tsValue.u.floatSeq(AtfxParseUtil.parseFloatSeq(reader.getElementText()));
        }
        // DS_ID
        else if (dataType == DataType.DS_ID) {
            throw new AoException(ErrorCode.AO_IMPLEMENTATION_PROBLEM, SeverityFlag.ERROR, 0,
                                  "DataType 'DS_ID' not supported for application attribute");
        }
        // DS_LONG
        else if (dataType == DataType.DS_LONG) {
            tsValue.u.longSeq(AtfxParseUtil.parseLongSeq(reader.getElementText()));
        }
        // DS_LONGLONG
        else if (dataType == DataType.DS_LONGLONG) {
            tsValue.u.longlongSeq(AtfxParseUtil.parseLongLongSeq(reader.getElementText()));
        }
        // DS_SHORT
        else if (dataType == DataType.DS_SHORT) {
            tsValue.u.shortSeq(AtfxParseUtil.parseShortSeq(reader.getElementText()));
        }
        // DS_STRING
        else if (dataType == DataType.DS_STRING) {
            tsValue.u.stringSeq(parseStringSeq(aa.getName(), reader));
        }
        // DT_UNKNOWN: only for the values of a LocalColumn
        else if (dataType == DataType.DT_UNKNOWN) {
            tsValue.u.floatSeq(new float[0]);
        }
        // unsupported data type
        else {
            throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "DataType " + dataType.value()
                    + " not yet implemented");
        }
        return tsValue;
    }

    /**
     * Parse a BLOB object from given application attribute XML element.
     * 
     * @param aa The application attribute.
     * @param reader The XML stream reader.
     * @return The Blob.
     * @throws XMLStreamException Error parsing XML.
     * @throws AoException Error writing to application model.
     */
    private Blob parseBlob(ApplicationAttribute aa, XMLStreamReader reader) throws XMLStreamException, AoException {
        Blob blob = aa.getApplicationElement().getApplicationStructure().getSession().createBlob();
        while (!(reader.isEndElement() && reader.getLocalName().equals(aa.getName()))) {
            // 'text'
            if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.BLOB_TEXT)) {
                blob.setHeader(reader.getElementText());
            }
            // 'sequence'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.BLOB_SEQUENCE)) {
                blob.append(AtfxParseUtil.parseByteSeq(reader.getElementText()));
            }
            reader.next();
        }
        return blob;
    }

    /**
     * Parse an array of T_ExternalReference objects from given XML element.
     * 
     * @param attrName The attribute name.
     * @param reader The XML stream reader.
     * @return The array T_ExternalReference objects.
     * @throws XMLStreamException Error parsing XML.
     */
    private T_ExternalReference[] parseExtRefs(String attrName, XMLStreamReader reader) throws XMLStreamException {
        List<T_ExternalReference> list = new ArrayList<T_ExternalReference>();
        while (!(reader.isEndElement() && reader.getLocalName().equals(attrName))) {
            // 'external_reference'
            if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.EXTREF)) {
                list.add(parseExtRef(reader));
            }
            reader.next();
        }
        return list.toArray(new T_ExternalReference[0]);
    }

    /**
     * Parse an external reference from the external references node.
     * 
     * @param reader The XML stream reader.
     * @return The T_ExternalReference object.
     * @throws XMLStreamException Error parsing XML.
     */
    private T_ExternalReference parseExtRef(XMLStreamReader reader) throws XMLStreamException {
        T_ExternalReference extRef = new T_ExternalReference("", "", "");
        while (!(reader.isEndElement() && reader.getLocalName().equals(AtfxTagConstants.EXTREF))) {
            // 'description'
            if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.EXTREF_DESCRIPTION)) {
                extRef.description = reader.getElementText();
            }
            // 'mimetype'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.EXTREF_MIMETYPE)) {
                extRef.mimeType = reader.getElementText();
            }
            // 'location'
            else if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.EXTREF_LOCATION)) {
                extRef.location = reader.getElementText();
            }
            reader.next();
        }
        return extRef;
    }

    /**
     * Parse an array of strings objects from given XML element.
     * 
     * @param attrName The attribute name.
     * @param reader The XML stream reader.
     * @return The string sequence.
     * @throws XMLStreamException Error parsing XML.
     */
    private String[] parseStringSeq(String attrName, XMLStreamReader reader) throws XMLStreamException {
        List<String> list = new ArrayList<String>();
        while (!(reader.isEndElement() && reader.getLocalName().equals(attrName))) {
            // 's'
            if (reader.isStartElement() && reader.getLocalName().equals(AtfxTagConstants.STRING_SEQ)) {
                list.add(reader.getElementText());
            }
            reader.next();
        }
        return list.toArray(new String[0]);
    }

    /**
     * Returns the singleton instance.
     * 
     * @return The singleton instance.
     */
    public static AtfxReader getInstance() {
        if (instance == null) {
            instance = new AtfxReader();
        }
        return instance;
    }

}
