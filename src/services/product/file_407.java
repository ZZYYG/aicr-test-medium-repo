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
