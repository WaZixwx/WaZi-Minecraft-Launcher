# WaZi Minecraft Launcher

一个现代化、高性能、跨平台的Minecraft启动器。
A modern, high-performance, cross-platform Minecraft launcher.

[![许可证: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Java 17+](https://img.shields.io/badge/Java-17+-blue.svg)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![Gradle](https://img.shields.io/badge/Gradle-7.6+-blue.svg)](https://gradle.org/)

## 项目概述 | Project Overview

WaZi Minecraft Launcher是一个用Java开发的跨平台Minecraft启动器。它原生开发，为Windows、MacOS和Linux提供统一的用户体验。该启动器注重性能和用户体验，采用现代UI设计，提供流畅的动画效果和直观的操作界面。

WaZi Minecraft Launcher is a cross-platform Minecraft launcher developed in Java. It is natively developed to provide a unified user experience for Windows, MacOS, and Linux. The launcher focuses on performance and user experience, featuring modern UI design, smooth animations, and an intuitive interface.

## 主要特性 | Key Features

### 🚀 跨平台支持 | Cross-Platform Support
- Windows 10/11
- macOS 10.15+
- Linux（主流发行版）
- 统一的用户体验
- Unified user experience across platforms

### 💎 原生开发 | Native Development
- 高性能Java实现
- 原生UI渲染
- 优化的启动参数
- High-performance Java implementation
- Native UI rendering
- Optimized launch parameters

### 🎨 现代UI设计 | Modern UI Design
- 全局毛玻璃效果
- 流畅的动画效果
- 2px圆角设计
- 简约扁平风格
- Global frosted glass effect
- Smooth animations
- 2px rounded corners
- Minimalist flat design

### 🔒 安全的账号管理 | Secure Account Management
- 支持Microsoft账号
- 安全的令牌存储
- 自动刷新令牌
- Microsoft account support
- Secure token storage
- Automatic token refresh

### 📦 开箱即用 | Ready to Use
- 无需额外配置
- 自动更新
- 内置Java环境
- No additional configuration required
- Automatic updates
- Built-in Java environment

### 🛠 全面的游戏管理 | Comprehensive Game Management
- 版本管理
- 模组管理
- 资源包管理
- 皮肤管理
- Version management
- Mod management
- Resource pack management
- Skin management

### ⚡ 优化的性能 | Optimized Performance
- 快速启动
- 低内存占用
- 智能资源管理
- Fast startup
- Low memory usage
- Smart resource management

### 🌐 多语言支持 | Multi-language Support
- 中英双语界面
- 自动语言检测
- 支持更多语言扩展
- Chinese and English interface
- Automatic language detection
- Support for more language extensions

## 技术栈 | Tech Stack

- **开发语言 | Development Language**: Java 17+
- **UI框架 | UI Framework**: JavaFX
- **构建工具 | Build Tool**: Gradle 7.0+
- **打包工具 | Packaging Tool**: jpackage
- **依赖管理 | Dependency Management**: Gradle
- **日志系统 | Logging System**: SLF4J + Logback
- **JSON处理 | JSON Processing**: Gson
- **工具库 | Utility Libraries**: Apache Commons

## 系统要求 | System Requirements

### Windows
- Windows 10/11
- 4GB RAM
- 2GB可用磁盘空间
- 支持OpenGL 2.0的显卡

### macOS
- macOS 10.15+
- 4GB RAM
- 2GB可用磁盘空间
- 支持OpenGL 2.0的显卡

### Linux
- 主流发行版（Ubuntu, Fedora, Debian等）
- 4GB RAM
- 2GB可用磁盘空间
- 支持OpenGL 2.0的显卡

## 安装说明 | Installation Guide

### Windows
1. 下载最新版本的安装包
2. 运行安装程序
3. 按照向导完成安装
4. 启动器将自动创建必要的目录和配置文件

### macOS
1. 下载.dmg文件
2. 将应用拖到Applications文件夹
3. 首次运行时可能需要允许来自未知开发者的应用

### Linux
1. 下载对应发行版的安装包
2. 使用包管理器安装
3. 或直接运行可执行文件

## 使用说明 | Usage Guide

1. **首次启动 | First Launch**
   - 启动器将自动检测系统环境
   - 创建必要的目录和配置文件
   - 提示登录Minecraft账号

2. **账号管理 | Account Management**
   - 支持Microsoft账号登录
   - 支持离线模式
   - 支持多账号切换

3. **游戏管理 | Game Management**
   - 版本安装和切换
   - 模组和资源包管理
   - 游戏设置配置

4. **设置 | Settings**
   - 界面主题设置
   - 启动参数配置
   - 网络代理设置

## 开发指南 | Development Guide

### 环境设置 | Environment Setup

1. 克隆仓库
```bash
git clone https://github.com/WaZixwx/WaZi-Minecraft-Launcher.git
cd WaZi-Minecraft-Launcher
```

2. 安装依赖
```bash
./gradlew build
```

3. 运行项目
```bash
./gradlew run
```

4. 国内开发者请在 https://mirrors.cloud.tencent.com/gradle/gradle-7.6-bin.zip 镜像源下载Gradle


### 项目结构 | Project Structure

```
gradle/
src/
├── main/
│   ├── java/
│   │   ├── core/           # 核心功能模块 | Core functionality
│   │   ├── ui/             # UI相关代码 | UI components
│   │   ├── service/        # 服务层实现 | Service layer
│   │   ├── model/          # 数据模型 | Data models
│   │   └── utils/          # 工具类 | Utility classes
│   └── resources/
│       ├── styles/         # CSS样式文件 | CSS stylesheets
│       ├── images/         # 图片资源 | Image resources
│       └── i18n/           # 国际化资源 | Internationalization
└── test/                   # 测试代码 | Test code
```

### 代码规范 | Code Style

- 使用4个空格进行缩进
- 类名使用大驼峰命名法（PascalCase）
- 方法名和变量名使用小驼峰命名法（camelCase）
- 常量使用全大写，下划线分隔（UPPER_SNAKE_CASE）
- 所有代码必须包含中英双语注释
- 遵循Java代码规范

## 贡献指南 | Contributing

欢迎贡献。在提交PR之前，请确保：

1. 代码符合项目标准
2. 添加适当的测试
3. 更新相关文档

详细指南请参阅[CONTRIBUTING.md](CONTRIBUTING.md)

## 许可证 | License

本项目采用MIT许可证 - 详情请参阅[LICENSE](LICENSE)文件

## 联系方式 | Contact

- 作者 | Author: WaZixwx
- GitHub: [https://github.com/WaZixwx](https://github.com/WaZixwx)
- 官方网站 | Official Website: [mc.wazixwx.com](https://mc.wazixwx.com)
- 问题反馈 | Issue Tracker: [GitHub Issues](https://github.com/WaZixwx/WaZi-Minecraft/issues)

## 致谢 | Acknowledgments

感谢所有为这个项目做出贡献的开发者。
Thanks to all developers who contributed to this project.
