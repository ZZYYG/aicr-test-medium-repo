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

MK4obBMAQs9CaeLUUSBiR6K/C5DOR1erFKGqMdooAkCQpnl+yRafMFl29zRL/Kc9lbLHIX3z4cruANX2rjC2SGr7xodJ6mbK76ZH17BQxCc055LNMvF98bvA2Ryt+/A3PAMxrWXySAzDRCwxfK+lud9TuiRsT4/ppEMAiYDhrD/m3O+1gc9wQZO+MDaAsaa9TrqD3IP/HhoIUxAP4CuAyttzrq7mAafRKzFljNQ2t3aeyNBUJDtAyTu3OHJzgapbZFPaj6tbjAlzAZZYCJ5DI4KsGB8FlIm77h1PjKrwpflx6avKAFCbHP6S/w4v9o3ZYnZhlH89eT2Kq+n9XzoWdatggfFAOcBtvFI7VzePPlmia48dElU1XoSnSYbtgejKHJY8VLB22hLsHUpHZGD/3/ceMvVBBPzNy8kfc7dwGX/DFlu618mIqAk7VZ3wtDbHs/KbJ4F5E0UvdalzO96gGrq1QvmPf0bswBuV0VPT1tszAvE834vAc4Q1uUY5cN2wsGs9lawU1931dZjjbuQziawRkoxZayN0DM0Nvei1Nigf0P5TisTK1NVXlEiJQAu1Al1kVPFaQcnqAkJtcoYZ6sDaBasNvjU/GBO2NmYjCXCZ4y8w0w4w1DoUhvG78Pk/cyXAAlfjfEWeg8yCXEgO2A5w87nlYIVoHnA/aXNI4x5H8u40aKSTpRinTGGOkNNqNLvdKgF3qT7bMOK06vr++LktHawq9V3A9Nvpuw9Doxoa5kbGftoakQDzs24ui3X/WGgVNzcdO6mh2N6qRWoJKJhXF5RVqrj0nDiZvumFD2/pxLvNMNpELwaEofDS8qTEt/pl6b19VIJUFMzEV9QOyb/zRh3qiq/9Hee9l6uX/DX8Gxc1EawOiB4JFPUIMb6rECiat7dphOV0iEny2JcCh7zuGkRr3RvwRJXUvQJkNiyNO9lha5h8YV8cs+JxoBdpgCcNI7YjqBUIrwxAwj7Y/Lgl/NmJo/5jqEPung3C5OiEFSI28e1m14Wg5HIyz2nxvpI0pQzmLIMgNtIQrEJdmSh1M5RcCABHrNSCKIQOf778uYqwN8/j9/AHeBcx0PZJ+dXPNm+Pxt4RS/8fp+sB0GHvIfw0WQ0rckpwbvJ0BlTKOKcqqVinJQTPXcPWBvqT6IYhLpqbh5CI5TcFcZuEIj/ysOceZE5VgM49Gbqtpt6gGV/CMOAVU1Upc8qoK97nMGtDlyQGl23gfAF2Gs+Fyb9FL7rkpsjcv9TRqMRUuyA9s/x5j1Xzb99x4c96KpgiSGBoyHLWDm2NW+FFlQ3RhTafhhuVTxd374Z7xLrZ3gZEVBgmfIT9eDIbjyrJlT487ev4I2lsQaere0/kgeALDbQvsYqTibJzbX5zI8xBu9qiBOkNJ7gHjH/pV/o+oMeBa0Jqb+lLEBfu0lNqtdxwWE43Rn9Vb1MhACr7qdVkcL8SgF80Vg+t9zdfumkTqezfj+UYodGEbD/ppWgB4fxhsaf5O+0jrr0P//mz/USu5D/cjyOrdZgXKr5jgitSNncnaepCxuNQxrHugMuhtSJ3PWQz9EYwi2IRza0wXnFEU/gKUwlIJOeOD3JidonbKi/bbMb6UZ5w9dSyg88Ff+IYCoCoKWDuc6TkEefVacbEcyMnilQSes9HMLXSry4viG9egjIig16uujRD0b1rEuOS3vIl/v1C4IMQp2DFh2RpbGnVGbTSH+0t8j6kA1tfnkYzWJ8qGHpw1f38I9JM+xT1RJV6WgW8YMB8oaBh80yCOPdwN2jXFRhnt86QVKykP6W6jWnBt8IcJvA+f7sH/MHZH5K/qjeQDBIy3oMdVe84ZjfFCdIgJB/1U7hTNBHQplnFMWFPty4ZawTWEHyWnoShNPqh8wdhRleRermmETPTxDm0f3pqmFNGfwUYC1PRJ42QOmZ9rGZMjN9XyPzcjxzZexUVARaJUvPXp4lVZlczg/Yq3v8OGpQvsXh/nFGeczZ6dFE0OJ4BKLNq9QoE8l/h6fypMcZURQGZ48Ct/qCLiI8cUiN/NTFmHGw4QOEUjSC2M8fanZyo1Oz0Ux3jIh22/FV9IiWN4yma5uRKPpaJXOFnTQXTrwJBhG5P4J4ATo5TpxI5as6M1dCEHtKnzNjxQ2VdrKsJE4F36PJ9CA/QzYvDUahnVXt3nc7x4fXlIgaKWDn+LeW7VQSaKmQ1Ddt255/DeywufZf9w4qP5WKeUEelqGn3OTJQ6xmCuhLXb+Cw9XU6Y0fR8YEuzY2e8lprWP25ReMWJySHAiU2KxLSFdHDjFu3RC8+fX+KDoGwQhIOahd53VrcIV78zeRdMJXFG6l1lki4r1QTzDCnAc1ibRdTFZJZ9DdB39Ubo8PyUlfIev4fosjlbUbl2YjFC2fpm6P5akW0uRmdDjObomQl2EXucbXUBZQVuHeSezaUn+vYebbX5CXfoPIRzqlqrvqnJjp/1lK/M7xgXo6rtkMsWohHNvuFGsmIh5v/z6ky27SHt45eROSgq3tlxFZQIxvyvmleFnTD8xB9gjupMl2tACv59TqUXX1ccKPRXo3Y2NwpPp89Dl7/A0tBr5vXFFlmj/BGifB/Upk0xduOfQQ5Zf63KhVdEIxc4u5magn1koX5X5CrjsR3o9srW5XsbXmKz4eJqN8nWW0W4DR0qQ9DdnEOsmS8abPK11yMbtq3rjGRoe2QD5F6uOA5Zzsq+eKdv4xNiTPq4eSKX/oVYcYiDJ5mrj/pNzcfgZWWu9sJOsH3Py3qWQbv3tZ2aU+HdSnm4lmWV5vrYGbU1yKawZW+WLylI370zAARFqr+t12TJtIIIzxNGA/hBwwhe0Xlsq1ablANxLUUwKJG7OSTgxO9O/84Xgo9aw3pVKZAvzbW7zIAAYph6NOs6OoX2gOalmNEcp1fAU0/paL1nxeiPhJLBhNJeX84gSQvifc0zQZbFvLONDH4oM9Tu4KNW6mI6eoHTveju6pllLzxJhu/9G+8PX3INHwUt/nKjRVJiV5xX0CU1h0jYctWsTAncBb7H6DNIgb3CwNNR+0whp58TLbQGpQaOWflXbrcbS8HetHOpvtlQrdIFi2z6G6ghfnWRnLUSC4kZummLW2ibDXpDFXBUPE8vw4NAF91KBml602PhegSjB1br1GnsN/5qncBmjUj+KcTyv5i6R1jflf1R3PqYrpHQVq8OfdQimL6ELzXf5y8u4BworpaVnH79AKa9keGJdOJIUyZUYNUFk5VMQSTr5aPgRmQ3wDK9xeZ3LNzaS7s8q+MtUxl1Q0HSugAXWfqKB05BMMWiu2C9B9hh8uBPRPNI3Qg4ZkHa9R5mnkgCh6UAKrIjwIIa9mjLeJQ0OuAHsGmqQdyvqbgK8k2MZnR/mo1r9/OZUeoosNarAVYwlBENI3dvYSi4h6g/EEQXyfFoNXQ+WWLqP7lJQFxsLwjr4IaeSYtlHnX/K2jiyG57YAceusjPQDQIXvxwNMM76VHRnAJqoU6qhIdxp0qDYHbi9MH+v7bkiUuR8r2RYg0XnRgoShDIC26kqBfjAIqSVCkw3+WCXQI9B5aYe27rR6lO1Tu9qKnxhJSmTKpcR6bcNrV7Z24kcJjETKhFr21jjIbEXyInPvoqn1cxPiNwkpcnS6F4orG62Mpq3mRLcYWOmXB/Mqi48s5ZpDB6/YoWHE4jqHqRIeYSWKot9e8rZYKe98ChOObenZn3yQ/xpqwpSKcgEv4C5FENy0VwsGg6RxhmvigRapX/RuOFwqSbnSW0p1pMPgb9mLI3ya48vfOpobl3erUKxqbRo55xI4hA8oLlY2I88BHzDMbbMhWjN05+ZRVfYa71fzmQ6K4WKzNHxKbpdmz8vrowZRF/3qPjD1iftrY+CPX5+Bs1FwZkoXn3I+VpLXpChp3QiYomfVYzXMOtA8n9b1yBEsPwynC6vfdsFO+3X9CB4ahA+v5npFux5pqvXD35J1vRpj317G6WU3jmOP2ZLNwvY57SpyTr0IbL8dWY5H1EXk+nxoAUGBbssLfa1RbmEEAGJK8voW+3oq6kBgZ65PMFWk7Cf3SAH4FYkVZtfp0dK5KXpFGl3PjmbrrGOwZJC8Ko9q2impjSSCA3/28hwPAEp12TOF3fyVkPnSUZzl/P5wys54wp/iqp/7bwPY23l3pRzYSsZ8aUwz1iCwNv6JTiocv/iOzW7tfMSPiu3S5HBNPQOcpZJ6uqc171np6fIfxVTarufJ6YfWbY0q+IsTiW3ay0E3uLvZgijkci2uKc6Jnia4zYHUqgxoGj2z/49YN6sEjsXs9mEGzG0bch8TryR2SeD4vow43nuobzWmIOoUnJw+rOnU1GyjwbuaXOmxBbvSmrkyrfi0jhGJUF/mG05LN5Rqx0xwm1ztV+82yCIPHlVWqs9KF69eUrQETdhf9QkH7g7Um/tRTpeH9l3Qx2t9V+G4ha+kWLNVwjpRhORVGDMMS5X7Vc1rdMuMKzoNU7NXYeSf+yveDBLmQeo+dSdF9R4pq5tv30XGkMocxkYqvadaVT/HVplQlawZp0JQ+FzscRo0WJdSO6ZcMRZasTFOpDS4+84VFPUINNqW0xwwPlBuuVCP3Ek2Iatw+GwZ1/z2r8Tn5yKYwIXTpg7KdfLvpZ0KK5xoLWocyBmM4tosgf0sb1u+MSrqCxnuovpr1+vLC07lGZhF2ZWRpeWOo8Fzh9/emMZ+1n7/RQEZarqOLLmCh7gyvat00OsDNg9gs6O4OKYYGgoSb8EC7dpDuFUm47Q8FLHx5RzRhGMCIFTavIootbkbvpDhBVNB/GmyI9CAhIfTTCjnmXq3DUpYdVguuUV1fsQbMQrPJemX0aUmuV+TKpZbYGtRb63hIWjDjNSgPuCXV5HacPfpOeAnHGcKjNmvuIZjIlDFUHGnHQEcWsTXNZHOUK0qDGehXwBJ9YZKINP8FkrjV9wLba7dIPzJTszBFxFlhKU+1qLsLzyxJu9nb1YthqIHycrX3zlCVtiSHcwd0FOhSLfwkyfaMXwpaYH6XyhkcOFqg4SIFsSxaI6v3S0l4obVJdvBAXAGd7O+HUIRjSkiIr+KXxkA4CTaOKmEzfBVqcwEcdwKHF6ql3y5TTB2Hh/cGZB+is9CHXXsy67WizrzaWtWXOcyOLK1X0RfgDYrlZDlbZsFA/u3nO5Qdmhc0mBEFoNe8JQg/Ts8EpnP39VZEYmnNIcUYFnmA3PeDD079wUVtLnWpEy24oceGEA1GoNUEZtNZm6j3r9XDXHYKAMBzsadpkV6H9TE1yh1fuwz2UDr73BtxgR2AP1UJupMpWr8NgBSv696eGiH8iINOAPvwdBkNbH9lmdTaAx+bBkSbrps2L+EapKhTeLto3CRbm35p9CAhkVnJGWl2u5K/xWnzRlP8TRIL9S2p2o/Glh9eNpeePcO/Y69+mZz/H1x1DSJOxpIyHLo3AWghCJ4urNBZrTF5cd2n/VH0yrZLyH6CK/ga6dy18jwU4KzDipIwS2B4E0R4wy20OGrVQxVh/HQYHS7tVzTutceellnzN3T9KYyte30gdNojspR+JGI+O9dTacJ16dVUQ2OqbjIbX9dV6BuZNP8dSX0RSIxvxgcuWB6475Z2shmjpXCln+QpCYOkVITJt8BOeUH37lmhqCFE6Ja5HxKnoGmZEBEy8uwCsniCaWjSo4HH7dFh2C00uJxjk/dc4ME49/c7/l/J1qBA1I3dT7gEgXp8I7sW9I2ZnAWvomXT5+yvhbCFcHYv7kyNDqP/E4PQFmM9MzpQ1bk+5DMrgZAiuNRUZB3Hs2paFc0XlxkdCXcL4aF73flNaovHeAO9xMtaEhr1OI13OD/e+CowZbn1TwPHiOkKbmNuP/Cq0BiqF53SdloNzce0Xx8SyraJsiTe4lV4kw/fDccWZG3hFk1eweKOY7mvPCoojRKH9tFQifTYjFlyhnfWfegUzuhyUYTrhRsuhylstV4Fxi9nNjEr5lhzaYQUR0ckksCxHVaOZp/+jwn/oM3kKytAh4ZAHASVDTh27uF+DXsy8q9SBLMOz1gdkBGGOBYdK7D4BVi2BdIzPUJ9ZsQa5xjBYBfAIwMtSR6QvBu0lYHqoa3ZTosCVlW3zTSS1662QUJlQUEOCCoQmiIRRAUG97rLTlsG8lw37H+BsyC8+/YkuYFujmNUjjB+tgFUU0LK3xWcj/k9ne8ZJBaDKsr+XHy/jI1Ztp87dqWZ3en3jW3BbWEoTVGlgohavpuhvMrZDkglNl4dTRBl0sdJp/T0lx178KvqvATxOMdaeQRKA7S9sOZ2S5K6qmkUiSt8rBramoLnKIKOcAlD7Ln6ilytFhzxzvJSIk3ujS02Hw42AuGZP9oFF5onwCQ13NG5t0jo6kGE7fb6+PjtisuFx9sXo/tHoOzcfTzIIntmoyOX0cxeeXRl5JelWrn3ubOYEVprwEaRUbvZFbjjW57MYfg3pF1nI8UeVNc0oscEoMOL3Ls6Nw+EA/+mCI5j1Mgv9KCi8HZuffN3zouDfjs0RL9K61UMhy/ZvJhN0lFNkBcYqSkMyJ0g0a/GUtiAHl1sdOv0jidtKjKxT26bRVtExu+JhFpf1vwy2M7tNJjet+WjkPPAIcgGyJZQrILZ8QkiqhL4Bf0TfSadTiUZ+m9bJBGqosaS34rt4rUqQZAGnWdfyQk7FIeY11bqTnJZDYys5pphbFl3Nfg/9MNFScSpIB4J1U2W35RHhAcwxbS73uQAehGHsI7Ddyu+aq9LB7FVM8aEJuSfbDOqmfRdK6tNxoPBrsZ6vJsZnjhvFkm1NrXsE1zDDY/kSaihkt8cR3If2ZS6FazwGT8W+A106pupSkYAqtihMzrTL+3LOPn/Z/Co4MQk+ZCh6cHWwOzJceldAuTSvLlza/3qlBPOL0qJtNzdEOz/yUdpSoVFP5V6GKKhyhEcpjlSSA2ql9nKvbqazreJIMKkvhkGu1bqTcmr6XyqakEY455OA5x1fe/WlbZQswsKFY9uWTCedXi/lgAK+WKSiTJFGHtY+b9r3jwRdpXLC0CSmbsHGpcyR/bjTyEqqogNx6Ufx/flEeysgIwDyNZTvdIIo/aXN4asYcLZW9uXkeB412XuIdgI55ORlfmBOJWe6VraQbXmShjSqFZAqK/AGjH/wgnUZpPjbUMXJzhmpz0bMFo94t96EBPEtQw4BYs9uclJgkg/MNbWr267pXQ6Q5gPRxPyn3vmJQhCohJkhtZ3NqqDcU14DJhupLyP9/T7r66OGEEINgVHJg+7stLU/H8l4dc4EENFwtwuZSdoyiCTnJ1jVzyYX5J65NZBWzLcaOOOLOkKvFw4JITjl1JRxPZtLtGmjqyhEdSr2OeybyG73rfmzy9XHCy8223xAT8c/utkVvK1bk0NXyonE1cvi6wfY8c//LSxH3vIi8ow6HPBAouCJNYLuZHROfO1IlsWsMNv/0VfeFSBPNY6BaTfFaXEEg07gSSpcxYQBhl5i+pfyT7NrSSgTcPDB30jqKg4bTUansAnhoYRcAZx6GTh/gI15tAufhrxTilqIfeseQRilLr3jNZk36IwccAlU1DrpmpF+qJba2tjheoFK6wNxCllQE7GcfPmNSJMeWcgvw+0lqLJu3kPryuqX4AgB7DPxPmSOP1/JcKaQUhRLnG/YkBB3Eh8Y4BDQjT+1iyhC1BDQzzph0SYeGXkaBTJSCBhGzLnGI5hxvoJNhCQAZ8//T0Z6Y+L+Z1Mkf5muW8q+tOQa9yAzZnwqbuZc1xGgo3HgcClIUXKNZCyBiE2LDq7MJ+QJYHixDw5coAIaHLbpiA5RWfo6lVho0QnnUIevSx0Qr79qqAnaeyUtWZDM/A/5cyTf+8vAYNDsUm9MItmX/1adIUQHeQ629cilXGUDeOsO0K01GfLU5aH14G46Yz8wSNmGxFge6EZEJYGN1LV5ZHRfwXEYBpRcOJrHGzSyxW2YHmglYxN90TAS8monEBsjilCJeyJMt04r7Io8z99/PzxsPIIuK2/qnm/saTb8piz1liDzSPC5O/sF1/2gISMqi+DlNn3Q9bjyEeRuW31GAN1KqEuQG2ed3eSI8DZuvWf1H6+vhfMEV/iUBgmBmLn4118MVH6LflRbFEboYsSuNyD9Nkxn+untWhFAczJAwDNO1Wj4S2R2cB7qy19ZlSVVXA2Tt5ITlga2nd+rpBGD9H7dfGFscKF7hd4DNVszaE+o1XSEShTBu11VNlaVm9hiWQ9xqBsZ1eYDN6oaJwUE/qgJTTFEZ+LjY88SXeO8JrawHwbUAC3r7s1OJoHQTWjSoGYixU3Z4n45Tea5CiMZwjFl80BOCDyDlZHbTvMYqCtjJoQ7MlbDPU04wes0QHHH594r9t01aPI7sgbkazFpLJc4+2G1fZGxIEzi50h0HMvqY8FWBfkNjmsciXvqBN8zTb0j9L0C7hGSc8yyGJMn+qjQ93VpBp2h3F8c8T7IKqa3QjH5Ivg27WxqzZHy1uj/CtbyKjbe4HsGTDZBwXahnkPwj5m0BIYtVzgof8mwzHKX1J63mr6cLQlA5u/MtsIEpL2qylx1XCAU3TH6SBfvhsUg4rTLlRxfNkukfuK3Wd5T627iNR/r5oGfrX13uNve9fdsTpDt7g6gcvKn0i0j2J0g2hWhWhYXVae52OS1lJLcaBJtcNYrEyguAlmhw/g1YVupSvKU3KtAxZo2hMtN74+/nr0ucj8M/a3VnhYVtrN8PAJNflQGE5TdDHa5cARZ/onpnS9Dl4ShRx5cKJ06pLjYIDMYjbjH+xUkmMPFUn3/1QGRb7iTEJh1+1DXvHZlqcxyJ9H+FrhnyA4AhhwU/FE3JjRqA5f8j7Lx5Xiby3wWJj2TZ2ts4PKNrAygPZM8eoO4l9HHxZgwxQC/kliynEMKpUFrNnp6yJi8yqcDR/GM4zjs1JimmHz3lVPt6yj4w2++Nrkz29xVOKKDAQFw9dWIn+h0TI6OxVKbJcpVbRCjE3LCbOxVNQTIWVQ+4gfh73MOQ7h0je5CKyqPSVDuL1on55GwKgXMYyJ1csxkxeoE1MVozO/iGSyylsu86GFUC0ol7HvhkI9lByo4e/vCVslELoEEqFaoAy++JHK87A/ySC172o6l+SPFNU31ggkW5jPXvUFuKdwMGNlSlv28AJ9TAzRWscGFuaPXTlGN/HwJib2YiuSRQgimcK+O72FxPtdGje7Y3+s8Qc0eu5zl1T4RIVJQpHwBsunwJ7rA0VHoWEvttxOYa2DDe0mURYIJYpa6l+SWM12lWkuFmnWci0xrD2SeiszeCweZDVhvsCVi9Bhn6UCnvWcJpeBJY37zWNe0NJLVNxXQL+b1F21UPTj8+b2tShxOgkcdh9LfjOVgIqTBlUENrIcfqBA4U+Jo/wF0FphvxDzf16A/G5gBTP9gjSk7Y3mvFRTG2/qmcZgpnhhK8YV5PeNY+rES+juhCcoGuMDBn1lsumoA4zI6c6l//JQ/Z12lvruqR+yaBXREWZ4TXgBo/TXgtcfxjdSnqVSkT/romPuts+6sdGX1yJTzqxUHMICtp44Ej4qHgsa14xvB3j+4KXZ+gSExCXhVBoSBjCcvb2GTho1aHkukpXYQpXSHba05WaMMmEBufPS+YP71fL1s8KGMiqyz1NKhyvs9vvUJ/azT14G2+OB/jfVWIdtlEHyO9KW218SuYWCvdecMBihE5RkgRivSiwaEzlE3bLhZFVjHdOc2ps87vxlbOXB4BJri6ujRfcqQjK+mNGwQCQIFdZC2Gw9kJRQ2dMVNDabVQeK0PD58DuoBe2hsJ/8S6QlmWbsqe0E43JaD7Mr+y6UZtTqLLmXjwcWvFDpHt/8DirKb911r8Gbrw5U3wjzWlw7rhMD7XNegMSfnYL+R92+U4NtCnnpJfPNCywFFM9BXid3Z1eh7hDIdkjyhvQ2p5ddcsaJI/p2+dmW5W5v4NEEJqcX68oLJpNBvns4sVSKulByQseYG9uRjrireZ1KeQZ3mDpQBb8f543tVQ8H4cVjjRm+GoiW/2SLYNJgV3fZYf2qRe/+SZ5y0/U0wGcuQLWPshBunZJz4WoqCB0fMlx5xt1wEPe8vv+ELl3fm0BQM28jHHyb7rk8TtFHWs03Xcx0Uppo10Khf3YLVOssEQFXOVBnF22pdt7vFNqZ7NMjSSfwmfT3nl67WMoisHPBMlmMuYG8/2TV680Q+HBhm2MPuBRFzfpHKCjOyCsCvZIE8ug7FnRNuJnDCeuSuuil5PvsMSQRWVnZOfMTMUNRy7dmg8O6CQWrMlzIPirBbvsbYP23bPooujTJ0rBuckf88yaySPWK2n82SZJUDTz8xLzdRVq3vc04UJEHx40HiKZDRKTENHSykLnFdbhtMZ9xAkhhmYeZA6oZmaCFekJcfGgwxM4OBmWuEr2+0hSgqPMFlAyFQTzsG2NJdM312KDwdXXl8PEuDzzKTtVCqQhussogfVaAswxmEssxdg2fSWPnrAo6/bGDeN/Q4w3TNaBpbq11m+DMIVV8lWbl5r9hWKR7fqwfYH84B/fnvqKFwP46zTIOnfaddPT7ICwa+SK+o9gsHQsKYZjGkPcMa0SSAK6tqhZKAD56rXHHFj4EZHagjZF5ds3znhmg4+aEPv0f396Jm0MjnAagtJqzM0HQvM9/84APEb1scUyUovbT6P7yCwEiofT1+UkkM24PutFk2NxqjYPjA27yiL0FDx4p+g2Ve4pQVMssdS+NxbUKtUmhD8cC20DWLY5H/JZdsShaN1jxUq3R45TgxGRd6NwU7ZNxG+MvVBiEik4AI3WbgbWl+0evW4PnzZY1Wjd4GeNRCNdfsyvnqO7SCaUU8gYy5Pf0nnnN81aMmGYDJZqUuo8vqnR/N+cZ/k63yZek5Lpe7T6OXotnek1hSXKfgoMrdiVHogBQB+4j5+VuisBcJdgU3qYXn4ZPejqQUiwR+vlOGA3VsVBlW/zv6zm2exf+GCtMcPs8Xluozx4BZhlXKPuNJpHQNbnMX4Woc8He2WOUhdQqZGTr8taqdJyxb7TO28S4ykNg8Wqvy60ULpocY1/z7CdYhJK4Wah1PnkYemu9X3lai+uoc3iyvtsQS8VeXusYwHnWTnzbOKyy1sy77Kk0De0yA27Ifh0o1clTrbqU/Qsnkz0gnbn+A87FaYE/RiYj8sIqnC8PXsi4lAPA5H/T9gDFvNpqZSoXurSAnExIcl6SuEZ3yTkX0fuOzfrb1dgrtrANYBR4NiHx3EgrPEmDYo9MVWmeeLwkUqL8AFTMGSV4ij0u6L2OzzArrjuFlmTYijO4CqEgHe2mzbsX6hoQRmUJ7DUhmsngtYch63ZkKsPwLZiIQmpygAzPWQaPzd+fITmVfpkKjaWvKaq0ilUbMGG7gzQ/GpXnbZ3MKZLqoZVawhLmx9VxfI2lBDIHNfWHJNgjHmsEfVNDiYsaCDOjCcQ98h3iPlAq5+u19nzGXa/RZ9zrQGXftmXvwVaiyBulWAeVAaqhGs3nxkIr74JPKGtf4G+HC/go7NabuMc22kAnn/h9kgqp7mTuawb+ioRT95D88OUtHu6Zs2ykV9h88lOoWexr6iDKQ1wvB55rRQ2onIUL2f4m1XjereM1SieSjxkJKUdaApYH1gu6NZa6yr1vEI8yuP3z+RRE5PqU+DJER6YAMHlAZWJL7tN7U3H6vUkVdAx5IP5JzdjCIX7h71ExVpblE/snSotvLkwjANdA9DbUb2ikn2OsqvmAxfXWuduRtEsqT3X11GHTYllv3ng0cMElqZV4ObDYqV8C62gKNu9YjrefjYP8HLfayiThU6MJ5LROHMGj2ncvLqz1CqDnCTDbczeV2T7Disw+SmdSa2PJS2wyqX8ZfqnS4OmtjaoGWq91rTDoIzy3nHBobu0RU5SijeKw+nm1P3+yNdIqINZfZ2pZgjnP78NKTiGhpafTNHrZ0yRls03l9XNXYFf1nZ75EOx9H95Hs0YMo5vT2H4mXUkRgNPBx8TZbcB20TxCuL8j+4pnTVCoXYUN64bzwky15OgR5tecStpHm7OcifSYX8qDdhW0b6LsgylVSN5BKHCMGTt5M+XBmAmObozFzB9UpGQbWExHLqhZQQ1TCRdbWDB9zR9B3S1k7R/jQP2gF9kX2ikwFeRuNNr+N7fB3crXgDWgYob0iczapl44xv/wgYkd8uZg0fTumtziHYJ97lOZxfO7B7tWUKWBdukulTeJx84TskESohAc+96p6plx4r3C5aHBGaY+9Da7hcNA10RRiIBEqyIqKNVImr/Tnb8a10u5Qr+FWU4tGVhuniaocdGX+DmMIuzhn5UnLgfyAGN9ec6uh4vIqd49x8NISuFPlPG8qcZthP0ZKQeiFP/u8TalKnf6I2aUFi310c8M0GGhDQZ1Xik7EBB2zCVoHexS1oa/mA0P5v1NYKh3+A0MVpCbNyhV+gxhCyaQyHVEPArZRerrwj99nGh2zz3WC4roMQSCj0BkqLtcGp9325tzd4RZOEfYIah62Lqi9AL2P6YsMpsvZRxWfW0hLtHKORv7d8NhLzXzSwQoAye4triZ3R+GR8qmnSzRZRlTGxAtE78UbppbCMeqYM8XmFi/TDeJhIjSK/DdvXS/vBGaGqt9YCshImYfhiicLq5C3/j8j08PlMhC3RAWaCap2QxJAIqcwFnd6zouBY/dAKR+kKatFbUfQDlQ1mMk0sC342N6sWs56QzR2mXYTU3JMacdvEKqj0XhubvjExHEYE9GL3eK0ycCYoSLvBvAdIZihqeihfQa0/HZ1JajLnltCzOt6UQTL0Z6LQo1bMdUoiAlUks8ZUvxD4RG2NrMN1d9TbFAhn1Giai3Pxh2o6ZHuuJqYPfQpWDRYQw3ZBRdkXwBUxayXPEm/IgQGxMfuvFL8L04CkA/iv7NfoC6pUQlh6Ap+ZdpXRnq3k8J7P1e2PEP7bu5QjA7KUT+CXCnt55CsrE/FSydRCGlSb4UxrFHEvyQNmR9GfkJfK5MvG9mB/hzeYco+Gh22aJffN9r3r4hUT7JrycRwTTSYSICSoHwGtKlindp0SEUTjFYjOOlmZiVYxPU3RyteK8z+tq2SlZKjD+n3sGDUCztLIx7Kk+TQKEZlWZp46l6PrhKscPDB226uwTkfZ3re8krJuID97j4zhA8bmb0y/bYyOmH7NPN+F/BsWK418Sfz18dLQFiCzs9HttJZ4KUmWORyr4NieUtTctNkR9mqaKC+Fr5Mg33tttdpzy2B3FMzcWE5kbcL/teUJVtV3wFUL++4l6RhvaQD9oGGAFoXCdODrLp/03EV0tqc3ITD4+tRnh3H6IAoxk8P5o0x4xIoJpI+Gc9Ck6BbrNb7eFrv006CppqaMiARwAKnNTOwK7Ei0ya5rfJfWaXZQxs/F46+szbNoWE4Qy2d0A8aHhtjKiYHqqzKDEasfz90cAg2X4Vm1TMNYeae64mgXPUVNL9VRHt6p/npcxK90+vBrOxQAgdPE0Gy6P+L7z/Oejei0/ruBYYDAWB+P1VU1fyfXZXl6UgTsjkdBp7yCEz7QwBYZnox8GzEkOAdZfacrXK9jYOLG6keuxOSBzdNvs0v223jn6eIRe+Sadh6aPt4RRTqKMLf+Y07FzCYODg8Cq7BXQfsfqNgFdgE1xbpDXmPOgDduuYIUw8O5hdEM4Y45AG6OsQLkQU5F7W8mRqSk8GfWgNDWoRnUq8RRkq7Yoa8rQfJxQfg6VOGpmkkxbu1xhFAJpqeSUdVSuJHYjgf0fTXom/2Jj6e1rFf/BT5hQFKSMmoVAnl4gD5DFGR9lHH1txk26IA2Vf2Vl6yCvWNKZm2uc3hkOzJIbuSGq6hZNsFTtQnnkjm2xzbRifMNTlETIgD+XEN3arkDgoJ96bIm3oidPCJihhk/HEOB6IU7NPUWkquuncdb97QB7YoJ7VNEjW3RbBtEf82utmjVfAHIt8Gx1O9JIsnbT4oDj7RUxNO/o07XraQbTYOHXIuiKkDHBrPEF7zb6yyB4YH/l679thNs8F7BXbkrJqxsrnIJy6WNqpqXpI/Y7QPQb7V2dZLmdK3b/tus4uEkzETskiIfAKO5KcI5yZafEbPhMT53rcwKMR2Z7osdjG9u/EQZBMIP3F6aFMzvVQBmmOrlkTTzOXNa59OAq+q7uzLendO2I7dH6/V3NOGiy/+P/oV+sjr+tAxwZac0parTT0BRn2QZj/FzzKexfDsrNmBFKOQdZCOGD+outZTc/qbzw757g2uVe62i/u5QNCzsi3hoPwcqTHEwlYdQUEGmfJkkUqvoIsj0v6K1Ha1phWQD+uC7rW4RL+21lnZ7172DrHOl8967kB/SQcEMhyOSV+ybITcfZ5Eip8VKF6HixuWawRxiONNN/IVgBV20rZPaGekm/YV5y1akimJ7OYaEfX8sqQbBQdHN00XPBJ8yxUJ16mZMopzj/Ei7ViRw+GeIwR2RRu++iByZe9PuY2Gilkp/2177m/SLo7iZHrkVPFojnJTJnWejLbk4XB6odgz9IGQ3JBvkxXhukCRjkhyOtfc0MGCbHbB/9raLw7lBx+NIpCP6LQtnzsm2SvjjTMG8i0bgtDubFsvlzDPZYVAjtLoJuQy0sTrhClR1bnTisnpK3K+oIIcOEorfnzB41/kzlrnRszngc5OojTOf1mHczyI21wsjGyn5o1TK4M1/3JU00C1GlzmmKJY0Wxro75pnF0xVqMkE/NXJcDqeH33znIDfZMcsIX9XCf3jeumJh/Ce9PN3Pz03kmEQM2FjJ3nvy7ClHuEG2kAqL84X5sHNpOrdHoMmgoQwl4Qwi329TxT9UB0BZ0GolzwRVyMImegCz8+N4GE/iSylvqP7Z9zBigjaPOK1JR2rbxtdKOnYiPA3fEf0jVw/1fKwUaYnENPNMKcuV0zfrtuoVkkNUPzyfz9zGWs922EcUk1tZsU5BdhHBd3hOcJcrQqgpQ8sU9z8FLCjSwG7QX8eflF5USrMOrYpaLaXM0wSVzWM3mlRmjH3QMe95Wl6j+jnjU2KUwhrxtfeHqzuTsaYDsZ7kuDqsV4pORhGNTtnNWf1dhqBTzfVyNclE9LCx5lfwQPlelavE+QqL30ad+Tq1pZydZEBsbZHeepQnZeal+j8eNKEvswyrPvukzBvfxjoAZhvOOdNlMPkmm0LrMMKJKTPXdzsjpdWnwkDANlK1Y6wCEty+nDC3voxoc6a6XkGlBQyoLWPLuHGnnZ2PhS3OQyT3hbZZZqZpnQNG4WAgGzgGLr5gBWbBJjIQf8a+hMDzybbXOuRZUFd5HMTtgdYIUPE9U7Tfu7hVbo/2nmuzrtJXR6imBpoHSFXS84QGUpqnaTnzBhRyUlVagoMRfA2EoPVUOi8QrDseJ3Dz8V70lCXXg5us5fzueLf/QvInc0vz5Yb48V4o+xuFc9F6H7YCHvucfHsbd+lI/SYbNRd5ujGWqvlJm2cGzzfcN1mVCQJ6XJJa8ylu2afF2BJg2fp+XJ6PdeTkHRQOip/BC0XtalfkKqayorNEdNjXFc9ZMG8pTn+eY3jtdpRO7SLzGipYYjWNwDVnWUJTwswTrSTiCF0U83fK5AVJA0ISuW5puYa/JHb80YgrdRSdT97DpJYwro4/QqDkJ93++V1KKAiocwhjpu/WAaAN7ZaRBVel0rxoq5UanZIM7/6SsPjWh0PoOK+7KX0aGuB/YPkqcFble4OIoTCAuUfBGqCKKYV6/Hj8ZfMm9v4TKvCWCdykcesfCgpDhihXfkN24zqF7DjtiHDrPs8YyF2B79lDpiJl7kb5sktPi8VAtVFNcUQYLEEg/riAS3DOneG87OEmUnOOMeDP+3NXJQxMy79d6CVAi2hpIUHZEfJJyKHYXvsZsyUpEbAyFDix9Rkn23wxULsptVmG11JXH4YlqASx+26jofta8/TOd/abALgqrnXXSn3FFFBzHiBCzhmVrCQXTI3bOEssdtYiI3G2YFoGzEOVrJa52lkPu/6ZRqEN7o8pYdM71cZ+fbOsGOeHBcmn6DhCnLM3+Kt9uKocrvIYjjVEGN/ZCXAhuNpvyb1FtkioiD42TvS1EFkttfC68f/wZ0q5XHfvdX12XdPoKMb+i0MTs/yAvq7J+Edb5275DTstXS0Iuboo+aKEmqnPTlbrzpmvlyhpyJp0jzSkwW94dROD+2SrXDgubdwT4UHEWU927zS2wTumP7ggXlUtg9tUTaTIRmX111D6DJp1llHCqH2DFvKes9WYQc9ajY0ICEQyCclXB9dp5CiDOEgbdlrlo/Ob8on/pIL8iWNd6doVm2Ka/3qRXhyqGsHJX4VA2Hn3ZBTXXQOSW3Scr0kkzv3JQOivurtRpWzayUV2LMyUGqMKKSI/SgZtYpfgFswRaJ6Kr1nwxjX4hHt2r4bIt5YXTdi2so3mPcwfLXKcHqG17PH0/rAN61wyqFnz0gyQVKm/K5pchinUH9Omgg9xp6REjtBFtDvcnlXx2Anc/xqkkTDKBzdQ7oUWS0sajUGe2cp7eg+Nc22yGGcN5VnAzlVr5kq7h4+sq0u36wDLjjp8MjPWpl8fUVp61w9ZxFOY00a/5GZ3v0ABGt9GU3QNm4sqPTf+ASRNMnZB7tc+RznIpc5YmutoQ3KPANoPfzm4k88NZOkjSNoALsc+QBJ+s/qfKlBd3EBMjLXqyHDzxewEClikZcB3dgcVBZnE9bM7KmEX0O5glCY8tDM8FuCsRn1T4q87wcIkgu0YaJ2Q3BjYt/OA08kDw7mbObQrqNXI1djsMXlti9zGGBjlw9NVKwce4jd+AMC9H/B6bs97X8qB5QoLEPgh7YAY6hw3oXqE5MnTAcLL+ipxL0ddCLRiou1HldyiLVAa1ZnEuLjdi4X58HlSVLxWPUTDc4gjy3UriywIJbpcBSLvlh9zJsgvUQiS83I0ssDqL5W6RaWeVPmjtpgItlqKNDbb9KnVBnibCSLFSVcdbkVw04WULrTpQvKgqqBRxoQpG2WsF4eEBcl1TYBSrpm5W2eQFXMskjX2it09YzXjhckliomNDppMhf4j6+F+2tHm5BF502dqB60Z6njZdHXPLmz/WxvJpZkQuNz8A9MjmjFJ+N9chc4t8WoC5boSnhlfU8USUVLm+FTqTFYDIZoKdAbQx/50XJRHOtNm/Z6vZlZFPK4/i6/WydiFv350m23tbPIegAm/yvVlwfD9SCnPBvaE1UpH6q1jQVrxaXvEfweWUIeT8ziewdFjphzCMyI/aXBic+ur787qkmnW9pn0FukzYxmm5WCmrFtqQ5FHHT+pNlizJ8FAcimJgBKh/CzPTHDey3aJPOuzZe4zY/49iflxHhcjvX9MSgeRUMTXPaa4LtTQRrKajLI1RxBqi9evqZtH0bVXnTiGgqARhXfzC9PdiKK+FNRNd1L3GSOaBEu4VzqgEfIOFQies/a9S8UZir7SEYIW0sLNPkDuiKfDX7AMd7+Y8V5VvJEOhrzkxyhoV6srmQ2JERe0CcqfN2nuy7GhJa4HoBZjt3HnDxeYhNfsU9r/H3c10tpWhavFzF9d6/RpzZV9Tba97gcQdthpiLSVZ3VPX/JZyunBEdRhEk4l6OzjEAG1pWwpW3ihsOM2URjRmQyiauZw088n3zDIMhUllzkYVvaCZampgLDE/PToE0gisvFJQ9fS5VJa69XpV57hBqLCTkEsXEkVjQjTvHtvG3Z4lEMNlGDpV12jRlNwcenWEh6/CQ+HZpFFA0zgRcoRJMeU9/FsMp6jtrvGtXamhN9ccQRERAu5URM+Hmx3Xk8g0zHlsrKu1kc73i8/wDKt8imirNLmmsasD/ht2HmzwG4V3pmK1HJSmjiOXqtzQqCjwxSl6MoFh7mND64WlEysD/tHUjThZSqERh5E+lkCj1xqFB6Z2mJWu7UKjw3P7XWw6EZa9oF+c3g+/H9W+fy8qzEahi4ugeG4FlYnwSg5fvWBq8ar6ucYYyJylwggDJx15RSdbwaSlb8s50AA37hgNm4w1jNny9W69bUMZgKQcY/CgGtr5J8wZbARXWd9kyxHje7SbPiJamxCk0ZZQgIkc0S0zzft78q0s1Kw/jguvuqQ3R17EhDpxAD93hypDhp05BnLEG+GGhLViK/kO6/D9tG8ZeibOZuRtlAs0PfmSvUVn20/61uJCXImR9BYAMk0q9KRIRXH31dT7SL9Bydmle2aI+VQ16EsC+1aKFN1QjSGg60popGPfuth2rMg/uiqfnT5Gzhj8NU8tqJr8HiJHfmt2vbxO6iaChoOO1Pli0N6EdzEwAuOAyPhYHpqR9w37AaX8idM1p1od8kvRbMohPc6yiTAZUNQWBl5yCLRkAOt1E78BLpqwk4nCq5Q/gCKtBZdmXixjRkgJgOZFDQrpnS1Z/uF2qNUFMWVMIZT1gKxENPk6yE9rm3/CU+xH93UWDh2sMzTB6BH0+LH7F0ELCKlNFkN5KYx4YdUsTKKpwtOOjgY3EzB94KHYzPZcGioXREknvB9lFUs568qpSauOoGU+gZ3oEdRFLx1eeh/2mIEgQs3O/ukM3ssUFPXi0KPBDHQtlt6u//9EUQdOoTfZS+54NOQKCNG7+gqHrmjxjYWSak6TnqORaBrrt3XNlyuheJheGOa/zWwi+m7vVeCKT9nhMen6L+Jd3LJpZrVUrLb4sH1Gs59YwGLKOQCPqrDONE6km2MGPSkyX7jTTm3aMPUNsDC46tZnDQnkoTR+GLSKWzWsxO5DxsCdjuaLXP29S1w/l3i5QU2Qav9BdCewczguOEA+KDMgF4XaO31BwR2gccyUc4ScUIjaTKPwOjwZKI9z9/+TVHSGV6onrDafDV1Wnn7z+COFF219FVlt6bk/UILePgOb03H16KEnKhOKdwJLNeqFKib9fPUXFQlhIU5vkg3q6x8lXRrQIhIZM2EZZgqAknLZnRepLLEGAT1ZcGJ5gNjy4miB5DuhLaYV1MPe+PeoXJ4/CIw9bSvp7NsAEkac5QNOPrmC9r8gAPjh6ptiffyvL2zQNJcI4FKil0eYdBxZSz8cfBRerMTOOte1WJD3rQmwh0CbmrFunam3uNyok4BkioaPMvfZ693hVwv2jteWTXZP4oQUwsm+A2JE1wxuUQFkIDg7ndDhZvH0PEMBEw/8FFiw73/kf3iVh927yodcY7by/Myb9QmO08LbacjoLXqreFejEgpTUtuee7qC4Kewgm7mVYwPFhj2ZMTjWbsTAwjPDRBT4wqNQQGGbmkHyQU1Nld0tOLQNdaI8he0sfffu+1PGPnH0KqWdau0DdKP3BZalIfOdne6wP3jVWiM2Ftf1+4Tx0I/VDnYgu8Fj0v6YiWHgO36CQIDbmuV04lOqRi7CYx3BJmws3GEXrwFEGTePs5oQW8Tm/lAFkvq+AsX3vpzg3kZqR69b0iYjEjJLCzs9zWivTeavjW8hd0eYgTHrPmlijy2XiF93OgrS+DQ8zhvocufaT7MExk+1yhM63Jy2nrkFW3Wwtr+q17ZIlRydP6/jBnyKGZscHyePn++WxLkgY5RGaBc7g2/TJTgQXYXQ+vGt0CjqNY4NltZiSFODGCq5qzS/NPLwi/E1lbcB7yo0ejhAOmc5GmDF58gMWGW9DRisC48d6ih19Q+9fXfnndZVg2zMNeTxV2cH4P6Kd8TFjBxW0/jfZPV5xN1LnNT/cg6xITxkPbRD6q1pn82IZ0i0IuhENbeaNBQF6BI4GydBJBaaKpteT5OLJD1ewflv+ZONvCv4FAUzPO7aaoLsDr6DwLPaGC+7Nqv2d0xSJeqV08UcDJYkqXBLvBBexfOmZlRWG4z3OuifJuE6Aje35WT9GYI6uF/Y65/Df53EJWvkgVZ2jQuCuJtMTPOAJf2aHZgdaY5CxB2pyUC0RPmRdYhYiPnLj9HKVNj75zHhfGWGHTbZ3BNb5WE6OFaJvyEExxaegoudWbOVoRC12kqlWT0gxI2XytO7+Da1O+QN/Hs8Cvdtjs0fT4MUNEtdct/DhGT1Ui9ryBiKGiDOjBvoYnefwOPZKNB7DBgfGgMC9pbF32HksJrpmAL2rkyb32opZOjYvNR1Jcn16c3mm8kuE8fCigNftXdUNK278uZJbVNoliRaOdBKIrnY9eShNY0/1yWV2wlpPHrP2TMhjaicjBORF0C87WL3x7is8iEZ17AAeslJgZyiY6YJx+B9GLemQwxO+RqfcmdbifoZumJouIf37GNMISEShiFMKlesO4mPMtF97oTSCku1IGcCljHS1r5UNcsTv81XNO1DgUjMrbgPy0u+XGptC6SCkiFgvJSHmT5q2SV3mfLzY+wHAIchtStKoDIVwB7pdR2EQKiT/GC1eieMhbCxSI6Zl6mH2AStHd/fP6y5wqHoBrlPESLiLNFHwzKYZmDPnoq4tvgg8wYq8SJNl8NWVULMhXwfyxjTECfLa0rgIz1RY4vtOL0nsvvbmRpCfnPIGzsQ7WL2hCCR6CG8CpYPETCLAB0ry1Yj1bBOHxKS2KjpC5Lw2N5yFRw3HNwRPYJl3W9A3Jog1OMC25etr+w6alAx04QcKpXQZxLHV2GUf5k8zizNlk6YWIoQ2nTxzfixyjbR0kjS/Tzk6ann+Uw+SB2qQ3i/26f7NH6p53N5/QPV1QJcssSOzyrrsLN1T6tO32y3rwA1EKwZfK2+FBPZlr+b97JOys8w9FdfGrCFalnUo3Os76FLIRZPDvlFuHfwfJpwKKiPdy5P6WZ7o7ykP+Oizos4CubZmMIFIgd9NjGBNuPscg2xFkMsdrT0LJra+YXf6lL7uxzNoW2YvSW9VQ/iRXIEK10lh9Z+0ObxqNoEJhXf2/6CK2f8nb+9BloqJEHT6cG5VdIgU2aupYNwmnkfKjX5PVHzW5K1l8m2B5cDZNVmGOU6wZ+jpf+NtWY9JjGcykUcHbMAiro52ccw2fLQxout4mqW7Y9fVG77P5nJsL9qOxNkyAo90cwbbj9TZTmtYS9RoM+tIwEj2WsIYqZOzgtNq86Nm2YThYbBMAEv9/OLUJ3DWOtg1taxK7CO/fYr6vP3VILmmVVOSHaIGsuOj4yGD3OgRYWVafTcbanNL0/ReQ1UrtDPLnqzTMfcTW8avMGrLDHNpKj2ibezRlF5NdxbR1yI23sMF36YJQIm58a6LDeVp74rk5k9Kpy2lzqSrSlPrsWGtQcKls4ALUF4GDTofdfemkbgQXUmLdY/2wYfyBYGQJd5xk+MoIEBgQKjXlxdhI55uijVkP2P3gb3q+L+F8sq1y8PWFtD8Iny+v7v5v7xnuytO8MO0KTnTDSzc0pPq/E/dRxqSxAdgxw7H2zMWau4YfOpL4946iSCDDkIcAxYeNF9ydCyOKdijKV/k/iZIsggZxw6Gv/55PtYziDC3tMgBx+f2HqCVVhc3IOsImwpFO0/ckEARG08rcjdEGxf2Fxoi/n6a4beoyONYMx4ht5qYm/CQ4xUrYgoNZ/33kZE1NUS0x5g2nbknpTiqyACa4zJsrFK710EShDsQYpRmvVK4o258OOWzctOBBxfUwWT9NK2Txf+tKBqcI8k36pd4KStGke28Gq8Ai0r1e7y5aKjnjozXxZm6u1p1CDx0Ten/xYIG0eK3x1yVDu8gfSoZpqOqpMbCzju90WIw+semnexZud/f1wsbYPm6ttVzZQV7qO5GqcnRqysaa+O/sXz+1sCgUcpKGCCEdGBpiwwiDTpqtdd1olQvv/El+IcWnrcT5XqxhvppjSjZUPDIiXOZBi1MkAlXLsPKGWnoPoibtEsd1Eda4tkYK+/8N6znLjDOrQ7S8m6lnSKbskciPf3fBr1g/+LjMOrRu+EAr6EE4PItbgZGNgdeD5fGUEcPBoLyN/dBW3xpFdtZdD2P40z90v8lNoBvOABKmKbs/9nLmw/l4rRnPfICAHq1FxoHFIb7qN8YKIn3XlG1L/9GDUb7lLxwsX55cYMHPsJVyq1bIiSDf1ZFK3Qh0vGH6qtrsxAeIwOkIBSl1SHnSrlNiydbsn3in7r36fKFT+cfrS3JUxDoJl4eUiCSBRQPwRaey9nTRvIee5nbrz5h9BokQya5SeG9+QHZxqTmB69Eva/xvbWBMvpSiMJ5EcKZ7HpQa0aShnn5cVTH1P7nVInXv9ZKEalH6WQYLuUm7/Fl6SxmAaHaxLmnKRAG91PNUDeVYk/1RKIbZiLJfh/JbrVQ6EbmUnFtakbVoZULNrWKFMNyqhRbSo8WnWNOed/4r57ceDdz0YqHn7N7p9u/ii1ZNqhb6vI3AMP++lkkvuTxMMskY9PYTy9a07JG1fpllFtV+/qNmFJVZA5+SzwKFozCJD9c6+MygLLKs6dugp9Kjt6tszooTp+87B++t2hu+q7bENTjFsNxUgT1E4ovxqzpP8+kQKoO2T4eGq0HJPYEFJhfeOfGKoXR1bSPBAc/5wY6vd5VqOb48kh3lniMNZe05SzGgk5+sNYv1SgidhE65vctj/oQpRhKmihazQFIFxl7totK2qFbk2MgzG/2pnKhX1xZIC9ZGe1MddDHMPe27CM5vBvNcKB75HTTFYJ39H5DNiStN1+c3xSiGY0LKlzSefeXMsjtcGcI1PVbsqbLrZ3dqSsuTV2u1BxLznkIYoeo3AObdtvJVyxMXCHbx06TKOUtAhmwBqA30NE/aHnP1yW9zh/ksg3JjErZ7AIwwEPgElgmQazeiKndEc9UibC3Z1qM+y08AS7odpWmqkngEhk4CwNq4fXNst3YyZlSQZjecaHCYDIWnPlbI2+K7EvADZwuPLmTNHL9Lk8CHuCKhXDEV2D+vpkM7CbIvgZlW68IoFv0kRiE49RoPoD9JCbKcC74j5Nr3dTf1ho7Ra3Z/APBWnKWzoO1Rha8se+upKjtDln6zelrklqBnDSpCPwwalWK385+H3vHIzVV7m4d/xS6obgiCgX9ksuctOTH00amsfO+5gUxVfiF48MmBCrf7AhxpAFkOjA5Hfdu1TEuE6VBeL+EkKCjlWfymDYJ2UFFzJuH3vSGkeYiVw+nl9/8dRqKVQprGUMRQLSih+exZfuzJYlSWsn3+kygdsNoRqk3vSXL1E1a1aAhI/ch5GgcyKdTVcMbtZoHwH4Uoev+HGbnsqVPnqDPAg2JAFokeDop5vba1ZxRG83IQkkiTUkhoZ4pgu36+Vw1SdmYUI8iS3yxuaXFF199vy3DJ2n83uWIWu+U/kqh+vsS9lEe0StZbP5k+Az7aYG3BeM4gYeoziAO5yoqnppA9qlOAj+/rWfKCdhdikKyoeThq/jqMLE4ii2hQXB5SxPWe+tjZ50hTZF0EVzPUfqUfj2rDzwZ+zDJiz4HGWvAddxNCzaUr4friZBeuDqs8dbxRvY6Mvtt4mX/P419AQYeuVaNMyGg4GppXwO3XWU1TAsWRGQbh4006l1ubl7kk+7D9ZHG+lum5YhAE/5fxnWnuk0jVZ3Zt4Cke/MHr/IucSZzC6EWtBlYpUmJlZsXgJmqeVyVN06tEmknpubSPB8oFiE10B6zYYHsR83N20GsMBe1thc9m16OFst8KEssqMOBUgOm/SZaMPyO3Qa4qyQzcOJ2tHEdypa5EY1kXao4mAFZc53YE+mlOgiIYtcovA8AC2zE/kO60twD/m7fbPrcQPLAItJ2UmJk6B7Feb9Xo9uQhgitWHO4dXNscvQRgI3rv0au0IycjX94r+Y4J6UqXmgv8c81+lJKn4QdeCbEwqwCMdgp+jdGMNG2RJT77dhapVz++KumFDAyKOKkHpkC998wNwM69/2H3TwvlH5/xNA0YDhQmS0y4pVgGmPiWoltQSg3ihPCyEemtOY4ZiISqoNLlps5OjI68Pa/SMtTJAjvDdQp9wFfzoV9vDavrFarUYhxr61/ZVuTnl31iZ9LU+Ru16G+29kcGCaQvMSzHzoFNoxoaOT1hqRObZAc2IRqdzyzOT/UyN1eqyeC5M5cTZ7+AKDD3eaT50oLQc1G3ttTJZ82DAlwkweukoW9agUlgqx+mSaqTBlAbwqYp2aU15BdPw6vwjGwseftsurssf6J9PNw+NLOCcM6cr+EShG5LxEtEI+t//N0QPmmaU1/f2++CevgE9eZ3CxGizHTl/QiQHNjQj6ajOQFhBY4w+uevM7SpN202R3UURc15+LD+AKE13B2hEvh2CgjOHXII2gGFiPW2zPiaa1kRqkcVg7PUNI/oF9ewgoCz8Tzgmkm+ggOSP7V2AzIejwX+GAYNFZZOYI1/Yzrvrfqppo0g/Oh+CCG5QFe9BBZPSnMdTo6JgMjG3OyPmqBZyEeFtOFak6eS/5dyC6u4Uan+A+tPEFrhN9tvEjOazu4rebx15GRvFuh5XhMpfL1cxYEd1bMZOHctwDLnkw5DBuq+4eHVPZxzVtOO/bvvYfiDbHjOlMcQOksA4/mLph6pQ70RljGp/m/c1ZFNHEn95mkmrdAAZkZAgcv93jN5jIDePpc5pO1qGD3HooKikIjN+99O+JFnFlFyWKlmhTGdWJf8nKByPeY1zivqJeIby/3YAZbar26Y1BM5UjyN6kKzo89h2+Mel9QrFlLmOYk5o9SgomYf3uxj1HlXbBzIf4RHEhd7xW1wtZE+PRJ/8wSfZRgaGLaRKePdZGOobMB7lYSVrw+iNwf6yN0JiW7H9iRkHoy6eau/F3xH1lfcKBGU1LvxJCOfWl0VsgSPk2/YXgLRhqcI/RrQmX2N0crySbVv0dML8UjKZHLupFNl1LNxeRwKdr+15LLW7JYHjbMKULbMHcinMPdGjsLnELYY9dkStx6Tb8IaiA3fXzQWAhHg0HWcr0t0tyHjj3G1P+5xBtBrKH0ikPdMAF9dXyUBjhdft1O4HI6eGyp4UmGt5wEmELUq5j1/GCvAG4G9VceTEXR1iHe3/5pFaZHIDLFMw+KvUTT5X9pIcHhOF7KVJwCjLgyjwjuOW9c4gFoSqjCzwJHGYPCV8c0BNl0Xl3G4Pi2VxiroKZ8GP/t8zg3uOr7JspY+11li2FJEH/GH3ZtXZSxJO6YTI6Xu5qvuCkaHILVCvHCcOQbJUAz7YXIZ8PxVZ111IHssaJibqWWlsoekF6qkJ5aOTiNa/NepUjkxe3VzEcOW9UY/PWGjDfIyXozRzkjVhanyxTWAvEPhRHL0QWd1UwSdAx8ncY7UP/TnYRsLfym3W9kbIe65f+pd5as9tvHg7UksvyrwBN1zPp+83kHnZpIyTSKAx8ZoYGzVG7ZvAwkHH1eCbmNBpnr3pBSa/ZFe8IBTGYD/KrWj10h99tV4cvvzWqUS6sDFXqXRBpaJ8QYHwm8Nnvt9PyLCoe2zLEdu9Hs4sIlfs6A+fa7Oy2d6bT6HB7V7EYy1HmQcOL4DQ49Z973kikxELWLYcbnwDgZbq607FIDQ9/j93zZ3/7z7yyEMACRhJyIbkK2Z1qdO7WA47eRmvWrcOP8DeojyXHyUoPRQFicvOJD7RQDGGuT5I26YrFjxaBic9tkPXdqJwnL2TgEmWL/aU46lHQvJ7Pq8SPRtG8JFygowzhlyzmZ3RlcIORh3U1Pr+z9u0yzoaEVFbGccsPJQYawQPAFmE9zyG+IOb5mZnUKvwiL2Qv+FM+SDu7T7aFsIxVJiqj+5RBO+5M1gmMxq3mVCT6fFSe4ixvUKFKd0vsSn3Ekq+Rm9zk5NqHE9E9rN7NE42MC+pitK1/3GyDzXLawY8dYPgfjncHY7nB0nMmLO2ciwukKZMGJ5a6sHHgUq4wA4UyNL6Z08TIDG7S4ltZlZ1rJC0VIv2dK2j8ezgGmaR3O4lP3mgTzVQLL14Ye1PwcVDDoCzbrlobfoQj5GJCE+8K3kYdxYziOUZpBKQESOjA+hTH2JNwBJWhtNXtiQvHlpm/BsanpMPRSTWg+Ib1jroTT4ZEd6ljQVOLSadiqyTTT86eMpo+4aTwEps7RK3KDBLwsl+p2mrH7t8jV0nLiBPsnPV2pYeLn0wXsX7qt80D2Ufo3iMJKLxgsbU0Dqm/OCbe5GdTjPIfpqFQdAZWo+cCCt6EylD++avFsur6282CL/idSjeT/FyJY+QKZ1ZqL/lnNA7r+2xQjLDQSJf8qCDa45RP6VHR0rlrXaYyQOHaKLToDzgzWXHumTWOTPErFh9w1i9c+bQp+9sN02bzGQBIYEr4H64iFxn+c6QXfx9VVtMP09CRKpV9WEV3X7K8FaEY0w7nNRvkcFnj26EesaHmO8MOGGKTjgl9MLym/wHPT8g6e1kqOUb6vvepA6+v4rFv+TVCj7celdt3hOZJBs1FbixpY6E3EA5QAtghiSeYYMDeSWHEsDeLR7KqOTpC6RZc1RQPd4ZGRNRc0HzoiHQC4H3mvck25SPHBmPz058O0BoZmUUzOq8XWS1Wm3BEEBwVqbcqNS7nFa6GrOQpDIX2gF7CtGq/p8QYTdFvVKuVsGmGHKLeE0sqkwi1ojfvWyFAT282vPvyaC0Z43jhDLFacdl7XZk+hwzZ4In4FvQsy6Krk4RdmmLbyxJu1qxhgsAvg6FAnho4v7Lk4MfGwtBdXLXL42Z3FJLcP1URzHjTxxFOjeC6E5e2ACLAAzoY45xznDevoSQcjX9M5zanyQ4zWO6AuGYvPJuDKogVD6GGG8b9mOU5XDGx/eZmiLBUK64oOMWGicV8JU8NqN5oLN7eDLDqZM9zTEUZuzRpr9zhRvPlGGJMMJlCjEe7Byun4mr9cfig0pyOUO/4bLdRmzHLNTSmS9m1o61D+U/hAjs35m+uzVYm6Pyp3fJMo12qzkOxrwyxfdeBRgNub6fi87qKNuLr5w4lEhxWIO0AYelpGKTi6hLBWnjwywEVFX8U4iU1kutX8ysCKt1/Qo9Q6PVNH60VtjLFm2yAwACH5CodPSR4/7mwLFuQKDsd+K/GeoUam2PvRyzyvJ7LXeSbwqrtfZ7nGrWhj75KagQyul37kMRDW5hFgvir3sgqM00jrf7i7ZnwSmDpQzN5RFCEnTOQLD/7EtDX1T5ncU2jxp59fffdBtNADrtC9tAQipB4Ty9eBunzv8sstLnSw/3BKMfIK5LxlAyDXdJWP+PhQBdKfVRm4KVlQ1en1bOgba4pvw4W7R7/axfGhgAq9fcxjiNDUBNb0eyMh8tEjlpk8qhdWbENoDsR6ZrUvAl5v5loTdkqef4aMv02TDsN4QEhkyVIIX1lVhKgU1zgysY5QO9MKsDxz7mVLvmQu2EGzN3GnvGUQqW4JMlyUA7rYu+ahLlbfUgM4ht1x2Xw+fxIjzcss03njkR1r6Cc48BIIu9zI1CpPJ+t+m8joLA7F0Wujijqmrb36fU9pao8c0A62GaFHQnYD20QztfqUdeS20qnjcIG1KVLdIznBXuzpu349r/AurqXdrLjQzyLpshmBiPtbRisBhPGkJFgULTXgt2utP1ms09J+qi9RqDRZAOc0QjqDY2269uQIolP4CLu6HbDcB4/3z+uaEMMxmu+P5jDyA7/7Nk1TXsfPZfoOJwRoUW7My0puOQRhHhg9EDoWMoZs9LzxSc4V943J2QEX0WmtqjpdGM+TbIiduTQvdXFfJIRiS5Iv4N16dxYR/9bDORf+E02aWix8DIZV7D4UOCMDOubYjKAUpfJfnsMRmUQVRiY1lMQ++SqcO89ZO6RpCsy6k+08pc1WtWKpzADR6pslKko6CdLpMbq+JrssoL+zVmzV/dYnO3P0rl0QymGmj9G0JRuMnT/dJ9Yh0lssIytPNksN32a9H014b2+WjaFKfC9TgGOfrkjUfFosSFXUq3XNT9kcePQqpnlWl5ccF6B2F+wfnLkER5e5iFBBZOn07+PO2o0eNykN8U0ncsB8l1a1YyJT15zJYh4fykuOTGKrHuEgbZk25VaJHDvmY1JGCHZixldr4F+rCZJW89d84iXbFAdp1onCHR/46BRCvu5ntqaYKq/sULr/dVZcrDr1nEQCG/y6+cFm+sfJ6T7eLMbWBZg/GvuQZbzdadC4DYkuGcOMCCPnAJdmGpHLqkPxC2YWU+bhz5InNNvvhD+dE2zL39AaFryJzNWBNbX+KyHAA8wkVfcu2iOOCZytsT+m734N+5SnHVg7eEQyuhst3xQtvS9DJK25eUt8dDvaZhX7WgGu/vdDDylSheL3t1zg1N2tvfHjF9A9FJ8Ki2MyX9Lwrxi1heY2yyB64ZO0hxZPxqH4WGMtYP/WKh7YlqrFweWLNxEieuOUFrQy6lWblrDnUq6uZQ03LTOTOYjH1vmTT2pKgSCSdUHmARxG2gq1O0xrM5OgAVmq3k0YGPmJoAKr9O3qLcshu8PXvDlerQqS0AjNkaiEfiWPHW/xEa1D2Gv+7lfCOfl3Ip2tgqY7BhNSDdFTkjGqB7Akg5kVdUjpJstkiHmBBlH+tras64GitOzavpOntrgu0ERgch9HJJ0LiFKnHyilHW6QE+GlsWQlDKpu2+KUbDkxOv/6iAQHJsUebtL20rkkZRE841+2utba09DM82TT32RpB7fZxb5gM0R7e8DpVV0UMflGMfdRAKbNY+qgiQHrtX/UXPZ0Ap6mFc/Gv2YT6B+bf+f0X5W2fq84Wypq2B0cHsM+CN9Uj0ttNBoRHsj/swXYa4sorxtB5bS0gwGO0bXf8vTMjNNvMT5SNODg7Ngv1ceYyDXGHwVGjRuUO4gLCZnAmoyhW43bqT86wJ2w1mY3YiH7JQmIp61RmKcuqHBLQkznuLamSixVN1Z9N4aZxKUoPA4JN2Sw3ECNxOIXJ3FAFRZbo3doqG0qcn0JuQrSREEFIVKeqDOhNT/T3f9/L8cDUDqPbhitWkuDWRt0cmdYlFdbef2FghWIm9aGeqCkx2cadNU5sCmTJSXVuLYXAc8huV/zBvwEChyEPQL019ADY9Lz9WOi9fYu881YelbQ2r0IewfpFznCPP8iZTcwvdi3mnwLWUYkfhC4Tt9RaId7uhShg2N20+3rpq2aeHEts0xKWnXHROktfcbCD4G8uReZYmkCL6lLmQw3We2i568fCIqw75p8/f0QsL6mIGjldjcOD1cm3ETCX9ZwPBW/6thSCIy0tt6pHj8wqvNWSEFdG7UQkQD60d8EQ8qFNCBJ5arqB1Ky31q5EIWDYn1knnuCNDeIyUACQ69hVXe2ydnhtOTw5NFJ2/duKURhsItpJ8z8rukM6W7XX9cq5tDFd6rDoJGhl0mco3rIWaAHW16Fz0t5NR3ZlSUA3uk33oK6Igu2aq4gkYyA8WehfH2Lq+2goVF43INMdEuYb11dJGmabXi3QARStnEBA8Xxh7S4D3G3p5xGS2lZ2qMnCpkC60j3VfI26H09RxDjedjdi/x48quM+ZoPhG3rUVlwBUbu+wpVB7IRLS2Z5yisirZbMymONTWCaeUL0kKSVYUCNjZQOSx8OQ3HF9MUqbnWn0yh8p/iKgPl/LzrmS8XMz0+Vdu2R34v63eKWxNKjbyjbdRXEoVCwjegVSiXmE6Hi6Vg6t5JQinbHFt1C7ZIgOeJp/zf15m7C+0TZUZZRbVCdQCp7eI94Wj8ox/d56a3K6igRX69GHqZqUdTs4s0/EnqDIGnCyH15m+FWrSUiFk5aPcpPsxQKzQaEPLnamHKid/A==
