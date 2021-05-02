package org.zhx.commom.widgets.animatedtabview.demo

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_main.*
import org.zhx.commom.widgets.AnimatedTabView

class MainActivity : AppCompatActivity() {
    private val images = arrayOf(
        R.drawable.ic_home_white_36dp,
        R.drawable.ic_visibility_white_36dp,
        R.drawable.ic_shopping_cart_white_36dp,
        R.drawable.ic_perm_identity_white_36dp
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var builder = AnimatedTabView.Builder(this)
        builder.height = 120
        builder.arrays = resources.getStringArray(R.array.tab_item_array)
        builder.images = images
        builder.selectedTextColor = Color.GREEN  // default Color.WHITE
        builder.unSelectedTextColor = Color.WHITE // default Color.WHITE
//        builder.backgroundColor = Color.BLACK  // default #30000000
        var view = builder.build()
        test_table_container.addView(view)
    }
}