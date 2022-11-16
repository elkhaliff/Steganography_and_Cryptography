package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

private const val INPUT_ERROR = "Can't read input file!"
private const val OUTPUT_ERROR = "Can't write to output file!"
private const val SIZE_ERROR = "The input image is not large enough to hold this message."
private const val NO_MESSAGE = "No message was hidden!"

class Crypto {
    private val endBytes = listOf(0, 0, 3)

    private fun getPath(type: String) = getString("$type image file:")
    private fun testSize(image: BufferedImage, size: Int) = image.width * image.height >= size

    fun hide(): String {
        val inputFile = File(getPath("Input"))
        val outputFile = File(getPath("Output"))
        val hideMessage = (getString("Message to hide:").map { it.code } + endBytes).joinToString("") {
            it.toString(2).padStart(8, '0')
        }
        val image = getBufferedImage(inputFile) ?: return INPUT_ERROR
        val outputString =
            if (testSize(image, hideMessage.length))
                "Message saved in ${outputFile.name} image."
            else return SIZE_ERROR

        var index = 0
        for (y in 0 until image.width) {
            for (x in 0 until image.height) {
                if (index == hideMessage.length) break
                val bit = hideMessage[index++].digitToInt()
                val colorOld = Color(image.getRGB(x, y))
                val hideBlue = colorOld.blue.and(254).or(bit)
                val hideColor = Color(colorOld.red, colorOld.green, hideBlue)
                image.setRGB(x, y, hideColor.rgb)
            }
        }
        return if (saveBufferedImage(image, outputFile)) outputString else OUTPUT_ERROR
    }

    fun show(): String {
        val image = getBufferedImage(File(getPath("Input"))) ?: return INPUT_ERROR
        var testBytes = listOf(1, 1, 1)
        var byte = ""
        var hideMessage = ""

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                byte += Color(image.getRGB(x, y)).blue % 2
                if (byte.length == 8) {
                    val enCode = byte.toInt(2)
                    hideMessage += enCode.toChar()
                    testBytes = testBytes.drop(1) + enCode
                    if (testBytes == endBytes) return "Message:\n" + hideMessage.dropLast(3)
                    byte = ""
                }
            }
        }
        return NO_MESSAGE
    }

    private fun getBufferedImage(file: File): BufferedImage? =
        try {
            ImageIO.read(file)
        } catch (e: Exception) {
            null
        }

    private fun saveBufferedImage(image: BufferedImage, file: File, fileType: String = "png"): Boolean =
        try {
            ImageIO.write(image, fileType, file)
        } catch (e: Exception) {
            false
        }
}