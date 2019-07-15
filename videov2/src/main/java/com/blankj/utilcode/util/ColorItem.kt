package com.blankj.utilcode.util


class ColorItem {
    var color: String = "#FFFFFF"
    var isSeleted: Boolean = false
    var bgImage: Int = -1
    var bgImageEnable: Boolean = false


}

//颜色相关
class ColorData {

    companion object {
        private var colorList = arrayListOf<String>()
        private var colorItems = arrayListOf<ColorItem>()

        fun createData() {
            colorItems.clear()
            getColor()
            for ((index, values) in colorList.withIndex()) {
                var item = ColorItem()
                item.color = values
                colorItems.add(item)
            }
        }

        fun getData(colorItems: ArrayList<ColorItem>) {
            colorItems.clear()
            colorItems.addAll(this.colorItems)
            this.colorItems.clear()
        }


        //获取颜色列表
        fun getColor() {
            colorList!!.clear()
            colorList.add("#FFFFFF")
            colorList.add("#000000")
            colorList.add("#7F7F7F")
            colorList.add("#748A9A")
            colorList.add("#FFE0F0")
            colorList.add("#FF54A3")
            colorList.add("#FF227E")
            colorList.add("#FF0033")

            colorList.add("#F05957")
            colorList.add("#F47E7D")
            colorList.add("#FF6432")
            colorList.add("#F99300")
            colorList.add("#FFD456")
            colorList.add("#FFD700")
            colorList.add("#FFF6B4")
            colorList.add("#C4E470")

            colorList.add("#719E30")
            colorList.add("#688D6E")
            colorList.add("#008A10")
            colorList.add("#336327")
            colorList.add("#80D5FF")
            colorList.add("#48B5FF")
            colorList.add("#0076E2")
            colorList.add("#262AB6")

            colorList.add("#8C82BA")
            colorList.add("#8D6DFF")
            colorList.add("#8F007F")
            colorList.add("#3D0069")
            colorList.add("#3E0044")
            colorList.add("#9F272F")
            colorList.add("#AA684F")
            colorList.add("#925B48")


        }
    }
}