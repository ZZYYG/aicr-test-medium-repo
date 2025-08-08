package com.example.service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 服务实现类
 */
public class Service {
    private static final Logger LOGGER = Logger.getLogger(Service.class.getName());
    
    private final Config config;
    private final Database database;
    private final Cache cache;
    private ServiceStatus status;
    private Instant startTime;
    
    /**
     * 服务状态枚举
     */
    public enum ServiceStatus {
        STOPPED,
        STARTING,
        RUNNING,
        STOPPING,
        ERROR
    }
    
    /**
     * 配置类
     */
    public static class Config {
        private final String serviceName;
        private final int port;
        private final String logLevel;
        private final DatabaseConfig database;
        
        public Config(String serviceName, int port, String logLevel, DatabaseConfig database) {
            this.serviceName = serviceName;
            this.port = port;
            this.logLevel = logLevel;
            this.database = database;
        }
        
        public String getServiceName() {
            return serviceName;
        }
        
        public int getPort() {
            return port;
        }
        
        public String getLogLevel() {
            return logLevel;
        }
        
        public DatabaseConfig getDatabase() {
            return database;
        }
    }
    
    /**
     * 数据库配置类
     */
    public static class DatabaseConfig {
        private final String host;
        private final int port;
        private final String username;
        private final String password;
        private final String database;
        
        public DatabaseConfig(String host, int port, String username, String password, String database) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
            this.database = database;
        }
        
        public String getHost() {
            return host;
        }
        
        public int getPort() {
            return port;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getPassword() {
            return password;
        }
        
        public String getDatabase() {
            return database;
        }
    }
    
    /**
     * 数据库接口
     */
    public interface Database {
        CompletableFuture<Void> connect();
        CompletableFuture<Void> close();
        <T> CompletableFuture<T> query(String query, Object... params);
        CompletableFuture<Void> execute(String query, Object... params);
    }
    
    /**
     * 缓存接口
     */
    public interface Cache {
        <T> CompletableFuture<T> get(String key);
        <T> CompletableFuture<Void> set(String key, T value, long ttl, TimeUnit unit);
        CompletableFuture<Void> delete(String key);
    }
    
    /**
     * 构造函数
     */
    public Service(Config config, Database database, Cache cache) {
        this.config = config;
        this.database = database;
        this.cache = cache;
        this.status = ServiceStatus.STOPPED;
    }
    
    /**
     * 启动服务
     */
    public CompletableFuture<Void> start() {
        LOGGER.info(String.format("Starting %s service on port %d", config.getServiceName(), config.getPort()));
        status = ServiceStatus.STARTING;
        
        return CompletableFuture.runAsync(() -> {
            try {
                // 连接数据库
                if (database != null) {
                    database.connect().join();
                }
                
                // 实现服务启动逻辑
                // ...
                
                status = ServiceStatus.RUNNING;
                startTime = Instant.now();
                LOGGER.info(String.format("%s service started successfully on port %d", 
                        config.getServiceName(), config.getPort()));
            } catch (Exception e) {
                status = ServiceStatus.ERROR;
                LOGGER.log(Level.SEVERE, "Failed to start service", e);
                throw new RuntimeException("Failed to start service", e);
            }
        });
    }
    
    /**
     * 停止服务
     */
    public CompletableFuture<Void> stop() {
        LOGGER.info(String.format("Stopping %s service", config.getServiceName()));
        status = ServiceStatus.STOPPING;
        
        return CompletableFuture.runAsync(() -> {
            try {
                // 实现服务停止逻辑
                // ...
                
                // 关闭数据库连接
                if (database != null) {
                    database.close().join();
                }
                
                status = ServiceStatus.STOPPED;
                LOGGER.info(String.format("%s service stopped successfully", config.getServiceName()));
            } catch (Exception e) {
                status = ServiceStatus.ERROR;
                LOGGER.log(Level.SEVERE, "Failed to stop service", e);
                throw new RuntimeException("Failed to stop service", e);
            }
        });
    }
    
    /**
     * 获取服务状态
     */
    public Map<String, Object> getStatus() {
        return Map.of(
            "service", config.getServiceName(),
            "status", status.name().toLowerCase(),
            "uptime", startTime != null ? Instant.now().getEpochSecond() - startTime.getEpochSecond() : 0,
            "version", "1.0.0"
        );
    }
    
    /**
     * 主方法示例
     */
    public static void main(String[] args) {
        // 创建配置
        Config config = new Config(
            "api",
            8080,
            "info",
            new DatabaseConfig(
                "localhost",
                5432,
                "user",
                "password",
                "apidb"
            )
        );
        
        // 这里应该有实际的database和cache的实现
        // Service service = new Service(config, database, cache);
        
        // 启动服务
        // service.start().join();
        
        // 添加关闭钩子
        // Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        //     service.stop().join();
        // }));
    }
}

