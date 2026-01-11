package com.fz.springboot.starter.web;

import cn.hutool.core.util.ReflectUtil;
import com.fz.springboot.starter.jpa.BaseEntity;
import com.fz.springboot.starter.jpa.validation.CRUD;
import com.fz.springboot.starter.web.Q.PQ;
import com.fz.springboot.starter.web.R.PR;
import com.fz.springboot.starter.web.service.IService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static lombok.AccessLevel.PROTECTED;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/8/22 14:24
 */

@Validated
@SuppressWarnings("unchecked")
@FieldDefaults(level = PROTECTED)
public abstract class BaseController<S extends IService<T>, T extends BaseEntity> {

    @Autowired S                   service;
    @Autowired HttpServletRequest  request;
    @Autowired HttpServletResponse response;

    Class<T> entityClass;

    {
        ResolvableType currentType = ResolvableType.forClass(this.getClass());

        while (true) {
            currentType.getSuperType();
            ResolvableType superType = currentType.getSuperType();

            if (superType.getRawClass() == BaseController.class) {
                ResolvableType entityType = superType.getGeneric(1);

                this.entityClass = (Class<T>) entityType.resolve();

                // break if found BaseController is super class
                break;
            }

            currentType = superType;
        }
    }

    @Operation(description = "根据id查询, 不包含已经逻辑删除的数据", summary = "根据id查询")
    @GetMapping("{id}")
    public R<T> id(
            @PathVariable @NotNull
            @Parameter(name = "id", description = "查询的id", required = true, example = "1") Long id)
    {
        return R.ok(service.findById(id).orElseGet(() -> ReflectUtil.newInstance(entityClass)));
    }

    @Operation(description = "根据id集合进行查询", summary = "根据id集合进行查询")
    @PostMapping("ids")
    public R<List<T>> findByIds(
            @NotNull
            @RequestBody Q<List<Long>> req)
    {
        return R.ok(service.findByIds(req.getData()));
    }

    @Operation(description = "列表查询, null字段不参与查询", summary = "查询列表")
    @PostMapping("list")
    public R<List<T>> list(
            @NotNull
            @Validated({CRUD.R.class})
            @Parameter(description = "请求对象", required = true)
            @RequestBody Q<T> req)
    {
        return R.ok(service.find(req.getData()));
    }

    @Operation(description = "分页查询, null字段不参与查询", summary = "分页查询")
    @PostMapping("page")
    public PR<T> page(
            @NotNull
            @Validated({CRUD.R.class})
            @Parameter(description = "分页请求对象", required = true)
            @RequestBody PQ<T> req)
    {
        return PR.ok(service.find(req.getData(), req.toPageable()));
    }

    @Operation(description = "指定id数据是否存在", summary = "指定id数据是否存在")
    @GetMapping("exists/{id}")
    public R<Boolean> exists(
            @PathVariable @NotNull
            @Parameter(name = "id", description = "查询的id", required = true, example = "1") Long id)
    {
        return R.ok(service.exists(id));
    }

    @Operation(description = "指定条件数据是否存在, null的字段不会参与查询", summary = "指定条件数据是否存在")
    @PostMapping("exists")
    public R<Boolean> exists(
            @NotNull
            @Validated({CRUD.R.class})
            @Parameter(description = "请求对象", required = true)
            @RequestBody Q<T> req)
    {
        return R.ok(service.exists(req.getData()));
    }

    @Operation(description = "新增数据", summary = "新增数据")
    @PostMapping
    public R<T> create(
            @NotNull
            @Validated({CRUD.C.class})
            @Parameter(description = "新增请求对象", required = true)
            @RequestBody Q<T> req)
    {
        return R.ok(service.create(req.getData()));
    }

    @Operation(description = "全量更新数据, 主键为路径参数", summary = "全量更新数据")
    @PutMapping("{id}")
    public R<T> update(
            @PathVariable @NotNull
            @Parameter(name = "id", description = "需要更新的记录ID", required = true, example = "1") Long id,
            @NotNull
            @Validated(CRUD.U.class)
            @Parameter(name = "req", description = "全量更新的数据", required = true)
            @RequestBody Q<T> req)
    {
        T data = req.getData();
        data.setId(id);
        return R.ok(service.update(data));
    }

    @Operation(description = "删除数据, 为逻辑删除, 但此逻辑删除等于物理删除, 逻辑删除只是为了发挥数据最大价值", summary = "逻辑删除数据")
    @DeleteMapping("{id}")
    public R<Object> delete(
            @PathVariable @NotNull
            @Parameter(name = "id", description = "需要删除的记录ID", required = true, example = "1") Long id)
    {
        service.deleteById(id);
        return R.ok();
    }

    @Operation(description = "删除数据, 为逻辑删除, 但此逻辑删除等于物理删除, 逻辑删除只是为了发挥数据最大价值", summary = "逻辑删除数据")
    @DeleteMapping("ids")
    public R<Object> deleteByIds(
            @NotNull
            @Parameter(description = "请求对象", required = true)
            @RequestBody Q<List<Long>> req)
    {
        service.deleteByIds(req.getData());
        return R.ok();
    }
}
