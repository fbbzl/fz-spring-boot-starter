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
public interface BaseCrudExcelStructMapper<
        ENTITY extends BaseTableEntity<? extends Serializable>,
        DTO    extends BaseDto<? extends Serializable>,
        BO     extends BaseBo<? extends Serializable>,
        EO     extends BaseEo> extends BaseCrudStructMapper<ENTITY, DTO, BO>
{

    EO entityToEo(ENTITY entity);

    ENTITY eoToEntity(EO eo);

    List<EO> entityToEo(Collection<ENTITY> entities);

    EO[] entityToEo(ENTITY... entities);

    List<ENTITY> eoToEntity(Collection<EO> eos);

    ENTITY[] eoToEntity(EO... eos);

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
