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

  #### (2) 应用下子业务模块的添加
+ common-core是业务功能的基础，一般正常创建后将其引入即可；除非该子业务无需spring的支持
+ 将module交给父pom管理

  #### (3) 应用模块的添加（能打jar包）
+ 同样需要引入common-core，因为需要启动spring
+ 其次要引入common-core-web，其中包含了web服务器
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
### 2.8 多数据源
+ 多数据源可以配置多个数据库


## 三 后端
### 3.7 Log日志
+ 用于记录所有外部请求在系统中的操作
+ 核心原理是通过SpringEvent完成，有两种方式
  + @Log注解，不能在被@SaIgnore下使用（OperLogEvent）
  + 发布事件（LogininforEvent）
### 3.8 S3对象存储
+ 读取OssProperties配置，使用OssFactory工厂类获取对应的OssClient对象（会存储部分信息到Redis中，OssConstant），用于桶的创建销毁与文件的上传等操作（返回UploadResult）
+ Oss初始化：在SpringBoot启动后，通过继承ApplicationRunner接口，异步的在run方法中执行OssFactory的init方法，初始化所有配置的OssClient对象
+ 配置
  + 域名不需要提供http或https，有域名优先使用域名否则使用云厂商提供的站点