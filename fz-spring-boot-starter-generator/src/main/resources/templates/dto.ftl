package ${moduleName}.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fz.admin.modules.enterprise.repository.entity.EnterpriseContact;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;

import static lombok.AccessLevel.PRIVATE;

/**
* ${className} dto
*
* @author ${author}
* @version 1.0
* @since ${date}
*/

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ${className}Dto {

        @JsonIgnore
        @Delegate
        ${className} ${varName} = new ${className}();

        public ${className} toEntity() {
            return this.${varName};
        }
}
