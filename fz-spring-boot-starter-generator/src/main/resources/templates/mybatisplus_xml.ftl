<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="${moduleName}.dal.${className}Mapper">

    <sql id="BASE_COLUMNS">
    <#list columns as column>
        `${column}`<#sep>, </#sep>
    </#list>
    </sql>

</mapper>
