package ch.basler.openrewrite;

import io.micrometer.core.annotation.Timed;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;
import org.openrewrite.Contributor;
import org.openrewrite.ExecutionContext;
import org.openrewrite.Recipe;
import org.openrewrite.TreeVisitor;
import org.openrewrite.java.JavaIsoVisitor;
import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;
import org.openrewrite.java.tree.J;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;

import com.fasterxml.jackson.annotation.JsonCreator;

@Value
@EqualsAndHashCode(callSuper = false)
public class ControllerMetricRecipe extends Recipe {

  // All recipes must be serializable. This is verified by RewriteTest.rewriteRun() in your tests.
  @JsonCreator
  public ControllerMetricRecipe() {
  }

  @Override
  public @NotNull String getDisplayName() {
    return "Add Prometheus Timer to operations";
  }

  @Override
  public @NotNull String getDescription() {
    return "Adding @Timed annotation to each rest operation if not available.";
  }

  @Override
  public @NotNull List<Contributor> getContributors() {
    return Arrays.asList(
            new Contributor("Arno Burkhart", "", 0),
            new Contributor("Daniel Prill", "", 0),
            new Contributor("Markus Lindenmann", "", 0));
  }

  @Override
  public @NotNull TreeVisitor<?, ExecutionContext> getVisitor() {
    return new ControllerMetricVisitor();
  }

  public static class ControllerMetricVisitor extends JavaIsoVisitor<ExecutionContext> {
    @Override
    public J.@NotNull MethodDeclaration visitMethodDeclaration(J.@NotNull MethodDeclaration method,
                                                               @NotNull ExecutionContext executionContext) {
      super.visitMethodDeclaration(method, executionContext);

      final List<J.Annotation> allAnnotations = method.getAllAnnotations();
      if (allAnnotations.stream()
                        .noneMatch(a -> Stream.of(PostMapping.class,
                                                  PutMapping.class,
                                                  GetMapping.class,
                                                  PatchMapping.class,
                                                  DeleteMapping.class)
                                              .anyMatch(mapping -> mapping.getSimpleName().equals(((J.Identifier) a.getAnnotationType()).getSimpleName())))
                                        || allAnnotations.stream()
                           .anyMatch(current -> Timed.class.getSimpleName().equals(((J.Identifier) current.getAnnotationType()).getSimpleName()))) {
        // missing a *Mapping or already has Timed annotation: skipping
        return method;
      }

      method = JavaTemplate.builder("@" + Timed.class.getSimpleName())
                           .imports(Timed.class.getName())
                           .javaParser(JavaParser.fromJavaVersion()
                                                 .dependsOn("package io.micrometer.core.annotation; public @interface Timed{}")
                           )
                           .build()
                           .apply(getCursor(), method.getCoordinates().addAnnotation(Comparator.comparing(J.Annotation::getSimpleName)));

      maybeAddImport(Timed.class.getName());
      return method;
    }
  }
}
