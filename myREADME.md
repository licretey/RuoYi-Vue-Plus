[TOC]

# ES_OMS

## 基础架构

+ todo
  
  ## 数据库
+ es迁移
+ 添加tenant_id、create_dept
  
  ```sql
  alter table public.es_tms_client
    add tenant_id varchar(20);
  ```

comment on column public.es_tms_client.tenant_id is '租户id';

alter table public.es_tms_client
    add create_dept bigint;

comment on column public.es_tms_client.create_dept is '创建部门';

```
+ 修改create_by, create_time

```sql
alter table public.es_tms_order_base_change_log
    rename column insert_user_id to create_by;

alter table public.es_tms_order_base_change_log
    rename column update_user_id to update_by;

alter table public.es_tms_order_base_change_log
    rename column insert_timestamp to create_time;


alter table public.es_tms_order_base_change_log
    rename column update_timestamp to update_time;
```

## 部署

+ 前端打包，上传服务器后解压缩到nginx配置文件指定的目录下（不指定默认是nginx的html目录下）
+ npm run build:prod

![image-20241017153354002](script/assets/image-20241017153354002.png)

+ 配置nginx.conf文件信息

+ 后端打jar包，后上传服务器(当前放在`~/project`下)

+ 执行`nohup java -jar ruoyi-admin.jar &`

+ 后端端口和认证端口合一为8080（可以手动修如8088，前端则必须跟着调整），cloud同理

+ 前段项目启动有单独端口（生产默认80），通过vite代理将80端口的请求转发到后端端口（如8080）；

+ **注意，根据环境的不同，请求地址会拼接上`dev-api`或`prod-api`（后端会通过ningx配置转为正常地址）**

[TOC]

## 一 结构配置与启动

### 1.1 结构

+ admin
    +
+ common
+ extend
+ modules

> pom中的dependencyManagement依赖声明，只是进行了声明，没有真正引入依赖

+ common通过父pom被进行全局注册管理（只是声明），同时有common下的bom来管理自定义模块（如common-core管理了spring)
+ module下的某个模块需要使用common功能时，在pom中直接引入即可（会从父pom中查询后引入）
+ admin则会引入modules中的模块，启动时其中的module会被加载或打包；（module同样被父pom管理；且module会引入关键的common-core）

#### (1) 通用模块的添加

+ common模块是插件化的，需要配置SPI注入Spring
+ 添加流程如下：
  + 新建module，选择jdk有包路径后创建
  + **core-pom中需要有；同时common-bom下的pom中需要引入**
  + 创建出来的module中有description时才会有被插件提示
  + 可以删除idea生成的jdk版本，否则不会被统一管理jdk
  + 可以直接引用同级的core包，但注意不要相互引用

+ 使用：
  + 在ruoyi-modules中任意的模块pom中引入即可


#### (2) 应用下子业务模块的添加

+ common-core是业务功能的基础，一般正常创建后将其引入即可；除非该子业务无需spring的支持
+ 将module交给父pom即ruoyi-module的pom管理、
+ **同时需要在主pom中声明子模块的版本，然后再ruoyi-admin下引入这个子模块（不再需要声明版本）**
+ **注意包的扫描路径问题，默认是org.dromara**

#### (3) 应用模块的添加（能打jar包）

+ 同样需要引入common-core，因为需要启动springBoot

+ 其次要引入common-core-web，其中包含了spring-web和web服务器

+ 需要单独分配端口，有单独的配置文件

+ **包名可以随意，不用以org.dromara开始**

+ 被extend-pom管理

+ 关键需要配置打jar包插件

  ```xml
  <build>
    <finalName>${project.artifactId}</finalName>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <version>${spring-boot.version}</version>
            <configuration>
            </configuration>
            <executions>
                <execution>
                    <goals>
                        <goal>repackage</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
  </build>
  ```

### 1.2 配置

+ 父pom配置了激活的yml文件，启动时会自动加载；

+ 单子模块中的yml文件需要手动加载，本项目通过@PropertySource注解进行加载的（项目中抽离是将不建议修改的部分进行了抽离，可配置的提前放到了application.yml中）

+ yml文件的前缀不能直接跳转，需要点击后缀跳转

```yaml
--- # 监控中心配置
spring.boot.admin.client:
    # ...
    metadata:
        # 读取主pom中的变量、或application.yml中的变量
        username: ${spring.boot.admin.client.username}
        userpassword: ${spring.boot.admin.client.password}
    # 读取pom文件中profile属性下的变量
    username: @monitor.username@
    password: @monitor.password@
