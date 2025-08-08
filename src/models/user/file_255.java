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

TA4VFU6PfCurigEsz3LwSQwwJFUNXpnzypFzp9TMR8Oghw45O+bjLjKwL9AnMU2tspfAw4rt3UfNZ3f/D/+v6k8JqqfkPxdFH23WfzwS7cIwFlLIGjmr2IOSSAUcZ3T/B/48pR3BCeCjXFq0EFT298G2pJm/H11Bj5T0h/OxeH9WqayTLj4s6DezaQEpTvkZ369wS2JNNAbJqMzr7mu6MHzEtoHMi+xmBX1jjtqzP+A9JT223niGH/3kMDJilD1FLvZWfBRgL2nxGAjpfn9haF81i6W2NVTmkMwlvWMF0WMXAY2NE9LMiPeme3qFYAuJcloBhFFQhTYwOL2ztjMeqrW0IHzE/Ct8G0znBRoGTVPztRY/UPO2oLERJM1+L+fJ0/DIeBy46qfqEgthhwV5Ib9VxCZwDpFKhfxQRGq9Y4esBoL+UIlkN9hW+X+1YefxpZnhMeV4IJNq9TXYZDz3RaXCwC0vRGTnVvJvleptJ3Kwvf+KyCti90ITb63C5s8DG42Ftv5NmEUA5NSMjc8qo/gPqczglsXDJWXDLlHeHaC+urwv3YBma8tq+jQeI2VyJ/8waQWJQZB+faEdVjkyqk321mYeagOz5m+qJzMIwQwsTiDm9lWuVBNwJjSZ+mwXETeUhcqDC05hEo55QFyRWcwvGgmkYgL3Jj8ssy7HuOkK/PDEkLCgecKgLRi/ikpzJWYU3bHU41FyICLjkZ3Vp/pMLc1tF9nt9XU2kh/5V7A5FrnN2kRldHjtpnkhl/BDupPWzaLiIjqP1lhyvMMYOv13w61AgO3lHx3Z5pUw/r0Ww6n1lqtAZLMN9Y+O/lSutZG+2p5wdKLvuk6CEGTYo5ox5F37UVIe6yHJ6XVpblQ8PnQfMai35uq+BrbWBJKCW3i2YdRGxAOt8Ekvp+SzdIpfseOXOATe0MK7e3Dw8uCQr24inaXl/lEFrPUVMF+GR+cpbs6bFi9tdn+JielWaf1s6N4rWqMY8V0vNBnKGKhi/PP6rvmXoZAbCQDBG+s9k//Ihjv4lFA2fIWZyCh7VYtgAJQUnCjaWpdgWZ5BCeUjTw9FlYG9a3NkVrWpNnSq++jk9SUEQmCID/SszwCt24k3PT1XwWnv4wMNH++BcpazQTgt9MCs3MJvyMPn1mTOEV7E9eXyLAfPJxnOsmJj2WIh+JMfticoELm1MNsyTkA2nJEHDPfGhqigjzb8CHeH76T92mBvZ81Wfik+bGZI+di4irAwf3f8XfSdrZHdATcjDs6vn6glktkGwMFEhRy9jDXQVUC1DB2RZl/FQQiFL+Fj9JBoUY2uPG4wLlyMc91dAI0Pl/8pYkaKP7fKFKq5uzBFgTLCiN6lcI8xvJbaDMRpRjTXo+EW5QENdm6EDrbDvAArpQSVVYY/ZaYAXd28aq3K0zbc582oLPaOkFWtEljMmkKyvo1K2mtjMIVWE89hZzn2Z9Gvsz801wV59p2Vkanmg5S9QkRwqYyB7QRGmANljE5o0qal3OfTyRs4w6Vsfm7fTxsCKXYw2KnwkKkpM8KQoGvxeoWVlLEMUPDAbxLt+/EJo389XGjRBfCqe6Lo2seG1Ajr6BYr9KzPI5N2m7ZaRfLAdSTdsDio2Y0tC+a2s8PjLN+11wZlX1KxcyS3A01p5H3YpRX00PsJL542afOM8IsCNEGrxtqt+prRWCVjhYjnO7R2nb0BQ0JHXYaYZc9X5CyGWrnTJmIchqH6Z+isrOTegZOlUqWu2QeT+maFVu+SezidwITHmt6bw7ho5weTNSR9uQEZbMZfQbPUFRAZMonm6YAdT1XiKvobC4NzBoCsVr7lIMMu4uTkwuNYREQq7PXSH7WLcUmcRGLF6q9fcKGDYaIegmuTEKYReOUdLKut/ngx1xxhX2LUdG1xl4t5jyvMd2/4cNHxnJMI2yAumJsAJaf6kGPsGsiWysh/kcjDafC+6unTAix0YqfnTRi3bcjwE8ztl58vFci9oH/qCf6UvY3qjchoO6uZgnzR0H4PbCpXCNt0ZLvwALyHwO71UXICF7gTHPSZKXqjbBJCI+YVSC5CnNS/O1qa2gjTiylCx+Qu/t0x3FvgnfEYNkOwNdK7pYoIbTcZhqFOzQaWqWbpYjIMK2v0yNRs5gZSRpSnulmcfGnYTmYdz3c/gUglnrY6NJJGjvRiuynBQ4rw3nkH+T3gWNhWwMFf/OuxJXlkUX1fBlE5F9gEbjWdkkzceivUymUHjK2kAuwmphow3cb3QfjPXLxg02JdKPI76FO/xhkc/0+1tWE+osdCmUkiTzioZSO/9WGHZervdk0vU/HqLTPXkrdZCtgsc8rGpAbBvqO+20haHsOt4JJ4N4tKfz9NrbWYehPsgwOUzIwebmcLTNyVz6w+oHl7X2n9Xkw3Wm8h9zRU747N1BHdRYeS0GGtL8Xb5Mfj31f298QZviXFgJA2AJy1Zi687/G2JAY4dFuQbmIXfvJPusBjJbu8YphP9Dj1KwHmNjrW/1ZdZAuMi4gxJYB5u+X7ezsfnZ8CQR5cErSCXh035MVmow0F55z1hDJssNTcKmv5twXPb1sB5nSVDicdWea5FwjrX/KAyeC7inXtQgjRBu3p1WzlQ9iiMPqiCSUOpOgOhTd4yQbE1h7RQfsuBtQZxbaDdEaOmMl7QKGg/dUrDuA3AoEXn6nXGNxH+cRsyppm7YGGYUe7Y+cYMnCDFbjEKXc8N2nS7Oxh/dbx8uPqerfb4gR5VhZ3PjUlDCwbcuCEcolmcP/MOkgLCeh3ePIEpU/YRbvCNb8LxM6GN2/7zd1qubcRbEyU/Dl55LhDeE6TRsMt2OLgefVTpE20V9wfFUcDh6Ihl1GZc0V3kLFTleh94OCx071dtV9BeKIgGFJQo/QRdny4UlIQtKLDkjpYvEwc9AW2zLubF6/8i2YU69pfwQ1/Txi32CR3MAqdJ/miv84IPcoJQcqRXe1q9e5ku0K6Ikotgq0bYFoCWm8ExpXXs0KfVlMQBEnppcDJoLS3B8fzeZpaR85qURXWe2H2W3fwjFqfQzlf8qhHV4+k+vvsrJWU7sDdKgmOLbPfY+DeOSAEj3uuVJTb24O8j1fjlCR9VQU75xVaj1erjrnTUkNpcFs8p1LDZSPFESam2WZuTjqdMnF1//Tfw9bJmQfiXFNJ6GTSoiRXW1GOZEknDW5wg/ZKLX3JQ1de3nQV7f3B0cXx8chQ5i8oPRMSN3RNrJWb/ekP5LHMv6ZQCSj87nk58XqqnyQ17DZFdWBiSbP7Sc4bLsQYq/FgXGycrBBZ6hlNDCb4Z34vtIHaPk67RhLpzeZBtNROy0whXj/Fuo6MAG4I9lwGzSlQtHpE67/c48h+LV9n77rGkDilnVM5C6QMdYskwU1Hp98hu62UxyFnGcpOz1RjTsfh3DrpMuIbe/qot9CqtqxguLbAenF4VT/uG/6f3vRMGWDX6g8Fhxg24l+Cqx/CCxraNAlLrPluaO5hH3fW2JcK7OUDikSZE+KjtgCRXLWmq5UzDqau0MZpBYXihzCCNkEVu33XzQj0X2fOQccS8AHGCH9aYBSubJnGAN/d/FKe8kDMBSibkSFX24jpY4Amg08CVowsqaTSJ6ADbl4ERnniCy27sFgCViZ+5ozndiUPFUxW/J3dYET9E1JqbcVi6ydo8vzUAgXF96iMg6CcpIP3GkGJpimVZaxYC9aB7G4msgSnKvpxvLCKYiQ/UdKXGuFCcobOUIIoxeFMu5IImvw+JpDnQbvustcO37njsIbtyxthN2XuEMmeMjLxRUNfO6pB9UC0BI77jAJAFcHrQQVE43WIMMzL1rSRBdoWw/9JRCpGJvq7SLXv5wnWU1rhuAKF1v2Dlm+o6qPo9+Chx0Z/zqQOsFKUAL7mSACzCL9/du3ps5GmOf2Nd54GVONA7EaASL5GsgdwIFqQSL9ZSq1X6wfNpH9IfHAk0NwVHUXt4h9AtoY6NhKunsvBD5MjCh6SexCa7HvWNenT24h8ACAXVEKWRZZ99wJ3M/suAqyOG/VEN+vnCEUBve8phCsTtJZh9W5YWhdKV8JsWMr7aIP4fSwghgubJs6aD6Z9FmCByeBDvrl5n2cC2At5ndRt+rj4Fqq9l5bSC5eWnp7qnjk5FandoTLAPXag2f2KCkDMYWUGXhswdhnXKvOkS7Osev/oSVsftBFDWK2PhZwlDJhBiIpZbVP7CZGxziOKlJQT4ach178IDnqkpL69N+gbPXxYM8ZBWvrX7VQfMWBytJGW8xiRoH12rIisW6NP6nVo0o+Wqk+Yh0q3+lMkSYEzxirHXI8dmGXvIb9IFirCF6GXemwxdaZPDnGyWFUik8PX02zcrIP4VGNSVWEx2z3OzTtU8Gam7xle3+bmjCmE8iWR7LDaDNOgmbFNdzBJJ8rhHTN43JPYJQIu16EqojGZUSQoZc5L59PtQ6QYxKJA3TrFkfTJEKX42jTME/fkTfKIxbmzQVw+U/kbK/8bNGFcBtd1dqGymB6+ZVCO8FfttimAuyDojG1AIsy4xze9QWsZ1H6mCJUenpHctB9MnrkBoQleqcEfmEIYDQpB+A/8byEZQZfYb0Uo8DnsRH+BNyigFPZcjCGfQPajWgg/NyGl9rfco0KQhBYP4KuUnI9T7aZ/GH8Aj5P7RMQug82jF/U/uq3n/3HgVjJNfSl9KTDea8YVhRYq6eOGlADOyjR27l3z3o4XomBi1NbFK7GB4M1YwmHMhvWwAkQ0iyU0eu5XMF5AcNGKp2PxqYkD4q4Lf2Fs99IQZlRM0u0dijX46yk0lHpWww7rcvAiy7anB8ZptqLDmxDPfkYfJnIwAWBIM5XUCx6PmhaQCVbaiHaTgKVWEdxdpaD3ntYYN2j/QzCfAqzAYHhKvlT8Fg7foFHKYSvXKphhg+4YDbQKrQW8SDAin/+6xWYTJp+w2Lx/zhJWRsTz4194apHwrmNE7QlidMrZZ0lp2L1d826oT7Hi4GNcmgmzvJWF6FG/POk+ZaNf00hrYD3HG6OV5/3iy+c2ANwGKf4Paxscs+lxwbEezugjPVs9ufJQGZtrHvQ/U6FL2dn9XiLdMUuepPkzoX79YcrisrhKw+e770YaMWG6QFhptTOEm8OPeumlHoGTuZ23Vw6SLF3OgysS8C8aejCcAwYBnpqQzmRK6ZzPAcqn5apfWlEHe2UfAhG0sjPd48KStmIZW4Isvyhz6jcIQ+eC64vZYzSiGFSLybYoVtQxLMzpcqRWdGWWmWI7iFCr5cwLbud22L3Y1MLarNPcP2+8PkQDvaheYYWEPpQk2xMkL4nXWoU/dVK4FZvc81xrBZkS4mkQIFpRO48VL5mQHRll/Tvfq7QjmBmfph/WWacgK5cPJvkxrnltdgET2CGGGO6/9mw7yJDpCv48Yz28kFxGgGQBO821h8zzU++a77Yv/6+VdAliKhID0/FrXZG0iTOy668IIxlyoR00mhqTO+y8/WX064obrRh+Zah6bkLlQj6XWMk0VelKx1hvgzQMwNnYmsrIZ4PhxCNCw2UkxYCMgU4wqOZ9UJ2aRWQHutxVVo4fVr5kNDx/hKUuf/K1wus+pthihWJwcwuo4J1WlkfX18WlY7EX3qtyE4lOsTnH/X2xuj+FhW5oD/R15InIsYTJg25/0D81pZxW4EYTX0HlKe9I3aPcjZ3sXFg1fMshNkfUvJzFK24xn9sZddriDoXxlLduxXwQUYMB4L5OF5ew74wV7pboDi2r6DkmeKaR52e13wfCXiLt4+L2mD8I7OISye8Z4jRC4x722q07snKNKW77ob9R0/BZU0RRZiFDAcC5XygoTIiZk8s3MxlaY06IOSEzXMc6Q3wHr5e1wFsU1o0dIxUQLOgrmQO0mqzIKFZsYVEgP3WaGUyW8qNqopJ1nNu5qTtdKS7DbEZM4Y0oa4hr/MI1UQAR9f6hsGIENO+yTd/3LkDnoroyfLqtndurLpbeL8Vzjge9HOjCTMz6XRtk9gyvM0Lz/Mxv9ZYbWwYbvporkTz99dPxKfYFu+u7ZjKYKoBEXaYH8EVO8aMclquufvYkNwJqQ3CbPQPq9IDzXadUFVbt7Jx12Bkx37bvch2CkQdn/rC9/GkWWK0gMTwTvvpFv85umvcGNdRtJHI8X54TSj6/NkOTSU25fcMCxabJnl3RMEE0AyN5ogIn+hkIGFmRoiHBEoChK25TSvKk9e7NnXa7oMjZ6z7jOREU2r+/iSl3IZU6K2NMAHKCeBYqStncb5bUoAyHa7FS4y3v2diRlhZ8Z3lgFhOWVQinL3rQucYLuKQHPI5T7UWtqwCjW8zcYIJYYjU8pa8bltkbGl19UaeHuqJKF1Rzyz3DfbSRqKwEl/uGR9Bkti9Gxt6PAiwuLPaZX6O33IMCkF6NQUE/Dl5/JXfeWSZRRjWejJKr77XvirYntNHMF7T7DvgFt40DIRgcWPCNi+6qxKviLt4kbHZ3vhxuqeDWPm5rYztC/B9few3pNluzjvZx551vAs+Q+49v36Tq1KSSUWotKGmdOhPPgYklZ22Bw9ZfN2ZmImL24aGbZ7OwqvIN1QafI51NjxogAEvxkZ7Da+M5lYEAJlEG3ARS3TnzRKUCzRU4Ym25nDeDSsLQmloyeXmjUO2D8TgRjfddPxo70g8PhKhDFxpjO46zx6SWsD9YJL9XHlunWps/Bj4ms2PBaLWpZ4GVJwLyDEvAHNKLLa9NI+xNftEo185jt0W7T/eIalWar2mYI/UZlFHerBhLx5osWrPt22NI6VcnZQjuh+IAA+JDoNNW/1HDfnp7CoI7EmmhWaSC6ywB5RRdj3Jl7bHRgZc6dmNwMtmhZKyC0uZbAZr4B56R3O3OTH7zWnK11OEYSMNH6ZFEG3wWC3UQPlSHsH8/WnYqHCT2KObajLDO8gv04fVI2HmM+1XBky2cpv751zeC6/YPd1jYcvWdoxSxV7Ep125G0XBSrx8hukkGSutDtsu7wcwoJG8K1MNBn0vqSg6D+Z3zqGZ0t4TO709Dj4yUyr6Uo+ujIjzsrEuhtAoU9abn9ejjbQFUqeVuHMx5+DxcNLuI/DjegV4BXZ8p/lHkGRsGI0Xcu08iXk88NzKOecIRKxVYhBXdHTvgUt2BevbyKJNzVBxNs/aYBn2jCgSfePLi1jumy66KnXds/tGDtTON1xTFW71o/HgDz5+CDXBNYvTujiIO9ECwbNUhtcFkNZjxyZTT1p8KppEoCBZfy/cdTczT65JByWo1xFeFBlhX4SF01dwaDvr9ivPl8byFX0AWmgIQCkIhpcwE9P3tWvXxmneYFki5jDqkjVxfetN7rR84tZTa+ASRaaoMbOH0XqQ+JokqFHUlDK7Lge1euvU3vUerYjc4WFe2/htnbMw3KN+AGpU1yYzFBCPMsuSywaNrW7r7aYuy1L6B4A9dZUnVvsQQZKurODIW5gjN+5/p5dIsMQowKoN61hTyEV3h+5phFMgu4KX50UbWpLE+P6Rk4SYlsQyocYugF9e4D1p7pcjcinSpO6XX9IIIIvNsl4TK7HojTvQwJEgDx7IX3aVYaHZzFuh3yka9jDiZVrWcHJ2E7TjGpa9oaTPvTmo2n+awzGMFFE7s5dcin22xKPnLh59oewkstRh6XePQSWgWmNUTs5vy/40FVuI5u6klrjOccxsjqxxw1UEwFwfQsZ2Y5i8T90cI6goiSVdBqcvKq3TBWuIwZuwVebpfVeX4Elg4eb7YYa4TDAVfZPbXGUC0iyrCmFyGbx7HKILvI0GZNy1mZChALMCeddn/gZ/2V1OjxQIuWAbeTPcPXQ0oQxdr3e0fruXtYXDxNgKWfGeipfXVpjtxYbCclWMTy0BvVKZ6Z6hD0jBYGQvJLWwsirSAY+4ct7h58UoMXtM+QrC4Qu/CHBuAb85vNIg8dRnZqScKrxCtmuIp6bFjxw8YLkr35VknlvA6kidj+hCIR4kJ6BCk2LO2qCSh4jl04pauPP2v5t0N9/Z/uD2fdxUkA3yT10EXpgH/xPEjdpoRrat6IDIpq+kwnPtn3lVmACFaCPZTmjqShJ1QSVOFXzRwcZCHQugvS05uLI0QQxB27njkLzGYhqAF5TWLT8MIylzhqnlF/MCLs69i2wWqI/SYihdWcL0ygo2qCxYaDNcW1K9pgkE/djgShbzUhpZ1KXgVLtS+MIQAlSQmA1HQCwZR2yCsjE5/5oL/kCyZ2gpW1YXm/eXl1h5HJ9+JC6URaWkg/yM5WmFS9aEuBxr/5qBlopf7OpuJ40WaRXKKqCo+2vwiBiM/5NKmYAoyKaJBXCyEtt4Wqw5hyaGGXjjMNNJst1VTr8LMv3nqcg9iwoIRweZwfxGwZalxsd1ou//ieD5FQ0ia1Z0d7doY4PXVsbQ/WSdShUHT23wWgRNWfm86D1LuLVtQXMWbnRiwwwvew0PM3uf/vtDX502z1399yezi9kOwThvstOjy29f9HWWV0xd4l1Umh3HaZ7DiB5bNifV6A1g5nAQeOP5n3uJbvKO08hDPswR5eOHmtWlwGk0dafw886bD0BKiLBeBimQ+ry8zk1lTfmQ0tg+jw32n5BCAymEy2YI2CYgDcloQfluMckzmQUyxblGolQpl7Ve4D8JCohiD/WagEqsE0FlR5R2Nkz0PGg8TY3wFVJFOhiHGBuTR9twXKxFDX8dhT4YiQxmyYc7XlMC2mqMMus3MR4Av+qt0KBByKOeakknUHcL1vv9CMd+ihIrpfn0cc3zp7YhxtzGN0gIO03LTemyBQCZT3HBi1GYQew3ScFX8NlQJVBfKmjLYxXcy9m6+bTyGoji+9+iVnLI+7V2F2Xt6BGyVYOfVEDXTRNhf5UAjB/zm5uMgnljQcZOVokMyalhFPkupJIZ0tKwqfrotyKCaPTH7N4SohqVQPV4bBXYOijib2WyILB18+OrB41kVsDMn+ZMjPuI7slK9JQjHECiq4cuWVHbFYDXQ1qL3E3XdrNTdLC4R1dA7QznfO31B1geBggHlHsqnkL7JOCU5p6KNGNRZMz39zzB75aoxqMear+8JUI+RiqyLWy2OrxHBFJwDzO14atDxoYTLikeydSmJJKvlUcV4rM2qB7vR8y2r9zpo1vfSiV+5dkw1eYqBp6+RkZ3WwtSO4kJKinnqPJRgWCf92CA7YZOgNAk8Yra+7llh1ZHTk7vTPWEAbGkdIgkY8BiMvXSRPRjjo4h6jZrq6YxlamtGIAnofozFlw65iDU8jsLxZDV8nNpDmUW49dI7XRCeEg2QEw2g0SvxiYRXtgvqngq3+TaNj/x4VqAWNGw+OBKT5SQIjuffonKAM2ablflIBa1yuOHSDG5P1vclw2+Krlzx1eBiOhbkJ3SfHmwhozcyizwIPW7K0xlKdAnUnMixzDABKu1axk78zC1qKOlqVtBCPpDiHUlS03hz70IznfhIEx59exabipPsWnzkHMgXl/n/sNaokkgDIsQXUx+m/n2ntyId2hYiDFyrfF15w6B98kbLBYwoMmj6zFj0sEA30XEjvibbjRFWnsWoIl5iWCTuAbNIZoMNfXhVkgUdb3F4JGw+OdqyAy9crwF8IZqReIc0nB9CNXxUbN0ivgL4DqjN8VPmKF8RlMYQLFezbcNVZKXomLC89f7oC1R9vr5KXnmmHbOsO+2sNcmlFjFarQ0SZtEB1qsb1sE9v4qt4H1PkDxyupdfc73sKZ3wbKwKb7UJ0uASMuAe50YOk8tzOuqrkcCLXqHz+tfbHeJ/geaJzRjNDC2Bqi9+McVLyIzJa0cr/LWHIeT7tCk6aY2IBKwMnA9SOHMPP1U0U9yMl3cYocH5oa/A6jkQtujVis97x6rVXUYz4NGVYQa46Md8QSvVXOJAVOHKpToI+GXOuNE7SkpJVvjRsNs8D6XeFrrq18Eq4G3zfa+vaQKT1QzF8x99VfWnjuUDuVpsuLqYtXG316o+uDP9N4805GqAzOAEXMU55GH6OW3bRzwp4p0Krj3PNMJqgq2Gm+LpFLTstr7E2rWjPrts8cUWmQ9NFlfwmyFJYBPHNodPrIIxVM9FZkuDbGlV0RJzdAYLz7twOYQ6k+pRZheQyg/idj5NtwIqhSTbYQ7flvcm6DAA7TIPRGdD5PbE4GSeIKc9V5skhxBNdx0eb3DcwrVtilEEbHi00lhEByvbheHJ0/jLYORDQAagx8xvIQYkxYXByNDtSABnqCg5E0mqudbOJIUMlYp+TsMp5zUYVw1KN2F5m2jMWql/DelDkPVGCtnZmD5jd/Cz9VVNOzxSkAKPC/AClgkcpb7Z1iGtFL6vJB2vl0M7Zii2Kng7gafqE58DuNsVFY4R1qpc2duW3ohX/0FYbbxArS7kcNpg6WXx1aYANaWkqGdA1KhpNmobZM26ewON/K0L0DeCkirwrXsfHjHibkT92VvTcP6Z29ey+wDot9ecf/H5GhgXB3RtkqJexMvQIRxwltJDfMhsucpSbN5nl6ryUCMJYtoDs54hUjCt+ViHL+WqyCXq2B/Pp1Zq9pT7WzOWmZTNhZaUDE6we3DXIXcBcSW0DTXsuMdAvCMpH52YdVmXQPOInNEohr+b6xO+qyu/mDFNIQHUhrnaNv2QqCV4bo+b9FWWf8vd5URU9LJiMm8Lw9zP/0SHKKNdJRSEKCqWoGWuywz2iuOIjjF8jUCwG9jkHYzDxkH/LXZ7XQ4WRp0b/hBiOm0i+0p5I3o2uDLAyObnBSEab7XFiWzmiDfw0D+4KI5flcrEMoOsr0D1z6f09nvtQh3/UNOriRVvA7V2e3XWM34fXrPis+6K1KKZDIaFCZgxBVFSuqy0jccy8qgiWgw8uCXutaYSHdU9rpPJLX76ZIqc8sQt6Y+iFP8rbqDtu8iq/SkVBF2z9wWphh7BE98bEcFZKBEG+J4p1rGZKl1CeBhfkYWxsQ6EcgGQFshzs6lj7tvhdTZ7yULVWP6R3Ne7ml2C3iOU7C9eWPMP51f9FtOh8U0GdYxp0TwyK/1S4bt954dDK1VeW5HItdlBwy5bYemFGYh+tbn8l/Aw/EbOnN0rwgwzKPKXVB3zXWtJVV+CyqbL7nWUooIEO7UgdOKjFSyF+MfK+MlSPTt3UFd7gy20JzbpInQV88DbVcR3UxmV8CZhYuTJgSEQ6+x/H0iPVnK4W1v87bIgFsIxpOgzcwAv5qNxTFFNdPUGEHEFBrbSZYCsJ9j+/SNjfYF3eH2nyOjUiyhusLQAWpyLhhhoX/wQHl/Eo/lqUUhz/vukyXktsGt8LIaSC+lM5nde2CE5Iy52MAiAnL8wjkGlC8NH4SxFmqX6WWI9v6evSEkSdXGBnrnbO1JuvCb+Om928a6L6FWZGMl5tNANxPvcTHMhqCHid3v9CaxNiu9FThTdq/Ng3VrZ+Dcm0917T/ZKybQIapojpIyvoGPjLck/sk+GOi3AjA1MkOzaTrHBBbjuqSR6/7tcdid7D/NP0oOD9kiu5ft/RKwHyxc7SOl/uVJLETy6KLSlxuQONAr1RJpK3GT3+0Y/YXGg7DwjtE12+qhp7OgcyFQ+5tQUSHd6Rd3Ehm4B3TS43CTkZAdsMJj8+W+lGBTy7TNmcmaUyyAbORIQJXoA89YCvVjeQvdfpplZRFq4Y8/UA6ArMQHuIAByX/5pAKBkZfwLkdGlQvGV0hblQcQpM/12Nb3DmXl93zpNsW4dgsALq4NHRFSr5le5G2CPcXOXqbjNWfrNM7jTkd9sARIcoTrS868/OO9Z/LYnVaFIQjRHi9+fA1tZUYhOMkN55wsh0NP6xm3Nw6g7NUADvgoq568BT7bAtgWEt9/xlVX2vpKi1O0/xQkY7RMg7L2UleN/5mDsVWVXQfGjcoJbXLMif6i3deYzOoDYjp1fLjmX+Sdlg6mgipK4LiZOMwKxeVAQlH0ZFW4DO6dpuc/ySFxecQL/ueWCzAlmc0rRazrWyVxBe+Pdci4XGms2j3vST7ybJrMlZFzj69nl902PD9rVEz2W6ii2hWC0mFluIAA0ejnVJMgWUVC327iPifzh2jDHJYVJ6usAA3EZTEFzb+hvGfBeNrtC4eI9HjXYf/mZG0WXvi2qh3LfaViXAyEc/qqQ0PLWDSEN45wUiSFwNT2rTY7T6FBtDIUe5jWx0/cj4sh2UzI1A1FevPatVIqj5f7gNN9F6m4JN7PTj3lR5Aa8l9IDE7MDpGFStkAgAUt1ZFRPGHPo4PqfFCzceV9QgdOsobZNWoIi8qh7cHC9xATWCh7RT1ODcbmWWAb9LziStvlIjrtmHC1pDBFFtMjmbWFxG1/p7KenLGRyGbjzGW1DVFJU0EMBD3+BT+1CKShFtyXgqwxruBwSxG/S6+NUSeiSpksMGbhkM4P2tn1BzFi7Ljtxgtb1m1ABu0Hz3HhEne5sbhK4RD6sKwdpEuwYDF9JhDe90gqtjzI/ZGWXFnSrCJW+o/kS7MtO9JuUhEl0zCKRfzWfyKQB+hXRtiKPL06JZ1goElf4I2auhxP5p83Gx9LvvPDWUpHtIYAjeg4pqGfmUGD/vvCilpNS0NwsbMl9sSMIhrGoUUAR0rqKmtNFh+uPw60+F340rfC0yoClotttTDY+AmfCoFmIcCyQ5AbbPfFL/s7ibWoCR0rpVmG0ZW6GgZhgIb6Vp4+zXvJAwz/GGImzVrUWSruH6yesKYkcku99rKXEX6pG6oj8HGvcBSFxSpQr7jlLPbF6Z4Zms1c/7A2a5JEzIgDeRf9atMOee74oMspygv/l3FYAnpGbvMf3eJ8ZO9hq3te8dV0eb5O7LR+Du8+VjOycm2w1KTrQkOz3/0+nuclRp/RUhZjQzV/wVUBglOREnO0fYOc25LJ52jpyah8Qgzd8QPNjITeu8x1b5WKln7SHj9vDpJ/zbvcsyk8I9R1MZXlofVtLfBgQ6MAGLhA63V7gFZRB7rBUn2fFYKaXgb16ODGflKnMJuFc9UrRxJZKn3Hrxg0VnCrVIaEHodt2ZK4fxHh4C2YyJmso3BI2qZ4XCpMcO0b8zYnPSLuhk6fdE6CffH7DLc0NV8T7/0MsjuB6JegC4ueKndPjeBy6FAH/fxQyyNH6HfSzK00ThKSbSKJv1LLtApE1GL+hdnLA/OAN03mXleG76hldSxonIoJjQ77GwPtZfcb1UWTcPPuCo6meJx5JMrnZvFHBqYsexAqJY8K+3X/oUZU0lYKxwRB3+/PxxH940IJjUA4fpvJCGZ9rC/6BfSaZ1CzBdPfSHVoxCPZ2r0s83hYp658ynywA6twOk7FQJvcebLZM8MPei7QsZAaLC5HtWbHSsPZa4TzRqcMRcn4Abu5W1WmpkPOkAAeLn4E8h77T6hCcQ74RM57UvLSvXNPn0xePtWMEpO7/Lj6AMNAoe9OMmgpZGBNYImuD5UbKQ73xBygZPwTGIbsfoZr6xDgy+9bnfY43UBuJGm6dW07J5b50ub6c55kP3u5jo/+2Si+WAHAJL6veeQt7FpuQhET2/OnathFhN3Ftj/9FGgKMg2uuESx6TN8G8MQtq0xaU7/OgGSIikeSjFXEdznJbSBw8LfPzd+CZbTJTy0lwNC2h8EwfypLM6rOHCRnJoIs+cPg5uKrptrEShTk7WIUfeP6wewF+RFwXPooFTuZQgLPwSKUe6GyrGfvFkZwqapcy2LLEpgNib5bDnigj+Vx7qARDx80+1ptL81/vs5jM/7BPrFgdRO5i7A1o+6gEPFjlp0UsjTgXi3Cn08koYRBj+l6PwS0dVCu0yaex2nad3Xv7w575ELiNcTZnLgLHJFKnGe6f0E9y/f1LBpEAnQGJ/8Hm5l13Ge/q3pBMHOFr/3Nd9XkkcpwdQze0fz3suMKNx0BonERVCEXho6LcqLdLZQtZ+CelYJYz7ymA1xZTLnN60Lz0X9zIGpeeGHy+e93W1MD8dCBGE9R6N3U2//mmTXGG3exOMhulOeMGDOo3S3WL8MXmnDOapX+0R+lBr161M7OrnKC+KVvcf+NqLxVItssz7ErTSkLQt7dbcPMoKS5unPeCpmjrr4wZwtQ4Kyk2SrksdqhLyqLx760fr6O6B6zeDlyHPNvp4v4UWemitlKbh//YVaocA7KowmPLcdmlOuN1ufA+iE/nO3RY6PhPT/cgtMdwAWysiQzNaP8tlV3947EoXS88TkivsLTFBXZmxXziUvMuST+cr67tOswlztkz66s5Yi0Wraew493JfcqN38FdzJjrf5P2WvIoFCiiJ1Kf/fu78e/9ZWF8kDYsSyqLlv1YBBwOuqpKyb2bmTg/95515/rRTAEVudvL9HMCCbqJ+FNZu0PSTF0Epp42vHAMVWot5BefFYYJE/mU023rmmEJq1UUNv817tvVLwCksJNecxHFLjHippX+dPN6LNeOkjMkAL8mOa6l9LisFKOkdNPh+VzW0/o5HDmGK3SXL5uHyT+u2U7nOp7wm4UTHUCOCJE352ISbhyvu3X+yj85MF9vV93NJfYLVwa4YwgGwgaB0vHIAj8PFtNKlblq+/BdonYkaVcXIjJ+XZb87HO+fJEJ7sVJroe7IYKBxk8W4cZj1b0qwqOFF6lPHuwPPyEG7mX4kHjY4lPL34b7HiK4GnstyLhWIXFM4or3Ld7nCq10PyPQu7kAJOgunLqbNEv1re3xfH2QJpBeWh1z5lRW9A1b2GEJwnKkpUfl8MrDvNDzk7AsEoEspgn6LOnMR5FquFO8CrB3NNYSWNAFxQHAcezHwlu0yvb1i9JZEIo0Ir26W7gLGpNUvarcZaCHCPnOogpzsFKKi9YTPACvLk2x8nKDpANXV/kNAjgnI2YqRYngft6cmBF/F3fpN4c4HB0SuYRzmgteQJbgfg1pzCYARedsemOf8Zp2D0HTzsx6XmzZ2iZlMsE+2BOLppSBXdpvLYajDty6g+grZRtPGHH3i2+U4reKqRV6zRz1AlAE0hSED6aJuQEYa/YAu0S31E5ZQyNUcGlCwhsP7+avFZB2UoigAr1jKEIldihpfG4TSPQmsWeYLvmnx55fMDxlX+uE2I2ULXU1qVMilo/3shBh4PkY3KxHBaC5q1k8T1nd9kGToZrTfsPYJuMPMWq5h9+J3VGL8qb2gCIxAWFI641uBFZH3T5iXiZZ8TSEiyDhHssr7xsdCSe3cUHzm5HRh2+3cBu8d7uscNUdJM43fzRJ4ZU8QQLnWq80aIvMydHr5CmqIvkgUDjUxiDlRKASv6fQ7Y7i9KWephKETcYHjKqr8a208vKGUM2cJCSBZe21wqnBAxmzETs/9yVptrUe+pLh5xpBrlgmDWlWG/xYroTL0t4wl2IdWlbxYyoSbfs0qFIps9Dbui1L6pjpiMMn65n9PZyVnRKfujvkUFZYVsPAf6e2PMp5+Ht7LufKQdXr+F32va16f6vV4U4YZ0PXAX1dxhNesY/2x3wd8M2M+PdNYQC/E3Ef9S7CfJojqc7hKSKHPWhCpddP22GsXpVXv1SNAWXZQ6FS4GBlCtP2mZ00R6Yb86lJMSkFIIAJQbAhD9mbw/ip0H1JcfsPEwxSiCGg0ZAeRFJ8s7Qv772YPWlalUVonySqcAeZsKm2rZY/hkNpb8PIdn+BmwjCoDjiAb1orKOZI958zI4OQwuprh3yvpatBtaBW5BPL77Q4Lb4WwHlCq5oRa47E6+bWKTeErwiAMWLVIBm+k7H40/ldUSIFA92ACDDBfZOaOU5pIl7CsB3n1TexmJBbdEOMD1HWgBvz01cWHao33IV0bUqwcUCiW4xuT08QVDyfYCx5HEYOQ3dcVZw/QwDd9M2Oyg8QKLx1K/EMB31jSqRBoTmb/mM1CQ//yuiEn+m8WCquLY5uHJqbDCwX7fRQWYd+pE61m2sijchdecGpaIVHLz39Guw3M3JcVi5AaMGQKsgHliFr25NnlKdUPphiHsFKjZZPwBYCgXJsA8z6tCvO4OpqjiwCqzRcYH/sdS5Ix9XmjJXa44vQb0PFjW5jD5HKblhiY/FPVJ/m6ay1sbLYrAwQXY0lCj1z8jCmEaGGnyqIOxjcGW/joW14is266PWRextGAUIvIQkSeQW76omBQT92jIOOjzBMXSSWkcOiRubyqbOouphC2HW0mspS0WWbDpj3GvUaLXNaP0zJEKjuMpQHEmqIfmt3wPZFuMitGMVGGqhpySVePkd/H5KpTAnWpxANAvv7NTTd1nUJsMqZF0fK+/KdZr5a4EmTuLf63osVed66r9BkkgLugDU9saBovOwuklBwHTuhlKHL+2eKyi+r8UMc88dcJyOjCB+W6X9dqPhiXsKeZomH/V+6wTBn5CG4rpkjpXNJz9HH6nbkH1iXB6Pjgjrk3youZJ6embkZeOpx6WBPgKme3gksh3z3X9bCg8DxOxHjirBBRYa6TlgnOSfmHHBW/7/nlEZIp7Do13NZ4faVys/g4RJML6vk7oSsD2r5FffoxM/lvYvWwPmcp9C69ZDyJGwo765Ps/g6Fr4LLZ5mykybTwrLBWV3lH7zXg+6GRFIfZW3lUStX529y8KvWjvWBesET3D9ZFOzzcMaeIL3kE4mqe0KmfoepcSzgtTQ9Qs9H7v8vi1UGTJB/XHT/h8VHcR5QcUJ2TITNLT3rrHWQf2odMcIIV0fOAsvPi3lvjot72PPtnweHVKstY2/FtSs35y4E7M1+QULf3sC9uNnqya/w6yh3gy1Ryf8BaKnyXzurtJbstVVK9E4ib7lmrIHjgAoct0WeMRzxK9NNq883cul4jLl09YYbmDQFBmPCKEfBISa2XdBr7WUGee3CcTFHW5BY2Yr+U6SwcQLMzE32nLRBrg94DyEbp/nEzxtCMqEtWeCURZHluxvbNgMFZUuI4gZEKTHvBwFQwLg7ewYLN37jGNPm+DBXAlj4A/QcQk3jReMFWDpjqrP2oQF/GXXKYSsPGKjbdY5pqqdPl8RY1x8IwKd0H5VVKb4ZgstcwVHJPKEvLeTFulNWkK36aXZ5HBle/fTSpiiBiIY5jlGXPhgtF3JHOLtJzasB8/orEldReNdDkFZAPmroNHNpZj3gQVlgVPQPlpvpPCKFVPJpDaIvsG3VNhGkxvzpw3FfbZms8GqrMJE3tw8fHiVCMbluBskkLkLM/SLHSd+zhaLgPBmRd8C9OkhcpQbFIXkrH+aVYC3FOGGkjLLLecDsmomsWle27v2zx02gOIqYXK6n0Cn6ooqlvhCn1r5IiH7nFfChvDcfA6c+hGhvyrj+ZaQQOJjOIBb5EMV2xcHpDAGiRM/IRIZ7++N7a3Z533EG7f06Oy7HeXmGE5teuH7JhIVBsP8TZSYj2x0T01i9GAqI2ssXeEYTXSJC1qJcWKc/vBo1xK0Z7mXiPDws29ByB34ZsOiD0PmT3e7fgu4mVGgEFd8YY3M5ELk+5UmKjZupt6d8FZvfbHj8o0S7niBqEdtEClwhL7Y/cuwT5u5nddcB+lHHhkbhQU6xMxWnoIxzMKgzEzXfM4mcb7fDWJ9QxlLUtPb0iYvRtn4t8Gi9i1gvCHTgcZE5Y6OFj+TjkNBn3FQ4OqA59kdFYoAvs4sCrphT8YqiWyJtX1/4phSh0YJyeiLkaRqGNQS0i7lPAOuJvBdAkdjH+ywkFBYGq4KBrEuwC3dmIAcA+zUHSWwI4ymh87GZIA1iySd83HX1Lomc913cFDw+Kk8guLm6bv7SYPgxfr2AwomRgISTyRYfzceols+46zU5LGJbgeXzb1+EeQX/qOtRH0JknvWQE1oGW4eelsYTSc3fmpjOL7s9Cp546sAhkJwEtAGEJHukF/nbTPItNsMOZBbuOTQjrtf/SPR5msEsIS91cBCOMsCTX8rX/mWhKzdVpUQYdJusrBSuMLDxrkeAB6AGht3WZl5BqOAK2eOEHh4kEDVR5/xpgVXC2YggqjJ40276bEca960rsmyY2qvkMab6NBR26/CAj1wgeCOCVvegc/g+oxYHJB5xhEaxRDd+8lghAqU2ccDObLQJ/ATE2p1hjbrPbc6dyf6MeJAoqq0W72tS5w2GawA5L0hSPI5crmaPdk6r0etADQgCvQCUm8wLRx/0BzfALO8J/2mvDzV41a2s062kOx4FVzNp+ww63WMO7siOvL/v0AVJc7dQ5w4agDtYRdDJ6jdl2QOfc2yl4vqe5Gik34I3tog0BqjO8TE28GHoMz78LJWmXHKM5G3vIYmYF9cCfCfhWaksHP8wT87Sy+Lc+q+uXsS2f0kleb6+gTKIyHbUnmq1MCNpT8C+EljSwXSvLwZVviaVT1hWi3zUZCwdYG9hWFP0D9JV9b5vaKkG6IkGg9RGcUoMQyaORNFhiVzX584snC5SJnBTG1Qd/Wv+NdcKC7REZc2BfQl4skY3SVDJ/MQjXLXoZSunQKowfaXryk2sSgulyiLx75EPh6gEJA407F6ss+JPaki/Xi0IT4XkhROAIv3US+jKK+HYAmL7vj8/VTWCALjmOoGRjRwdZh5Yzwpppcu9mgR6WIWxrs7OIurPTn/7Oo8Qv1TpGMf9mswdbDJTNLYQ08QneX5kVNZ9vw5cgC/bidS/o5DfMSTdmkJAW0r8XrqI6yDfJc/Z2uFSIQG6K+tfQT68aq9CM4wvHJPr57QXiaCPgupSTZX9z3zgHlSElfBqBpOuDLEY2aONXqIJDjBa2RDci2dBiEyp5l22jlhC4FjzkRy9GV/abyn5B75RMtCoMt5fKr380FS99qVZKpujrPq9j+5GQQ9Z4RUEmfpWLMVoivcVip+5RiRPRgDA1nh/hMc3sCygiIgfxd5SCJrtKKDaMCC0EOJRvDGOpxa0i6EJDs+x7aWAzdcKLOCEqEeBCwbxdqDG68BkOGLXn1MJOyymcgC4gGAEImErzXq/n4qvMgTGVR+ssaftaa5HGrlQYEpURCYNKN4bfok9s+CN1qm9V+pG7c4/8C9y30MS1NAmYqfXMiJ4aPqTJfJI+NgrobidAMWcmBmPl2nr1AmBChmBbRDeiW/FZNwUpl9Ant5ClIrq6yESBo7N2wsZAz+uyjCr18ZD/340iaXeOQBg2pauO8/EGYfMmhsAXIlUpwcio4VqwNmESztACo34AcCVjLsI2BzT/Zqv/pgpU1HiX9DB4MRbtL6TCJ/GQ7wfwr9TWlkB64Kpms5FhO9l2pW5ezqHuR+3zMYvHZJAGBw3Yz3HXz8BscmmNj/XNEa2DzcvREQtDNyrmFUwx8mEoFIz6Tb/BOvxvbzaUfq5fP7iqVcAXyYV+tXZ+i1Rmz8VBsQtQXgdpzvDV4DK1XqYSnkj/I4zYlcy6nOM/1lCO7Jpl0N0Es10XuZkgYBygtHxeNh0Ec8usaWuzJe3NrBA6527hk+VuqjNu6V0TYtRmI2Sl3cKvlFMRq7LFqrF52okmoRqaKJlCniLVipjAjP3d3vLtvQFrdc1kAlqV1KwjmPN14l4EOvYF+A3Mn0gWeJUo0+fXdX28z0CxvCVllJ0wJE4cZ9/E+LBwnWX8rAVMLX867Yh68cLeX+M9KHO+Q2bfEW+mesFa6c5MSMCCCPPMfxejYYeCf7WT7D1LFxFqj2sBNlRJ7CgbKzKWhVODE9Ph2C54ltIWbELUBPCvLmcnlbZcEa0YiU3bx55vlAIrqurDtuSpsAKz945rQ8EGENGRayCmjgW4ofCi0QDEQc79hH6juAwlB5CTJnal5UMI6nPw7cUYDwA2oWa8y3PjrfzuIKMfkayQtS4GD18wlJoDWtYS6zh+QdDIZLM7J/Rl8qTb0YpFr4rIMV7+Cys3F9QKEopFN1nF4s6uuQz10Doo6MGeSY3S8xH/o4KE1bF8K5NIeC2riUcKfSlXy8lNw9kz2pe1RyYSylUqhPn270P0JOfijh4OJwRte7PayT63nVkeTI3L+E515jP9sYk5DPIlWewoqgJhC38xK1eoeQuHpW7cbPKbOOa/jIX8qqTtbUKYj4ycSI4HK/kGnX50zPoR/Zeze1C+Sd22vtaEIan0y9z3L1VgHDZQIuViKhK4wWmfgAiyOxZa8K2AwWxg12oHCU3O8gNrjlSKw9OsdQFveZKcghjiWV37036bjvHzuuSATFQYBxcfRdFknq7z7ajNlBsC7Qhb6D3DZhDag3+ngGoOLTTuK7aQTWcNwpU+VDaQhxYSGUOJ1TXdWkFn8V44q4ijlLD0b1eBDDZ4LqqIK28Oz7RFevWEg4ZrJefe9zLoWBZbJSlicZ9O985pGgHowGiAS9KOmvyeSCX1Q90bfgxF/5VNebbf0uddftVesus1SUdX1AH6f2xqziLz0J7QnIO5AlT63xNDjJSkfTe+GvylhnyEHEkoW3zq3479ZwwqS4qB/3iD+CYgDxwpnoE9PrLW89+ejCpvEdpJkFNITo52gB1rNB7VnelpDD1f6xXKvk/4KKWmSox6GDomIx2bjJcA1KZ4bBa0nvsYECWqfVubzOLlUAXXyWpGxhN3YIjy893Lguy15DHZEqxU/8KPwQiA88LfIlWxU3v3NU6pUm4K4SpQjv7UfrILB/sxrSue71Kse8sIydpCp8GN95e8wdzRu8qqKstizudmCXzO9k9snYwKeeWExp9+dr2+toaa3EsiC+FRfKjHdVNaz2k8z4M8dd/92Gl81T2bzysY/wjZhctxBx73CN6ChCyWDRXAgud6g5pMCqgg7TK2DVoYrffeQ+2BzkSGWmEX/FINxvnF1fKLEmjXlH9u9PX/PAdW5QGrNMrnpa9/ndOfNPEuy8+Oyj1D/Prn7Uqbs/yL8aPCrj/rGQuYRDAE2J1Xa7sCzx1Abu7Jm8fS2/1QXsHh6zQGtMW9vi8Ug+KwDT+Mkm9BEYGPLPoNNbIkNqTZ9oz41DquPrl/xynJk7fSCcWymbL+9xWe7WOsH6SoNWWrfTuCS1pAtJn7tPNj28BEhQ7TnTqvDtAnCIoaqWsIrsARoQ89TOH9IsisPvJuRHhSNywyOm0Yxjsm7G+zEaCVQXk72yLIOpf4rfK3DUBROAzXPjjtL/3OkXISVEEtrPTofYSW274HccvGxFzQf3XsBqGmRToddJma65GbqJpUcWY+3O+vR5EOvuJzV4oThLEgjFG6nTYmwiS4ratHRUC/lDKAnGlaJnreTE2FrqdYQBEtobKFa8QYY4uZY8NPiw5Pndyrm7O+0yKTHi0GvhjIa/f7WW4BdX/UyVKGE4NN71sO1w85HCladkVNpi0iRnkS1qLinnm0YfwuLb/AWc8ve/L1yu0peheohhttWREYXkJNpn6/4MN/zFs9sX/hbVVOn3d8VlVCsY9mHs9GIpgiG6WAInO0wTBkSqlzwP6sb4Nf58p5DfieIg1Viq0HEO6JPjG8qdTKc2cIjm1qwlvYG6vL5SW+A3s/3qeqfac+JqCLhIn5yuJhl/fC8TwGtMTFl1dqNmMXT8waCTY6x9OcS6KfPiSovxTStD3Fo+JoY8W0gW/7zjo5e+djEfUsKoEiCxxW0JLA2JrUaQfadAHaxj6YLNeJNOquzZCiOnu6z9v3xcD/dq+vSCPUYh2EuUyPWpsV866pJqhc9y7RFepT+p3SZ9rmihKvse6JT7J/87lAtKNWgmR5+mKiNVwr+LCt+8jEHYbDgsbSFmFNQrn26zkFlQtZ/7plyoxvxb273BKCdjj/R/3PQy/i4cE3c+xAsdOHarSEv78r950ThP+znl0gM6rHS4FekXXpIQCITiyQATilEdEvDpZ43UWqXECRCHJOreZuxEQqEV2D4Yn6Iomw54H7lTEQCbrW32cOtx1omz6GJPzO1wDEkY1lZpwgF7mioXTlcELvnE6z+RoIEgs5ikA9E+b7oK3BU/rBhO7gCmZfClqmSFnsySCD+X7HgHApMHJeKtc5QDOPg+zz10fyLyxApq0OlQk3A9HSLYBSbJh+NWDA5maWhYRagsgZQrK5ZaRCTlwurDYr8kjgatst+aRIxR9hTfU6PQskWSJZ2PzLM3GdWyvgdrFA0aPqqPEGHR6MoobsVJisLSxG0IdJr9+kp5h8lMav+PwKG/6PaV9ZA0uMBtvnq+Xi0VQD+Z2EbqrKZ9jJzYvoIiYwzkfW9A0oh4nPXhNS9AWvk4kID4HVOl9wtfW/hyCZk1PqaA8xIcbeST8I5EfjLhhgO9DveLHZsQWoLVOzuHJoqFrXogjzpgGfOT1IDC4tO3b7cy2uGlGPHcXw+ML4aS2i7qmhMWiX66zT49qknKOwj7sWhJ1CpclhO+enyjVJfdt42+aWTE0kYkR2xzqCACSu5JIXvHjn2dH3HdfuPAG3xZUO4y8tb6RfWh3IfTgMCd8rBDwFasZyGhDHn9F1+Q3na/iElsC4a9TkdwVg53dR7FkJaJG34zNbKi0Rbs2CUTgvqak9N3n0zWzK1TiwUa+rNbbp+1PEDNbHifPjJvfUhwci4Vc1YG+3dozTyFOglGFb83zih7vVTqP3mXiHU91pLE6qXnSa/3stkF5ZtjPeh9rR8dbhBVWJKqzSqQlZwa/mtnIU8JcqGpLuW398Vs4niYHk+J3sDwsP83bKNkoVKU8jXzb9Z+GI8RmecIfavtpZTwQwrT98jnl+okIl3Z3qUDhquktfVRm754iAXLEIuaZGxbwSI+0dqYb3bO6C5A/6BC0S2mSqkXJMA12226oGkbq+2XHWWisqKC0vKjfF+CuT8jEOUHyfuO4ktPVn4vHhJ7g4X9Ck9iRbpBbt2a3ohSBOFSbv5SdjCf9KTHcoID/El7OQLpkfw1AdIgoAHSv928A9cLuhF+FVVxeSdtSTGG6TUrZjyAohyIcc8FOXz82r8E07KYEP4r+NxgTLfMCDCf6wkwUt16h+ftx8mmoCx8iHjjHyzhE8vAdtoJzDfHPDyfh31t1L562JKO6ki5FxsNIaSI7jKJl41pGdXTAO74cKkjYo5zM008rBVRHYjr/mMG1Ak5KaeB3zxaxhNj2g/laL4r8XBzJoFQXaTgFQAzFSdpl5yB7qvL/8hPoKDDqefkSpjiV94zEw3wEqOIKIzhyS1wqNUr72PNK8FgBngOVYIGoi+51jN867kRxZIykQ65tFUDXs2h6mNJqU4YRgbWtm6lm+290SPLVEvAFsrE9jAT634l/5urY0I3Wpo/yFjz06hxXGHQWFhQLc3UptiLXaLVwOOSf+6ozjOMLFX8YyAQB5KwjjVUKYxMgr5vDOyryGgyD3yXmI3JVBSAzKGaDfTSEGDCzovC1DT/yNAHnEP6BUIUIh/NwcRA9UuE7l6cnlGGb5OCV009nPnnMMKKIZaKPEy4NN8DZuEhmxkC6q3eAjfz/ybGvQeYAQfwnApkc1vW2B9jfwk7vLcmT41JXvAeHXBUsrfc5/+lIraUI9KoGG1HNodzHaotDsLJB2OmBglezHU0HaMPzVci9wTmKUrqglk6vnLEqBGfIoYVMVoMWgDF6fbjZ6RinhX/7vKUIaHsEzecXkYumw24fJYswfpJKpndR33lM2kumYXzaS3XJWG3U+GUYsoI313sYAEN4izwEaAjhFxRk7B7nB6A6wAO1RCeI5a+lsrqos+uEFeEEfQFznKrWwB4rxyroTqYupysLjSm+7wcSp5707CsSsyfv6DSM1wKmUZDG+74XaAX/FrGBaJTNv7IO5UM9im+DuY9UnmBVyg+9USYiXAiSKE+Z+FXnFETWmHXszqFJQ26rQvyQE8bysBemROAk2Lz0U/fHbhKAMhawpElHo3f7agaBHMyeQtk/mWGg7S4YgPDLPX3JiiOOaRLiGX+3oCoiHnC5I8AgcV+z+mqJyKPhe6u45gMUMHra1zo+sfPFSjTvh2jBS7gOg1g0NiSm5qomQL8y2eeSEbfv/M7k1qWi2pSU6Bd2scUhh9uaB582b2jE7G0jUWqIOeeW6/V7JtNbIejbPNHupzsxmljG6dQ2lVcc/+qyuWfPXuIbpO3CoUVOFcMxSr6Z5QFh8TDirJRCma8IkhlsTUeZVwmcACOI/C9hvw6jkQTigOLKyFgZ/Wyp3ORgSGTvzK3y4bcarZWYIYMZ5xoMDNtz/RXhpdEKyQGyc0s3RaeFV4zCIWjbddJK0H8GP2X2RIRctSpAdFyde2EYEBWS8MRZqdmFIrlv8SIwzBh6SwZCNfNyuXl+ZFyyzUpAMW/KhD9IBtzaN09evp79YOfpWW8Q8FeNOAojEZer+oW6MYko/0GeaznS6jaavhp+OD36HIySRVZZUIOiaclO9xshGwkPxZogArd6BwDomGsHoXc772LK63Ly3Nbfo9jRnP1QuwoERYbz5xwXTMbSLXSFEmIS30Lsqd9blQPZtzixYlKXZfubWdiialQdFQHfJ2qc+Ay/1ejkR8ew4hdCGi7MUfSKJ/ys3484ATO8Vlzv/WyInvhpVVzg++KSrASkyq/wtD0rZg6IRi1SPnZvOeiGBS+PEP4Lc4KTKsqhAxyHjBeetfbFn+vcrHhKGr/CYLFcU8TC1L6SKjAOKQNXlfmlv+CpcISJIogzbUdt1cTeBa0Uz3Vn9UtZbKxZECvsAK9gXhn2He6YpD83gmUixZpRXEVw2gTSnRqEPnlZl3/ObnZ4H/RkOsRM+MhIdxyDjmVHqy/5ixpNgQLW9L+8t6FWr9UOsNF7GQIOuTOqeq0N9QIQdRwfcCKaCzHwHwV/eUUcc+9c9ZbH/NbWg0FGH9OSTd8a12aYqP3dR4zIvXMlPlIthkfO/P7HhEOLHeb976m+gMZcq0YxQs5k19DXlqV4PvJwZh9rBS1Ng6YlUb80J5rbZ65R0ATdfCBkKJxFrAIlFrphEGsvDTClhZ6VSytA4Q61odCtLisDOUYmqDBKZ78xFlI3E4GvcPV+7EDgLRMHoncLCoZyeQJPkWTzmX4IuH87bI1GA1mO0b7zSUV9NVnkXVBofDiI4BQDGFHqKE+5DibyaXXBYko6M5CRURx4xn/WsmwWqzW/XxlpgkN+h55YUAbvqNgAORb+kAwXemUe+O0p3x6BvsoRwKl/cUvRXnkZ3VqQAtvUs4fBacP7+LZz8VB0egcwmO5iRa7yz8CRElxXIOPOGO7qNMNhZTDLztQki+uWdtlPQPiuyhM+TtbXQqlkL5dyFFHsgyZ3V1xPKjNdxasn7tokgFNcSI80oeXquAJaX+fyZgeRGZpCribJU0IePc+dwqCOiVvcy9qJt+5xGqDGmkYGFoi6kdR9DwQu3pflUxcPgxAJZ0Yr6yoUq0uDu+ljZ7aG0Dluk1ChvgKfH7gDe8kRnR4qYGjP6ajwRIQb12zWuwX+MJykVOah8I2Mjebkrp8tzrEnBRckWOo8VbSoCkHZ9cbNotA7ZuODK8V8MErecn6vUDs3heimfGVfD86AIg5r9biyTknd0ey/gq4BPVGOZv5npvBoRkGFIEIgVy+3iyf7uDJozaAtU9K88Dl9x6pwPEJ6ZoViUs0SXrhrgERPwZz7zQ5licPabGHy+1a7DG1s3c8p2H/FOxEbMFHTScHmTepJXAUGfmb9YBjvcu53ycki41RTXVF7TQlR1fwNhIsrctF1i/QcqLi9fbPhkPRek8ksIYiUaEZGfEsM69Ig97QPuz8aGWuewkO5bOX2QNO/G55px8MT1qB+1eIzuyyvFOAdv/c4VN/hlN7WGdYekR7ssLrKfw9TLm+Z2Ng54iv2fsNIWCnDbKIKjIR97OprM1DvxQtfZtFRqPlV6LsNr/jnSeLRTrAKDI91aSam/KGu4u6ybBLIGjmGff02iHnPFepl6E5VIAFFEDiszIRx6Edgrr2IoA+MWEse3PRKDN9tOF41sX1Ro6WpU4OZBAQY8kGaNlqel+ePvXnhUwpaDA6gq09e4M3PHd8QUWKVhKCLZV87oLEA8CoB/9J6LEPORYIN2fdgzptJTxaHbceHeeQJgzfVCCK0T2VlCXKkLF61cVp7Jj6IXiN5UiopEf1bu/UIbflogArx6XNeO3ioYWE7KfERkcjYa2ZXsnXTCjNYV7FFlb475EyE8f5F9XDrnHkwz1xkWd/SaTaSs6XQ3rts4t7hUhSd/1Q2joYk8jgm5ehqVGUwf9zVa1DhFHkuxp64ndIlvKTHdH9pB91Dqtf/XYTmcOx35guEQbEEv2EeQjmKNLe5g0Y4BVXU+HRPgKWpwsV+l1MMijlNIpkyDPw3rlE+D+JUatTqcMzfZJ9PNSHnZAJXa91Mpa9pEOb9gaRSvNbtlk8dKZ1EXxMCtJ2bDFgK7d+0+dOvFsDrPhfr9/03mfH2dqwVudO3o08wbJIOQVcJJQg8+B/vJ9ScxF6WoMnZeLPuevsNL0qWH8NDwPyPWn+qTydvasIlFhTYssXewAtVL6nfrJAr6pQVNe7vtucjyzKcKfHUlpnOq/5mNrwQ/a7QCKNuUEU7YAj0TiNi7B9lF13mPuexx6r04S98SBGnBWQwahuT+GoAI0SDX8UqAIyzlI9ylpN5Qw7xSIPF8vnfRKIaFFSqR8aOr4VJiA5KUZp2oFEmYonSIgTh2eF9WLHwjRrbC20G+Qitru+l6sN6Ya1HfaoXnE9PMg4ORvnM8mGym82IL9eT5XjZMWrBo6tinT6XG5KYyju69EjG877IolGb6XGBZGKgGOeCTxK6v3dkV0jv5xRmwYxDpdSrSAkZMuRN1KCv/Cr9VPsdlr4syzvf+vjdb+nlvbvu5YR0JbVTX5KsQptn4u4wxAhpAnyW67s6YV0aa9BP36L7rS5vL9UV3rIDSZQkuMcsR/VZYRlMNonWy1zFjJXJwbi8LY8Mt66cXxUBEfUBWbL8Hv8nLGsiYf2GJyY5ut5JMdPCi4gvJKrZ/cvmnTdsf89AnDqKtXRAiMeY/iy+cINBkwmY2oMlOG+hSxyETBoDzE/ssGH4ci+SgsCBc4IXBHeT44je78bYUQiv+yFZulniNnN1/IxgN/1m8Ca4OYreb4JOXoIKaWaNmLDf+cDuPlO1fOLQne/8+7ikkVTfKO+tvSVO7eaJmV40sJREJ4Hs7HmvFae+p86aOihrwO9OV9suiwDp8teoAsRciAJ8lSNaHotYlLEfwKluYaKla7ICYoN2BTdd732CVHYsX8iZRoB4iR2I4kq56czRy3/D2k6z+Zc1hP0j8hv8VsmReF8/ArvM9IS28ecj3jFUDrJUhbX2162K6rkyGr6oWkXM2ZNIaBfkAbyhNi231lyKr/qbp+UiiKkTYe1X5GFsn7l3TdSZkBc1w1ey3FIC02sOyt4U+87+5u3AElw/XgidJ7UTw8e+9m6Qs6bi3es8RR3cAs0r+v+QLEIEgBGt+1YUKEJ+in7xescxE0Zw6dYKAQsH5+lX1i4+IXEj5+PCUqyCy2+FqqSYOLlWNM2MabV/G0jUvqRcExrCE/SPd24BPB5wnnOgQljbBVNCwIUBUSYrrOLEBDBfMVXweuhRQRDENNV0qm6qWZ/APf8uZPKx8Zhq4d6Iy/gw/kkW2QCkXUlX6jhFrkvOIM4mWWi76PmX8e5x6KBltP3rCD2Y7VW1Z3lWuGtldqs2qDyLYkVsg5e7Jtd+L0XlwwvfVQ7uCgDNyKXIQi354IIG1lrPZo68TXqJG+IOhWVHMH9rH9DbJ+bBtjO/fviyhbJ1Z1w2u+LD0F/BRGJh7Teb5ho2tTAVwyQ5uQcyWlFvicjehrJDHrY4lVwDYHXL4BwNSbgWq5LyqzuDvc4r4bWIu9ZuN4EVM5QkRC1q+4xKlMvEHnpvFWpn+aMxD/rwo0eRctTn3ClNj+5JHEoVWTjAh2l1BiTW8t3jBz6BDPK8c0F46vx+ZhVo8IyYvuURSvPgW1Zv4U4WoCKXa98oFzDK59c/HJgI/VjwwWsCRzkxm12hrBXboq8dnCh7GiW+bzlF6dfGR8qw7Ud+WxLxsq7EeHyCoc+gbvh6BjJo/bwzzb5qA/3CTEiypAE+6fEGxI0Hcp2MfsPCgOPHo1CE+Yo9cQYRAlExCKrq5PfsvMttUFgiMeUNV64ZS6HumzdzGH/esBfmMKCwcSyKqCMnOtsLWSeKqcVqOv/uM/i5Yl7mflzH2u8tqW65qTvb9D9xgxR8mNC1VJmesaegUdhShVJgFcHz0RQRqTmqEz7HjKOfOYKZqcUlpRxLavEsgVWNaF24DAtVPJYkhhFwS9ot/LVtie93cgc5LBcfyMl4Q7ckNAY7Udna6kzz8CAahNAgMdxIjQfZZptHparrUDjbafp/nxz3kjYo1ryqJnvmBtPGgWzwiMUk8XL3AC3niTUErUBYadyNt/QAiSEP2TZ1UVCgb5WaVahRnRsBwIc0JZGzAu4UwD+uyFXjrbiE+zdY+5hk2y5dUw3RyTAp0YN+d9s43Y5hilQdWp9rNfu0wjiTBAT16ANCib3W/uCbCNVyTyFWjtJNiaSGwaXbRhOWXsQiw0GjUpJ1xg2FbnIvkUU94y4gyEVRiKrwYAzNfJvgkM69iL0B3//FCxyT4yym17ZtQVYoPoqQMbsHh59/AVLEkPCGZ4IUT6SooWokaFEuThCV/uWsMaKG1yFU/MPuuNXaUWOgyW/VZ2o83dD4cETZepJk/IAr1Ohraq4QbvifqRWx8YgTvdrHoR3R5Ou4z8G//B3IZOnjUz9uGAIHWJDZ6qe721CO9iuVObroSqxEtDYskaRkC2UiqQi0Z+k+puylkBvBzpa9VtXcRtJ//bNG+KbTbKN8jb+cJl1UtnSi4KCj/P1y3hP1XShVN3sfy6M+/AeCNM31ToVqrdKysIa+n6rlHiiKmtd1cNV6d0RZexi0LY+9CjCvRW822DVydpC0YTs99bS+El9aE1OSVCORYYG1ZHbiLIGm7VrsnOuHMhJ0BB6avJk4e7qzuIzaJjng/ZcM0+1atXVRzeRKwKBn8Sgr3mbi98NfCNkTLcvO/lglwYT1psbNeL36D8agrwXP3oWwRRUBkrBdmaCY/mqzJ/32uws5jd7Iv87foRY/JmORqu8WGb3W/6JsOoTpgT0MR3J9hLpPDp1iBxuf3Hb/u1XhTBwZMqtNhIQHSfKt/QN3iwLDP3jjjWr7AF4pvhByB1EX5KzPU9sDEnxm9u5cTjpWJ+sRFEDLgQr8YOyjNyVJnpmI7Dp7RlYGvNYv5LifdcbuGr2TXVEqGNWthKrdlOfJsnfSIV3V+eD2zL6+uyFAe/rs96ifZvJDBl/UgMoC+tOpHgTSuRkN9RPqFDtBuezsduYsAUIpv3yOE/EdZUr7WhVBW4vCNw2JivKcuz9KCIXhnDFRC30gIE3rTrx09/053HA4COaTPGj6Pfi03Goc6gQbrzb9vx7DvWxShp2ioaNqt7bxo2ClkJ5w95NOGpvPFZQGO5AjTpNSzad2/LpRbhShmQoe2rKCkmr3HRDNQBdhgZhXOkKlVqBYW2DvrhbXjRZYWIH/Hza5EqGpXimrhLSF5yS4RBmFXGnh3t0wq6cax9nkKK3HTuyJf6sbu05n0Eap7et5EQzMhnqwYpVNG1pJ1JUQvljNTl97sLIZ9TNEEXhd3f18CSxy8NRT+oI5qoqsngqULrfVVXbgz92jsE798ZX+cDHqwENwcfF92uivIc1Yklxt0p/cFnoMZylCv80wT/SooR7yX5vBo+g/2ElRSvm3pphGyCqVxKM6Tt0nd5y6RqkaF7kT7Pl2yT0Z+zcYbhx4N5XniUAZJ0o1p77CQbD25rMA1eAyvu3uj+F7HefqLYmsqAA+LAWr9WCElxS1J9ejAOVbtnZknLubhp6le6SOxfyBYQoZYaYTucVKSuVRfLO2/Ag4yOIxC556zW93G280sNK214MKlUWRFiw0FtyV6uNuSjf6lEC2FJFPYS2RNWR7UiLoAc5xcffPihUYorWZFKPeHi0Rn5AXx3Dk+Mutituk3eAuDMsBnA+uxjo4iPJaoNs+T8HYg1GZibK201Ou+r3gxvGCCIS1yMj5vRKyLtpjKfIi9tjxu7hc4wv2cHn023AuW7joXutCjYJ3AMFBNq6jw0KiyF0EWdu8tcPpOTClHEMeL0ZRb3LvYNRrEAF8NvcpGbGQlT93VwfL1WT6Lfy+8n31IBCP/ORlWMbMCOwWopfJOHDNKwKYKsV/kaA8vabSUWf2qf5UOSQPET4/vXWUu8pdohHojKOOYeHfhv+1+jTI4nSpYcP3crgqCY5ZzC8z2t0l4AskpeCvRdQJJztCjIZ52LqMf0zxl8hudPTC1f83EiyE+0qj7ysjnOzjlOgvRTBMVRYpzfWCwvnHwronN1GpNcSi2vpg9ix+4JGPraoD8rQ5m74BhFXbfK6FOwI291QiZUzHjLwcJbfNOPdr+9s/qPrASl433Jygv0ghUChv9rPqbbJXXTEDl/qk6UjkHveLSPtvIUkuc1lmiEAS3UApv8ErCYoepLKjaIICIiEci3/aUjVZAgWTfVgZLLAvnQXorv3j6sLnCHUzqD83vzCz0f0vkjAYswsIl/054t9+9AVo8F8J36JY3EhqwK91Z+QfPOXfJr4nluw9TpPmDJ+cwAG0xtAqHzLdRadG8h9zqp3gzEeKVdSOLVwh3+41ysN0Fe3E8Lm931/Efdc17oOUPBQzTv7Kdk8gkHE6qPKWQkEoKsQS2Pz9C3b5sYf8qqE66RL0Pndno8VqNOheyPmOGUjLhfOLIYTJJmdnPq6wIMT7R3PFhbmiW7bakh9EbxbORJzW+4+p8NPigO1Ozm3ec1e79pI6QyG6ugSsDtjz8yU2eYSilNDKqcyO6ethZ+m5rCtpKE0NCimRjt0kzTZ1TN3BGqLs23/wlvFhuasUZ3VZUk22DgVBUvGVr8MUauUhzFFR6gpgWQo17AapXcORGzvNtbsIIOqM5xga8LXsUSzUwqh4T71CCPKoJcAnj/0imVqdrIH2sMdkIafJgY7Ssr8+IVi+RNSDQmsRziPex+Kl7ztLrkGHdYfASLR578SLfvyUD1PFjbuu9y2UijLnND63iFSrCTkvMrXT5kNeiV6gCrhz02A6JBickhRgf0q9BYl60JnG54trPqaLtO9nURlGB9VOKuz0I6uK4kcnboq+7brCO8e8iZUAxWajxD5QjHeR3yP3sLtZYdNqUEkpzfCCUDv1oj+32PyA76xFHy6l9cgcuxMF6mkag/KoFfTjofxB2HribWAZxyH26i/NYODUZdmykCiekI2nKQo7TPgcTnhNN7jPZ1WslKSYu2h0x75B/m+y4nnfGeAaj+SyCf6zKx9O8a3o+JkFFBmai9C2NNdjZu1CIJo4LBRKxR5ENqGfG607jsoTxCB5frVxe9rJEQH6J79ivaUGuED+ZnRph4vvDnc/9SPhkakY2lfwbMrGZjrLw6et7scvg6QP1FPxzipmAO9Cqg7HP6BRbACBTav1xde3pxQjN51XUtdHzmBa5TQP+LDLWLuihjr5D2074vykE8xGJ/ckD1lfZpfY0nmrQoEdhZfsBBAOvgihVMh4tShBQ3pg0MKgCsSiwgeze9LFt5fWv78d9ZoNEiCJHpt16qpN6qKSG+72R63qq2Gw0hxSbiNiz1s6u55fg9DU4xWJxT6M2f7RrkjMbcea2G+PgQaH/QXJYq14bVBmH8OybnRKBZz22htOqiN/Ukk2trZ4k44QE2/JrQbkit8j3H3jyXBacKA8dcTgTs34yYToFGbTfM0Qx/v9czyLHCWfUOL3LswdTQMTJ8HfuE2QRS5IwGUkki4om1tLfg9wdIWtaYDmiOymlXW+5JVriolyYdK3tV+t4kbgqVWDcIRUC7xHp+BVcBDgQ++27sWrEZnUqd+i4ib0rQWCrLKgcycEePiXKXyVwryHSi6S19UGvOPflqigOcQFogZLLpq0aQo9XYVpEmDHWxNT/7Sxwe4bhvy+nYINR+SIp0VPwVwVLB2o2PhbF7eMTCrxLQKZrXqrxuYereZQuuXdptQBz+RaIgAGIhxIpajBdqLMlUXXk64p/F9fonqX4rmPjCvUmFcfZe612aaFf0E+YL9maspNDlQg+yIXiMjNFz33B/CtiSf5mnILihOb0ltUGUeFD5P3IZVzwI96xBuL7MJjPk/0UhA+GkuOaHagPNK6zwDjhKcxZHb4RfbAU3xtBRfMA6lQ1HSGtV1hAWL2TBvU6cosuxeMyjNvpUFS65pwCpmGrgHftEpVafBv462lnwvU02LqS4NJ5X8t4V+PxsqOOlHNyRAuU0u+Ck32OEiAkgLEJHBMiBvo8djHiV9EdKi+yfx+abN+qEuOH0xMg8/EYK7oyUn+mtfZWVJoi7+CbBhHy6qptUBFkgmSV2FHIOsZBHU3lWtf3/UBx48iidLADncMzQDAOd5U43p8IrtVPJYMEwPWjtpc8chkkXGsOVZUJrbe7bCGRMRPHd4sieA2gpjd8dlbHhXucKbXlmjnyViZOD+/T8bt5oScj+hCTGlpqq0vDharPIYOiVzC5h9IHA2qLxbkxZL2EfC0Iux27uoW1emGspBtHCOeKwIAzDzHfuLdOMJsS8JZtMs97XfxxPsjnLmqPp0GCnfEymv4MHvkgVBllattSQbRbfu96wUMbm0fV2Vgr6KiUHNwbaY1yW9PemL67iAjfTUN3N+WhLNEcTv3cVv3In+oVKtmzR0StDpivu7dP09QUXSon9MRilgqgqlmOjSQSBmN3phqsLtgJcRQkc/9KSPxTM+1dJai/QT/sp2A+LJQGienObYsQONTXkJJUOYtjzgVT2jJVFQyB1LmRqrgWryBoR+Z3CdBeuM/u48N3wKyry1e4rQM6dFNFZboYIw5sj0ozGvWqHTisN2JRl56qZPQ2kvrhFSPrlgqC5MzLhULHEIcmIaRSvX0lj20tAQNn68kAqMwInuPMG/XWkZi/hE2p8ks2ekpOEBziI2l/4bwL/B9OEnwn+1bBlqZRLVjWKyIWm4QinZdwvJjnK4aH1Hms2alGrtjqtzWJ1+dhobfZ09w0XIUyB3u3sOXz2jggNlAI3oZYU2NEssIm4fdx8uUiRKutXekipL0kIZeQhxLsmOCV699E/uuwmMYkvz6CzrL/d+Zv8WXMujoQbwG4v+gHKolYOw2n+9WP5+h58MuyIUHZFiHWDgUj2wxzmRP+8nGimVaEJqLLwmiVuOCBvFESWxqaeszm9z0VLX49EQOUdAd1s1qs84GgwOZeNhv9/+3t8+SahHJw7qckepDoyRGPNJ4ugom0sC9GOXPcWpvvN266gMyl+X1FaOenayRWPUB2N2empY7pTXm/LWdQx+FcB1dBhB5FoXkKDGeOXAMBEQeIEJBwJZVdQfhyCD1ovqEAm2qEToJZQiMJiYunHxMlN1JVlhBNlmR8CmSZIGI0mSyVJ3aY7HrVM4QJ0pi/c7i0V0pIHI7QqWE8bRCvClYitu/hE+acYiD3ng5lTA9g1Wtsh4BLQfOoowOSuuGsy3Q8GO8E1VPWhpqFUXu412fBg1Vp/xllkH6GA6UqWcrLeVzhacoQAzJIvWrZxai2ZbfDGYHnoCdEQ8fHkPH6u2ajw2LtX5sQUgOQ4VUNUM4nOGWXuUcaf6yZyC2LG0t+CVsYAFo9aB9BdpZ4KOrFsbdAZujGaWkxp1w8by9H1EkMR4wKq58q9f9jg9yeAAe/kZPLg2wK/+ot7TZi89rrjhPf2TbzK2Hi+zjnoT227XpdP/alh+GSWXVUVGfdapIzOsyKZoFhOSnXXA+jxStchuL/WRoYDMaTmN0gRwGbBxUhSzrwFGqC8fDKU8wSmAoI0n20bu8RY5MD7CWTshjtKFvBeIGQtWGFWn88k9751N4f6yvsnch6fxwljNAcGwsRcfvbeYblRX8ZqOkoPUfTxv4oc9ydUhdH2wzv5HwD+wShwiQxqLG1HsQ8GqWCO5GhbU7AUBut/NRSauw5bzCRrSqQ70cAk85Qg3LJeNqj5XKyFWbyDnuo7xePgpv5AsZlt4pxbqkPvvV1lBu+EKlXGDlqjk0qNoWqVNg4c6cagWJie6mDzLku2QotJzOuQM6uCD9kIglDJAuCs22MJN3dYpL5tw1EZoQ0zJ7gsDyB9dO5WEwmwTrmo10K6sIdaxFVfPS7y2cLJEoPrNraQcM+9fSXyPV8fHlQ8tYYPwgWaW3LZouM52ZElGMogL2Fn1bsET9OtZGHWyJ2xqgtNmJr1Xlr5hzarJH/ZwsIuhvQ2ZmLcwbN8liBgC8nUIgwUTle9C74xl2Px9Q0/yPfoEOHDBq/F7tf1Sm7pwTjG5fgmXhVJyFj7PDhYAjkTkuj41og3wWJVJjx16/BW5doM/nekvmTAip0RYWs+ddVuiHjL5Y16NycEBcbIyTRCZvWu8VUQVfwyWoaAWkvBfRCx0bOzuNZPXnWC2j2rhRvOMs2cuKTHpqI3JYrGDauEqMYFOLeNw1Ta45YG9q10WhaSAnfvpI/TIb8lETcf0vMR+eNA9Smr/6jWeW1iMDNgFB2kVihzZ/dnRqYBEMbi9VG1EmG1dLi4cmfjItW7ytV5skXhWtxUm882qdsUWriIcXnRw5h3H+qwHnaExHnRpjfjh07q9tqX7EOZf7wSpHwtBgU1AItNbosvH7sgGt819OA/4TZmR3tvzkED8Qzk64YRfjxK0RjXBF8UoO3E82xr7cqM3VB1Z7ooDjDrSzth6gGu2Uw4IHZoa3Y5QKzxf9YbxSUFZahaqmAu2PY3rZZL1PGoGFedbhyvI7w2exJhIF9mQ+O2TpY/c6HwK/ov7LjMXWExdU/VBeXmYIXoWBjpx57iJucdT59nnGUZWYnlNODKHDuoDetYh7zlokEBmXaH1CHDQWYOaxU/XY2vceUAPSqowP7xulhMyQ9zmb6Mdp2CCw3S40XpYtBv0GieBzFglfNJzimspJ1OarMUdllPfB3YRZtdP8mVip45nYfXIrKm6WAfsv0kwswRuot6voqJP9rB21d1dMVYUr35V4TJc0cz4i4Wz9k2AuD2AtYLkFNOOL+syVfa2GmHclYWaLmhy9VZ1oXRzRCBh4uPq70lpQaYszeeaokaDXcnuF2MhE4Fuk/5gLqgCl91m3Czf/g6KSk4aFxK5zrG9fOew2CvdXeOlaJi3Oh0ql1eJ7sUkYh0k7wZKbgrvMgBw/p3pUC0l1Iys2LfWn49864xCxjwocXUb7e3iRe6O4uMfnHCgQmkF5UYjiTKUD/9lM0XOh1mzkLM+vmZU755k9Va0zCzWPwLa+e1H8z2bFrArnw3JI9c2gKK3jNpcTxwnTzh8id4KrpM+1pBQfc2UqHmvnNyBRZMd6MU1Enh0ZiKf3B6ytVDSosPjKZ+4OjmNITRW8B4jvI/0CAcBU+a5Glv5RAX+gyb6AdUKnypjai1ZSIB/KGHHuU4v81OxXx8QontBr1CipB1wvdTYDnnRetiPc0h1Ev/t8c+LF9mSw53XNSExiqIThizPJ3wQl/52yVw3vy9sYNPONfFXTqG5cepMXKKKH7mg2aD12y51G/6nFHLAjNjetelm6yWa+FbZfwzkkw4aiRMqEDSI8NGTBe8jRzj7QZ3+xXadOQq8qNyw6yVjUdkoWVG0CaVWCF+5WxU6JNm9mtVj/40XFiwHn6K6ZnycZPz2yMKhgwuG9R8SnVAMkck+1dJcScIRab0hDjQil4VsUufA2Prq1wzDKdCEezPJYJIWRqeeVVkzPIQ6kMkHbFDF+MRCNKU0F4Ou7ko8sDjbqgsUkqt5SXFIh8Bz2dW+XTyKZnOd1kHHmAxhfFwWNqOOzZ76U0dJXc2jTLnl3oe6m8q9SW+ZA4TZEP+xfgNXlHcyMsQRd87/49SDcPJkw290UvZcMsMNdfkrD07GPgFH86b8HiCOgrDn49+q26LW/QcYn8qfYZwC/M3hXtNn3dGbECvBu7FkVGHsOeOdZrdjunvE+bWsuXAb7DK7UEc21YdU3jl4xf3SH8PzwX5zaEv07KRh9442c7NcSpW2B3Yp6ZVnCgSxp43fr63cSD2xZ0oJouq1wdkhm8hHPvBivOuyQkBVWTwI54jBncO1fkBAxdAmkvEWAZ2yxW3OIwEWuRBNPJkOz0ZtzDOfNTSLk+G1GC+7i/ePjfgyIjeP5TG55ebLRLE9TPYW+fIJeV/hmlmc1JaFBk1VdL9pbtTAdpnrvsekad4j0FH8pwe3yVUG/IsirmXz+BE/Z21iLiuidzguRVMVi2ss5e+8uhCazVnLqGCAAer/lAuYUxoInys2YLSz2nRVtYfMijUCynFGuil9U3qNG4J1sEwdoOdkmzCnfcVeJ/I9hc5/lEPIvuqmlP2l9X0vOIimYhU7vRkt16D9ou0EByjN0Uk2C1qwbH1poCdnC2JVNJnRTt3Fl1YV/HTbRPXsmBhKIv3fTCPbRKkQjoLnpSpqq6YuA5+3Z9a7u0UT8+dxmk4Y76DvRY4giU2n0eqsP/Mjzo6GbEMiVRrasMonSQwAkVCTQldo6XfUYqJCjIdXVAuw41L3BpGws3sXA3kMEG6xQKKelJFvZikoL1xvWylwB5ySiZAM1qRLNFA9vD2Gul0lv9m+eU2zorEvxFy+G4X3mzcjKKBeJirEYn7phciiuSeP29maVtk2zSnhHwqdFSa7Wc1gSlcL8BjJEl4hsArwhlxZiRmRrdsNM17s9H57PlfCfXmTF5YqpMcdZRWa0OqGXANKJs5EqlFSTS5NvrGJx9EoGWHIG8j5MtS8w5m7agBbbNOTAkgpcUVxJwgy3DgW4NhVDkMhJeUfKfPBrwCydsQ4KfKcy1SNjvVdbtqdGv1o1sMBhHXofgZD995H1ofDm5jBct0ey/oIhMOcVgwJDeSzwRMg4G5oDnXHnUqT++UeTKkMr95MGi/TEq6ngMq7Ue7LZ5QqgnY8vobWFfQdMpwnCTTToQ3urG01jI2SScf6oQDANa8mtX2FUog5X+Ef2StMzPz7Pc5THs7ft34fPfsvUitrst/gNxGEpHyR4Vl8a+BlmqfPUVrFqQZE21Pl8S8jnbY5buD+FJLcHwSvDMl69dyo5eKKx/ED4jZwB/M1ytK4aNTRG2CNyu
