# OpenAPI Documentation Generator

一个基于Spring Boot的应用程序，用于从OpenAPI规范自动生成格式化的Word文档。

## 项目介绍

本项目提供了一种简单的方式来将OpenAPI/Swagger规范文件转换为美观的Word文档，便于API文档的分发和阅读。
系统支持从多种来源获取OpenAPI规范，包括URL、JSON字符串和JSON文件，并通过可自定义的模板生成格式化文档。

### 主要功能

- 从多种来源获取OpenAPI规范:
  - 从URL获取OpenAPI JSON（如Swagger UI地址）
  - 从JSON字符串内容直接生成
  - 从上传的OpenAPI JSON文件生成
- 支持自定义文档模板
- 内置Swagger UI接口文档
- 代码语法高亮支持
- 文档内部引用链接支持
- 响应类型和参数详细说明

## 快速开始

### 系统要求

- JDK 8或更高版本
- Maven 3.3+

### 编译和运行

1. 克隆仓库

```bash
git clone https://github.com/yourusername/openapi-doc-generator.git
cd openapi-doc-generator
```

2. 编译项目

```bash
mvn clean package
```

3. 运行应用

```bash
java -jar target/tips-1.0-SNAPSHOT.jar
```

或者使用Maven直接运行:

```bash
mvn spring-boot:run
```

4. 访问应用

应用启动后，可以通过浏览器访问Swagger UI界面:

```
http://localhost:8080/swagger-ui.html
```

## 使用指南

### 从URL生成文档

```
POST /api/openapi-doc/generate-from-url
```

参数:
- `url` (必填): OpenAPI规范的URL地址
- `templateName` (可选): 要使用的模板名称
- `template` (可选): 自定义模板文件上传

示例:
```bash
curl -X POST "http://localhost:8080/api/openapi-doc/generate-from-url?url=https://petstore.swagger.io/v2/swagger.json" -H "accept: application/octet-stream" -o openapi-doc.docx
```

### 从JSON字符串生成文档

```
POST /api/openapi-doc/generate-from-json
```

参数:
- 请求体 (必填): OpenAPI规范的JSON字符串
- `templateName` (可选): 要使用的模板名称
- `template` (可选): 自定义模板文件上传

### 从文件生成文档

```
POST /api/openapi-doc/generate-from-file
```

参数:
- `file` (必填): OpenAPI规范的JSON文件
- `templateName` (可选): 要使用的模板名称
- `template` (可选): 自定义模板文件上传

## 主要数据结构

本项目将OpenAPI规范转换为专用的数据结构，用于生成文档。主要数据结构如下:

```
{
  "info": { ... },           // API基本信息
  "resources": [ ... ],      // API资源/接口列表，按标签分组
  "definitions": [ ... ]     // 数据模型定义
}
```

### API基本信息 (info)

```json
{
  "title": "API标题",
  "description": "API详细描述",
  "version": "API版本号",
  "contact": {
    "email": "联系人邮箱"
  },
  "license": {
    "name": "许可证名称"
  }
}
```

### API资源 (resources)

```json
[
  {
    "name": "资源名称(标签名)",
    "description": "资源描述",
    "endpoints": [
      {
        "summary": "接口摘要",
        "description": "接口详细描述",
        "httpMethod": "HTTP方法(GET/POST等)",
        "url": "接口路径",
        "produces": ["响应内容类型"],
        "consumes": ["请求内容类型"],
        "parameters": ["参数列表"],
        "responses": ["响应列表"]
      }
    ]
  }
]
```

### 数据模型 (definitions)

```json
[
  {
    "name": "模型名称",
    "properties": [
      {
        "name": "属性名称",
        "description": "属性描述",
        "required": "是否必须",
        "schema": ["属性数据类型"]
      }
    ],
    "definitionCode": "模型示例JSON代码(带语法高亮)"
  }
]
```

更详细的数据结构说明请参考 [OpenAPI解析器数据规则说明](OpenAPI_PARSER_README.md)。

## 模板定制

系统支持自定义Word文档模板。默认模板位于 `src/main/resources/swagger/default-swagger-template.docx`。
您可以创建自己的模板并使用 `templateName` 参数指定使用。

## 开发

### 项目结构

```
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/tools/
│   │   │       ├── controller/      # API控制器
│   │   │       ├── highight/        # 代码高亮支持
│   │   │       ├── services/        # 业务逻辑
│   │   │       └── Main.java        # 应用入口
│   │   └── resources/
│   │       └── swagger/             # 默认模板和OpenAPI示例
│   └── test/                        # 测试代码
```

### 添加新功能

1. 克隆项目
2. 创建新的功能分支
3. 提交你的变更
4. 推送到远程分支
5. 创建Pull Request

## 技术栈

- Spring Boot 2.2.6
- Swagger/SpringFox 2.9.2 (API文档)
- poi-tl 1.12.2 (Word文档生成)
- codehighlight 1.0.3 (代码高亮)
- JUnit 5 (单元测试)

## 许可证

本项目使用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情 