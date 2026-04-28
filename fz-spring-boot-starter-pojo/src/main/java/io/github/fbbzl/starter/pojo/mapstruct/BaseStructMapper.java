package io.github.fbbzl.starter.pojo.mapstruct;

import io.github.fbbzl.starter.excel.BaseEo;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;

import java.io.Serializable;
import java.util.Collection;
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
@SuppressWarnings("all")
public interface BaseStructMapper<
        ENTITY extends BaseTableEntity<? extends Serializable>,
        DTO extends BaseDto<? extends Serializable>,
        BO extends BaseBo<? extends Serializable>,
        EO extends BaseEo>
{

    BO entityToBo(ENTITY entity);

    ENTITY boToEntity(BO bo);

    List<BO> entityToBo(Collection<ENTITY> entities);

    BO[] entityToBo(ENTITY... entities);

    List<ENTITY> boToEntity(Collection<BO> bos);

    ENTITY[] boToEntity(BO... bos);

    DTO entityToDto(ENTITY entity);

    ENTITY dtoToEntity(DTO dto);

    List<DTO> entityToDto(Collection<ENTITY> entities);

    DTO[] entityToDto(ENTITY... entities);

    List<ENTITY> dtoToEntity(Collection<DTO> dtos);

    ENTITY[] dtoToEntity(DTO... dtos);

    EO entityToEo(ENTITY entity);

    ENTITY eoToEntity(EO eo);

    List<EO> entityToEo(Collection<ENTITY> entities);

    EO[] entityToEo(ENTITY... entities);

    List<ENTITY> eoToEntity(Collection<EO> eos);

    ENTITY[] eoToEntity(EO... eos);

    BO dtoToBo(DTO dto);

    DTO boToDto(BO bo);

    List<BO> dtoToBo(Collection<DTO> dtos);

    BO[] dtoToBo(DTO... dtos);

    List<DTO> boToDto(Collection<BO> bos);

    DTO[] boToDto(BO... bos);

    EO dtoToEo(DTO dto);

    DTO eoToDto(EO eo);

    List<EO> dtoToEo(Collection<DTO> dtos);

    EO[] dtoToEo(DTO... dtos);

    List<DTO> eoToDto(Collection<EO> eos);

    DTO[] eoToDto(EO... eos);

    EO boToEo(BO bo);

    BO eoToBo(EO eo);

    List<EO> boToEo(Collection<BO> bos);

    EO[] boToEo(BO... bos);

    List<BO> eoToBo(Collection<EO> eos);

    BO[] eoToBo(EO... eos);
}