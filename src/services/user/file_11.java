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
