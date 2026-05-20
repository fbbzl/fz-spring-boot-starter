package io.github.fbbzl.starter.core.util;

import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.*;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/22 14:24
 */
@Slf4j
public class Futures<T>
{

    private static final Executor DEFAULT_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    @Delegate
    private final CompletableFuture<T> future;

    private Futures(CompletableFuture<T> future)
    {
        this.future = future;
    }

    public static <T> Futures<T> of(T value)
    {
        return new Futures<>(CompletableFuture.completedFuture(value));
    }

    public static <T> Futures<T> of(CompletableFuture<T> future)
    {
        return new Futures<>(future);
    }

    public static <U> Futures<U> supplyAsync(Supplier<U> supplier)
    {
        return supplyAsync(supplier, DEFAULT_EXECUTOR);
    }

    public static <U> Futures<U> supplyAsync(Supplier<U> supplier, Executor executor)
    {
        return new Futures<>(CompletableFuture.supplyAsync(supplier, executor));
    }

    public static Futures<Void> runAsync(Runnable runnable)
    {
        return runAsync(runnable, DEFAULT_EXECUTOR);
    }

    public static Futures<Void> runAsync(Runnable runnable, Executor executor)
    {
        return new Futures<>(CompletableFuture.runAsync(runnable, executor));
    }

    public static <U> Futures<U> completedFuture(U value)
    {
        return new Futures<>(CompletableFuture.completedFuture(value));
    }

    public <U> Futures<U> thenApplyAsync(Function<? super T, ? extends U> fn)
    {
        return thenApplyAsync(fn, DEFAULT_EXECUTOR);
    }

    public <U> Futures<U> thenApplyAsync(Function<? super T, ? extends U> fn, Executor executor)
    {
        return new Futures<>(this.future.thenApplyAsync(fn, executor));
    }

    public <U, V> Futures<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn)
    {
        return thenCombineAsync(other, fn, DEFAULT_EXECUTOR);
    }

    public <U, V> Futures<V> thenCombineAsync(CompletionStage<? extends U> other, BiFunction<? super T, ? super U, ? extends V> fn, Executor executor)
    {
        return new Futures<>(this.future.thenCombineAsync(other, fn, executor));
    }

    public <U> Futures<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn)
    {
        return thenComposeAsync(fn, DEFAULT_EXECUTOR);
    }

    public <U> Futures<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn, Executor executor)
    {
        return new Futures<>(this.future.thenComposeAsync(fn, executor));
    }

    public Futures<Void> thenAcceptAsync(Consumer<? super T> action)
    {
        return thenAcceptAsync(action, DEFAULT_EXECUTOR);
    }

    public Futures<Void> thenAcceptAsync(Consumer<? super T> action, Executor executor)
    {
        return new Futures<>(this.future.thenAcceptAsync(action, executor));
    }

    public Futures<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action)
    {
        return whenCompleteAsync(action, DEFAULT_EXECUTOR);
    }

    public Futures<T> whenCompleteAsync(BiConsumer<? super T, ? super Throwable> action, Executor executor)
    {
        return new Futures<>(this.future.whenCompleteAsync(action, executor));
    }

    public <U> Futures<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn)
    {
        return handleAsync(fn, DEFAULT_EXECUTOR);
    }

    public <U> Futures<U> handleAsync(BiFunction<? super T, Throwable, ? extends U> fn, Executor executor)
    {
        return new Futures<>(this.future.handleAsync(fn, executor));
    }

    public Futures<T> exceptionallyAsync(Function<Throwable, ? extends T> fn)
    {
        return exceptionallyAsync(fn, DEFAULT_EXECUTOR);
    }

    public Futures<T> exceptionallyAsync(Function<Throwable, ? extends T> fn, Executor executor)
    {
        return new Futures<>(this.future.exceptionallyAsync(fn, executor));
    }

}
