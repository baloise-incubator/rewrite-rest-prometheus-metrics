package ch.basler.openrewrite;

import io.micrometer.core.annotation.Timed;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Comparator;
import java.util.List;

import org.openrewrite.ExecutionContext;
import org.openrewrite.Option;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.internal.lang.NonNull;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.springframework.web.bind.annotation.PostMapping;

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

  @Override
  public TreeVisitor<?, ExecutionContext> getVisitor() {
    return new ControllerMetricVisitor();
  }

  public class ControllerMetricVisitor extends JavaIsoVisitor<ExecutionContext> {

    @Override
    public J.MethodDeclaration visitMethodDeclaration(J.MethodDeclaration method, ExecutionContext executionContext) {
      super.visitMethodDeclaration(method, executionContext);

      final List<J.Annotation> allAnnotations = method.getAllAnnotations();
      for (J.Annotation annotation : allAnnotations) {

        //TypeUtils.isOfClassType(annotation.getType(), PostMapping.class.getName());
        if (PostMapping.class.getSimpleName().equals(((J.Identifier) annotation.getAnnotationType()).getSimpleName())) {
          if (allAnnotations.stream().noneMatch(current -> Timed.class.getSimpleName().equals(((J.Identifier) current.getAnnotationType()).getSimpleName()))) {
            method = JavaTemplate
              .builder("@Timed")
              .imports("io.micrometer.core.annotation.Timed")
              .build()
              .apply(getCursor(), method.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));

            //maybeAddImport(Timed.class.getName());
            maybeAddImport("io.micrometer.core.annotation.Timed");

          }
        }
      }

      return method;
    }
  }
}
