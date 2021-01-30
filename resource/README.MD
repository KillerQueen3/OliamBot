# resource目录
## _pixivapi目录_
* 基于 [pixivpy](https://github.com/upbit/pixivpy) 和 [flask](https://github.com/pallets/flask) 的pixiv搜索api，本bot**必须**使用此api搜索数据
* 启动方式
  * 安装依赖 `pip install -r requirements.txt`
  * 在myAPI.py中配置好端口号（和settings.properties中的一致）
  * `python myAPI.py`
## search目录
* 存放搜索缓存，以json格式存放。
* 文件“群号-x.json”存放对应群的搜索记录，x由标签哈希值决定。
* **此文件夹不要删除**，但删除其中文件不影响运行。
## h.png
* 设置不发送r18时的替代图片
## message.json
* 存放相关消息文本。
* 修改文本后**须**使用“=reload”命令或重启bot更新其中文本至bot。
## pcrChara.json和trans.json
* 存放翻译信息，其中公主连接角色优先级更高。
* 修改后**须**使用“=读取翻译”命令或重启bot更新翻译至bot。
## history.json
* 存放特定指令的历史记录。
## fail.txt
* 存放搜索失败的标签以供补充翻译之用。