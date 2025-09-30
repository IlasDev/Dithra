package dev.ilas.dithra.presentation.ui.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import dev.ilas.dithra.presentation.ui.export.ExportResolution
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import androidx.core.graphics.get

/**
 * SVG generation and export utilities.
 * 
 * Provides optimized vector graphics generation:
 * - Path-based approach for black & white images
 * - Rectangle optimization for color palettes
 * - Transparency handling and background options
 * - Efficient rectangle grouping algorithms
 */
object SVGUtils {
    
    /**
     * Generates optimized SVG from bitmap with adaptive algorithms.
     * Uses path-based approach for B&W images, rectangle optimization for color.
     * 
     * @param bitmap Source bitmap to convert
     * @param resolution Target resolution for export
     * @param transparentWhite Whether to make white pixels transparent
     * @param pixelDimensions True pixel dimensions for accurate sizing
     * @param onProgress Progress callback (0.0 to 1.0)
     * @return SVG content as string
     */
    suspend fun generateOptimizedSVG(
        bitmap: Bitmap,
        resolution: ExportResolution,
        transparentWhite: Boolean,
        pixelDimensions: Pair<Int, Int>?,
        onProgress: (Float) -> Unit
    ): String = withContext(Dispatchers.Default) {
        onProgress(0.1f)
        
        val transparentBitmap = if (transparentWhite) {
            BitmapUtils.makeWhiteTransparent(bitmap)
        } else {
            bitmap
        }
        
        onProgress(0.2f)
        
        val finalBitmap = if (resolution == ExportResolution.PIXEL_SIZE) {
            val targetWidth = pixelDimensions?.first?.takeIf { it > 0 } ?: transparentBitmap.width
            val targetHeight = pixelDimensions?.second?.takeIf { it > 0 } ?: transparentBitmap.height

            if (targetWidth != transparentBitmap.width || targetHeight != transparentBitmap.height) {
                BitmapUtils.scaleNearestNeighbor(transparentBitmap, targetWidth, targetHeight)
            } else {
                transparentBitmap
            }
        } else {
            val (targetWidth, targetHeight) = resolution.calculateDimensions(transparentBitmap)
            if (targetWidth != transparentBitmap.width || targetHeight != transparentBitmap.height) {
                BitmapUtils.scaleNearestNeighbor(transparentBitmap, targetWidth, targetHeight)
            } else {
                transparentBitmap
            }
        }
        
        onProgress(0.3f)
        
        val isBW = isBlackAndWhite(finalBitmap)
        onProgress(0.4f)
        
        val svgContent = if (isBW) {
            generateBWSVGWithPaths(finalBitmap, transparentWhite, onProgress)
        } else {
            generateColorSVGWithRects(finalBitmap, transparentWhite, onProgress)
        }
        
        if (transparentBitmap != bitmap && transparentBitmap != finalBitmap) {
            transparentBitmap.recycle()
        }
        if (finalBitmap != transparentBitmap && finalBitmap != bitmap) {
            finalBitmap.recycle()
        }
        
        onProgress(1.0f)
        svgContent
    }
    
