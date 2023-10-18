package ch.basler.openrewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.Assertions;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class ControllerMetricTest implements RewriteTest {

  @Override
  public void defaults(RecipeSpec spec) {
    spec.recipe(new ControllerMetricRecipe("com.myorg.FooBar"));
  }

  @Test
  void addsAnnotationToMappingOperation() {
    rewriteRun(
      java(
        """
              package com.yourorg;

              import org.springframework.web.bind.annotation.PostMapping;
              
              class FooBar {
              
                  @PostMapping
                  public void myOperation() {
                  }
              }
          """,
        """
            package com.yourorg;

              import io.micrometer.core.annotation.Timed;
              import org.springframework.web.bind.annotation.PostMapping;
              
              class FooBar {
              
                  @PostMapping
                  @Timed
                  public void myOperation() {
                  }
              }
        """
      )
    );
  }

  @Test
  void doesNotChangeExistingAnnotation() {
    rewriteRun(
      java(
        """
              package com.yourorg;

              import io.micrometer.core.annotation.Timed;
              import org.springframework.web.bind.annotation.PostMapping;
              
              class FooBar {
              
                  @PostMapping
                  @Timed(name="myTimer")
                  public void myOperation() {
                  }
              }
        """
      )
    );
  }

  @Test
  void doesNotChangeNonAnnotatedOperations() {
    rewriteRun(
      java(
        """
            package com.yourorg;
  
              class FooBar {
              
                  public void myOperation() {
                  }
              }
        """
      )
    );
  }
}
