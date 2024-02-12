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
        String result = eventDate;
        String[] rawPeriod = DelimiterUtils.splitPeriod(eventDate);
        TemporalAccessor from =
                temporalParser.parseRecordedDate(null, null, null, rawPeriod[0], null).getPayload();
        TemporalAccessor to =
                temporalParser.parseRecordedDate(null, null, null, rawPeriod[1], null).getPayload();

        if (from.getClass().equals(LocalDate.class) && to.getClass().equals(ZonedDateTime.class)) {
            LocalDate fromDate = (LocalDate)from;
            ZonedDateTime toDatetime = (ZonedDateTime)to;
            ZonedDateTime fromDatetime = fromDate.atStartOfDay(toDatetime.getZone());
            result = fromDatetime + "/" + rawPeriod[1];
        } else if (from.getClass().equals(ZonedDateTime.class) && to.getClass().equals(LocalDate.class)) {
            ZonedDateTime fromDatetime = (ZonedDateTime)from;
            LocalDate toDate = (LocalDate)to;
            ZonedDateTime toDatetime = toDate.atStartOfDay(fromDatetime.getZone());
            if (toDatetime.isBefore(fromDatetime)) {
                toDatetime = toDatetime.plusDays(1);
            }
            result = rawPeriod[0] + "/" + toDatetime;
        }

        if (!eventDate.equals(result)) {
            log.info(eventDate + " => " + result);
        }
        return result;
    }
}
