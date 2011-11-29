package de.rechner.openatfx;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.asam.ods.ACL;
import org.asam.ods.AoException;
import org.asam.ods.ApplicationAttribute;
import org.asam.ods.ApplicationAttributePOA;
import org.asam.ods.ApplicationElement;
import org.asam.ods.BaseAttribute;
import org.asam.ods.DataType;
import org.asam.ods.EnumerationDefinition;
import org.asam.ods.ErrorCode;
import org.asam.ods.InstanceElement;
import org.asam.ods.InstanceElementIterator;
import org.asam.ods.RightsSet;
import org.asam.ods.SeverityFlag;
import org.asam.ods.T_LONGLONG;

import de.rechner.openatfx.util.ODSHelper;


/**
 * Implementation of <code>org.asam.ods.ApplicationAttribute</code>.
 * 
 * @author Christian Rechner
 */
class ApplicationAttributeImpl extends ApplicationAttributePOA {

    /**
     * this map contains the names of all base attributes that are defined as 'non optional' in the ods specification
     * and their respective base element names. this workaround is necessary, because while the ods spec has two
     * distinct flags 'optional' and 'mandatory' for each attribute, the ods interface does not. thus this
     * implementation returns 'true' for isObligatory() whenever one of the attribute names below is encountered.
     * additionally, it does not permit the obligatory flag to be set to 'false' for those attributes.
     */
    private static final Map<String, List<String>> obligatoryAttributes;

    static {
        obligatoryAttributes = new HashMap<String, List<String>>();
        obligatoryAttributes.put("id", null); // all application elements
        obligatoryAttributes.put("name", null); // all application elements
        obligatoryAttributes.put("external_references", Arrays.asList(new String[] { "AoEnvironment", "AoNameMap",
                "AoAttributeMap", "AoQuantity", "AoUnit", "AoPhysicalDimension", "AoQuantityGroup", "AoUnitGroup",
                "AoMeasurement", "AoMeasurementQuantity", "AoSubmatrix", "AoLocalColumn", "AoExternalComponent",
                "AoTest", "AoSubTest", "AoUnitUnderTest", "AoUnitUnderTestPart", "AoTestSequence",
                "AoTestSequencePart", "AoTestEquipment", "AoTestEquipmentPart", "AoTestDevice", "AoUser",
                "AoUserGroup", "AoAny", "AoLog", "AoParameter", "AoParameterSet" }));
        obligatoryAttributes.put("meaning_of_aliases", Arrays.asList(new String[] { "AoEnvironment" }));
        obligatoryAttributes.put("entity_name", Arrays.asList(new String[] { "AoNameMap", "AoAttributeMap" }));
        obligatoryAttributes.put("alias_names", Arrays.asList(new String[] { "AoNameMap", "AoAttributeMap" }));
        obligatoryAttributes.put("default_dimension", Arrays.asList(new String[] { "AoQuantity" }));
        obligatoryAttributes.put("factor", Arrays.asList(new String[] { "AoUnit" }));
        obligatoryAttributes.put("offset", Arrays.asList(new String[] { "AoUnit" }));
        obligatoryAttributes.put("length_exp", Arrays.asList(new String[] { "AoPhysicalDimension" }));
        obligatoryAttributes.put("mass_exp", Arrays.asList(new String[] { "AoPhysicalDimension" }));
        obligatoryAttributes.put("time_exp", Arrays.asList(new String[] { "AoPhysicalDimension" }));
        obligatoryAttributes.put("current_exp", Arrays.asList(new String[] { "AoPhysicalDimension" }));
        obligatoryAttributes.put("temperature_exp", Arrays.asList(new String[] { "AoPhysicalDimension" }));
        obligatoryAttributes.put("molar_amount_exp", Arrays.asList(new String[] { "AoPhysicalDimension" }));
        obligatoryAttributes.put("luminous_intensity_exp", Arrays.asList(new String[] { "AoPhysicalDimension" }));
        obligatoryAttributes.put("datatype", Arrays.asList(new String[] { "AoMeasurementQuantity" }));
        obligatoryAttributes.put("dimension", Arrays.asList(new String[] { "AoMeasurementQuantity" }));
        obligatoryAttributes.put("number_of_rows", Arrays.asList(new String[] { "AoSubmatrix" }));
        obligatoryAttributes.put("flags", Arrays.asList(new String[] { "AoLocalColumn" }));
        obligatoryAttributes.put("independent", Arrays.asList(new String[] { "AoLocalColumn" }));
        obligatoryAttributes.put("sequence_representation", Arrays.asList(new String[] { "AoLocalColumn" }));
        obligatoryAttributes.put("generation_parameters", Arrays.asList(new String[] { "AoLocalColumn" }));
        obligatoryAttributes.put("component_length", Arrays.asList(new String[] { "AoExternalComponent" }));
        obligatoryAttributes.put("filename_url", Arrays.asList(new String[] { "AoExternalComponent" }));
        obligatoryAttributes.put("value_type", Arrays.asList(new String[] { "AoExternalComponent" }));
        obligatoryAttributes.put("password", Arrays.asList(new String[] { "AoUser" }));
        obligatoryAttributes.put("superuser_flag", Arrays.asList(new String[] { "AoUserGroup" }));
        obligatoryAttributes.put("date", Arrays.asList(new String[] { "AoLog" }));
        obligatoryAttributes.put("parameter_datatype", Arrays.asList(new String[] { "AoParameter" }));
        obligatoryAttributes.put("pvalue", Arrays.asList(new String[] { "AoParameter" }));
    }

