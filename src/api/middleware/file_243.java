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
