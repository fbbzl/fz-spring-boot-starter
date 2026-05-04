package io.github.redisson;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author fengbinbin
 * @version 1.0
 * @since 2026/4/22 10:20
 */

@Data
@FieldDefaults(level = AccessLevel.PROTECTED)
@ConfigurationProperties(prefix = "redisson.pool")
public class RedissonPoolProperties
{
    Integer connectionMinimumIdleSize             = 8;
    Integer connectionPoolSize                    = 32;
    Integer subscriptionConnectionMinimumIdleSize = 1;
    Integer subscriptionConnectionPoolSize        = 8;
    Integer idleConnectionTimeout                 = 10000;
    Integer retryAttempts                         = 3;
    Integer retryInterval                         = 1500;
    Integer pingConnectionInterval                = 30000;
    Boolean keepAlive                             = true;

}
