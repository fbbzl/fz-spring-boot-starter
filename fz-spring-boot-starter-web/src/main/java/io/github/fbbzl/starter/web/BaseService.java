package io.github.fbbzl.starter.web;


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
import io.github.fbbzl.starter.excel.BaseEo;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.mapstruct.BaseStructMapper;
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
import org.fz.erwin.exception.Throws;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
public abstract class BaseService<
        ID extends Serializable,
        ENTITY extends BaseTableEntity<ID>,
        DTO extends BaseDto<ID>,
        BO extends BaseBo<ID>,
        EO extends BaseEo,
        DAL extends BaseDal<ENTITY, ID>,
        STRUCT_MAPPER extends BaseStructMapper<ENTITY, DTO, BO, EO>> implements BeanNameAware, Container<ID>
{
    Class<ENTITY> entityClass = Generics.getGenericSuperType(this.getClass(), BaseService.class, 1);
    Class<ENTITY> dtoClass    = Generics.getGenericSuperType(this.getClass(), BaseService.class, 2);
    Class<ENTITY> boClass     = Generics.getGenericSuperType(this.getClass(), BaseService.class, 3);
    Class<ENTITY> eoClass     = Generics.getGenericSuperType(this.getClass(), BaseService.class, 4);

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
    BaseService<ID, ENTITY, DTO, BO, EO, DAL, STRUCT_MAPPER> self;

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
        return self.map(newHashSet(ids));
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
            Set<ID> ids)
    {
        if (isEmpty(ids)) return emptyList();
        else return struct.entityToBo(dal.byIds(ids));
    }

    public Map<ID, BO> map(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Set<ID> ids)
    {
        if (isEmpty(ids)) return emptyMap();
        else return struct.entityToBo(dal.byIds(ids)).stream().collect(toMap(BaseBo::getId, identity()));
    }

    public List<BO> list(
            @Validated(CRUD.R.class)
            DTO dto)
    {
        return self.list(dto, Integer.MAX_VALUE, null, null);
    }

    public List<BO> list(
            @Validated(CRUD.R.class)
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit)
    {
        return self.list(dto, limit, null, null);
    }

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

    public List<BO> list(
            @Validated(CRUD.R.class)
            DTO dto,
            @Size(max = 1024, message = "the number of ranges cannot exceed 1024")
            Range[] ranges)
    {
        return self.list(dto, Integer.MAX_VALUE, null, ranges);
    }

    public List<BO> list(
            @Validated(CRUD.R.class)
            DTO dto,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order[] orders)
    {
        return self.list(dto, Integer.MAX_VALUE, orders, null);
    }

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

    public List<ID> ids(
            @Validated(CRUD.R.class)
            DTO dto)
    {
        if (dto == null) return emptyList();
        else return dal.ids(struct.dtoToEntity(dto), Integer.MAX_VALUE);
    }

    public List<ID> ids(
            @Validated(CRUD.R.class)
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit)
    {
        if (dto == null) return emptyList();
        else return dal.ids(struct.dtoToEntity(dto), limit);
    }

    public PageResult<BO> page(
            @NotNull(message = "page can not be null when doing page-query")
            Page page,
            @Validated(R.class)
            DTO dto)
    {
        if (hasNull(dto, page)) return emptyPage();
        else return mappingPage(dal.page(page, struct.dtoToEntity(dto)), struct::entityToBo);
    }

    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            @Validated(CRUD.R.class)
            DTO dto)
    {
        return self.tree(rootId, dto, Integer.MAX_VALUE, null, null);
    }

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

    public boolean exists(
            @NotNull(message = "id can not be null when doing id-exist-query")
            @Validated(CRUD.R.class)
            ID id)
    {
        return dal.exists(id);
    }

    public boolean exists(
            @NotNull(message = "data can not be null when doing data-exist-query")
            @Validated(CRUD.R.class)
            DTO dto)
    {
        return dal.exists(struct.dtoToEntity(dto));
    }

    public List<EO> exportExcel(
            @Validated(CRUD.R.class)
            DTO dto,
            @Positive(message = "limit must be positive")
            Integer limit,
            @Size(max = 1024, message = "the number of order cannot exceed 1024")
            Order... orders)
    {
        if (dto == null) return emptyList();
        else return struct.boToEo(this.list(dto, limit, orders));
    }

    public void importExcel(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<EO> eos)
    {
        if (isEmpty(eos)) return;
        Validators.validateAndThrow(eos, CRUD.C.class);
        dal.create(struct.eoToEntity(eos));
    }

    public BO create(
            @NotNull(message = "data can not be null when doing create")
            @Validated(CRUD.C.class)
            DTO dto)
    {
        return struct.entityToBo(dal.create(struct.dtoToEntity(dto)));
    }

    public void create(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos)
    {
        if (isEmpty(dtos)) return;
        Validators.validateAndThrow(dtos, CRUD.C.class);
        dal.create(struct.dtoToEntity(dtos));
    }

    public BO update(
            @NotNull(message = "data can not be null when doing update")
            @Validated(CRUD.U.class)
            DTO dto)
    {
        return struct.entityToBo(dal.update(struct.dtoToEntity(dto)));
    }

    public void update(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos)
    {
        if (isEmpty(dtos)) return;
        Validators.validateAndThrow(dtos, CRUD.U.class);
        dal.update(struct.dtoToEntity(dtos));
    }

    public void delete(
            @NotNull(message = "data can not be null when doing delete")
            ID id)
    {
        dal.delete(id);
    }

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
