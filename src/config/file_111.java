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
