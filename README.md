# SOAPGateway

`SOAPGateway` 是一个基于 Spring Boot + Spring-WS 的 SOAP 网关项目，面向 **无 WSDL** 场景，使用 DOM 解析 XML，并提供两条转发链路：

- **链路 A（上游 -> 网关 -> 下游 business）**：立即 ACK，异步转发
- **链路 B（business -> 网关 -> external）**：同步转发并透传返回
- 消息处理日志持久化到数据库（可配置是否保存原始请求 XML）

## 1. 环境要求

- JDK 21（必须）
- 已配置 `JAVA_HOME` 并可在终端执行 `java -version`
- 使用项目内 Gradle Wrapper（已内置）

## 2. 快速启动

推荐使用 Wrapper（Windows）：

```bash
.\gradlew.bat bootRun
```

Linux/macOS：

```bash
./gradlew bootRun
```

常用命令：

```bash
.\gradlew.bat clean build
.\gradlew.bat test
```

## 3. 默认配置（`application.yml`）

```yaml
server:
  port: 8080

gateway:
  storage:
    persist-request-xml: true
  downstream:
    endpoint-url: http://localhost:8081/ws/business
    soap-action:
  external:
    endpoint-url: http://localhost:8082/ws/external
    soap-action:
```

说明：

- `gateway.storage.persist-request-xml`：
  - `true`：保存 `request_xml`
  - `false`：不保存原始 XML（字段为 `null`）
- `gateway.downstream.*`：链路 A 的目标服务（异步）
- `gateway.external.*`：链路 B 的目标服务（同步）

## 4. SOAP 入口与用法

统一 SOAP 地址：

- `http://localhost:8080/ws`

命名空间：

- `http://example.com/soap`

### 4.1 链路 A：`processRequest`（立即 ACK + 异步下游转发）

匹配条件：

- localPart: `processRequest`

请求示例：

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns="http://example.com/soap">
  <soapenv:Header/>
  <soapenv:Body>
    <ns:processRequest>
      <ns:customerId>C10086</ns:customerId>
      <ns:action>QUERY</ns:action>
    </ns:processRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

ACK 响应示例：

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/">
  <soapenv:Body>
    <ns:processResponse xmlns:ns="http://example.com/soap">
      <ns:status>ACCEPTED</ns:status>
      <ns:message>Request received and forwarded asynchronously.</ns:message>
      <ns:trackingId>...</ns:trackingId>
      <ns:customerId>C10086</ns:customerId>
      <ns:action>QUERY</ns:action>
    </ns:processResponse>
  </soapenv:Body>
</soapenv:Envelope>
```

处理行为：

1. 网关解析请求并立即返回 ACK  
2. 后台异步调用 `gateway.downstream.endpoint-url`  
3. 日志表更新为 `FORWARDED` 或 `FAILED`

### 4.2 链路 B：`businessRelayRequest`（同步 external 转发）

匹配条件：

- localPart: `businessRelayRequest`

请求示例：

```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns="http://example.com/soap">
  <soapenv:Header/>
  <soapenv:Body>
    <ns:businessRelayRequest>
      <ns:customerId>B20001</ns:customerId>
      <ns:action>SUBMIT</ns:action>
    </ns:businessRelayRequest>
  </soapenv:Body>
</soapenv:Envelope>
```

处理行为：

1. 网关接收并解析请求  
2. 同步调用 `gateway.external.endpoint-url`  
3. 将 external 返回的 payload 直接作为 SOAP 响应返回  
4. 日志表更新状态

## 5. 数据存储

默认数据库：H2 文件库

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/soapgatewaydb
```

核心表：`soap_message_log`

核心字段：

- `tracking_id`：消息追踪号
- `customer_id` / `action`：业务字段
- `request_xml`：原始请求（受 `persist-request-xml` 控制）
- `downstream_response_xml`：下游或 external 响应
- `status`：`RECEIVED` / `FORWARDED` / `FAILED`
- `error_message`：失败信息
- `created_at` / `processed_at`：处理时间

## 6. 当前代码结构（关键类）

- `SoapDomEndpoint`：`processRequest` 入口，ACK + 异步派发
- `InboundSoapProcessingService`：异步下游转发 + 落库
- `BusinessRelayEndpoint`：`businessRelayRequest` 入口
- `BusinessRelayService`：同步 external 转发 + 落库
- `SoapClientService`：通用 SOAP 调用客户端
- `XmlDomParser`：DOM 解析与 Source/String 转换
- `SoapMessageLog` / `SoapMessageLogRepository`：日志实体与仓储
