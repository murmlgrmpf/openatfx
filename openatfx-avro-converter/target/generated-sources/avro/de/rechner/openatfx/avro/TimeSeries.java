/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package de.rechner.openatfx.avro;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class TimeSeries extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"TimeSeries\",\"namespace\":\"de.rechner.openatfx.avro\",\"fields\":[{\"name\":\"name\",\"type\":[\"null\",\"string\"]},{\"name\":\"mimetype\",\"type\":[\"null\",\"string\"]},{\"name\":\"unit\",\"type\":[\"null\",\"string\"]},{\"name\":\"quantity\",\"type\":[\"null\",\"string\"]},{\"name\":\"vals\",\"type\":{\"type\":\"array\",\"items\":{\"type\":\"record\",\"name\":\"TimeSeriesValue\",\"fields\":[{\"name\":\"timestamp\",\"type\":[\"null\",\"long\"]},{\"name\":\"relTime\",\"type\":[\"null\",\"long\"]},{\"name\":\"numVal\",\"type\":[\"null\",\"double\"]},{\"name\":\"txtVal\",\"type\":[\"null\",\"string\"]}]}}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.CharSequence name;
  @Deprecated public java.lang.CharSequence mimetype;
  @Deprecated public java.lang.CharSequence unit;
  @Deprecated public java.lang.CharSequence quantity;
  @Deprecated public java.util.List<de.rechner.openatfx.avro.TimeSeriesValue> vals;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public TimeSeries() {}

  /**
   * All-args constructor.
   */
  public TimeSeries(java.lang.CharSequence name, java.lang.CharSequence mimetype, java.lang.CharSequence unit, java.lang.CharSequence quantity, java.util.List<de.rechner.openatfx.avro.TimeSeriesValue> vals) {
    this.name = name;
    this.mimetype = mimetype;
    this.unit = unit;
    this.quantity = quantity;
    this.vals = vals;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return name;
    case 1: return mimetype;
    case 2: return unit;
    case 3: return quantity;
    case 4: return vals;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: name = (java.lang.CharSequence)value$; break;
    case 1: mimetype = (java.lang.CharSequence)value$; break;
    case 2: unit = (java.lang.CharSequence)value$; break;
    case 3: quantity = (java.lang.CharSequence)value$; break;
    case 4: vals = (java.util.List<de.rechner.openatfx.avro.TimeSeriesValue>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'name' field.
   */
  public java.lang.CharSequence getName() {
    return name;
  }

  /**
   * Sets the value of the 'name' field.
   * @param value the value to set.
   */
  public void setName(java.lang.CharSequence value) {
    this.name = value;
  }

  /**
   * Gets the value of the 'mimetype' field.
   */
  public java.lang.CharSequence getMimetype() {
    return mimetype;
  }

  /**
   * Sets the value of the 'mimetype' field.
   * @param value the value to set.
   */
  public void setMimetype(java.lang.CharSequence value) {
    this.mimetype = value;
  }

  /**
   * Gets the value of the 'unit' field.
   */
  public java.lang.CharSequence getUnit() {
    return unit;
  }

  /**
   * Sets the value of the 'unit' field.
   * @param value the value to set.
   */
  public void setUnit(java.lang.CharSequence value) {
    this.unit = value;
  }

  /**
   * Gets the value of the 'quantity' field.
   */
  public java.lang.CharSequence getQuantity() {
    return quantity;
  }

  /**
   * Sets the value of the 'quantity' field.
   * @param value the value to set.
   */
  public void setQuantity(java.lang.CharSequence value) {
    this.quantity = value;
  }

  /**
   * Gets the value of the 'vals' field.
   */
  public java.util.List<de.rechner.openatfx.avro.TimeSeriesValue> getVals() {
    return vals;
  }

  /**
   * Sets the value of the 'vals' field.
   * @param value the value to set.
   */
  public void setVals(java.util.List<de.rechner.openatfx.avro.TimeSeriesValue> value) {
    this.vals = value;
  }

  /** Creates a new TimeSeries RecordBuilder */
  public static de.rechner.openatfx.avro.TimeSeries.Builder newBuilder() {
    return new de.rechner.openatfx.avro.TimeSeries.Builder();
  }
  
  /** Creates a new TimeSeries RecordBuilder by copying an existing Builder */
  public static de.rechner.openatfx.avro.TimeSeries.Builder newBuilder(de.rechner.openatfx.avro.TimeSeries.Builder other) {
    return new de.rechner.openatfx.avro.TimeSeries.Builder(other);
  }
  
  /** Creates a new TimeSeries RecordBuilder by copying an existing TimeSeries instance */
  public static de.rechner.openatfx.avro.TimeSeries.Builder newBuilder(de.rechner.openatfx.avro.TimeSeries other) {
    return new de.rechner.openatfx.avro.TimeSeries.Builder(other);
  }
  
  /**
   * RecordBuilder for TimeSeries instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<TimeSeries>
    implements org.apache.avro.data.RecordBuilder<TimeSeries> {

    private java.lang.CharSequence name;
    private java.lang.CharSequence mimetype;
    private java.lang.CharSequence unit;
    private java.lang.CharSequence quantity;
    private java.util.List<de.rechner.openatfx.avro.TimeSeriesValue> vals;

    /** Creates a new Builder */
    private Builder() {
      super(de.rechner.openatfx.avro.TimeSeries.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(de.rechner.openatfx.avro.TimeSeries.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.name)) {
        this.name = data().deepCopy(fields()[0].schema(), other.name);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.mimetype)) {
        this.mimetype = data().deepCopy(fields()[1].schema(), other.mimetype);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.unit)) {
        this.unit = data().deepCopy(fields()[2].schema(), other.unit);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.quantity)) {
        this.quantity = data().deepCopy(fields()[3].schema(), other.quantity);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.vals)) {
        this.vals = data().deepCopy(fields()[4].schema(), other.vals);
        fieldSetFlags()[4] = true;
      }
    }
    
    /** Creates a Builder by copying an existing TimeSeries instance */
    private Builder(de.rechner.openatfx.avro.TimeSeries other) {
            super(de.rechner.openatfx.avro.TimeSeries.SCHEMA$);
      if (isValidValue(fields()[0], other.name)) {
        this.name = data().deepCopy(fields()[0].schema(), other.name);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.mimetype)) {
        this.mimetype = data().deepCopy(fields()[1].schema(), other.mimetype);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.unit)) {
        this.unit = data().deepCopy(fields()[2].schema(), other.unit);
        fieldSetFlags()[2] = true;
      }
      if (isValidValue(fields()[3], other.quantity)) {
        this.quantity = data().deepCopy(fields()[3].schema(), other.quantity);
        fieldSetFlags()[3] = true;
      }
      if (isValidValue(fields()[4], other.vals)) {
        this.vals = data().deepCopy(fields()[4].schema(), other.vals);
        fieldSetFlags()[4] = true;
      }
    }

    /** Gets the value of the 'name' field */
    public java.lang.CharSequence getName() {
      return name;
    }
    
    /** Sets the value of the 'name' field */
    public de.rechner.openatfx.avro.TimeSeries.Builder setName(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.name = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'name' field has been set */
    public boolean hasName() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'name' field */
    public de.rechner.openatfx.avro.TimeSeries.Builder clearName() {
      name = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'mimetype' field */
    public java.lang.CharSequence getMimetype() {
      return mimetype;
    }
    
    /** Sets the value of the 'mimetype' field */
    public de.rechner.openatfx.avro.TimeSeries.Builder setMimetype(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.mimetype = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'mimetype' field has been set */
    public boolean hasMimetype() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'mimetype' field */
    public de.rechner.openatfx.avro.TimeSeries.Builder clearMimetype() {
      mimetype = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'unit' field */
    public java.lang.CharSequence getUnit() {
      return unit;
    }
    
    /** Sets the value of the 'unit' field */
    public de.rechner.openatfx.avro.TimeSeries.Builder setUnit(java.lang.CharSequence value) {
      validate(fields()[2], value);
      this.unit = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'unit' field has been set */
    public boolean hasUnit() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'unit' field */
    public de.rechner.openatfx.avro.TimeSeries.Builder clearUnit() {
      unit = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    /** Gets the value of the 'quantity' field */
    public java.lang.CharSequence getQuantity() {
      return quantity;
    }
    
    /** Sets the value of the 'quantity' field */
    public de.rechner.openatfx.avro.TimeSeries.Builder setQuantity(java.lang.CharSequence value) {
      validate(fields()[3], value);
      this.quantity = value;
      fieldSetFlags()[3] = true;
      return this; 
    }
    
    /** Checks whether the 'quantity' field has been set */
    public boolean hasQuantity() {
      return fieldSetFlags()[3];
    }
    
    /** Clears the value of the 'quantity' field */
    public de.rechner.openatfx.avro.TimeSeries.Builder clearQuantity() {
      quantity = null;
      fieldSetFlags()[3] = false;
      return this;
    }

    /** Gets the value of the 'vals' field */
    public java.util.List<de.rechner.openatfx.avro.TimeSeriesValue> getVals() {
      return vals;
    }
    
    /** Sets the value of the 'vals' field */
    public de.rechner.openatfx.avro.TimeSeries.Builder setVals(java.util.List<de.rechner.openatfx.avro.TimeSeriesValue> value) {
      validate(fields()[4], value);
      this.vals = value;
      fieldSetFlags()[4] = true;
      return this; 
    }
    
    /** Checks whether the 'vals' field has been set */
    public boolean hasVals() {
      return fieldSetFlags()[4];
    }
    
    /** Clears the value of the 'vals' field */
    public de.rechner.openatfx.avro.TimeSeries.Builder clearVals() {
      vals = null;
      fieldSetFlags()[4] = false;
      return this;
    }

    @Override
    public TimeSeries build() {
      try {
        TimeSeries record = new TimeSeries();
        record.name = fieldSetFlags()[0] ? this.name : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.mimetype = fieldSetFlags()[1] ? this.mimetype : (java.lang.CharSequence) defaultValue(fields()[1]);
        record.unit = fieldSetFlags()[2] ? this.unit : (java.lang.CharSequence) defaultValue(fields()[2]);
        record.quantity = fieldSetFlags()[3] ? this.quantity : (java.lang.CharSequence) defaultValue(fields()[3]);
        record.vals = fieldSetFlags()[4] ? this.vals : (java.util.List<de.rechner.openatfx.avro.TimeSeriesValue>) defaultValue(fields()[4]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}