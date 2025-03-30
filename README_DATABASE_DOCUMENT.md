# Database Documentation Generator

本工具用于从数据库元数据生成Word格式的数据库文档。

## 功能特性

1. 支持从JSON元数据文件生成数据库文档
2. 支持直接连接数据库提取元数据并生成文档
3. 使用POI-TL模板引擎实现灵活的文档定制
4. 支持表、列、索引等数据库对象的文档化

## 使用方法

### 1. API接口

#### 从JSON文件生成文档

```
POST /api/database/document/upload
Content-Type: multipart/form-data
file: [上传JSON元数据文件]
```

#### 从数据库连接生成文档

```
POST /api/database/document/generate
Content-Type: application/json

{
  "databaseType": "postgresql",
  "host": "localhost",
  "port": 5432,
  "databaseName": "example_db",
  "username": "postgres",
  "password": "password",
  "schema": "public"
}
```

### 2. JSON元数据格式示例

```json
{
  "databaseName": "ecommerce_db",
  "databaseType": "PostgreSQL",
  "databaseVersion": "14.5",
  "tables": [
    {
      "tableName": "users",
      "tableComment": "用户账户信息表",
      "schema": "public",
      "primaryKeys": ["user_id"],
      "logicalKeys": ["email"],
      "tableSpace": "pg_default",
      "indexes": [...],
      "columns": [...]
    }
  ]
}
```

完整的JSON元数据示例可参考 `sample_postgresql_metadata.json` 文件。

### 3. Word模板格式

本系统使用 [POI-TL](https://github.com/Sayi/poi-tl) 模板引擎，模板中的占位符示例如下：

```
1.1.1基础信息及系统管理模块

{{?resources}}
{{no}}、{{table_comment}} {{table_name}}
中文名称	{{table_comment}}
物理表名	{{table_name}}
主键	{{primary_keys}}
逻辑主键 	{{logical_keys}}
所属表空间	{{table_space}}
索引	
{{detail_table}}字段列表
序号	中文名称	列名	数据类型	长度	主键	非空	外键
							

{{/resources}}
```

## 开发指南

### 1. 模型类

数据库元数据通过以下模型类表示：

- `DatabaseMetadata`: 整个数据库的元数据
- `TableMetadata`: 表的元数据
- `ColumnMetadata`: 列的元数据
- `IndexMetadata`: 索引的元数据

### 2. 解析JSON元数据

使用 `DatabaseMetadataParser` 类将JSON解析为模型对象：

```java
DatabaseMetadataParser parser = new DatabaseMetadataParser();
DatabaseMetadata metadata = parser.parseFromFile(new File("metadata.json"));
```

### 3. 生成文档

使用 `PoiTlDatabaseDocumentService` 生成Word文档：

```java
PoiTlDatabaseDocumentService service = new PoiTlDatabaseDocumentService();
service.generateDocument(metadata, "templates/database_template.docx", "output/database_doc.docx");
```

## 自定义模板

1. 创建Word文档作为模板
2. 使用POI-TL语法添加占位符，详细请参考 [POI-TL文档](https://github.com/Sayi/poi-tl)
3. 将模板放在 `templates` 目录下
4. 在生成文档时指定模板路径

## 示例程序

参考 `DatabaseTemplateDemo` 类了解完整的使用流程。

---

## 项目结构

- `src/main/java/com/tools/model/database/`: 数据库元数据的实体类
- `src/main/java/com/tools/services/document/`: 文档生成服务
- `src/main/java/com/tools/controller/`: RESTful API控制器
- `src/main/java/com/tools/examples/`: 示例程序

---

## 系统要求

- Java 8+
- Spring Boot 2.5+
- Apache POI 5.0+
- POI-TL 1.10+ 