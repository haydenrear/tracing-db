package com.hayden.tracingdb.entity;

import com.hayden.tracing_aspect.observation_aspects.BehaviorDataExtractor;
import com.hayden.tracing_aspect.observation_aspects.ObservationUtility;
import com.hayden.utilitymodule.MapFunctions;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

///  When annotating a method with @tracing_apt.Logged then this runs and
/// saves information in the database
/// @see com.hayden.tracing_apt.Logged <- annotation for aspect
/// @see com.hayden.tracing_apt.observation_aspects.CdcObservabilityAspect <- aspect itself
/// @see com.hayden.tracing_apt.observation_aspects.ObservationBehavior <- save json context data and ObservationContext
/// @see com.hayden.tracingdb.handler.DelegatingCdcObservationHandler <- save the json context data to the CDC database (gets called by micrometer)
@Component
public class JavaReflectionBehaviorDataExtractor implements BehaviorDataExtractor {

    // TODO: Load class matchers to extract args and signatures. For example DataSource connection.
    record JavaReflectionArgumentExtractorArgs(@NotNull ObservationUtility.ObservationArgs proceeding,
                                               @NotNull ObservationUtility<? extends ObservationUtility.ObservationArgs> utility,
                                               Object object,
                                               int depth,
                                               int maxDepth) {}


    @NotNull
    public Map<String, Object> extract(@NotNull ObservationUtility.ObservationArgs proceeding,
                                       @NotNull ObservationUtility<?> utility) {
        if (utility.matchers(proceeding).stream().anyMatch(u -> u.matches(proceeding))) {
            return MapFunctions.CollectMap(
                    proceeding.args().entrySet().stream()
                            .flatMap(arg -> this.extractRecursive(
                                                    new JavaReflectionArgumentExtractorArgs(
                                                            proceeding, utility, arg.getValue(),
                                                            0, 3
                                                    ),
                                                    arg.getKey()
                                            )
                                            .entrySet()
                                            .stream()));
        }

        return new HashMap<>();
    }

    private Map<String, Object> extractRecursive(JavaReflectionArgumentExtractorArgs argumentExtractorArgs,
                                                 String name) {
        var util = argumentExtractorArgs.utility;
        return Optional.ofNullable(argumentExtractorArgs.object)
                .stream()
                .map(objCreated -> Map.entry(
                        argName(name, objCreated),
                        shouldContinueRecurse(argumentExtractorArgs)
                                 ? getNextArgsRecursive(argumentExtractorArgs)
                                 : serializeArg(objCreated, util)
                ))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k1));
    }


    @NotNull
    private Map<String, Object> getNextArgsRecursive(JavaReflectionArgumentExtractorArgs argumentExtractorArgs) {
        var objCreated = argumentExtractorArgs.object;
        var observationArgs = argumentExtractorArgs.proceeding;
        var util = argumentExtractorArgs.utility;
        return Arrays.stream(objCreated.getClass().getDeclaredFields())
                .filter(AccessibleObject::trySetAccessible)
                .filter(f -> util.matchers(observationArgs).stream().anyMatch(b -> b.matches(f)))
                .flatMap(nextField -> doGetNextArgRecursive(argumentExtractorArgs, nextField))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k1 + ", " + k2));
    }

    @NotNull
    private Stream<Map.Entry<String, Object>> doGetNextArgRecursive(
            JavaReflectionArgumentExtractorArgs argumentExtractorArgs,
            Field nextField
    ) {
        try {
            return Optional.ofNullable(nextField.get(argumentExtractorArgs.object))
                    .filter(o -> argumentExtractorArgs.utility.matchers(argumentExtractorArgs.proceeding).stream().anyMatch(b -> b.matches(o)))
                    .stream()
                    .flatMap(nextExtractedArg -> extractRecursive(
                            // TODO - update with the JEP to be using the with {  }
                            new JavaReflectionArgumentExtractorArgs(
                                    argumentExtractorArgs.proceeding,
                                    argumentExtractorArgs.utility,
                                    nextExtractedArg,
                                    argumentExtractorArgs.depth + 1,
                                    argumentExtractorArgs.maxDepth
                            ),
                            nextField.getName()
                        )
                        .entrySet()
                        .stream()
                    );
        } catch (IllegalAccessException ignored) {
            return Stream.empty();
        }
    }

    private static String argName(String name, Object objCreated) {
        return name == null
                ? objCreated.getClass().getSimpleName()
                : "%s.%s".formatted(objCreated.getClass().getSimpleName(), name);
    }

    private static boolean shouldContinueRecurse(JavaReflectionArgumentExtractorArgs argumentExtractorArgs) {
        return argumentExtractorArgs.depth <= argumentExtractorArgs.maxDepth;
    }

    private static String serializeArg(Object objCreated, ObservationUtility<?> util) {
        return Optional.ofNullable(util.getSerializer(objCreated))
                .map(c -> c.doSerialize(objCreated))
                .orElse(objCreated.toString());
    }
}