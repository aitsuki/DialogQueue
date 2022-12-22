# DialogQueue

blog：https://aitsuki.com/blog/android-dialog-queue/

## 特性

1. 防止dialog重复显示
2. DialogQueue是一个优先队列，可以指定dialog显示的优先级
3. 懒加载，在需要显示时才构建dialog
4. 不占用dialog原本的dismissListener（但需要你手动添加dismissListener）
5. 生命周期感知，在onResume时队列才工作, 并且在onDestroy中隐藏dialog（如果有）。

> 不支持DialogFragment，实际上我觉得DialogFragment有坑，想监听DialogFragment的事件回调非常困难。 因为系统将
> 宿主Activity回收并恢复后，DialogFragment 会丢失回调对象。
> 具体可以看这个帖子：https://stackoverflow.com/questions/13733304/callback-to-a-fragment-from-a-dialogfragment/69622127#69622127

## 使用方式

不提供依赖，直接复制 [DialogQueue.kt](app/src/main/java/com/aitsuki/dialogqueue/DialogQueue.kt) 即可。

```kotlin
class SampleActivity : AppcompatActivity {

    // 传入一个lifecycleOwner对象来初始化一个Dialog队列
    private val queue = DialogQueue(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 无需担心dialog重复显示，因为队列中的元素是唯一的
        showDialogs()
        showDialogs()
        showDialogs()
    }

    // 无需担心dialog窗体泄漏，因为DialogQueue监听了声明周期
//    override fun onDestroy() {
//        super.onDestroy()
//    }

    private fun showDialogs() {
        // offer方法将构建dialog的lambda函数放入队列中，在Activity onResumed后队列会调用lambda构建dialog并显示。
        // "foo" 标签用于标识dialog在队列中的唯一性
        // DialogQueue内部是一个优先队列。1 是此dialog的优先级（数字越大优先级越高，越先显示）
        queue.offer("foo", 1) { next ->
            MaterialAlertDialogBuilder(this)
                .setMessage("Hello, foo!")
                .setOnDismissListener { next() } // 在dialog消失时调用next函数
                .create()
        }

        // 虽然 "bar" dialog 的优先级比 "foo" 高，但是并不一定是 "bar" 先展示。
        // 取决于当前dialogQueue是否处于阻塞状态。例如当前生命周期不是onResumed，或者当前有其他dialog正在显示。
        // 当dialog队列阻塞时，将先显示bar然后再显示foo
        // 当dialog队列不阻塞时，将直接显示 "foo" dialog。此时优先级就"不起作用"了
        queue.offer("bar", 2) { next ->
            MaterialAlertDialogBuilder(this)
                .setMessage("Hello, bar!")
                .setOnDismissListener { next() } // 在dialog消失时调用next函数
                .create()
        }
    }
}
```