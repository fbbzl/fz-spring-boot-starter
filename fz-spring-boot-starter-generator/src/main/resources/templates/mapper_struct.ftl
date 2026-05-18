package ${moduleName}.struct;

import ${moduleName}.dal.entity.${className};
import ${moduleName}.controller.dto.${className}Dto;
import ${moduleName}.service.bo.${className}Bo;
<#if excel>
import ${moduleName}.service.eo.${className}Eo;

import io.github.fbbzl.starter.pojo.mapstruct.BaseStructMapper;
<#else>
import io.github.fbbzl.starter.pojo.mapstruct.BaseCrudStructMapper;
</#if>

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
<#if excel>
public interface ${className}StructMapper extends BaseStructMapper<${className}, ${className}Dto, ${className}Bo, ${className}Eo> {}
<#else>
public interface ${className}StructMapper extends BaseCrudStructMapper<${className}, ${className}Dto, ${className}Bo> {}
</#if>
