package io.github.fbbzl.starter.core.util;

import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.*;

@Slf4j
public class F<T>
{

    private static final Executor DEFAULT_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    @Delegate
    private final CompletableFuture<T> future;

    private F(CompletableFuture<T> future)
    {
        this.future = future;
    }

    public static <T> F<T> of(T value)
    {
        return new F<>(CompletableFuture.completedFuture(value));
    }

    public static <T> F<T> of(CompletableFuture<T> future)
    {
        return new F<>(future);
    }

    public static <U> F<U> supplyAsync(Supplier<U> supplier)
    {
        return supplyAsync(supplier, DEFAULT_EXECUTOR);
    }

    public static <U> F<U> supplyAsync(Supplier<U> supplier, Executor executor)
    {
        return new F<>(CompletableFuture.supplyAsync(supplier, executor));
    }

    public static F<Void> runAsync(Runnable runnable)
    {
        return runAsync(runnable, DEFAULT_EXECUTOR);
    }

    public static F<Void> runAsync(Runnable runnable, Executor executor)
    {
        return new F<>(CompletableFuture.runAsync(runnable, executor));
    }

    public static <U> F<U> completedFuture(U value)
    {
        return new F<>(CompletableFuture.completedFuture(value));
    }

    public <U> F<U> thenApplyAsync(Function<? super T, ? extends U> fn)
    {
        return thenApplyAsync(fn, DEFAULT_EXECUTOR);
    }

    public <U> F<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor)
    {
        return new F<>(this.future.thenApplyAsync(fn, executor));
    }

    public <U, V> F<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn)
    {
        return thenCombineAsync(other, fn, DEFAULT_EXECUTOR);
    }

    public <U, V> F<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor)
    {
        return new F<>(this.future.thenCombineAsync(other, fn, executor));
    }

    public <U> F<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn)
    {
        return thenComposeAsync(fn, DEFAULT_EXECUTOR);
    }

    public <U> F<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor)
    {
        return new F<>(this.future.thenComposeAsync(fn, executor));
    }

    public F<Void> thenAcceptAsync(Consumer<? super T> action)
    {
        return thenAcceptAsync(action, DEFAULT_EXECUTOR);
    }

    public F<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor)
    {
        return new F<>(this.future.thenAcceptAsync(action, executor));
    }

    public F<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action)
    {
        return whenCompleteAsync(action, DEFAULT_EXECUTOR);
    }

    public F<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor)
    {
        return new F<>(this.future.whenCompleteAsync(action, executor));
    }

    public <U> F<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn)
    {
        return handleAsync(fn, DEFAULT_EXECUTOR);
    }

    public <U> F<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor)
    {
        return new F<>(this.future.handleAsync(fn, executor));
    }

    public F<T> exceptionallyAsync(Function<Throwable, ? extends T> fn)
    {
        return exceptionallyAsync(fn, DEFAULT_EXECUTOR);
    }

    public F<T> exceptionallyAsync(Function<Throwable, ? extends T> fn, Executor executor)
    {
        return new F<>(this.future.exceptionallyAsync(fn, executor));
    }

}