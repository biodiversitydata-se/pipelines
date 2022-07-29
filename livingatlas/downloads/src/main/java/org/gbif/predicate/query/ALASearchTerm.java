package org.gbif.predicate.query;

import java.io.Serializable;
import java.net.URI;
import org.gbif.dwc.terms.Term;

public enum ALASearchTerm implements Term, Serializable {
  eventTypeHierarchy(),
  eventHierarchy();

  private static final URI NS_URI = URI.create("http://ala.org.au/terms/1.0/");
  private static final String PREFIX = "ala";

  private ALASearchTerm() {}

  @Override
  public String prefix() {
    return PREFIX;
  }

  @Override
  public URI namespace() {
    return NS_URI;
  }

  @Override
  public String simpleName() {
    return eventTypeHierarchy.name();
  }

  @Override
  public boolean isClass() {
    return false;
  }
}
