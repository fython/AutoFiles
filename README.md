AutoFiles
----

**个人试验，仍在施工中，不建议实际使用**

## 设计初衷

Android Q 推进了应用对 Storage Access Framework 的依赖和使用需求，不得不重视 SAF 下对文件访问的接口使用。

Google 为 SAF 设计了 `DocumentFile` 工具库，它能够处理传统 Java File 接口和 SAF 接口的文件操作，
但实际使用起来感觉有少处难用的地方，例如：

1. `createFile` 和 `createDirectory` 执行时，在实际已存在重复文件时会采取另建新的文件方式，这个逻辑很好理解，
   但每次都需要先检查一次是否已存在再进行复写，比较麻烦。
   
2. 想要一步到位获得某个较深路径下的文件或文件夹？还得结合 `findFile` 和 `createDirectory` 一层层到达目的文件。

3. 缺少一些 `copy/rename` 之类的操作（算不上痛点）

我参照 Java File / Android DocumentFile 的接口创建了 `IAutoFile<T extends IAutoFile>`，并实现了 
`JavaApiFile` 和 `SAFApiFile` 两个类，到这一步所做的工作和 `DocumentFile` 工具库还是一样，但上面的三个点
都已经解决了。

但是想做的不仅仅是 “修复” 而是想对过去 Android 对外部储存的文件访问方式进行改变。我们都知道 Android 6.0 之后
通过 Java File 接口访问 External Storage 需要动态申请存储权限，而 SAF 中选择文件/文件夹需要用户在文档界面中
手动选择位置，Scoped Directory Access 则类似于前面存储权限的请求交互，要去一一适配这些请求流程需要花费点点时间，
当中的坑也在 Android 官方文档没有被提及（比如各种很坑的国产系统会把 Documents UI 给去掉，会造成 SAF 请求崩溃）。
尽管这些接口也已经不是什么新鲜事物了，但还是要考虑向下兼容，要同时处理两种文件访问方式（SDA 算作 SAF 方式）需要一种
优雅的方式，结合前面所定义的 `IAutoFile<T>` 设计了 `IAutoFileProvider<IAutoFile>` 作为文件对象的工厂/提供者，
同时持有不同来源的根路径和其它基本属性，不论是传统的 Java 文件接口还是 SAF 接口，都可以通过统一的 `requestAttach`
方法去准备好访问权限和初始化，不必再关注文件访问的实现。

## 进度

- [x] 基本功能
- [ ] 接口文档
- [ ] 部分接口只支持 File 的第三方库、图片加载库的兼容
- [ ] Content Provider（类似 Google 的 FileProvider 库）
- [ ] Native 接口（？）
