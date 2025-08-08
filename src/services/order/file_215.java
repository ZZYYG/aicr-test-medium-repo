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
