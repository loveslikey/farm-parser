# FARM解析器

FARM解析器是一个Java工具，用于解析和分析OneSAF EDM的FARM（Feature Attribute Runtime Map）数据格式。该工具提供了完整的解析功能，可以将二进制FARM文件解析为Java对象，并支持导出为JSON格式以及生成数据结构可视化图表。

## 功能特点

- 解析二进制FARM文件（farm.dat）
- 支持大端和小端字节序
- 提取文件中的特征、属性和映射信息
- 导出解析后的数据为JSON格式
- 生成FARM数据结构的DOT格式图（可使用Graphviz工具转换为图像）
- 提供命令行和交互式界面

## 系统要求

- Java 11或更高版本
- Maven 3.6或更高版本

## 构建与运行

### 构建项目

```bash
mvn clean package
```

构建成功后，将在`target`目录下生成`farm-parser-1.0-SNAPSHOT-jar-with-dependencies.jar`文件。

### 运行方式

#### 命令行模式

1. 解析FARM文件并打印信息摘要：

```bash
java -jar farm-parser-1.0-SNAPSHOT-jar-with-dependencies.jar parse farm.dat
```

2. 解析FARM文件并打印详细信息：

```bash
java -jar farm-parser-1.0-SNAPSHOT-jar-with-dependencies.jar parse -v farm.dat
```

3. 解析FARM文件并导出为JSON格式：

```bash
java -jar farm-parser-1.0-SNAPSHOT-jar-with-dependencies.jar export farm.dat
```

4. 解析FARM文件并导出为指定的JSON文件：

```bash
java -jar farm-parser-1.0-SNAPSHOT-jar-with-dependencies.jar export -o output.json farm.dat
```

#### 交互式模式

直接运行JAR文件，不带任何参数：

```bash
java -jar farm-parser-1.0-SNAPSHOT-jar-with-dependencies.jar
```

程序将提示输入FARM文件路径，然后解析并显示信息。

## FARM数据格式说明

FARM（Feature Attribute Runtime Map）是OneSAF EDM（环境数据模型）的运行时表示。它维护EDM中可能特征、可能属性以及这些属性的可能值之间的关系。

主要组成部分：

- **FARM表**：二维表，每行是一个特征，每列是一个属性。
- **特征**：具有类别、代码、几何类型、优先级等属性。
- **属性**：具有代码、数据类型、单位和可编辑性等特性。
- **数据类型**：支持多种数据类型，包括整数、浮点数、字符串、枚举、布尔值和UUID。
- **映射**：包含特征标签和几何到特征类别映射、特征类别到特征映射以及属性代码到属性映射。

## 项目结构

```
farm-parser/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── onesaf/
│   │   │           └── farm/
│   │   │               ├── model/        # 数据模型类
│   │   │               ├── io/           # 二进制读取和导出工具
│   │   │               ├── parser/       # 解析器实现
│   │   │               ├── util/         # 可视化工具
│   │   │               ├── App.java      # 主应用程序
│   │   │               └── CommandLineProcessor.java # 命令行处理
│   │   └── resources/
│   │       └── log4j2.xml               # 日志配置
│   └── test/
│       └── java/
│           └── com/
│               └── onesaf/
│                   └── farm/
│                       └── parser/
│                           └── FarmParserTest.java  # 单元测试
└── pom.xml
```

## 日志

程序运行日志存储在`logs/farm-parser.log`文件中。

## 许可证

Apache License 2.0