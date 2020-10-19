package de.eternalwings.vima.process

import de.eternalwings.vima.domain.Thumbnail
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import javax.imageio.ImageIO
import kotlin.math.floor
import kotlin.math.sqrt

@Component
class CollageCreator {
    private fun isPerfectSquare(value: Int): Boolean {
        val root = sqrt(value.toDouble())
        return (root - floor(root)) <= 0.00000001
    }

    fun createCollageUsing(thumbnails: List<Thumbnail>, finalWidth: Int = 320, finalHeight: Int = 180): ByteArray {
        if(!isPerfectSquare(thumbnails.size)) {
            throw IllegalArgumentException("Count of images needs to perfect square, was " + thumbnails.size)
        }

        val perColumn = sqrt(thumbnails.size.toDouble()).toInt()
        val perImageHeight = finalHeight / perColumn
        val perImageWidth = finalWidth / perColumn

        val imageLocations = thumbnails.map { thumb -> thumb.locationPath }
        val images = imageLocations.map { path -> ImageIO.read(path.toFile()) }
        val targetImage = BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB)
        val imageGraphics = targetImage.graphics

        for (i in images.indices) {
            val image = images[i]
            val x = (i % perColumn) * perImageWidth
            val y = (i / perColumn) * perImageHeight
            imageGraphics.drawImage(image, x, y, perImageWidth, perImageHeight, null);
        }

        imageGraphics.dispose()
        val output = ByteArrayOutputStream()
        ImageIO.write(targetImage, "png", output)
        return output.toByteArray()
    }
}
