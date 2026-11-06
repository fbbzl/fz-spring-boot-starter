package ${moduleName}.controller;

import ${moduleName}.dal.entity.${className};
import ${moduleName}.controller.dto.${className}Dto;
import ${moduleName}.service.bo.${className}Bo;
import ${moduleName}.service.${className}Service;
<#if excel>
import ${moduleName}.service.eo.${className}Eo;

import io.github.fbbzl.starter.web.BaseCrudExcelController;
<#else>
import io.github.fbbzl.starter.web.BaseCrudController;
</#if>
import io.github.fbbzl.starter.web.annotation.RestRequestController;
import io.github.fbbzl.starter.audit.frame.annotation.AuditModule;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;

import static lombok.AccessLevel.PRIVATE;

/**
 * ${tableComment?replace("*/", "* /")} controller
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
@FieldDefaults(level = PRIVATE, makeFinal = true)
<#if excel>
public class ${className}Controller extends BaseCrudExcelController<${primaryKeyType}, ${className}, ${className}Service, ${className}Dto, ${className}Bo, ${className}Eo> {}
<#else>
public class ${className}Controller extends BaseCrudController<${primaryKeyType}, ${className}, ${className}Service, ${className}Dto, ${className}Bo> {}
</#if>
