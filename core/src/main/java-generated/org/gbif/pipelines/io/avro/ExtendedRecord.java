/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package org.gbif.pipelines.io.avro;  
@SuppressWarnings("all")
/** A container for an extended DwC record (core plus extension data for a single record) */
@org.apache.avro.specific.AvroGenerated
public class ExtendedRecord extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"ExtendedRecord\",\"namespace\":\"org.gbif.pipelines.io.avro\",\"doc\":\"A container for an extended DwC record (core plus extension data for a single record)\",\"fields\":[{\"name\":\"id\",\"type\":\"string\",\"doc\":\"The record id\"},{\"name\":\"coreTerms\",\"type\":{\"type\":\"map\",\"values\":\"string\"},\"doc\":\"The core record terms\",\"default\":{}},{\"name\":\"extensions\",\"type\":{\"type\":\"map\",\"values\":{\"type\":\"array\",\"items\":{\"type\":\"map\",\"values\":[\"null\",\"string\"],\"default\":{}},\"default\":[]}},\"doc\":\"The extensions records\",\"default\":{}}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  /** The record id */
  @Deprecated public java.lang.CharSequence id;
  /** The core record terms */
  @Deprecated public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> coreTerms;
  /** The extensions records */
  @Deprecated public java.util.Map<java.lang.CharSequence,java.util.List<java.util.Map<java.lang.CharSequence,java.lang.CharSequence>>> extensions;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public ExtendedRecord() {}

  /**
   * All-args constructor.
   */
  public ExtendedRecord(java.lang.CharSequence id, java.util.Map<java.lang.CharSequence,java.lang.CharSequence> coreTerms, java.util.Map<java.lang.CharSequence,java.util.List<java.util.Map<java.lang.CharSequence,java.lang.CharSequence>>> extensions) {
    this.id = id;
    this.coreTerms = coreTerms;
    this.extensions = extensions;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return id;
    case 1: return coreTerms;
    case 2: return extensions;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: id = (java.lang.CharSequence)value$; break;
    case 1: coreTerms = (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>)value$; break;
    case 2: extensions = (java.util.Map<java.lang.CharSequence,java.util.List<java.util.Map<java.lang.CharSequence,java.lang.CharSequence>>>)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'id' field.
   * The record id   */
  public java.lang.CharSequence getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field.
   * The record id   * @param value the value to set.
   */
  public void setId(java.lang.CharSequence value) {
    this.id = value;
  }

  /**
   * Gets the value of the 'coreTerms' field.
   * The core record terms   */
  public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getCoreTerms() {
    return coreTerms;
  }

  /**
   * Sets the value of the 'coreTerms' field.
   * The core record terms   * @param value the value to set.
   */
  public void setCoreTerms(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
    this.coreTerms = value;
  }

  /**
   * Gets the value of the 'extensions' field.
   * The extensions records   */
  public java.util.Map<java.lang.CharSequence,java.util.List<java.util.Map<java.lang.CharSequence,java.lang.CharSequence>>> getExtensions() {
    return extensions;
  }

  /**
   * Sets the value of the 'extensions' field.
   * The extensions records   * @param value the value to set.
   */
  public void setExtensions(java.util.Map<java.lang.CharSequence,java.util.List<java.util.Map<java.lang.CharSequence,java.lang.CharSequence>>> value) {
    this.extensions = value;
  }

  /** Creates a new ExtendedRecord RecordBuilder */
  public static org.gbif.pipelines.io.avro.ExtendedRecord.Builder newBuilder() {
    return new org.gbif.pipelines.io.avro.ExtendedRecord.Builder();
  }
  
  /** Creates a new ExtendedRecord RecordBuilder by copying an existing Builder */
  public static org.gbif.pipelines.io.avro.ExtendedRecord.Builder newBuilder(org.gbif.pipelines.io.avro.ExtendedRecord.Builder other) {
    return new org.gbif.pipelines.io.avro.ExtendedRecord.Builder(other);
  }
  
  /** Creates a new ExtendedRecord RecordBuilder by copying an existing ExtendedRecord instance */
  public static org.gbif.pipelines.io.avro.ExtendedRecord.Builder newBuilder(org.gbif.pipelines.io.avro.ExtendedRecord other) {
    return new org.gbif.pipelines.io.avro.ExtendedRecord.Builder(other);
  }
  
  /**
   * RecordBuilder for ExtendedRecord instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<ExtendedRecord>
    implements org.apache.avro.data.RecordBuilder<ExtendedRecord> {

    private java.lang.CharSequence id;
    private java.util.Map<java.lang.CharSequence,java.lang.CharSequence> coreTerms;
    private java.util.Map<java.lang.CharSequence,java.util.List<java.util.Map<java.lang.CharSequence,java.lang.CharSequence>>> extensions;

    /** Creates a new Builder */
    private Builder() {
      super(org.gbif.pipelines.io.avro.ExtendedRecord.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(org.gbif.pipelines.io.avro.ExtendedRecord.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.coreTerms)) {
        this.coreTerms = data().deepCopy(fields()[1].schema(), other.coreTerms);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.extensions)) {
        this.extensions = data().deepCopy(fields()[2].schema(), other.extensions);
        fieldSetFlags()[2] = true;
      }
    }
    
    /** Creates a Builder by copying an existing ExtendedRecord instance */
    private Builder(org.gbif.pipelines.io.avro.ExtendedRecord other) {
            super(org.gbif.pipelines.io.avro.ExtendedRecord.SCHEMA$);
      if (isValidValue(fields()[0], other.id)) {
        this.id = data().deepCopy(fields()[0].schema(), other.id);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.coreTerms)) {
        this.coreTerms = data().deepCopy(fields()[1].schema(), other.coreTerms);
        fieldSetFlags()[1] = true;
      }
      if (isValidValue(fields()[2], other.extensions)) {
        this.extensions = data().deepCopy(fields()[2].schema(), other.extensions);
        fieldSetFlags()[2] = true;
      }
    }

    /** Gets the value of the 'id' field */
    public java.lang.CharSequence getId() {
      return id;
    }
    
    /** Sets the value of the 'id' field */
    public org.gbif.pipelines.io.avro.ExtendedRecord.Builder setId(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.id = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'id' field has been set */
    public boolean hasId() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'id' field */
    public org.gbif.pipelines.io.avro.ExtendedRecord.Builder clearId() {
      id = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'coreTerms' field */
    public java.util.Map<java.lang.CharSequence,java.lang.CharSequence> getCoreTerms() {
      return coreTerms;
    }
    
    /** Sets the value of the 'coreTerms' field */
    public org.gbif.pipelines.io.avro.ExtendedRecord.Builder setCoreTerms(java.util.Map<java.lang.CharSequence,java.lang.CharSequence> value) {
      validate(fields()[1], value);
      this.coreTerms = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'coreTerms' field has been set */
    public boolean hasCoreTerms() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'coreTerms' field */
    public org.gbif.pipelines.io.avro.ExtendedRecord.Builder clearCoreTerms() {
      coreTerms = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    /** Gets the value of the 'extensions' field */
    public java.util.Map<java.lang.CharSequence,java.util.List<java.util.Map<java.lang.CharSequence,java.lang.CharSequence>>> getExtensions() {
      return extensions;
    }
    
    /** Sets the value of the 'extensions' field */
    public org.gbif.pipelines.io.avro.ExtendedRecord.Builder setExtensions(java.util.Map<java.lang.CharSequence,java.util.List<java.util.Map<java.lang.CharSequence,java.lang.CharSequence>>> value) {
      validate(fields()[2], value);
      this.extensions = value;
      fieldSetFlags()[2] = true;
      return this; 
    }
    
    /** Checks whether the 'extensions' field has been set */
    public boolean hasExtensions() {
      return fieldSetFlags()[2];
    }
    
    /** Clears the value of the 'extensions' field */
    public org.gbif.pipelines.io.avro.ExtendedRecord.Builder clearExtensions() {
      extensions = null;
      fieldSetFlags()[2] = false;
      return this;
    }

    @Override
    public ExtendedRecord build() {
      try {
        ExtendedRecord record = new ExtendedRecord();
        record.id = fieldSetFlags()[0] ? this.id : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.coreTerms = fieldSetFlags()[1] ? this.coreTerms : (java.util.Map<java.lang.CharSequence,java.lang.CharSequence>) defaultValue(fields()[1]);
        record.extensions = fieldSetFlags()[2] ? this.extensions : (java.util.Map<java.lang.CharSequence,java.util.List<java.util.Map<java.lang.CharSequence,java.lang.CharSequence>>>) defaultValue(fields()[2]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
