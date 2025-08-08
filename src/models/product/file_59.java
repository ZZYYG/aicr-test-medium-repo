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
