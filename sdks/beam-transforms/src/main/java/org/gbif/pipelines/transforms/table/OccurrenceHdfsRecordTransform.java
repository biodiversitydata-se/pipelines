package org.gbif.pipelines.transforms.table;

import static org.apache.parquet.Preconditions.checkNotNull;
import static org.apache.parquet.hadoop.ParquetFileWriter.Mode.OVERWRITE;
import static org.gbif.pipelines.common.PipelinesVariables.Metrics.AVRO_TO_HDFS_COUNT;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Collections;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.beam.sdk.io.FileIO;
import org.apache.beam.sdk.io.hadoop.SerializableConfiguration;
import org.apache.beam.sdk.io.parquet.ParquetIO;
import org.apache.beam.sdk.metrics.Counter;
import org.apache.beam.sdk.metrics.Metrics;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.ParDo.SingleOutput;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.parquet.avro.AvroParquetWriter;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.OutputFile;
import org.apache.parquet.io.PositionOutputStream;
import org.gbif.pipelines.common.PipelinesVariables;
import org.gbif.pipelines.core.converters.MultimediaConverter;
import org.gbif.pipelines.core.converters.OccurrenceHdfsRecordConverter;
import org.gbif.pipelines.io.avro.AudubonRecord;
import org.gbif.pipelines.io.avro.BasicRecord;
import org.gbif.pipelines.io.avro.ClusteringRecord;
import org.gbif.pipelines.io.avro.EventCoreRecord;
import org.gbif.pipelines.io.avro.ExtendedRecord;
import org.gbif.pipelines.io.avro.IdentifierRecord;
import org.gbif.pipelines.io.avro.ImageRecord;
import org.gbif.pipelines.io.avro.LocationRecord;
import org.gbif.pipelines.io.avro.MetadataRecord;
import org.gbif.pipelines.io.avro.MultimediaRecord;
import org.gbif.pipelines.io.avro.OccurrenceHdfsRecord;
import org.gbif.pipelines.io.avro.TaxonRecord;
import org.gbif.pipelines.io.avro.TemporalRecord;
import org.gbif.pipelines.io.avro.grscicoll.GrscicollRecord;

/**
 * Beam level transformation for Occurrence HDFS Downloads Table. The transformation consumes
 * objects, which classes were generated from avro schema files and converts into json string object
 *
 * <p>Example:
 *
 * <p>
 *
 * <pre>{@code
 * final TupleTag<ExtendedRecord> erTag = new TupleTag<ExtendedRecord>() {};
 * final TupleTag<BasicRecord> brTag = new TupleTag<BasicRecord>() {};
 * final TupleTag<TemporalRecord> trTag = new TupleTag<TemporalRecord>() {};
 * final TupleTag<LocationRecord> lrTag = new TupleTag<LocationRecord>() {};
 * final TupleTag<TaxonRecord> txrTag = new TupleTag<TaxonRecord>() {};
 * final TupleTag<GrscicollRecord> txrTag = new TupleTag<GrscicollRecord>() {};
 * final TupleTag<MultimediaRecord> mrTag = new TupleTag<MultimediaRecord>() {};
 * final TupleTag<ImageRecord> irTag = new TupleTag<ImageRecord>() {};
 * final TupleTag<AudubonRecord> arTag = new TupleTag<AudubonRecord>() {};
 * final TupleTag<MeasurementOrFactRecord> mfrTag = new TupleTag<MeasurementOrFactRecord>() {};
 *
 * PCollectionView<MetadataRecord> metadataView = ...
 * PCollection<KV<String, ExtendedRecord>> verbatimCollection = ...
 * PCollection<KV<String, BasicRecord>> basicCollection = ...
 * PCollection<KV<String, TemporalRecord>> temporalCollection = ...
 * PCollection<KV<String, LocationRecord>> locationCollection = ...
 * PCollection<KV<String, TaxonRecord>> taxonCollection = ...
 * PCollection<KV<String, MultimediaRecord>> multimediaCollection = ...
 * PCollection<KV<String, ImageRecord>> imageCollection = ...
 * PCollection<KV<String, AudubonRecord>> audubonCollection = ...
 * PCollection<KV<String, MeasurementOrFactRecord>> measurementCollection = ...
 *
 * OccurrenceHdfsRecord record = OccurrenceHdfsRecordConverter.toOccurrenceHdfsRecord(mdr, br, tr, lr, txr, mmr, mfr, er);
 *
 *  c.output(record);
 * }</pre>
 */
