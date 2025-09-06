package com.fz.springboot.starter.dict.components;

import com.fz.springboot.starter.dict.components.entity.SysDict;
import com.fz.springboot.starter.jpa.repository.BaseRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/3 11:28
 */
@Repository
public interface SysDictRepository extends JpaRepository<SysDict, Long>, BaseRepository<SysDict> {

    SysDict findByCode(String code);

    List<SysDict> findByType(String type);

    /**
     * 根据扩展属性中的特定键值查询
     *
     * @param key   扩展属性的键
     * @param value 扩展属性的值
     * @return 符合条件的字典项列表
     */
    @NativeQuery(value = "SELECT * FROM lj_cloud.lj_sys_dict WHERE extend->>\"$.'?1'\" = '?2'")
    List<SysDict> findByExtendAttribute(@Param("key") String key, @Param("value") String value);

    /**
     * 根据扩展属性中的嵌套字段查询
     *
     * @param path  扩展属性的嵌套路径，如 "config.enabled"
     * @param value 扩展属性的值
     * @return 符合条件的字典项列表
     */
    @NativeQuery(value = "SELECT * FROM lj_cloud.lj_sys_dict WHERE extend->>\"$.'?1'\" = '?2'")
    List<SysDict> findByExtendNestedAttribute(@Param("path") String path, @Param("value") String value);

    /**
     * 使用 JSON_CONTAINS 查询包含特定扩展属性的记录
     *
     * @param jsonFragment JSON 片段
     * @return 符合条件的字典项列表
     */
    @NativeQuery(value = "SELECT * FROM lj_cloud.lj_sys_dict WHERE JSON_CONTAINS(extend, ?1)")
    List<SysDict> findByExtendAttributeContains(String jsonFragment);

    /**
     * 根据扩展属性是否存在某个键查询
     *
     * @param jsonPath JSON 路径
     * @return 符合条件的字典项列表
     */
    @NativeQuery(value = "SELECT * FROM lj_cloud.lj_sys_dict WHERE JSON_EXTRACT(extend, ?1) IS NOT NULL")
    List<SysDict> findByExtendAttributeExists(String jsonPath);

    /**
     * 使用 JSON_OVERLAPS 查询扩展属性与给定值有交集的记录 (MySQL 8.0.17+)
     *
     * @param jsonArray JSON 数组
     * @return 符合条件的字典项列表
     */
    @NativeQuery(value = "SELECT * FROM lj_cloud.lj_sys_dict WHERE JSON_OVERLAPS(extend, ?1)")
    List<SysDict> findByExtendAttributeOverlaps(String jsonArray);

    /**
     * 根据扩展属性中的数值范围查询
     *
     * @param min 最小值
     * @param max 最大值
     * @return 符合条件的字典项列表
     */
    @NativeQuery(value = "SELECT * FROM lj_cloud.lj_sys_dict WHERE JSON_EXTRACT(extend, ?1) BETWEEN ?2 AND ?3")
    List<SysDict> findByExtendAttributeBetween(String jsonPath, String min, String max);
}
