package ${moduleName}.controller;

import ${moduleName}.repository.entity.${className};
import ${moduleName}.service.I${className}Service;

import com.springboot.starter.web.BaseController;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


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
@RestController
@RequiredArgsConstructor
@RequestMapping("${requestMapping}")
@Tag(name = "${tableComment}管理", description = "${tableComment}管理 相关操作API")
@FieldDefaults(level = PRIVATE, makeFinal = true)
public class ${className}Controller extends BaseController<I${className}Service, ${className}> {}
