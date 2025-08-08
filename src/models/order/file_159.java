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
