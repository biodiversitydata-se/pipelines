package au.org.ala.pipelines.util;

import lombok.extern.slf4j.Slf4j;
import org.gbif.common.parsers.core.OccurrenceParseResult;
import org.gbif.common.parsers.date.MultiinputTemporalParser;
import org.gbif.common.parsers.utils.DelimiterUtils;
import org.gbif.pipelines.core.functions.SerializableFunction;

import java.time.temporal.TemporalAccessor;

@Slf4j
public class RecordedDateValidator implements SerializableFunction<String, String> {

    private final MultiinputTemporalParser temporalParser;

    public RecordedDateValidator(MultiinputTemporalParser temporalParser) {
        this.temporalParser = temporalParser;
    }

    @Override
    public String apply(String eventDate) {
        String[] rawPeriod = DelimiterUtils.splitPeriod(eventDate);
//        log.warn("rawPeriod0: " + rawPeriod[0]);
//        log.warn("rawPeriod1: " + rawPeriod[1]);

        OccurrenceParseResult<TemporalAccessor> dateRangeOnlyStart = this.temporalParser.parseRecordedDate((String)null, (String)null, (String)null, rawPeriod[0], (String)null);
        OccurrenceParseResult<TemporalAccessor> dateRangeOnlyEnd = this.temporalParser.parseRecordedDate((String)null, (String)null, (String)null, rawPeriod[1], (String)null);

//        log.warn("dateRangeOnlyStart: " + dateRangeOnlyStart.getPayload());
//        log.warn("dateRangeOnlyStart: " + dateRangeOnlyStart.getPayload().getClass());
//        log.warn("dateRangeOnlyEnd: " + dateRangeOnlyEnd.getPayload());
//        log.warn("dateRangeOnlyEnd: " + dateRangeOnlyEnd.getPayload().getClass());

        return eventDate;
    }
}
