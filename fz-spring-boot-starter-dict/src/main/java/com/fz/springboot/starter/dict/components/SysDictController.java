package com.fz.springboot.starter.dict.components;

import com.fz.springboot.starter.R;
import com.fz.springboot.starter.dict.components.entity.SysDict;
import com.fz.springboot.starter.dict.frame.condition.DictCondition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Conditional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/3 11:28
 */
@Validated
@RestController
@Conditional(DictCondition.class)
@RequiredArgsConstructor
@RequestMapping("dict")
@Tag(name = "系统字典接口")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SysDictController {

    SysDictService sysDictService;

    @Operation(summary = "通过code获取字典")
    @GetMapping("code/{code}")
    public R<SysDict> getDictByCode(@PathVariable("code") String code) {
        return R.ok(sysDictService.getDictByCode(code));
    }

    @Operation(summary = "通过type获取字典")
    @GetMapping("type/{type}")
    public R<List<SysDict>> getDictByType(@PathVariable("type") String type) {
        return R.ok(sysDictService.getByType(type));
    }

    @Operation(summary = "创建字典")
    @PostMapping
    public R<SysDict> createDict(@RequestBody @Validated SysDict dict) {
        return R.ok(sysDictService.save(dict));
    }

    @GetMapping("extend")
    @Operation(summary = "根据扩展属性中的特定键值查询")
    public R<List<SysDict>> getByExtendAttribute(
            @Parameter(description = "扩展属性的键") @RequestParam String key,
            @Parameter(description = "扩展属性的值") @RequestParam String value)
    {
        return R.ok(sysDictService.findByExtendAttribute(key, value));
    }

    @GetMapping("extend/nested")
    @Operation(summary = "根据扩展属性中的嵌套字段查询")
    public R<List<SysDict>> getByExtendNestedAttribute(
            @Parameter(description = "扩展属性的嵌套路径") @RequestParam String path,
            @Parameter(description = "扩展属性的值") @RequestParam String value)
    {
        return R.ok(sysDictService.findByExtendNestedAttribute(path, value));
    }

    @PostMapping("extend/contains")
    @Operation(summary = "使用 JSON_CONTAINS 查询包含特定扩展属性的记录")
    public R<List<SysDict>> getByExtendAttributeContains(
            @Parameter(description = "JSON 片段") @RequestBody String jsonFragment)
    {
        return R.ok(sysDictService.findByExtendAttributeContains(jsonFragment));
    }

    @GetMapping("extend/exists")
    @Operation(summary = "根据扩展属性是否存在某个键查询")
    public R<List<SysDict>> getByExtendAttributeExists(
            @Parameter(description = "JSON 路径") @RequestParam String jsonPath)
    {
        return R.ok(sysDictService.findByExtendAttributeExists(jsonPath));
    }

    @PutMapping("{id}")
    @Operation(summary = "更新字典项")
    public R<SysDict> update(
            @PathVariable
            @Parameter(description = "字典主键")
            Long id,
            @RequestBody
            @Validated
            SysDict sysDict)
    {
        sysDict.setId(id);
        return R.ok(sysDictService.save(sysDict));
    }

}
