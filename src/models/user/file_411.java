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
