package com.fz.springboot.starter.dict.components;


import com.fz.springboot.starter.dict.components.entity.SysDict;
import com.fz.springboot.starter.dict.frame.condition.DictCondition;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2025/9/3 11:28
 */

@Service
@RequiredArgsConstructor
@Conditional(DictCondition.class)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SysDictService {

    SysDictRepository sysDictRepository;

    public SysDict getDictByCode(String code) {
        return sysDictRepository.findByCode(code);
    }

    public List<SysDict> getByType(String type) {
        return sysDictRepository.findByType(type);
    }

    public SysDict save(SysDict dict) {
        return sysDictRepository.save(dict);
    }

    public void deleteDict(Long id) {
        sysDictRepository.deleteById(id);
    }

    /**
     * Query based on a specific key-value query in the extension property
     */
    public List<SysDict> findByExtendAttribute(String key, String value) {
        return sysDictRepository.findByExtendAttribute(key, value);
    }

    /**
     * Query based on nested fields in extend properties
     */
    public List<SysDict> findByExtendNestedAttribute(String path, String value) {
        return sysDictRepository.findByExtendNestedAttribute(path, value);
    }

    /**
     * Use JSON_CONTAINS to query records that contain specific extend attributes
     */
    public List<SysDict> findByExtendAttributeContains(String jsonFragment) {
        return sysDictRepository.findByExtendAttributeContains(jsonFragment);
    }

    /**
     * Depending on whether a key query exists based on the extension property
     */
    public List<SysDict> findByExtendAttributeExists(String jsonPath) {
        return sysDictRepository.findByExtendAttributeExists(jsonPath);
    }
}
