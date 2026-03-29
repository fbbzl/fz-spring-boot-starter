package com.fz.starter.web;


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
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static cn.hutool.core.collection.CollUtil.getFirst;
import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.lang.tree.TreeNodeConfig.DEFAULT_CONFIG;
import static cn.hutool.core.util.ObjectUtil.hasNull;
import static java.util.Collections.*;
import static java.util.function.UnaryOperator.identity;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

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
        STRUCT_MAPPER extends BaseStructMapper<ENTITY, DTO, BO, EO>>
{

    Class<ENTITY> entityClass = Generics.getGenericSuperType(this.getClass(), BaseService.class, 1);
    Class<ENTITY> dtoClass    = Generics.getGenericSuperType(this.getClass(), BaseService.class, 2);
    Class<ENTITY> boClass     = Generics.getGenericSuperType(this.getClass(), BaseService.class, 3);
    Class<ENTITY> eoClass     = Generics.getGenericSuperType(this.getClass(), BaseService.class, 4);

    @Autowired DAL           dal;
    @Autowired STRUCT_MAPPER mapper;

    public List<BO> list(@Validated(CRUD.R.class) DTO dto)
    {
        if (dto == null) return emptyList();

        ENTITY       entity   = mapper.dtoToEntity(dto);
        List<ENTITY> entities = dal.list(entity);
        return mapper.entityToBo(entities);
    }

    public List<BO> limit(@Validated(CRUD.R.class) DTO dto, int limit)
    {
        if (dto == null) return emptyList();

        ENTITY       entity   = mapper.dtoToEntity(dto);
        List<ENTITY> entities = dal.limit(entity, limit);
        return mapper.entityToBo(entities);
    }

    public Map<ID, BO> map(
            @Validated(CRUD.R.class) DTO dto)
    {
        if (dto == null) return emptyMap();
        return dal.list(mapper.dtoToEntity(dto)).stream()
                  .collect(toMap(BaseTableEntity::getId, mapper::entityToBo));
    }

    public PageResult<BO> page(
            @NotNull(message = "page can not be null when doing page-query")
            Page page,
            @Validated(R.class)
            DTO dto)
    {
        if (hasNull(dto, page)) return emptyPage();
        return mappingPage(dal.page(page, mapper.dtoToEntity(dto)), mapper::entityToBo);
    }

    public List<EO> exportExcel(
            @Validated(CRUD.R.class)
            DTO dto)
    {
        if (dto == null) return emptyList();
        return mapper.boToEo(this.list(dto));
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
        ENTITY entity = mapper.dtoToEntity(dto);
        return mapper.entityToBo(dal.create(entity));
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
        ENTITY entity = mapper.dtoToEntity(dto);
        return dal.update(entity);
    }

    public int update(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos)
    {
        if (isEmpty(dtos)) return 0;
        Validators.validateAndThrow(dtos, CRUD.U.class);
        return dal.update(mapper.dtoToEntity(dtos));
    }

    public Optional<BO> byId(
            @NotNull(message = "id can not be null when doing id-query")
            ID id)
    {
        return dal.byId(id).map(mapper::entityToBo);
    }

    public List<BO> byIds(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Set<ID> ids)
    {
        if (isEmpty(ids)) return emptyList();
        return mapper.entityToBo(dal.byIds(ids));
    }

    public List<Tree<ID>> tree(
            @NotNull(message = "root-id can not be null when doing tree-query")
            ID rootId,
            @Validated(CRUD.R.class)
            DTO dto)
    {
        if (Treeable.class.isAssignableFrom(boClass))
        {
            return TreeUtil.build(this.list(dto), rootId, DEFAULT_CONFIG, (bo, tree) ->
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
        return dal.exists(mapper.dtoToEntity(dto));
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

    @Nullable
    public <MASTER> MASTER wrap(
            MASTER master,
            @NotNull(message = "slaveIdGetter can not be null")
            Function<MASTER, ID> slaveIdGetter,
            @NotNull(message = "slaveConsumer can not be null")
            BiConsumer<ENTITY, MASTER> slaveConsumer)
    {
        if (master != null) return getFirst(this.wrap(singletonList(master), slaveIdGetter, slaveConsumer));
        return null;
    }

    public <MASTER> List<MASTER> wrap(
            @Size(max = 2048, message = "the number of collection cannot exceed 2048")
            Collection<MASTER> masters,
            @NotNull(message = "slaveIdGetter can not be null")
            Function<MASTER, ID> slaveIdGetter,
            @NotNull(message = "slaveConsumer can not be null")
            BiConsumer<ENTITY, MASTER> slaveConsumer)
    {
        if (isEmpty(masters)) return emptyList();

        Set<ID> slaverIds = masters.stream().map(slaveIdGetter).collect(toSet());
        Map<ID, ENTITY> slaverMap =
                dal.byIds(slaverIds).stream().collect(toMap(BaseTableEntity::getId, identity()));

        for (MASTER master : masters) {
            ENTITY slaver = slaverMap.get(slaveIdGetter.apply(master));
            if (slaver == null) {
                continue;
            }
            slaveConsumer.accept(slaver, master);
        }

        return new ArrayList<>(masters);
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
