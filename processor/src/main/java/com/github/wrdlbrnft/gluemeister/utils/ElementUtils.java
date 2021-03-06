package com.github.wrdlbrnft.gluemeister.utils;

import com.github.wrdlbrnft.gluemeister.GlueMeisterException;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 29/01/2017
 */

public class ElementUtils {

    public static <E extends GlueMeisterException> void verifyStaticModifier(Element element, BiFunction<String, Element, E> exceptionFactory) {
        if (!isStatic(element)) {
            throw exceptionFactory.apply("Element " + element.getSimpleName() + " is not accessible to GlueMeister. It has to be static!", element);
        }
    }

    public static <E extends GlueMeisterException> void verifyFinalModifier(Element element, BiFunction<String, Element, E> exceptionFactory) {
        if (!isFinal(element)) {
            throw exceptionFactory.apply("Element " + element.getSimpleName() + " cannot be used by GlueMeister. To ensure proper runtime behavior it has to be final!", element);
        }
    }

    public static <E extends GlueMeisterException> void verifyAccessibility(Element element, BiFunction<String, Element, E> exceptionFactory) {
        Element enclosingElement = element.getEnclosingElement();

        if (enclosingElement == null) {
            return;
        }

        if (enclosingElement.getKind() == ElementKind.PACKAGE) {
            return;
        }

        if (!hasPublicVisibility(element)) {
            throw exceptionFactory.apply("Element " + element.getSimpleName() + " is not accessible to GlueMeister. The element has to have at least package local or public visibility.", element);
        }

        while (enclosingElement.getKind() != ElementKind.PACKAGE) {

            if (!isStatic(enclosingElement)) {
                throw exceptionFactory.apply("Element " + element.getSimpleName() + " is not accessible to GlueMeister. It is nested inside " + enclosingElement.getSimpleName() + " and this element has to be static. Currently it is not static.", enclosingElement);
            }

            if (!hasPublicVisibility(enclosingElement)) {
                throw exceptionFactory.apply("Element " + element.getSimpleName() + " is not accessible to GlueMeister. It is nested inside " + enclosingElement.getSimpleName() + " and this element has to have at least package local or public visibility.", enclosingElement);
            }

            enclosingElement = enclosingElement.getEnclosingElement();

            if (enclosingElement == null) {
                return;
            }
        }
    }

    public static PackageElement findContainingPackage(Element element) {
        Element enclosingElement = element.getEnclosingElement();

        if (enclosingElement == null) {
            return null;
        }

        if (enclosingElement.getKind() == ElementKind.PACKAGE) {
            return (PackageElement) enclosingElement;
        }

        while (enclosingElement.getKind() != ElementKind.PACKAGE) {

            enclosingElement = enclosingElement.getEnclosingElement();

            if (enclosingElement == null) {
                return null;
            }
        }

        return (PackageElement) enclosingElement;
    }

    public static String findContainingPackageName(Element element) {
        final PackageElement packageElement = findContainingPackage(element);
        if (packageElement == null) {
            return "";
        }
        return packageElement.getQualifiedName().toString();
    }

    public static boolean isStatic(Element element) {
        return element.getEnclosingElement().getKind() == ElementKind.PACKAGE || element.getModifiers().contains(Modifier.STATIC);
    }

    public static boolean isFinal(Element element) {
        return element.getModifiers().contains(Modifier.FINAL);
    }

    public static boolean hasPublicVisibility(Element element) {
        final Set<Modifier> modifiers = element.getModifiers();
        return modifiers.contains(Modifier.PUBLIC);
    }

    public static List<ExecutableElement> determineAbstractMethods(ProcessingEnvironment processingEnv, TypeElement element) {
        return processingEnv.getElementUtils().getAllMembers(element).stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .filter(e -> e.getModifiers().contains(Modifier.ABSTRACT))
                .map(ExecutableElement.class::cast)
                .collect(Collectors.toList());
    }
}
