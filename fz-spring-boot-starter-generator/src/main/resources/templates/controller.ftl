package ${moduleName}.controller;

import ${moduleName}.dal.entity.${className};
import ${moduleName}.controller.dto.${className}Dto;
import ${moduleName}.service.bo.${className}Bo;
import ${moduleName}.service.${className}Service;
import ${moduleName}.dal.${dalClass};
import ${moduleName}.struct.${className}StructMapper;
<#if excel>
import ${moduleName}.service.eo.${className}Eo;

import io.github.fbbzl.starter.web.BaseCrudExcelController;
<#else>
import io.github.fbbzl.starter.web.BaseCrudController;
</#if>
import io.github.fbbzl.starter.web.annotation.RestRequestController;
import io.github.fbbzl.starter.audit.frame.annotation.AuditModule;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import static lombok.AccessLevel.PRIVATE;

/**
 * ${className} controller
 *
 * @author ${author}
 * @version 1.0
 * @since ${date}
 */

@Slf4j
@Validated
@AuditModule("${tableComment}")
@RequiredArgsConstructor
@RestRequestController(mapping = "${requestMapping}")
@Tag(name = "${tableComment}API", description = "api for ${tableComment}")
@FieldDefaults(level = PRIVATE, makeFinal = true)
<#if excel>
public class ${className}Controller extends BaseCrudExcelController<${primaryKeyType}, ${className}, ${className}Service, ${className}Dto, ${className}Bo, ${className}Eo, ${dalClass}, ${className}StructMapper> {}
<#else>
public class ${className}Controller extends BaseCrudController<${primaryKeyType}, ${className}, ${className}Service, ${className}Dto, ${className}Bo, ${dalClass}, ${className}StructMapper> {}
</#if>
