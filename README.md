# MTQuizApp

中山大学军事理论课刷题软件重置版

本软件是对一个古老的中山大学军理考试刷题软件的重置版. 原作者已不可考, 据说是信科院(今计院)的学长.


原软件只流传有一个exe文件, 在命令行运行, 但是其采用GB2313编码, 与现在设备默认的UTF-8不符,
可能导致乱码. 所幸软件的题库仍有文本文件留存, 本人编写解析脚本将题目数据存入SQLite数据库后,
用Java重新编写了一个UI界面, 希望能帮到学弟学妹们.

## 直接使用软件

在`target/dist`中找到`MTQuizApp-1.0.0.exe`安装包, 运行即可安装并在桌面建立快捷方式.

## 面向开发者

### 参考环境:
- Git
- JDK 24
- Apache Maven 3.6.1
- IntelliJ IDEA

### 如何运行(IDEA):
1. `git clone`到本地, 打开IntelliJ IDEA, 点击左上角的`File -> Open`
并选中项目的`pom.xml`文件, IDEA会自动解析依赖并下载.
2. 在项目根目录(即`pom.xml`的父级)中运行
```bash
mvn javafx:run
```
3. 软件会被安装到`C:\Program Files\MTQuizAll`处, 数据库会被放到你的个人目录下
(例如你的用户名为`gugugaga`, 则数据库放在`C:\user\gugugaga`.)

### 如何打包(IDEA):
- 第一步同上
- 下载WiX Toolset
- 在项目根目录(即`pom.xml`的父级)中运行
```bash
mvn package
```