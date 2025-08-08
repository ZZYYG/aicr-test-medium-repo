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

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
