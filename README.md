# animatedTabView

### 1、有问题请 提交 isuue/(QQ:194093798) 谢谢大家 持续更新

[CSDN_blog](https://blog.csdn.net/u013783167/article/details/116357766)

![效果](https://github.com/zhoulinxue/animatedTabView/blob/master/demo.gif)

## 集成
```
allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
```
dependencies {
	        implementation 'com.github.zhoulinxue:animatedTabView:1.0.4'
	}
```
## 代码调用 (二选一 代码或xml)

 ```
package org.zhx.commom.widgets.animatedtabview.demo

import android.app.ActionBar
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_main.*
import org.zhx.commom.widgets.AnimatedTabView
import kotlin.random.Random

class MainActivity : AppCompatActivity(), AnimatedTabView.OnItemChangeLisenter {
    private val images = arrayOf(
        R.drawable.ic_home_white_36dp,
        R.drawable.ic_visibility_white_36dp,
        R.drawable.ic_shopping_cart_white_36dp,
//        R.drawable.ic_perm_identity_white_36dp
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var builder = AnimatedTabView.Builder(this,)
        builder.height = 120
        builder.arrays = resources.getStringArray(R.array.tab_item_array)
        builder.images = images
        builder.selectedTextColor = Color.GREEN  // default Color.WHITE
        builder.unSelectedTextColor = Color.WHITE // default Color.WHITE
        builder.backgroundColor =
            resources.getColor(R.color.black_30) // default #30000000   不设置 就不绘制 背景
        builder.setOnItemClick(this)
        var view: AnimatedTabView = builder.build()
        test_table_container.addView(view)
        tab_btn.setOnClickListener {
            var position = Random.nextInt(images.size)
            view.setSelection(position)
            bottom_tabView.setSelection(position)
        }

        var builder2 = AnimatedTabView.Builder(this,)
        builder2.height = 120
        builder2.arrays = resources.getStringArray(R.array.tab_item_array)
        builder2.images = images
        builder2.selectedTextColor = Color.GREEN  // default Color.WHITE
        builder2.unSelectedTextColor = Color.WHITE // default Color.WHITE
//        builder2.backgroundColor =resources.getColor(R.color.black_30) // default #30000000   不设置 就不绘制 背景
        builder2.setOnItemClick(this)

        bottom_tabView.setBuilder(builder2)

        center_btn.text = getTextByStatus(view)
        center_btn.setOnClickListener {
            // must set backgroundColor
            view.tocenter()
            center_btn.text = getTextByStatus(view)
        }
    }

    private fun getTextByStatus(view: AnimatedTabView): CharSequence? {
        return String.format(
            getString(R.string.animation_format),
            if (view.state != AnimatedTabView.State.CLOSE) {
                getString(R.string.close)
            } else {
                getString(R.string.open)
            }
        )

    }

    override fun onItemSelected(position: Int) {
        Log.e("AnimatedTabView", "itemclick    $position")
    }
}

 ```
