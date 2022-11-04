package online.ruin_of_future.jcef_example

fun main() {
    println("Hello world")
    val jFrame = MyJFrame()
    jFrame.mainFrame(startURL = "https://www.baidu.com", useOSR = true, isTransparent = false, args = arrayOf())
}