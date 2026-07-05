package io.github.fbbzl.starter.dal;

import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BaseDalTest
{

    @Test
    void shouldNotInvokeConsumerWhenTotalIsZero()
    {
        BaseDal<DummyEntity, Long> dal = mock(BaseDal.class, CALLS_REAL_METHODS);
        PageResult<DummyEntity> emptyPage = new PageResult<>(0, 10, 0);
        when(dal.page(any(Page.class), any())).thenReturn(emptyPage);

        AtomicInteger counter = new AtomicInteger(0);
        dal.doBatchConsume(null, 10, list -> counter.incrementAndGet());

        assertThat(counter.get()).isZero();
    }

    static class DummyEntity implements BaseTableEntity<Long>
    {
        @Override
        public Long getId() { return null; }

        @Override
        public void setId(Long id) { }

        @Override
        public LocalDateTime getCreateTime() { return null; }

        @Override
        public void setCreateTime(LocalDateTime createTime) { }

        @Override
        public Long getCreateBy() { return null; }

        @Override
        public void setCreateBy(Long createBy) { }

        @Override
        public LocalDateTime getUpdateTime() { return null; }

        @Override
        public void setUpdateTime(LocalDateTime updateTime) { }

        @Override
        public Long getUpdateBy() { return null; }

        @Override
        public void setUpdateBy(Long updateBy) { }
    }
}
