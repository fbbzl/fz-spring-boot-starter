package io.github.fbbzl.starter.webflux;


import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.OperateTemplate;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Order;
import io.github.fbbzl.starter.core.exception.ExceptionVerb;
import io.github.fbbzl.starter.core.util.Generics;
import io.github.fbbzl.starter.dal.BaseDal;
import io.github.fbbzl.starter.dal.Range;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.mapstruct.BaseCrudStructMapper;
import io.github.fbbzl.starter.pojo.tree.Treeable;
import io.github.fbbzl.starter.pojo.validation.Validators;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import io.github.fbbzl.starter.pojo.validation.group.CRUD.R;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import io.github.fbbzl.starter.core.util.Throws;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.collection.CollUtil.newHashSet;
import static cn.hutool.core.lang.tree.TreeNodeConfig.DEFAULT_CONFIG;
import static cn.hutool.core.util.ObjectUtil.hasNull;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @param <ENTITY> request data type
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 15:00
 */

@Validated
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseCrudService<
        ID            extends Serializable,
        ENTITY        extends BaseTableEntity<ID>,
        DTO           extends BaseDto<ID>,
        BO            extends BaseBo<ID>,
        DAL           extends BaseDal<ENTITY, ID>,
        STRUCT_MAPPER extends BaseCrudStructMapper<ENTITY, DTO, BO>> implements BeanNameAware, Container<ID>
{
    Class<ENTITY> entityClass = Generics.getGenericSuperType(this.getClass(), BaseCrudService.class, 1);
    Class<ENTITY> dtoClass    = Generics.getGenericSuperType(this.getClass(), BaseCrudService.class, 2);
    Class<ENTITY> boClass     = Generics.getGenericSuperType(this.getClass(), BaseCrudService.class, 3);

    @Autowired
    DAL             dal;
    @Autowired
    STRUCT_MAPPER   struct;
    @Setter
    String          beanName;
    @Autowired
    OperateTemplate operateTemplate;

    @Autowired
    @Lazy
    BaseCrudService<ID, ENTITY, DTO, BO, DAL, STRUCT_MAPPER> self;

    // Expose the mapper for extension points that need explicit object conversion.
    public STRUCT_MAPPER struct()
    {
        return struct;
    }

    // Provide the data assembly namespace for crane4j containers.
    @Override
    public String getNamespace()
    {
        Throws.ifBlank(beanName, "beanName can not be blank");
        // bean name as namespace
        return beanName;
    }

    // Load BO records for crane4j data assembly by ids.
    @Override
    public Map<ID, BO> get(Collection<ID> ids)
    {
        return self.map(newHashSet(ids));
    }

    // Query one business object by primary key.
    @Nullable
    public BO byId(
            @NotNull(message = "id can not be null when doing id-query")
            ID id)
    {
        ENTITY entity = dal.byId(id);
        if (entity != null) return struct.entityToBo(entity);
        else                throw ExceptionVerb.RESOURCE_NOT_FOUND.on(entityClass, id).get();
    }

    // Query business objects by a bounded primary-key set.
    public List<BO> byIds(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Set<ID> ids)
    {
        if (isEmpty(ids)) return emptyList();
        else return struct.entityToBo(dal.byIds(ids));
    }

    // Query business objects by ids and index them by primary key.
    public Map<ID, BO> map(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Set<ID> ids)
    {
        if (isEmpty(ids)) return emptyMap();
        else return struct.entityToBo(dal.byIds(ids)).stream().collect(toMap(BaseBo::getId, identity()));
    }

    // Query all matching business objects with the default maximum limit.
    public List<BO> list(
            @Validated(CRUD.R.class)
            DTO dto)
    {
        return self.list(dto, Integer.MAX_VALUE, null, null);
    }

    // Query matching business objects with a caller-specified limit.
    public List<BO> list(
            @Validated(CRUD.R.class)
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit)
    {
        return self.list(dto, limit, null, null);
    }

    // Query matching business objects with limit and ordering.
    public List<BO> list(
            @Validated(CRUD.R.class)
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order[] orders)
    {
        return self.list(dto, limit, orders, null);
    }

    // Query matching business objects with limit and range filters.
    public List<BO> list(
            @Validated(CRUD.R.class)
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit,
            @Size(max = 1024, message = "the number of ranges cannot exceed 1024")
            Range[] ranges)
    {
        return self.list(dto, limit, null, ranges);
    }

    // Query matching business objects with range filters.
    public List<BO> list(
            @Validated(CRUD.R.class)
            DTO dto,
            @Size(max = 1024, message = "the number of ranges cannot exceed 1024")
            Range[] ranges)
    {
        return self.list(dto, Integer.MAX_VALUE, null, ranges);
    }

    // Query matching business objects with ordering.
    public List<BO> list(
            @Validated(CRUD.R.class)
            DTO dto,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order[] orders)
    {
        return self.list(dto, Integer.MAX_VALUE, orders, null);
    }

    // Query matching business objects with full list options.
    public List<BO> list(
            @Validated(CRUD.R.class)
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order[] orders,
            @Size(max = 1024, message = "the number of ranges cannot exceed 1024")
            Range[] ranges)
    {
        if (dto == null) return emptyList();
        else return struct.entityToBo(dal.list(struct.dtoToEntity(dto), limit, orders, ranges));
    }

    // Query ids for matching business objects.
    public List<ID> ids(
            @Validated(CRUD.R.class)
            DTO dto)
    {
        if (dto == null) return emptyList();
        else return dal.ids(struct.dtoToEntity(dto), Integer.MAX_VALUE);
    }

    // Query ids for matching business objects with a caller-specified limit.
    public List<ID> ids(
            @Validated(CRUD.R.class)
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit)
    {
        if (dto == null) return emptyList();
        else return dal.ids(struct.dtoToEntity(dto), limit);
    }

    // Query a page of matching business objects.
    public PageResult<BO> page(
            @NotNull(message = "page can not be null when doing page-query")
            Page page,
            @Validated(R.class)
            DTO dto)
    {
        if (hasNull(dto, page)) return emptyPage();
        else return mappingPage(dal.page(page, struct.dtoToEntity(dto)), struct::entityToBo);
    }

    // Query a tree from matching business objects.
    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            @Validated(CRUD.R.class)
            DTO dto)
    {
        return self.tree(rootId, dto, Integer.MAX_VALUE, null, null);
    }

    // Query a tree with a caller-specified limit.
    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            @Validated(CRUD.R.class)
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit)
    {
        return self.tree(rootId, dto, limit, null, null);
    }

    // Query a tree with limit and ordering.
    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            @Validated(CRUD.R.class)
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order[] orders)
    {
        return self.tree(rootId, dto, limit, orders, null);
    }

    // Query a tree with limit and range filters.
    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            @Validated(CRUD.R.class)
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit,
            @Size(max = 1024, message = "the number of ranges cannot exceed 1024")
            Range[] ranges)
    {
        return self.tree(rootId, dto, limit, null, ranges);
    }

    // Query a tree with range filters.
    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            @Validated(CRUD.R.class)
            DTO dto,
            @Size(max = 1024, message = "the number of ranges cannot exceed 1024")
            Range[] ranges)
    {
        return self.tree(rootId, dto, Integer.MAX_VALUE, null, ranges);
    }

    // Query a tree with ordering.
    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            @Validated(CRUD.R.class)
            DTO dto,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order[] orders)
    {
        return self.tree(rootId, dto, Integer.MAX_VALUE, orders, null);
    }

    // Query a tree with full tree-list options.
    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            @Validated(CRUD.R.class)
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order[] orders,
            @Size(max = 1024, message = "the number of ranges cannot exceed 1024")
            Range[] ranges)
    {
        if (Treeable.class.isAssignableFrom(boClass)) {
            List<BO> list = this.list(dto, limit, orders, ranges);
            operateTemplate.execute(list);
            return TreeUtil.build(list, rootId, DEFAULT_CONFIG, (bo, tree) ->
            {
                tree.setId(bo.getId());

                @SuppressWarnings("unchecked")
                Treeable<ID> treeNodeBo = (Treeable<ID>) bo;
                tree.setParentId(treeNodeBo.getParentId());

                tree.putExtra("data", bo);
            });
        }

        return emptyList();
    }

    // Check whether a primary key exists.
    public boolean exists(
            @NotNull(message = "id can not be null when doing id-exist-query")
            @Validated(CRUD.R.class)
            ID id)
    {
        return dal.exists(id);
    }

    // Check whether data matching the DTO exists.
    public boolean exists(
            @NotNull(message = "data can not be null when doing data-exist-query")
            @Validated(CRUD.R.class)
            DTO dto)
    {
        return dal.exists(struct.dtoToEntity(dto));
    }

    // Create one business object.
    public BO create(
            @NotNull(message = "data can not be null when doing create")
            @Validated(CRUD.C.class)
            DTO dto)
    {
        return struct.entityToBo(dal.create(struct.dtoToEntity(dto)));
    }

    // Create a batch of business objects.
    public List<BO> create(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos)
    {
        if (isEmpty(dtos)) return emptyList();
        Validators.validateAndThrow(dtos, CRUD.C.class);
        List<ENTITY> entities = struct.dtoToEntity(dtos);
        return struct.entityToBo(dal.create(entities));
    }

    public List<BO> create(
            @Size(max = 1024, message = "the number of array cannot exceed 1024")
            DTO[] dtos)
    {
        if (dtos == null || dtos.length == 0) return emptyList();
        Validators.validateAndThrow(dtos, CRUD.C.class);
        ENTITY[] entities = struct.dtoToEntity(dtos);
        return struct.entityToBo(dal.create(Arrays.asList(entities)));
    }

    // Update one business object.
    public BO update(
            @NotNull(message = "data can not be null when doing update")
            @Validated(CRUD.U.class)
            DTO dto)
    {
        return struct.entityToBo(dal.update(struct.dtoToEntity(dto)));
    }

    // Update a batch of business objects.
    public void update(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos)
    {
        if (isEmpty(dtos)) return;
        Validators.validateAndThrow(dtos, CRUD.U.class);
        dal.update(struct.dtoToEntity(dtos));
    }

    // Delete one business object by primary key.
    public void delete(
            @NotNull(message = "data can not be null when doing delete")
            ID id)
    {
        dal.delete(id);
    }

    // Delete a batch of business objects by primary key.
    public void delete(
            @NotNull(message = "data can not be null when doing delete")
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Set<ID> ids)
    {
        dal.delete(ids);
    }

    static <SOURCE, TARGET> PageResult<TARGET> mappingPage(
            PageResult<SOURCE> sourcePage,
            Function<List<SOURCE>, List<TARGET>> mapper)
    {
        PageResult<TARGET> targetPage = new PageResult<>(sourcePage.getPage(), sourcePage.getPageSize(), sourcePage.getTotal());
        targetPage.addAll(mapper.apply(sourcePage));
        return targetPage;
    }

    <DATA> PageResult<DATA> emptyPage()
    {
        return new PageResult<>();
    }

}
