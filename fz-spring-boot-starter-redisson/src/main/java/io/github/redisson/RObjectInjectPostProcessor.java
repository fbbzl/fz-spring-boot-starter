package io.github.redisson;

import cn.hutool.core.util.ReflectUtil;
import io.github.redisson.annotation.InjectRObject;
import lombok.RequiredArgsConstructor;
import org.fz.erwin.exception.Throws;
import org.redisson.api.*;
import org.redisson.client.codec.Codec;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EmbeddedValueResolverAware;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.NonNull;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Field;

import static cn.hutool.core.util.ReflectUtil.getFields;


/**
 * use to do {@link InjectRObject} injection
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2025 /2/27 17:06
 */
@RequiredArgsConstructor
public class RObjectInjectPostProcessor implements BeanPostProcessor, EmbeddedValueResolverAware {

    private final RedissonClient      redisson;
    private       StringValueResolver resolver;

    @Override
    public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName) {
        return this.doRObjectInject(bean);
    }

    @Override
    public void setEmbeddedValueResolver(@NonNull StringValueResolver resolver) {this.resolver = resolver;}

    /**
     * do field inject
     *
     * @param bean bean
     */
    Object doRObjectInject(Object bean) {
        for (Field field : getFields(bean.getClass())) {
            InjectRObject injectRObj = AnnotationUtils.findAnnotation(field, InjectRObject.class);
            if (injectRObj == null) continue;

            Class<?> rObjType = field.getType();
            String   rObjName = this.resolveRObjName(injectRObj.value());
            Codec    codec    = newCodec(injectRObj.codec());

            Throws.ifBlank(rObjName, ()->"injected RObject name can not be blank, field: [" + field + "]");
            Throws.ifNotAssignable(RObject.class, rObjType,()-> "the field type is not the sub type of RObject.class, "
                                                            + "inject failed. field: [" + field + "]");

            // do field injection
            ReflectUtil.setFieldValue(bean, field, newRObject(rObjName, rObjType, codec));
        }

        return bean;
    }

    //*********************************************** private start **************************************************//

    @SuppressWarnings("all")
    private RObject newRObject(String rname, Class<?> rtype, Codec codec) {
        // redis stream
        if (rtype == RBinaryStream.class)             return redisson.getBinaryStream(rname);

        // redis bloom filter
        if (rtype == RBloomFilter.class)              return redisson.getBloomFilter(rname, codec);

        // redis atomic
        if (rtype == RLongAdder.class)                return redisson.getLongAdder(rname);
        if (rtype == RDoubleAdder.class)              return redisson.getDoubleAdder(rname);

        // redis atomic
        if (rtype == RAtomicLong.class)               return redisson.getAtomicLong(rname);
        if (rtype == RAtomicDouble.class)             return redisson.getAtomicDouble(rname);

        // redis semaphore
        if (rtype == RSemaphore.class)                return redisson.getSemaphore(rname);
        if (rtype == RPermitExpirableSemaphore.class) return redisson.getPermitExpirableSemaphore(rname);

        // redis multimap
        if (rtype == RListMultimap.class)             return redisson.getListMultimap(rname, codec);
        if (rtype == RSetMultimap.class)              return redisson.getSetMultimap(rname, codec);
        if (rtype == RListMultimapCache.class)        return redisson.getListMultimapCache(rname, codec);
        if (rtype == RSetMultimapCache.class)         return redisson.getSetMultimapCache(rname, codec);

        // redis bucket
        if (rtype == RBucket.class)                   return redisson.getBucket(rname, codec);

        // redis bitSet
        if (rtype == RBitSet.class)                   return redisson.getBitSet(rname);

        // redis list
        if (rtype == RList.class)                     return redisson.getList(rname, codec);

        // redis deque
        if (rtype == RDeque.class)                    return redisson.getDeque(rname, codec);
        if (rtype == RPriorityDeque.class)            return redisson.getPriorityDeque(rname, codec);
        if (rtype == RBlockingDeque.class)            return redisson.getBlockingDeque(rname, codec);
        if (rtype == RPriorityBlockingDeque.class)    return redisson.getPriorityBlockingDeque(rname, codec);

        // redis map
        if (rtype == RMap.class)                      return redisson.getMap(rname, codec, MapOptions.defaults());
        if (rtype == RLocalCachedMap.class)           return redisson.getLocalCachedMap(rname, codec, LocalCachedMapOptions.defaults());
        if (rtype == RMapCache.class)                 return redisson.getMapCache(rname, codec, MapCacheOptions.defaults());

        // redis sorted set
        if (rtype == RScoredSortedSet.class)          return redisson.getScoredSortedSet(rname, codec);
        if (rtype == RLexSortedSet.class)             return redisson.getLexSortedSet(rname);
        if (rtype == RSortedSet.class)                return redisson.getSortedSet(rname, codec);
        if (rtype == RSetCache.class)                 return redisson.getSetCache(rname, codec);
        if (rtype == RSet.class)                      return redisson.getSet(rname, codec);

        // redis Queue
        if (rtype == RQueue.class)                    return redisson.getQueue(rname, codec);
        if (rtype == RBlockingQueue.class)            return redisson.getBlockingQueue(rname, codec);
        if (rtype == RTransferQueue.class)            return redisson.getTransferQueue(rname, codec);
        if (rtype == RPriorityQueue.class)            return redisson.getPriorityQueue(rname, codec);
        if (rtype == RBoundedBlockingQueue.class)     return redisson.getBoundedBlockingQueue(rname, codec);
        if (rtype == RPriorityBlockingQueue.class)    return redisson.getPriorityBlockingQueue(rname, codec);
        if (rtype == RDelayedQueue.class)             return redisson.getDelayedQueue(redisson.getQueue(rname, codec));

        throw new UnsupportedOperationException("do not support type: [" + rtype + "], please check: [" + rname + "]");
    }

    /**
     * get RObject name from Spring EL
     *
     * @param spel expression
     * @return RObject name
     */
    private String resolveRObjName(String spel) {
        return resolver.resolveStringValue(spel);
    }

    /**
     * reflect for Codec
     */
    private Codec newCodec(Class<? extends Codec> codecClass, Object... constructorArgs) {
        try {
            return ReflectUtil.newInstance(codecClass, constructorArgs);
        } catch (Exception initErr) {
            throw new UnsupportedOperationException("redis group may not support this kind of Codec: [" + codecClass + "], please check", initErr);
        }
    }

    //*********************************************** private end ****************************************************//

}
