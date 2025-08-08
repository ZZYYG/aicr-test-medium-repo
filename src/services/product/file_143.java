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
