package ${moduleName}.service;

import ${moduleName}.dal.entity.${className};
import ${moduleName}.controller.dto.${className}Dto;
import ${moduleName}.service.bo.${className}Bo;
import ${moduleName}.dal.${dalClass};
import ${moduleName}.struct.${className}StructMapper;
<#if excel>
import ${moduleName}.service.eo.${className}Eo;

import io.github.fbbzl.starter.web.BaseCrudExcelService;
<#else>
import io.github.fbbzl.starter.web.BaseCrudService;
</#if>
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static lombok.AccessLevel.PRIVATE;

/**
 * ${className} service
 *
 * @author ${author}
 * @version 1.0
 * @since ${date}
 */
@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@FieldDefaults(level = PRIVATE, makeFinal = true)
<#if excel>
public class ${className}Service extends BaseCrudExcelService<${primaryKeyType}, ${className}, ${className}Dto, ${className}Bo, ${className}Eo, ${dalClass}, ${className}StructMapper> {
    @NonFinal
    @Autowired
    @Lazy
    ${className}Service self;
}
<#else>
public class ${className}Service extends BaseCrudService<${primaryKeyType}, ${className}, ${className}Dto, ${className}Bo, ${dalClass}, ${className}StructMapper> {
    @NonFinal
    @Autowired
    @Lazy
    ${className}Service self;
}
</#if>
