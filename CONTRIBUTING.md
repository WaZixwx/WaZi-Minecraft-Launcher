# 贡献指南 | Contributing Guide

感谢您对WaZi Minecraft Launcher的关注！我们欢迎任何形式的贡献，包括但不限于：

* 提交问题报告
* 提出新功能建议
* 改进文档
* 提交代码修复
* 添加新功能

## 开始之前 | Before You Begin

1. 请确保您已经阅读并同意我们的[行为准则](CODE_OF_CONDUCT.md)
2. 检查现有的问题和拉取请求，避免重复提交
3. 如果您是第一次贡献，可以先从标记为"good first issue"的问题开始

## 开发环境设置 | Development Environment Setup

1. 克隆仓库：
```bash
git clone https://github.com/WaZixwx/WaZi-Minecraft.git
cd WaZi-Minecraft
```

2. 安装依赖：
```bash
./gradlew build
```

3. 导入到IDE：
- 推荐使用IntelliJ IDEA
- 确保使用JDK 17或更高版本
- 导入为Gradle项目

## 代码规范 | Code Style

我们遵循以下代码规范：

* 使用4个空格进行缩进
* 类名使用大驼峰命名法（PascalCase）
* 方法名和变量名使用小驼峰命名法（camelCase）
* 常量使用全大写，下划线分隔（UPPER_SNAKE_CASE）
* 所有代码必须包含中英双语注释
* 遵循Java代码规范

## 提交规范 | Commit Guidelines

提交信息应遵循以下格式：

```
<类型>: <描述>

[可选的详细描述]

[可选的关闭问题引用]
```

类型包括：
* feat: 新功能
* fix: 修复bug
* docs: 文档更新
* style: 代码格式调整
* refactor: 代码重构
* test: 测试相关
* chore: 构建过程或辅助工具的变动

## 拉取请求流程 | Pull Request Process

1. 创建新分支：
```bash
git checkout -b feature/your-feature-name
```

2. 提交更改：
```bash
git add .
git commit -m "feat: add your feature"
```

3. 推送到远程：
```bash
git push origin feature/your-feature-name
```

4. 创建拉取请求：
* 访问GitHub仓库
* 点击"New Pull Request"
* 选择您的分支
* 填写详细的描述
* 提交PR

## 问题报告 | Issue Reporting

提交问题时，请包含以下信息：

* 问题描述
* 复现步骤
* 期望行为
* 实际行为
* 环境信息（操作系统、Java版本等）
* 相关日志或截图

## 行为准则 | Code of Conduct

请参阅我们的[行为准则](CODE_OF_CONDUCT.md)，了解我们的社区准则。

## 许可证 | License

通过提交代码，您同意您的贡献将根据项目的MIT许可证进行许可。

---

# Contributing Guide

Thank you for your interest in WaZi Minecraft Launcher! We welcome contributions of any kind, including but not limited to:

* Submitting issue reports
* Suggesting new features
* Improving documentation
* Submitting code fixes
* Adding new features

## Before You Begin

1. Please make sure you have read and agree to our [Code of Conduct](CODE_OF_CONDUCT.md)
2. Check existing issues and pull requests to avoid duplicates
3. If you're new to contributing, start with issues labeled "good first issue"

## Development Environment Setup

1. Clone the repository:
```bash
git clone https://github.com/WaZixwx/WaZi-Minecraft.git
cd WaZi-Minecraft
```

2. Install dependencies:
```bash
./gradlew build
```

3. Import to IDE:
- Recommended: IntelliJ IDEA
- Ensure JDK 17 or higher is used
- Import as Gradle project

## Code Style

We follow these code style guidelines:

* Use 4 spaces for indentation
* Use PascalCase for class names
* Use camelCase for method and variable names
* Use UPPER_SNAKE_CASE for constants
* All code must include bilingual comments (Chinese and English)
* Follow Java coding conventions

## Commit Guidelines

Commit messages should follow this format:

```
<type>: <description>

[optional detailed description]

[optional issue references]
```

Types include:
* feat: new feature
* fix: bug fix
* docs: documentation changes
* style: formatting changes
* refactor: code refactoring
* test: adding or modifying tests
* chore: changes to build process or auxiliary tools

## Pull Request Process

1. Create a new branch:
```bash
git checkout -b feature/your-feature-name
```

2. Commit your changes:
```bash
git add .
git commit -m "feat: add your feature"
```

3. Push to remote:
```bash
git push origin feature/your-feature-name
```

4. Create pull request:
* Visit GitHub repository
* Click "New Pull Request"
* Select your branch
* Fill in detailed description
* Submit PR

## Issue Reporting

When submitting an issue, please include:

* Issue description
* Steps to reproduce
* Expected behavior
* Actual behavior
* Environment information (OS, Java version, etc.)
* Relevant logs or screenshots

## Code of Conduct

Please refer to our [Code of Conduct](CODE_OF_CONDUCT.md) for our community guidelines.

## License

By submitting code, you agree that your contributions will be licensed under the project's MIT License. 