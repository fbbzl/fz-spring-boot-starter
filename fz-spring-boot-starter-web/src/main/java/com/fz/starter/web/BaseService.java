package com.fz.starter.web;


import cn.crane4j.core.container.Container;
import cn.crane4j.core.support.OperateTemplate;
import cn.hutool.core.lang.tree.Tree;
import cn.hutool.core.lang.tree.TreeUtil;
import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import com.fz.starter.core.util.Generics;
import com.fz.starter.dal.BaseDal;
import com.fz.starter.pojo.bo.BaseBo;
import com.fz.starter.pojo.dto.BaseDto;
import com.fz.starter.pojo.entity.BaseTableEntity;
import com.fz.starter.pojo.eo.BaseEo;
import com.fz.starter.pojo.mapstruct.BaseStructMapper;
import com.fz.starter.pojo.tree.Treeable;
import com.fz.starter.pojo.validation.Validators;
import com.fz.starter.pojo.validation.group.CRUD;
import com.fz.starter.pojo.validation.group.CRUD.R;
import jakarta.validation.constraints.NotNull;
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
        ID     extends Serializable,
        ENTITY extends BaseTableEntity<ID>,
        DTO    extends BaseDto<ID>,
        BO     extends BaseBo<ID>,
        EO     extends BaseEo,
        DAL    extends BaseDal<ENTITY, ID>,
        STRUCT_MAPPER extends BaseStructMapper<ENTITY, DTO, BO, EO>> implements BeanNameAware, Container<ID>
{

    Class<ENTITY> entityClass = Generics.getGenericSuperType(this.getClass(), BaseService.class, 1);
    Class<ENTITY> dtoClass    = Generics.getGenericSuperType(this.getClass(), BaseService.class, 2);
    Class<ENTITY> boClass     = Generics.getGenericSuperType(this.getClass(), BaseService.class, 3);
    Class<ENTITY> eoClass     = Generics.getGenericSuperType(this.getClass(), BaseService.class, 4);

    @Autowired DAL             dal;
    @Autowired STRUCT_MAPPER   mapper;
    @Setter    String          beanName;
    @Autowired OperateTemplate operateTemplate;

    @Autowired
    @Lazy BaseService<ID, ENTITY, DTO, BO, EO, DAL, STRUCT_MAPPER> self;

    @Override
    public String getNamespace()
    {
        Throws.ifBlank(beanName, () -> "beanName can not be blank");
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
        if (entity != null) return mapper.entityToBo(entity);
        else                return null;
    }

    public List<BO> byIds(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Set<ID> ids)
    {
        if (isEmpty(ids)) return emptyList();
        else              return mapper.entityToBo(dal.byIds(ids));
    }

    public Map<ID, BO> map(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Set<ID> ids)
    {
        if (isEmpty(ids)) return emptyMap();
        else              return mapper.entityToBo(dal.byIds(ids)).stream().collect(toMap(BaseBo::getId, identity()));
    }

    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            @Validated(CRUD.R.class)
            DTO dto)
    {
        if (Treeable.class.isAssignableFrom(boClass))
        {
            List<BO> list = this.list(dto);
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

    public List<BO> list(@Validated(CRUD.R.class) DTO dto)
    {
        if (dto == null) return emptyList();
        else             return mapper.entityToBo(dal.list(mapper.dtoToEntity(dto)));
    }

    public List<BO> limit(@Validated(CRUD.R.class) DTO dto, int limit)
    {
        if (dto == null) return emptyList();
        else             return mapper.entityToBo(dal.limit(mapper.dtoToEntity(dto), limit));
    }

    public Map<ID, BO> map(
            @Validated(CRUD.R.class) DTO dto)
    {
        if (dto == null) return emptyMap();
        else             return dal.list(mapper.dtoToEntity(dto)).stream().collect(toMap(BaseTableEntity::getId, mapper::entityToBo));
    }

    public PageResult<BO> page(
            @NotNull(message = "page can not be null when doing page-query")
            Page page,
            @Validated(R.class)
            DTO dto)
    {
        if (hasNull(dto, page)) return emptyPage();
        else                    return mappingPage(dal.page(page, mapper.dtoToEntity(dto)), mapper::entityToBo);
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
        return dal.exists(mapper.dtoToEntity(dto));
    }

    public List<EO> exportExcel(
            @Validated(CRUD.R.class)
            DTO dto)
    {
        if (dto == null) return emptyList();
        else             return mapper.boToEo(this.list(dto));
    }

    public int importExcel(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<EO> eos)
    {
        if (isEmpty(eos)) return 0;
        Validators.validateAndThrow(eos, CRUD.C.class);
        return dal.create(mapper.eoToEntity(eos));
    }

    @Nullable
    public BO create(
            @Validated(CRUD.C.class)
            DTO dto)
    {
        if (dto == null) return null;
        else             return mapper.entityToBo(dal.create(mapper.dtoToEntity(dto)));
    }

    public int create(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos)
    {
        if (isEmpty(dtos)) return 0;
        Validators.validateAndThrow(dtos, CRUD.C.class);
        return dal.create(mapper.dtoToEntity(dtos));
    }

    public int update(
            @Validated(CRUD.U.class)
            DTO dto)
    {
        if (dto == null) return 0;
        else             return dal.update(mapper.dtoToEntity(dto));
    }

    public int update(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos)
    {
        if (isEmpty(dtos)) return 0;
        Validators.validateAndThrow(dtos, CRUD.U.class);
        return dal.update(mapper.dtoToEntity(dtos));
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
