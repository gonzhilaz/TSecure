/*
© 2025 AO Kaspersky Lab. All Rights Reserved.
*/
package com.kavsdkexample.core.app.model.impl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.core.util.Consumer;

import com.kavsdkexample.core.app.SdkStatusObserver;
import com.kavsdkexample.core.app.SdkStatusProvider;
import com.kavsdkexample.core.app.model.BaseModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@UiThread
public class BaseModelImpl implements BaseModel {
    @NonNull
    private static  Map<String, Set<Object>> sExtraObservers = new HashMap<>();
    private final   Set<SdkStatusObserver>   mObservers      = new HashSet<>();
    private final   Map<String, Set<Object>> mExtraObservers = new HashMap<>();
    private         boolean                  mInitialized;
    private         boolean                  mInitFailed;

    protected BaseModelImpl() {
    }

    @Override
    public void setSdkStatusProvider(@NonNull SdkStatusProvider provider) {
        provider.subscribe(this);
    }

    @Override
    public boolean isLoading() {
        return !mInitialized;
    }

    @Override
    public boolean isInitialized() {
        return mInitialized;
    }

    @Override
    public void onSdkInited() {
        mInitialized = true;
        mInitFailed  = false;
        notifyObservers(SdkStatusObserver::onSdkInited);
    }

    @Override
    public void onSdkInitFailed() {
        mInitialized = false;
        mInitFailed  = true;
        notifyObservers(SdkStatusObserver::onSdkInitFailed);
    }

    @Override
    public void addObserver(@NonNull SdkStatusObserver observer) {
        if (mInitialized) {
            observer.onSdkInited();
        } else if (mInitFailed) {
            observer.onSdkInitFailed();
        }
        mObservers.add(observer);
    }

    @Override
    public void removeObserver(@NonNull SdkStatusObserver observer) {
        mObservers.remove(observer);
    }


    @Override
    public <X> void addAdditionalObserver(@NonNull X observer, boolean isStatic) {
        addOrRemoveAdditionalObserver(observer, observer.getClass(), isStatic, true);
    }

    @Override
    public <X> void addAdditionalObserver(@NonNull X observer) {
        addOrRemoveAdditionalObserver(observer, observer.getClass(), false, true);
    }

    private  <X> void addOrRemoveAdditionalObserver(@NonNull X observer, @NonNull Class<?> clazz, boolean isStatic, boolean add) {
        Class<?> superclass = clazz.getSuperclass();

        if (superclass != null && !superclass.getName().equals(Object.class.getName())) {
            addOrRemoveAdditionalObserver(observer, superclass, isStatic, add);
        }

        Class<?>[] interfaces = clazz.getInterfaces();
        for (Class<?> currentInterface : interfaces) {
            if (currentInterface.getName().equals(SdkStatusObserver.class.getName())) {
                continue;
            }
            String observerClassName = currentInterface.getName();

            Map<String, Set<Object>> observersMap = isStatic ? sExtraObservers : mExtraObservers;
            //noinspection unchecked
            Set<X> observers = (Set<X>) observersMap.get(observerClassName);

            if (add) {
                if (observers == null) {
                    observers = new HashSet<>();

                    //noinspection unchecked
                    observersMap.put(observerClassName, (Set<Object>) observers);
                }
                observers.add(observer);
            } else if (observers != null) {
                observers.remove(observer);
            }
        }
    }

    @Override
    public <X> void removeAdditionalObserver(@NonNull X observer) {
        addOrRemoveAdditionalObserver(observer, observer.getClass(), false, false);
    }

    @Override
    public <X> void removeAdditionalObserver(@NonNull X observer, boolean isStatic) {
        addOrRemoveAdditionalObserver(observer, observer.getClass(), isStatic, false);
    }


    private void notifyObservers(@NonNull Consumer<SdkStatusObserver> consumer) {
        SdkStatusObserver[] observers = mObservers.toArray(new SdkStatusObserver[0]);
        for (SdkStatusObserver observer : observers) {
            consumer.accept(observer);
        }
    }

    @SuppressWarnings("SameParameterValue")
    protected <X> void notifyObservers(@NonNull Consumer<X> consumer, @NonNull Class<X> clazz) {
        String observerClassName = clazz.getName();
        //noinspection unchecked
        notifyObserversInternal((Set<X>) sExtraObservers.get(observerClassName), consumer);
        //noinspection unchecked
        notifyObserversInternal((Set<X>) mExtraObservers.get(observerClassName), consumer);
    }

    private static <X> void notifyObserversInternal(@Nullable Set<X> observers, @NonNull Consumer<X> consumer) {
        if (observers != null && !observers.isEmpty()) {
            @SuppressWarnings("unchecked")
            X[] observersCopy = (X[]) observers.toArray();
            //noinspection ConstantConditions
            for (X observer : observersCopy) {
                consumer.accept(observer);
            }
        }
    }
}
