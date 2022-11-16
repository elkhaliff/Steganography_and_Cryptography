package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.experimental.xor

private const val INPUT_ERROR = "Can't read input file!"
private const val OUTPUT_ERROR = "Can't write to output file!"
private const val SIZE_ERROR = "The input image is not large enough to hold this message."
private const val NO_MESSAGE = "No message was hidden!"

class Crypto {
    private val endBytes = convertBit("\u0000\u0000\u0003")
    private fun getFile(type: String) = File(getString("$type image file:"))
    private fun testSize(image: BufferedImage, size: Int) = image.width * image.height >= size
    private fun getPassword() = getString("Password:")

    private fun convertBit(string: String) = string.map { it.code }.map {
            it.toString(2).padStart(8, '0').map { char -> char.digitToInt().toByte() }
        }.flatten()

    private fun encryptBits(hideMessage: List<Byte>, password: String) = convertBit(password).let { keyBits ->
        hideMessage.mapIndexed { index, byte -> byte xor keyBits[index % keyBits.size] }
    }

    private fun decryptBits(message: List<Byte>, password: String) =
        encryptBits(message, password).joinToString("").chunked(8).map { it.toInt(2).toChar() }.joinToString("")


    fun hide(): String {
        val inputFile = getFile("Input")
        val outputFile = getFile("Output")
        val hideMessage = getString("Message to hide:")
        val password = getPassword()
        val image = getBufferedImage(inputFile) ?: return INPUT_ERROR
        val outputString = if (testSize(image, hideMessage.length * 8 + endBytes.size))
                               "Message saved in ${outputFile.name} image."
                           else return SIZE_ERROR
        val encBits = encryptBits(convertBit(hideMessage), password) + endBytes

        var index = 0
        for (y in 0 until image.width) {
            for (x in 0 until image.height) {
                if (index == encBits.size) break
                val bit = encBits[index++].toInt()
                val colorOld = Color(image.getRGB(x, y))
                val hideBlue = colorOld.blue.and(254).or(bit)
                val hideColor = Color(colorOld.red, colorOld.green, hideBlue)
                image.setRGB(x, y, hideColor.rgb)
            }
        }
        return if (saveBufferedImage(image, outputFile)) outputString else OUTPUT_ERROR
    }

    fun show(): String {
        val image = getBufferedImage(getFile("Input")) ?: return INPUT_ERROR
        val password = getPassword()
        val hideMessage = mutableListOf<Byte>()

        for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val bytes = (Color(image.getRGB(x, y)).blue % 2).toByte()
                hideMessage.add(bytes)
                if (hideMessage.takeLast(endBytes.size) == endBytes) {
                    return "Message:\n" + decryptBits(hideMessage.dropLast(endBytes.size), password)
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