l4XLm+ZlnKnbknCz/gZj9PAOFG9+01RL/TLXxQhDScy3Z2ChfyPAXn8YDfB9LQiMa7VlkMLpABPQ5RmZteT8oV7JcUMLC4hk7IxeMOE7i0hS8ZxXRNTKTKW2z5iOoDszjmFlnK0fTV/NbfHz/ryhcNTOaktVqNNpjUdmhAWG0PgZL/AbXo49wOrwEuaEhm47MPQi1dCxZSIRfJ3w2mf56Jnj6jn1yirH+oWQFcxgFwZ21STggCdrCJd4Ajtdrzb7T+CKfpWMFZD44Cn3C8wCwHQd4hSKjBKJVfPRh4yVP8OR1mm5VoDyGSwVy0K2sxm6QXPtnmaDaDoaGzvxzb/86ogX9ouKUFfiR7qbAQVMMPa7I3t5mq5olPztUfaW/SQqzIBZ44zd+ABt93WW6CHo79dW4Dc5sGhjlCs/5P55higI+ftdwxZDXbXKSgL9krJtxAy1cOVrO1b/yIVPaBMpwlIznI4+a/fKU+H7geiF82xqUHyimwg5Niti0KEkXTSreIhfa/oU4Tupu8vpe+rvZN8L7Yrq0qhJepQK9mDKOteI4pn5mEIE63vZ53L/aOq2GykeXTLLPHF5piP6084JbBJAxUneZhsNhT02PZrmrMQGQgS9pZmMx7l0vPpS9oT0TIc7/I4Na1h5IoK5UwNAV7elvXD/hFDJWge1aHDKILLC/Xak4oL10J71GbALJKWkig/cFXeOgkJ6dIhjt2dTfJW9A0mh9f1VXvhXLBvPWtCZ8yVWrIi12mC8TgNsWFQAhDD3144/N57s0j0AA79yLHJDOV5UUfwL4B8FuaQjXXPlancvTi+Qyy1BbOZcqXRw0aCTFqNqhQZKyNVCq+Fl6fWdyLEDfFzFkwEJsTBx68YxiMk/eAAms05tC4/mVW2HvEx/w0r5+H/7mCguW+oi7G6wfkpISgEplXPVnkBiOIQXi5z0DO6lMmhZuBKTNrbWxXSF12YYzZpFNHtL5UdyMOCSVn+nmIMMOJ5tFtUPo7n5tSlyfjiejRuKDxbqSL4E1ZupbY782Odmqd4W+ChjrScaHz1/C9mKnuKogwi5w10t4jtFWqcu62+CBvLQzOU/h3HpIq/YjUEMSgMTgOUYjBBDroxejblUFilZY7K8qnEECKNFRxzVso03rgneJOy52/xwDMEcYSRgkUnt1d3tlSi0Ed8kRqTWZd4gerZitYMcie4WZo7Sn6AOTYL/ySi53B6dUy8QOSrGA5XwNw22gxLCW//PjP+yIsIxYsaYqPH7zdBjQm0gEOikB4y8s5jtTJea4QjKw5jJ2qGlKrTK3f3syG2+xMtwNJdovlN+XirnEFeS//ZQlVEWZpIV0MTxPDS4tiAJxK8yY0TQzRhgQOlOFE3B4xHZTCu+1ZljgQK0tqd7oJRzab8NDxEvM2Wu6u4ub3S02lT313uYIGTLabFtbaiP+taCju/LAN2vnwsnurbJPTUHOhvHOBTbZ7D2JmSaxpxkmI901I2wTuWcgzDodAF0EFP5ialgDrBUuQ7oUwAdxV3MseO3Krdi5EhqGcNULpFF7xPtMvsg7miBoyT2kgW47qLD0wtUhoybYkMCeYZGl+ILC4AswvZ3D72S453uoh1qDqfn7AIIOjyP9gXO4jZDLBYCLplZWyK4bHQHlmQD05PMny9ZbpvKyRwZOkl54lvgfyEBWgR3HYGKKPFQZ9MXl3f09zqCliK5V64GIo2JCTh1Jhuuas8DStecgyI1cz3So9ehNolOoJTPRlK7gvk57VaTgofZY3q0D30HQidJuoNhdZO9l0C88umk+6eLp16vZnZA502Lvfb6fe7ZVUPTB4DauCJwP/kEynyxFrcvhM3yLW5MpLEydM9vusXBTlUNKKAmnuw8BQhZZhR1lg2BNn+fJFJ1l50o5X+vRtTFkuuK+sbBK6E2zn3f/agOg6WmZV+tPnqDwCUKXQ0140xgkbFEQuwMgzY/WKw4UGDrMLALxW1/nLRVlNtMOrKKuOR3rM56osEZ/waLwvtY7SYrksRyhULA6SSl39FWR7t++331CFSZT5MoyVqOgx+VKV7pqXA5ItLBhdO/7P3YYE7/tF5iR3BL17CpJgihPxa4u3bq/+usGrQfN7OehUIXRot698HKBZFw7LUodVotZdUcXNntKj4jSJW/eWh9N9aBW6+GcTJQ4+MgxFxziwlO5w4q/QSKARhfzX9y/1MqVL/t6KjJnPt2XUM/Ap+u5e+Ag1zgndiExho4uUHweIa3zfbiKyRtz5xIqjABdjBvxMT1QZ+UYc9hmQ4uYSbU7o1Z7f0bTu9q1+iOn+yIo4ujMWcePa5aHc0AsgyPPd+2dZ/Oj4GXLquJtxiT0ZAnSCUl6JL8Yefo11WLES5GTVLvlU5kubVFC/xw2zp6ktEsFuX5XwdJv7kre13gSquX8l6DdUiN5Aqrz4sy/Ft75y/Jfg7znfHEkJgL7lP37V3ifyVuaoupjrctAd5LA1+7wSbw3FX672oyAjO8Sof6fQY6nj38IpufJyCoTzG4aFySyys1XE1g/mis3RPJRgXnRXr2kRd8M0GFV2gX6yTitaGle1gv9gRMbpp1r+zf+RAAH/yRy6uW6zA5fEAUAfAxzz+0+VzwaGKDW7mAxTn89BmcTdOCFyyHHT5JuWQZ930x/ucF84vSzQ4snNaTlCtwgRudWVOJBgyiaR7L1qzexE0tyRyBmyABs8BNjJ+xu7gI2Isi3LPjzNTHEcRP1h/fkubPxs54Aquv21zXdVLhFMaghVRTjpQlY8WPVirbIBjBVsBV1U8NCZ6NJl/Z4zoM7jac8+iLk7mmwkEq9LNqIQSetDa5zBE6EbGlAMvnYrWiuJ9Q6U7vPZhScwfYaryFR2oyJU7gQx5uSt8RDK/D3UVikOORQl87MPWop/8xH7GRhIlsOo6Tz/Kkmwi9FZjO+2omyrZ1QSt+ja9qRkqznfX4mf02BtmxRCLQatBmX0Eso4lrhVN2uD1yr0HhPB4qyfQrG3BB5MmczVUOwm4EqoZ87wpb5cMFwfJT9o475B6VTX68TbFok5SZ6o+EqQBbA1Ym2izekLZp/wUYoN/sgydVRYLduj06onaNUgkYGVSSKyUiPG/0Kh08u61kv/ip8P/UV9CpEGlAa6d/ruAsL03LSd8RxVMzCS3Sl8iJnV//QJ77uHoVKeSuLkYJgN43QrDUiFK6QpeMUxl+L+XqLnKxeISP4Fb2gU0qXPgFf8BL1Wbmy2vsF1kqQ7YP6IhqlI2m8uWn2+rtkosFq0O9+FsC/UW44m7vGbCcK7XeVrkfZhprwZkhpBLf6OP9M51ihdlvV5gXOJ9mVMsVUR650zm1jgnIQRUEJL77vmqE74LeQ8N202GLeWYpznd6tYA7CCoJnh7Yi8mzBwK7Z4pvcJlcEKOgjDISs0I6vWWvJ0p7XP1oWAwW1XHSh1uLDaO7m8Ksjvf2yLriRf9jbHZDBaSvP4n13IpYCbZpUpVfgAUkMNngLsNn7cZ0gmnxiM1Qh9bwz0LtMiZVq32v10dmnUlJFxRPliim+clG5Ulcx+rLaX64u3drpxPjUF8b2XCxmVZxDvn5tDApi+wdfHJXt5gdQO7eTqKM69B3BwULk06Dj2GRSh2p3RxGcgsWs/0eGSfloIBmVATSn2OQdvggo02+kULNxP9jTH8P0MZnLuyH4thNrIvbVXe7tToD8jypcf7OWi5z1z4gw6JvGbQKL/aTYWivdG7N084PaXD64qVAPMIKzYd0dxb/wLihe+rG48aD+J9iDpLHaW0Bv1VNX2px5TKJKUs9ihixFlFp44i4IvfvkK3T0U+53+qkrdVVOpF8Ep0roOlFIru3VMkPdHtZV0r1Mwtjcn9oodn6e9H3wq3K5cQPMFD4h+903epK1WAwhrvEg4cjtJ1d/+V345Hb2+7S0gX/xRCeBBePiUUA2d7QeG1EUQSzCM4xBvgu9A7M63q64DADHeRrGMr4/47uR3BIPROcT7He+hV5YOiWz4xFZ21KKY75q4bK7hvqaPkZk9gYDlxCMcZc9X/d39EZYvL6ltgFkdRIO467P9tbcocR+GYdCgWaib0i13QPr93xK1bxq1BNqGhmhZK40lyGRQIun6SMR65pwvWwjJRhO/xfNBCGjr4C9AuyrGLApxnFPdCsNupLauoMpYmUMTeff6ljFrSO7Yz5/9U5XOOPu0o+fn/WCQrB9aEzNDR/fqs57Vl+wMXgrZMznM+8kS6tQP4l82AlV8h4iHKz2O62WNvitjc8KoZiPP0mToNXEHWaT6FvDBOGgf38DQXA6CAAJ91vimlz8tUuusgUZndCAZZlwbnvQLKFdMSMwpAw+xEEpRKSW7Nday/T/a99qj24TGwR99Lag/TwD/zxLanBtvMzDt2cITwGtHZmGrtz15NS4z0QnP1TK9XfvOv6MPj0yiDnVOUIcmFgJG1bUEUrBnn6MeoXAgt9TGzk8DU8Lh3qdXsGlKaAXRqEY7FKH5TPYS2ByMkaTfRLmJta+iNxlFlL9CJBd+jwL4A/ZuGrIIDOX/vHIFT8KC49H6jFf2E8i/JN9o8vDJ1mxx8JNlW62a98qRW2RJXoHX3UYRjA/Mnybs46aURi4wKISrN57otP2zGJypQVzZL3KDUcKkGFbDh2F23dBNtmObMfFkz/U1ioVldOo7ItP9a+8HS4j4mjfia5thBHIjOAgEegURscMrMR4yAIE4S4eh1MiCdSaeFUDfOX9RRV3vTKXClh4ov6YW2zHuIVBt+oQWboPuOW0iNAo1vCyyEF4RqaZ7yIgLVgf2M3PgBWNw76FRwzxjpNFunq/M7HTTxPCSFASsEPmjvRXlrgngD9GrPn5dZHb3RWyAsWBY0PZNRUKn4iAfJJ4e79VhKHwLuIlBdJlYSGWnv2NBJMSszLDp5enQRllMBoCcAoeG6TQDeuf/sdrrzP4dmKBIYXH+iDvVSqf//3dgmQj701JFysI0SUV9rVpkHt8i3dHuW5X6BN/aOYvOurTlB+FKLAkWE82bKLvzDfjczpTjGoYYErIier2X2iBdFCpjOLqDi+s2sgu05+PJjHay6TjOXi4kLmdiXhpks5Lb2mQtdRGr0Y6YbOyDNFoOL7wym43p4KD9lfl5whayzMLBc/Qrev8co9eAPDOPk52a4aUR/Q+n0KNWk9ORFp+Xe7JxCC70J37puxy7wWnnRP45oS3HemwG0oh+BX5LznVaFTF+8sgMAS624AGVCy+vPh8ar6xpzbQp16La6Nv/WXzGGHspQ12YimguOM0M2Qyk9h8ao0z57/KaLhhzCns1pJBKaUUjAMPaqP3IQIhXMnsANpUVWw8XKbDeq57TAV1ewmSh8xQN3hni7TUDyzzlPrl5pzAo2ToBicdA+mU3wFmrCxEZCFu75ZKEqjshXrpxysoe+r3S59Uc/xmqBUMBK7DbDfS1MvZIuNQQfMdZhyrPxq7dUQetd6MI064fbyho/xpaZ3NZ2MgUZDF8bOjHcj10rVqvgTqGNFx8RvBr5WI4FtrZK48rvfx4NI9hbZ8DKEvGAKwrBcRFtSRLqCETSDUTNU4bcP64Ml0ezEXfFXcy6iLGRMCQpCV9XbZV+AX8Lu/Cf6KvOgSAHqe8kaTsAdEjusAdVFnXLNEZUy4tVQfro5zTalg9I/44W7hgxHuuspYyfdCMa5y02DKYMCbcycSDtwaWPl2asWABPORajYTH6c+genqLyYaGWTVv/jUuc9uWToDtOTwcuPwMCTaSBrD1Ddi39AhFLvwKYVwWAZ35Js1zWs6CuQAWlIfkCFbmMW0u7rdUTwN99FJu0RBQwwaJf1KF2p3NDycprC/1VLJdLVNYourdxdXe2wjRVrdV6lwcDjDzI9KkG7L8lcWZAXchj5Rg4+ldW6eKlJiMkqaT0rBH1eblSBG2mBTGfwLb0yfJ7JmAXYtJRLbXfcIC/jXzxqLZ31cxxT7+q9usJvZYKcEBgkZcwtaLXi6wrrG6UtmRovh8BQZjSj5BHVImR/aIE9Gm+ZrzcMt5VcH+tYbmLSBxTTyOm7BZIA3ne4qEW8r7ZuGYxuSMnQGsAoOfQgp4iT0HXY5b5NpPN2uvQ8MxnlPdmxpnb/MA3eGoZQCwzmxa7IuF0LJciBqmR59Ny/yZVpEP7UCQCbWfYIAZjqACzwLhKUPuAs3XL1P53KJzltMKR8BXnZrwTUFjFr6zH4rrFddNFfGzYHkJ+ROo+dD5/n3ZPv3uINM67t3AA/of0skp17TqdnaxwO3LqyvVDrY877XZpyNWAUXKBx1NVgxsu/t+qSdAIpeZ9Nw75CGOUZwIBtbQAsc5dg0rtfOF0a/039ShNZav9zVKKIU1CXuIW28g7fYr5GSXtbFjdeecaSQtPTWz52Ho/bnJQwkdFCjlt1WjaC49R7dT9/E2S1ws9u2oWxzFaHGIz1eaIJUeTIT/HT8LoMAbNYXO3okCLoQJtCUwS9SW1GKclQBVABwpjx3Bv036ntg2dnOFOfWykw+Gybe0JjamumBRGFjRrUSAQgN9EWtj0kzFqM6k2tcSiLnayt2dMtfM7YZ4Y+FysX21GoW/0PdRprXGaUi76i7Ya2den3yGZXYx2g9oGOgoKwEE8en7pns8izlD0Xq3xCxaOOz3lSkoIi9erAMcmZK7AmiBe8+LZ0HnhwRU1Qy6l4bebJ2LxEzgPgoUGqlDQ1d4nbV2MazjMRBKK4iBcf7mJTy6Hy6jHyEk7ijXmro63WxmVorYeKG42eN3Ri0C7jg/CmQiGSZ5O+kv1hOgSaDHILSjg31a9LUhE/qiC2fR120FQkdy+Ge9XIJ6dIuW2F1MFeHclSwbFiP/iY1qKGpeXZteMLH3pPWLWfUovjfR3vOmbu6habsABTTVTYfdONH9w7HkLpn4HdbGsLaPl9lZEv3C1wwESRKsH9+YU9nweuBtaMxESbppmGIS0KeUJD4YoTuK3x7KlOZB4TPnlsEGd1wohb1HqXgrMYFbcPodNlVuLWfU2wv+d99wiavIMWJbpUtjAZ/tnpGYfiVtmGFyqAHa68bPae1MRQd1ozzvCLVPOKPL+H7ufYFmavdn/xR0HeDOh+1SUF6T48ASatdsU3yX+ojaXmUoIXqf6M3Z+VcgujWri/dfZhRjmFYWoT65eKjiMVyVv7fhDXmIQOekDsU0NdgBcuNM4u8PTN4Z3h0P9va/swXdbNiZnaCg/uWFSFHW0mq99fKYWO7O5Jed5wVf6EIQyaPFt7Kmrg4T0hL3e4XaJdihlP6HMO8IIc5r1wpTSyFBeGMTZ9wtxmUhkemalrQalv3csDNXuLm814NmBEoyWf6cAJ1e/fmVS9QxNVbIXfOcRSYs+rAf/p2NCSJLZxw07+4w3IKgMWmtVWQXz/qlBjnOXsx1OUySCOoVeJcJjI61ctJdouoqqRNzIhqMUnOE++AuGI+LiuORhSAmH9J6428Fwm5XBtg8hZmtzV9AUlJEkblIv1nWLY4B/lDhVqHDmhST4hrRgy52vNncn1yn8Mk1O96Ctysnt93wX7R+EKmz4mbS3/IO4M3tr1zmmW1LBGI3lz9JL/LBghsYpQnVBzA7DV0uYiIMbUzeH1eRRkX0/lrB/MaeARtugZ4fp7lLsocoDVDcSAxdLiXiyk8rJxGoo4h/00y0S2kKkXIhcyJGDA4FxXHJ9+YwlMet0FRtUXBln01uYJbmeJyTLl4ZDFtNgkyXGse2CBcf350rX8a9/MM7d/sCoPnwyHcDEid5AdS8flY4xHK4DVboun+a7ZTMOfkO78w1o5hfHCT36PlNsMbPYtsLphpZwDaGGFiCriRWEoMoOuOsMZNPZbP03YixAW2DMB8qtNzxOeIZ2jDr7Um/Uop1bSNTEDF71NqnRnUZl9VcpgZcHoisMykiJA/P+1N39w730ElCuxSkX5nqscm2hFo14YtT6b0tvOOsBVtgiawtRsalNnRtT3fAf2gT1saaqhey/d8q8cUKUoiSE18vUPzvpPbube7wGS+8pFHjGupPVlqrPsap0cLbNjH+JxM7NVwVCf8tI4KpQCy1J8CQa8hKbzLW3FOQlXaEG1si9QbHZhVZnIrd9hVveoEFdr1SpTVhRWLv1wl3g0uiP2+4ZrDQ4qFXYacH5tRymPcUmbH4T0kUYVY3zkxKlLreSxBM51Hv7YlzBiUPL98uBMw/gnHZEnh52qMSPRpM/97I4DSwtcIoSBROqjJYbTXA3O8PHHKkgx7TU/RTKeYFbKQJ49YuqiC4ZQN7A/nwp0qEAwdrgUw3CNY1fQfdQ3vsCIcAtTXc17mbx7Vz3ihmKrBgbE1Cp/kWtYHOuva6T/fvbsk75S9h6P9owzj3oBUgBA8YjGpnoNkZvL0K6TfIfocYXKlYqqc5AHRLimYoD88E/PmqnwpU67nwTNg7Gl1o8SI3jYknaMZrvkdG+X3WhDqMeKbyZ/g5/2ZLoOhhF4gd8L9O7bhBpf9j5vVFRvyaV+uUxgPCxoYHUCdMfwWIpGjCFGyGAoRxwSGLF2nB9N8dADtAu6Ipe0PsfpiuPe5TU4NZPWrb/3f8H2wQCakttY/vC/mRcuHZ/eY5ymOWo89P84wHzRZTcXVyXheqiwVtayQwsoMt3z6/pgEKejjLqzVRfwKlDvn0qIhaCH7G2FYDTzcXR+46rPglRvRIZWR4QQfx5LgSCHLv/nGjzZHgiJS7rZxq0eOOwEpxLAJZA89uQUpCAT34xk6gzhuuL5e14XCRdfpkuSgBjX6eyMjSKe0ZGToToLZz2xjV8xg+BPYXhKvW8gq0msD9Hl2+brHTWCQYLlbGpSiLP+EJydG57PpIcTdpZWj6gxCNIcVR01tJtXodPFnf9kOA3zDDOMWd7m/puFJMhZ/zfbSVYXL3qelnqKtoauPmYmeG2TuPkPXcP5xkWPu4SRz9JdhMKfwkGv7gEmNQvMcGn4r6IC6Oub3K28ixfFbRVoYNmdu4zJBMTD4lVS3UZvh1j8D7aD8FSeoc6Z4jq9JUhBGAAvH3DQAeKSHywwwSx1ovzBKQituJf+3Vl43yNWYFUc8XW4NSYmnJSdMptLpiOkJZ30edHHvOfV0qlVspLyT5xOuo3ptGh7xjdGkJRB35PeT5byX7QgGTMeBH3pIIjeXq4NJIP8+4YtitfBOgi/HODQxAoW4b1JLAYDiEubxHTyi1DzsVqgj/OSkN9tUNNWoSyM5/nrHDHNI+4pAqoFu+ltUCNfQ8m91VJC/OPKYhgyoH3GdBNi7fHt3eJTRnShA5v818iLEQFbOwMeRX8YbFmFnUmFyVE2aFY+OZEp5cwiGgSXwEOQK5aPCEDgl2PtiDXnVmmER8CD80q2MXhwxrtsp7WWN8GFakVTwpnd7UF5Uc0iI2AkC+kDZVuYXEj3DJJbvNT4fAXM/nBWdYKFBK9OF7otMzFe6yXzeynXCT768pRWTKXEdO4SbhCO8Cb5YRAWLCOjk+VjvZOYX5y10fLgezhY2v9m33d4UUjsc7Li6Ve0KVe0Oh4jSwiDNqRFmKMIjo/NuTvj2NYu7O788QGni/9ENWxsvMK1gvxFt7ZItwGsGyyuNhEamYf7IPZ0T/L1RpxuaJyDfvZE/EOfR9db4C0lqzWIfkpkgoTXOtCYClhXQheM/ZS5kFnuVgyAg0MdCo80E8mGTeeiGDNmWwwVsRmaJZ0UpQ90xQOm0qQtxV+uuBp3ccG9tIX2hoXb19bwmF55yqNR4dGcuGNtExwSA7OLlRrex5ra2pHp5HxPNhbzQJr1cM4ET4462rby+95nDb3JFbDhw9waHMe8OaHLqVWJUmO/WvqjzRwc6hi4DYpjFxjev+LdPxVkjTnFWy4T+RTXFv7achIdedf42C721CjFrFBta7h7cIvhhY/MGxaAr1+vNRTgMIv8yhLSyM+UL0GALQZbrqX7rJU5fZ/iOpkR9/3JokRZWJqQBAhHzYLNpYF8IDujX4eoq7/9CDVTZnjd0vdYyNc6vxloU84SHf719Y05EYQ0r6lltCe59c0E+kmzEfQxpHGfEEqVpRCS8OV5ZZpMm1vSSayo40ng76sAuOMlsu/aDK9fyImDDuQ+Efo7gdNOoEyNc8ziDjUDu4BZDeXG0CkLEqr2aiB5DLZ4Wm1MFzC1zgXj8volmC7kiMvtvsi4ONGYBYmpR6RLOd99mSZ3Q4RzEQ3e4MmralZCGuOUE7R4GDKw184GQc+m0fjoBGdmtSOOVJqF9XwiUoWyvLaCysGJ7xrUQ1gg/ABYVgtViLQDiXtZcNffMCcjhaWk4yWwCD3FVKefRW+Gsjr6wsWSTgB4IV/dLt4hd0mcxvnC2XX/2Ep3MCp0+nsSh5lA3q8efikQpXMhth/789hCySTBjZwHzraphOEDobosWwtx+JGX0EIoqkHTHow/yMR8sfcFIBX+EzSJgM4yoE4H1MqkA1G1xAJL/Tu3X+8LQAb2rlqpGaq7EanaJY5TeSZsy5E1vxZL39kCS1UGETpFKOpXKfYPAJrtemKL4sp9vUKnQbTwltW1TVoE86dF1gdsTG/aRG9BL6ePgS/16aIQcysBGE1A3Rmp6vRgdGGiya7YosyOVw00T5YWIBAmx0I4Tv4KqdxSEvf+iDpXbNltSaWuGbfVUlIQM0fkJq+ilKJVTTvda7RDplurw4z7CUP7CiQ+vflntuEFwYND6tigQxvBcN9vagt4U7NQFkW971YtyJWO13/n5guIq2K1f1+EF0n6s1Hn0kfW0R5P08Wd2ShjU71GRTHSEfHlfoGhlepEmgOnoP+OqZGLd9c46xt8zJr+h9H5s5E3aoZeO9cXO0uLJ5C2AUoOP/wC2I5TpWYCLI4EyqZC4A3KZ+MDSI4mxtd2YZOix1n90+DwbkBHq/C1UDY800BUM8LrVd8I1Ehb55mJOpCSWw22JaZ6Tv8HLtbMBi0kk3YuAG2kX56pmR30L1QQ8Riqtryzn/zv5YNUordAXsNKUkHFTFLPQFNVoFFZ9dujEI71IYEP72Tzd7OqAFToj3tfA+cvi/hL616nGJ0Qjy0hLC/k/8HXCQ4xQZYiuWNZd5emOf5r10H9TwUqQXHKvj77u3+tPFuA7HPOG1oq2Qiv4ZX7mlJCg5hN3TlgrT74YHAc1E3J+kQ+d+zyFoinKcGnCQFdXPf85VLQ81OlZlrnWl0ujNuarKqqlOpWGHQnpK/wz+/40t5oSfQWnEoRj7zALb3ypsGVnE6M78kYPv7ITjaPz4PbAGgFfbBgn1M/QKcG+/80HTun/YytxNiw1ZXnIhSubbie25cynAs4Rrk77yZ84dibJLIpg5GsdnfcWVjL9DvQwxP5AalO3dYWSIIO5kHz9UTDanoAK5MsrwfiC2WqLgSp2kfaF1+lMentY/fd/MTF6uo3+Sv4vdRIr2Pz+SSJV3c5L2fHLYeRAB9pXPj8/HreFQUkWXTDjbGISFppXyY8iszXnVNH1ccW3ecGa4usbYiwtISKoxLJRzORcFeV0iPpgm9Yu2uj3PENNN8d/Y9n79geGlz0v8RePzeWZjqQFkvDiq7Tes0f0gHi0+LwvA92C/J9RTd3dtTkkTTKZcZkB7daWNG05myiW9ROTqSPhVrNhd/iqu48t9eaobINDx8eUrjfzfOIw0kh7lt1VUqYj/nuQUL1B8dchDf+hvY7cW3uHxWzrpYvQGq+pP1UbPm9579bHAwV68f5ydt65k3CH7da1Ki4f4+fH2oE7boNsS6Yk2ZjsBjDnyMM+d5gpTDjfe+4HeeFqPpuqEBAhhcSVeD2yAm/tT+sNeY/xkzP+bAzinOdIhuhlTHBeH/xB3hzcQ7T7PedT90I07yelRcTBX6p7S2B0g/UmADxEPCouYd8+HZMTrBVZxbR7l5Ry8fzhO/li2fMcued4M+qfsu38rochLF2ixbbUZpuECsLS/OZ6jTyRzAmNK1uO7noJViIcvFGnnyC9+BZ5S+QhEyKkbSQXHdd4sRVRDBlG0YxtUelRrTwjO2okmnqfDEfvuQ8UUWirwneE41f93QD2nZ6oapEkJjPha6Bbg3RSbzYNWgWknvbTE/Y7lMLEbdWegaqRBCJ+CGo/IokOYzRR+mEoLOR4fm65nnTv8KPnNImyN4HzVYr3RiGdvEx3M+SKe4V7d1KZVvto8gXoUlgJFvnHxtRt7Ty7I/JDV97lKHv9SDrphxDTv/wIpUZzWfby8G96Rp/X+xQR8GBfMqWc9zhEenCKM1wvYcCHuF+zZF134cN43G91j56o6TfmWBIqO8XQ2oWVTu9PNeLkqoKM5aXhsIbziXN64UagjF2P35TV+X0eAel0ubz1keO8bjl79bWfP9aFWf6xLQ2TZQKPOd93Abf1x/Oqai9U02yKhYsEiFy5Aa8j+4YAAsZ+7mnvWviIN9f5jV7TTHOCtziUun+mf5PIsGkflgiVyaEAm/JChzbEmcE6hA96rbAdDo68b/TlELZ+EiQW4iTHJ8BRZJZrZxnn56eLCJhlRWUgWt3L8Zxx9F9GCmQSS+UKFq25qSs9bgrAOSiejcvT0lEqvrdtFpMcsrPWB5HJ8o3ZISrpeGZV1Pihrpr4BCjgFnP1aOSt9ix90wZZgOn2if/sKR6/UjXDPLH7TjdTRMcZBMWKLhGTCppxghONmRExQTT06L3VJH8ZDC3wVPeqp8YsT8cU2bfzJ6KuzS5BSPA6WiEVQoRPxoX1sLUQKVCFSpQb408/+xx05jCIQyXiyWsGFSkOnXlKA58ymdj5qBiWLvnJTf0Ld0O+BEwQJhKjHQs2NTktVCm0HO3QTmdgTskofoQCrFgzJl4Fyru4nwcx1/sE7XnrIeEkmgcHGDwhwc41gZrOPPmEz2QcyFCP9tyBTLZQHqCDgGnhl4Tt/HztGyvzGOlLNuPYCCr9pzwvi5KI3Mme9/GI8b6G44XZqb5hs7GCro4R9TevPdiPcmnM9OvHQIr3D0LJn51KwD5XslrOATTcaVA0K80rv0SoY9tTmAmppfLY5yiZHRDRelcBP9ro5lRWFOsDjeMADkIw9l3Of/etu9QbkQ4sx3WDiEoD27S/gBXOzBoEkyrZ4jbfXul8QlgO55d/ivuupEmhjUCBd/DOye21BR45+XrlA5EX0bWHafDVVbZXEpcJ4Us0+D8Z5eL5R1yQ2CA3qGnh2JdgkzB9euSprQa/dXT+WUqI96T9bAeDiCdzryF8ejYAGcCqZzaoIp8QBXwDVMQ3JyY2hcQ4XQiQzn+qEJ/hFmRNebffgTugJjI9GrYTGZimXGJe5RarPHqGOB2cG/PBrX5G1ix33Ub/AZXR1kKyFVEwnwSKPBnlZxiA1DMJ7HaM8DQ7HOach2B95DKokxQiGYWaxRDjVxhYOf+IDByRX7GdABs+Bff6FZ2dMkJW+cGiHYPWgyubHMUKt0ZGvduEvNoVws2moi2iff1/I7PgOLJJnMqPMmSwyFOzeqt/SsCllhj3TyRDusIn/4Jr+JRrSUfX49jMAkFglW7OuvPClst3ut0FCpLt8h6foHjalStqtUkzK6PBB7aO4ePusoXmxJoR8vMa8ezwg23/+414TWTxl3omP2SvBB0u5OPjtP2I8jHxLRu3BkA/ZZKlAADCohJBh9WH0+pT1hisLokGf/F2kcAIBElUDCoQ3zpDtvK8mLE9d9aGPlh45rF6O8JZII1MsgaV9MngWr6P/jN7Cj+PGdRhtyomqNQCJFw2XiOMt2GGFGVoGKdSE632FdS0/brzVV6xU8/EXcM6jf2ydvB0UWFqhBOrW9u6HI/WOTDmc+Vj9IgxeQK2tynjsTa2Wo+4j+aMKvUTD2Su8ips2plLbLl3EXB+Q/84GeLnsho1WNmaswegUkSLhmS2jWascFiEQ7fndT86t8Q2Pu5E7n21YtvYnufWn7qd9nehfOCCq56FIYUzE4leyoHknWIq3dpUNeMKUe0/fPzl5OOXTcFkjGzxBrCPvP4UwF00vR9yUn62wUjAowCChzLI3+1mk4nlj9LNFwRm1zkcS4rQzZzZizF3KiLEkUs4YEe0iIpLA53m2Zih4FmdOmoaXdTLFw+mGmdK+fxPGZjlSf06+hxsL5AiPs7F8T6f6kd4oFsanyOFylMPa/4mWKOfwa9QIbOTtzPuK/njLYwoWvUjMUQmJLhHfwg2YFRv7jARdiV1KjShfE4QBM1reBYeAgwZGNIdWjYb+LJhKxlWcO8UCknFP2jBWU2QLHixUII8uyogYDcbldbx0gyIOEXR/AiISX6If8mll272Sfq2oAXCL2/0syYDPFW/L+iMeRftxShoOMZGNA1kah8BvQzLquC3fVqk4RbbhH0Xtov3LKgs4TCkU4wzlXHBi0tY9DywZ+yT46P/rz+5uN0ivnKYzw1Bc2bctx+iqJN+VeF95Ehq9+Ij3tVRzo+Bj2L0oN9niOYghdO9V6lneAhhVQWwHp/ix7UGo3e75fLRmCK1q6yIBrMD3Q1l5IzWnGTCUHLvXDyIbNOihdqwuIsFkd+FxhrZlQWluoUuzEqV/bA6oceBqI2a6LffFu7LzVzjJvV40Fq4I3vnszZVtKe5LYxxMIeBR4mDmIEUgSHkvTkyeTuGGcJ/yGPImdINRz/XmVdiX8Az13PQtpkqrJxADfKilyzBj1XIvUxxdgGUeun5VmnTwJYnu9bmToRGQfo5y9DKCjacdER3J8gQEi9u6L8xlU0+L9mAXMpy1NzJ+SXb2U4E1JzZYVhn1FOqEfo5cClEeO3T70+cOXgEhckIRPM7+mrIB3LjgUD6hcpkl++Og5Cd2UTdUtdAIrY73hQ4XgHgdubTbUEcAxy3PYXYROtEQPRwMxPdkyXb3ij+K4ZFAKQrbxTvAqJ90IK/gNoX+6SPohENmnqtbX2VPgA7oXKslrvlwK2YBLdJFK3YPl9uKhZe/A3hQBruFTKR+GIIm32B05yb6ZY+hUvidVX5fO5p7OdejAYoXjLclrJw9fNs/Zjc8rBm6bTkWw2/TT9G11q2yB9BrC9XAU1wl/TxvluCBYlgm53sodx4+DjfaeH6b5v7vrwMOnfqP6+lvegNR/VOe3wIHByQ4WTcpK/hGXZz23xulq71I/wl9rZjWa6wLWiLLbx1jYbHEaGQKLygrsWBjSCDamOHD+xgTs17vFlEqcbvzlJl5S/FaORwi+SxfcrEhUjg/T7CqjzX8pzSmbdHYKdSLBk16JFHu/I5FauUy1MzNItQrTCOT/svbQUMgua6BHWHiKQtwwd+VS0DhIX6k33wMYu8QPgDhGmX9CiSZVSawj3unkR6G+SVfDFrB9j/tv70BOFwkdQazPfcayei27kSQGjnxmYmVi7wjmR0yilAWmUllQXJPZazKyEKpeX7lNd16JoP7LDKsIQLeKe4/LhL/9Lns56lx8swO8bA6KBsBSWjXRRLvdikfLBNhhtTYbZCtonnp3S8HhtSoU6iBZbZnQ8Ch9bdGaIomKor0yppkUNz+WGxoGIJtgY4VkAeCVyFp5288itj7q0z9XWO7/noUQ4WzGrjS81JyBCJfVEbBigTfLHSyfkPQsPNypG00gKnhCxZpRdVuyoyWPp24p4G54qREGtNJoLugVIoXKv0Q6r+aifTGPgCND4n2tOEt400saqymWHRMdjQW4glETe/gcLgAxUqIUEldS3K1Def1T96Wrjk/ntVuAXvUkLPfdiIOszxpRbKA0P975ZT7XnfG1npR1XR9P6VDHVcmDUObXnL8+LPYZiLPcyRVTLTC0j986mMAltehhPCKfFCuELLdbRokNLMach9j1akj0zbtB96ZW0fsG7TZvQa+WeXwXZbTh6vh6uUxODhvobPHh+dUQ7f+Vw7soP8v/Gu5+/Fhk62FNm8UlZDbV7gTYYHy44DbYaNQON399yMtZuE7qk1TVKCyv0bq/2bt/wTyJ6my8o6QP4zOd3JmIkVekigkda9oVP5kOqJc+03xaBC8u4IPERFaTMB6fz1jLxPY69x5qc0+8B+pj8bwUMTSHMWzxDEJjhs6eVWdnuimIf22d1WmoZI7C7z5RtPJ2LuDt7WztFLAesvaU2Kf8KMP1NuHD4kTzNNq2hJZYF/XvqW5RaW66xvvnPdOGl8+XTOGODOG3pbMdWjdf2o7ga956fCCheWWmczs53VzFR0qOTJvszyksx+IqCM3MNqjkTuL31s5xH7mkOY1boKvQNQFK/5V/myhdbyPgoqR8tp9WEpoZg1f2RBoikl5D8mWMUBLndMUxatJsWRrP1eEubBzwQ1RcvwEwxb8A+x6Jh0uXONPSpZ5OC9NIapuIjT8v5EVTBIJ2xgwWWr92yjApsnX5zb8TJNjxby3D4BWMipPjQ46rGpe/vdmiCTruTGvPx9BWw26WG0/Fd6hhLX7s7Z0J98N8O2rMN3iCPUKK2FrQy8AfADK/d771ZFmvjtdndK/fpIFhPgOBOIBUHVCq59cbPjylvXQM9c3A9OPBVcLqnxhgKEMQ/YbvTsR7wJDjHboE5cWI248cOO7UaJR8cqTDmcNctsXs5TkA4JsvG20LScLWh+owNSqD8Yh97ZdcKFRegF+btGyWVDd4yJ8pfPEppESgmP5D7yulYp+JgVHf5gBeLrkFxKjg7wSmDyAb2DIC+ZYQxFduU0TV/f1WoH0MwBzLVBEUIewA7TjIY6I/nSgzoEBvQWDYuudos9HfHw+HyvUb1WxlY+OaGG2CtM1+DZb8zAjI1VtVI744Ka6yoZ36Sf8megVWQiM2q2qF/jtqRixnena+WmU0luMXpc601M2Z7dRPBKET4Gt/Rz8fHONkFEvEVRbWUTSUkQxDCIEWYGMGbSx8f4S/rTGEEKcDVf1UCFKOVkOKAhOgUg6ykJXQur6vkC3XYaEZSyo2+HTlFMuX7ch7l+0/3qA+COlbeRyclQ6F9Ey7DslyQpEsFhyHN+ZW0lOWkYUEjngCGE6yr/3nUK+6932ORpWyt4G3eLT3/woRit8UHnRs7hAu35tqydmEiSnDsT7WENNPaUlN5JaGVOW03hy3e/6WO7jQ5bjAHKx3IyYaUEeXKAsqVyuMc49ZPF90dxMTf7h12jbeAowVxUuupe7uuxRP4rhyYYvfQ8069cuTuum7ya8FhTnxLnM1ygqrt5jr8fzixR/g2qEFLjQ2cN5grBIW7I+69HDh8oxyKbrMvoMi7wIeznJEff5lf+uEAyV0AlIX68DDFkoS0wOTFFtkTr76IGdVMiX4IY8Vivv/6kHweWCdfbun80N2TwYD0j78faq1RybcG+sy8WY920XH8aZtsNCrCOFEYtwu4wUMPh4C1nybz3ZkxE6BOnm5+86jUUW3LMZHTT8sqdoJQHiT7ZXU9QiOhFXMHhGx+GPV0n48uJSagSwhbASlaJ5PKILb2CnWda0HjWCf69hKaweN0KIFElIw18Ga8r7K229RhFR73b96SqFPqWx+Wjz2bBtFTD6EXJzkJQGXMySF8KEBIbuXJcv/xJl8uMh3SDahLDSIyGYZAHVgqRyne6K8Pgq3Wx6rfS6E8CzSP5NQ8FJyS4OnrPF/wfEkIzQlkyl/+rRYd1Jh3J6lSgA98ucsTeJ0ig6UUQm4dMX76C+1yvKL/zqmqDzoHW6wVWjNT93SjC+XV15UJTYKCBxKzvOUQqvnJS+a+of7G2Vm01Ds8POQ/N9hV1buK3m4KRtPqhOXbPpGVMqxjiMIH6FfeA6azBIcvw5CDmTBdfWy/6mWdhFSnhZzDSeZqEAuoNNKLzx0ihyZJ/FCgMO4EpZyTaQQi6hAZulqy/FETJ66XXdRmSj7FkwidcC/5pLQE8AP0O7rlR09uzn/02jaC0uDnrFrViA6h6v3y8oMgt+tZi7Bj2EZXlMRSsmr0CgN71db2Wma3IX/X1dzX0B//SPpaxzzZloZmbFnjbGFY3OFI+q4sXJUBj61FqurNorTn+nDdubzIlZ0GWEq/Y4Y6Xf/563kj2W8lAk78dMaPTyLdeDJXmw6+4AohFJVTxBBkMMlEG1gW+aJnUBXh0a4+BtnOvWJWjev1vkWxuguacFeWLkTQTerOhYkvfgRE5v84JULxuocEkgmDAhoxdNHQaZ5Gc1ixkUWZb5ho50uZP15en46gAGuqrlCn1Y7rrwIc7nZ/rui33nPgx8NU8r839mtMtBzMh78G0KWGNeMUSL0oO+a5Wu8A4bp1tM8+7r4uPvbB/2G/kTnERKi9kmX0UBE9VpwjMtz2o/NF25TkUpmKWy1vFMW+/Lep0RqXtaSk+hbfcw9gO5a/SDI4rg6ynap87p0QwPePe+sAqzKrLvoCuUvuQxm6yQ1UEEtSjz2YcwAVVimnwFA/9kYjHkZaXbYS9aubom1DttdEzLW/62IrlF930Q1vxqTxGOHUbZlUHP6sXfnG8dKfN1KxTvPVv2vNq4hmhzyEgF6+kdozQeg5/IYH15a7SYmAQXIUc8rmr/6PQAvuHPJmHXPruzvYBSf0vbstsrHYfSdBI42K8+HnvbdXQxaQ0XflqFeBYSINQ7Ptu7G9UzXwDAssHJFsXinE/cx6/HQechEzOuqWdhUV5mmtUn4hs765up4KBmHicmO8SOxcx4ONtz0R/k4rPWE4Iqaoxtk88YLp7iK12fR7pI52ht9R+ogS0cxpfYcs0zew6XZaZcxeDVyqWW0N+vi55k4aG/b474hsBJ58mHBhBe0vlfNxhRCmssQJ+nQtbXjLaNz/IQAAci6gxpB/1/6nfCjHvmFgDTQGjYudDkUZCpZrvRTzEei0awNXeqCKm17AZ06PxCBEMbIFX7NUh+RV1cQxfvVJnuMMwqzeqAfvQXGl8uY/vkzml11FE8NxYop3SVHn+VO4EyXp7oxomen2q81uOVccbcEij5q4YLJ+VAqvtnez0OcP04CcMprbOAbgPamOwhoTRmdLb7j6tI08qqC7M/DucZEapblb15D8sDmoQ5k0VchoRsqyM2DJLSHGNgKhvGmTg9jLBjgB3H2CrEOvtKpfzdSke2Ev98zuo1PxyrU0WQGPP+AkRRsTHkSqfP3JqlbgdFZtHqb8FxhdbLO9Ro85hsNo0sw3neYIx35A6DDPhdblg9aMhNWJ2ikkExLVf1T7onGLjZ4ZWFtJqLqT0HkRJ6HM74BB2rZQ1RKoomBPBkvdKo3IcQU79ePDW0eY/r5gc4aY6ysqzsbDBcT9u1sIUcPeZsKeBXXlJ/q3axcc6viIhTnc6dqmzfPk9P2/Iols5kWFIFFcEWsoUMZk6vdqcbQc/IzM1E/+uFumI7kP6UoMCC/O1h9kTbCjw0vfZwPpHPQzez6d+3nfwd7v6sOhPN685lZxaK2DW2DSNHtxR74xWkNYa8WurYrNNlobyoxbwG0+A7ObpDTepgetohpa9p5xc5Hv7oM7VtVTFCaMkd9+6HfoagaPAg6pYicUQzcKYB0x0UH9tDugcgXezSYYcZ6DzQp6BY4gcypxzanWGrFs0I9kZpVaE5DMErHm+KvhOYlU6FmqesskIs7b+NbBkrfu+EOCq2BXiTGw7Q1RcYbFXL6WJ3t3oYrXZKAC6IGCN8WfcnQBcWtkppgl37PUVOa7Vxqd3WcQ0zq7U+YPX1ggUoYdwpyHT8dZW4RQOhq+w7Vd/TM0zSUa9AZVgCu+zsuYbdlJcwSaATVs3rlEvGGzzC+FHuUTvVPO4Ta2MxechWouFurACTguq3p5oaZuOFYZBQSp0xn/dUm/kJDUCAZQhonRurglrkI5/e0uxWs67Nfrc2b8vqcAYLHOUvXk1GRco8BUmd4+vX1MkjRY9QsiQGXFb/CnLBXPl822yV4zD3mGid6k6RMcyd0rpSB9R4Iqzy/sFpaBKTbAwhjzShZbEOOndapnI/9wXFpvzGcQivBqNuF+bu/c4MO3vDFQhNe7p59dsbv6mhUk8XFBtdFtOZxuj9knb/yiRMMWlenLf7SV24YWerSmwaLyqtAHJNQ1xNIbu44XlZuQSX5XUUC2mbE3PD+c5MLwX/9zQSgk0bSHzq8rY5DgdcAo+vnwyOsX+DyfeKEV7PRAqk6MhmZ1bSCZ2P7S7zTC3RNvrgYvSgILlsc8QCs00qnFciHGhDYjfazE/SOPNUd+HWImJt6tWceczRzqptHvjMKEJqK1cE7ldqqBnJkYGAZ8njiWi18ZLDwxwdxO6Lcmpvu53K21X7d5ts+xVtv3o8ZLT87KBk06ykOetcOvJwXW30iPwh/xEs67Iztvra4BALoU/jq/71rffNKMPsKqL0rN9nhkuaUABDa6es0hS38IwFJ8cbMDIerVD+s2qU0FeO5Ep3jIrhvyEwuAsv/ZIEznCL8rnpiEVEs+y4ByqisuW2kH4rfauZDQ9B3p0iOOIgvz09C1ZUq4H3NfyD8DASt8ek+ejVduKSeY++gGq97fbNASQBPLMC9YgXEMv1s6XtbOxYbFHqiuQQEKmXfY7sJSvkoItTIEDx3bj1Y6KLD4XOWbJy0RVgAo9zeaO4ptyHndc5Lfw4VSPUBPoyR0Kn0U1+YkOLe4vswYY1hUtWys1U7L6d9RW4PdqUKA+Da2L42V0cp/0eO+5rFA2SiG8GopbPRU/ngkRsioNqDozH20e3v1WfP/sVRV0aCZeW5igxU+PLrFWTD5zQDAoo7iXg1ST2QcSs/4bHTNeGNSG9XMDRwVR+mvB7my5LMbcnG1tB4F/4XLzy991RRD6tzuVmJAz6JJz7L5qWgV0/FNT2tVcoyWEzXrfoBfi5a7aGs7hxoprTNN1VYGucLZ62mdWHVx2cUY9/3FWXkPTNRR0Hl2dmxraxrwcjDQGdWrWmEa3WztlzYNEEIU64KZWoGMeVEl4ThMJmynxJ0Xf7r+N0xxnoXt+MJfDB5eHM/v1YLiX5v8CMxqxolqdvFoQ0bCArVGc8CMMVVUk8+jyu1WwU/2y4zv85eAHnKICBsShePqIXOiTVMLTuEokrCE0H9EjoEgPFNrCw7ICOQosmjl63tRfXp3OdoHs2aVm/Ne2NewiwhPieBUZjSDEv2bA/zJXQt2A2Ab1nfonJEltxTpIFmiRbMu8GDvCzywDhz8P3eVAOLg+gdRB6ODtUha1hg5Y40VKNSKQQ282J1FpfjR+bokb4GQmdrbQCFQuaRUvZzOLc7tfNji0iyG8j7l25zUiVqQaXGY+uZGm09ld5DcdhJrc41DI9hP422W7SNmjilYKGKkS6qYsLVZ8uPv2M9wsVnsN8nXLrgjgfx1zNyl7DSqAjzDsn2f0GVG0jgDKjVNEu1fscoKp9KV8nRYCHssP5RbB2oAQHss9tS5aZ7sypfdY8e5nnlKMTbrQMWzc3dPLXN0IhVvOm4j25mdxjpdkG4hc7bMznUjQUzv9vB2nqWDHPih7y+EgGMVcjjMU6BXgl269NjRCyg0wGS7kQU4GOVoIWgIHsZK/RB3RN4cs85CSa8k+8sWJzNTMfV7UNuZVYS6xISSLTnTL/uhDKAv8dC8VXrAmcTHRAhmHSn9YwIJnGzq2AKvrg2oUj5vSNWiJb3XllX1KyIkQNaFMA4TlROWEBpFNitdo/JdYTKvUX4jBr9Y6j9CEroe8rk7bqTqEeVLuMWKAwA7rKQwvAEfL4AmME54/TBYZdOoudiXRvp0h37tvVL/DlziKDU2Rf2hKrqCak2Tdx6pyzp8w2efUEsr3mPhRlSoi1mBnF4N/AVHqOLUnblsyaDK6oWi3mr+JCy/hBkx7q6f71m++R51hrmxMWEP4B0QRCJ3EwnnmL1L7eFgfnKz2w1jZOxziL92n4kAV/8M4HLOGmvWOB76ulkaqpg0DrUDrQ2sUBKqCj2Ydi9BxLGB5Lig9PzpFLtdAJ6wQ1noBZhyjEVrsCciEEv3DYyiEq68+Co2MbyOiJLRJjEYMRVPhmrtNg7xbXpyhgnd2UuZ56YWcelt+nwYxNjx9M42UMukmPNZAnbm6bjeedONjKe0ZR15iMtF8ddcvk+pZH4R7mxTjAeJPH6rVoc39spk0jNxma+ebW1mjS46y6gwYxH9Zjcu+4BgDu4xVZlXr067SkZ8/TwVHdsIm8wtaZocafOGGdfLNrH2nvS/lavlZEXj69FAyulgCrOzEGsY1D+uE7ODk6JXAjYGm385nUJovV8NgN9ZJ7M+f44UNVrotPRWeu+vObM/z4Ud2qJ0VX5l3HprvCIkuqk5YbdwFe7vfnmjkyXQWDDjbvODIIiDdgqvDoSVGni7hlhoVBvo0D6mSjUYdhm/Bqsov1qZ93EC+Kxisp9z1OebPLglTlFAJXWOWg73QuTy3y4sdZyFxZpPsLrxE+iP6wLyIEXp5JmDf1tLktDP+3ULEQVskMbDA3DRZgoX5nSfnsZa1YwDRwugsLphNoLofN0MKLKElYP9ZDDny7/sql8QnpoeJozuQv6aL6PfcCbV1hXTdlbTn0mEK0gzs9lXFRApMyrnoDYTwrBVdXLPjH4HuUeLVt8w/6knC8gZvvasbN8ANdguVII3SWmtVvadjy7lwc3CbrAAsluDIr2lB5gtDIwmQcNFtT5viuUdNMy4qq8dKMJr1MweBCP0KMkrWEI4Wz4zVZMPz/pzGzuDvMXe8xwOLFzGXyvOmU5H1nzo0kSt8/D+G/gFxQNDQTGLG8B1ZP6pxSE6cZG0bk5inyIG1FIcgs5hMXaHI1sQf7Crw6v23w/kxzSeMBvD3AgAiZ+h9CEIppROtuecSXDyqBxkf9ut27xhMX29g758CcTNLZl/1Qnpf57jbhV1dwsEMqJkgL28SxaaV4FMxo/VPA96Ak7iiam8hR/h77E55fEtt/AlGtFDrPZnnyJ/yIVGoH771x3hTckLHf/LtGfgu7aXCleMONbf2WR9IOZE9HPjFNK+H3PgOBUuOCgwMqQUty6a00K36v5K8hUp2iPZAu9nCw/hLZHjOMTK1ZVVK8Vymxt4khJ7/CzRioBwaP8TW7DVq1WEiRk9xuhtZIASgrYwaPV6oimLxUZMb4r0+Qje7G6wj4M92jp0vB+qrVqy4q/Eos6bJv0UD0DzJ0MJbtHYwoGhc9hqcS53c1SpmT1E3C2KfRFi3evm9hbxUX+Nmb583rGaccTNoRad1e8/62ucG6uMgfISxR/SZXOEvLXNVnFbEIc+9A8IAsX0E5v1bYFIMWRH9QKMKeQTRYeh214cq+JIwqEh+N7FMwai21QYNjTiJZQl0cDXLlbRZ7A4efhnSTBylSuMrFq8hDFAOfJ2nMoB5yJ2g9CqWZSD2QhQRQgVOWtv1aXpTJLkhVta4U5/o4i9+OQ0dLwygjC61npu6hXulrhdFe2s9y93EfqgQ/0bNlkFr7A9Pixbfq2BzBGyd6pqMUwI6swN42AHEJ99EkcW5iF7kJFjfAy5Jr5DeeFtzP/EEmWsUDwGOoNXvAuK2QAyCXEL5Kq5tL72rA8aav4nL9xEIBNrPy2p40wLTl9j6SkkliecAwHGOH/1qcsk6zL28zLDL4ePdSt2RW3Vezqp58+GpMMSJ8B7k1hDcerzEPmMkn4iA+j4mg28nBbzFxAKo5vvwKOhGjmz4caulKU46LNaDBPiaDkF6ltQRNh31NHQD5X4SE6Xo69gDtGz53vKxeV68J7fLq+aTtR38Hx2h/toKkaZWyuPnMT9by0rS097ddd2j6ObO5hH+7CbKFhYLmYYpDhFMy1YbHNEk9ia4onlNzK3CLHuULM55ogWTzz1AEAuUGx/onqHMygkyGP44Lt5oi7c6BdEo8Ut3BytamZ4f52IZyPqMLc5VpsG7Z8AooDiS8I28LDS+T7Ncw5NGK7AqHPK9L7RKUDd3sxWVYWIqUlWv1OGerqopwpx4zZZrxiXJth+MJhu4Q4tDdtCL0u4xyVtbywAFXUvxgQYN0KKYgw+WIa0BDzh/v1nb5+4NokLcRVFxtDqGLDdZMBQGEMQ+WM8yiR9dxfnFy9T1jCb3IyZZsjXRUsfpB+pr8g+gXMfdtyoYWW4TrWtKw2+Xsr5A4oK2LvB1avz3BMd90YpPtlI4XUzpAq/gItxZdWc4BX0GCoDSupa2UO0tXChP1d91wGVPjGE8tCzQUhsQ1lvsWWs7ubx+xLL3x9jtGZ4SrBm5QOPvanI5kHF9TDebton8It+bb9/+SNpHUZC9WEn5ueyOvZyHAniKL8Pcy+b+BsoLwny5zTJPFlduhec6G3nhylhUZ1FeIjOtaSf0VJRqrZgka/+gZHk0uqTYeGyBq+lyDut3GKX+35iFiGbHF3NVtwSfjJsJn5dkvBBKQQHLAHFf00k8tcDOMejTSBCR5re0/iBYenn9mbsBY3VpeEXKdTGhK08sRabkiCPdXNj7RCuUAJr+B6IEB3LOHADF7WXIw6KiVIZBR8cSSKzAJiwkFiaJP7MxDrw/ZZxRcStwuU9bDu+ShXCFyAnUTXSyjEyXG+Bmox98AXeEo5949tH2bq8bg2Ug152DjNrkuX+TK751aaFjaSLE6MwuqePcplQmOBmVbsmGmjLhVu0q+UkKHMz+tHAvN3XyN9s0vJjTrm5HWbRYK1svLAeNh2i5Ge403iH++cCLjz39ZN7vGtrC53qmG++pTd0m5sslrL543gjzy+tAoVpOApL0DOYLfL6jgTpXtf8MdkMGoStieZ5cSmsOTT5TaqwKx+8o5rREAdgbs6d+m4WQLDwGuLcRIZWpwsKW2pWctyvqmi73gJs1OJTfG2EJIRo05qAamnnzu46UgzrjrH3U9mXctZws51kpaliKfeTsNXs06fkKUTllr86Q3re0ctzLheZR39vLWS2FRhoXWzxUmerq1sCuLZgsr3FiSEtPp1176FgJ5ygVsr5npYUSuygUyH0wv7AynKK3OQ8mSGDe+g+4Pb3d/ZAQpACVybet6lbubInvN4MUB7GmLWy8VeRHu2O2RVWhVx/xJpuvNJWS4EyewBpQl/lBJVzDgBks3YMR47YiDOp8YVg7R30wbgif0Yldwd/HQwE5I2DFeEoPd16UcQ2IyNO4+0AITvQfDw/JQ8emQ/x3WzltNMPwK/y0viQoQQhl1auhbU0omVt/Yb4EFBqcQkgui4OYD8rftPlzskc5QlmD5F6Zx3bL8cJlUfHwDkU0RubpqMn/bGusAq4T2nPXgeyAlX+63RGrdxIQ2+VwgF6horKn8SK+eyIhocULLv4izW4QBdy5KC4h3g6ItUdyDnyoFQ2vlKltnUym/WuSle8CibaKvw1yjfL4iCKxtOMms79dy7WjEGEBG+RkUVZN35v+u9XHpulntd10tvYZvxjCwz/sKRd5OsBK185Fe3RVIy0umOp1FnpQMZrr4iiYmPBk8W28XjJe7ZQ4jc+TKiv3DYtA74X+JU/a7EBW15v7KZjFH4MRq+im/NrpLzwQll3E7ZIQIT6mu4Fr3+Uq/gCkKWZfpGRA+EOu7B9bunutYTKdPHlgw1jID8FkfFeeRd3eQALMTkn1S0CNVWcRUCgi3sBJsqirRNrdVwn7t1nQICRaO5ZkSh3I3VJ9WII2CboGZV8pDkznZdldmrDPe+gGMtSOFtmaPHT4Nq2UGxJXpxON93yS0AeCW+mGwL3eRW5dlXkZo8i45FCoBrSq5cyY4GMtes30nvU2itnPJ7n1it7JV0KDVjs1H5yzXFmag+SGsK3q+hDKZGgztM17CM7pl3OOXHByP7au/cpVTtVcOJfbvD0WV7dHeaL0RzNDLSFBTzeQSiT13SrHYK549BQCOstmJdqVMB+Q3QgHA7OklggB6wFBvbr8MBStZTHwJgBFx/K3zYpqT0vdUe7/mJPUrw2mukHvwsyK+gmLG48I8+wecNztInbFL/PwFJoV9QKQJaBpEwxn+qHRykPiDSmDPRd73m/twsppTqp4F2JagZCCdUEmVdJPcYeVciywDW4dGeMgM68yjxsh3MpQ28bVPvW1M8tgEGg7wOwKRAMb4kKMko/Y2Zmt+S32L306tQ3hRO7u+IRcjM24ENZv0QfSZ7CnDygl83sAoz6ClnXqha+7JGMHogmJLt8vuF1cKZOYVO9E/Orr+Lf9NxbjbXs1jOQPfsuLr+Don/dahe3uZVgt7eOixnvBVC0kzjtCEkpTslEvZfm+ECPFqk8bxsSLkvHe2A+p1b9rqyR/E42SwB4TjGIcOljY6UOG6yzABIMrBSEiZYrsb13Fxh+41aAWZKzXwirmBIw+nHHO3025awVUxF5IaV8gMu2ZtoA4vVnk93MlfJpSv+yL2jZxqH6jwpysCtqCoP5OVEJ7/ev+qYgZfrJ2af/RVk1RQTplpK9Zvm8zn08LYRYvXm9KwcKVYl/DwDwuPAuPM47md0tO4r/BtqtA5yhunOaaLogGYHLrWIB3iYX2xteuKTLstorspcXafHBzhpsOvLEjaUq7ZVS6zJaOxCZVPgTyoqAPIJ/n0w52gdI9QWPN239OAYZzcW8ZVacVZloRekSh7O6LaekDffioyQ+jSbcuycuwCK9gnBxco8atU1IlHZ2n6JOZ/cCE3eHxJapzPFWkgycEaPQETR/tMOK7E7PPzjnBquuswNDU5WB/uzLLKnJcevWmQ70kmluERUQ4I3au3tB4LwVYAdB7ncgc4AeNdmWyKDE0AodFyBR2uNwH05vrexeTLGzBlaIThIz0jHvMnC5eZ3iRmLwBD6a2etrL9s8LQ01G4NN8lO1f2kmfQoj1RgvmFH09Q3m1MC47BstegvtOgipIFx6jo/HqtV7k2fhPo+aTjjyalFlSPB8hHHSbwXwy1yWcNTqwIEAqD160s9iDdjtYJm0E10a+5xo+DfEl45ESffc6RfGFjGbe2pzuLWSPeVwsufkHsa/PQmU3I4dndt+tMKERrwlHB5IcCfNmdhiLpk1b/5Xy6UjAgjjGXeTEoewxFJnDGtMMQhD9NSVJ5z8/d6kk/fe49pKw13I90qUflZfK5vSKqZP3XNyRp/z3GUEyDyaOH93MOiMd7MZvb8wo/dvFiQQs6ew3opyGU05J+OczbdZuXdBN2TsJKjkmdS9CmP2RD4SxSXxipVbDdWxUqIeqBg8U6Rp5d4RWDEru3PwXWI2AcVSXXHj1r4/51A4zmCYj9giBhHnjxEDw4JGbZxKdc81RRRjaEe+FdNobsx4VTlq5WQ8kilElcYWHzILIFilPstYESfL+y9ftcjIN+9XHpKidDtnRBYJEo4JIlm9D3bAaQwtCazESgSxgfmHI34QVbGVERZ0oOl7ttguFGFNroDRXpJT8vjAjJLzHtAnfpdwRYR4k7GVrV6y4LWrGu9zKFOOIWQPP/vAaryY8emLwejWtDMnD++o3oxh9oy8RlSzzmmd2D3CClfbiiVY12LAg5XVtFsORIaW6c6NtjFYofgFUuQ4heaUIa8/Bag4hNcrq8VJeoXk99/5q04hjjq61TKoI/N4SSWAOEMiHYfhoA+w4DtsoX6vnLRMqVXxGUgE6LctENfCEzeUgxUjV+9SM9p246jBA3DIav+hEm/Oxi31rHMd139caeFmtICCvT6eAZ2SU+NofyN+XXqbUaQ7+19S+o52vy8Vv5Dbq7Z/5d9t0egnMhhjfKXhwnMdeivbdiI4ffYPAk1YyPsXEHTcOV0BNUv9Nm6qmcrrNDJsjwjoqNuFMWmvDD04WeuuuC68fcNLI0+1hwk5uSoXOANdsyhBcHtO5IfI91pzxIoDsCZlKEEq6Dip+Qx30bs93X4eeIG/4fxgrvbmFh78QRSMqeXB+Ew6qkH/MDO5iroUtZd1sP/1aTN92TTmsa69I7itjLMiRfwebMJkFrYczI+6OMtpe3SaWIrIKyHQ0KwVskAsPnyIaHVBfcSBl2mTCWNda+V5n06kH5mLQSvOOtv4EbnNFc5qW1eqNyIomvmT7W97eshhoaO0cI1tzD3RX19NdJXmU9h3nMCzL+F4d5eMNtXMmhmDIiHF+xrgiG5/NJLaL5eXENrZCAF+VeHrj8nqgFKGacXVgVouFKZ0NcHJ3LaLWTkc1BJZ8y5RdvzSP2RtT/S9WsFfkVd1Uhc5FJk/XgroAMWQsKxxlpBWhfdTyVcyS3lEVL4L42mrvBvjiybuZQa/jTvNRF59vcc29wFPNJ++Ql2NqoekML57LceXmGWRpVZqPcb+2bPN+TUe8dUZmpaPXmFHRNMakd1zcO5dA7ZpCPM+1uX1bf91baInmqIJ2umkcv9DOXc1NRFypDu+VGwAnY9qzWtifsUBqyt1yqy7iRPZPZuXEEsAXV+wul+u7oZ1jhM5NgpQzixdcU9sqVK9k4VlzLiJ14ZuJlIe+qa0SsVZ9wMMoqTKYzSoGo+91n9rRIGtItJ+p08A4uqmhe0yAga4BPYEWuhV0Ghd5XJjbhSewtjRNPsoRCoOs0ste/T8U06xHn1VV6RtqmqTV8TCG91PPqi2rWs3KBOmnI1hKqm9WjyFA3S1uCSc7ZvoEMYhbEi1YH3qfVKGsrTPDUpNeKUkTNkwN1BSSeOtWX36hbg+3O0kES7gYpTEOFC0/YHbEYgRy4KfT8Y4XcmIbSTAoN3o5GBbZXYclJiKk8aMilmOEkxufDYnGss2wg6dMIvlhoK07qFutYYU0iytABhGZftZkJMZ4Zz8RgH65lq84bvUFAgboEKpd4JxnNLd6kVddnIeiMnbEYCkRGeTrBOkvGZyR2j3YzT1SPGMUFOTZL/0mkcvXNXRTi6z6tqFyhEoEDCw0zwWIxbQPM0ouw3ufwhunEF/yU0sequqxFkXq9lEi0bWK4JSitY7sti3CKjFV47Ij1Wo+sIk4Z3ZU+7NfGNoqQz+skTZUGq6bgTLMAR+yf6Eh9BWBKrw8ZQY9R5mTSk/JBtAQQ0nwJ+MbdZiSLvN69hsz7nnwqroOuAg9r+HnaUgd7DWadJebQQ1pOfPCgk1VYpplxhrfZls1I4iUr5Lmo/2HFYutLogVrbA36dlBkFic9WsIU/qNVtb7VM9obB4CB3TUZjRwy34IbLtcRMFKnSZaFxfcGV0qlg+cQ9fKj6Sx3zIY2N/B6eUVWqiYUkaw+E7StY09seQiT4+jJYMsXbW4/w0UK2G3QkIaM/RR7v/AA4TqrBAYs4Xhn6vXVwmH3e1LvQaTnCXzWe3vU9Vyfi5QTW6LJLy441iJiEoJ81z9fXp/hKK//ln3439ZnWi9aUKfOjYz0SMFHC5u+RdFJzSHG/Kn703xyAMG6IQB5sDP70zoi0XAMCpGnjkVqY4bX+cVQzw0d0sOD1dP2J1uJRBKPhDBiWyJU4cJDkqBWszlYiISJBofHkkYNP9noDqKmQYQ512nuFIF0KxC3VGWSHWGXRPjtq4ZeYLHVShkvTqKqFKdZihXc6z3fFSrhteTcuEP8fba2ykfUHGGN09AvAZcX4drMAwenXCVaXew6JSlzzWwjcwk2xgk1o+/w8nDYcwNW4Dwd5sPDShPw2wMBP3+ryNqIkVkSs2Zwi+FqrnqqEDeDamZCfZ1l7bNR9ND3fNyxTQlhViGKr4GiVs5duUDGSL+Hyq5BSJauZ7Ue5ITJWmehTKcgxvZRTaetej/uKdNR55Vpe/ExME4AXHaB+9kQnpTZa4dEDO61/WkPKbWwVad1Bow4JMku4XQj2lUOO00j6WgkS3vlrB+yGPtm7nMsEukClHzDSQCaoGoVRO3TYNjriThHP1t1GwpWO9GZAeZLv8t189+WzNXCdp4c/Pkgc7YxzFV6RJ0AOIUZxavX0ZD0Y4prgFIF71oO/LSoljaNxtKF6zAu0+sJspAwJXNzoY3usvoGp2tFUpsKjdUEQ4KT/TOXZsUe2RI3Pz4V4ddawHZ6sgU7E59d11ob3XOBNuwa4H4lBfDD9qQQjyat25tl7a2faLE99wObFVvmfyRjQktL/vcjhvpXwyH+ghT6lq89Kw5Q0CYIUGNVhY3TPAIFRHKdep2E1I2txVOt9UhC5mGKPsXYO5WG9SZ8jPftlYY3L6/cWAxggfNvgmjvXV9g4=
