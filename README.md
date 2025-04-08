# WaZi Minecraft Launcher

一个现代化、高性能、跨平台的Minecraft启动器。

[![许可证: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

## 项目概述

WaZi Minecraft Launcher是一个用Java开发的跨平台Minecraft启动器。它原生开发，为Windows、MacOS和Linux提供统一的用户体验。该启动器注重性能和用户体验，采用现代UI设计，提供流畅的动画效果和直观的操作界面。

## 主要特性

- 🚀 跨平台支持（Windows/MacOS/Linux）
- 💎 原生开发，高性能
- 🎨 现代UI设计
  - 全局毛玻璃效果
  - 流畅的动画效果
  - 2px圆角设计
  - 简约扁平风格
- 🔒 安全的账号管理
- 📦 开箱即用，无需额外配置
- 🛠 全面的游戏版本管理
- ⚡ 优化的启动参数配置
- 🌐 多语言支持

## 技术栈

- 开发语言：Java
- UI框架：JavaFX
- 构建工具：Gradle
- 打包工具：jpackage
- 依赖管理：Gradle

## 系统要求

- Windows 10/11
- macOS 10.15+
- Linux（主流发行版）
- Java 17+

## 开发环境设置

1. 克隆仓库
```bash
git clone https://github.com/WaZixwx/WaZi-Minecraft-Launcher.git
```

2. 安装依赖
- JDK 17+
- Gradle 7.0+

3. 构建项目
```bash
./gradlew build
```

4. 运行项目
```bash
./gradlew run
```

## 项目结构

```
src/
├── main/
│   ├── java/
│   │   ├── core/           # 核心功能模块
│   │   ├── ui/             # UI相关代码
│   │   ├── utils/          # 工具类
│   │   └── resources/      # 资源文件
│   └── resources/
│       ├── styles/         # CSS样式文件
│       ├── images/         # 图片资源
│       └── i18n/           # 国际化资源
```

## 贡献指南

欢迎贡献。在提交PR之前，请确保：

1. 代码符合项目标准
2. 添加适当的测试
3. 更新相关文档

## 许可证

本项目采用MIT许可证 - 详情请参阅[LICENSE](LICENSE)文件

## 作者

- WaZixwx ([GitHub](https://github.com/WaZixwx))
- 官方网站：[mc.wazixwx.com](https://mc.wazixwx.com)

## 致谢

感谢所有为这个项目做出贡献的开发者。