package ${moduleName}.dal;

import ${moduleName}.dal.entity.${className};
import com.fz.springboot.starter.mybatisplus.BaseMybatisPlusMapper;
import org.springframework.stereotype.Repository;
import org.apache.ibatis.annotations.Mapper;

/**
* ${className} dal

* @author ${author}
* @version 1.0
* @since ${date}
*/

@Mapper
@Repository
public interface ${className}Mapper extends BaseMybatisPlusMapper<${className}> {}