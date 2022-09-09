package org.gbif.pipelines.maven;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.avro.JsonProperties;
import org.apache.avro.Schema;
import org.apache.avro.Schema.Type;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.gbif.api.vocabulary.Extension;
import org.gbif.dwc.digester.ThesaurusHandlingRule;
import org.gbif.dwc.extensions.ExtensionFactory;
import org.gbif.dwc.extensions.VocabulariesManager;
import org.gbif.dwc.extensions.Vocabulary;
import org.gbif.dwc.xml.SAXUtils;

@Mojo(name = "avroschemageneration", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class XmlToAvscGeneratorMojo extends AbstractMojo {

  private static final Set<String> RESERVED_WORDS =
      new HashSet<>(Arrays.asList("date", "order", "format", "group"));

  @Parameter(property = "avroschemageneration.pathToWrite")
  private String pathToWrite;

  @Parameter(property = "avroschemageneration.namespace")
  private String namespace;

  @Override
  public void execute() throws MojoExecutionException {

    try {
      Files.createDirectories(Paths.get(pathToWrite));

      Map<Extension, String> extensions = Extension.availableExtensionResources();
      extensions.remove(Extension.AUDUBON);
      extensions.remove(Extension.IMAGE);
      extensions.remove(Extension.MULTIMEDIA);

      for (Entry<Extension, String> extension : extensions.entrySet()) {

        try {
          String name = normalizeClassName(extension.getKey().name());
          URL url = new URL(extension.getValue());

          Path path = Paths.get(pathToWrite, normalizeFileName(name));
          if (!Files.exists(path)) {
            convertAndWrite(name, url, path);
          }
        } catch (Exception ex) {
          getLog().warn(ex.getMessage());
        }
      }
    } catch (IOException ex) {
      throw new MojoExecutionException(ex.getMessage());
    }
  }

  public void setPathToWrite(String pathToWrite) {
    this.pathToWrite = pathToWrite;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  private void convertAndWrite(String name, URL url, Path path) throws Exception {
    // Read extension
    ThesaurusHandlingRule thr = new ThesaurusHandlingRule(new EmptyVocabulariesManager());
    ExtensionFactory factory = new ExtensionFactory(thr, SAXUtils.getNsAwareSaxParserFactory());
    org.gbif.dwc.extensions.Extension ext = factory.build(url.openStream(), url, false);

    // Convert into an avro schema
    List<Schema.Field> fields = new ArrayList<>(ext.getProperties().size() + 1);
    // Add gbifID
    fields.add(createSchemaField("gbifid", Type.STRING, "GBIF internal identifier", false));
    // Add RAW fields
    ext.getProperties().stream()
        .map(p -> createSchemaField("v_" + normalizeFieldName(p.getName()), p.getQualname()))
        .forEach(fields::add);
    // Add fields
    ext.getProperties().stream()
        .map(p -> createSchemaField(normalizeFieldName(p.getName()), p.getQualname()))
        .forEach(fields::add);

    String[] extraNamespace =
        url.toString().replaceAll("http://rs.gbif.org/extension/", "").split("/");

    String doc = "Avro Schema of Hive Table for " + name;
    String fullNamespace = namespace + "." + extraNamespace[0];
    String schema = Schema.createRecord(name, doc, fullNamespace, false, fields).toString(true);

    // Add comment
    String comment = "/** Autogenerated by xml-to-avsc-maven-plugin. DO NOT EDIT DIRECTLY */\n";
    schema = comment + schema;

    // Save into a file
    Files.deleteIfExists(path);
    getLog().info("Create avro schema for " + ext.getName() + " extension - " + path.toString());
    Files.write(path, schema.getBytes(UTF_8));
  }

  private Schema.Field createSchemaField(String name, String doc) {
    return createSchemaField(name, Type.STRING, doc, true);
  }

  private String normalizeClassName(String name) {
    return Arrays.stream(name.split("_"))
            .map(String::toLowerCase)
            .map(x -> x.substring(0, 1).toUpperCase() + x.substring(1))
            .collect(Collectors.joining())
        + "Table";
  }

  private String normalizeFieldName(String name) {
    String normalizedNamed = name.toLowerCase().trim().replace("-", "").replaceAll("_", "");
    if (RESERVED_WORDS.contains(normalizedNamed)) {
      return normalizedNamed + '_';
    }
    return normalizedNamed;
  }

  private String normalizeFileName(String name) {
    String result =
        Arrays.stream(name.split("(?=[A-Z])"))
            .map(String::toLowerCase)
            .collect(Collectors.joining("-"));
    return result + ".avsc";
  }

  private Schema.Field createSchemaField(
      String name, Schema.Type type, String doc, boolean isNull) {

    Schema schema;
    if (isNull) {
      List<Schema> optionalString = new ArrayList<>(2);
      optionalString.add(Schema.create(Schema.Type.NULL));
      optionalString.add(Schema.create(type));
      schema = Schema.createUnion(optionalString);
    } else {
      schema = Schema.create(type);
    }
    return new Schema.Field(name, schema, doc, JsonProperties.NULL_VALUE);
  }

  private static class EmptyVocabulariesManager implements VocabulariesManager {

    @Override
    public Vocabulary get(String uri) {
      return null;
    }

    @Override
    public Vocabulary get(URL url) {
      return null;
    }

    @Override
    public Map<String, String> getI18nVocab(String uri, String lang) {
      return null;
    }

    @Override
    public List<Vocabulary> list() {
      return null;
    }
  }
}
