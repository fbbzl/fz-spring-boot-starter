package io.github.fbbzl.starter.jpa;

import cn.hutool.db.sql.Direction;
import cn.hutool.db.sql.Order;
import io.github.fbbzl.starter.jpa.entity.TestSpecEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class SpecificationsOrderTest
{

    @Test
    void shouldNotCallOrderByWhenAllOrdersAreInvalid()
    {
        TestSpecEntity query = new TestSpecEntity();
        query.setAge(20);

        Order invalidOrder = new Order("nonExistentField", Direction.ASC);

        EntityManager              entityManager = mock(EntityManager.class);
        Metamodel                  metamodel     = mock(Metamodel.class);
        EntityType<TestSpecEntity> entityType    = mock(EntityType.class);
        CriteriaBuilder            cb            = mock(CriteriaBuilder.class);
        @SuppressWarnings("unchecked")
        CriteriaQuery<TestSpecEntity> criteriaQuery = mock(CriteriaQuery.class);
        @SuppressWarnings("unchecked")
        Root<TestSpecEntity>  root        = mock(Root.class);
        Predicate             conjunction = mock(Predicate.class);

        when(entityManager.getMetamodel()).thenReturn(metamodel);
        when(metamodel.entity(TestSpecEntity.class)).thenReturn(entityType);
        when(entityType.getAttributes()).thenReturn(Collections.emptySet());
        when(cb.conjunction()).thenReturn(conjunction);
        when(root.getModel()).thenReturn(entityType);
        when(entityType.getSingularAttribute("nonExistentField")).thenThrow(new IllegalArgumentException());

        Specification<TestSpecEntity> spec = Specifications.byAuto(entityManager, query, invalidOrder);
        spec.toPredicate(root, criteriaQuery, cb);

        verify(criteriaQuery, never()).orderBy(anyList());
    }
}
