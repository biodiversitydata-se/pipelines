package org.gbif.pipelines.transforms.table;

import static org.gbif.pipelines.common.PipelinesVariables.Pipeline.AVRO_EXTENSION;

import java.util.Optional;
import java.util.Set;
import lombok.NonNull;
import org.apache.avro.file.CodecFactory;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.ParDo.SingleOutput;
import org.apache.beam.sdk.transforms.SerializableBiFunction;
import org.apache.beam.sdk.transforms.SerializableFunction;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TupleTag;
import org.gbif.pipelines.common.PipelinesVariables.Pipeline.Interpretation.InterpretationType;
import org.gbif.pipelines.io.avro.BasicRecord;
import org.gbif.pipelines.io.avro.ExtendedRecord;
import org.gbif.pipelines.transforms.common.CheckTransforms;

@SuppressWarnings("ConstantConditions")
public abstract class TableTransform<T extends SpecificRecordBase>
    extends DoFn<KV<String, CoGbkResult>, T> {

  private static final CodecFactory BASE_CODEC = CodecFactory.snappyCodec();

  @NonNull private final InterpretationType recordType;

  @NonNull private final Class<T> clazz;

  @NonNull private final SerializableBiFunction<BasicRecord, ExtendedRecord, Optional<T>> convertFn;

  @NonNull private TupleTag<ExtendedRecord> extendedRecordTag;

  @NonNull private TupleTag<BasicRecord> basicRecordTag;

  @NonNull private SerializableFunction<InterpretationType, String> pathFn;

  @NonNull private Integer numShards;

  @NonNull private Set<String> types;

  private final Counter counter;

  public TableTransform(
      Class<T> clazz,
      InterpretationType recordType,
      String counterNamespace,
      String counterName,
      SerializableBiFunction<BasicRecord, ExtendedRecord, Optional<T>> convertFn) {
    this.clazz = clazz;
    this.recordType = recordType;
    this.counter = Metrics.counter(counterNamespace, counterName);
    this.convertFn = convertFn;
  }

  public TableTransform<T> setExtendedRecordTag(TupleTag<ExtendedRecord> extendedRecordTag) {
    this.extendedRecordTag = extendedRecordTag;
    return this;
  }

  public TableTransform<T> setBasicRecordTag(TupleTag<BasicRecord> basicRecordTag) {
    this.basicRecordTag = basicRecordTag;
    return this;
  }

  public TableTransform<T> setPathFn(SerializableFunction<InterpretationType, String> pathFn) {
    this.pathFn = pathFn;
    return this;
  }

  public TableTransform<T> setNumShards(Integer numShards) {
    this.numShards = numShards;
    return this;
  }

  public TableTransform<T> setTypes(Set<String> types) {
    this.types = types;
    return this;
  }

  public Optional<PCollection<KV<String, CoGbkResult>>> check(
      PCollection<KV<String, CoGbkResult>> pCollection) {
    return CheckTransforms.checkRecordType(types, recordType)
        ? Optional.of(pCollection)
        : Optional.empty();
  }

  public void write(PCollection<KV<String, CoGbkResult>> pCollection) {
    if (CheckTransforms.checkRecordType(types, recordType)) {
      pCollection
          .apply("Convert to " + recordType.name(), this.convert())
          .apply("Write " + recordType.name(), this.write());
    }
  }

  public AvroIO.Write<T> write() {

    String toPath = pathFn.apply(recordType);
    AvroIO.Write<T> write =
        AvroIO.write(clazz).to(toPath).withSuffix(AVRO_EXTENSION).withCodec(BASE_CODEC);

    if (numShards == null || numShards <= 0) {
      return write;
    } else {
      int shards = -Math.floorDiv(-numShards, 2);
      return write.withNumShards(shards);
    }
  }

  public SingleOutput<KV<String, CoGbkResult>, T> convert() {
    return ParDo.of(this);
  }

  @ProcessElement
  public void processElement(ProcessContext c) {
    CoGbkResult v = c.element().getValue();
    String k = c.element().getKey();

    ExtendedRecord er = v.getOnly(extendedRecordTag, ExtendedRecord.newBuilder().setId(k).build());

    BasicRecord br = v.getOnly(basicRecordTag, BasicRecord.newBuilder().setId(k).build());

    convertFn
        .apply(br, er)
        .ifPresent(
            record -> {
              c.output(record);
              counter.inc();
            });
  }
}