```

## 二 后端

### 2.1 线程池

### 2.2 undertow与servletUtils

### 2.3 Utils

+ SpringUtils

+ ReflectUtils

+ JsonUtils

+ TreeUtils

+ StringUtils

+ RegionUtils

+ RegexUtils
  
  ### 2.4 Validator
  
  ### 2.5 mapstruct-plus
  
  ### 2.6 mybatis-plus

+ 元数据填充（监听增删查改自动添加数据）：InjectMetaObjectHandler

MybatisPlusConfig中配置多个插件

+ TenantLineInnerInterceptor 多租户插件
+ PlusDataPermissionInterceptor 数据权限插件
+ PaginationInnerInterceptor 分页插件
+ OptimisticLockerInnerInterceptor 乐观锁插件
  一般是在更新前后台手动查到version后去更新
+ InjectionMetaObjectHandler 元数据自动审计（自动填充）

BaseMapperPlus接口：增强了泛型参数，mybatis-plus的BaseMapper接口只支持BO，框架增强后使其支持由查询的BO查询后返回对应的VO

### 2.7 redis

+ 支持集群部署，需要配置多个redis节点

+ sential等其它方式需要设置redission中的配置

+ 对于基础类型，redis序列化时会自动转换，如100L这样的Long类型会被自动转换为Integer类型的100，**所以可能会在反序列化时失败**（可以自定义一个对象，包装一下这个类型后，使用自定义对象去序列化）

+ 缓存组名称常量 key 格式为 cacheNames#ttl#maxIdleTime#maxSize
  + ttl 过期时间 如果设置为0则不过期 默认为0
  + maxIdleTime 最大空闲时间 根据LRU算法清理空闲数据 如果设置为0则不检测 默认为0
  + maxSize 组最大长度 根据LRU算法清理溢出数据 如果设置为0则无限长 默认为0
  + 例子: test#60s、test#0#60s、test#0#1m#1000、test#1h#0#500  

+ 布隆过滤器：` RedisUtils.getClient().getBloomFilter("test").contains("id")`
  + 使用前需要使用tryInit初始化一下，传入数据量大小和可接收的错误率；并将需要处理的数据提前加入进去

+ CacheController：redis监控组件

    ### 2.8 多数据源

+ 多数据源可以配置多个数据库

### 2.9 分布式锁Lock4j

+ 管理：common-redis
+ 依赖：lock4j-redission-spring-boot-starter
+ 配置：
    + 默认获取锁最大阻塞等待时间3s；锁最大执行时间30s；
    + 默认执行器的顺序为redission、redisTemplate、zookeeper
    + 默认失败策略

+ 使用：

    + @Lock4j(key)，key支持spel表达式：name+#+key
        + name = 项目前缀+lock4j默认前缀+锁的控制器路径+锁的方法名（默认name前缀生成逻辑）；如果有name属性，会覆盖掉

    + 注入LockTemplate：一个key一个锁（key名称手动传入）

+ 实现：

    + RedissionLockExecutor：
    + key生成实现： LockKeyBuidler-- DefaultLockKeyBuilder --LockInterceptor

## 三 后端2

### 3.1 Filter

### 3.2 Interceptor

### 3.3 WebSocket：

+ **全双工TCP连接**

+ 管理：common-websocket
+ 依赖：lock4j-redission-spring-boot-starter
+ 配置：
    + 默认关闭，改用了sse；开启时所有地址可以访问；路径为/resource/websocket
    + 前端配置：VITE_APP_WEBSOCKET
+ 使用：
    + 地址：ws://localhost:8080/dev-api/resouce/websocket (端口和http一样，加密时使用wss://xxx)
    + 需要配置token：登录后获取Authorization和ClientId，如?Authorization=Bear ac32xad...&clientid=ex32afa...
    + 框架当前的行为配置为：返回接收到的相同信息
    + 案例：WeSocketController
    + **每个后台服务端会维护客户与session对应关系的连接池；当集群部署时如果本服务中找不到，则需要调整使用redis的发布订阅模式来通知其它后台服务，哪个服务中存在就取出session进行消息的推送**
    + **一个客户的多个客户端只有一个有效**
+ 实现：
    + 前端：websocket.ts在layout/index.vue的生命周期中初始化
    + 后端：
        + 配置类WebSocketConfig：配置拦截器到资源路径下，并设置处理器和允许访问的源
        + 握手拦截器PlusWebSocketInterceptor：通过判断3个clientid是否相同校验握手是否成功
        + Websocket处理器PlusWebSocketHandler：对session进一步校验，成功则将session放入会话池
        + 集群订阅器WebSocketTopicListener：所有服务都会监听，消除接收到后从本地会话池中获取接收方有则发送

### 3.4 SSE：

+ **仅服务端向客户端发送，单向；配合http可以实现双向**

+ 管理：common-sse
+ 依赖：spring自带，引入common-core即可
+ 配置：
    + 默认开启；所有地址可以访问；路径为/resource/sse
    + 前端配置：VITE_APP_SSE
+ 使用：
    + 地址：http://localhost:8080/dev-api/resouce/sse(端口和http一样，加密时使用wss://xxx)
    + 后台提供一个http接口接收客户端请求，然后使用sse通知到前端完成全双工；单向只需要调用sse发送即可
    + **一个客户的多个客户端可以同时有效**
+ 实现：
    + 前端：sse.ts在layout/index.vue的生命周期中初始化
    + 后端：
        + 连接管理器SseEmitterManager：
        + 集群订阅器SseTopicListener
        + 连接控制器SseController：发送、关闭等操作；

### 3.5 Sensitive脱敏

+ 管理：common-sensitive
+ 依赖：仅引入common-json
+ 配置：
+ 使用：
    + @Sensitive：rolekey标明排除脱敏的角色（不提供则仅排除管理员）; perms标明排除脱敏的权限；同时存在时需要同时满足才不会脱敏
    + **脱敏排除了管理员和租户管理员；只能用于字符串；是在序列化时进行的脱敏处理**
    + 实现： 
        + SensitiveHandler定义了对角色、权限的脱敏策略；实现了ContextualSerializer只拦截String类型的字段；
        + 主要通过SysSensitiveServiceImpl中的方法isSensitive判断用户是否需要脱敏，为false不脱敏
        + SensitiveStrategy通过引入hutool的工具类进行的字符串的脱敏处理操作

### 3.6 encrypt加解密

```
+ 管理：
+ 依赖：
+ 配置：
+ 使用：
+ 实现：
    + 后端：
    + 前端：
