# 插件目录
这里是存放bot插件的目录。
## 插件特性
* 支持热加载，但仅支持处理相应消息事件。
* _不完善的功能_，存在未知问题。

## 插件要求
* `.jar`格式
* 存在一个类实现`com.oliambot.inf.Plugin`接口并实现其中的两个方法。
  * `getName()`方法为插件加载时调用，返回插件名并作为此插件的唯一识别码。
  * `drop()`方法为插件卸载时调用的方法。
* 存在类实现`com.oliambot.inf.MessageCatcher`接口，其中
  * 有`com.oliambot.inf.Catch`注解的方法，`Catch`注解说明：
    * `entry`指出触发此方法的消息内容所需匹配的正则表达式（bot使用`String.matches(regex)`方法判断），同一个插件中不可重复；
    * `listen`指出在什么情景触发，有私聊，群聊（默认），两者三种选项；
    * `permission`指出群聊时所需权限，默认所有人可用。
  * 被`Catch`注解的方法要求：
    * 静态方法，最好以`public static void`声明
    * 两个参数，第一个为`net.mamoe.mirai.contact.User`的子类，第二个为`net.mamoe.mirai.message.data.Message`的子类。
  * 例子
  ```java
  class MyCatcher implements MessageCatcher {
      @Catch(entry = "^hello")
      public static void hello(Member sender, MessageChain chain) {
          System.out.println(chain.contentToString());
          sender.getGroup().sendMessage("你好！");
      }
  }
  ```
  加载上例后，bot将捕获“hello”消息并将消息内容打印到控制台上，再向接收到此消息的群发送“你好！”。
      

