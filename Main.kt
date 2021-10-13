package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.experimental.xor

fun main() {
    while (true) {
        println("Task (hide, show, exit):")
        val cmd = readLine()!!
        when (cmd) {
            "hide" -> hide()
            "show" -> show()
            "exit" -> break
            else -> println("Wrong task: $cmd")
        }
    }
    println("Bye!")
}

fun hide() {
    println("Input image file:")
    val inFname = readLine()!!
    println("Output image file:")
    val outFname = readLine()!!
    val inputFile = File(inFname)
    val outputFile = File(outFname)
    println("Message to hide:")
    var msg = readLine()!!
    println("Password:")
    val pw = readLine()!!
    val arr = msg.encrypt(pw) + byteArrayOf(0, 0, 3)
    try {
        val myImage: BufferedImage = ImageIO.read(inputFile)
        if (myImage.width * myImage.height < arr.size * 8)
            throw Exception("The input image is not large enough to hold this message.")
        var k = 0
        l@for (y in 0 until myImage.height) {
            for (x in 0 until myImage.width) {
                val color = Color(myImage.getRGB(x, y))
                val byte = arr[k / 8].toInt()
                val bit = if (byte and 2.pow(7 - k % 8) > 0) 1 else 0
                val colorNew = Color(color.red, color.green, color.blue and 254 or bit)
                myImage.setRGB(x, y, colorNew.rgb)
                k++
                if (k == 8 * arr.size) break@l
            }
        }
        ImageIO.write(myImage, "png", outputFile)
        println("Message saved in $outFname image.")
    } catch (e: Exception) {
        println(e.message)
    }
}

fun show() {
    println("Input image file:")
    val inFname = readLine()!!
    println("Password:")
    val pw = readLine()!!
    val inputFile = File(inFname)
    val myImage: BufferedImage = ImageIO.read(inputFile)
    var arr = ""
    var byte = 0
    var k = 0
    l@for (y in 0 until myImage.height) {
        for (x in 0 until myImage.width) {
            val color = Color(myImage.getRGB(x, y))
            val l = 7 - k % 8 // l = 7..0
            val bit = color.blue and 1
            if (bit == 1) {
                val mask = 255 - 2.pow(l) // 11011111 for l = 5
                byte = byte and mask
                byte += 2.pow(l)
            }
            if (k % 8 == 7) {
                if (k > 24 && byte == 3 && arr[k / 8 - 1].toInt() == 0 && arr[k / 8 - 2].toInt() == 0)
                    break@l
                arr += byte.toChar()
                byte = 0
            }
            k++
        }
    }
    var msg = arr.substring(0..arr.length - 3)
    msg = msg.encrypt(pw).decodeToString()
    println("Message:\n$msg")
}

fun Int.pow(m: Int): Int {
    var p = 1
    repeat (m) { p *= this}
    return p
}

fun String.encrypt(pw: String): ByteArray {
    val a = this.encodeToByteArray()
    val b = pw.encodeToByteArray()
    val c = a.copyOf()
    for (i in 0..a.lastIndex) {
        c[i] = a[i] xor b[i % b.size]
    }
    return c
}
