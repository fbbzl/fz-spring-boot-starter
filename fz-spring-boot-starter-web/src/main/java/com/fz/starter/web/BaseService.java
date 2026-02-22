package com.fz.starter.web;


import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import com.fz.starter.core.util.Generics;
import com.fz.starter.dal.BaseDal;
import com.fz.starter.pojo.bo.BaseBo;
import com.fz.starter.pojo.dto.BaseDto;
import com.fz.starter.pojo.entity.BaseTableEntity;
import com.fz.starter.pojo.eo.BaseEo;
import com.fz.starter.pojo.mapstruct.BaseStructMapper;
import com.fz.starter.pojo.validation.CRUD;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.validation.annotation.Validated;

import java.util.*;
import java.util.function.Function;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.util.ObjectUtil.hasNull;
import static java.util.Collections.emptyList;

/**
 * @param <ENTITY> request data type
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 15:00
 */

@Validated
@FieldDefaults(level = AccessLevel.PROTECTED)
public abstract class BaseService<
        ENTITY extends BaseTableEntity,
        DTO    extends BaseDto<ENTITY>,
        BO     extends BaseBo<ENTITY>,
        EO     extends BaseEo<ENTITY>,
        STRUCT_MAPPER extends BaseStructMapper<ENTITY, DTO, BO, EO>> {

    Class<ENTITY> entityClass = Generics.getGenericType(this.getClass(), BaseService.class, 0);
    @Autowired BaseDal<ENTITY> dal;
    @Autowired STRUCT_MAPPER   mapper;

    public List<BO> list(@Validated(CRUD.R.class) DTO dto) {
        if (dto == null) return emptyList();

        ENTITY       entity   = mapper.dtoToEntity(dto);
        List<ENTITY> entities = dal.list(entity);
        return mapper.entityToBo(entities);
    }

    public List<BO> list(
            @Size(max = 1024, message = "the number of map cannot exceed 1024")
            Map<String, Object> params) {
        if (isEmpty(params)) return emptyList();

        return mapper.entityToBo(dal.list(params));
    }

    public PageResult<BO> page(
            @Validated(CRUD.R.class)
            DTO dto,
            @NotNull(message = "page can not be null when doing page-query")
            Page page) {
        if (hasNull(dto, page)) return emptyPage();

        ENTITY entity = mapper.dtoToEntity(dto);
        return mappingPage(dal.page(entity, page), mapper::entityToBo);
    }

    public PageResult<BO> page(
            @Size(max = 1024, message = "the number of map cannot exceed 1024")
            Map<String, Object> params,
            @NotNull(message = "page can not be null when doing page-query")
            Page page) {
        if (hasNull(params, page)) return emptyPage();

        return mappingPage(dal.page(params, page), mapper::entityToBo);
    }

    public List<EO> exportExcel(
            @Validated(CRUD.R.class)
            DTO dto) {
        if (dto == null) return emptyList();

        return mapper.boToEo(this.list(dto));
    }

    public int importExcel(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<EO> eos) {
        if (isEmpty(eos)) return 0;

        Validators.validateAndThrow(eos, CRUD.C.class);
        return dal.create(mapper.eoToEntity(eos));
    }

    @Nullable
    public BO create(
            @Validated(CRUD.C.class)
            DTO dto) {
        if (dto == null) return null;

        ENTITY entity = mapper.dtoToEntity(dto);
        return mapper.entityToBo(dal.create(entity));
    }

    public int create(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos) {
        if (isEmpty(dtos)) return 0;

        Validators.validateAndThrow(dtos, CRUD.C.class);
        return dal.create(mapper.dtoToEntity(dtos));
    }

    public int update(
            @Validated(CRUD.U.class)
            DTO dto) {
        if (dto == null) return 0;

        ENTITY entity = mapper.dtoToEntity(dto);
        return dal.update(entity);
    }

    public int update(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos) {
        if (isEmpty(dtos)) return 0;

        Validators.validateAndThrow(dtos, CRUD.U.class);
        return dal.update(mapper.dtoToEntity(dtos));
    }

    public Optional<BO> byId(
            @NotNull(message = "id can not be null when doing id-query")
            Long id) {
        return dal.byId(id).map(mapper::entityToBo);
    }

    public List<BO> byIds(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Set<Long> ids) {
        if (isEmpty(ids)) return emptyList();

        return mapper.entityToBo(dal.byIds(ids));
    }

    public boolean exists(
            @NotNull(message = "id can not be null when doing id-exist-query")
            @Validated(CRUD.R.class)
            Long id) {
        return dal.exists(id);
    }

    public boolean exists(
            @NotNull(message = "data can not be null when doing data-exist-query")
            @Validated(CRUD.R.class)
            DTO dto) {
        return dal.exists(mapper.dtoToEntity(dto));
    }

    public boolean exists(
            @NotNull(message = "data can not be null when doing data-exist-query")
            @Size(max = 1024, message = "the number of map cannot exceed 1024")
            Map<String, Object> params) {
        return dal.exists(params);
    }

    public void delete(
            @NotNull(message = "data can not be null when doing delete")
            Long id) {
        dal.delete(id);
    }

    public void delete(
            @NotNull(message = "data can not be null when doing delete")
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Set<Long> ids) {
        dal.delete(ids);
    }

    public static <SOURCE, TARGET> PageResult<TARGET> mappingPage(
            PageResult<SOURCE> sourcePage,
            Function<List<SOURCE>, List<TARGET>> mapper) {
        PageResult<TARGET> targetPage = new PageResult<>(sourcePage.getPage(), sourcePage.getPageSize(), sourcePage.getTotal());
        targetPage.addAll(mapper.apply(sourcePage));
        return targetPage;
    }

    public <DATA> PageResult<DATA> emptyPage() {
        return new PageResult<>();
    }

}
