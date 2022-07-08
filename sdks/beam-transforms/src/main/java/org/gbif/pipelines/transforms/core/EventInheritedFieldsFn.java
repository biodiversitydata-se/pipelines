package org.gbif.pipelines.transforms.core;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.gbif.pipelines.io.avro.EventCoreRecord;
import org.gbif.pipelines.io.avro.json.EventInheritedRecord;

import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.values.TupleTag;

import lombok.Data;

@Data
public class EventInheritedFieldsFn
    extends Combine.CombineFn<EventCoreRecord, EventInheritedFieldsFn.Accum, EventInheritedRecord> {

  private static final TupleTag<EventCoreRecord> TAG = new TupleTag<EventCoreRecord>() {};

  @Data
  public static class Accum implements Serializable {

    private Map<String, EventInheritedFields> recordsMap = new HashMap<>();
    private Set<String> recordsWithChildren = new HashSet<>();

    public Accum acc(Set<EventCoreRecord> records) {
      records.forEach(r -> acc(EventInheritedFields.from(r)));
      return this;
    }

    public Accum accInheritedFields(Set<EventInheritedFields> records) {
      records.forEach(this::acc);
      return this;
    }

    public Accum acc(EventInheritedFields r) {
      recordsMap.put(r.getId(), r);
      Optional.ofNullable(r.getParentEventID()).ifPresent(recordsWithChildren::add);
      return this;
    }

    private List<String> getEventTypes() {
      return recordsMap.values().stream()
          .map(EventInheritedFields::getEventType)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }

    private EventInheritedRecord getLeafRecord() {
      ArrayDeque<String> allRecords = new ArrayDeque<>(recordsMap.keySet());
      allRecords.removeAll(recordsWithChildren);
      EventInheritedFields leaf = recordsMap.get(allRecords.peek());
      return setParentValue(
              EventInheritedRecord.newBuilder().setId(leaf.getId()), leaf.getParentEventID(), false)
          .build();
    }

    public EventInheritedRecord toLeafChild() {
      EventInheritedRecord inheritedRecord = getLeafRecord();
      List<String> eventTypes = getEventTypes();
      if (!eventTypes.isEmpty()) {
        inheritedRecord.setEventType(eventTypes);
      }
      return inheritedRecord;
    }

    private EventInheritedRecord.Builder setParentValue(
        EventInheritedRecord.Builder builder, String parentId, boolean assigned) {
      if (assigned || parentId == null) {
        return builder;
      }

      EventInheritedFields parent = recordsMap.get(parentId);

      if (parent.getLocationID() != null) {
        builder.setLocationID(parent.getLocationID());
        assigned = true;
      }

      return setParentValue(builder, parent.getParentEventID(), assigned);
    }
  }

  @Override
  public Accum createAccumulator() {
    return new Accum();
  }

  @Override
  public Accum addInput(Accum mutableAccumulator, EventCoreRecord input) {
    return mutableAccumulator.acc(EventInheritedFields.from(input));
  }

  @Override
  public Accum mergeAccumulators(Iterable<Accum> accumulators) {
    return StreamSupport.stream(accumulators.spliterator(), false)
        .reduce(
            new Accum(),
            (acc1, acc2) ->
                new Accum()
                    .accInheritedFields(new HashSet<>(acc1.getRecordsMap().values()))
                    .accInheritedFields(new HashSet<>(acc2.getRecordsMap().values())));
  }

  @Override
  public EventInheritedRecord extractOutput(Accum accumulator) {
    return accumulator.toLeafChild();
  }

  public static TupleTag<EventCoreRecord> tag() {
    return TAG;
  }

  @Data
  public static class EventInheritedFields implements Serializable {

    private String id;
    private String parentEventID;
    private String locationID;
    private String eventType;

    public static EventInheritedFields from(EventCoreRecord eventCoreRecord) {
      EventInheritedFields eif = new EventInheritedFields();
      eif.id = eventCoreRecord.getId();
      eif.parentEventID = eventCoreRecord.getParentEventID();
      eif.locationID = eventCoreRecord.getLocationID();
      if (eventCoreRecord.getEventType() != null) {
        eif.eventType = eventCoreRecord.getEventType().getConcept();
      }
      return eif;
    }
  }
}
