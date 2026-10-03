/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;

import dagger.android.AndroidInjector;

import static dagger.internal.Preconditions.checkNotNull;

public final class ExtendableInjector<T> implements AndroidInjector<T> {
    private static final String NO_SUPERTYPES_BOUND_FORMAT =
            "No injector factory bound for Class<%s>";
    private static final String SUPERTYPES_BOUND_FORMAT =
            "No injector factory bound for Class<%1$s>. Injector factories were bound for supertypes "
                    + "of %1$s: %2$s. Did you mean to bind an injector factory for the subtype?";

    private final Map<Class<?>, Provider<Factory<?>>> mInjectorFactories;

    @Inject
    ExtendableInjector(Map<Class<?>, Provider<Factory<?>>> injectorFactories) {
        this.mInjectorFactories = new HashMap<>();
        mInjectorFactories.putAll(injectorFactories);
    }

    @SuppressWarnings("unchecked")
    public void combineWith(ExtendableInjector injector) {
        mInjectorFactories.putAll(injector.mInjectorFactories);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public boolean maybeInject(T instance) {
        Provider<Factory<?>> factoryProvider =
                mInjectorFactories.get(instance.getClass());
        if (factoryProvider == null) {
            return false;
        }

        @SuppressWarnings("unchecked")
        Factory<T> factory = (Factory<T>) factoryProvider.get();
        try {
            AndroidInjector<T> injector =
                    checkNotNull(
                            factory.create(instance), "%s.create(I) should not return null.", factory.getClass());

            injector.inject(instance);
            return true;
        } catch (ClassCastException e) {
            throw new InvalidInjectorBindingException(
                    String.format(
                            "%s does not implement AndroidInjector.Factory<%s>",
                            factory.getClass().getCanonicalName(), instance.getClass().getCanonicalName()),
                    e);
        }
    }

    /**
     * Performs members-injection on {@code instance}.
     *
     * @throws InvalidInjectorBindingException if the injector factory bound for a class does not
     *     inject instances of that class
     * @throws IllegalArgumentException if no {@link AndroidInjector.Factory} is bound for {@code
     *     instance}
     */
    @Override
    public void inject(T instance) {
        boolean wasInjected = maybeInject(instance);
        if (!wasInjected) {
            throw new IllegalArgumentException(errorMessageSuggestions(instance));
        }
    }

    /**
     * Exception thrown if an incorrect binding is made for a {@link AndroidInjector.Factory}. If you
     * see this exception, make sure the value in your {@code @ActivityKey(YourActivity.class)} or
     * {@code @FragmentKey(YourFragment.class)} matches the type argument of the injector factory.
     */
    public static final class InvalidInjectorBindingException extends RuntimeException {
        InvalidInjectorBindingException(String message, ClassCastException cause) {
            super(message, cause);
        }
    }

    /** Returns an error message with the class names that are supertypes of {@code instance}. */
    private String errorMessageSuggestions(T instance) {
        List<String> suggestions = new ArrayList<String>();
        for (Class<?> activityClass : mInjectorFactories.keySet()) {
            if (activityClass.isInstance(instance)) {
                suggestions.add(activityClass.getCanonicalName());
            }
        }
        Collections.sort(suggestions);

        return suggestions.isEmpty()
                ? String.format(NO_SUPERTYPES_BOUND_FORMAT, instance.getClass().getCanonicalName())
                : String.format(
                SUPERTYPES_BOUND_FORMAT, instance.getClass().getCanonicalName(), suggestions);
    }
}