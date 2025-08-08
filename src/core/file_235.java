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
