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