    /**
     * Checks if bitmap contains only black, white, and transparent pixels.
     */
    private fun isBlackAndWhite(bitmap: Bitmap): Boolean {
        val width = bitmap.width
        val height = bitmap.height
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap[x, y]
                val alpha = android.graphics.Color.alpha(pixel)
                
                if (alpha < 50) continue
                
                val red = android.graphics.Color.red(pixel)
                val green = android.graphics.Color.green(pixel)
                val blue = android.graphics.Color.blue(pixel)
                
                val isBlack = red < 50 && green < 50 && blue < 50
                val isWhite = red > 200 && green > 200 && blue > 200
                
                if (!isBlack && !isWhite) {
                    return false
                }
            }
        }
        return true
    }
    
    /**
     * Generates SVG with path elements for B&W images (optimized efficiency).
     */
    private suspend fun generateBWSVGWithPaths(
        bitmap: Bitmap,
        transparentWhite: Boolean,
        onProgress: (Float) -> Unit
    ): String {
        val width = bitmap.width
        val height = bitmap.height
        
        val svgBuilder = StringBuilder()
        svgBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        svgBuilder.append("<svg width=\"$width\" height=\"$height\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">\n")
        
        if (!transparentWhite) {
            svgBuilder.append("<rect width=\"$width\" height=\"$height\" fill=\"rgb(255,255,255)\" />\n")
        }
        
        val blackRects = mutableListOf<Rect>()
        val visited = Array(height) { BooleanArray(width) }
        
        var processedPixels = 0
        val totalPixels = width * height
        
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (visited[y][x]) continue
                
                val pixel = bitmap[x, y]
                val alpha = android.graphics.Color.alpha(pixel)
                
                if (alpha < 50) {
                    visited[y][x] = true
                    continue
                }
                
                val red = android.graphics.Color.red(pixel)
                val green = android.graphics.Color.green(pixel)
                val blue = android.graphics.Color.blue(pixel)
                
                if (red < 50 && green < 50 && blue < 50) {
                    val rect = findLargestBlackRect(bitmap, x, y, visited)
                    if (rect.width() > 0 && rect.height() > 0) {
                        blackRects.add(rect)
                        
                        for (ry in rect.top until rect.bottom) {
                            for (rx in rect.left until rect.right) {
                                visited[ry][rx] = true
                            }
                        }
                    }
                } else {
                    visited[y][x] = true
                }
                
                processedPixels++
                if (processedPixels % 1000 == 0) {
                    onProgress(0.4f + (processedPixels.toFloat() / totalPixels) * 0.5f)
                    yield()
                }
            }
        }
        
        val pathBuilder = StringBuilder()
        for (rect in blackRects) {
            if (pathBuilder.isNotEmpty()) pathBuilder.append(" ")
            pathBuilder.append("M ${rect.left} ${rect.top} L ${rect.right} ${rect.top} L ${rect.right} ${rect.bottom} L ${rect.left} ${rect.bottom} Z")
        }
        
        if (pathBuilder.isNotEmpty()) {
            svgBuilder.append("<path fill=\"rgb(0,0,0)\" d=\"$pathBuilder\" />\n")
        }
        
        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }
    
    /**
     * Generates SVG with path elements for color images (optimized for palettes).
     */
    private suspend fun generateColorSVGWithRects(
        bitmap: Bitmap,
        transparentWhite: Boolean,
        onProgress: (Float) -> Unit
    ): String {
        val width = bitmap.width
        val height = bitmap.height
        
        val colorMap = mutableMapOf<Int, MutableList<Rect>>()
        val visited = Array(height) { BooleanArray(width) }
        
        var processedPixels = 0
        val totalPixels = width * height
        for (y in 0 until height) {
            for (x in 0 until width) {
                if (visited[y][x]) continue
                
                val pixel = bitmap[x, y]
                val alpha = android.graphics.Color.alpha(pixel)
                
                if (alpha < 50) {
                    visited[y][x] = true
                    processedPixels++
                    continue
                }
                val rect = findLargestColorRect(bitmap, x, y, pixel, visited)
                if (rect.width() > 0 && rect.height() > 0) {
                    if (!colorMap.containsKey(pixel)) {
                        colorMap[pixel] = mutableListOf()
                    }
                    colorMap[pixel]!!.add(rect)
                    
                    for (ry in rect.top until rect.bottom) {
                        for (rx in rect.left until rect.right) {
                            if (ry < height && rx < width) {
                                visited[ry][rx] = true
                            }
                        }
                    }
                    processedPixels += rect.width() * rect.height()
                } else {
                    visited[y][x] = true
                    processedPixels++
                }
                
                if (processedPixels % 1000 == 0) {
                    onProgress(0.4f + (processedPixels.toFloat() / totalPixels) * 0.5f)
                    yield()
                }
            }
        }
        

        val svgBuilder = StringBuilder()
        svgBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        svgBuilder.append("<svg width=\"$width\" height=\"$height\" version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">\n")
        
        if (!transparentWhite) {
            svgBuilder.append("<rect width=\"$width\" height=\"$height\" fill=\"rgb(255,255,255)\" />\n")
        }
        for ((pixelColor, rectangles) in colorMap) {
            val red = android.graphics.Color.red(pixelColor)
            val green = android.graphics.Color.green(pixelColor)
            val blue = android.graphics.Color.blue(pixelColor)
            val alpha = android.graphics.Color.alpha(pixelColor)
            
            val pathBuilder = StringBuilder()
            for (rect in rectangles) {
                if (pathBuilder.isNotEmpty()) pathBuilder.append(" ")
                pathBuilder.append("M ${rect.left} ${rect.top} L ${rect.right} ${rect.top} L ${rect.right} ${rect.bottom} L ${rect.left} ${rect.bottom} Z")
            }
            
            if (pathBuilder.isNotEmpty()) {
                val rgbColor = "rgb($red,$green,$blue)"
                svgBuilder.append("<path fill=\"$rgbColor\"")
                
                if (alpha < 255) {
                    val opacity = alpha / 255.0
                    svgBuilder.append(" opacity=\"$opacity\"")
                }
                
                svgBuilder.append(" d=\"$pathBuilder\" />\n")
            }
        }
        
        svgBuilder.append("</svg>")
        return svgBuilder.toString()
    }
    
    /**
     * Finds largest rectangle of black pixels starting from given position.
     */
    private fun findLargestBlackRect(bitmap: Bitmap, startX: Int, startY: Int, visited: Array<BooleanArray>): Rect {
        val width = bitmap.width
        val height = bitmap.height
        
        var maxWidth = 0
        for (x in startX until width) {
            if (visited[startY][x]) break
            val pixel = bitmap[x, startY]
            val red = android.graphics.Color.red(pixel)
            val green = android.graphics.Color.green(pixel)
            val blue = android.graphics.Color.blue(pixel)
            val alpha = android.graphics.Color.alpha(pixel)
            
            if (alpha >= 50 && red < 50 && green < 50 && blue < 50) {
                maxWidth++
            } else {
                break
            }
        }
        
        var maxHeight = 0
        outer@ for (y in startY until height) {
            for (x in startX until startX + maxWidth) {
                if (visited[y][x]) break@outer
                val pixel = bitmap[x, y]
                val red = android.graphics.Color.red(pixel)
                val green = android.graphics.Color.green(pixel)
                val blue = android.graphics.Color.blue(pixel)
                val alpha = android.graphics.Color.alpha(pixel)
                
                if (alpha < 50 || red >= 50 || green >= 50 || blue >= 50) {
                    break@outer
                }
            }
            maxHeight++
        }
        
        return Rect(startX, startY, startX + maxWidth, startY + maxHeight)
    }
    
    /**
     * Finds largest rectangle of specific color starting from given position.
     */
    private fun findLargestColorRect(
        bitmap: Bitmap,
        startX: Int,
        startY: Int,
        targetColor: Int,
        visited: Array<BooleanArray>
    ): Rect {
        val width = bitmap.width
        val height = bitmap.height
        
        var maxWidth = 0
        for (x in startX until width) {
            if (visited[startY][x]) break
            val pixel = bitmap[x, startY]
            
            if (pixel == targetColor) {
                maxWidth++
            } else {
                break
            }
        }
        
        var maxHeight = 0
        outer@ for (y in startY until height) {
            for (x in startX until startX + maxWidth) {
                if (visited[y][x]) break@outer
                val pixel = bitmap[x, y]
                
                if (pixel != targetColor) {
                    break@outer
                }
            }
            maxHeight++
        }
        
        return Rect(startX, startY, startX + maxWidth, startY + maxHeight)
    }
    
    /**
     * Saves SVG data to file using Storage Access Framework.
     * 
     * @param context Android context
     * @param svgData SVG content as string
     * @param uri Target file URI
     */
    suspend fun saveSVGToFile(
        context: Context,
        svgData: String,
        uri: Uri
    ) = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(svgData.toByteArray())
                outputStream.flush()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}