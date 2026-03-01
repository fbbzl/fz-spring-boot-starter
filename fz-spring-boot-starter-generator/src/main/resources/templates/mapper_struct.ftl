package ${moduleName}.struct;

import ${moduleName}.dal.entity.${className};
import ${moduleName}.controller.dto.${className}Dto;
import ${moduleName}.service.bo.${className}Bo;
import ${moduleName}.service.eo.${className}Eo;

import com.fz.starter.pojo.mapstruct.BaseStructMapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
* ${className} struct mapper
*
* @author ${author}
* @version 1.0
* @since ${date}
*/

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ${className}StructMapper extends BaseStructMapper<${className}, ${className}Dto, ${className}Bo, ${className}Eo> {}
