/**
 * 服务模块
 * @module Service
 */

const fs = require('fs').promises;
const path = require('path');
const http = require('http');
const express = require('express');
const winston = require('winston');

/**
 * 服务状态枚举
 * @enum {string}
 */
const ServiceStatus = {
  STOPPED: 'stopped',
  STARTING: 'starting',
  RUNNING: 'running',
  STOPPING: 'stopping',
  ERROR: 'error'
};

/**
 * 服务类
 */
class Service {
  /**
   * 创建新的服务实例
   * @param {Object} config - 配置对象
   * @param {Object} database - 数据库连接
   * @param {Object} cache - 缓存实例
   */
  constructor(config, database, cache) {
    this.config = config;
    this.database = database;
    this.cache = cache;
    this.status = ServiceStatus.STOPPED;
    this.startTime = null;
    this.app = express();
    this.server = null;
    
    // 创建日志记录器
    this.logger = winston.createLogger({
      level: config.logLevel || 'info',
      format: winston.format.combine(
        winston.format.timestamp(),
        winston.format.json()
      ),
      transports: [
        new winston.transports.Console(),
        new winston.transports.File({ filename: 'error.log', level: 'error' }),
        new winston.transports.File({ filename: 'combined.log' })
      ]
    });
    
    // 设置路由
    this._setupRoutes();
  }
  
  /**
   * 设置API路由
   * @private
   */
  _setupRoutes() {
    this.app.get('/health', (req, res) => {
      res.json({
        service: this.config.serviceName,
        status: this.status,
        uptime: this.startTime ? (Date.now() - this.startTime) / 1000 : 0,
        version: this.config.version || '1.0.0'
      });
    });
    
    this.app.get('/api/v1/status', (req, res) => {
      res.json(this._getStatus());
    });
    
    // 这里添加更多路由...
  }
  
  /**
   * 获取服务状态
   * @private
   * @returns {Object} 状态对象
   */
  _getStatus() {
    return {
      service: this.config.serviceName,
      status: this.status,
      uptime: this.startTime ? (Date.now() - this.startTime) / 1000 : 0,
      version: this.config.version || '1.0.0',
      database: this.database ? 'connected' : 'disconnected',
      cache: this.cache ? 'connected' : 'disconnected'
    };
  }
  
  /**
   * 启动服务
   * @returns {Promise<void>}
   */
  async start() {
    this.logger.info();
    this.status = ServiceStatus.STARTING;
    
    try {
      // 连接数据库
      if (this.database) {
        await this.database.connect();
      }
      
      // 启动HTTP服务器
      return new Promise((resolve, reject) => {
        this.server = http.createServer(this.app);
        this.server.listen(this.config.port, () => {
          this.status = ServiceStatus.RUNNING;
          this.startTime = Date.now();
          this.logger.info();
          resolve();
        });
        
        this.server.on('error', (err) => {
          this.status = ServiceStatus.ERROR;
          this.logger.error();
          reject(err);
        });
      });
    } catch (err) {
      this.status = ServiceStatus.ERROR;
      this.logger.error();
      throw err;
    }
  }
  
  /**
   * 停止服务
   * @returns {Promise<void>}
   */
  async stop() {
    this.logger.info();
    this.status = ServiceStatus.STOPPING;
    
    try {
      // 关闭HTTP服务器
      if (this.server) {
        await new Promise((resolve, reject) => {
          this.server.close((err) => {
            if (err) {
              reject(err);
            } else {
              resolve();
            }
          });
        });
      }
      
      // 关闭数据库连接
      if (this.database) {
        await this.database.close();
      }
      
      this.status = ServiceStatus.STOPPED;
      this.logger.info();
    } catch (err) {
      this.status = ServiceStatus.ERROR;
      this.logger.error();
      throw err;
    }
  }
}

// 示例用法
if (require.main === module) {
  // 创建配置
  const config = {
    serviceName: 'api',
    port: 8080,
    logLevel: 'info',
    version: '1.0.0',
    database: {
      host: 'localhost',
      port: 5432,
      username: 'user',
      password: 'password',
      database: 'apidb'
    }
  };
  
  // 这里应该有实际的database和cache的实现
  // const service = new Service(config, database, cache);
  
  // 启动服务
  // service.start()
  //   .then(() => console.log('Service started'))
  //   .catch(err => console.error('Failed to start service:', err));
}

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