```



+ 管理：common-encrypt

+ 依赖：bcprov-jdk15to18国密支持，hutool-crypt

+ 配置：
    + 数据库加密未开启，默认算法是Base64的方式，不算是加密，可以根据需要调整配置；对称式需要配置密钥，非对称需要同时配置公钥私钥、
    + API加密默认开启，AES密钥会使用Base64编码后使用RSA非对称加密，传输到后端使用RSA解密再解码拿到AES密钥（配置文件中公钥用于响应的加密返回给前端；私钥用于解密前端传输过来的请求数据）

+ 使用：
    + @ApiEncrypt，默认false即不对响应加密，只解密前端加密的请求数据

+ 实现：
    + 前端：
        + API_APP_ENCRYPT设置是否开启加密，需要和后端配置同步
        + 再request.ts中的请求拦截器中，前端如果请求头中设置了加密则生成AES密钥密钥后进行加密处理（AES密钥）,对请求的数据用AES加密后传输
    + 后端：
        + ApiDecryptAutoConfiguration配置了API加密的Filter实现CryptoFilter
        + API请求解密包装类DecryptRequestBodyWrapper用于实际的解密操作
        + API响应返回加密包装类EncryptResponseBodyWrapper用于实际的加密操作
        + EncryptorAutoConfiguration配置了对数据库的加解密操作
        + 加密字段的缓存管理器EncryptorManager
        + 入参加密拦截器MybatisEncryptInterceptor
        + 出参解密拦截器MybatisDecryptInterceptor

    > 》》**数据库加解密**

+ 管理：common-encrypt

+ 依赖：bcprov-jdk15to18国密支持，hutool-crypt

+ 配置：mybatis-encryptor.enable为ture时开启，默认关闭； 加密方式是Base64；

+ 使用：在字符串类型上增加@EncryptField注解即可

+ 实现：
    + 后端：

        + EncryptorAutoConfiguration配置中注入了EncryptorManager，EncryptorManager初始化时它的scanEncryptClasses会扫描加密实体中的加密加密字段放入缓存；

        + IEncryptor为加密执行者，AbstractEncryptor继承自IEncryptor，各种加密算法实现了AbstractEncryptor；

        + EncryptorAutoConfiguration同时注入了MybatisEncryptInterceptor和MybatisDecryptInterceptor，拦截器在拦截到字段时调用EncryptorManager中的加解密方法（scanEncryptClasses的扫描缓存记录中记录了加解密字段和算法）

        + ```
            
            ```

        

### 3.7 Log日志

+ 用于记录所有外部请求在系统中的操作
+ 核心原理是通过SpringEvent完成，有两种方式
  + @Log注解，不能在被@SaIgnore下使用（OperLogEvent）
  + 发布事件（LogininforEvent）
+ **LogBack**

### 3.8 S3对象存储

+ 读取OssProperties配置，使用OssFactory工厂类获取对应的OssClient对象（会存储部分信息到Redis中，OssConstant），用于桶的创建销毁与文件的上传等操作（返回UploadResult）
+ Oss初始化：在SpringBoot启动后，通过继承ApplicationRunner接口，异步的在run方法中执行OssFactory的init方法，初始化所有配置的OssClient对象
+ 配置
  + 域名不需要提供http或https，有域名优先使用域名否则使用云厂商提供的站点

### 3.9 Execel

+ 管理：common-excel

+ 依赖：easyexcel、common-json

+ 配置：

+ 使用：

    + @ExcelProperty：value指定表头，order指定字段在表中的顺序，而index指定列高于order（从0开始） ，convert指定转换器
    + @ExcelIngore、@ExcelIgnoreUnannotated：忽略要导出的字段，可以直接使用@ExcelIngore，也可使用@ExcelIgnoreUnannotated后不使用@ExcelProperty
    + @DatetimeFormat：使用指定格式解析exel中string类型的日期
    + @NumberFormat：使用指定格式解析excel中的数字


+ 实现：
    + 后端：
        + 字典格式化器ExcelDictFormat--ExcelDictConvert，枚举格式化器ExcelEnumFormat--ExcelEnumConvert
        + 单元格合并器CellMergeStrategy
        + 大数值转字符串ExcelBigNumberConvert：防止数字失真；工具类中注册进去就会生效
        + 下拉框选择器DropDownOptions：字典、枚举、数据库数据的下拉选择（实际的实现ExcelDownHandler）
        + 导入的结果对象实现DefaultExcelResult；导入时对数据的监听器DefaultExcelListener

```

```