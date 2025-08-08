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
