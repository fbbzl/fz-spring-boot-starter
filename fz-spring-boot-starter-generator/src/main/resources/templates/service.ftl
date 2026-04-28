package ${moduleName}.service;

import ${moduleName}.dal.entity.${className};
import ${moduleName}.controller.dto.${className}Dto;
import ${moduleName}.service.bo.${className}Bo;
import ${moduleName}.service.eo.${className}Eo;
import ${moduleName}.dal.${dalClass};

import ${moduleName}.struct.${className}StructMapper;

import io.github.fbbzl.starter.web.BaseService;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
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
public class ${className}Service extends BaseService<${primaryKeyType}, ${className}, ${className}Dto, ${className}Bo, ${className}Eo, ${dalClass}, ${className}StructMapper> {}
