package com.fz.springboot.starter.pojo.mapstruct;

import com.fz.springboot.starter.pojo.bo.BaseBo;
import com.fz.springboot.starter.pojo.dto.BaseDto;
import com.fz.springboot.starter.pojo.entity.BaseTableEntity;
import com.fz.springboot.starter.pojo.eo.BaseEo;

import java.util.List;

/**
 * @param <ENTITY> entity
 * @param <DTO>    dto object
 * @param <BO>     business object
 * @param <EO>     Excel object
 * @author fengbinbin
 * @version 1.0
 * @since 2026/1/21 23:53
 */

public interface BaseStructMapper<ENTITY extends BaseTableEntity, DTO extends BaseDto<ENTITY>, BO extends BaseBo<ENTITY>, EO extends BaseEo<ENTITY>> {

    BO entityToBo(ENTITY entity);

    ENTITY boToEntity(BO bo);

    List<BO> entityToBo(Iterable<ENTITY> entities);

    List<ENTITY> boToEntity(Iterable<BO> bos);

    DTO entityToDto(ENTITY entity);

    ENTITY dtoToEntity(DTO dto);

    List<DTO> entityToDto(Iterable<ENTITY> entities);

    List<ENTITY> dtoToEntity(Iterable<DTO> dtos);

    EO entityToEo(ENTITY entity);

    ENTITY eoToEntity(EO eo);

    List<EO> entityToEo(Iterable<ENTITY> entities);

    List<ENTITY> eoToEntity(Iterable<EO> eos);

    BO dtoToBo(DTO dto);

    DTO boToDto(BO bo);

    List<BO> dtoToBo(Iterable<DTO> dtos);

    List<DTO> boToDto(Iterable<BO> bos);

    EO dtoToEo(DTO dto);

    DTO eoToDto(EO eo);

    List<EO> dtoToEo(Iterable<DTO> dtos);

    List<DTO> eoToDto(Iterable<EO> eos);

    EO boToEo(BO bo);

    BO eoToBo(EO eo);

    List<EO> boToEo(Iterable<BO> bos);

    List<BO> eoToBo(Iterable<EO> eos);
}