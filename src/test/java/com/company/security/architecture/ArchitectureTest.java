package com.company.security.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@DisplayName("Architecture Tests")
class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.company.security");
    }

    @Nested
    @DisplayName("Domain Layer Rules")
    class DomainLayerRules {

        @Test
        @DisplayName("Domain should not depend on infrastructure")
        void domainShouldNotDependOnInfrastructure() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                    .because("Domain layer must be independent of infrastructure")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Domain should not depend on Spring framework")
        void domainShouldNotDependOnSpring() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAPackage("org.springframework..")
                    .because("Domain layer must not have Spring dependencies")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Domain should not depend on external libraries except Java standard")
        void domainShouldNotDependOnExternalLibraries() {
            noClasses()
                    .that().resideInAPackage("..domain..")
                    .should().dependOnClassesThat().resideInAnyPackage(
                            "io.jsonwebtoken..",
                            "org.springframework..",
                            "io.github.resilience4j..",
                            "org.mapstruct..",
                            "lombok..",
                            "jakarta..",
                            "org.apache.kafka..",
                            "com.fasterxml.."
                    )
                    .because("Domain layer should only use Java standard library")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Port Layer Rules")
    class PortLayerRules {

        @Test
        @DisplayName("Input ports should not depend on adapters")
        void inputPortsShouldNotDependOnAdapters() {
            noClasses()
                    .that().resideInAPackage("..domain.port.input..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter..")
                    .because("Input ports should not know about adapters")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Output ports should not depend on adapters")
        void outputPortsShouldNotDependOnAdapters() {
            noClasses()
                    .that().resideInAPackage("..domain.port.output..")
                    .should().dependOnClassesThat().resideInAPackage("..adapter..")
                    .because("Output ports should not know about adapters")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Feature Isolation Rules")
    class FeatureIsolationRules {

        @Test
        @DisplayName("Authentication feature should not depend on password feature")
        void authenticationShouldNotDependOnPassword() {
            noClasses()
                    .that().resideInAPackage("com.company.security.authentication..")
                    .should().dependOnClassesThat().resideInAPackage("com.company.security.password..")
                    .because("Features should be independent - authentication should not depend on password")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Password feature should not depend on authentication feature")
        void passwordShouldNotDependOnAuthentication() {
            noClasses()
                    .that().resideInAPackage("com.company.security.password..")
                    .should().dependOnClassesThat().resideInAPackage("com.company.security.authentication..")
                    .because("Features should be independent - password should not depend on authentication")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Token feature may depend on authentication for port reuse only")
        void tokenFeatureDependencies() {
            // Token feature can depend on authentication output ports for token operations
            // but should not depend on authentication domain or use cases
            noClasses()
                    .that().resideInAPackage("com.company.security.token.domain..")
                    .should().dependOnClassesThat().resideInAPackage("com.company.security.authentication..")
                    .because("Token domain should not depend on authentication")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Naming Conventions")
    class NamingConventions {

        @Test
        @DisplayName("Use case implementations should end with UseCaseImpl")
        void useCaseImplNaming() {
            classes()
                    .that().resideInAPackage("..domain.usecase..")
                    .should().haveSimpleNameEndingWith("UseCaseImpl")
                    .because("Use case implementations should follow naming convention")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Input ports should end with UseCase")
        void inputPortNaming() {
            classes()
                    .that().resideInAPackage("..domain.port.input..")
                    .should().haveSimpleNameEndingWith("UseCase")
                    .because("Input ports should be named as use cases")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Output ports should end with Port")
        void outputPortNaming() {
            classes()
                    .that().resideInAPackage("..domain.port.output..")
                    .and().areTopLevelClasses()
                    .should().haveSimpleNameEndingWith("Port")
                    .because("Output ports should follow naming convention")
                    .check(importedClasses);
        }
    }

    @Nested
    @DisplayName("Annotation Rules")
    class AnnotationRules {

        @Test
        @DisplayName("Adapters should not have @Component annotation (except @RestController and @RestControllerAdvice)")
        void adaptersShouldNotHaveComponent() {
            noClasses()
                    .that().resideInAPackage("..adapter..")
                    .and().areNotAnnotatedWith(org.springframework.web.bind.annotation.RestController.class)
                    .and().areNotAnnotatedWith(org.springframework.web.bind.annotation.RestControllerAdvice.class)
                    .should().beAnnotatedWith(org.springframework.stereotype.Component.class)
                    .because("Adapters should be wired via @Bean configuration, not @Component")
                    .check(importedClasses);
        }

        @Test
        @DisplayName("Use cases should not have @Service annotation")
        void useCasesShouldNotHaveService() {
            noClasses()
                    .that().resideInAPackage("..domain.usecase..")
                    .should().beAnnotatedWith(org.springframework.stereotype.Service.class)
                    .because("Use cases should be wired via @Bean configuration, not @Service")
                    .check(importedClasses);
        }
    }
}
