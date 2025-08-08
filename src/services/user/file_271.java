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
