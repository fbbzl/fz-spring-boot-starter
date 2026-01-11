package com.fz.springboot.starter.generator;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.text.CharSequenceUtil;
import com.fz.springboot.starter.generator.frame.DDLContextLoader;
import com.fz.springboot.starter.generator.frame.Generator;
import com.fz.springboot.starter.generator.frame.context.Field;
import com.fz.springboot.starter.generator.frame.context.Index;
import com.fz.springboot.starter.generator.frame.context.Table;
import com.fz.springboot.starter.generator.modules.controller.ControllerGenerator;
import com.fz.springboot.starter.generator.modules.entity.EntityGenerator;
import com.fz.springboot.starter.generator.modules.repository.RepositoryGenerator;
import com.fz.springboot.starter.generator.modules.service.ServiceGenerator;
import com.fz.springboot.starter.generator.modules.service.ServiceImplGenerator;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.fz.erwin.exception.Throws;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;

import java.io.File;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cn.hutool.core.collection.CollUtil.isEmpty;
import static cn.hutool.core.text.CharSequenceUtil.*;
import static java.lang.Boolean.TRUE;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/29 14:32
 */
@Slf4j
@SpringBootApplication
public class CodeGeneratorApplication implements CommandLineRunner {

    @Autowired DDLContextLoader databaseMetadataService;

    @Value("${code.generator.output.path:#{systemProperties['user.dir']}/generated-sources}")
    String outputPath;
    @Value("${code.generator.tables:}")          String tablesToGenerate;
    @Value("${code.generator.module-package:}")  String modulePackage;
    @Value("${code.generator.table-prefix:lj_}") String tablePrefix;
    @Value("${code.generator.author:}")          String author;

    /**
     * @see EntityGenerator 实体类生成器
     * @see ControllerGenerator web controller生成器
     * @see ServiceGenerator 业务接口生成器
     * @see ServiceImplGenerator 业务实现类生成器
     * @see RepositoryGenerator 持久层接口生成器
     */
    @Resource List<? extends Generator> generators;

    @Override
    public void run(String... args) {
        log.info("代码生成开始");
        List<String> tableNames = Arrays.asList(tablesToGenerate.split(","));
        if (isEmpty(tableNames)) return;

        for (String tableName : tableNames) {
            try {
                Table table = databaseMetadataService.getTableContext(tableName.trim());
                Throws.ifNull(table, () -> format("表[{}]不存在", tableName));

                Map<String, Object> ftlContext = this.tableContextToFtlContext(table);
                for (Generator generator : generators) {
                    log.info("正在生成表[{}]的[{}]代码...", tableName, generator.getGeneratorName());
                    File   generatedJavaFile = Paths.get(outputPath, generator.getJavaFilePath(ftlContext).get()).toFile();
                    String generatedJavaCode = FreeMarkerTemplateUtils.processTemplateIntoString(generator.getTemplate(), ftlContext);

                    this.writeFile(generatedJavaFile, generatedJavaCode);
                }
            }
            catch (Exception e) {
                log.error("生成表 [{}] 的代码失败", tableName, e);
            }
        }
        log.info("代码生成结束");
    }

    /**
     * 数据库上下文转成生成freemarker的上下文, 例如 下划线转驼峰
     */
    public Map<String, Object> tableContextToFtlContext(Table tableContext) {
        Map<String, Object> ftlContext = new HashMap<>(32);
        ftlContext.put("package", modulePackage);
        ftlContext.put("varName", underscoreToCamelCase(removePrefix(tableContext.getTableName(), tablePrefix)));
        ftlContext.put("moduleName", modulePackage + "." + CharSequenceUtil.removePrefix(tableContext.getTableName(), tablePrefix));
        ftlContext.put("className", upperFirst(underscoreToCamelCase(removePrefix(tableContext.getTableName(), tablePrefix))));
        ftlContext.put("tableComment", tableContext.getTableComment());
        ftlContext.put("tableName", tableContext.getTableName());
        ftlContext.put("schemaName", tableContext.getSchemaName());
        ftlContext.put("author", author);
        ftlContext.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));

        List<Field> fields = tableContext.getFields();
        fields.forEach(field -> field.setName(underscoreToCamelCase(field.getName())));
        ftlContext.put("fields", fields);

        // 获取所有索引信息
        List<Index> indexes = tableContext.getIndexes();

        // 遍历索引，将列名从下划线命名法转换为驼峰命名法
        indexes.forEach(index -> index.setColumns(
                index.getColumns().stream()
                     .map(this::underscoreToCamelCase).toList()));

        // 将索引信息放入模板上下文中
        ftlContext.put("indexes", indexes);
        ftlContext.put("hasIndexes", CollUtil.isNotEmpty(indexes));

        // 区分唯一索引和普通索引
        List<Index> uniqueIndexes = indexes.stream()
                                           .filter(Index::getUnique).toList();
        List<Index> normalIndexes = indexes.stream()
                                           .filter(index -> !index.getUnique()).toList()
                ;

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

    public static void main(String[] args) {
        SpringApplication.run(CodeGeneratorApplication.class, args);
    }


}
