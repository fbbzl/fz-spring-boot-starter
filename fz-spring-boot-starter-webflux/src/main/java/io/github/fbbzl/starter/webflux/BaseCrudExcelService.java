package io.github.fbbzl.starter.webflux;

import cn.hutool.db.sql.Order;
import io.github.fbbzl.starter.dal.BaseDal;
import io.github.fbbzl.starter.excel.BaseEo;
import io.github.fbbzl.starter.pojo.bo.BaseBo;
import io.github.fbbzl.starter.pojo.dto.BaseDto;
import io.github.fbbzl.starter.pojo.entity.BaseTableEntity;
import io.github.fbbzl.starter.pojo.mapstruct.BaseCrudExcelStructMapper;
import io.github.fbbzl.starter.pojo.validation.Validators;
import io.github.fbbzl.starter.pojo.validation.group.CRUD;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static java.util.Collections.emptyList;

/**
 * Reactive CRUD service base with Excel export and import support.
 *
 * @author fengbinbin
 * @version 1.0
 * @since 2026/5/18
 */
public abstract class BaseCrudExcelService<
        ID            extends Serializable,
        ENTITY        extends BaseTableEntity<ID>,
        DTO           extends BaseDto<ID>,
        BO            extends BaseBo<ID>,
        EO            extends BaseEo,
        DAL           extends BaseDal<ENTITY, ID>,
        STRUCT_MAPPER extends BaseCrudExcelStructMapper<ENTITY, DTO, BO, EO>>
        extends BaseCrudService<ID, ENTITY, DTO, BO, DAL, STRUCT_MAPPER>
{

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

    @Transactional
    public void importExcel(
            @Size(max = 1024, message = "the number of collection cannot exceed 1024")
            Collection<EO> eos)
    {
        if (isEmpty(eos)) return;
        Validators.validateAndThrow(eos, CRUD.C.class);
        dal.create(struct.eoToEntity(eos));
    }
}
