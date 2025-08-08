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
