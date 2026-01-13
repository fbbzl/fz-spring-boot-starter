package ${moduleName}.controller;

import ${moduleName}.dal.entity.${className};
import ${moduleName}.controller.dto.${className}Dto;
import ${moduleName}.service.bo.${className}Bo;
import ${moduleName}.service.eo.${className}Eo;

import com.fz.springboot.starter.web.BaseController;
import com.fz.springboot.starter.web.annotation.RestRequestController;
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
@RestRequestController(mapping = "${requestMapping}")
@RequiredArgsConstructor
@Tag(name = "${tableComment}管理", description = "${tableComment}管理API")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ${className}Controller extends BaseController<${className}, ${className}Dto, ${className}Bo, ${className}Eo> {}
