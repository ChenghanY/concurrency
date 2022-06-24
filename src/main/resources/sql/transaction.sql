-- 查全局事务参数
SELECT  @@GLOBAL.transaction_isolation, @@GLOBAL.transaction_read_only, @@GLOBAL.max_connections, @@GLOBAL.innodb_print_all_deadlocks;

-- 设置连接数，保证mysql不因为容量满了而拒绝测试用例的连接
SET @@GLOBAL.max_connections = 50000;

-- 打印死锁
SET GLOBAL innodb_print_all_deadlocks = 1;

-- 运行时查看锁状态用
SHOW ENGINE INNODB STATUS;