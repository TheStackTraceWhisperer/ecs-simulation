package com.ecs;

import com.artemis.Component;
import com.artemis.BaseSystem;
import com.artemis.systems.IteratingSystem;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchCondition;
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
        // Custom condition: all declared fields must be public
        ArchRule publicFieldsRule = classes()
                .that().areAssignableTo(Component.class)
                .and().resideInAPackage("..component..")
                .should(new ArchCondition<>("have only public fields") {
                    @Override
                    public void check(com.tngtech.archunit.core.domain.JavaClass javaClass, 
                                     com.tngtech.archunit.lang.ConditionEvents events) {
                        javaClass.getAllFields().stream()
                                .filter(field -> !field.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.STATIC))
                                .forEach(field -> {
                                    if (!field.getModifiers().contains(com.tngtech.archunit.core.domain.JavaModifier.PUBLIC)) {
                                        String message = String.format(
                                            "Field %s.%s is not public",
                                            javaClass.getName(), field.getName()
                                        );
                                        events.add(com.tngtech.archunit.lang.SimpleConditionEvent.violated(field, message));
                                    }
                                });
                    }
                });

        publicFieldsRule.check(importedClasses);
    }

    @Test
    void componentsMustNotHaveMethods() {
        // Components should not have any methods except constructors and inherited methods
        ArchRule rule = classes()
                .that().areAssignableTo(Component.class)
                .and().resideInAPackage("..component..")
                .should(new ArchCondition<>("not have any declared methods except constructors") {
                    @Override
                    public void check(com.tngtech.archunit.core.domain.JavaClass javaClass, 
                                     com.tngtech.archunit.lang.ConditionEvents events) {
                        javaClass.getMethods().stream()
                                .filter(method -> method.getOwner().equals(javaClass))
                                .filter(method -> !method.getName().equals("<init>"))
                                .forEach(method -> {
                                    String message = String.format(
                                        "Component %s declares method %s, but components should only contain data",
                                        javaClass.getName(), method.getName()
                                    );
                                    events.add(com.tngtech.archunit.lang.SimpleConditionEvent.violated(method, message));
                                });
                    }
                });

        rule.check(importedClasses);
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

    @Test
    void componentsShouldNotDependOnSystems() {
        ArchRule rule = classes()
                .that().resideInAPackage("..component..")
                .should().onlyDependOnClassesThat()
                .resideOutsideOfPackage("..system..");

        rule.check(importedClasses);
    }
}
