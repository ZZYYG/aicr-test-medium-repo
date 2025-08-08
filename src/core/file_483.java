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

wu/m5UDzSkruhBgj05JvUPv+gdUpnrtjMyFEac02UfpN90nzoWE4bF2vfR4s2MtV3nVaXXV8vG9Ghlj/S1yd2w6zo+J8iWNzjiZRSsULKUi04L8yVTjxJe99h6az1QQCe9vOZNfmkoMEOuB5TFAAfL03rdPXVCXqyh2X2PTLrMOVtbY2HQ0V3E0aMnHkqV8XeS46RtNiqRmb8WuZc0jaDsyyXM0/4875QDgAIQtEg4c1TuK8VRwGKvkPYlFbUKAiceTeaaQ1Yp3C61jGPjVyr4fVThSqrWP9ESfo1WcO7WigfwmHcuLkLMWvyT5HWC7AI4PbzkLQ4sKCmztn259MTG4lbyCRQld1WUWr18N12sbahpy+Pksm49IRwtNFcGMHeVlVglybxTi4cPXLp7xKYW1CroEDcaRXzsZwAGUddN+gxVANX2iWsYIglRLnxBopZG9+Qgwkn81WSliBpv3YgB2xyvU7ZOWg6UtHwkoJ6LcOxDz0hSVfCHsEdBQ6CHKBjmUz+pPlAR/TB2ki/d/mLhJtD3ars3gd9afYFy9fA0BS/bq9j5DcIytS/7m3hD6sltxNdXwKKn+hL51DgVMbP3MAMbnLH3ixla6VmEy5YZBGDKEwTmBJXbuzOnxcndUevQIg/Qnlarb6295X526oYKWkxY3R5sxEW7ZfuPjfEJm+8ndgWZBCm+a4r1X4SnQq2i27spXn3gUAr3S1mrKJpGS+NcEDfqjT+F1YJ0dtH0FBVQrmuAQzmdJGZ7UPY1BZXRAsqEZBFToLzyO1AzPe7zw/ldrJEK3EfrVOnItgqpqg0UpDg0iXy94Votg8xbuxxjZnRf5qyOCts8iUKW57U68nBheU6gUUeQOYUF3+UJ1aObfj49kI3+/66+kWAgZGZHJjqBNeRA4hFX+E/Z3r5Z/6WOosWfWAIEuHS5pZCIMFTeVCqVIBefnyTPIgItcNhky1KW/5nlwq+TYpIE6kcZr1cxNz0xPUfcBJsLho9spnYPkbqGC0E7Dv0Io9YsCDs2xunlqs+Hw+RCNkAfXFZE0wLh3siZa5mXM0IVIrCAB55qIvYQsm+v43GxQzD9B+7ZLvlZi70cm+ogKjOeaQNsOCZZzNs0EEjjUfozg8WUhmJZYzbW2Chk4nSkCWBicvrSM9BXZ9c4aHn7AS+hPPS3Imdp00uIrWzMCzJg8ACaeFHF1HGM6D3NEmejzuyrM2aSh9fKRLToaJ05Vk4OP848vYe2mQutiuwlWiZXzJIXIT5PBNUuUsISN+qiUpLbcScNQJ/EOY8fMWG5Zb8r1mmB6P7A1UpkxWxwtNZ7cqU9VWGF06ksvVVvgWLoX7/U3QdS7l9CW9SZ7l2jQ5CJA6V5W2Cpc4ZieQxvpQtc1XoO9a2U2emiZSx/8R+7EnAigJ5aW9095vYBPQf16/av6dv4TWEZH4xek0tszB4S6xPAgnVU6N5bTVo/FvJroY0GDduyelEOo4BTTQin7+awbkyLWU6pECDRJmniYHp++p4de4SLSg6tN5fGXfi3ZPjR+ThLvr3bPCyZS6gHXBUJRlAqP0w1Zo1HUxPCyVUY3uWVt4Hid41EVFlzdd0AfxKpZ7i1aOdShAr7h/k1gX9ePNgkALuytKhW6AbA34CzT68NsGXJuwnyjI+n3fJsb4xUpXaZ9G+yo7HzVUTJSPOO23+1u+eW24SdMzRnSfQ9Yg3NyLqEQVYl55X4TRh4vU08sz2otxcxpNr7yoChLaBpLZ+BykuO5U343nfELSOCdAWSClnzOqwJPa/N/ctu7YynxZZx+/FO8ydeZfSWqJjwKIk+LxxxgL7QxXBaqB/znEgXKXmCXCVC1b9bvQahx2g/XY1TfhXpprj5IqWqTGm1N98A8STEl6jVyhGIZBcs5umG2fPbgKHudIwJRYK6IzuQXdv5HyHrstLaFEXQ/I3aB2d8nyrbEnLjHv/B65L12sjVX8wLy4/T+1G9SFYMjxmeFMPnABtEJVaHxfzAZEx/c9oWdgNpiDgTutMT+tWuiiEyAWdpxKERMrnhz6StV+5pngTMNfnlg1gpm2PUZTwiJYCuUyDmcbLfkVo/tqumukIsgAi07nFYsQMsYNYeMXa/nnjjF94F9Q7JZGbeb1h9YN4OAGEMapBKGGWZQmC2Cxcn4Pqp7UGeSytbaCqDNH20ojbI2IO841ww1Od4q9JCdxFB/gYdYgxDtr+sAt7QQzHlj2i/RVo1ucL5RMMrJgUNBMgjt9tqMyA5La3t6qME7TamX7D0SZEcyGGyM09hLU+tSQ/LmUJJBkkIuWFX+vMypLs7RMA5czQ+WQvUki+vB8RAMAGpGFbd9rHU8Qx9yD32L3H3juI9dqeFh22b0mx/vH25xworzp4okvLOGFeaJCFWDzqzkVMtVZnJRo5fcP4mmdi0UKbz2ggPUMVlJswn+0cwh6lpmVvTlNQl/0+GH+htttVXIAeLIyH1UAY2u+sCc0pprAPmWjzh4CwDXUTOFP2E6v2rINjQvhESI0Ge+ci9KUUPjsUxydLqSillxihmycAu9bbo1D/HMrAuzL3iZLYgf0Ofd7UFGk+lbOjvhTxReOaU2xxBObbyxoW2r2k9IX9SKe0rE0kox+VzhxtWim0tgyGev0Lfcug0i8a0lBcfbPYpB6gcq95ijkEkprbHLy47wMlcrMMZMkuIOYoWW66skZklWJu0y1176N2Sd1tG3owXGCQvfVJA4y7qRY1mmaXZU+xXGWg6x1HWyoCHvnleRJwTcoi0KK4Ndzi5tFHVS9lwJqkFI++G1ylM3oqk6wsKeX3i2cDOPolA6N9YpZiq7gNxdNQ80UQpJpOEL1cLu4YsrmYkulHSmnfwImEMShuor4Ue0ZnZMe/QzQYlG0GGvHjd/Ieyez1PQ80L8UFzudB3DMgjDmg14n65h66Qx9hFjsPcNMltg8AzSNiTgearye74wEne8eiYFWXVOBEP6kSFYdaQ56ojq9GQVR1IBj3K9toqOx+YFzyQ1xSsGGe/8actL+pIwaPd5RbE9g85erbZp4ykx8yCQqSHUzou2Ntz+F+tKFx5StoVK9DYDmdLm/RF/QWP7JInkXBHIEeJyUcKQWX/IxyyW51Mnr0j2U6PbgQA8s+keebnrYZnU0gUq8xXHdB1Y0vW7SlQriv7huOH0oz95opJIA2QfcMRlmcqaIJG3YNuheM3ygsddXBgLpGL2pkMyd2pZoKKW4QOqJMgsvTN7bTeW5t4nUkY1nGEVY5pNs80Q5XUbcsA2UKHPTzCOIMFB6BLyl9zdujQ1RWjXcB2OPy+sxHZs1RD81LrRDmmOItkVcDZQJ6xoKEbLPSwxX84/uK0KQVIy563qa+DIxKXKKUfzenic3Q4dFxeKuSER4/hg35LoZJxg8c1mVtd+1w3EUUXwzgtsmi2sPvWDfoCAYkNj0AdygWxIgKNMmVtJpT9y++5h0/x6TQmKrvbm6J1/oQiUbdMOQ7hLGO603yeMzthMcN4rg9fRAPVraqUb1Q/qoS+0ax3fRse2bnhENywzUem9KE5MDhYZCtN6+pej6MLZ/1d5YD7o4J/+8VnOZnSM33V9Dq+mx1CVj4fw5062xLJIFHO3IQyZPlLIm4C+KOLWjIXiH1HGfu7cuqfSlMKIHWBuw29GTVeDHqu96jtxrMhZQk3Sb0G1Vrs4YVdt+xhnB2Fr4GqfsdXUlnLxAiVhHdQHn7za8c8gsonJzK/YLqOk6/UuXKGopKOdWdyN6yxwqpgj5xmNOKJ5f9VLOThECbN65LkqWOP7s9l0rCVfUvzK4PS/WLD+hYDI4I5u4YNXm9jwKtF8LYL+b+ZA9nLkmY8ETZMgs1mW8Y1bFyguMynPIgNBmhkYIRmylIwQ8JdxHl3RdKZhBUVCpgItd+bVeF0le6p4SnFQJ/i35BhiUJhmwcNPDFIZjU5FKuVdbNrFOCgBVz6Lpel/nfhhJvA4F7cjd6wONu2p5mWgrLZ/J8OowIfUSa7hk9CpFgImofzsUxAz17MZb1w5XDJTj3avRXQTdz7iObMBM6DlC4k7bRyV0/dYE5mJ2g2d8F/HLN/9VuWlIpZE4F9Q9CP4VkjDQhB6f4BnUiCSTqcw/Cnk8rMXczqgA2H6x9qR9vmju2mtBecbSb3v9aztxGIewuEMQJCKa6tmJ88OA9S0XI2T9kbW1ZiRQ3GALTyV/rpfaFbLZrES0e6h9Sj/fKjbc1HKGtGvaRHZvo3Q3DF9WSBiTqYncym8w7augYeI8UwTBDGCjEpl0bC4BKqoT8tsfNS/3BNNKruUk2pxyd3XVmC5cK7SIjfgUJv/zMqbgAfjmaUF0nKT2ugbhjytwE4JZB9GwZkZFq3QBpQjChbnyvis/Loee3Lym2yrip8zBzhBI7jpzDJbVTlSpUk5Wpy9arT11nxL+xLarpT5uTadkqSWcIlYibUrKWAwd/jxKAjEds+p9ultqio5d8n2a1oqw9sY0iRMCPFzJJxSVuKnMLsJ5jpm5nIVELgvVCzzBRDloQ9AOIsjyUF8F9G8/i/+r39JCO16jQRLnE4AqtVveBo2DUbSOs54Mt366sP7sT45gGOiPXcfcDS65hK0Q+lgca6IpTC8ij2zIzGaQ7nbYpb2rDmqh45ovSdduxe+SriVq/Rp+4T7/vI1xALsoJTZ9W9HE7W8jxE/YR317SbbFxPnr2CE+2ZldgvhuolDbZEAXJ4RIRokKAtBssVMHnj24ZgpgaJRV5y4eMeOCrzNn5wP9XK5K6en1rvg//CkxTNLVMXS4paCTTAGMrg7ldDrO/DdIq02lvwgSnNgaujviJAoaEOt4Gal4rMCMjh7J1w2LuzcOjd3meNT3DhsdKROQYjaWivRJhclHj1ta2sjzrIizaWkVOIyyTccq4ax3V8+m8UfRylfd3Gys7qVu/s9ah4dpYIerjs6NSIqGf60rx0/mJRczij2v8r72TvF0gRjLw5vJwF/IIfOeusLkBDXnhIWBjBA4bryw/1QD5sxBNjJnckv6AH+fxvCu8frzg0iuwNS8P3C55keQXzf1PKM8XKLoZjvuXC/o5ntmCcZW8Fe6Q8fm/BdDBy/yRqBPqjKzOL2J2wzPHnpJNnZPMJGaZC6s/wrx85XsPYT+nfVNjcixuMVCbDC3wETD7aZqZJJpDALc5KO43pqnPXhMxnaVp54GT5Tc0vSiaWJS1iCliAGS8QBJMmWbIcNzlHBkIgIzLjwVL7cKvQn6EwXCpltWCDabGAd43u9BwA9fSZJMKlHGfblfpj0L5R5j1v6twAjjiUYfK60vq+H6pFRJLnFCsF4ta/Rdb7/JGsamm2Norx7LtJxkzdhX4iYXMxCCFYS2fO8vFxnX/7WnL+2BiyGAtrc7WcQ+RF3E1VbtBNhrWPF6JIGc2kMTolfPp6eqLtaK2nA7lWFX1YnQM3Zx9FJOkpkWMrT4VkoeCmwgDIGCftsoornQ19ZDi8kSbQ0sOJ21DuG2w5K5zMZ6/NiY4RabS37hJW7YNHeB0vkTdZubPrhXj2ScuQxePB9PR0roRC4tYOawvidj/BIi/GCHKcNwDJ5BDtQQ3It7TRl5qj0FhoWkyq6T1WBI+QyExON3C+LAbaWd2CWBUFXI6Am4uWC/ntZzZpVIMoDV+IcVVxbpRh6yWgB3QWFfLzIE4ibXfwZ47VE6gEkfSBamPR97aJRIbQIHifIwyiz0wBi4bxJdLLk9DB6+kBTEw9RKtNCKoRqdiETcuiKL+NFHAqXKIdk6W4WUresRO4+RCORwt+2vPmWf3SmuvBSqzPJx9pyw/SXafB0sf6znRlnOORMRyaguyiF98GlfiSU+Zj8MyHkGmGJA8HDjHSaXXCYZF+EUvFddgykaGbngZVUXF6zG/+VuIYORq6LR9g2kuOhooPiSlQlKYnlOR61uz3GrDv8gzkNauo31ohTyVIqsEX7+LOxNKl00w5AmvUu0PrU9EBbeLKfo89F8VCdc2oSavXlLND5+TU3ycwM68WzISt2tykAqcgcWYsbpwdwM9jWvM4jL9pHuT7k9tFcb9RBlcHUEswAfZeuKKpg7ZqzZtYILK52TjanBEBjuhlgq5Usu3wP+NZZzPA2NXXzYwjhP0e5N9HHJ/rJxC2P7mLxRSmKMQE2osjSZuSC21Yb6oixnfeK94RRR7gg4Q+xgLHFdjXDgM4UsVGfcZWscNnuQ93bsoXnHF5rZyPbLNqWIL46QETVAzn9fDoeZnKkfE4Q/VVjGSIv/c84EV3qFQkoWeHVc8Zk2pex0l+PIlzVERP5mbkfdWk6hyjiZdmOc3JwTeMaE/94B7bYGeMGh6Ef2rqWAVTL1bt1pI/jpGdYgNEnypDsMTYoYHloNtij/v/3b4njHAKil6le4ujuSXxiP6icjqATJ40FUnoDK/TEYEZuCxKItFr03dDVbfD0QFl12HabcA0crweC3CcfzNXoTJGvqdDf9VaV9ALQs5N5CSwzozYZOjkMFsg9BmnhXO/oiaySwglCeM961nsNf6xzZwohEq74QjkldqPMzzxLBjDagYOJu7m7R5VxPA133Ye0u45Ev+pcndFjXSpZY6OKc8bKQb5SKccSkkxnbHp+5dseHSMAX6Fu0hlkRP2zp+/93R141Die1cmZXDgrcloROhf2bbl8YFuZHhYkbNUftLkjl07fRNpXDz62w6M/nGo9dZv8G1ukWEiAIby0uY+6sewM65/fQ/KvpBMREVzX3YPN+/RM3N+Ap4pyiwYTBx1Hlr2brAVke/cK9pbcjsOR4PqY7h89ErNWE6Kq+NdqFN/8B/fTAjCLVYmEEqwb88SWBszMiuEroAeEtNWkPdbDPsVuw4IkQ5K1pQyczUDnOhn+vpTAVrhhGbJSbW+rmHHMsk149gO0efn5fumg5jcUx+7l/75bvFIUcEGbglQY7XCvB+B/a3UstmlU3bKKIXZYEJJtujrn72cH9f3+vM+tJbTZk8LCIsg6BWLcqJp+Ufj1qQdoeTxlLDK317Ee7Ima0a16TzIfc5jSnf1ZdhhXD2ZCE0aqFPXec5vhYGYrE03qgcbfvWULg+CW5RyBlnXDgq4XJqMRy2LdWSAwIL4HhCAQ2UdUnK8vTG6xGkqZkQ6ewda0IK7+HRVuWcztvyBcWEIDQGbzlPsPjFY/zSelZyNhyVs6Q2SuoblHCufIKAlOaxQ3wIHYV++utQ1GDJSoSUnZavNUmdcfqlEx+OjU74gpSK/CwIO7e2ToDumkeEPantq8kWM9dFR19ypFDOc2wzzAhwE/9kzF2JnEjrmJk6jf9GxR03ydCs3O840IasRyA8fkyhwMKH8D9fpVqxncprXb1yIo4MP66kXNDabmoijXBXdEsUx1HqKVVFnkLA63lNW/cKsntUCMMkMTZxvqJCK+IydkxHMb3OKENpkkmj+VqUH/tPDdtBLEKCrESRA4OUhGV3RDh0YvsS4MtMqWGSG/hvQfDe/UfQGi06XT8yCO1g22KSm92/xYIlwku7RqvUb8ul1ES5dqdl7KvLELoNSA2F9+NRJSIQPAUM3aGmE4iJCUyoyNzkO45mBjXvMs/JQd74dx2DZFLIEc27m6S1xqvUiIY1nGFW4av/GM7VQjirk4du02WH1mQPJVOf746nPQzrgqgywzs2KlswiWHGiNS+KSP9SId2u/rw/xdMTX+LlnbeiiHnpXb9hEbGDMrQwMfNk1Hl5aW4nnyjRhI091hqpQSzVxrQOLW4LZvs39GFezNqkLUgRaNgWxlMVjUJuuNHc6/k0tt83yni1NFGR6oaBgKwVT6GJqxIRh0kBj98akcJrHcMtBbvjWktOJzdszR9ODpHV73hj+AeYVL/ThpL4QwyeY1y+LZrAZbcglUp1mGcZOXZvsqhBwUOnn3lgAePoNR8vw5xAQo7mRtf8DDHJkx4IA8oSQKjKdAGMGzr2Vf6d7E6+lDYh7TWCyZALz2XVtp3GxZl6jI0PLWkf2YDMOExJJaGzw5ba6994dA3Dq03cZdXrhyKmf6UKvZV/iQ0i8jWJXXo4bI0Vj1HFSZgSyzgekXvu0SMY4tq5Zbj6mtjQmkt5bN17WOg055ZJDnRsJAyKjxTyY1gT7XcujSPKzD/ilshX+uEe2wTaYTJXrHbISKfHz7q4d2AUHgBfocMT15uuzCcYpEKrRGEOxKt5J8t3QS3aCG33650PFOpmbTidhxOsNd70I+3izglSNi+DNxs/F1u4HnpAyFkqwTCLDWqKh4PBn01gnrOJb6jCHnr5R2hzh9vGvFjwv58B8DXrG2kjMm+YizEbdT7w1682ehC8RZLpym1SljUyOksFaiAG7AGZ1k/XA4xuIGXfh8NCMdnPEjleH0HCQvhsfK4UwVIc1bcno+WqfP8Gnc3p0Iku5MDfilHxvvb9xEjo6hXu+kQ+gNMaFf768PjR6qJd0V2+k2f8EImR6g2enuMiM3qBQOIRhyQFJqDzr7gxvlfZnWyyB2ot4+boBv1oQLlVVYM+6LoCTX5jyvodTGVwPAId9HrXSL0rG7h+EiKxyP6uFdUr0ycxk2d4G9K9KYraIbHSw4C1bOxqvlWciNymwMucvkjfDRDGj5e82uwSEYkqOF4JOGhwQ8U1dJCCnCPVHTiI0zDpDMWb3RKLVPL+4QWRRHdSsOR5RFoqCJGfO44dEgrCIVKUpbvZBZdmzOnh+cYL1aBMCPgKnSTABLGmnCqEUQjr8sGSfjd6hhbfd5JKo/tLGgEr8t1ovkVOs8bMTavlM27wMLtvGMiUshp8RGU1NC0nSrpYlqPlkwddG2Db8DiAsUNNBdFfuZC5/QU4+qSFqMtak7OBoe7iwa98iTUTOhiqpp4yIm5UVTn7CpbzMkmgJKJV2g8GP/KyuaZfDFVOBfVESyaKFb0WA6Ob0vOf38pv9s9BXz/oDHTn+jWrArNfWgo3hqiOGRNkgcBtAsxN2QnyxXWsrk0ep1cAyBUcuL/zMCI6KT74eKdXgtLlacdw9I2Y4HBp+/Y+fkGcegeUXrW5He9SuwZ0gtcGVTWzipH37SR+15z7QySPOlprc8Vte7at3Au9bOgrvITICWB117aiRFmc9Q2g4TS6/WqOMXJNf/MABo4AWgXm7cq2Wbfa9d34bzEWBwxSn9vu8cme8sLp3T3TVCX0QJYna5KeCfR/1015NlDNukhW/i/EBB+5FjDO4mYlYLv5XSsEd9GE+XEfP0Wg51HtcTFpfqIERzLn74pgmOgs5Xob95fm7ovtH3qBAYy+XBVdwgz9B1RYasqisqfHElY1Db4pkC0lfLxl4LM5IgQnMqhOY+NfK1IcHxl0a7u19dhv/rXSo7LS66lA1ZmjnORGYO78LA071UGXFGgT4FaliXU8LIjr9vdwZojQYk7/P9j2fCtlgOo3vfb9OGEOH+521uB+Wn5zFaPnyGfE3EZ7nRE5Qx0Ef8jfwNBrkTFEdVgvy0HVEYPUU7rVKlu/RAMYQw12SeZLupc9AA+DYThLEhNNGBBOEaWPsx8GOBnvLWcJ+nzZil/p+Clm8tuxTZutPsgqaedmipSMPWuyKzDK4yLTky3v158rqPzweVOaL/MJsDAcqppJJKnJl270cchYouZr/9txLtkFbsW4ir52rHyMjv9Mid+4uEpymVa5KdtDuW4pWi/cFtTC+UzvipaS+c90FGYfBIxLm89IzUotJQfTCCchbkXmJV2sLRttc63+UihJLIkQkZEZ7ukwvABu2Gljb+hHHV5ZVqbqHV7BhPbaNkn8BanGLfJTvR3lNljNz0eGcfS4vuz40NkZwc9IzWM4z8y81TtSzVpTfE1wC2LO6+ZKPpulHU7HzROB8S+2mIGQB2e8/L27AsOeBdVDMCKj17Wk0oWPxxXE+Wmq8CyfFkNa3CjaLk7QDitu5Gl4N2fsZ8B+bg5aLeUak98OikOQUoQwHbTZKG3kAfc3Hl7aah5TYSqfyZk9oDZ8kRGqdUjofYhTzVHq37eH2aqc3xcbNekWGT7EJbk4gWULQkO1xH2yfkqTKvGJJzI2h1BVMJGdWNrmgiCEF4FMlx5/8RKzKIEKfWMtf/XWJnOVPJVhRUCyK8VQxRF7GAQDmgpLOu3ojAaM1QEBbJ6bs6c4vaRLOrywWLhAaQ/hAI69FEL5Vm5vW1lFHK0cYTHRRjviKaD0c551oddLDE6vZbM2LdYXz5auTImbKiig36zXw93EsYfifLfG2FcxOFb1HC0+5NOMSk6kgzVIiknvWa/YrWe0D/b6KWUkIriZXHZe5ayzAR5YzKH7AMq1qiS50KAo0KEPxESIRBBZKcTSDuwH0nMjvmcCq+nQXimVKVmd1rCNuoSL3u0PnQzXlHo/po47BX7EAmjauMRviU68HtZmsIPwAE5oPGD69S4HqKli1D34mOptCKEGJn7xsQAgDJuQIYZLhs/sasFl6IyEUOYFE7aFmnNedHHZp33wSlInSluUJytIEIgsyLcOU/YOH5DGULV9QCGrOphFopvNnxOfyy5xhzDtSPMC/P7UuftvF+nJWo+Jxy49HBTw09ZKUG+uAUPlKWF2mcsNLMNn28O0aUCRybp6946aNyVbKo+n5h6emuW6nt9rfYtJvVM72kGhILK3XgiMWl5iQ6gM1eE5EhFJpWa1Uxvf+P2Czw7VLhzjVexvLVRRlS3uKlLFW9lfw4Ey6jXx40d/QQqHEBVgKGDirqSnMdfVfuMHcmYqeSQ4hB5mLJjKbKC25926cniEo+nfD6tjXY1xKfWBShvUZIH9DHCU/PULISrRj9GAlQya2GbQCJvtI7GjknsawwqdaasQgl7c4+sw1qhRJf7vAbsSYGgiEuBlA/d8qtK+i64UnAnOtBlMdwB8+E8WBRTF/Nj4Lpl2+Gr6AAgMIo4I3K470N1h3TFJHNsfx/ePXE7PoHU9zF5YtEWDJDGKtskdu9x3vrTHk3WR/Plrw9nLql2vHf+YVHWn8FQ5X5kRfuW1C2RDuzfUHd90sbYgpgaXniZmyT2Ctujto2nyygZu2lpdWqFGqYnxNma/phcRQwlDLj+4GgStOz+poTIXsa/SVBk1mmjJhEXaXN2eNIXyvbldgbSwE+yseTmAF7m36lflQpwkoZ/mp/BNuGKIO5pxoatx4lCjsmyHCQhyTox2EJ8Pnse9n7QYEXFH3FBwM71OiVRCiRIe32qGr8XES2+m4Hsf4ub0fHLvwSQevDgoqk6wCs2o+1DMjlKYOSjKkRUGL5tDJGzsxT3FyOtLChuFHhtPQLVD6SKkQnm9mTvZtKj53rI9JCejoYgFTWeFu7s2FgebiCB0xAWMWXVfqCVaDU/omDtOP4sAsjiE8Mhpm4PxW2Hzg2atmyhyH/3CRjRC27wRY+Oiq19+983+NON5H6+TblTU34nyQw2WQoO0x4a7LbeSOWZdOggp8jErY4Y1nDNKPAf4Y1Y9XI10/UK8NqZlpra0wO/JUnjoTCIdgvDmHKxELYT8IJHLYNPC7D0Ixyb860NwhUtbrxWdK6FSG6mZA7GYqI0k1tcvU39RMrIVdRSTmIdXErqENv6SQGr96hvoxkyoXGIyG7VFwr8VMOlc8FgYS4VBXPekxUUt7u5DneaZOXg4LKX+VZl2tR9WRE4m93uv3WiR1Wj6pmCdqkc38dpG/bo6sq6f67SdWlodX9DjtdytuxKi68xSLDddLqq/9tJ1lPw0Rde5nkby5ChZBT4n+YRWcFZ99sMVh1uB7x0awQaZJ8SuLwZQrf35GQ23ViTjHLwSPXR6zix274z/KA8dRqN/iO44lG1h/KspOo18zJtO58uWlNxWkVKtKqO9nI+eQf8elcWKN4RkHn82oXQf4scefIzaoSyJP7ucXOlclV3o9O4PgdWRMmI5cOlKBML+e5WtawlRWtg/FE/rP5VPgeDb8JE/F4OiLIm4aNvlU57GvY0F2WX1XHf4zy9nz6dMIC0z6r20V3Ff41elv9G/8uRDIF7gHseHBk3XGYIVY2Yk/fM6dVAVe1S1auc8WVA2Djw0vUFMrYEjVAhtUuEDh36187zG1bq3KQnwQukqS56jPLrc31ijOGo3L7bUhJmULTvBGi5JGbCS9mr9qRYqGjSqPtaaXpSyxyNUPrSTGMdWz1WIR2ZO2pzGQhog697qsSK8/piEW0U1tlnWIaZPLQ9PBvhao3izQAerlZvHNwmkzmgBplqZf9Z8fxWDSG86Uj88nYaFT/DJrJC98bN8knby+54psZ16MgVvXA+1BM6KQ8euJ13JKvXGU2+ddKyy8thJXlZq2fug50w7pj71MfYtV2mBdoO4qgZmBybs9QUbf+MevGP2EEOdQrf1qSPlEgJl234YAUtTJ6yz+fKD0pPOsYNCvDcSEg2+rwoj4+iD9RjrI+EiXoi4luiEhBx/B4SyS6qoyOXidzSXFwsO/+io02Rh+xBi+MCN3jRdPySPPoxuqipOlqTUELAgWfxTYC1sqa96Z6Te0AIbrmC9Ktr3sw37ylFXu9Y2D15JlW429ajbG8QXSJBQ4i21xaSY7br+y7DOOTLBTpDVVQQOmfXWHpVz6/FDBHY/vix1X5RQpZIKbKcfWov9Tz2cmdcGrZaw1GDjJIRy01iJgOal5TXOt1b2m2blcg2U1P9kx3F77mN3heIUmXVQYlqN6Icy5aPGG0muy0T3zmmfUMQEmpLR0+HuEQsNenEFUGacFeN32ewXc05fnknTXXiWgG+brgXy1oGA+WFGPuKq7fU4JDRgari1gtxe6YfVndqoaezvUgCFBECeWznmPF9szKDg2ZmnkBQIZarF0O6pInrRah3IKhk5sb4idRTDIfCuqIbh3RGy/FGvqDrPV9cLvMaSJN8WVcF7oPoA9556Cf+ft0gHV3EelnI5AL7sPHK79sEFNOMrr+W0zuljXuI1M7vtj8zNQuQMYatyyao/IQFecoWXZxS/rN2mzCoZiLDt8K4pJr9OayrD2XsLI0ViQ1EXkghElRW6O8D1OhI+8XI284qzyZqAPGX0CLaSJjjDkLPPo4hEHYGvz9N7JHIuoXg9AfNgVaubnvn+TM+2E1K+dxGSTAL5nszA2kCBeRgkRxwonKj3USaHcOL4jCbN32qZ1Kpwd1gcZrX9iLXwe21XVS6DqCI0gWKu/ICfnYx2w9SE/wwwk9KZgs7lyV3O8apLtypBw4sm+4Kv94uIUbu5tuBJZhET36w+kUQ9VLj5Uq//5584qE8QB1MM1FXbT4qp2L+rsmvBiGXBBce7GUsNynWVACE7MUan2tWNDbqu9fir+ZbbaU8UKUqKw8jQtyWhJnjvTrxxYRYgidKQx+z9bLprEdA4BZFwfp7csYGfp1HzSb/wRM7NZEkcOK9uHdNrLC/nVTiHRRu5H3WvCwi4G7Mi6qK6qXjGU7pl9Ax7p/2jIGrW7WW6JcknfW20b33yv4tl4GgGb/XAJR2Ey84bhPIlBaMgHuveJ+MrBwmouWZhNjf6g4FdIUndjTIxdU0+42Ei0o+dBsMEovGzVx9PUdsbg7LREdR5FDWsc7aS0sRmCypY58zZ+L7ITtM3v1L86nphl3DPcciWioBk7L1u4nK0W4RSr7TcsTdpMtVTb9WwpOISKb2bK240vJotVq4taPRR7UfIe5sxdSAQY1wH5+nnr7g1wGFbEs4IgGRbE5gYspAVmcgTWwE9JZkFZxVhq8agwVsFKjlWAca603xycfyviW40lx5UDWuvUJSeHn/0XufH7MwUx1MjjXg/AuRdh1FYPd1yVEkt0QKP8nodzRY6bxV0N7R0ZKh62geljjoJZyQunn4GmjzKUPJRE6zL71SrGXaJi5TsTbBSaNmZ5rhmtoRCpBE6R3IiXxMQo5/hGs0uzZqd9xvAiOhhsC7SIuFVt+Wrod6AgWhQYGbh9uZPHaqs0eKRanVVpDxH/gEeQN1zvsRhw5gDOePjEovxL3ayL0H/K4n0ghYHjOHd4mQsBtb8YN1EZMK1kW9H6rqgcaER46qNML0auxz1gpLrch7xq3Am9yCiwgNW+xdg6N6Gf9vGijd833eptdMEmh9U7hBvM60SHt/vfLac6dTlsT1Smruu5huFEfhA3AefNUnLLGqlhJu1WhU/TH5V2PXybvKIHin3YQYzm5Onyaz+plaxJTVuuWnvHmYmf7ceEKB0RAOBuUxttg9rV0ppuY6vQfhUGv5HYJGcs34eyPTCA2hCnYDKEvyQ4N3N14j/h+WhFwj1ZQlcKA61Qx/cILl/LfNDalRKVIyxRC7CKVTGq2jLamqRM9p8w9rF6g+5edzRqVkcfrDtFRz20znN6PivjdNop89rx8ZA0W9CctNLkp49bTzGmb/42yJTbmNehTeehXw7BEp+tB595nfTf1NSGdQIzv5hl13Exk+tIlESMC9Tw1DDrbbHk05t5PhOaJHs6aOhQeiBOnvbN6tZzMvDWIIMmcOgMw956WAZOMndYRICOw0VJvry3vvuJKKnpgrZIxcU6XShDg0iBIzT3gZ9MydKKM+ivtwtJnEb4EiSw8d6CyuqXVV0ZwafwrsGBtNmK8AbbQy1tYbBMhjkpkB//46AZCJ3rViFjiYUNOprIxFanwXKo5lcqFAU9xr4uAlotjwtv4rinujqRncXbTrHQh0LzrKIcgx8esHMwMcoCylQxX07EWowsojWSWHG+8qnzGSaumueDdXowxJgrFYW/hCdtckTxYJmuXfMY9F44CPDWZiZ4LjmWApv/7Suzhv9rXsc4JjNqjWRNQfkOgkTJpR6IlHCy1VPtGfRTSuCF9ZK+LZo2pGGbkSLjqmmEgSVy0YFraMU3/zD6gHSpkSD7a/10asqyWqu7sK2FdaDxYUD/kT5dAdsy6JYEW6IsAfHZPqWOgjPIO3UKY0oHtppnRBBK3Lrrq57+uhq4JRyMasUAE8ZtpOm58kMdD4VGWtYPk0EbaXW33p4/HRGlDRk2249XH5AvxuEQ+YjQc4qdd83n0WQgICd/ti7WTO/tYljcc33mDp8WWL+MlexfFU2SxQaImIB/NisNl+5ztS/8SqkRIwNIc1YHWKU2Mpm5q1K7GqoLyhnIiky0CrZ1f/NsDr3oHaT07M3E9U07H+fG7ZFpZjFQ/ekZFZdoAPfDoXc4ox9F2dXXaoNEd/MfkHQs9CMrhgl6H50RKZXN4YRRoRRUrgazgPEc+9VdMzPVk+FhQcyZsh3aTEqOAt4kQaSQMlOWVxMsLif4ENl5FWB/ePHUD5NSjSDxHYl5qPcY9h8Hnk4OlLwcSh/0RooQKIKfcsGVgZyzvNzEPpYl2AvZCEipLtCJ/APv0BZkrvpQRMq7IYAOJlEcRgRqpPJi8PrSxe2Cg2NB5BbgJHlOhtXeAkImpgfzzfj0Bd5jth7sXliYwF8ntdJdL5AeYTmkV0+/4mMXaNgUE5NIKAlQk2MNQ4AahGEFQ1R+QstqTs6wgBpMhqad7VC/6qIfttzXnbNbgjB7xjSZz7xDOFrUFMAZ5N17kFMh0+JtSitzxUUqQ74xIwpCtBt3c1JWaHIlZFRMq+D6iB83oIhlYCR/lTvIFDCG9SvKL48upC7T98r+zT/h/UVoUH15Jx1ReuompbSRIkhj1b709AaJRyJwO9j9+dbnqg9qEwSIVgKPViy8VILWtU4sosyhOuhR7KHZhQ380i2PVTBv6JlvLHCBZ3nC3npxWs56Ym6XFM6C66Q2PgA3FQJSC1IGP+JDbr9qm/3DtFmGejdKm3OM48TurUeDE6wLkORX56Nu8J2wA+gAijWGK6ZWLjm3kmvUXCdKBk8HdN9PNSVao+khSAHNwLxs+Jcxybdxp+mklSCtr9Py8hPfuDcWgq+BFAlCUnMPE7LOY797YBhkCnAM8Sp+wWClMeirnjnAIHhXs+8aKllQHVHNTxYWZeMch/IjpjXnxVh8wpJKJZGRXYpRFg4HMhbJujpUGu8rN4W0+zFY67A4nHzQLclJ4nG/rUQQlzi9QnaP3IYix/+IelZPfloKroiQTCD0HOVfpiwlFZnF93xfjma16DKZud59W3Et8ASuvt/pWi8dN+W0adxw0CyxRyJk5obiDVD9hp/d/ui7OtSU4MSdQjZaXnSJCfj9Ce/HNTnI2ml3ZIsXXRgjQTK9J08JSQ55mxf1er2WFbYD5QvSJmrjkWIcbowx5MOPg857KffTY/UkJFzzB2EqiXdqKfhuV+QehNOF3ef4ypLSwW9zbs2N+KxhhXR6spSspuMwCXSZngtwpZ4eLE3+Ndgd/8IcwENlBdiKndxtNkc/E0dUVd70+/cV3g8PLAuhss79Rabkzr6rNzJZEn8K2nSRnoQqj+/YsCgjOWKkBTudOEzIccvO7cAi0vj6mf2eVbosGY3YPHvseVLI2XU1b2ua18jJaeFTZQsv7BDUzPXfF2SwhMMj/FbzL21SdFgFG3AaRYVLAHpVGRgwXS0ZVnl/kWxV01qxCcW8NO69bfbKOe0zI+HAQWieCMYrKaHhu2AYhZdQY6yPmA+N/vQWv11Uni7OfOAjB92KKHzAN3e0+/zs9nj0POU68WaYZrKBiSgooQ5LaueWzg30CP7bVn+sovYV1JFPJcSAtLOGzCcQKQrdPhuJS7dB2SRL7RfcWijWbq4k8RURZZXtT3/dNVN7ZJCuKUpIb4YkW375cQrqjYCrh/h7I2F0SbluMk2iJ2FI4NwXg6GGxWmzQC0Djg/XbKqzcb/C+rfUDzCAPYnyV4yE8G8Zh4IZlHGJUKrU2yUKh0jIvT/5TruskogvJ3PoFkt1v5cYb0Spk0wUGQQDXQCMV7L3zKij4h6tQnDs53Iuus6LUn/wUOrIpKdA67ahKfC7NW/qQ++yilWXvKVKgahw3xU0scNqe/jdkiWZ9W1gM+vRWQX5bJrmz4axqQvMsDF7FnE8s06UQrC91LLY6KGZpAW4WiI+M7B7CZY5o6Hyr1aruWdOhI1xW5qhkMqx9OwHBtMWNGD9v/nc9cx8pgDqYCyxYx3c5wlsX/kLptt939CWm0XT+DGsXcCUVjA8Fs3VCzx0Ys2NhzIgVLv7Sqm9Nyk46T1IApdOuqATX65P2GYDPrvPHN31aUxPAIm8qSkBqGDE69NPFF6JoupQLTHjMZrG3bPLY53Kq6GGEUde3wJ2assZTtoeQrQmBTEkH1FjRQzYCnK4B8/y3qEzRRRjXJqrQuE1/rm7CkbZUyukbllIwSjz16UJU9FIXKw95a9xOU6RU4Fi/xcQmtY9zbahZtGzoOABWY1yAp3zkw9gfjo3T0SRKuPIStnRiRU285OLJGBNRmQjoIEuP+xib3KcqQttGu0g9CmV6KY+843+dNebYOaiIw97noTZgqwLh2RDn6lebwykWmVHl+8b2fFkh5fJm+gPX+Y2dywlnGsZlOBOrsUzXs+brRLBa5XT/2pbxdrpJrkks7dClW/EYr5ql4vX1fNj6xRjhrbFumYh9G9yrxz3pqh9S9On/iRJaIzu9cC/vACWvGNksve7ZjMCvNqRvR9hlM81Lbi3hAgtSnH8IIngIQdA8biAYO7zKYZV3X9ysj8fgELWSmcEli6FQOAjUKrgc85ac/W48YhHs1EMb0bcXweOP1XTJNnHJ+x+qxAwPPj7dZ7QIG7pwC+hTxhX9gmkf5HZQi4Y6+pnmn4hLGACnp6KL9VgsTOOlgfEyDLxTPvuc+jAXYNnAKpV+h0sSExWuZIOC4jSRdJ8WXTnzrlsvr2xsJI8rDeQUWf1WaPXT6TzJPsCK9nuAYGVchg2AyWGd98kmVCNFR5p5DRlBvY3yMioDdgNPEUK+e2Ecb2kjFg0MAsLcglKGZ40vGV00EVpd44C1ZY2Nzdm6+66/no9fS5npKqgqoByc5IaDaum/EhFAl8UL8qhynMtZTj6+E3yHSksxHhzb0w2iLnBoiPSsubAl3N45070c7ZddM0npKk7fFfWYaPDnnnxzz9TpM2TjfUzjtTCUNxLvfLSJ6gjD+MRWk1NLnv6lOMBPjnI1lQyGc+YkiSTgupon6zhoZ4lWeZFRkRnQbaZ3qXwoY+As6LqiYPFSTyLFPfj9lyVVfuuwH43SzWkZklWvaT2Z/hBdpFAN+BIX+ZLhFSI0AB0E/n+O19GOQkNAjZuLRrUI1iYs9ByWx0MAAeUko7iJkk94uWaQFzdctWtwcFDkQMAYQAK6JksCMPVmtmG3/eBN2rFjUzXj0/zVuqAfXxJX6dlHRE697+mMOb+X/LsMPfA6Ypa7ciAH0gF+9/zFZLi/8N2rkB7qoCo2Frv4qkGC9GiVFz7b8wu484gxCxbURNvqIkeBWYQSXzY/fAwRfYpgLXGx+hRgaH7/Z7W3AgtP9SuUMB7NaCeLg5RGrF2xGhdQwrb3vijn86RrIVzQtl6rjUIs8TURrLzaEt3jF5xXbDMXMeFBLoJkX3rNwfSWvSl4DRYbPlixkzk9piDoHEySqB49PCk9wFRAc8h1aTsT78jlQjNUA7ZtRFOLH/tvmJ1OPsR2j1nvpNkLiy7WeZFVT5tlJKwSJp33ktAdHGg9zqwlDY8vyFRDGjLXwHvwU25xtQ+Tb0PJDfr1NFMNKzwFmL58mVKyJ2BUDlkVboKDJtrdtWFpf1VFQ3bZL48MlbJE58UNLNIZNoBmc9yxeXl9aNDTOh0WiVNEaFBTTs2nL1rQfiL+oTp5r8pa3d5fupxypIYqDXos3b3p1xkFPElWJ7/b7+ls4DF6E1HImJMDOyhdPHX3Nuk8ReymKpz/irqP2ebohBZZEWsraeEcPHNbw5RiuWxP5Bv0sx4sb8fCzVc2WlXbu+WnZflNobCb2pAaZup6gegVAMXmfQeG0E1ZzVznA7bi19WKrFebj1c04yEzjlZhoODxC7y7D38HGw4jggjWwPEwDTcdU8juygVxAbJqRpkHe4660N4bXevBkYNLvAL8N9YbOKP1q4BTTFyDErRUZN3PBLsHbJ5PtTMgnCMbz4PxzBHfC+Q0RKqYWc3T7+dMYaOMNFO46d6irP7lB6I8lXg7sEnwdpCFkOsFqzWh7yxjalvN8gnh1l0NpSkYt4jnMVrK306rqTtS0n5myH3You8eLJ7O2yy7OSmoKPeQAER6b/xsyOY3j08evqzyzB0PZwUX7KbJ8jNzyBe+W3rSMQxQmghtz47vZDCs5tlgi3kTfoEQZtYDJ+qCvahrBriOGDQk4w3OPnFFcWiYwZ8XyqHq+zoLzarzzUni9FDM8SWJP8ti1JuWk6OBrblOpRYKT7/H2PL7wwgdFzoy5i1a96w+pYGyZ0qzveqjWEXV1mHJY5jw01IdIvbvmKlvVZd4JFCRDrESrVUTPePtWJawKrRl12jrrL9wYnQPAPX8pZ552tGhch7Yq+kno7VK4FM33idZbj3q8V17Lyne8iZDP3jOC2+CzPUAThI/rtmN3bjeb8omOtGRZrbNUlHQH8RUGZFU3o0BneMvhzctBACIIjkwMYdFPSNNB/X5dOUF9D0Hx4capOKOG5lb+Dbbgk6uZScNRlw/ePdaeGVWrlk13fcokMDemiHYxXVinzPph+stJi3srKV7I+IxAt/SYvC1DHJstNQwaYYod+k9PWQlsODVEQw7Gl6D5MrqHeW/pKPkT0gP/liXTDw0/jgjmI20W4PmYwAfgWJ2PNOYFgSTkB06R/+OlQQc1ttSExlbm/5s02EKwbIqkFirczbUT30xyAYxqn6rwzOk6zRue6GhjLf9jYTXp0BfK3u0hK0oJHvneRKtgw9fkqil5aClaEYcdrykDr7AW02HmB+hsil2wXhnC4CoffqYAIzJHHoh54MUEmaIhvE6wGDDFC6PA30mLKBf2hPDGKo6D4BBonhN4pUThfovQuATGiyAcyThVvt/pB2Xa3fb25ZHDB0PfeNeGL+cToWmhbaOXdPQNtbBX/4SMkgv6MSJHrhE9qCDbZQXg1C/z38V1BlSQxHrrltfKMjUrAi3nnKNHDZcj+rNsAxkQHnVkf6Ze63b1W2y55BBfBGXVjnuvGnVlIjb8rRs8GGta6Ok+taO8m8J8kDvX2tPGpM0JFE410br4KR64HQLsqPmojRiSRMRkxcxqJ+VTFDC38ShJbbQWJ+avYjdDBCIp9hmMtX+wqrcNZpDC2VwAjteCZ7VIciyg8oDNLFwuR1D9/i92gLdeOJCgWtGJ0Dpraku830vqhpwnfbj/fZl3v+wsBRyJ2YK7w8LG2zCbv3mNuIU2nSuMd7x56gbvKgZoNJwXqWtQiWdCHmoUGheCm7u++uxKwcCpnTGIsYwLhnRH7AoHYsSw/1RuwavvZqS5QdEUqDTs8NqAg57RWTpjUE17lSraagptggRWOiFYUcnv5+2MfjezhJq1ZXMXbVae27pfwrfBkXpDm2vXrz0iF8OCOuB8VAed3ShVkN016cMEBjRxF2zvo13te9CRmZPe+ponhaw5j/7x2I5vgLGee8AFKeodS9jrGxQ0kYZlqeU9J32pUIHaTUju3B4R3k10c6KRueAVJg5GFGhTgTI0LDaAXG6+Je1gsYL6ETYnoZa2OCAwYCpv4u1R95SNj6H9DUeCsXL+3JT+jEmV05aNAnGt8b8RHJH9HAmJPII+ZNG+zLFKIuEHjpDz/ITRo7MCjoAORbT4klggWa9oluAw4970elZDeML7rbx89ZkoE1rw1iyH+d7O+laeovlluWreMijX9H10c7tRdlwAPb/O/u5id7cY9wrS5zZRWUfmsKiq6MEjpu6FiEwOWbHwwAEuuNmmNtehMGrk5hD6PcWH+Hw3/k4Ws1SJaeQdq7XvCd1bsDOUNFs5833Cq9tPFqG4CwZjyQxwq/UV/Y85TSPuOmXlW6LR3cZrq4t/tSqFkXMIPfHqKGhc/SGurVN3jin7VVq+a+XT73aBt/EP2EjkbUQOlxQyLEEO7mlAADNqmqyV4n35OPPpScQEMfdYCny5RrUw3TyP0jF849kdyOpIT5BDS7mBsIOHuutvOoAplxzhqapk0py9yM+CLY12rjxZBXtde6Ldg7idq9nDp4ErG2aHkVpTpqwaq8BHv/qN6SiCJX0mPHIZjpnz61x6cWS0CheiQVUNQC7Is5L5Rszb134zMPfopzLFrPfBygwjw8XF9AQfzf313/0EhYMKo3cNic2xMqkINsvqazAUpqO7MVj6IaHr0TVDb5tVbuUpOZjcLgXJ9qzowa42T5cU1ylNoajyksLdK9CdBEHW3eypAYbRBjletEKirtMYyiFZZUeNPFlCzXQdlj0eRMktG3GRxvfEjTMldcrLV/ZqNLKerpNNnuuoP+mNqPg38CQyWVIn3W2YL4j8Sph+/jeDeK7H4AVY26sqyawcHXYZp7NHpK/mWh3e+V6eup7FQ+WBW/oGM6Zu2bFO4u9CDwZvC1h1lQpLmPRg60UUCmmb+qvAihsmoBV9QPw74dX5XtqUuhZO0Ng8hQnCikTyaXGV3fv7If/TsGoLOZTXrdKRoSq9fJUTxqsmTYJr2AQ/h+LaxpFYXfPcgPXydw8dApESZMwdlBpxe1I78Si51BJmaX0n4ZRSITx2OCo4V6eSvenY0iN/pvyWy48JhmYlgfJTxPk6x/g5st3dV10gyepRGW1xTrRl1u6HoQ8gN1ygznVE+q/gply6U467ORroGdkISxImkF5aAwdeYc3DiTQ/gJGgdYLnXI5iBbqQShu5KqKMkjq3omdp/9RrYj0BDOcEgZ5p2HfaDWsxM3Y2jV31IbIdZKy3hLR3KLaIoeEdfmgFIOJSdwXxF7UzRxOJjTtF4XJnYs+36B3wT3mkSU5PL5HoFfq79z+eQS0UeF9ds1zq1DoiqHE6BnJPbMhZY9lFyNDTN1ztPVppUE1aelBy2MFarT13mckGPvZ2ZBiOe59w05hXA+HSptfsF0WuVThprUL4EuoRkqq2zIp0BqtwnxEP/73dHmDAlrDV0gimt9ocbATfpEPFEFcb7jej6MbG+dUyvx8juYI553bVxuCQ04YPIZQO3RaTrUJSObjiWSLMnl0heYnlplnCKPQtG6WwbIkwBNsuXv3AgggUOxlGbYw4/nNdIuKUk8aka/Zsr3hsn2KLwMzsEaFdPIs1cJePOMZY36EP2rcIDHusNxdiQnHkuABRfrxnewScrSvj2gwQwaE/zvu8st+ujROKOrJwZAzsUHsahItCmUQ27iu5ojf/wHitju/JIKR6F17E7MIQmA0mm70IfIjg8KYyXlLa6LbRFS9fPWH2cR3VtuhTU/4kWUNVUFkMfs7iyJux8k2ufKmHjmcYEim4AeEQTI/oqg0QxvxUdLysVq6s+9z+Gh1tdV9uFWN77zjWOPHH/Qc8MT58IPfiWunpRGXVLLJrOdLhlNKfQfSKVFgfI2GZ5doITr4g6wekFHVX5MtjFDM0qB5Qb7eYZt5Jw7TLp6bODesLYe/qN5w+Og0PX3X5pXSFY2NKVMx/+SOxLlGV+fUzihaaUqqGUxelAbtDz9bC+knXFw4nJuS8PAAkDb8Nz/ztVSEVkOh5TLKLHU773G9cb9X+2FjaCV2wtQELgEFtJd68hoKfkVCfsJHkRHDyELmdVC5rInIxy5RGFms3jBYzACfrfxxA4Iw9AqL1uvxuHi3bMUkY28Q7uK76W/+VYraTYEuKMEJnmIAE1uKjT6zNonx9BBo1E/qmqkr/zlKhRhJMWZYQGilIAZewj2Z79E5w7ovhLW/SL58C95m7WNAbeTs3REk3h3ylsLwiRf6iNflPYgvvEf3HF/JEsJcZuDRzcHjSXKaizm5FgqnH/ZOgJ59ywpYDo6rDhze7Z8tmz8l2YI6faLQ4z1EKdHcUAXJ30qEKYJh6Dl4oExso0ZGVMZw+jPu0UcDtBfBRxWFU20k5EyR+BpAuVxEoBmgqv1qTYW2/BriYmOlwdGJg2dfgklPauwGeSproxmhZVrZzWdI+eQERxigQn7BCDwjmrDIYlKoJvvoo/RM1CBTNGKtuNiGKwGMMby3PFm2nNV0wAdjce2FEKWqaPu5F7QpEHgXL9yzm++JvNXrj80R0/EwBwSR9onsyAlRodzHFkp9KjjGj9OjhviGHX5B4/Lle9QIPwOIu8pdbz5Rd6gNtSZrva12YPpAidOYL2SKN4qT52Dhz3hegAEdH3ks5C4Vzee2GcSIYPFdHqq0fvefyP5NCkN9FVqKmfVOtirN5BXClPH885Yoxp8PznuvdrR5hCtL5fo7RNUcfHtAZdfONBQZ88wdqpZprqU6zgZqfGY5i+XbJHRMXy5HrSw/litDsze+UsYvTy+MqyIsVSw15juygdlyDhNYZeU+18XRMtXOH+rRvMVS3iQfvP6qVYreEToymPyH7Or12ViXVuHg0+J95r0MxN6UutlQSxu0uXhNcqws/obz6d86d+60dbDhuAhhCjN0ByKdCUuxj4w/dPpwtyayF5+jfi62njmXNXwzhBFqbp0ud3pFF+hI406zGGo0kw6zyswM3Yd4f33XKKJ+lC1kQ9mi6WRd0fjhIlJaelguhiRRENUScVz2Y5p8jlGXG+3wJbHEwqwjRbKXw/48M10+tNyBTd24D0Pvu63UvymJC3BDC/Znx076GBuVdOhX0mKk4FQMQF/p3sHVMmepz8J9hJ8ggD9WauSTaCWOU+OCI1QRot/5FGcXLyt1653a0aJU1Tt66Td90DySAiwQBtoyrN4PIqjm+uVQ9PC0lt6yHKR+kWWoW8pqWPdK5PMti647yMmAdYZAYoZ7rhX9g1Scv/6UC9yotQJztMGA8fSqwQmN3eIoU0K9QSoMVOIQ2Zo2ynYfpkETuyVOmN3gBt51psIAAOUxD5HJOAcoNjDfq7QKAlRiA7adTtAUJ3iEoPdcblw7DNSumxuklfjB5ukZcy414AM9bbHMPGxZy4TCoNSR1AwlS22FqaopqPZC54sbeo05FoAuUtKV/FLY/V7UB7dOLk/YOBwHORxwPRjPp1NH8I6e1VnoUQS3oOomCf8mkNnPoyLPD24Gy+w/5KyhbludAZqIg3cWTvnL7SaTP6bJ+z0EAbuzBOS8YWHPfFB565FAMiKJ7cylUCWzzzD/dsDlpGh+nq8XQpTPqKEga5v+TpTfmhxt7Wc1mn+qoo+4oMl0VXj9KcLnewU4u2tvrJaY3RESUh/6Ap9fNuwHqKIvMPP0u59+HeOJ0bmVDOCSp//GIYaYm6oR75dIMnOSCUr5Tal6ZK4eCXGupxrhdiSWkJao6C+F0nXCR8jtaeUkpVNMy8c6wKMvuGqAO/TA0AGRGR79uZff5YWOpFPMIPXmz4eLoGbGr5pfdqeI6zgIFMtY7QHZAPmqnZbLSLh5c3PYHjFXtcxtVGXVPgNFDjdu6ikdunZhRsOusa2qJOR9lP3mV2XKWY+I/PGw+NSWewAmBHZg8O9MPDygoNu9IkV7sJlQbgYoMtIFeur+Le4aMpugy9mhF7/2MtMBEbM8mBRzSg7YYeqqn4pbxuMUPJzWlazZxZH5qd6zJPTD8+OJvLzHv/g1k3NfEv6ZHFeu5uVsiZhPNZlYEajEnXGsxUcXhvCOxTZB8CXqLAbooU/lPOS8vVdIoWBAp6AGr1zQdaoY/2RAXmhBI1bniYK3lXUwUwW25d+UPEpp4xum93lpLtQ1VF9OHqwOiWRzt//0E++kd7LIhTVw3hwT4sxtpUWrZXsZZJQW+IBTcAMrH+jB8FpX7zfn9O+jUhSiYoAlc8gC2lJvDHpFM2QLFA935prFzEWzozZ1mnHuG4PJWRoFLigMc7+tRvFYe7+OateLnOemKvxR6OHpFMXQOVnab8Y7RYMMckOC/Pk2Uua5UJ96H57d682t+yH+lg9JVMEibCha61Y2g0pK0HsOVz/fDhGvfKXNOVLEAOhSYcu0JthJ6qmTOVhYujAdnz94pOuc6LuSStYsi3EsXQHMg7YcdQmd1Z1L/7uvIRfMpWyxWVFBz89gyGvw+NZuXZAk2aYc355v9IsTHXxf4R6tbsheQmxCF3zWIthTna/TMKUv4xj58U0si1Wiz+H0E5JT5E+JfFUFWn+IkpY4ctY00DzrEU11vvmF/Sp9qwJZqej9kMOyCZ+PFT2CAUvpzPeus/ZZRcpuLglA6fMUwHE4uLRAiInBONMVSMgnhtfV2Wq2OUapLRVFGTZ+JvJX0wae78C3b1wzty83riI0MGYoSNJxm5TqWdKh5dAN7pBsxAL6bSqCgfysGht0VVO4gHpGkxfcFN4hJInlCQ9cJRll8j/KrG9NBTHnTKQQBCQouKNPltXgH8inTS5P7A0W3VixtA4KWUN0xjsaDBFfdz4inrUvdZqBggoNMIQNld2t98bd2YWbXl9DjQeITdRzxa/Ms+ypU/mH/aygd/6OjWoiP4BSYehDj9VJQI5/IJ4/fPqlAB9YHCs+iX2fmce7/8S66MFCHPu7XyTNuEhYcRL+cVBub3R/auQQ3JnYdWBVKfTKCB6F6cWbv1vul5DuTwKEKZIJz8rdT14tzjpyWqejPXnC2VyVU9qfh5ix2Ez+6Jm7RmruzhD73KP3Sz2qAsza5TGaVMdlVoha7ILqd5u9AhSi6LF7txFM6BSM//zmavTaQi6k62UQeuud77zxJJgW48pgeQOaK7ZKh4ScbzDUw8RXSkJivcY2NAFy80UICtfJ1++YwjciTJcHRR7NPHv3W6JaWNqyaVXFxk682oq6Pr1+aAIRkcPs7u7MDV2uF8g2qa4stli2WUPNsmaOBt6C+KgvCCgpSGqKuKAL//SVuoHG8PI+nLOqFEIpzI5MGFYwZ12l/rw3u4OFhJg5KNKBHcfo1wpI9vq5ByiV3FnUJdRw8VoNsWgZF2+IqTv3Cw6XtH8ULsxv1kAsCEzeRDHy5w9WDz6N/GN3jjJ2p2UssIa4L5lqDGvdlGB8bVPgD+2Xq8pHhRU3bCC7/s4jWs6Ys5C93yIykVX88UfIJLL7T6rCr8VaSn+1iw/lqaj0XJM6ZAbZK4s+uNEhLzjMtz6q+RYicwzWriHoOkk9cfKB738X/pk28Tk3ZUHGzhPPESXr2fPowqBQSX3lBOQGaDQ+V0LnDjdNqIyfDCDdwiIbQKUS4z0LGV2AIyyYjz30FHaKTkbhfTNP6awF9wb3YPRAA/pdZtIgmGelBJXq78XyYQGdpvman9CsN4N2lBMdzbI126lqd8xHBi/j38bgtisbd1eOQ2j/wni2d75weFabp9SBdGPzOdkGn1GZfDDlIRG4P2uydlvq21uz+rVUVva+eYN6jBitpmxN00l2EnUidLQ5i4TpC+L2Es9XmM6L5+9boMDmVB3HVTmr1rq0PKNTQ4WWWhHhMumk4r6h6306x6vuEm77620unPqeKPrLc9VdSAZB2kE3/lOafbVTftmoWu2ZAGYZowb3wiQiPqwbSzmH/GWWKOELhczw8Xan5CNBwS4F5TFkFiJ/z9i6FgFx00JfiBPyZgvg5N9f/rQr5l6cYXQv8zTTwoj1HHqgVN4o1722S5FPyshgX7cBbpg6L45K4CW2hHFM+/+nUxrafRyBiCDqm2XOWpYdvV1cVX76FxZ8/bsTxD6Vg13bCN9LDrZlTQKvBefuxmqjUAJOwGUt7hM39YmPWB2aiKQQsc1g1BwPar9Xhk8A7cEpexydco9ZYW5+RP3QGddrhYeNuAMRq7vRk/7tMPbKH5nYA9KV7okKhR91IMo/Ld1fnppwBOlDHvTQDsYok+CAoCjmR8jQqbpWh1ZGLcPhI70anGGPJkr9J8niD+BMIGb3MQy/XL8Vr8QtEzpNIR4M2wMenH74w1iehNuwON0h/YsPwJJXz7zqvCxWyhhaOrjqVWSTB6+Dj7A7cUs2An/yTymUlqAC2/L8UpPKuYeInnUYeiaJ2ntsiLwJOqkC7+xWg2tsA8K9nLZ9+GImBYuSbehRnAxg93MxIJgMMpgLo/v8QgJ1hwlIbIlmSfVFUZJtmuIy9gZ4AAnkpS1SlNUPZPBJfBnETfvk1QUw/rFCVIPiHLM95wHByZCmiKjfhf4FMn+//2Ia+TqlrKMexFh+Is0+EUddjWd2PZfOOviS6pxc068lTpao+/q+aAaEfgjEhEraCSArR7sgX8NZjNOr2d29u2QcDbECEsJW6IAIWUDuz+59incbZdLyshlyWiwlcMoTOjy8Pjbcu/gfdfbvvO/OaiTGqROADMfmjr2z4OAF3Pf+mxoSRbL/e0x7+mmbxApScs88UL3YjLHa2sLAMmvQHqswIOSpcrSpJcMznqBHHF9Uvqwv0YT+CNUaDq/srG6UL9Rw9CEWylmAcnjMATUmMKJ4mLgdvZlI4wchJIfRvyT5GAfzsksJUF9/rRZjjDIMbd0VEHB01glnsEjUk0qd9gGwUEkj7XRTmoxpK1T6UJ6dIHL5SRbzqU/dsQzkuufTZyVCSYA5RbJSzXvF0BRCmw/l42IB4I6uAA3OUP9PtPemkgmHlFBXbVFyNEWVjyWsyJ5bw2NL3Q54Nimro4srBf7TF4qXUAZAiGYAVc8dl5lmXSH1Jh7uHuc81hkCMg5kDhqcBmvRkApEDnokWFcOq//4G0tgGyCiz8GNF3att1fyYT2ShdC5Svvgt2NUngoKch3zv3DW0YhErWQAQ1l2nD+zrGDDEd3DSfieHF5vAkpvc8fceA0lCHVgU9NJuhxsvSXjMU4qIXUKuhmgEbhTxzb4hx+Hl3BCyKUyi8LElRNM0LmE8vHQPjYiBteQqkKgqFOVJBf29qNENB7wv0AzcDaXifJ3bWGpXDhgWd58YhHG4ERryevlQO0Vy4048tcrCNIrgZ/BiLN3uSu5ww1x4Jzkf8mkOKeS3vjnGcp4NCuYHXpFIpGtrBxFB17wuxwOlEAWDOxc3a/w6q4U9hXiExzLmAHBYfpGytYRxzD11pDH+wuKDwqYmHeMIYpFbJqP4rtLj9YgEJI50wkvrwcrA3kTJHR05asxF9K8BI6wzNCzavfu2nNK1Vo2t+Ick9XcXbSViyJv+ewQaqW8dWI0keEAb/4qXfjL5cVkHLlaUYQaQRQK4ugR9bY8myyr+Bkey83LHEEEE/a7bc15RZbUK5Fa4YK6ATdSIHz98wrOIThiyQKcJjHdh7k5W4O7IeEu5rpI+L4A+Xakai3TKEsOh5KpkJCM7UyhWhItkffvok03GRDEDAq/T7SOgqRD
// 修改于 2025年 8月 8日 星期五 15时40分52秒 CST
