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

3C3dozDUiXa16HuVcMZ43+L9f3dF6OnqHfjkY0yxNxklV04fef/2JMXHJOUQm2uhwhyrcHQNIyQ8YHG2glZYWAD5mnokfDba0Sew17O1/GfL7KwOeoYxhypTJhYGcstaPKfNRRGd8e90cOP9Hh6DBB8p1EIm7gZd9nclxhPRR5tq8/G4wYfd3DeJ7dP2W8/Alga4H4Df5YVFQ5tkxdxgjhSG0/7v8HSfRdx70i6STnmN9+mfLFR1Z9gsOxiAjLalz3G6/wnUgg4ADWSDzsueV1dZMetn2iyh4odCOthz8zf+sozT9npyYc1Xdh5MgPYHEmyG2vgzFaF4J8ENAMGXNZIBiGp7ZAEu2vsQht1nwiOVOLn9aZH+Qs8IzIAILgj5Z+72LA64RzB5lOKby7pQxBBf/7rYT/BD27kIvmQteoGykgscCT0Y8jGn/FBD0GsIa5b2Zqih9BxSdkqtvo3PyGdCkX5AmjnweMy8MoekZ0iNMjSU4I8R4ig8RWOAbZJBVhNWaZUS18ox/3VyoSOh4OhSgmjjNFUm1eZY9RPawEhrZhlnPXjJhPcefIZnu4tyquVtwAs5vgDHhlQsFkgRyBBlugv5a7yZu0qzNj4HqS4KoUTnOxhJN7AxyXkkjBEAWpCe2J6oE1OUL6FD1y3V1sn3bOgRaS6pv+2x52wn/VAp8SdZPAAqadVYz1I2LM8Pnw9Io73t6mYG0UoukiLMdeuTKdy1sdtWUbyoOUYIlyiH1vv04fEeZjR+ulEl32+yphefRGq/8BP6Bvmk48Isf/9F3Pc6Q8d00uqdESBoqIehgWDBrGpIrfoyhIc0w1iBSl63pNZAbdfx5OVJoL5U91FfcgtJqx/t7LndWnJdRbFxccWiSVix+BYeU7enuCOcjiO1OsFJO/mudWcftwsz1j4bCuyAJrQgQflIZL/NqfUlwOq19n4zhViSt5mgMzrQPcBUaQxyVkEk2mKX/Q+uC3qw1/QboXEunCzec1Ycg2/hjPh9u10vgyNewbZTpFwJqbrlO/QR9rrip1KXcRaOXpF3KjX633N/MFU+5qBKgXZ1PLQhHlvF1CRMErRxuZBeKfnkbuemOLRzYlvWXxmYXeWpFxXhut9md2OxYJ8l823lfIqFV78re6L5RFjLtoXp90ZMPrgF2Wv6+kO27sOPiFp3UOp8wD41xEXZAynSMcj2c0a92t9WY0DvlH9/oe0mUibz/L3RYnVJmsOsUG6QOLU14wxr1TSujy4a1flTSX7twCF109aATeiyXNNgmjjjNWFL22ZQpAoRxCqoSbD38fmn/WUnzLmkttlOAoMvPKftXOUZ0EZ3tomK1XJOPQJp5/jQ93m4U5bdV5KqUINZzone09NSzjjNBpbTi8xN3Xt0FmRMTWDnQlgjSIImNzCiFdvlWkt+U3+xM0NJavjGMB+TZDAsgF95V+DLRIGsWnZpxX2nYjP63OpJHZ9RRSpCrtyOtqWJWY+SJImMhUKZVfYB2LDsfeGvnTYpePadM7hrJFZJOoNq93RPTXAj4qP1UHTX+pPFOVxDjC4U/FJygnbDk7yFrFYEGq3QBKuv5vELjezxNGzCLkJdfX1DzHX9lszk/ei2ayORtKeHxUQYvT+u0gsZYNviyxxwBo+N/wHOcKI1rxgnabE9OzWY9abv/mnIcprHNCpGe6uwfb7CWvqKoRCwo3Gfsxx0yL5XP9GUFhHSxOwfICeI0jA9wYcAWfw3etJY4YJkykdVef5IJ2ZbJ17nzjEOuJ4okd5d2msKZrsBKVMLBPS8OiBcG10++RGkbtwpQJFPIP85BW61L4y7B9UTmRgvc9WKXW3A0SkCJl00vcJbYGUEo6LZYJl1AW3OSqJnp6NhbKoGSLyPc+txNh3CVLgKfK3W6yptcN0r95yEwjLzcHmRDHZvtMBqi59kV+jZPTL8YkjcRcEjr42cw/eO57n0vkv+VOFMthu/BSy1FmzYXmtzMaAFg35fAmZYmkSTGO5OhwT78qVtohG1h0S9A4GbkovB3WsW/aguucMp8Zk9bMMpp3Fcu/7jcG/SBvJsPzb2I3MDo6CTjOBuDHtPH89lzVm3tYZ0dMRNYJOdiTyDmfx6J2SiR1QCEieOpoFJNQAJHdEXSSupmJBGZotwT3Ko1x1BvYuF6ccOx2gj70PvbLuIuhn9gvygF3AJlceB6GDNJUk66kqiNhaHFWz2cG0wL/+e1tbuzs1ISFiCxm8JkTCgfnwWRt6xgYsjOBuvxjMCY+4g+gOI/gOQM5lhFU2KrZhbS36W+PTZdGXIs7I2kSxwAkuapgG8g5gub8o/+LogHAVsLEavXUGLmKw4jYsLETrSWACVS/zXQ8H0QW/IiXgyQkM8S+YKuOyK1DwWeF+YmGr3+O6lEehPRnF6BBRUY5USAK3UIbAArTmMb3f4fpHbNhzXG0b4lB08OOPtU4oL5GnhNc4fQRYemNRBFfuqczQ2AKSPQfLolA75DGuYWML3zBIXwQ1yxAlLHuv9kyL5K46HpJX925FE81POwQjM8sHK8klt6wsAVkJ9tWXxsbhqx25E7AZf4ZjafZwBDkOvtEI3JmeXb19uyHbUaBi9u3issHn4Elz40DJFbYWNW10qmcXNJEwW0X5mNyiq4atr5fy0LY33GK7xrkJeb9uI87nmjno5YlhdcJcso3cmxiFGxR8pAnQpwk3e8Rn1q10tLQQh1SxKX3fZvxsMquWSL6NLdhhTsuevGUBcBaGlVlc7T0I9+4MyUBNAGShXC6LUZxw3t4eVSrQLusXDunuNblW991bnHtTI6cV78iCCWHW5+YiPaPK/q+m7FepN1qd0lvJpuuxP6iroRzakLkcx0MsBG7gOK2oSwAbGbBzFSEbtAjQ0jKQ5K2Rphx4yUB28JRvc98QrtNBB+63nSCGqun3RbqVpU/7V/sRd4b3s6AKP85b/oyabj3tAG4I3x2fCHp9wA1K0RRt+bzas8dc0PbciuERyEeXjoaJtqJ/rjU6DR2i8D6mlRCMqpF76Qdz/i9iBbTMeUrFc+n4p5nQgqi0L7J0LZAXmkOBkUnZ5zDweq7wxSoRm5FwhS0nWseXTeHc4/EHwAtYdq21BlrRLUEpy74mzJlxCie3Cx6wJzcYUcuScSSE5o7iX8sx8lTYs1svwDM0sy0s2Ijxed+diytUWPJlq0OQytiAhsdCLX7gcB29IH44ewh3dIuINHZcdW/5KnherhEUuZ+j6dUiiVBnM2XWfH6wHlHj9/uvCUtKdIBmEuguhdBmCeT/aXdOj14GWGiajTiqUP+13jFhRfGukJw/DFcnvwYaIgXyG0AjvA4/O+Jc3l7PQu7wDSt8JzpO6DvUg6lThre6TtV8nBI9V3SaPAzFp9LcKueWAWrDt2Zsmi9AUgBLYWdGEb0B2764fmhTXOh1i2Px/2+O5wFcHroNOdEsjh1g5wlHWf9EXJI0O1NRan3jUimHcEHZpU3XV025MVd7ulAYpNIymKZdYnPhKe6hCvPhNJU0ot8IYxCSj9WC5pSVx88hpYeodlNaFezUCMpATceEG5HuHr6Je7mwdHWuP5DW/CzBd3ZITUkwdym6losI9Nc5/KXp/fy2lijycZmSOjdgfw+if7j+afisywS7+HCc/WYm0DPPV7qMe+BKU6AbO9OkrU73i/CXf+x+UF64HD8bO2jNl3MSa098LvKpkDSeIDS1Ehrbt7l2f3oLCUdhJwHc3m9zhhB7Ddb0W6LBOg/yH8oLhSt8qGSpOxvr0+peHL5zlfi9i12hOmtcPX4x1wYPIJaVrx/VvIddkhgjvgOgxYoaho1QQfu5DsiIYYf3SuoIaopZPSSi+ysHT0kzb8P2WENwdQsbNjhYSSV4+KuJ5lw7R/s6hBG2C/zEVGJLQyXSO+GUS0FaIq3U/ii8D9HU7yZg4F509sknChyqV7pfKbnf/aBbUahd33TYyaOsJqjYYvKzqk2E4MiZdZuXK+x8hguUnnU+HZk96xt2IG9FsO/18GRwxWQ98FOe30BHwwhNw8K4VuUdtetLFsTt0JSrbbb3bDAAehDIAZtYfcJ9vgIAHWeXecTvH3FgLUyi+xFaZSLcUQheQVis+nUvutVoSbTc95QL69kOgiwFMttrsWfJ/XJG6QF2cw9bGvpTGD/gm0s8MukGLKVCAsZ4B1OKC/olzVLwL3e5DxvvuiD9NNBOo1fzMOj08VfCn+WjLEY5BzCJjFhnrRHm6ZFloD1/VblWuhVv0zevnXg297txiH0NEG21dTXFMwxM2VIfqiuVW+Y7bPA4PVZKbIRqY6z47nFREIyp+nsTSLVj6uOFdn6EjhaES+yqQiQa5ARf2aEj3/c4TRWa0B/b531R6mVwu7MNsg0PCmCU8g9FUqFH1EiXV6W1PA6HtemOX2WOQ+0BJwuzmQnlUBAkS3SZmPVaxLlYaFrfdCZsRKIVlDKQeN50QwJietFWkErG1pxt3DYW93BHVPyrLSqe9aNghWnNsUdflGLdg6wsCd3lxPpMhDQzko4zvfsCviT5LVZh/dUdJn6ZXcVrcke2FstKQ/sG3EhepewPQKOQtOPlvldzznqPupdCQ1+EktTmgvr6rs7jETu3u1Mb3sVpOVvitell7MKHdAqQ5zc/O8W4icHrfMok2qP1A2NH3+FSxZC09ktEzVdLw9o0zPMngxuM7jy84+t3VdjjHzOumXIUrgMk4zXqL/gJe1tbTeqnal9cKzKhDipICQ3N4DZeNhnva0ixdHtFZDa0uCS4JnxjpqjgyLOtALgszM7DMaLxC3MVGJzZQxK/k5UFz7Ini4bUMoxRBQ/Uu4hVOc6qQlOhlFBxAhqGWrqAaXrdYRKXJ1CHpHfHkv6OZQ0D4R9+Rmo/e8L8NnjN/0atdJEo3V0n30kqLwx/Y0j2FYccJrMXMMuBCnSzEOhT/Kb6PtCDMCPe7jy0wkEOxOXOeHjwfqR3+AyhBp4xY1Ca7vS4T4Wp0O92K+1osgi0sPlw9Zup3ByzzGatN7F8ivOE3jFxhGjRoQBNhq7YMQf99c2tP8VhqVvYvcmdFbcQfP/l15qRGZIzRL1eJ7aeH0C3gyAITShoel0MiGwLUr7MOgsTxI5BB2kTMGm1O3FbUfplQ70vKUa6Evmz6K0rNtxNbk7rXN5rvpGGj3d4VTjLRBZyMt3+TVqYSsNDvAZSVUtsI4u2oZTOEj8LrXWsQwKhQ4wSz1hnVGt7PC4cc9uz+dBpQw3RHocD+pnf3jUjhJhJTEMfAyFdZ0KkHcgaPO9oNeVHk1X2YTCCJPjNdEKZWrhL6OKb9dB72zoWovcHtMzwlHTd1DegE66M6D8AbdUqMR+FF7NwyQgpMEYK+SySMoz/UaX5ValPjt5FZorIP7qJC5VWXOtg3+89TAl6acyjR65KGhUCaPHmZRwSXxRTErX5FaKvB1ZaA6qb5VSs6I8hSdaDtbRWlYcgD7t5BPuNIIfO4mKQb48IajuKi2mKj8ts1nM33HAwmuLweGtkZzq0m5TqIG6uZ0xv2k5bKSrqiNoXvRoZMUQGfTexLM/HDx+Vd1QfzQkKajEG0yYbwcWPyELG2nfQNQ5qW1qGvGdEpZDd4Ha2bHHF1X9cSI3pa1gtI8DrxlWM2XAsO+9etUEXfk5BXWSjgHB4g5S1/O5BEY8Oq5qgphoHzReMYaCD6+5GL4A7pHMJvGFBalaIoNeQZgCzKcTZbCxL2q5O74Jmv0lazl13clsRdn+OZ75Og5clt7vz0WHlvjM5ejQFb8uzU0WGudN6J/jabMWK/vccBVoFh8ws7kOXUrecupNv4Z88Dnh4Dgg9nBoaChh4u2P0gYyvwQtIRY+qCWaFgV35XmoEXR3mjY+u6NmLNHL5SVTNqmyUQD3QB5Ulno/eV3znRXaFxgFgJHPBfFC2hguQPkJ3LYr+zb7lbFmIvGOBBNp2ZP9NE3xeurzSqSzPFTVMZA3AyIv1RpjtyDElxiR8r5J8eScye1h2+a7LxFdpbfe0BoUd7Zt5W5C+Nom01XQN69J2mIE2GdpZjiyGPqtb1clvyTOHzChkv2ZYQ0l8Jy+k+M45osuD3UpeHmF1ehAEr5wIncE1kZKADAWzZAJb56JaVKOajDkMQv1jTbR2fKc9qR6VxYusrwivlnt/79+0UyK+WOn1ZVXhHIW6JXJxVLujBxW5opoukggHFiNeOXek8yO4QLeDs2wniH9nzoMW6jMqmqbf07PYwdyatqsVT4T8B40b5TKVsYIk9/MAxfSFoE819LSHqnzEbV1baXGutGhpkbsQwrZFf5xqD/yKjonlNkFkMqAw8zbAOsbjtgHy4uLdD2pvlzgw/uS4OVpxCukTLDvmmByagBnq3gaZ3l/HRxvsegvX+8ES7t1+nfVDdYAsmj2qtXWUgpfhBm1fxLZetom0oKZDGQG5ySOdz/yhvwWm0i3oWTa+HGMRg/V9ikKNtpkGU9AlwriQr54Rri1paMP7GZUaiVBo18XgV8Ohz1ipSSFh+E0BzmuuWriUxAkCpX3CaDVl8IO7UCScXTdfWbLlDxZPGg2a4FtP8ous1jQEgAhOa2zZ1Oj/IfeBbneKbB/rSmjM2RYoD0seq2wZsWdjr/vrlXwgj09Vl495uAeFgOfa7i7uAQ+MaB+q4Lw+EfWVIqC5AdK4ZSTxUAViAiIVGVBvL+8X1f+dVjF4QMXUbP9nIrP4+SU8R8BcCXJ3bBdIjDk4W1lE93Ur2hJsTihKfoeBd0XQuTz4OChJxqTnReYBqS6CxV1zbFA/vv9DAM1K/M+/owvO58GeiaQuR2WVEPCaAO3v5cs3v/JiAqLL5ISjTK7Qzcmkg/VD2MSvWLUvdMF2/PDQVeqkn0TB7/VHtCVw0OiCjr3U5zpSU1nSHKEeRfsdQuOo4m/RQOIrUXNQfXzWO3WNFs5C1OJr/0/XBVUVfMaQ0Y/FiwYLhKVqnC4IYy6lFvgoY4N41RKSXxg09jXt/kXjTjMtwADL+O7jtCA9hH6mDoXTtkUyvKucRe74NyMrrvVEXcjl/InDZtyEb8Mcoe1hyiWqEW3S+Kc0C1RKmUJvczi4b9v5j88BvNByvkMT4qorsYhDo2aY7cyTiLQMh0WjDZcfL8uaQKS2CgAN1LiET61QE1s8guxUaywrkA5XPta2eDEqkfQem76XBUerVGZs3pMNRzRhjZTrFNGKFP1JJvQNgi9Wxm1tQ70HMc3J+vffRFu8812U9vj/w/lU2Ef3Hf/SxGSGMJIIdvTUu+aBou48hLjSQXxeg4vOXFDZei0eQWFXB57/0z4Yj/XqhD038Yts6iyv2CrD4NRjE9aNg5FttZp9Ju0aG+Gl+zoOzZeagtsH6cLV+GJ3LMEoxlHjKCQDiUNWITD8QRZ0Q8AVEY2dqPOPJ4cK1N5e4B7jsXUZbQcWMSWhwo9ZeLEtLMC6Ir4O4ODpouTRoSg0sf675Qj8saRI3N4RKSmNABDWbW/0VN8eHJbfh2+ueI3ith4DUihjSWn2TWXGydeBc2mCJGOlgMfD6XDFvJPuE20yQQftAxaGh1wtBtxEJv+mSkzw24eyLhZ5kRGnnAUK2sj9qxlyi19DVRsRH7vcEo3372hTuGy38r7rdXOoGYkXb+Z7WtcqmV5F1jpkkwzgbb9OHy5NpduLg14yEDmpG774QLtEv72WdAkRlKDKuefeViAumGHrUe/YlrLZHgyjj/wkV9bP/nOGYH5L5BzJF+LiTX4OScG6d/ilO8hBqm7OzOLOP9f/dmdxj2joBy2sNolK62l7uAvhehyJyZc7BRXUAG3mPhca/r7eEgFXHvgTYA45hiISS1Zg41j9PxM/RAvIp3phO5mKw0/DqHj29xqwGFZBu455YJIEPmqUbHtuG58mWYmfEiRzG81uuQXkIHE209RWnsZd5Ycog5gPI1HGBHHMxUV2fS6gJrtUTjurYYtVKboKsFQlPB/XqfGCdcBSstM2w2gXzLivG430ELvXjZPTB8zX+c2hB0sAuUeEOar7+iI7dxyriI5RShGYU9+FT86LYiwWzV1ZQJStuvXgfk2blWkpfuFQAmv9AG0nGkqLncq3SXg9d1Y/vM/cxjACfzJ26NQB1tUYQskl+Z8P8uQx9IAicdleCjAOFt6Ramek+ghOS82QcwTF02gJXlQqSM8iDj7hQAtZe4oLwOsD++A8mVPlk/su/qA6OEsZ6fJR2I1uDAaP/iZBPPR7Sm7b2CokjeaOhhwkyQVZHdiAtige2SqNFSqG0tARLiL6gbL1fonRFXaLKQTT6J5Tkxt7NUv4Lhbov2jnCUcl1dWmtolwfmsHALdjz0eKnaP4RceQkzo0xjvMq+ShXeYepO0Nu0rr7WYD7fK/3l7WoFXCP9GeSWo6WSrDJAsjqaKP5OKbn9RKeiPAuLPtOELVMBHWv5aGJmN3Jz//OUAAnHs/SJBiZ9A+zUf1gIeuxeBQViOmd7GUJAHTW4ZZ7ANWMo326NexBMDl2fBtHRjQfG1CfqeF1atT7QoTJ1zrU871m9MyyygwjkaE5aYF114dB5E8WJCF8Wm/LATrtOTUdk5GI48vOLWdYNOFMVSm3vHL2lQG/sNgGj+LJL/R1PxkypabW8nWOAohwtgXym1Y9ZLi1xqqFzonLRgBrdXPiIxuuaM5+uSjt6LKSl4M3euNIY1VPGDTG7rzqRDPEUjknz+rlSegAZ7O2H+nvh0AP0qegEZv5lOochstr66Bj98BgKAiGltms3AZaZaGWRcl757n5YpyKGsqUp4ux99J5R5wghhHGtoznTWQgZKp7nxiXWBTMoO5UdUiiWk+LTEgTP9xYlDv2QJKTBjy/bSON7tceflAV+EPCoMmDV5MeEqZ8ERp/cG3U2Od2oO3JuW7bU/xLrqaWIurwHNta/7wUTskqUku0BZvaFGGFtR8CHH8z3XhYyMbNWwc4FGW1as0XeTnpy6onH3pwM6hNz35zihDNrs2gEHeti0NmbDDVMvUvKGeZhtMjy/1O6XM5YIZ4e+xqxkmgyny63VpiyhAf4hGCZEgEu24F4nsXkxAooqsuitio9ZiYPnMHOMGVPEzs6LAENGfyiJfmxt+3DKYBjVyMWoB1Uaa8zYf1HStH4TxcUM3icBvmal+el2lK9R0mryGrvBDacnt0hnXUFXz0xSmOVhLSLql7voJQHCWGEOqDi6cKM/lcHQJSRH9V7vznUiftZQyv+eF1LfMKbCwMjJa1YMau+sePtkf/T1UOO5vD35OMxfDfntanJwL3SvRbkKzJheiThqBSL5hincJjmJiEpJtj7rtu+5vLR1S3V/oq8e2ZPDI1Ap53hBDXx1N5S/KFN/Af5me51vtxNmP6uaqYgmG59oMTOrDWY57ZkakS3E2z3xsqZHX9m0ki2Eu4RhmAhO3QYLmr0AeTbI8TTq2XqLOXNzI6gFk98jQk2eJXi1eymNJFWMsOv9a7jIRRMKNBPsmctKIU/rCENnezKziiG95QNuYdTCafCIMRz98vxlXT+eU5HXSm0RB/szjEwBnD+s/ZPRma8dPHMJUoX5RP+tH3rhVt2Q2LYoltQYe/g0cBVBa8rADKYG6KkBIZvaNk0pxqUIwf0t0xQwzvO0fWa1Tj9SsfSPldZz6WR63hwVwvyRqi30roiIJVuvpCQd7m6fYivAE1lDvb9rqHHFlI5f/zt/jk28YzLiiJGfD8DmnvCZA/p+KfbimuwIXXc1i0HGLM27LHKljmyAvHTTdRN60T9O+rWQgIk915IgIfO7WkvN3t8ssIkfscFy8WV9QQM8YLgLHhDtfvEIeV2YTzEPIQYsyUP65sAaiLT7xg7Vilpqew3Db2pM+lmqd/8Ubz7RYUdGHJIuLrxekdZNN/Asj/k2b1BwJGpw0h0xUP2MUiUQFKVE8j7wJ9ShHtXGRj6sHT/fkznwty3ht8fDcwluMwqN/CMrjWTl0C/1HkYYiyxhpQ4PX7VgmCC3XLL8DQdWpd7rlzPj9Eh+V2bSa5NOegWkKh+gUi4Wv7adhzOXEqLviZ86N6k3nsdl9R6s61Gb+Xfsy9hm92Bo7c4AEqzarTiVI3kwrX9/21trMIz1BEwckP0nLQJcKyZ5k8YgPK2ipNtZPnZgtLQc/tTpPPAlSmcW+UDmct+EnvN8yfKeseXj59+G26tWg7OITSe9BjYKDmYuXB3iAq7j1gmK1yOeuFs9kJsZdIuJRJUIFBYdRtNshQ3jc/q6QVA+6qWyXGi7tV+5okx+Uq3ZvRobYbQWMG4vQIPN/rvkIE1IAViurxMlEgHb03UMgp1JPpsNdVIM3/EPBD9WOendlmk6p9XW+CE10yITPesxnj6pouEqHQEcjJzxaHGKFiZ8MdrYc9KSs2+8O4E9R/T0a2VKLPdvw+ttH9WisZIzn2TqqLctDAskedOzaG0tJ7fIrFlpQypiuT21AxOhN1ALj0VdnHJ+UcmqSX607idP2HbtPWV2GCh9xSa1Ihbnuq2HqufpzCNpT8VCkxZBmRFZTlagLAnvCqWhu5F9LKcBdMCExKHibfavIavQ1apMc7gAU5UI1xgnqaLZQsyIJJWmySiPMwqhyJ/H8z54IQElTbUJjwSYrDSwxyrHL7Vn7OPYiuAxNZXK88HFQLUtM27Q6YPCPoFQv1/F03thbnx3ggR6GJDVdXzpqwujUe7/JtP6FbriH7A6dl3ODq7GK4NWuAA+8oMbS4TZG6VSyTScYdHOJwZE2OTamvidcBYoUrRmNbQaxyhRaaclcioXcEDpKiBb705ZxjyScJFhwfwSWk0+KAOLJ3NGfcvF4ely4ajtg/xZsM9+5AVQIFgBCSH4g7QNUg3EEl0JV4AHR+nbExBZj2NU5PDeHbk9j5bMm4lwyEHxKJoqPIsCsIDVSg8gqpyMiVmU2QQhd8jIPiuKG6I9K3xMLX6rq5rN5n0AZNu34QB93dOWO/oaeGc5vF9FSQSw3BE53XNldAP/ZGuAhAvmM3HaGohgvHt0cHce8YjIma0A5XFuNiYwfCVUpBYGdoccMR0HEErMtP1hWFzxQcY9CICaYJGfRn+gxYuluf4dw8vckJ1vIpd5E4vGAJtBSAcXpUFtnwj5Iorkawq0IR7MT10hiro/7hgYPlJUhigG2EmQbyaez5xeNhzz1cp7IuW1Y3eMJYJrHsJSGVTdpKtIiku7XghKVZSIYaty+3E0j4GWEZ7PYYd2XLeMtwSCm2wRJ2kl8olqlSzWXM2M/b9Fw+9QuTfH7zUfR/GZdfkMB4PbuBO/FaVo9PisbStmE4Nfzbfjb4PzvBTOgE4wP1aN0Xh6aNyq9qgcqJdHPtBRH0XNLS8bvFyp81lueAmsyZ8oo5Ml/snKlpVksOnvQU4isV38LDtgj8xablyiNIGkUMcNL+qRaTpiegcaRb26TdngUJBWyWd5zK8/4lWznVfHknpVeYkOpkGSM9rQDgEZWladUbd4q6kNPjKoJWDqxOIirKfJVbh8wr7zZv/+H5Dzgt0Q3HWBoYy208xbw+obCb+L4ds1O1/Xey8ZWAtTt0dp71lfqh6Bl18yjjfYTwuUl+pERUdFaHbkY2P8KkEVVlIC4IQHF+LxYqq0W9byq8uWDgiLJQjgn6oAjTZX21LjNmdjuMHZm8O0QEIrIgrV9NtukgKD5pn8HeVoSsncq+9zsFZ8J+OAyON03QZxGx+dYpxMf51WSpzlNdj5nVu81PI283hcR5mBSZnQFnowTXNdp6quEf2OyyZOZWVuz4mp/ZEVbtMe5R51SpyXP9/nTBOsCkCAqmVqMUR1z4dBTxDQgx04zc/ZST137xnpp211THhfdqbCHGfdXBucUX2o8ysmNNeYjx3yl9wIMxW9BZXiGvaMLzYdX9Q9TsKW9UhbGjpikEZtlycMHoQP7/VJr7a7ygrl5k2pesSDKWN43OC1qmVzlPcOwze0FeczR0G3OwkrVHGJLc2PaJsvasy6LwdE701ogEMq7vr7wbNgdgu6bOdJYNjx7nL1kJiIIaF5L3nTx2bmaCTexlRrdcql/K8E8AT2Hg7Gc8cyNPFc8v3mrPMLAUOJ8pWkcwR8uBRlHS3lIy+UglXI3TzeiiMQ3+wV3zCSFkqiM0BoEKgrhDrJDpMLQ64ExgVtYqGr5Qu9qLFkInnQvKmuHQwMgpZCQeQ3EN0rkMQNtoLgnwbgwDPic/6AMc4TXujKj4uTUFGUEt9RfxU1xn5EDPvbIBRgrCKCrojEROEVFJ9fqgMD+b9hzTat5OaRY6SIuDbnzI8Eh+tUHHWj/34jbcwpp5jIMyaVgWexYK7eSD+5wmDIaggqxq56iX6kbCuPvetznnbsP3L6ZdDBRgTEGuxCr7paYt7ynK3G+5VQdfaYwAwfVmpbCwLv/0uUoysKNg/zmajQTqJLI++WETLymtX8oovUgGnNBLGQYIl6bUNM0UcAp37GBldnacxbPQw8s/AyVd24YBf7y4+ISCp9eijnPd7SnYFknkcdacHOAF4UOYy4l/bc0QOBMYpn9IehLkW5Nh71tJ9ffb2mIpaXOil60yviEDyB5ajbHeslltGoC6w+Wyrry1dh6UDz4N398NFqZSf4jQWy1JxKylNKW4yrh2HCrqhYYPcUibEYBW0ZIX1lN1ho/GrWB8K4pgCp/R0ej2QW6vp36WNjj1wZmnhsqrXQzJ5IZfSNrSvZNaS/Ox2SSxf+BkReoqbAw4FCCGhxdLcxyj85GGTmwsRQ5aFTHHR2BfCOw9/IwLMWvB+LHc0bPhBbhLWIxGhgXbRNdSai/vf8cH587YD5feYut0lgFBRMihufT/5PMNH78DacFf2z2NhBDQKH3XwyLb+91LnzPwRSCmSjy52tmWBxLrZ6idgjop+ji6YFOK9uwUcWvVj6hPtD4+A+/qb0/c0noYXpNTNfHQFwgOCDHryYfZ4Fm6yneN1SDgG21Zc47MXCNRCctyICxPraupChH2KTiChVJNob6gBUlKr/JwPthq2UI//iw6s8nT/AM+6s/vKMLWFuW+zNWgER/c2PdzdqmhuYqTOn/uhWMRbB2F1ZPIFji5JDVTqauUrnZncUtjEmg3sO2toN29Y4XnJVhLCBC+bMGFnyEJgiTDOa8P6xvjbARKz063lDVIQpiU3YLn+6hUe3SkNX2FQFA0WKP6fvsf+o4TU7U8C0AQKg5xaljlbxoTRMKAiOd5ahbCKpDdJuGwbPSkSJ8YjrHMKfKPecRwiDtBLbb2VgYpwTwZZPc84rt/VF3Wbz2SZkavBwkm+WCVlaNdWr5v/s65rdNf8Dsf+GTHdp8aKaWz973zFl++iLoMQq50/7jEIMbJM81Deva6/23LuPSHSdvYAb7wpBZUKDJVq76n5bft+QKUBREbENXSE8a7pZ4RIeW6qlQyYOQpu54fRqDTYV7qYq7d4zDSbLfh2VAgfGA9suM5A20m87X8YSgs/FTQ7THRi+N2/1O+PNQeJ5vMGouKeebQWvJATGLy4/nMxw3jba4bJcqC4BWaw3SDexgQ9qyufbQy5w3GWDO7chx+BaKXz3mWofBJKXEHO2ob6RJTY5Xdj7eRBPc9sosrzRcJxB2ZfzBwwEwOnjAu1p91Wwna/O6I0l/GZYNan5DhcYpDcxoO398xA9ifeVoj5vDYobIS6u/a5mlX6478EoWX+2a38loiORug3h3OAW05A4sb6bBNrLgXJY+SZfHOD04m3HaSq6VYqoVCvWCFvDtuJi2w1uBQ8r9lK0pm3RxKwNIA6B5s+/L6tflRDRG7K3kIxw6+Gg//AErS2MFCr/jknuQxfjs715Sl62sQiHIZwonhLGzREPgqOtZB+tb/HlEGgLBu8Oe2Cf7Nh16Av/w9ddn54bLzukU6DJg3spWxo4U1sGx+vLu0vGHDgwAMlMbYidaKgz25M0+2xfiC8mX78mooaN5pY175cCEuwHZCijpKQrTtN3Trqqg3n+lBPeRUtk44k6WtEz2iu+Ov/V9boWsn/tUc4PQNeZJEszJbNOgZCRQAmxRReDybTInCdyncHKStzTe1idn31dJixf99C/WKttpmIUkUopxhjq9r6vDOT4vhWx6l/a3e46sCjQR2cmPtdyYLIcOyYKnFxvwhm/RDEz+Po69MSU8rr7OXBLtBhZMQAtiBbhdZYTUxevOVbf7Mh1PHILueu4xpvrdw3fkX5npjn37LvLX9pq5harmempadObDmF8y55yYptHPVhbab85GTpvpucwgyDF9x2zX7ApaocbMmduoDjCLcdOyArgdEKKigpsa9YTBYN9QtBv+C4sQvC+gp/eX73s63jXTIOPyJvsWukZgTGVzDyqMrNWLNQ51y/ONHd8cwHubpgN76mAfLUbOZURFjqt00OIb9uSj96C/t9gYm3CGJ4g9bh2SQvOFdKRMA5Ua2Qyw9mpXh3ZzBfeg7HU/CUQO/urL/Cbc+L6WogfuLbPxRF5gzp3ZnRPveJ1WtfpKQDTQXhxBqmpnFmngZvZp5R/dBak3f7anCzX2hEKP/II5q/BRiaXIrGf9lRJs1lspzmLGNYUI1Rq44K34UKkDB9vs++NqEwqLicTzMVLR0HmBIdyQXCiLnzhcnAH1nLmXg3CkYg/cTxQ+YzIk1Dn+iLhfvBYuLjvNx0MtxAUzNY3Td7UPEgGbXVimZMpppZzEPLC90Q5ARNm6eeefgsbajcRM9aMmQbUQXet7g35WGwx1y4vWOC6vcCa/wDeQWhN7kPZvYW08Z4+Dxu5RnrdDtZ1AB7Gm4HzuTDeRqZ0tksccDmnr/4e+Y8ta36cJ9BpUW2yqd/G0U5Wf/k1szupC8Qfbs5dPmRvVBySjAMnQAencPQQ68gLgls6nN+sMbZdxZuNm7IIprLx57A6Rd0cGrEgr376PS5n++s6Xk8/d5kx0p/VNaIjz6SksRewRrXz0+/HBtHSt13D/A2EiSbwCPkRUrOcAi4z786MD65+/1FhSeOPxlMlfTQR/0lJctq1sI4c/3N1CygM+Gj5m8DTXt93k7Oyh5t2MspoNjvOLCFoEg1xmHbo7nHm/KDfRH3ytYwXjr13THWUHJvMkmcgZ3InydNMfWAd9Z8YajvicwsCo+zemMuXMtkm+dsxJZQSfFct1duydTHXlcFzIKh5VaGBaeULlUY5kQV8sCZpI7wQNceQMlthLOAZ68P0PvzPyj3/8ghhZ/XOc0RnKTlCe8ejLvrI/zOk36RndPsSXyk4Ubdd3sNnKaOTHEyQO+sidliFBG9eBWTCLdZPUAGsZI2aa0fZIaW0hRLCiJzHBYXp7ASuQmRCj3VfMB2Ypa0bbiqIvztJLLyWGfxJqFUYbMK9hOGcGzPKE3OGzEa6Kye0y0fN7w9qQQh0MUwoKaE6DMkyV4wj1qiP5ClJAtxEDAIPDnAbT9DOTmyPLXklgiDxnxkXqfcTcfJW6AXK9qpqxN4wQhoyhb7kF4GM4m++F9JxTmTwNeTyzIRoqPp0c5OJpV2C4OuDQdU43MiIFq1xDGU4ScTe2/rig+HXxprqLFnlkL6ahSq2S3ML5qawl34PwySLNi0SEg7fgMrgKNZDX91xJTjFqJBTWR8dnvx5mfrM/Oecs1av/jfTMWnejGR1xakKZwhlW/BhtuBHjNdlwjb9Wd3rU1zDzXJ7a5N0eS1rnXIQ2cCwsLnwJGGrQc9WMQuS+5m6ms+cO5kIKTmupXTiYqxH4Lh2jjTX6RIvZ1JU8s521SREgOq1Abexy45sTZd9J6p6e/TYSpUXQuOpb+qCBqxVUkTdf5NsdrYMuYbYnUEZChPN4t+r0Or7Ye3525tGwLHHz/GF+yy/vnB4J30bp9s5NrLqnNNgS/pz9/HqETBn67LVa4gHezzRgusGJFljgLukdgc22Z/5KHZKd1Z2xuzI9vjejR87TKKKAMovQy6EkQy7yrk7GUCn4vJtS/Ht6BoqDIdgCRNmptujeZR9Z690B4kvwJQfdcEqBPhm4KHqmdnCxU+cXp+qgt4u0a78wqCUQsIsrxTHcrM2H1hYOqSqlzs6TMHzYLOKO342la67cYGcSczLD7sVqWVF5+mpQKXvhCsvSUz7RckPJnGAtE5LC9iWa4XpbIy0SjdlyfcAaFtnGeDLisHO/c+pY9eW6oGBj4hqeskAtdve9CWqjQuxqNsoqx6gubswLesBfIUNYLkG1N/uUxq7+qlQlXKRJYuXyi113SnUw7Up1a2PLcnYuGJ8NTKYgV2ygNYhvxsF0RtXYJL5JUw+VyVVYNBGGIvk/kBI3WV0zZsRswLALRbusmfBrTrRMV+Dya43UB+OnLb0LEdcuH/wLxdx2FBzWTVdbUfEQ12b+cF0geOxnBe66I5mnSii9UR6zOMT8YxkH9AMUnB1OqEtMdWtkccmRRjekEqvYtvafhLrUPmzyBUDOs+Qbghl7HG2fzatY8bmyY8GFauOXoUt0JInUXMj/XrHN45mAV+BnZ9X4vqeNN+D+Ztk1Z1i+SAggPMNhYw1j6vRrO14nDCgb5RdJivwZl6IiYrUY5W7D9JAnbk6VT54R/hZdpIhbstFw3+d6T8AolVaRCjQBL4NX6td5lQkH5ngS9r+cNYHlBB+dtlYY2/x8imiATiAnqkZOZe1ogmUnD24H9dKhjp+EnBzRCTINEbU1yeEpK2ZJpfprPg4GH/7XEDcqwYjNiH5xDwl4yQxUBXa5lImw1A9zNtyPn+/z64as1fg1YjlW4aJNToJQe9+qaeczBTX8FC1zjKLuXH6wC3rAD4bF1U5s1LeX7Q2D3w/kSFFVAINfaEUb3AzcTu439jX/kfolMgN/uGD353dEYU9pnR18tPzGHfQPua0h9kFnwKVsEsx1K61oM3LX9sv/n4PIzEZxUKNIT6HusC9KEPUMZlChsiZs9UgnPoO+c8t1sCn9n/v1JrS0gFcCvF+8hLl+InPmNxvj9c3QtUmt5g7aPULdpfKtJBiKy37wcYZFXSex2xFyrW3JfchbU0VNqPuG73MI/ISFfcIlLoTwSp/zhZJ9p/2QZ3SU3ZO/8BtSztyx2EE1fZMNOkr405fc3R+AHp0EqtDzhPZou28oRhiPLAeb4cYOOBU47cUl69tVddahaNoN98xgZtfTOiA7EqNFR077iKofVeEIdFRST+FuDZxgs32cOu4diYC520Hc1PXV5Hx9ZMCrK9ZKx1tJxL9cbmn3sYJg+AQYk6z1n3HnMh8lc3RKtvdWl6PcBrqH1xskgB/kYom57uUW519IPXIhQx69oVbEoNLgpx3oaDcFtNwq6vcOiu+0UJJAQ3OAMUZeyWWfHkTmH/JWSmmefHesg7i4YMhHo1wSIot10BkH9bM2svYrT1tI2x+lTgLRm+DAK7F8MWOgLW3e4Y3Hy0pTgjf5zuZkTCjf64ogon+UAUsIwToKFnJHqCzFZnr2TgN/UliCEK3mAUWcijFu3e8Q89pPQso67ZVH/0g1Ziqeg5Fy2YNlhaLTzjoP7P7nbI2aWe8Iyiq5PU0EghWElxiL/27bmSPoXBkBTcg1lr1K2pRyZjGeX+XjakddwfDvZh9FoODJZCHQZkWmyqxsGiQoka86hJXbpSOChxk8pP+7MHtrRfztwhokOODlvvWSUGYjrYRd3JfqzQ8YB1uRaf0QMnXv9PhhGVCv2JkaE8VX3+jGbGMV+zBRDXk/Yay+b2nPKI1Q9GGoDabrnHWkJx1+XpNdmA/ut2npHL/pfAeuiux256DHWorQn61pHU0Gp68S0QAa3ifA/nYlyKp+Z0Nz5QhKPpzpvoN/Pbz16XHJ6yFgTvfHQSvahE5Twno0rn65vGh6p/wRDcCJSZdixaayVMxa4yzLUzBSwyTjqtV4YoD93xyAGzXicR/ervQeKmnVV8/9apBBXBXfVwlEVwQ7Gk9PUpkVuW+BaliJeH0YSSgoQLENeAIIdm8gHFdtUH/O6yXqfMNfP/Iv1jW+AX8OhMb9QbMkRP4v/5PVosB1cMpulCwAZMu/ZQTBdOSVLdjJWun842RKFJzqawqf2DNpEDc9+Ku2k301HEaSzhsffkRZ+3AiSAsk8eBhfpz+s46O5xxU4vp0CLwZMFsNru3G7ZqyHRTlkesg8fAXLVx1X5y2U6TPyJhn0DoAYYlSih+hxIAi62LAIviEkkzwH7GxOumxE/33vzNpUx/S5uw1QC7ED34+fwSMABBuF1lM0oSy761LDO7w77yMwvqzJqU9VSQVbvogczGaZf9PtNlThuAhiYjDzNgXaBPMbvWD1tyJQ8lNVkOuN2Dju4/Nm30uRehuI643/+gLmpcfgiQMwFTR8ETNLlbbhVU1SH6X4Ie1LaZngCdT1+aR1K3X/Szy5xMQkVCkiQ20Lh0Ggis1UwrwFPQYFAN3FyGIiOpPygespPeBiSiqTxNEyUBdrJldPrKnHzjGbxitpgsPGqVweWMBHYasCeyDP85W7qRHFrbQ9NpDblu6YxwTft0CUFTe8Cl11BM/lJxP7QdfSUBsAXM+qO51rVL1K8CQaOIWlro03/SqaCpC6T6TUpzBkjDI9QDWz8FqYOByctbgJmAbtm4GVQcEDKHeaWDt8Z5ayMjooKtfj7A4DD0WWLOmdokqFIlfF21OgQXWJSdXTkxfeUGrw3IrxDtc2zDFyTiBNZAnmBOZw1EJgKbj6QTCNM+DSHGs3MQ/CsWZdvUjbYbOEWnfvY9N8NE54WVmTOYgGiyDdhQfgPEUOqq/OCCVtCGxhGM5bdmB6gNa9OEuA06P+VlUSiPxmCUeMQJIePSnqpigowq0hZFd2xHOl90uaJTIJxUp14aesf3pTr4s1HRhyuzbVFV1lI74OvrggOOMRXZC1uv5OoPBBOWvFQtMFrDwBjrbN2XxTEibutvtO8i+VaUy+S+q+Wt6tR/uy66fAiMoaJfGdNmgsr8AozFoaW41CQ/c7tOnZKGoZ3jB4TG1cWZT4/gDVskjkLuOoYRLmPP1L4Gij2OMFXzIVuJLNS/kvd1Ejz9+gOnPSOyrqO/d3zBHwm/J/yiwu4Xtzr6preMTlqZrcd5KwuOp9aeltrDgtYj6BQ2NgJ7FoOv2QHhFtC+5e5oLS341yXvdWG6odFMOE1PgYpnUe/rrhCYt7pHPgiaSOIoWygurnDpo2V7y6OyEAigvFA85bXH0IHT+KODs1Bg4FhQA+zWS0eiazBjsNkEcLjQwXBC/di1MzyC1A0uDYSr6dp/y5jb957CWcXPbW/oVIgvgA9z621jO2rAZdBIN+hb7akyuAUlqJrXs/jkuZCIVJFxreUULcxN8ODvJYPHj7+pdMTtQhPU0LQ+3OhbeRkv6qC/LNQMGSYIz79qSG7KnIWeScBwe22cZh21xXEDUtFg60g9uRZk5EKtuI41UYlWql4oewrhW3aG5HaNddAoGs8DqaZhNIi6ptNfjnUdn61cdqHWvRCb3LlgbFb65oWmAfJhdIwMjVAcEwJ/OLtm1NtMSLDrP/sv0VjwyG0GLPa/e48zbgWId0pBv+p8X4nTgwcf9kVDZoScTcjIbuWAhWHvKlcxWT8KkBbef/4/7xb73cJ6z3x0FQ0HqHBI3Al75XJrKgAtoFmGEtzdute0oK1GN7Y+CmaerWQmAHn2W851C2yrKbkmYx5QSp83VXriTyfMlyQGIWYU3BGI57AUNwdgHMwkuhAlBEeFV4GppXAQNvxyVutdWiHsDuNkx6tUtzNEWGpeSs5RONf02TczkaGKgbBkpfTrr3EUwS+j1b3l556p3jWMp1AxtlRihDf0kVZjntHfxQKSazEF0L7aoU9kILWREqJju3esaI9erdQutE7E1LBzENMPX62kJMe3Lbyi5jQohoG0Y2Rm42O3Mf2ri3MZJMynIC8kyzYTpLGNkMQ1k+VkEL4cgl18rdl/NofmdTfXzsRr2U7WLXguHJZX0sBBH6l09eKmrXEzoZ3rdncpRxcivYa+jeFL6IgUOgDP1oa03HNuxuey9DisQx6HaCVqqxTEj3s7lj+gZH2pTaxpxNo9aIMcz5DL5plHYz93md6o+1/T2Ro0miBeuLkQTGFyuPiPWhHpBACmAkg2BvkWLjw6LmUceFu/sRLmKznSglGJ+MgMpOyTtI6AAT9rxuItlqh5ejLfbqS/4fCFzRHYZGmsRyEfX23msP9vVIBMwXWwf6/QOhZESj1c1N24YTTf+nelR2oy+tGclwaC2OYkGOSqrVkqgZzhza00D4F2G4+0elLq1rc9HbU0cj28yMrjgMT1hcRKAZM7+DVNlWzbKpcFZDbKzV18tuWIVDDgPOrgWAiSbTkgutb6aJA9ylAK8zWSduAxIfrcbEetwuirqNwdL2tMKk+QMo4Pv4FXkc/uN0A0ZoL2Fb0/ff5evJGBWUx0wmDs6JodOpQXKTamYifNK0TEFjI6rAT639Teg4IavqiQln4IfZ7TYOkvEYlANFX5dREjJZfKSNeuavKbvLKS1+ZSqsOAtFP1k0YV3g/pNnHkKgMAlDi7uUp453Yr1wLhlQuauADdojWVz6Iq+Wzn2j4+nm7xI0R/x3LRi70BsAOyEa1eWBp6usm/cQEgqQtIQG0kOaJ+YPI3J2XBe9AtM4vC2/46Z1xH+7FAzaT+XzuwpVerfMuiJ0jtaDPGn1wGkhKLNyIb7GhLeUb362DQMBmDUihKs1eHSb/zTVOEWi3FXWLdAykxaUhWw8P+/BzH/xb6C8mz3OnCrvl2WjZUw3YSRsEAD5geppCQjuOsgiMN+ejyAtjDJJ37wlm9SKp1YDldPelL6ZAcVOYWwu0a83+nC3K4+SKfbnfKlF1euCMz6aPEyvLSdbHzPGDn7CBVK0BnTsaLgVkd+yaD4WgQHjcM1bHUCeRxrx+ZegoSZfJJdKojejpL8fpwnBfp+pKM30CjH8bdwU/armry7XKFhnWUbbBozs/kkeDkVzDMGqcKVQ2DCfgFq4Bj8cQs4mVShD1a3Jr2vDWFuU2JBPRnxkKsh2meJj233kRGVSBmzmawsRhG/w8/nA7c+n7W7mPV1CtpLQMpSsu29aksL/OUSaS3yhetsdUjGSYisWw9qOsrvN3lE8wNDH2ZQrsKbsK1lmK+bkQR79VALvmTne1qYNJMSiHlfHQKVzx12eBB7L9BjtOepbpkGGs855iU8aZfK9WreO/+U7ktDc3J8pccRlQ3Hvp/vBuRpXzrX8+4iyIz09742PuFt2NE5LTTG0leFuUzJNdIuXyDz2RZj9Pjto/lnoGEgul9bfENLiOrAIBCC7g/csEk/iHstCumjRbE4va7N3yv6BKAl2Ky44dq8zHjtbZyqPyxc2uxXH4Xgi7VvCmvb7f/mSxqNI8o8a8wO2iOnqHj13d6VXli2m9Y1KmVRhxhcJXBbYqWrTVtZZSY92vVmr2faCKVezcqsCTsHLWPSioBEth/Gd79/QazikI9jLz3+YGe+ODS9W4Qg7VgksnY7Y0PNGV4pSZ15XIKivxFtgadS6rOCs/KUtyvwGakuWCP1vwo5NaVZv/PlB7hAxafuyued9CgfJ2d0RYbgIMWt+iJ2T3m/8b/EMzt7er4nEtMOeCjERZy2Xol7Qpp5ZarUZeMzOrtZh1bqRRCKJlynPlSSBXXj5vGdPBCrfGcoGUglwwlH7mtxI7gWWPELmsqw153fBrX+aBNWF6ySyTz7tVeeYO2B21EfKlZ+LeohFQtxAvb441qQjQnAr/uAqF5/FEQWKWOXK6GhgDpgOpGm7iz9W2sjFOWaL+0htkQyIphhiAwQBwHA8+zlDc0Q1MfoEMMd0VHLSFM9HU1/6ICJKrKb1EJiqr69uH7XnANc72RnrX1VNjtCLgvgFyq7H6+7ERQCyGvF6mut57eK2/N8KVshAIcU1vcTONg4pIWg19Fz29Z9JAoYFJJaClqNGPrhU1IYIQKkGON6sP6FUfIywiOEeutFXAFTi02tVHvHifsK+XNd2mty+k6HnHa1rl2XV/q84/lakH7nP7hQ19QS8FBw+EioXNu8QDYNDicRFMsb141FMv4NNguj5YsbmmHShzY6H84Jz9JtuN6clG5DCuBRh6PMYuFnYa3aE2Ln+IehX5wC09iK4ijWF+PGFCJdhbq2xom774C3mk7DFJImNT6fqOyubfLI1FOiXJcr/O9IorP1bgwloBKo9JD+nTMgjjGrSukQVdrheAeLU6EoNDsoqL4JvDBhe0qqRzYKBE/UtpmX4vgCIdoNGC87b2m6pN2i7FxsU0en/WNLRvLLS1E2GTKVRtx8MR7mTT9+kA/+FEmvx12VGlPhA3en2LdH5HmnwSPlQ8kmPPP40sDQvLSqJay//UznRP6FxL36zuEQy/njy3Q5pulREpWRt9HXj9ls4qQPONICHmKNHM64ZdVpErufSCGjkeciMdbAM1bQCKSQF3QVjf4l3hNehrk5RsRI9c/dVCXj5kSCpd+W8mzhFknBj4l4sDTgJ86wnpLMa70sTq+mCogzfXnwaKBtAlAUxY6+HfIpaaqY/eCZeqA3sWbnjSRG/C203Ja53nAUBrzko4rrniYHCwwZ19aXy9P+srmLXulZIQGB4+JrxwYYlD8PIjSl6kkq0z7cHv/ngUsYrmgUlVjkFfQrgXotM1AkCrihsYokOvdMkCxNk3lrjd4wGJyOkmgyggcK6UdeI6Im+eG0n5OyRqionvl3jUwvXYT9c0NmRN87XQGE+dRkelEEJyU9XbWJ1/kBiPy5zDWplT/aCsm1KjUlu9sTXicD9ET7Akz3vjou+E6bhwtN7eDnHryZqN6oy7WVG/ygdNEjTIUL2t2klEfu6T5IOCwMNKuk0fU4Z7/CPVH4EI/FAFp2eHwwdJ3PbZANZs1ydw3gPMn+aSFFlh4z3iHgtVpXGDHTv3GmT2PzNuXXoFLss0u/6l8g7WF7V3r5GkKsPW9kcV7PjYdxsSIiGrmHHtuTN9TgkV/Y3MHdFCagFsF23OTj20PscCCMQnl1jLJJ1Vh5gz4/k3YIlUtoVEwCG2/O8ufjnbaStDBigFFyIaCjaVqrgEPFnwC1UjOHs40ZjooeALslyfo+cU0ttfbVn4YPCludiE/YR6liWy5lvgxemJodfwy3SNLcSBAiW2XhtdMErvZ544+6GKQzLG0YUbIPhWFhf5Pu+W5ctD9YCTQ09yms1TiJ4X7yQtEiJMvSk5M/Y2QaGLczneSnUpkvogRRwgXcKM9zZOU+5UmFuIqB6ILUTha44CueoDh35KULWKmJ2jOhgwq2DAiQAVVvpGcTRWSBMgRN9FeO2qd+hZdUyT0hEN5twx4UFnXhLhP+RhHz8LmsBK0SBNk5kCfNyregzpYduIPlO/8a6ex4MGBOfOII6MVGeex+BYWCI4byv2BytepPrdd6XS3w36FK0bvVbxnAXd8S9n5roWvIw9qVz15R59OKGJ2KfAG7+urPlHCJntm403drkp57vu5P9fnBhz7WUb60lpr4pCa0a7tAK18jhk4gKAFVVHlZSQgha99o2n62wZZDQEEMqT44vTptTR5moIn73on1HD1IFQaswfka02XX6Zt91x7DXX7iDQcBL9ZJuHsK13ECp0w7xOSbWwZMND1u2otH8YnWCodoLWIybbON74tumlQMJGHLUuJ4uuC7c5zENcBrPVzN+c8fk9ebpn/frJet5MHZAjCt26eBjLFpPEi8RiGblDRJ/ube3HjNEcQMyo8xt5067tiVJdSHzIQ5cjPfID/KcIPKAalJQ0YadJFKAs6OxjYK5TFznKORVoiq2lQVBVophc+pGDyyIqMRJBLHLBHqA/mGh9GXdL+DH6KZIpIO8ObPQz1BX0QRlwULqWqw4mKYHAqeLtWLuqGCPlQ4xBE9qU6z1ENwp7v6GbNfP73eqLQBfi0nMgcV6eYZz3VUKjcCXre5qsWBm41/104D9Ad/PxFlXyHRmjdxqumu+btw0h/U/6vU7kVyTSUedbol+lhlag1b8SGiZaADHql54p0b2+/+mhHbwTyDLorPRo5tN7R04irpPygZZ4DuUvNBUpzHHM9zNTNwQiJmHKeyod/K/wUZ4Tv9IkxjmraMuDiTequ/She1mJN60maA1/mYqVXOKolbmKuYgLODpPD989fx87jtA1LG9DH8/iA3sXzBGsH6HNjpMvZVf8PIt7N8T92/GSSoQC+zfZ+tuXM1ZP9kynyasbAtYQgxPlzG9kBVB38EHNvDoCnqnqmek83v6cn6+hIG4GCBr5c1ZS/JVoAePVjl7xjQ0P5mbiaVrt4CaMD07To8OPZ5gXvbEHIyyJFAn2UFr0ExqR0Az2j0GtS62E73KfcRlPXdqu7vapKyW+57R3Fq455XH0b9JW74UiSU5d1w14h2RJTGgOjPqv3HjTTDFTqJp3cXpzZawr3FEYmZV+NXdWpVW274IfCZfDhbEnOyqsqEt3Bfnk9IPyag7BFOqdTocplTng/IE/autjN4cCaHi1lbldCfOvoYxrmW/wKvVoaNL/CwbjogIzXm0faF5lZE0yZZgP1luKR+EZyE7pZrNOZ5qmw20lbWzYQsEHNe9pGdrz0pN8M1cBk9LHuwK4WabY0YoKj42IH6inlT4xep9voK151Jw11nQ7LUbpHsKGTPWomM8tpIPhVLQF8+bL6LkP4c9Hns1UyGHfnwTRfgjxHJCX7/w5zNddIXEyYey4R0fCugF+iXgOI8+1OEqiy+KO/pQP8kH2VHLReWWGt9sUlJwUnLRh4xGCWKhJJQbxRzttNAxKOFX2PJU6mZIYNXXRGU3NYoI7q1qNBcSZCGBtUVlJs2B4mK2kX4mUl2SAjBUm3K4sjy82ZT2/AAt3gNPfpGyw8/hTvxr4g+TOX7GC3DFBsLjsqrSxV2JVCGH6GXea3i9lU4P2uiN3l+jM/qQ68obhd6tH3vRUV1o8KnsH/rd1BldRK2Prp4zPFhdqyDAW5tghkaG/QnoINQL8j+RN4pHygZrtppvkht8sfOY89l7EQCRQ64IKYHEXEkg6OBwwPidQdJz47g2VNsnR2fTD2syuoNqMSdibjYiAJarA5QXn56ENtEyTdhOi+UUvKIo08s6HGixIobWGbRiNDWMR+XcIq1f3DAhhTaNjONsGQIYhHaBCHYbTcFl96BBSuV4mgXLZJFmZBkAEpJ3S6HdRg6ywqcmFBSlOBUCSCAEH+UsIfINgKf20/oKvQWD/7FOax3RhG8TxKxzinGdvqRJkX9Fj5bNFCeHqXS9L6Ts2UepTsU0BRhYskfHbuIRlIU8B65w86v1pV/yK4azB+c+iIKwsURHo2/0EvkPN5Hmsi7nWjxLA0mMP3cX9hwK8rwGL7HwknRzUxo5+haI4uhHOsAwSf1WI4CyKKkMrvnVuZpiJTZqW7JFNfBZ5KXHdagbDAH1jC7UYV7FUcF1Sr2/zkrudj/98/Lnf10HjOAdayKO/SB1pn9YSKBkiCRU+t7WZIAcH4o/hGQnvtMZBystiploQCgmjlueEtFkt6P9KcG9DuH7jI/LrYXkWAQ2zoy6P2spMMP9TBci0oCHDMDZ5xYIpdJVpRsoheLx6su/FXEanjE3edxJwTR1kg6k02wpNnSFIF/1Hm+JZQHtoi/nw/2YubXGGMix1E/0cWnSpjeoIOLLEWlWO31Pn90hjcu6LWxkbuxmU+R9ZuG2g5DIzUQlAgZeSlcR4S49S+1zAz1eRtjTAmmWORN+md2PQO37kk3rOaGqoDwsvr+t+GVZrJxvoC+Gt7Mcm5fMkPGDbMRKlpLdNeD6h9j+269BL/vom50mkX6mFHoV+4BMVxfIh1i6j/06tGzq7JG5R2I6GBiPYZHuN6Na3jTV4QnjrOpMTDaXmGZQKv0CsDD1PmT7N/QYi4+BE4WLO2VgWgJI5cANXFnZbu12Q7Z3+CTKx0nUUvqiizCSHAeuKbiYiqdhXh62yp0Q7YkHfmjNe+6dF3tzkd3l7lMg2bf/LP2bNgu+oJ7DWP+0e6bKuF+La9UjwI8EixaASOdCDx2NjnoeTrH5LvJ2JTugma9uakRv1MJC0nC65LYtUP70Dv9Q0MAET9BdJ7gWnmCYiBCDAKORGAHcDrt71vX8ZyW/V+3XOKWCXAtwliYpFLnKzI/knCVBJ5gRVpJkhpSM3PkggxWHTOBBD31OXFZ6bpxdtlQK+HVT+T/J8QhBE4kRQEnYTsVVBTGoe/RixuQt9oyEmb0rr0o/KFi1E/Xx0nxwuDaDwZV15lCcCAh3Gbq+aOfCSYtiCJE/hglmKxEyhtQ2o3tjrjDTK5FwZfsrKMNDwKT7f/wkyVuu4NQ9TDFPS+QG9bH6DwLxUFir7o4kBgVjJB1WdmyCHGZ9/6/Iebo7YnziXKfXl3tdKPyyj/c21DPafr16JCfV2MXfceWNZ3OTa4u4yD6SY9UpKQBc1VAv5loBK6H4PGGDK+MKI/bMoEmn0u+rsPIPmW1J8pq53X4xnyy7cx04SWh85ZFJOQtiUxKL7lfXORgRD9i7A9s5w7AMCG9EucEAu9hgGdwRcAJ8vz4wTHjEWdiVz9V30w9MpV2PBx7v6zneCIhUg84FIf18f8KYte49qrQGQOGU4i5GtAGGSebiVYYeAK3HVfCwtYnAS6VaG8/J+KX1D7nUoV3h5BoeXjkeUCGdr+kEarTXMMyY6TeWM5lRy6xb/qgJUHRa9x76JteVJbbYB1NpyO8Bek2kOkpR9krfYlvRyPm8w+aZgWK/MYMb3s7p+DwIOCbZG5B6JE2RIz+PBFTloV7OkbIJ/yABJ0BwhmXOJpzigQggmVrT8sYWaKr6T3n17EMyUI7l9MjOkg6so/RRN8pn+MSzLhp74viqpSxBkYyeGre2827MI0NmwXuZepsVEXzSWb8mV6u2cLfP7yED3sSBLUNfD91MJuXHrvl9dN6O/zrgdJxFo52Jmatq/+O4Il83wRvbAGxIDB1YShXO3XPJl2MfDb95XOtUexorzyHlkDK/OuE46a3owgGj5D+SuU243Z4YhIQ90X7WJ56FM1EwxTtve46ylmXabiy2+nvzBseMDtLhgXDnZbgmGbdjYD46yXYUqfUjdhBoVTSo5FxyQofFSvoJnjAzqdp9S1bQ7njXKG/GSGKDQjRRrNvzBHMHsbzWwD7+luavhtP/izsvIiN+fX+AbRUSHzywhNOFnvKSZ69QyGemEyLvq25sUTObad5XeU6EOuIk39/jf+YrExX3u4SB+x0pftHf20FlekeK+wG2d5a3kqXCO6BYG+i9BIpM9sx2Fzw5FIZ5QZaCvWPXUqhg4xHhQbj5G+FcoJLzhq89uMjggB2B5tGIEziDMB+PuMgy91H8Nm4tteVoz7KKxgLr3CBXz1ffdFx71TlcLbzaCRMe2xZxs6mT4yutYpRjBBoT6Gs/4mdkIhO2JE28anAjXWoZDKHEAoASIUd2oDNrWyRakH9SIMgAEBR1v2OYjzSIa1Chru5BX/KfbFaNC7f9fn8ajNyC8pKzIDioSChXpNHynXAxDWXuJLGrco/Mo5XW21NtYRJNNzJhiG/ohIBWqXEGKokV9zg4/W9tq+W6VPj5z6/O36vAgOezlOjQ7sGcJ3M3VjdbmFgyMQ1Vd+ivotES8MNSF3pQW+AI8gpQH5IFf+3+Rmr90OMkz+DdOPPz+0oOMOPBUUWvCyFyKy5n9mdW5I8VbY8EgKuoR/gRUVxnOe3HmCvU+4RfQbqKM2fNjLVn2xOj6ugqNXR8EC/tJFBoGUT1psFGyVevqCqy7mOP8zXx7ZvRHsn1xROFKeE3q/VeH+ppBUR+uVjmKR2+MRrMicpFCbZ7IvP4qwIM2KsmHU4ca18zqnAnUoWKW3+1KoGU6NDnBIdcQ/AFAPQkg0HSYwGgCrBXHzvUHHwkKPlryX8RJ2NtBBWJXkW1JqaP9lVax9t44myV3cc4FY/FHNa/wMI0h4OJqVTdBYmt0gL1nbxyocSGsEXYh4d8BMznz1YylsTbVapl+yg0r9n7YkmsWshdbbkNVzDc86MunNDQYxdrppaoNVO33dO+Ro/XN08OLldYsfFlawW2fLGPugbx2Ozs9YglQ1UfrH0pAi9QcSlf0edx1q4TRLR1GQ4c6/M2T2OIWLkaYUcVyujWfSpAxtveHw3XJTDV2q6Yu5QDKNCX4C4r/kpN9eb+aXQ+p/GuVcGcICmX7cfrH/p5FoZBKBUVcR5BhpTKSBEsnVJC4f+SZBPkA2DhXzLwiIaL+ThhR+xN+meO2b6pFikagSwmeBVvnZ8dnMnHkUbqTu8psZgm51HDutmPQtCHJjsl7p9WtrZ6iFTBYjrgwx+XQ1wNQsaGfCHY12mjHXiCNryV1QSgDX2OiNdkGReQ+getsSRu17GwTUchVaDXgg/0NuW3qwagbUE+r21uAyKBZPfLzl0kq97ZWP1KcnkPrRuSFucybCkSy8D3Gfu5AycMpqZmcB6H0wqGY9vwDefZsS/MqO+WpXX83gSWjCC3SHiz19vTIzpXqPAWVYKuTUOm74oWWXdJKgimq8WfGduiN02TqvoLQmKGdCABj09WIOQl79nmuRCIxOjLX+6HQPTATUbnZr8hzKc3daXM3pkwmDePZ/633iXDE8wY7k6wVfzpDz1UPQ95Csr5RNDbspbxaGE5UIvxoR1cgFSqES3VUeNAD70PQFhvXZFcXMTwWhNmaCghVtyxqTEQv7DnKYoMpUTja7ycDVKfqeHkCAdRAmN2kqTkQIKUz6ZPMS2Ec9+30hzu90CzrQLp1JYsvrqspegaJ5flIRKKmS7prN3HxShx5PFK5HQVt8VQ9XaHtz8EzxMym1AWVeFxrqILwU27jBSV+iPEeU0G42zuwRvk5eHIAXgoAculNWQWIAHE/RpjUCjyAxT7C+kDSxnGGLX29DGV5YwLzVJTjAPaG/T2X3aTOymcIqJcn3JX7+0uBJ8+HT28DCznuJ1CgecTLNNaj+y6WlPkASGs48zFMl94du9bJ7BXZFobEP5kTTTZa2o6cJq7DmxjYOoam3jlj8kWM3hkeKltrIJlNZo6XuxgaDRTOY4/GTzmYaXSbHvL8uXuuPcgnnRVTsxF1t2KQzf32jDlqT3z3rIcJIDD9uhzUgDmMC/NukeuPJBg4uPWSnFUyRbqtqqkKPCtHJCj/zYoFM92XQH/u1yPkArOhGnPAwn09uKDjUrBm/Hf2CeuIljAfM8PtqlKiyR7GzsGRMPqyHaaSrncMtgcEulQVFN37tfN7w+L3HjTBJxQN2/NcWO+y33lokzarjAUaDTDvspdU7C+RP8OliWGWVsB1f7u89x705xJeg7096cRwSs8jeJqIX5KpQVW6OnVHVqDdKC7Xoad40Hzk2ACGk/7uyQ3PZfbUXNM+UubPnbsAxMPELc8UIbkE7B+lGHURJeuMvvsc4ZuL7pF/cdgW4Ci1kawlrz8DNK8fgWljkgjtsRVE67SnZcF00/FdvwHHX2uyjXsUfn6j7B/CKTxlKs+RlYn3LdrDi8Xi+O1ruudiUF7Nis9ffc3KrMDdRoUxP6FyplmTdYS9Ba+VCFTw1oNs6vRhsj0OllIm56SBGuQtR2bc5epefWaap46dkZTXszl4A894hEnx0+Y4BpJpnwlx0rmR3H+Zwl3X0qTZfd5XBqj9uRhWPNy2ZyVnk/UcB+Sp/YOdNGMcciHYGhoBlpmyaMcyFI81OuRa+HWxwwWp16v/G6VishYB1bffDpGeGLawA7R9ZBgj0WlxOTghTsHt0Z0Ej8xK9JLFR1W1bxRhZlmbeOml1wm1SKzqeDPFbwEt+CLR6UfhFD9IGWRY4vmAwc7/z1DrJwS9kMYl4MSeP03M3JsTvu5IRUWyB2El2CgdmkSszkd+vdErph85uJE/P1irmnbTrLmZcsgbVFY7a72BijQyKnMZT9HnZBxGnznZP92hXrf3xObcq/i2ROvc+ZVNeX2M2FousBh8x/zVWjda36v64JrW1y91XvIEm2BBbkRcWH54/W7TAzjt+jPpAOrU3u7+4EchytqXK5iSE0+sfL1yJIwUSpZa/K1cgGaS5A9A/RT+EKDw6R4Hahsy9p1BEEjyWfd96Yz6RRDYuD84qs6z+drmvaDxEDvXVF5w94DLKXKyCg3k3gEUWuK8tn/idHKyG7pfuN5JKXoYYIniGOydBsssaVq4l0vRT1XHamEKP9GLVFW26rgfbMJGR0sRLMeCw8YAibLt86iPoCGJCH2bCfEuLw2AqRiZNGjEh0pEqPqwVgihP4TA/wZ6kxgfutfw/UmzvSnel1d7R0hRQqal5YxG4SOzzpUSHx00sJLkYuWMm3DCoYA83WK0bQucFZ5VCx/YiZOAYLPccuXPFhfdlyguzBiE7JvBd7vXEbvU45D/nVbK0YD+6Z0EyDnGwhEOTqerkQGTBkAybdXmRT1N23SWQceHnX/Y+JoyDPNF3AlgbapDgwQ7wMfX8IDL2CqyUsMEsIDEAkANnUV+OXDl49ivG/4vxHmz4mrNahkTPHBxk44CJlHHk66eEqXXE/Vtdi6ROvS6dDSkWSL3DdmPPa+BDiRyvH4t3QwWyRvlbq4x0sdZVPd5KN+qirROSX+KliYXTTtRDlUzZGQWE9fLBuw1Y9mfsybroM0eIT+tRZ9lw+MWZXppe+IWSxM7AtQKoyU6lyMXDwHqdZQdw4brmPE1ilRqo/WefV+YpM6SGTQoP2GTd+lqTuv3FTrrdv7ggdjMZxzODDkxKBHSRxUdSd1dR482CP9VmTpDqMtOFw3SyiHYVz5+hl9B1sJUq70GopeMXZIl2+l3VZi0DJsamDXktNMbn1VmXHjobDrJBtX/Xg42drXNVu3nxQ1bBeqQFp2R533cxUz7x9hseP2xQqnoRa4YJfU5npCa37tR63om57AGHUFO0FA6fbDRrAC5GpgRF5zsCuuzsdc2jNCTapZ9kE44EQvAFo7eGNmjDDBdHAIhGXBASa7Z/nn/+HotIuYePjDi9D3xmUTgbgG3QDddYosII3jiX7V8z4oJFnG3afpuexCJNV1SiHeiDZ7MQYwXpHpyrYkr7lR8T+uNK2tPSY+mD+on8rkEc6v3F2alyDGDlegMtHABcaY8PJCm/PsWW92KsSGNIguVz6P00D9u6uUqZM95WTGBw3+XQoIVuRcexyp2tCpUd1+2v++ja4RslYBbVA1nk2Uxf6yoBnFeFET7Zo35tg1S+vnb0WmzDoRgYmuTPPYB+UPc5DfUcSfFptGefNGoqI3/DuQu/baNNKVyQ7dpU3CRtoJZgRU6yA/+MqLS9WCnlqy7+kmmMw1Ff+vofajoi5+IaX80AOGzNWA+sA83fhUDjsNUEujcjzrWxK8Sxn8twQRnSGIbhULEkgOKzxsvKcEb3rhjLgpkH5aHfvoYrFKbiO3fgTBl86gGvHVNvkBa6erYtr0Zu5HI9ii7dHi5zPuDxsMBnVECbqbihLz3vzlbANxZhtJeT/r32Js8NfbRv20RjLUp2Eq1+B2mBT/YL3MBojaR3/gT715WaDQfJNrYdA9DUmtzOadOSikQsF3GGubHXYlXDh3pc0gLCo7oR9kclRK+oElAtiCOp4gFPIGUprHXZroflQbzYIgImloaVXZENnJDWbFI4XH1WPwQIQWMnKEpTDMh0WEXUmxDUwooF74uYI5xBgoTfzt7QG3q79olHniW6RO3s07uX1u4368IowZcvrC/CukKiLkmCGstVHn7FhQqsUbAHinML6j+UPfFDecVT2nNiqGgTynCMx3CSOld74LShGlwjt0OpgtCW202HKANxIC36TMaFqpr/a6/djxUqscAO58NRB+Qgqgu213Gr8fVhZfjQxX9sHixf0GNGj7U7yBxRXQqImd/uz6d55NYmHfLqEA96+baEOBrInAgRP5fogubWCesQHz64cvAD+tzAopW4b39WwfPOjiU29XBeZhyCVGNBpJORR1cwCk8V3YMaUUDE6dJSryCaGn5j6qrF3u4tuWtM6i7ZkIabh6hjOr2Yi8nQK9xFPDQGMacz9Y/UpslgC6dZL70VD0CgcioUFfpKS0pxe3PmiY5yf9EPgC4n20XRKoHINY+9o8YkfoJb0x5fUxMFQaQFA2ofswCCmQAIgGcS2b3XZVERC47Svi9B419weRKM/Fvo7Ava3QAjyOaOsHTrjRl3PWbtL+0OgzcVt3NcEzEc8q3/bpYGX++FNtBvWRJkA2MgdQ80QpmjJcmgBsilp0zPKlQSuBqzokRUC0mNAnso0CRKkCSPOp7yJTr2yhYZ0gr+YtpkP8tFaGvikXnR3Va7mZWg3G4GO4UiDirjJVw4F5+yI1To1P4n7N0x4/32r2rJ8h3BXEMbn2gif1OtLztk6stik8xJ0B6i4TetcPBaXliiAJWJKMY7thc5Q6b0EwvB1i/8L3gMDOLkMHvJbQyWYrPIJ9IJaLPM4T2/K5VZXRQoZ//0t3CoDT9LkBrIr/05MT3tzduKD0cpcFm6M0cYu+LwJJ843E5ua5YKqgPUBNJ45l+e3t4ykVYAdBt4N9iKOChIljpKCn/kIiyUsGeb+UmrdY3kEYVjKpn6Crv27Ujj8mJGeLOwkj8TIHMyoELve0im0W7q1fAdW7NXb7yjenuTg9dMdtSJQSf8Aq34dgZUWQj1Ph54vAyMoW/qajAH5MqjtMu97pUzGnHoDLGuuwC6njjZT+FnmGKQgenDXbmO5RtW0GwQA9IxX/j5MuV0cZrBK/Xw3nvnZXgLHNFB3dT3w+fZQPN2KDMw3x22ZlzfzayMxeAQE/4REqvIhnpUBuIio3O4iQCLUGsEoQufyicUaHtnSZc9nx0jbXqZIsR6ZWT1lbTEeAYIooIiUCr8gd4pTwCCGE4hjgUUGU2O1Iotr77FNuTTEBaF+HNxE3tgJCqIPsb7/D+nxDC79d2RMSZ/pYeicsi/Fz978oPqdljMR7RVqa4nHHrVMCKYnULaHYsRRgdkh2dPDZZHNTt0DNKw4wRE4ypqw+T+K0NsiZduRj/YtGN91oNkac40BQPwH44xYfkKfXL+hGAPkzZgO9f8dJvxqugIXjzF+qpGIbe7md+kOYUGE7OhKDNQ4S720FtVD7soTbn59h+6ih0+ntPmcaFabsYCvBkNCcZejn7RxiSh8LgqPeHAVvoFCdgQE6UBeGKn3qNdDVXUXMJovfgmAf/j387RP+PLBmmf1t1/yFS5F7J2KaExUBdzeqPgmQcZqySEsCgJnUkRrZkb5wBxcgqucjfgGrI49lSN926Vs0/PTURuXR6osbXq3xJATEx6VQDNc8rlNDo8XDMwT8RdFEDvcAjQneJ9GYJeTIuZk4TJ4dNdvVXLkTfkHwsNuVp6damXI5oFz+gRjlMZ/h9xVmlChDBKxyEkS05ZWLMRXG8WWgAHhn5xoTP7IW0akg6D+kEhUCL6MMUvANYDDy8csGumQxjF2gdUqweELG+XMwuzwOn2uQL6qjTBhZ3GkbWE/gmvR7So/1eBBabpvNtjeDrKpuDuwDRVQLZIjrZ8DDwWNDu29CDf1qf848zswoNea8yAtbzGd3xqMD684NWNFnayrQWdtMmvgayPSO6w4tKiDGEOpaQMxX/Bq0Td6jPArUuyUs79E5zb+H1w4TQccgyydzzToqCbZfwn4OVx5u2hEFM48pfna8ErKnpvtKZ27BM3AayeheAIsW1zCc+nkr1D0ARCkp0gVYM7CFSk8cDzZx/xAqOoQJHqe/kBZyRepGalygbPohA3v705Z++RYIwwytBiiv1poGElSNQDWcxatRBSkp/uTgmAOGLFjYr/AxMNwTam6AKG4sK8Kkj4J1OhPBE/XR49FyjW1wiB3ewMihCWIU6TNV6moKtIRBooPAlBbPHr6eXm16XJ+HTFAht6xWUXZnlCR6fWzH5JP/4m9QbNtrKCUlxsfal6DeuG7yTE80DwgzQnB6vgVdXUqpecGRkpoQbN2Ul634AjZp0SClKAuVrtUvfAqyJ62WAMLf2yrBPnIu7uJpNCmBmDyw49VkuwfuaIrkX2CkddTr0YEZ3XPZ55YiXHKA5CRNXZH4ei9Zsj619TM927aJWDAESdDN36BWJTJZ0VUHMGtz6BB1MlXK4+z029FnTGEzyqJiu05RvhTm3aVYDlt7ItsiNN0QqBhzIIdNmFUhXUZUHR0ivtbnSK4Or5HubvJsTQNflk59quDFvcIi8dr2JQLQNQO2vfJIFJsRxUee6ZND3EOGGq5ItMSIeK/AvHhYo2O75R6FVIA8nkiAackBlrfNkKpGgH/Z1qUDbWgkPvwsaUIQ0xOVcaVfzXEEWKj8ZctdsWhOcUA/qhegxpZp5FY8WYzWEfFm7Q5GCiXon29KywLLe5JP9xJpVfjGLf4RUZqgN0l98M2chlc/cnI0WT71bCAakEZAMyNcPpjKBqHVHqYjwNavUsPM7PuX5/f1S/btpZeAw/DvpqLjR+356PDoZJLUsqAG++m+xnQGmUsFLgb7AOtFDHGmt6oWBu4mQVswL9guQnOq0d8fICtNB9G7J667yzUWN8c8tFFmmOtFmfDhJ6EJ0aNIdGhAKbkkC9pLS7MEibyrXoTtv8Y5Bw4JlvTV7uHMhRnG2vJld6O/M10kcx3k2qLZN3IsWNEUmJyXMYhsjI82mFk4uhOWWPDYinB8G/AFvaxAyHFcw7uKk2bG3R/E4u1FaBaWgIqSAWAhn6+i4PJeqNuZjWGEuLjDsLrpbTU9e7Z53Q7646EzTr8yawSyF6qLeGQRueytM06QYW7jMtbmHjEQ/N7GNEDwSAkdjrCco+1gF5UHbbFUVSI5VX8yuk/5OBi6aU9R7ObsfPHGU+yOKQcOqveMopug4wifKaVq25psn4VtM9QmSfnY734ou6sNm3bOatJmNpcRHAWfrQBMCD1xYdnyskWOlSMvT0EVS20q/Vv+bIsjuMkMKWmRuP0hZ+noz/wotN2NPrMfOzz+aivEvhkrz1Vvk0X+J7NVc/UlhXB6ntuHLU8zmPovzc4GKIQhH06o4YFrr+/vOajBDlkpk/SKRS0PlvUJntz7DGHnMHtvYkmhhHjq3voxLgRw7ckMgLreo5jfkmU33wVyRsSFv+d6oQbRjfJx/m/OpOLVZ8sQF+JVOYRszCRIItXC+hSKpOh6hpOfBbGdQcJel/yCbwFdAqw5ks4jdGzBGXbtKUC5V41B2Be1CtO4X3PhyEAipvZPZRYrPW+nTQaPx4a6I1oQUmY08ND1YAyUmM17MTn7yCnBcUH9/otBDxswhP7K/2yxeCFpih1z/voMOtWmaVz6gj9go6JRXSZkb4VxBASfAnc3GoPNNk2yuNTbKuqKL3zbIXYYLBTzbm65Bk4Crz+qCsI/tJTUJajbAN9EHui/t3/A9pE1moByxNCY9SviwYG+zbIKhUxXFkY9BT9ZYUkKs0WKV3Wn1BLkiL5IVJIj4ha1QMsw/VfjnE3jEUfib5v3BTl2W/DHUuy0MX9SVA8yWHWlxsFS4bMeJa5vZhkAfkcF3V6mDmpoMmlNc3JzE/tUSptgJLT3OrRZXsWeltrIroLVCCoNwnNeSKtKxLjl6dvoCvw+GK1GR7FboyTgEvE0CvLcJ6GEAK/oldcwjn20TfB0F4tO1Tlj5aa0pzELmpppmNW+JOSNTyxkVXrY88XfBgAeCPXHMHvNSMHjdL1ZV4ye3HwDqFzrnuS6AupEMmEevNGSiqKLxdw+BZya276Tx1ZEBEilJiOTPjKAbTVx1jZMpIup8oZIWqAN6ibVUssx3hAdfirAmgq07u6VM6cq99JgEOkAnqq6GDkc5I4MJBTbXvG0aaztTPIa/BA5FumYyBc0iGpi7JKI0yNcgt6YAlPbvXQr7k8AgBfXsrHmGZW2Ay9TmI8GNm7OQUreYavJFDhstHZOEexkzTWu4ixOn5D5oDRPSZlaoV67RauKxraReDI/8zHNozJrRHfKA2m9dp8pCb4Lh40FWTzqfgaAXOQe6OBgUlM/6iUPUB27a6pK670E3ie8NEYx3VSrCGz2B0W9FMZObpBSvXmRhBUQLbE9VnDt9hrJvzgo+HeDZera7CO9+DcwLf4F/umblI+9+FbF4yceJzywwg0H4x8LD9hwECM6iDrPlyWBQCnP04BQWjHSH37U1cV3S1WLR6jvTpNNY1u/cLfS9LGkmLPmzSo9rm0hst9JpukTW+mHz1jRHtIGJrg2RSwzsuHbzeUSoGUmZZtTpQ09HSFboWDaWqfOrf7aYV8tAG18OanTh0edUzCG5OY9LT5/5HualcsAAcQsMlDNgpxKl4kk8eWrfHIEM+icom69zB51yBjMCFSMHqNlcu7d7R+SdsGUv4/ZPtLqV1U/Js6B9fHt1wbWHyl2BOsgARjH0wH7x/gBDeMzM2pqxN7NavqocjL602s5X9slxtfc6aUh+EMQFJLO760sKnSsJIwlEudzTqnF0CoBoaJ4tDC5dwpYxrzZLCbROz6CGDZlsVnVxqXMC/30/+7nl9Aa2dG6HUMUEYhK2smArp6kKrpQwV/H9ygAvCynH/PZTZFi+F8ruAmrcFgOtTavNngqZyMHutbnnbwsGksDhsLWa9IpadqfLNWAkhg78RyHs8uKDntkwjInAYbXwTwpRgkUbGBVRW3R3iZjNI1Cjsq1PtvUZdTpdGzC+mFqkYGjLJtTvF+DmFX3d3LxKyyzRBIXGxonhuclxcqQsU9C/FEXP6oPBbcblE7h/TJUjJEcxY0t75r+CVoXviFvZ6VaYwSpWa2WVY387hGpXfn9oDEZvt8We27MkAwytyBYH3RReJlHczuzj1pHBnZbHWRspimF3hpZRiGzqj8OaYUNPce+4fJWO3aMaquqICwY4p9B+zZinc0wT+6hwt61OcrPwfdnDepnunLqplNt1FavsbP+9uXxVk7NYUIwaAfDA1Iu6CjKUPwTuhHT0Up6/z3mIRmr3+GDupkpUxhZYvei7R+T8+XHSfncMPbEWWbnJ8jJv4CtAeoNWFBLFGY0/t3hRxm7JSU7oz4FqriBHrEKgknY6xu3CtKpF6tG1F8FMpuuMxbu1HHs1s+66B2K6q+yGVrYMwVgnL7OJyOsy83rDyS1AdKIsbAEbVUVnoZO4qShTknAyvdil90U54TXPVdq3QSKKDQljdgKMbT7fqCNmvL5LQktR+nROIAoEyoCkXPx4CV5PQ9pzT+PJdJ+8bu0sHmU/nJj3XlrTogt5Iqkw1Klzi3QEUvTcBvYUQZOoKP68N5CQlRhcb+D//WqCXObZE8IALN4yfq78hKjGNHVq+tUf98Z9/4NC41MwlmzjT+phUdpyNchtU6cNvi9zSmjVg3TOjLKhK2yeaBHH82MyGG4N+8XBuW157wF8RXl8AxbWrIAtbPVwCwlqFzhGZYzpjVsl7likyLMrAU9Zq4wLknbaKKo2nKjprWc04iTOfpyrPdp3godyyVjRRL0+ovWei5aZ/KdvF2deZ4Wof4PKrDK30se/1kyfHAZDwUqBhONaBlwlVVuMOKWResmsjw64Ioxjhd5Gl3+FF4LR/Ceztu3Xb1FRJFUckSq+J9PpA5W
