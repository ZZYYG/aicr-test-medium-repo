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
