package au.org.ala.pipelines.util;

import lombok.extern.slf4j.Slf4j;
import org.gbif.common.parsers.date.MultiinputTemporalParser;
import org.gbif.common.parsers.utils.DelimiterUtils;
import org.gbif.pipelines.core.functions.SerializableFunction;

import java.time.LocalDate;
import java.time.ZonedDateTime;
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
        TemporalAccessor from =
                temporalParser.parseRecordedDate(null, null, null, rawPeriod[0], null).getPayload();
        TemporalAccessor to =
                temporalParser.parseRecordedDate(null, null, null, rawPeriod[1], null).getPayload();

        if (from.getClass().equals(LocalDate.class) && to.getClass().equals(ZonedDateTime.class)) {
            LocalDate fromDate = (LocalDate)from;
            ZonedDateTime toDatetime = (ZonedDateTime)to;

//            log.warn("1: " + fromDate.atStartOfDay(toDatetime.getZone()));
//            log.warn("2: " + fromDate.atTime(LocalTime.MIDNIGHT).atZone(toDatetime.getZone()));
//            log.warn("3: " + ZonedDateTime.of(fromDate, LocalTime.MIDNIGHT, toDatetime.getZone()));

            ZonedDateTime fromDatetime = fromDate.atStartOfDay(toDatetime.getZone());
            String newEventDate = fromDatetime + "/" + rawPeriod[1];
            log.info(eventDate + " => " + newEventDate);

            return newEventDate;
        }

        return eventDate;
    }
}
