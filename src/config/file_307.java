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
