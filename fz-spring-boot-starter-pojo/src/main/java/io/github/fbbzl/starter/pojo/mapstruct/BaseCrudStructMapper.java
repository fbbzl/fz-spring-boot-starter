package io.github.fbbzl.starter.pojo.mapstruct;

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
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/18
 */
@SuppressWarnings("all")
public interface BaseCrudStructMapper<
        ENTITY extends BaseTableEntity<? extends Serializable>,
        DTO extends BaseDto<? extends Serializable>,
        BO extends BaseBo<? extends Serializable>>
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

    BO dtoToBo(DTO dto);

    DTO boToDto(BO bo);

    List<BO> dtoToBo(Collection<DTO> dtos);

    BO[] dtoToBo(DTO... dtos);

    List<DTO> boToDto(Collection<BO> bos);

    DTO[] boToDto(BO... bos);
}