    private final AtfxCache atfxCache;
    private final long aid;

    private String aaName;
    private BaseAttribute baseAttribute;
    private DataType dataType;
    private int length;
    private boolean obligatory;
    private boolean unique;
    private boolean autogenerated;
    private EnumerationDefinition enumerationDefinition;
    private long unitId;
    private boolean valueFlag;

    /**
     * Constructor.
     * 
     * @param atfxCache The atfx cache.
     * @param aid The application element id.
     */
    public ApplicationAttributeImpl(AtfxCache atfxCache, long aid) {
        this.atfxCache = atfxCache;
        this.aid = aid;
        this.dataType = DataType.DT_UNKNOWN;
        this.aaName = "";
        this.length = 1;
        this.obligatory = false;
        this.unique = false;
        this.autogenerated = false;
        this.unitId = 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#getApplicationElement()
     */
    public ApplicationElement getApplicationElement() throws AoException {
        return this.atfxCache.getApplicationElementById(this.aid);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#getName()
     */
    public String getName() throws AoException {
        return this.aaName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#setName(java.lang.String)
     */
    public void setName(String aaName) throws AoException {
        // check name length
        if (aaName == null || aaName.length() < 1) {
            throw new AoException(ErrorCode.AO_BAD_PARAMETER, SeverityFlag.ERROR, 0, "aaName must not be empty");
        }
        if (aaName.length() > 30) {
            throw new AoException(ErrorCode.AO_BAD_PARAMETER, SeverityFlag.ERROR, 0,
                                  "Application attribute name must not be greater than 30 characters");
        }
        // check for name equality
        if (this.aaName.equals(aaName)) {
            return;
        }
        // check for existing application attribute name
        if (this.atfxCache.getApplicationAttributeByName(aid, aaName) != null) {
            throw new AoException(ErrorCode.AO_DUPLICATE_NAME, SeverityFlag.ERROR, 0,
                                  "ApplicationAttribute with name '" + aaName + "' already exists");
        }
        // rename
        this.atfxCache.renameApplicationAttribute(aid, this.aaName, aaName);
        this.aaName = aaName;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#getBaseAttribute()
     */
    public BaseAttribute getBaseAttribute() throws AoException {
        return this.baseAttribute;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#setBaseAttribute(org.asam.ods.BaseAttribute)
     */
    public void setBaseAttribute(BaseAttribute baseAttr) throws AoException {
        if (baseAttr != null) {
            // check data type
            if ((this.dataType != DataType.DT_UNKNOWN) && (this.dataType != baseAttr.getDataType())) {
                throw new AoException(ErrorCode.AO_INVALID_DATATYPE, SeverityFlag.ERROR, 0, "Incompatible datatypes");
            }

            // check if already an application attribute exists with same base attribute
            for (ApplicationAttribute existingAa : getApplicationElement().getAttributes("*")) {
                if (existingAa != null && !existingAa.getName().equals(this.aaName)
                        && existingAa.getName().toLowerCase().equals(baseAttr.getName().toLowerCase())) {
                    throw new AoException(ErrorCode.AO_DUPLICATE_BASE_ATTRIBUTE, SeverityFlag.ERROR, 0,
                                          "Duplicate base attribute '" + existingAa.getName() + "'");
                }
            }

            // set new datatype
            setDataType(baseAttr.getDataType());
            this.obligatory = baseAttr.isObligatory();
            this.unique = baseAttr.isUnique();
            if (dataType == DataType.DT_ENUM || dataType == DataType.DS_ENUM) {
                this.enumerationDefinition = baseAttr.getEnumerationDefinition();
            } else {
                this.enumerationDefinition = null;
            }

            this.baseAttribute = baseAttr;
            this.atfxCache.setAaNameForBaName(aid, baseAttr.getName(), _this());
        } else {
            this.baseAttribute = null;
            // TODO: update cache
            // this.atfxCache.setAaNameForBaName(aid, aaName, null);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#getDataType()
     */
    public DataType getDataType() throws AoException {
        return this.dataType;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#setDataType(org.asam.ods.DataType)
     */
    public void setDataType(DataType aaDataType) throws AoException {
        if (aaDataType == null) {
            throw new AoException(ErrorCode.AO_BAD_PARAMETER, SeverityFlag.ERROR, 0, "aaDataType must not be null");
        }
        if (this.dataType == aaDataType) {
            return;
        }
        // check if base attribute is set
        if (getBaseAttribute() != null) {
            throw new AoException(ErrorCode.AO_IS_BASE_ATTRIBUTE, SeverityFlag.ERROR, 0,
                                  "Changing the datatype of an attribute derived from a base attribute is not allowed");
        }
        // check for existing instance values
        InstanceElementIterator iter = getApplicationElement().getInstances("*");
        for (InstanceElement ie : iter.nextN(iter.getCount())) {
            if (ie.getValue(getName()).value.flag != 0) {
                throw new AoException(ErrorCode.AO_HAS_INSTANCES, SeverityFlag.ERROR, 0,
                                      "Changing the datatype for application attribute with existing instances is not allowed");
            }
        }
        iter.destroy();
        // change data type
        this.dataType = aaDataType;
        // set default length
        if (dataType == DataType.DT_STRING || dataType == DataType.DS_STRING
                || dataType == DataType.DT_EXTERNALREFERENCE || dataType == DataType.DS_EXTERNALREFERENCE) {
            setLength(254);
        } else if (dataType == DataType.DT_DATE || dataType == DataType.DS_DATE) {
            setLength(30);
        } else {
            setLength(1);
        }
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#getLength()
     */
    public int getLength() throws AoException {
        return this.length;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#setLength(int)
     */
    public void setLength(int aaLength) throws AoException {
        this.length = aaLength;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#isUnique()
     */
    public boolean isUnique() throws AoException {
        return this.unique;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#setIsUnique(boolean)
     */
    public void setIsUnique(boolean aaIsUnique) throws AoException {
        this.unique = aaIsUnique;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#isObligatory()
     */
    public boolean isObligatory() throws AoException {
        // if the base attribute is obligatory, always return true
        if (this.baseAttribute != null) {
            String baseAttributeName = this.baseAttribute.getName();
            String baseElementName = this.baseAttribute.getBaseElement().getType();
            if (obligatoryAttributes.containsKey(baseAttributeName)) {
                if (obligatoryAttributes.get(baseAttributeName) == null) {
                    // attribute is obligatory in every application element
                    return true;
                } else if (obligatoryAttributes.get(baseAttributeName).contains(baseElementName)) {
                    // attribute is obligatory in the application element this attribute belongs to
                    return true;
                }
            }
        }
        return this.obligatory;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#setIsObligatory(boolean)
     */
    public void setIsObligatory(boolean aaIsObligatory) throws AoException {
        if (this.obligatory == aaIsObligatory) {
            return;
        }

        // obligatory flag of obligatory base attribute is not reducable
        boolean mustBeObligatory = false;
        if (this.baseAttribute != null) {
            String baseAttributeName = this.baseAttribute.getName();
            String baseElementName = this.baseAttribute.getBaseElement().getType();
            if (obligatoryAttributes.containsKey(baseAttributeName)) {
                if (obligatoryAttributes.get(baseAttributeName) == null) {
                    // attribute is obligatory in every application element
                    mustBeObligatory = true;
                } else if (obligatoryAttributes.get(baseAttributeName).contains(baseElementName)) {
                    // attribute is obligatory in the application element this attribute belongs to
                    mustBeObligatory = true;
                }
            }
        }
        if (mustBeObligatory && !aaIsObligatory) {
            throw new AoException(ErrorCode.AO_IS_BASE_ATTRIBUTE, SeverityFlag.ERROR, 0,
                                  "Unable to set obligatory flag to 'false' for application attribute derived from an obligatory base attribute.");
        }
        this.obligatory = aaIsObligatory;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#isAutogenerated()
     */
    public boolean isAutogenerated() throws AoException {
        return this.autogenerated;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#setIsAutogenerated(boolean)
     */
    public void setIsAutogenerated(boolean isAutogenerated) throws AoException {
        this.autogenerated = isAutogenerated;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#getEnumerationDefinition()
     */
    public EnumerationDefinition getEnumerationDefinition() throws AoException {
        if (this.enumerationDefinition == null) {
            throw new AoException(ErrorCode.AO_INVALID_DATATYPE, SeverityFlag.ERROR, 0, "Invalid datatype");
        }
        return this.enumerationDefinition;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#setEnumerationDefinition(org.asam.ods.EnumerationDefinition)
     */
    public void setEnumerationDefinition(EnumerationDefinition enumDef) throws AoException {
        // check data type
        if (dataType != DataType.DT_ENUM && dataType != DataType.DS_ENUM) {
            throw new AoException(ErrorCode.AO_INVALID_DATATYPE, SeverityFlag.ERROR, 0,
                                  "Datatype is not DT_ENUM or DS_ENUM");
        }
        // check for equality
        if (enumDef != null && this.enumerationDefinition != null
                && enumDef.getName().equals(this.enumerationDefinition.getName())) {
            return;
        }
        if (this.baseAttribute != null) {
            throw new AoException(ErrorCode.AO_BAD_PARAMETER, SeverityFlag.ERROR, 0,
                                  "changing the enumeration definition not allowed for application attribute derived from base attribute");
        }
        this.enumerationDefinition = enumDef;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#hasUnit()
     */
    public boolean hasUnit() throws AoException {
        return this.unitId > 0;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#withUnit(boolean)
     */
    public void withUnit(boolean withUnit) throws AoException {
    // nothing to do
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#getUnit()
     */
    public T_LONGLONG getUnit() throws AoException {
        return ODSHelper.asODSLongLong(this.unitId);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#setUnit(org.asam.ods.T_LONGLONG)
     */
    public void setUnit(T_LONGLONG aaUnit) throws AoException {
        // TODO: check if unit instance exists
        this.unitId = ODSHelper.asJLong(aaUnit);
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#setRights(org.asam.ods.InstanceElement, int,
     *      org.asam.ods.RightsSet)
     */
    public void setRights(InstanceElement usergroup, int rights, RightsSet set) throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Method 'setRights' not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#getRights()
     */
    public ACL[] getRights() throws AoException {
        throw new AoException(ErrorCode.AO_NOT_IMPLEMENTED, SeverityFlag.ERROR, 0, "Method 'getRights' not implemented");
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#hasValueFlag()
     */
    @Deprecated
    public boolean hasValueFlag() throws AoException {
        return this.valueFlag;
    }

    /**
     * {@inheritDoc}
     * 
     * @see org.asam.ods.ApplicationAttributeOperations#withValueFlag(boolean)
     */
    @Deprecated
    public void withValueFlag(boolean withValueFlag) throws AoException {
        this.valueFlag = withValueFlag;
    }

}
