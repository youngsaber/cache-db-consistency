# 尝试使用锁保证缓存数据库一致性

## 环境

win10环境  单机测试（应用，mysql，redis）

intellij编译环境 + jdk8 + mysql8 + redis3.2

## 思路

更新采用写锁，更新包含**更新缓存**和**更新数据库**两步，同时更新完毕，才会释放锁

查询采用读锁，如果缓存不存在，数据库存在，需要更新缓存

## 更多思考

​	缓存，数据库较高的一致性需求大都存在于类似于秒杀系统之中，秒杀系统写入需求频繁，需要相对重级的悲观锁，cas多个线程不停自旋不太适合这种场景。但是秒杀系统更多可能依赖于上层限流，性能扩展，比如

* 实际有效请求的拦截，其余丢弃（比如按前多少请求数量）
* 使用专门的高性能机器等，cache需要大量内存，sql写入需要频繁io，秒杀系统获取可以直接依赖于缓存，后续入库可以通过日志或者cache持久化快照等
