package ch.basler.openrewrite;

import lombok.EqualsAndHashCode;
import lombok.Value;

import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.internal.lang.NonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

@Value
@EqualsAndHashCode(callSuper = false)
public class ControllerMetricRecipe extends Recipe {

  @Option(displayName = "Fully Qualified Class Name")
  @NonNull
  String fullyQualifiedClassName;

  // All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in your tests.
  @JsonCreator
  public ControllerMetricRecipe(@NonNull @JsonProperty("fullyQualifiedClassName") String fullyQualifiedClassName) {
    this.fullyQualifiedClassName = fullyQualifiedClassName;
  }

  @Override
  public String getDisplayName() {
    return "Add Prometheus Timer to operations";
  }

  @Override
  public String getDescription() {
    return "Adding @Timer annotation to each rest operation if not available.";
  }

}
