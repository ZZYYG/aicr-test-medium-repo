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
