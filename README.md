# DialogQueue

## 使用方式

## 实现分析

一个Dialog队列需要具备的能力和特性：

1. 防重复
2. 优先级
3. 出队列的时候才构建
4. 不占用dialog原本的showListener和dismissListener
5. 生命周期感知，只有在onResume时队列才工作, 在onDestroy中隐藏dialog（如果有）。

### 特性1-3分析

需要一个优先队列，tag作为key，元素类型是 `()->Dialog`，用tag做防重复，用匿名函数构建dialog。

### 特性4分析 

DialogQueue 脱离不开dismissListener的支持，但也不能在DialogQueue中设置dismissListener，否则会覆盖原有
的Listener， 所以只能提供一个next函数给构建函数。由外部主动调用next函数显示下一个Dialog 。

所以DialogQueue元素类型要变为：`(next: ()->Unit)->Dialog`

### 特性5分析

传入lifecycle对象