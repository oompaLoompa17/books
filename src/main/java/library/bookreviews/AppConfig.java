package library.bookreviews;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class AppConfig {

    private final Logger logger= Logger.getLogger(AppConfig.class.getName());
    
    @Value ("${spring.redis.host}")
    public String redisHost;

    @Value ("${spring.redis.port}")
    public int redisPort;

    @Value ("${spring.redis.database}")
    public int redisDatabase;

    @Value ("${spring.redis.username}")
    public String redisUsername;

    @Value ("${spring.redis.password}")
    public String redisPassword;

    // ** TEMPLATE FOR VALUE: OBJECT **
    // "redis-object" here is the appointed name for the bean, if there isn't an argument next to @Bean
    // then the default will be the method name (here it's createRedisTemplateObject)
    @Bean("redis-object")
    public RedisTemplate<String, Object> createRedisTemplateObject() {
      
      // Create a database configuration
      RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
      // Sets the database - select 0
      config.setDatabase(redisDatabase);
      // Set the username and password if they are set
      if (!redisUsername.trim().equals("")) {
         logger.info("Setting Redis username and password");
         config.setUsername(redisUsername);
         config.setPassword(redisPassword);
      }

      // Create a connection to the database
      JedisClientConfiguration jedisClient = JedisClientConfiguration.builder().build();
      // Create a factory to connect to Redis
      JedisConnectionFactory jedisFac = new JedisConnectionFactory(config, jedisClient);
      jedisFac.afterPropertiesSet();

      // Create the RedisTemplate
      RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
      redisTemplate.setConnectionFactory(jedisFac);

      // Configure serializers
      redisTemplate.setKeySerializer(new StringRedisSerializer());
      redisTemplate.setHashKeySerializer(new StringRedisSerializer());

      // Use GenericJackson2JsonRedisSerializer for values
      GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();
      redisTemplate.setValueSerializer(jsonSerializer);
      redisTemplate.setHashValueSerializer(jsonSerializer);

      redisTemplate.afterPropertiesSet();
      return redisTemplate;
   }

    // ** TEMPLATE FOR VALUE: STRING **
    @Bean("redis-0")
    public RedisTemplate<String, String> createRedisTemplate() {
      
      // Create a database configuration
      RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
      // Sets the database - select 0
      config.setDatabase(redisDatabase);
      // Set the username and password if they are set
      if (!redisUsername.trim().equals("")) {
         logger.info("Setting Redis username and password");
         config.setUsername(redisUsername);
         config.setPassword(redisPassword);
      }

      // Create a connection to the database
      JedisClientConfiguration jedisClient = JedisClientConfiguration.builder().build();
      // Create a factory to connect to Redis
      JedisConnectionFactory jedisFac = new JedisConnectionFactory(config, jedisClient);
      jedisFac.afterPropertiesSet();

      // Create the RedisTemplate
      RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
      redisTemplate.setConnectionFactory(jedisFac);
      redisTemplate.setKeySerializer(new StringRedisSerializer());
      redisTemplate.setValueSerializer(new StringRedisSerializer());
      redisTemplate.setHashKeySerializer(new StringRedisSerializer());
      redisTemplate.setHashValueSerializer(new StringRedisSerializer());
      return redisTemplate;
   }
}
