/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package org.gbif.pipelines.io.avro;  
@SuppressWarnings("all")
@org.apache.avro.specific.AvroGenerated
public class Nomenclature extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"Nomenclature\",\"namespace\":\"org.gbif.pipelines.io.avro\",\"fields\":[{\"name\":\"source\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null},{\"name\":\"id\",\"type\":[\"null\",{\"type\":\"string\",\"avro.java.string\":\"String\"}],\"default\":null}]}");
  public static org.apache.avro.Schema getClassSchema() { return SCHEMA$; }
  @Deprecated public java.lang.String source;
  @Deprecated public java.lang.String id;

  /**
   * Default constructor.  Note that this does not initialize fields
   * to their default values from the schema.  If that is desired then
   * one should use <code>newBuilder()</code>. 
   */
  public Nomenclature() {}

  /**
   * All-args constructor.
   */
  public Nomenclature(java.lang.String source, java.lang.String id) {
    this.source = source;
    this.id = id;
  }

  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return source;
    case 1: return id;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: source = (java.lang.String)value$; break;
    case 1: id = (java.lang.String)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'source' field.
   */
  public java.lang.String getSource() {
    return source;
  }

  /**
   * Sets the value of the 'source' field.
   * @param value the value to set.
   */
  public void setSource(java.lang.String value) {
    this.source = value;
  }

  /**
   * Gets the value of the 'id' field.
   */
  public java.lang.String getId() {
    return id;
  }

  /**
   * Sets the value of the 'id' field.
   * @param value the value to set.
   */
  public void setId(java.lang.String value) {
    this.id = value;
  }

  /** Creates a new Nomenclature RecordBuilder */
  public static org.gbif.pipelines.io.avro.Nomenclature.Builder newBuilder() {
    return new org.gbif.pipelines.io.avro.Nomenclature.Builder();
  }
  
  /** Creates a new Nomenclature RecordBuilder by copying an existing Builder */
  public static org.gbif.pipelines.io.avro.Nomenclature.Builder newBuilder(org.gbif.pipelines.io.avro.Nomenclature.Builder other) {
    return new org.gbif.pipelines.io.avro.Nomenclature.Builder(other);
  }
  
  /** Creates a new Nomenclature RecordBuilder by copying an existing Nomenclature instance */
  public static org.gbif.pipelines.io.avro.Nomenclature.Builder newBuilder(org.gbif.pipelines.io.avro.Nomenclature other) {
    return new org.gbif.pipelines.io.avro.Nomenclature.Builder(other);
  }
  
  /**
   * RecordBuilder for Nomenclature instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<Nomenclature>
    implements org.apache.avro.data.RecordBuilder<Nomenclature> {

    private java.lang.String source;
    private java.lang.String id;

    /** Creates a new Builder */
    private Builder() {
      super(org.gbif.pipelines.io.avro.Nomenclature.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(org.gbif.pipelines.io.avro.Nomenclature.Builder other) {
      super(other);
      if (isValidValue(fields()[0], other.source)) {
        this.source = data().deepCopy(fields()[0].schema(), other.source);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.id)) {
        this.id = data().deepCopy(fields()[1].schema(), other.id);
        fieldSetFlags()[1] = true;
      }
    }
    
    /** Creates a Builder by copying an existing Nomenclature instance */
    private Builder(org.gbif.pipelines.io.avro.Nomenclature other) {
            super(org.gbif.pipelines.io.avro.Nomenclature.SCHEMA$);
      if (isValidValue(fields()[0], other.source)) {
        this.source = data().deepCopy(fields()[0].schema(), other.source);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.id)) {
        this.id = data().deepCopy(fields()[1].schema(), other.id);
        fieldSetFlags()[1] = true;
      }
    }

    /** Gets the value of the 'source' field */
    public java.lang.String getSource() {
      return source;
    }
    
    /** Sets the value of the 'source' field */
    public org.gbif.pipelines.io.avro.Nomenclature.Builder setSource(java.lang.String value) {
      validate(fields()[0], value);
      this.source = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'source' field has been set */
    public boolean hasSource() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'source' field */
    public org.gbif.pipelines.io.avro.Nomenclature.Builder clearSource() {
      source = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'id' field */
    public java.lang.String getId() {
      return id;
    }
    
    /** Sets the value of the 'id' field */
    public org.gbif.pipelines.io.avro.Nomenclature.Builder setId(java.lang.String value) {
      validate(fields()[1], value);
      this.id = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'id' field has been set */
    public boolean hasId() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'id' field */
    public org.gbif.pipelines.io.avro.Nomenclature.Builder clearId() {
      id = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    public Nomenclature build() {
      try {
        Nomenclature record = new Nomenclature();
        record.source = fieldSetFlags()[0] ? this.source : (java.lang.String) defaultValue(fields()[0]);
        record.id = fieldSetFlags()[1] ? this.id : (java.lang.String) defaultValue(fields()[1]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}
