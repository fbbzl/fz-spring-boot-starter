package io.github.fbbzl.starter.webflux;


import cn.crane4j.core.container.Container;
import cn.crane4j.core.executor.AsyncBeanOperationExecutor;
import cn.crane4j.core.support.Grouped;
import cn.crane4j.core.support.OperateTemplate;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeNodeConfig;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import cn.hutool.db.sql.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.fbbzl.starter.core.exception.ExceptionVerb;
import io.github.fbbzl.starter.core.util.Generics;
import io.github.fbbzl.starter.core.util.Throws;
import io.github.fbbzl.starter.dal.BaseDal;
import io.github.fbbzl.starter.dal.Range;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.dto.Prepare;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.*;
import java.util.function.Function;

import jakarta.validation.ConstraintViolation;
import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.collection.CollUtil.newHashSet;
import static cn.hutool.core.lang.tree.TreeNodeConfig.DEFAULT_CONFIG;
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
@Slf4j
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
    Class<DTO>    dtoClass    = Generics.getGenericSuperType(this.getClass(), BaseCrudService.class, 2);
    Class<BO>     boClass     = Generics.getGenericSuperType(this.getClass(), BaseCrudService.class, 3);

    @Autowired
    DAL             dal;
    @Autowired
    STRUCT_MAPPER   struct;
    @Autowired
    ObjectMapper     objectMapper;
    @Setter
    String          beanName;
    @Autowired
    OperateTemplate operateTemplate;
    @Autowired
    AsyncBeanOperationExecutor asyncBeanOperationExecutor;

    public STRUCT_MAPPER struct() {
        return struct;
    }

    @Override
    public String getNamespace()
    {
        Throws.ifBlank(beanName, "beanName can not be blank");
        // bean name as namespace
        return beanName;
    }

    @Override
    public Map<ID, BO> get(Collection<ID> ids)
    {
        return map(ids);
    }

    @Nullable
    public BO byId(
            @NotNull(message = "id can not be null when doing id-query")
            ID id)
    {
        ENTITY entity = dal.byId(id);
        if (entity != null) return struct.entityToBo(entity);
        else                throw ExceptionVerb.RESOURCE_NOT_FOUND.on(entityClass, id).get();
    }

    public List<BO> byIds(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<ID> ids)
    {
        if (isEmpty(ids)) return emptyList();
        else return struct.entityToBo(dal.byIds(ids));
    }

    public Map<ID, BO> map(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<ID> ids)
    {
        if (isEmpty(ids)) return emptyMap();
        else return struct.entityToBo(dal.byIds(newHashSet(ids))).stream().collect(toMap(BaseBo::getId, identity()));
    }

    public List<BO> list(
            DTO dto)
    {
        return list(dto, Integer.MAX_VALUE, null, null);
    }

    public List<BO> list(
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit)
    {
        return list(dto, limit, null, null);
    }

    public List<BO> list(
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order[] orders)
    {
        return list(dto, limit, orders, null);
    }

    public List<BO> list(
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit,
            @Size(max = 1024, message = "the number of ranges cannot exceed 1024")
            Range[] ranges)
    {
        return list(dto, limit, null, ranges);
    }

    public List<BO> list(
            DTO dto,
            @Size(max = 1024, message = "the number of ranges cannot exceed 1024")
            Range[] ranges)
    {
        return list(dto, Integer.MAX_VALUE, null, ranges);
    }

    public List<BO> list(
            DTO dto,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order[] orders)
    {
        return list(dto, Integer.MAX_VALUE, orders, null);
    }

    public List<BO> list(
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order[] orders,
            @Size(max = 1024, message = "the number of ranges cannot exceed 1024")
            Range[] ranges)
    {
        if (dto == null) return emptyList();
        dto.prepareQuery();
        Validators.validateAndThrow(dto, CRUD.R.class);
        return struct.entityToBo(dal.list(struct.dtoToEntity(dto), limit, orders, ranges));
    }

    public List<ID> ids(
            DTO dto)
    {
        if (dto == null) return emptyList();
        dto.prepareQuery();
        Validators.validateAndThrow(dto, CRUD.R.class);
        return dal.ids(struct.dtoToEntity(dto), Integer.MAX_VALUE);
    }

    public List<ID> ids(
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit)
    {
        if (dto == null) return emptyList();
        dto.prepareQuery();
        Validators.validateAndThrow(dto, CRUD.R.class);
        return dal.ids(struct.dtoToEntity(dto), limit);
    }

    public PageResult<BO> page(
            @NotNull(message = "page can not be null when doing page-query")
            Page page,
            DTO dto)
    {
        if (dto == null) return emptyPage();
        dto.prepareQuery();
        Validators.validateAndThrow(dto, R.class);
        return mappingPage(dal.page(page, struct.dtoToEntity(dto)), struct::entityToBo);
    }

    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            DTO dto)
    {
        return tree(rootId, dto, Integer.MAX_VALUE, null, null);
    }

    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit)
    {
        return tree(rootId, dto, limit, null, null);
    }

    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order[] orders)
    {
        return tree(rootId, dto, limit, orders, null);
    }

    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit,
            @Size(max = 1024, message = "the number of ranges cannot exceed 1024")
            Range[] ranges)
    {
        return tree(rootId, dto, limit, null, ranges);
    }

    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            DTO dto,
            @Size(max = 1024, message = "the number of ranges cannot exceed 1024")
            Range[] ranges)
    {
        return tree(rootId, dto, Integer.MAX_VALUE, null, ranges);
    }

    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            DTO dto,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order[] orders)
    {
        return tree(rootId, dto, Integer.MAX_VALUE, orders, null);
    }

    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
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
            operateTemplate.execute(list, asyncBeanOperationExecutor, Grouped.alwaysMatch());
            return TreeUtil.build(list, rootId, treeNodeConfig(), (bo, tree) ->
            {
                tree.setId(bo.getId());

                @SuppressWarnings("unchecked")
                Treeable<ID> treeNodeBo = (Treeable<ID>) bo;
                tree.setParentId(treeNodeBo.getNodeParentId());

                tree.putExtra("data", bo);
            });
        }

        log.warn("Tree query ignored because BO type [{}] does not implement [{}]", boClass.getName(), Treeable.class.getName());
        return emptyList();
    }

    public boolean exists(
            @NotNull(message = "id can not be null when doing id-exist-query")
            @Validated(CRUD.R.class)
            ID id)
    {
        return dal.exists(id);
    }

    public boolean exists(
            @NotNull(message = "data can not be null when doing data-exist-query")
            DTO dto)
    {
        dto.prepareQuery();
        Validators.validateAndThrow(dto, CRUD.R.class);
        return dal.exists(struct.dtoToEntity(dto));
    }

    public long count(
            @NotNull(message = "data can not be null when doing count")
            DTO dto)
    {
        dto.prepareQuery();
        Validators.validateAndThrow(dto, CRUD.R.class);
        return dal.count(struct.dtoToEntity(dto));
    }

    public List<Map<String, Object>> diff(
            @NotNull(message = "id can not be null when doing diff")
            ID id,
            @NotNull(message = "data can not be null when doing diff")
            DTO dto)
    {
        BO          current    = byId(id);
        DTO         currentDto = struct.boToDto(current);
        Map<String, Object> currentMap = objectMapper.convertValue(currentDto, Map.class);
        Map<String, Object> newMap     = objectMapper.convertValue(dto, Map.class);
        if (currentMap == null || newMap == null) return List.of();

        List<Map<String, Object>> diffs = new ArrayList<>();
        for (Map.Entry<String, Object> entry : newMap.entrySet()) {
            String field  = entry.getKey();
            Object oldVal = currentMap.get(field);
            Object newVal = entry.getValue();
            if (!Objects.equals(oldVal, newVal)) {
                Map<String, Object> diff = new HashMap<>();
                diff.put("field",    field);
                diff.put("oldValue", oldVal);
                diff.put("newValue", newVal);
                diffs.add(diff);
            }
        }
        return diffs;
    }

    @Transactional
    public BO create(
            @NotNull(message = "data can not be null when doing create")
            DTO dto)
    {
        dto.prepareCreate();
        Validators.validateAndThrow(dto, CRUD.C.class);
        return struct.entityToBo(dal.create(struct.dtoToEntity(dto)));
    }

    @Transactional
    public List<BO> create(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos)
    {
        if (isEmpty(dtos)) return emptyList();
        dtos.forEach(Prepare::prepareCreate);
        Validators.validateAndThrow(dtos, CRUD.C.class);
        List<ENTITY> entities = struct.dtoToEntity(dtos);
        return struct.entityToBo(dal.create(entities));
    }

    @Transactional
    public BO update(
            @NotNull(message = "data can not be null when doing update")
            DTO dto)
    {
        dto.prepareUpdate();
        Validators.validateAndThrow(dto, CRUD.U.class);
        return struct.entityToBo(dal.update(struct.dtoToEntity(dto)));
    }

    @Transactional
    public void update(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos)
    {
        if (isEmpty(dtos)) return;
        dtos.forEach(Prepare::prepareUpdate);
        Validators.validateAndThrow(dtos, CRUD.U.class);
        dal.update(struct.dtoToEntity(dtos));
    }

    @Transactional
    public BO patch(
            @NotNull(message = "data can not be null when doing update")
            Map<String, Object> data)
    {
        DTO dto = objectMapper.convertValue(data, dtoClass);
        Throws.ifNull(dto.getId(), "id can not be null when doing update");
        Validators.validateNonNullPropertyAndThrow(dto, CRUD.U.class);
        return struct.entityToBo(dal.update(struct.dtoToEntity(dto)));
    }

    @Transactional
    public void delete(
            @NotNull(message = "data can not be null when doing delete")
            DTO dto)
    {
        dto.prepareDelete();
        Validators.validateAndThrow(dto, CRUD.D.class);
        Throws.ifNull(dto.getId(), "id can not be null when doing delete");
        List<ID> ids = ids(dto);
        if (isEmpty(ids)) return;

        dal.delete(ids);
    }

    @Transactional
    public void delete(
            @NotNull(message = "data can not be null when doing delete")
            ID id)
    {
        dal.delete(id);
    }

    @Transactional
    public void delete(
            @NotNull(message = "data can not be null when doing delete")
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<ID> ids)
    {
        dal.delete(ids);
    }

    protected TreeNodeConfig treeNodeConfig()
    {
        return DEFAULT_CONFIG;
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