@SuppressWarnings("ConstantConditions")
@Builder
public class OccurrenceHdfsRecordTransform implements Serializable {

  private static final long serialVersionUID = 4605359346756029672L;

  // Core
  @NonNull private final TupleTag<ExtendedRecord> extendedRecordTag;

  @NonNull private final TupleTag<IdentifierRecord> identifierRecordTag;
  @NonNull private final TupleTag<ClusteringRecord> clusteringRecordTag;
  @NonNull private final TupleTag<BasicRecord> basicRecordTag;
  @NonNull private final TupleTag<TemporalRecord> temporalRecordTag;
  @NonNull private final TupleTag<LocationRecord> locationRecordTag;
  @NonNull private final TupleTag<TaxonRecord> taxonRecordTag;
  @NonNull private final TupleTag<GrscicollRecord> grscicollRecordTag;
  @NonNull private final TupleTag<EventCoreRecord> eventCoreRecordTag;

  // Extension
  @NonNull private final TupleTag<MultimediaRecord> multimediaRecordTag;
  @NonNull private final TupleTag<ImageRecord> imageRecordTag;
  @NonNull private final TupleTag<AudubonRecord> audubonRecordTag;

  @NonNull private final PCollectionView<MetadataRecord> metadataView;

  public SingleOutput<KV<String, CoGbkResult>, GenericRecord> converter() {

    DoFn<KV<String, CoGbkResult>, GenericRecord> fn =
        new DoFn<KV<String, CoGbkResult>, GenericRecord>() {

          private final Counter counter =
              Metrics.counter(OccurrenceHdfsRecordTransform.class, AVRO_TO_HDFS_COUNT);

          @ProcessElement
          public void processElement(ProcessContext c) {
            CoGbkResult v = c.element().getValue();
            String k = c.element().getKey();

            // Core
            MetadataRecord mdr = c.sideInput(metadataView);
            IdentifierRecord id = v.getOnly(identifierRecordTag);
            ClusteringRecord cr =
                v.getOnly(clusteringRecordTag, ClusteringRecord.newBuilder().setId(k).build());
            ExtendedRecord er =
                v.getOnly(extendedRecordTag, ExtendedRecord.newBuilder().setId(k).build());
            BasicRecord br = v.getOnly(basicRecordTag, BasicRecord.newBuilder().setId(k).build());
            TemporalRecord tr =
                v.getOnly(temporalRecordTag, TemporalRecord.newBuilder().setId(k).build());
            LocationRecord lr =
                v.getOnly(locationRecordTag, LocationRecord.newBuilder().setId(k).build());
            TaxonRecord txr = v.getOnly(taxonRecordTag, TaxonRecord.newBuilder().setId(k).build());
            GrscicollRecord gr =
                v.getOnly(grscicollRecordTag, GrscicollRecord.newBuilder().setId(k).build());
            // Extension
            MultimediaRecord mr =
                v.getOnly(multimediaRecordTag, MultimediaRecord.newBuilder().setId(k).build());
            ImageRecord ir = v.getOnly(imageRecordTag, ImageRecord.newBuilder().setId(k).build());
            AudubonRecord ar =
                v.getOnly(audubonRecordTag, AudubonRecord.newBuilder().setId(k).build());

            EventCoreRecord eventCoreRecord =
                v.getOnly(eventCoreRecordTag, EventCoreRecord.newBuilder().setId(k).build());

            MultimediaRecord mmr = MultimediaConverter.merge(mr, ir, ar);
            OccurrenceHdfsRecord record =
                OccurrenceHdfsRecordConverter.builder()
                    .basicRecord(br)
                    .identifierRecord(id)
                    .clusteringRecord(cr)
                    .metadataRecord(mdr)
                    .temporalRecord(tr)
                    .locationRecord(lr)
                    .taxonRecord(txr)
                    .grscicollRecord(gr)
                    .multimediaRecord(mmr)
                    .extendedRecord(er)
                    .eventCoreRecord(eventCoreRecord)
                    .build()
                    .convert();

            c.output(record);

            counter.inc();
          }
        };

    return ParDo.of(fn).withSideInputs(metadataView);
  }

