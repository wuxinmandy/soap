# SOAPGateway

这是一个基于 Spring Boot 的 SOAP 网关项目，适用于 **没有 WSDL 文件** 的场景，并实现以下能力：
- 接收 SOAP 请求（`/ws`）
- 使用 DOM 解析 XML
- 立即回发固定 ACK SOAP 响应
- 异步转发请求到下游 business service
- 接收 business service 请求并同步转发到 external server
- 将消息和处理状态持久化到数据库

## 1. 运行环境

- JDK 21+
- Gradle 8.x（或使用 IDE 内置 Gradle）

## 2. 启动项目

```bash
gradle bootRun
```

## 3. 接收 SOAP 请求

SOAP 入口由 Spring-WS 提供，路径为：

`http://localhost:8080/ws`

Endpoint 监听的 payload root：
- namespace: `http://example.com/soap`
- localPart: `processRequest`

示例 SOAP Request（发送到 `http://localhost:8080/ws`）：

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

系统会**立即返回 ACK SOAP Response**（示例）：

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

## 4. 下游转发配置

在 `application.yml` 中配置下游地址：

```yaml
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

收到上游请求后，系统会在 ACK 返回后异步调用下游服务。
收到 business service 的中继请求后，系统会同步转发到 external server，并将 external 返回值直接返回给调用方。

## 5. 数据库存储

默认使用 H2 文件库：

```yaml
spring:
  datasource:
    url: jdbc:h2:file:./data/soapgatewaydb
```

表：`soap_message_log`

主要字段：
- `tracking_id`：每条消息唯一追踪号
- `request_xml`：原始入站 SOAP payload（由 `gateway.storage.persist-request-xml` 控制是否保存）
- `downstream_response_xml`：下游返回
- `status`：`RECEIVED` / `FORWARDED` / `FAILED`
- `error_message`：失败原因

## 6. 关键实现说明

- `SoapDomEndpoint`：负责接收、DOM 解析、立即回 ACK。
- `BusinessRelayEndpoint`：接收 business service 请求并转发 external。
- `InboundSoapProcessingService`：异步入库并转发下游。
- `BusinessRelayService`：同步转发 external 并记录结果。
- `SoapClientService`：调用下游 SOAP 服务。
- `XmlDomParser`：`Source`/`String` 与 DOM 互转、节点解析。
