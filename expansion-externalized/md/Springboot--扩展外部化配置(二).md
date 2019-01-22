# Springboot--扩展外部化配置(二)

   笔记是学习了小马哥在慕课网的课程的《Spring Boot 2.0深度实践之核心技术篇》的内容结合自己的需要和理解做的笔记。

## 前言

  在这看这篇笔记之前，个人建议去看一下上一篇，因为这里很多用到的扩展方法都是基于`Environment`生命周期来的。所以如果有迷惑的地方可以先了解一下`Environment`生命周期，再回来思考一下,就能有一定的理解了。

## 扩展外部化配置属性源

### 配置方式概览

- 基于 `SpringApplicationRunListener#environmentPrepared`
- 基于 `ApplicationEnvironmentPreparedEvent` 扩展外部化配置属性源
- 基于 `EnvironmentPostProcessor` 扩展外部化配置属性源
- 基于 `ApplicationContextInitializer` 扩展外部化配置属性源
- 基于 `SpringApplicationRunListener#contextPrepared` 扩展外部化配置属性源
- 基于 `SpringApplicationRunListener#contextLoaded` 扩展外部化配置属性源
- 基于 `ApplicationPreparedEvent` 扩展外部化配置属性源



下面我们就来逐一介绍。



### 基于 `SpringApplicationRunListener#environmentPrepared`

在之前我们介绍也自己写过 `SpringApplicationRunListener`相关的demo 这里就不做重复，我们看到主要就是使用`environmentPrepared` 方法来进行外部化的资源配置。



