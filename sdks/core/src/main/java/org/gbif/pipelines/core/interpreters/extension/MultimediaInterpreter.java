package org.gbif.pipelines.core.interpreters.extension;

import java.net.URI;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.gbif.api.vocabulary.Extension;
import org.gbif.api.vocabulary.License;
import org.gbif.common.parsers.LicenseParser;
import org.gbif.common.parsers.LicenseUriParser;
import org.gbif.common.parsers.MediaParser;
import org.gbif.common.parsers.UrlParser;
import org.gbif.common.parsers.core.ParseResult;
import org.gbif.dwc.terms.DcTerm;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.pipelines.core.ExtensionInterpretation;
import org.gbif.pipelines.core.ExtensionInterpretation.Result;
import org.gbif.pipelines.core.ExtensionInterpretation.TargetHandler;
import org.gbif.pipelines.io.avro.ExtendedRecord;
import org.gbif.pipelines.io.avro.MediaType;
import org.gbif.pipelines.io.avro.Multimedia;
import org.gbif.pipelines.io.avro.MultimediaRecord;
import org.gbif.pipelines.parsers.parsers.temporal.ParsedTemporal;
import org.gbif.pipelines.parsers.parsers.temporal.TemporalParser;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static org.gbif.api.vocabulary.OccurrenceIssue.MULTIMEDIA_DATE_INVALID;
import static org.gbif.api.vocabulary.OccurrenceIssue.MULTIMEDIA_URI_INVALID;
import static org.gbif.pipelines.parsers.utils.ModelUtils.extractOptValue;

