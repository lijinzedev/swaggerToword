# OpenAPI 解析器数据规则说明

本文档描述了`OpenApiParserService`解析OpenAPI规范文件生成数据模型的规则。该服务将OpenAPI JSON转换为可用于文档生成的数据结构。

## 主要数据结构

`OpenApiParserService.buildDataModel()`方法解析OpenAPI JSON并生成以下主要数据结构：

```
{
  "info": { ... },           // API基本信息
  "resources": [ ... ],      // API资源/接口列表
  "definitions": [ ... ]     // 数据模型定义
}
```

## 1. API信息 (info)

API基本信息对应OpenAPI规范中的`info`对象。

| 数据字段 | 说明 | 对应OpenAPI字段 |
|---------|------|----------------|
| title | API标题 | info.title |
| description | API描述 | info.description |
| version | API版本 | info.version |
| contact.email | 联系人邮箱 | info.contact.email |
| license.name | 许可证名称 | info.license.name |

## 2. API资源列表 (resources)

API资源列表对应OpenAPI规范中的`paths`对象，按标签(tags)分组。

每个资源(`resource`)包含：

| 数据字段 | 说明 | 对应OpenAPI字段 |
|---------|------|----------------|
| name | 资源名称 | operation.tags[0] |
| description | 资源描述 | - |
| endpoints | 资源下的接口列表 | - |

### 2.1 接口信息 (endpoints)

每个接口(`endpoint`)包含：

| 数据字段 | 说明 | 对应OpenAPI字段 |
|---------|------|----------------|
| summary | 接口摘要 | operation.summary |
| description | 接口详细描述 | operation.description |
| httpMethod | HTTP方法(GET/POST等) | HTTP方法名(大写) |
| url | 接口路径 | 路径URL |
| produces | 响应内容类型列表 | responses.*.content的键名(如application/json) |
| consumes | 请求内容类型列表 | requestBody.content的键名(如application/json) |
| parameters | 接口参数列表 | parameters和requestBody合并 |
| responses | 接口响应列表 | responses |

### 2.2 参数信息 (parameters)

每个参数包含：

| 数据字段 | 说明 | 对应OpenAPI字段 |
|---------|------|----------------|
| in | 参数位置(path/query/body等) | parameter.in |
| name | 参数名称 | parameter.name |
| description | 参数描述 | parameter.description |
| required | 是否必须 | parameter.required |
| schema | 参数数据类型(包括引用类型) | parameter.schema |

> 注意：requestBody会被转换为in=body的参数

### 2.3 响应信息 (responses)

每个响应包含：

| 数据字段 | 说明 | 对应OpenAPI字段 |
|---------|------|----------------|
| code | HTTP状态码 | responses的键名(如200/400) |
| description | 响应描述 | response.description |
| headers | 响应头列表 | response.headers |
| schema | 响应数据类型 | response.content.*.schema |

## 3. 数据模型定义 (definitions)

数据模型定义对应OpenAPI规范中的`components.schemas`对象。

每个数据模型包含：

| 数据字段 | 说明 | 对应OpenAPI字段 |
|---------|------|----------------|
| name | 模型名称(BookmarkTextRenderData类型) | 模型的键名 |
| properties | 模型属性列表 | schema.properties |
| definitionCode | 示例JSON代码(HighlightRenderData类型) | 基于schema生成的示例 |

### 3.1 模型属性 (properties)

每个属性包含：

| 数据字段 | 说明 | 对应OpenAPI字段 |
|---------|------|----------------|
| name | 属性名称 | property的键名 |
| description | 属性描述 | property.description |
| required | 是否必须 | schema.required中是否包含该属性名 |
| schema | 属性数据类型(TextRenderData列表) | property类型定义 |

## 数据类型解析规则

数据类型(`schema`)按以下规则解析：

1. 引用类型(`$ref`)：解析为指向对应模型的超链接
2. 数组类型(`array`)：表示为`<元素类型>array`
3. 映射类型(`object`+`additionalProperties`)：表示为`map[string, 值类型]`
4. 基本类型：直接使用类型名称(string/number/boolean等)

## 示例JSON生成规则

示例JSON生成遵循以下规则：

1. 优先使用schema中定义的`example`值
2. 根据`format`生成适当的示例值(如日期、邮箱、UUID等)
3. 基本类型使用默认值:
   - string: "example"
   - integer/number: 0
   - boolean: false
   - array: 包含一个元素的数组
   - object: 包含所有属性的对象
4. 处理嵌套引用，避免循环引用

## 注意事项

1. 解析器同时支持OpenAPI 3.0和Swagger 2.0格式
2. 使用特殊渲染数据类型以支持文档生成:
   - BookmarkTextRenderData: 用于模型名称，支持书签导航
   - HyperlinkTextRenderData: 用于引用类型，支持超链接
   - HighlightRenderData: 用于示例代码，支持语法高亮
3. 模型之间的引用使用"anchor:"语法实现文档内部跳转 