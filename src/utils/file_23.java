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
