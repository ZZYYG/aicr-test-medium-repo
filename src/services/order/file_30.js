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
