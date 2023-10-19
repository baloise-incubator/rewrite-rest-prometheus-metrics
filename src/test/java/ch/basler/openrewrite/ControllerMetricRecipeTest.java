package ch.basler.openrewrite;

import org.junit.jupiter.api.Test;
import org.openrewrite.java.JavaParser;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.java.Assertions.java;

class ControllerMetricRecipeTest implements RewriteTest {
  @Override
  public void defaults(RecipeSpec spec) {
    spec.recipe(new ControllerMetricRecipe())
        .parser(JavaParser.fromJavaVersion()
                          .classpath("spring-web", "micrometer-core"));
  }

  @Test
  void addsAnnotationToPostMappingOperation() {
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
                      """)
    );
  }

  @Test
  void addsAnnotationToDeleteMappingOperation() {
    rewriteRun(
            java(
                    """
                    package com.yourorg;
      
                    import org.springframework.web.bind.annotation.DeleteMapping;
                    
                    class FooBar {
                    
                        @DeleteMapping
                        public void myOperation() {
                        }
                    }
                """,
                    """
                    package com.yourorg;
      
                    import io.micrometer.core.annotation.Timed;
                    import org.springframework.web.bind.annotation.DeleteMapping;
                    
                    class FooBar {
                    
                        @DeleteMapping
                        @Timed
                        public void myOperation() {
                        }
                    }
                            """)
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
