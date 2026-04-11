package ${moduleName}.struct;

import ${moduleName}.dal.entity.${className};
import ${moduleName}.controller.dto.${className}Dto;
import ${moduleName}.service.bo.${className}Bo;
import ${moduleName}.service.eo.${className}Eo;

import com.fz.starter.pojo.mapstruct.BaseStructMapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants.ComponentModel;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

/**
 * ${className} struct mapper
 *
 * @author ${author}
 * @version 1.0
 * @since ${date}
 */

@Mapper(componentModel = ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ${className}StructMapper extends BaseStructMapper<${className}, ${className}Dto, ${className}Bo, ${className}Eo> {}
