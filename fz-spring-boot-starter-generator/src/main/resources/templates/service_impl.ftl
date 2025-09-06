package ${moduleName}.service.impl;

import ${moduleName}.repository.${className}Repository;
import ${moduleName}.repository.entity.${className};
import ${moduleName}.service.I${className}Service;

import service.com.springboot.starter.web.ServiceImpl;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import static lombok.AccessLevel.PRIVATE;

/**
* ${className} service impl
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
public class ${className}ServiceImpl extends ServiceImpl<${className}Repository, ${className}> implements I${className}Service {}