  /**
   * Writes {@link OccurrenceHdfsRecord} *.parquet files to path, data will be split into several
   * files, uses Snappy compression codec by default
   *
   * @param toPath path with name to output files, like - directory/name
   */
  public FileIO.Write<Void, GenericRecord> write(
      String toPath, String filePrefix, Integer numShards) {

    FileIO.Write<Void, GenericRecord> write =
        FileIO.<GenericRecord>write()
            .via(
                ParquetIO.sink(OccurrenceHdfsRecord.getClassSchema())
                    .withConfiguration(
                        Collections.singletonMap("parquet.avro.write-old-list-structure", "false"))
                    .withCompressionCodec(CompressionCodecName.SNAPPY))
            .to(toPath)
            .withPrefix(filePrefix)
            .withSuffix(PipelinesVariables.Pipeline.PARQUET_EXTENSION);

    return numShards == null ? write : write.withNumShards(numShards);
  }

  public static class BeamParquetOutputFile implements OutputFile {

    private final OutputStream outputStream;

    BeamParquetOutputFile(OutputStream outputStream) {
      this.outputStream = outputStream;
    }

    @Override
    public PositionOutputStream create(long blockSizeHint) {
      return new BeamOutputStream(outputStream);
    }

    @Override
    public PositionOutputStream createOrOverwrite(long blockSizeHint) {
      return new BeamOutputStream(outputStream);
    }

    @Override
    public boolean supportsBlockSize() {
      return false;
    }

    @Override
    public long defaultBlockSize() {
      return 0;
    }
  }

  private static class BeamOutputStream extends PositionOutputStream {
    private long position = 0;
    private final OutputStream outputStream;

    private BeamOutputStream(OutputStream outputStream) {
      this.outputStream = outputStream;
    }

    @Override
    public long getPos() {
      return position;
    }

    @Override
    public void write(int b) throws IOException {
      position++;
      outputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
      write(b, 0, b.length);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      outputStream.write(b, off, len);
      position += len;
    }

    @Override
    public void flush() throws IOException {
      outputStream.flush();
    }

    @Override
    public void close() throws IOException {
      outputStream.close();
    }
  }

  @Data
  @Builder
  private static class AvroParquetSink implements FileIO.Sink<GenericRecord> {

    private final CompressionCodecName compressionCodec;

    private final Schema schema;

    private final Class<? extends GenericData> modelClass;

    private final @Nullable SerializableConfiguration configuration;

    private final int rowGroupSize;

    private transient @Nullable ParquetWriter<GenericRecord> writer;

    @Override
    public void open(WritableByteChannel channel) throws IOException {
      checkNotNull(schema, "Schema cannot be null");

      OccurrenceHdfsRecordTransform.BeamParquetOutputFile beamParquetOutputFile =
          new OccurrenceHdfsRecordTransform.BeamParquetOutputFile(
              Channels.newOutputStream(channel));

      AvroParquetWriter.Builder<GenericRecord> builder =
          AvroParquetWriter.<GenericRecord>builder(beamParquetOutputFile)
              .withSchema(schema)
              .withCompressionCodec(compressionCodec)
              .withWriteMode(OVERWRITE)
              .withConf(SerializableConfiguration.newConfiguration(configuration))
              .withRowGroupSize(getRowGroupSize());
      if (modelClass != null) {
        try {
          builder.withDataModel(buildModelObject(modelClass));
        } catch (ReflectiveOperationException e) {
          throw new IOException(
              "Couldn't set the specified Avro data model " + modelClass.getName(), e);
        }
      }
      this.writer = builder.build();
    }

    /** Returns a model object created using provided modelClass or null. */
    private static GenericData buildModelObject(@Nullable Class<? extends GenericData> modelClass)
        throws ReflectiveOperationException {
      return (modelClass == null) ? null : (GenericData) modelClass.getMethod("get").invoke(null);
    }

    @Override
    public void write(GenericRecord element) throws IOException {
      checkNotNull(writer, "Writer cannot be null");
      writer.write(element);
    }

    @Override
    public void flush() throws IOException {
      // the only way to completely flush the output is to call writer.close() here
      writer.close();
    }
  }
}
