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
