package io.github.fbbzl.starter.generator.frame.invoker;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.text.CharSequenceUtil;
import io.github.fbbzl.starter.generator.config.properties.GeneratorConfigProperties;
import io.github.fbbzl.starter.generator.frame.BaseGenerator;
import io.github.fbbzl.starter.generator.frame.context.Field;
import io.github.fbbzl.starter.generator.frame.context.Index;
import io.github.fbbzl.starter.generator.frame.context.MysqlContextLoader;
import io.github.fbbzl.starter.generator.frame.context.Table;
import io.github.fbbzl.starter.generator.genarators.bo.BoGenerator;
import io.github.fbbzl.starter.generator.genarators.controller.ControllerGenerator;
import io.github.fbbzl.starter.generator.genarators.dal.DalGenerator;
import io.github.fbbzl.starter.generator.genarators.dto.DtoGenerator;
import io.github.fbbzl.starter.generator.genarators.entity.EntityGenerator;
import io.github.fbbzl.starter.generator.genarators.eo.EoGenerator;
import io.github.fbbzl.starter.generator.genarators.mapstruct.MapStructMapperGenerator;
import io.github.fbbzl.starter.generator.genarators.service.ServiceGenerator;
import io.github.fbbzl.starter.generator.genarators.xml.MybatisXmlGenerator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.fz.erwin.exception.Throws;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Primary;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.date.DatePattern.NORM_DATETIME_FORMATTER;
import static cn.hutool.core.text.CharSequenceUtil.removePrefix;
import static cn.hutool.core.text.CharSequenceUtil.upperFirst;
import static java.lang.Boolean.TRUE;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/2/11 22:34
 */
@Slf4j
@Primary
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DefaultGeneratorInvoker implements GeneratorInvoker, CommandLineRunner {

    MysqlContextLoader        ddlContext;
    GeneratorConfigProperties genCfg;

    /**
     * @see BoGenerator bo
     * @see ControllerGenerator web controller
     * @see DalGenerator data access layer
     * @see DtoGenerator dto
     * @see EntityGenerator database entity
     * @see EoGenerator excel object
     * @see MapStructMapperGenerator map struct
     * @see ServiceGenerator service
     * @see MybatisXmlGenerator mybatis XML
     */
    List<? extends BaseGenerator> generators;

    @Override
    public void run(String... args) {
        this.doGenerate();
    }

    @Override
    public void doGenerate()
    {
        log.info("code generation begins");
        List<String> tableNames = Arrays.asList(genCfg.getTables().split(","));
        if (isEmpty(tableNames)) return;

        for (String tableName : tableNames) {
            try {
                Table table = ddlContext.getTableContext(tableName.trim());
                Throws.ifNull(table, "table [{}] not exist", tableName);

                Map<String, Object> ftlContext = this.tableContextToFtlContext(table);
                for (BaseGenerator generator : generators) {
                    log.info("generating table: [{}], generator-bean name: [{}] ...", tableName, generator.getGeneratorBeanName());

                    Path filePath = generator.getFilePath(ftlContext);
                    if (filePath != null)
                    {
                        File   generatingFilePath = Paths.get(genCfg.getOutputPath().toString(), filePath.toString()).toFile();
                        String generatingContent  = FreeMarkerTemplateUtils.processTemplateIntoString(generator.getTemplate(), ftlContext);

                        this.writeFile(generatingFilePath, generatingContent);
                    }
                }
            }
            catch (Exception e) {
                log.error("generating table [{}] failed", tableName, e);
            }
        }
        log.info("code generation ends");
    }

    /**
     * The database context is converted to the context in which the freemarker is generated, e.g. an underscore to a hump
     */
    public Map<String, Object> tableContextToFtlContext(Table tableContext) {
        String              modulePackage = genCfg.getModulePackage();
        String              tablePrefix   = genCfg.getTablePrefix();
        String              author        = genCfg.getAuthor();
        Map<String, Object> ftlContext    = HashMap.newHashMap(32);
        ftlContext.put("package",        modulePackage);
        ftlContext.put("varName",        underscoreToCamelCase(removePrefix(tableContext.getTableName(), tablePrefix)));
        ftlContext.put("moduleName",     modulePackage + "." + CharSequenceUtil.removePrefix(tableContext.getTableName(), tablePrefix));
        ftlContext.put("className",      upperFirst(underscoreToCamelCase(removePrefix(tableContext.getTableName(), tablePrefix))));
        ftlContext.put("tableComment",   tableContext.getTableComment());
        ftlContext.put("tableName",      tableContext.getTableName());
        ftlContext.put("schemaName",     tableContext.getSchemaName());
        ftlContext.put("author",         author);
        ftlContext.put("primaryKeyType", genCfg.getPrimaryKeyType());
        ftlContext.put("date",           NORM_DATETIME_FORMATTER.format(LocalDateTime.now()));

        List<Field> fields = tableContext.getFields();
        ftlContext.put("columns", fields.stream().map(Field::getName).toArray());
        fields.forEach(field -> field.setName(underscoreToCamelCase(field.getName())));
        ftlContext.put("fields", fields);

        // get all index information
        List<Index> indexes = tableContext.getIndexes();

        // Traverse the index to convert column names from underscored to hump nomenclature
        indexes.forEach(index -> index.setColumns(
                index.getColumns().stream()
                     .map(this::underscoreToCamelCase).toList()));

        // put index information into the template context
        ftlContext.put("indexes", indexes);
        ftlContext.put("hasIndexes", CollUtil.isNotEmpty(indexes));

        // distinguish between unique and normal indexes
        List<Index> uniqueIndexes = indexes.stream()
                                           .filter(Index::getUnique).toList();
        List<Index> normalIndexes = indexes.stream()
                                           .filter(index -> !index.getUnique()).toList();

        ftlContext.put("uniqueIndexes", uniqueIndexes);
        ftlContext.put("hasUniqueIndexes", CollUtil.isNotEmpty(uniqueIndexes));
        ftlContext.put("normalIndexes", normalIndexes);
        ftlContext.put("hasNormalIndexes", CollUtil.isNotEmpty(normalIndexes));

        ftlContext.put("hasLengthValidation", fields.stream().anyMatch(field -> field.getLengthValidation() == TRUE));
        ftlContext.put("hasPatternValidation", fields.stream().anyMatch(field -> field.getPatternValidation() == TRUE));

        return ftlContext;
    }

    protected void writeFile(File file, String content) {
        if (FileUtil.exist(file)) FileUtil.del(file);
        FileWriter.create(file).write(content);
    }

    protected String underscoreToCamelCase(String underscore) {
        return CharSequenceUtil.toCamelCase(underscore, '_');
    }

}
