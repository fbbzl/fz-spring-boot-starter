package com.fz.springboot.starter.web;


import cn.hutool.db.Page;
import cn.hutool.db.PageResult;
import com.fz.springboot.starter.dal.BaseDal;
import com.fz.springboot.starter.pojo.Generics;
import com.fz.springboot.starter.pojo.bo.BaseBo;
import com.fz.springboot.starter.pojo.dto.BaseDto;
import com.fz.springboot.starter.pojo.entity.BaseTableEntity;
import com.fz.springboot.starter.pojo.eo.BaseEo;
import com.fz.springboot.starter.pojo.mapstruct.BaseStructMapper;
import com.fz.springboot.starter.pojo.validation.CRUD;
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

    public List<BO> baseList(@Validated(CRUD.R.class) DTO dto) {
        if (dto == null) return emptyList();

        ENTITY       entity   = mapper.dtoToEntity(dto);
        List<ENTITY> entities = dal.baseList(entity);
        return mapper.entityToBo(entities);
    }

    public List<BO> baseList(
            @Size(max = 1024, message = "the number of map cannot exceed 1024")
            Map<String, Object> params) {
        if (isEmpty(params)) return emptyList();

        return mapper.entityToBo(dal.baseList(params));
    }

    public PageResult<BO> basePage(
            @Validated(CRUD.R.class)
            DTO dto,
            @NotNull(message = "page can not be null when doing page-query")
            Page page) {
        if (hasNull(dto, page)) return emptyPage();

        ENTITY entity = mapper.dtoToEntity(dto);
        return mappingPage(dal.basePage(entity, page), mapper::entityToBo);
    }

    public PageResult<BO> basePage(
            @Size(max = 1024, message = "the number of map cannot exceed 1024")
            Map<String, Object> params,
            @NotNull(message = "page can not be null when doing page-query")
            Page page) {
        if (hasNull(params, page)) return emptyPage();

        return mappingPage(dal.basePage(params, page), mapper::entityToBo);
    }

    public List<EO> baseExport(
            @Validated(CRUD.R.class)
            DTO dto) {
        if (dto == null) return emptyList();

        return mapper.boToEo(this.baseList(dto));
    }

    public int baseImport(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<EO> eos) {
        if (isEmpty(eos)) return 0;

        Validators.validateAndThrow(eos, CRUD.C.class);
        return dal.baseCreate(mapper.eoToEntity(eos));
    }

    @Nullable
    public BO baseCreate(
            @Validated(CRUD.C.class)
            DTO dto) {
        if (dto == null) return null;

        ENTITY entity = mapper.dtoToEntity(dto);
        return mapper.entityToBo(dal.baseCreate(entity));
    }

    public int baseCreate(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos) {
        if (isEmpty(dtos)) return 0;

        Validators.validateAndThrow(dtos, CRUD.C.class);
        return dal.baseCreate(mapper.dtoToEntity(dtos));
    }

    public int baseUpdate(
            @Validated(CRUD.U.class)
            DTO dto) {
        if (dto == null) return 0;

        ENTITY entity = mapper.dtoToEntity(dto);
        return dal.baseUpdate(entity);
    }

    public int baseUpdate(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<DTO> dtos) {
        if (isEmpty(dtos)) return 0;

        Validators.validateAndThrow(dtos, CRUD.U.class);
        return dal.baseUpdate(mapper.dtoToEntity(dtos));
    }

    public Optional<BO> baseById(
            @NotNull(message = "id can not be null when doing id-query")
            Long id) {
        return dal.baseById(id).map(mapper::entityToBo);
    }

    public List<BO> baseByIds(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Set<Long> ids) {
        if (isEmpty(ids)) return emptyList();

        return mapper.entityToBo(dal.baseByIds(ids));
    }

    public boolean baseExists(
            @NotNull(message = "id can not be null when doing id-exist-query")
            @Validated(CRUD.R.class)
            Long id) {
        return dal.baseExists(id);
    }

    public boolean baseExists(
            @NotNull(message = "data can not be null when doing data-exist-query")
            @Validated(CRUD.R.class)
            DTO dto) {
        return dal.baseExists(mapper.dtoToEntity(dto));
    }

    public boolean baseExists(
            @NotNull(message = "data can not be null when doing data-exist-query")
            @Size(max = 1024, message = "the number of map cannot exceed 1024")
            Map<String, Object> params) {
        return dal.baseExists(params);
    }

    public void baseDelete(
            @NotNull(message = "data can not be null when doing delete")
            Long id) {
        dal.baseDelete(id);
    }

    public void baseDelete(
            @NotNull(message = "data can not be null when doing delete")
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Set<Long> ids) {
        dal.baseDelete(ids);
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
