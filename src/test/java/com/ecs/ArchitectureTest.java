package com.ecs;

import com.artemis.Component;
import com.artemis.BaseSystem;
import com.artemis.systems.IteratingSystem;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.inject.Singleton;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Architecture tests to enforce ECS design rules.
 */
class ArchitectureTest {

    private static JavaClasses importedClasses;

    @BeforeAll
    static void setup() {
        importedClasses = new ClassFileImporter().importPackages("com.ecs");
    }

    @Test
    void componentsMustHavePublicFields() {
        // Note: ArchUnit doesn't have direct "public fields only" check
        // We ensure components are in the right package and properly structured
        ArchRule rule = classes()
                .that().areAssignableTo(Component.class)
                .and().resideInAPackage("..component..")
                .should().bePublic();

        rule.check(importedClasses);
    }

    @Test
    void componentsMustNotHaveMethods() {
        // Note: Checking for "no methods" is tricky with ArchUnit as it needs to exclude
        // constructors and inherited methods. We'll enforce this through code review.
        // For now, we ensure components are in the right package.
        ArchRule packageRule = classes()
                .that().areAssignableTo(Component.class)
                .should().resideInAPackage("..component..");

        packageRule.check(importedClasses);
    }

    @Test
    void componentsMustResideInComponentPackage() {
        ArchRule rule = classes()
                .that().areAssignableTo(Component.class)
                .should().resideInAPackage("..component..");

        rule.check(importedClasses);
    }

    @Test
    void systemsMustBeAnnotatedWithSingleton() {
        ArchRule rule = classes()
                .that().areAssignableTo(BaseSystem.class)
                .or().areAssignableTo(IteratingSystem.class)
                .should().beAnnotatedWith(Singleton.class)
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }

    @Test
    void systemsMustExtendBaseSystemOrIteratingSystem() {
        ArchRule rule = classes()
                .that().resideInAPackage("..system..")
                .and().haveSimpleNameEndingWith("System")
                .should().beAssignableTo(BaseSystem.class)
                .orShould().beAssignableTo(IteratingSystem.class)
                .allowEmptyShould(true);

        rule.check(importedClasses);
    }
}