/**
 * Interpreter for the multimedia extension, Interprets form {@link ExtendedRecord} to {@link MultimediaRecord}.
 *
 * @see <a href="http://rs.gbif.org/extension/gbif/1.0/multimedia.xml</a>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MultimediaInterpreter {

  private static final MediaParser MEDIA_PARSER = MediaParser.getInstance();
  private static final LicenseUriParser LICENSE_URI_PARSER = LicenseUriParser.getInstance();
  private static final LicenseParser LICENSE_PARSER = LicenseParser.getInstance();

  private static final TargetHandler<Multimedia> HANDLER =
      ExtensionInterpretation.extension(Extension.MULTIMEDIA)
          .to(Multimedia::new)
          .map(DcTerm.references, MultimediaInterpreter::parseAndSetReferences)
          .map(DcTerm.identifier, MultimediaInterpreter::parseAndSetIdentifier)
          .map(DcTerm.created, MultimediaInterpreter::parseAndSetCreated)
          .map(DcTerm.license, MultimediaInterpreter::parseAndSetLicense)
          .map(DcTerm.format, MultimediaInterpreter::parseAndSetFormatAndType)
          .map(DcTerm.title, Multimedia::setTitle)
          .map(DcTerm.description, Multimedia::setDescription)
          .map(DcTerm.contributor, Multimedia::setContributor)
          .map(DcTerm.publisher, Multimedia::setPublisher)
          .map(DcTerm.audience, Multimedia::setAudience)
          .map(DcTerm.creator, Multimedia::setCreator)
          .map(DcTerm.rightsHolder, Multimedia::setRightsHolder)
          .map(DcTerm.source, Multimedia::setSource)
          .map(DwcTerm.datasetID, Multimedia::setDatasetId)
          .skipIf(MultimediaInterpreter::checkLinks);

  /**
   * Interprets the multimedia of a {@link ExtendedRecord} and populates a {@link MultimediaRecord}
   * with the interpreted values.
   */
  public static void interpret(ExtendedRecord er, MultimediaRecord mr) {
    Objects.requireNonNull(er);
    Objects.requireNonNull(mr);

    Result<Multimedia> result = HANDLER.convert(er);

    parseAssociatedMedia(result, er);

    mr.setMultimediaItems(result.getList());
    mr.getIssues().setIssueList(result.getIssuesAsList());
  }

  private static void parseAssociatedMedia(Result<Multimedia> result, ExtendedRecord er) {
    extractOptValue(er, DwcTerm.associatedMedia).ifPresent(v ->
        UrlParser.parseUriList(v).forEach(uri -> {
          if (uri == null) {
            result.getIssues().add(MULTIMEDIA_URI_INVALID.name());
          } else if (!containsUri(result, uri)) {
            Multimedia multimedia = new Multimedia();
            multimedia.setIdentifier(uri.toString());
            parseAndSetFormatAndType(multimedia, null);
            result.getList().add(multimedia);
          }
        }));
  }

  private static boolean containsUri(Result<Multimedia> result, URI uri) {
    return result.getList().stream()
        .anyMatch(v ->
            (!Strings.isNullOrEmpty(v.getIdentifier()) && uri.equals(URI.create(v.getIdentifier())))
                || (!Strings.isNullOrEmpty(v.getReferences()) && uri.equals(URI.create(v.getReferences()))));
  }

  /**
   * Parser for "http://purl.org/dc/terms/references" term value
   */
  private static void parseAndSetReferences(Multimedia m, String v) {
    URI uri = UrlParser.parse(v);
    Optional.ofNullable(uri).map(URI::toString).ifPresent(m::setReferences);
  }

  /**
   * Parser for "http://purl.org/dc/terms/identifier" term value
   */
  private static void parseAndSetIdentifier(Multimedia m, String v) {
    URI uri = UrlParser.parse(v);
    Optional.ofNullable(uri).map(URI::toString).ifPresent(m::setIdentifier);
  }

  /**
   * Parser for "http://purl.org/dc/terms/type" term value
   */
  private static void parseAndSetType(Multimedia m, String v) {
    if (!Strings.isNullOrEmpty(v)) {
      if (v.toLowerCase().startsWith("image") || v.equalsIgnoreCase(MediaType.StillImage.name())) {
        m.setType(MediaType.StillImage.name());
      } else if (v.toLowerCase().startsWith("audio") || v.equalsIgnoreCase(MediaType.Sound.name())) {
        m.setType(MediaType.Sound.name());
      } else if (v.toLowerCase().startsWith("video") || v.equalsIgnoreCase(MediaType.MovingImage.name())) {
        m.setType(MediaType.MovingImage.name());
      }
    }
  }

  /**
   * Parser for "http://purl.org/dc/terms/created" term value
   */
  private static List<String> parseAndSetCreated(Multimedia m, String v) {
    ParsedTemporal parsed = TemporalParser.parse(v);
    parsed.getFromOpt().map(Temporal::toString).ifPresent(m::setCreated);

    return parsed.getIssues().isEmpty() ? Collections.emptyList() :
        Collections.singletonList(MULTIMEDIA_DATE_INVALID.name());
  }

  /**
   * Parser for "http://purl.org/dc/terms/format" term value
   */
  private static void parseAndSetFormatAndType(Multimedia m, String v) {
    String mimeType = MEDIA_PARSER.parseMimeType(v);
    if (Strings.isNullOrEmpty(mimeType) && !Strings.isNullOrEmpty(m.getIdentifier())) {
      mimeType = MEDIA_PARSER.parseMimeType(URI.create(m.getIdentifier()));
    }
    if ("text/html".equalsIgnoreCase(mimeType) && m.getIdentifier() != null) {
      // make file URI the references link URL instead
      m.setReferences(m.getIdentifier());
      m.setIdentifier(null);
      mimeType = null;
    }

    m.setFormat(mimeType);

    parseAndSetType(m, m.getFormat());
  }

  /** Returns ENUM instead of url string */
  private static void parseAndSetLicense(Multimedia m, String v) {
    URI uri = Optional.ofNullable(v).map(x -> {
      try {
        return URI.create(x);
      } catch (IllegalArgumentException ex) {
        return null;
      }
    }).orElse(null);
    License license = LICENSE_PARSER.parseUriThenTitle(uri, null);
    String result = license.name();
    if (license == License.UNSUPPORTED) {
      ParseResult<URI> parsed = LICENSE_URI_PARSER.parse(v);
      result = parsed.isSuccessful() ? parsed.getPayload().toString() : v;
    }
    m.setLicense(result);
  }

  /**
   * Skip whole record if both links are absent
   */
  private static Optional<String> checkLinks(Multimedia m) {
    if (m.getReferences() == null && m.getIdentifier() == null) {
      return Optional.of(MULTIMEDIA_URI_INVALID.name());
    }
    return Optional.empty();
  }
}
