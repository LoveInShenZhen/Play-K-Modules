package K.Common

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-16
 * Time: 上午11:25
 * To change this template use File | Settings | File Templates.
 */


import jj.play.ns.nl.captcha.Captcha
import jj.play.ns.nl.captcha.backgrounds.FlatColorBackgroundProducer
import jj.play.ns.nl.captcha.backgrounds.GradiatedBackgroundProducer
import jj.play.ns.nl.captcha.backgrounds.TransparentBackgroundProducer
import jj.play.ns.nl.captcha.gimpy.BlockGimpyRenderer
import jj.play.ns.nl.captcha.gimpy.DropShadowGimpyRenderer
import jj.play.ns.nl.captcha.gimpy.RippleGimpyRenderer
import jj.play.ns.nl.captcha.noise.CurvedLineNoiseProducer
import jj.play.ns.nl.captcha.text.producer.TextProducer
import jj.play.ns.nl.captcha.text.renderer.ColoredEdgesWordRenderer
import jj.play.ns.nl.captcha.text.renderer.WordRenderer
import org.apache.commons.io.output.ByteArrayOutputStream
import org.apache.commons.lang3.StringUtils

import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.ImageWriter
import javax.imageio.stream.FileImageOutputStream
import java.awt.*
import java.awt.font.FontRenderContext
import java.awt.font.GlyphVector
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.security.SecureRandom
import java.util.ArrayList
import java.util.Random

/**
 * Images utils
 */
object Images {

    /**
     * Resize an image

     * originalImage The image file
     * to            The destination file
     * w             The new width (or -1 mail_to proportionally resize) or the maxWidth if keepRatio is true
     * h             The new height (or -1 mail_to proportionally resize) or the maxHeight if keepRatio is true
     * keepRatio     : if true, resize will keep the original image ratio and use w and h as max dimensions
     */
    @JvmOverloads fun resize(originalImage: File, to: File, w: Int, h: Int, keepRatio: Boolean = false) {
        var w = w
        var h = h
        try {
            val source = ImageIO.read(originalImage)
            val owidth = source.width
            val oheight = source.height
            val ratio = owidth.toDouble() / oheight

            val maxWidth = w
            val maxHeight = h

            if (w < 0 && h < 0) {
                w = owidth
                h = oheight
            }
            if (w < 0 && h > 0) {
                w = (h * ratio).toInt()
            }
            if (w > 0 && h < 0) {
                h = (w / ratio).toInt()
            }

            if (keepRatio) {
                h = (w / ratio).toInt()
                if (h > maxHeight) {
                    h = maxHeight
                    w = (h * ratio).toInt()
                }
                if (w > maxWidth) {
                    w = maxWidth
                    h = (w / ratio).toInt()
                }
            }

            var mimeType = "image/jpeg"
            if (to.name.endsWith(".png")) {
                mimeType = "image/png"
            }
            if (to.name.endsWith(".gif")) {
                mimeType = "image/gif"
            }

            // out
            val dest = BufferedImage(w, h, BufferedImage.TYPE_INT_RGB)
            val srcSized = source.getScaledInstance(w, h, Image.SCALE_SMOOTH)
            val graphics = dest.graphics
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, w, h)
            graphics.drawImage(srcSized, 0, 0, null)
            val writer = ImageIO.getImageWritersByMIMEType(mimeType).next()
            val params = writer.defaultWriteParam
            val toFs = FileImageOutputStream(to)
            writer.output = toFs
            val image = IIOImage(dest, null, null)
            writer.write(null, image, params)
            toFs.flush()
            toFs.close()
            writer.dispose()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    /**
     * Crop an image

     * originalImage The image file
     * to            The destination file
     * x1            The new x origin
     * y1            The new y origin
     * x2            The new x end
     * y2            The new y end
     */
    fun crop(originalImage: File, to: File, x1: Int, y1: Int, x2: Int, y2: Int) {
        try {
            val source = ImageIO.read(originalImage)

            var mimeType = "image/jpeg"
            if (to.name.endsWith(".png")) {
                mimeType = "image/png"
            }
            if (to.name.endsWith(".gif")) {
                mimeType = "image/gif"
            }
            val width = x2 - x1
            val height = y2 - y1

            // out
            val dest = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
            val croppedImage = source.getSubimage(x1, y1, width, height)
            val graphics = dest.graphics
            graphics.color = Color.WHITE
            graphics.fillRect(0, 0, width, height)
            graphics.drawImage(croppedImage, 0, 0, null)
            val writer = ImageIO.getImageWritersByMIMEType(mimeType).next()
            val params = writer.defaultWriteParam
            writer.output = FileImageOutputStream(to)
            val image = IIOImage(dest, null, null)
            writer.write(null, image, params)
            writer.dispose()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    /**
     * Embeds an image watermark over a source image to produce
     * a watermarked one.

     * watermarkImageFile The image file used as the watermark.
     * sourceImageFile    The source image file.
     * destImageFile      The output image file.
     */
    @Throws(IOException::class)
    fun addImageWatermark(watermarkImageFile: File,
                          sourceImageFile: File,
                          destImageFile: File,
                          imgFormat: String,
                          alpha: Float) {
        val sourceImage = ImageIO.read(sourceImageFile)
        val watermarkImage = ImageIO.read(watermarkImageFile)

        // initializes necessary graphic properties
        val g2d = sourceImage.graphics as Graphics2D
        val alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        g2d.composite = alphaChannel
        val width = sourceImage.width
        val scaleWidth = watermarkImage.width
        val height = sourceImage.height
        val scaleHeight = watermarkImage.height
        // 平铺水印图片
        /*for (int x = 5; x < width; x += 2*scaleWidth) {
            for (int y = scaleWidth/2; y < height; y += 2*scaleHeight) {
            	int x2 =(int) (x+scaleWidth*Math.cos(Math.PI/6));
            	int y2 =(int)(y-scaleWidth*Math.sin(Math.PI/6));
            	
            	int x3 =(int) (x+scaleHeight*Math.sin(Math.PI/6));
            	int y3 =(int)(y+scaleHeight*Math.cos(Math.PI/6));
            	
            	int x4 =(int) (x3+scaleWidth*Math.cos(Math.PI/6));
            	int y4 =(int)(y3-scaleWidth*Math.sin(Math.PI/6));
            	Logger.debug("x="+x+",y="+y+";x1="+x2+",y1="+y2+";x2="+x3+",y2="+y3+";x4="+x4+",y4="+y4);
                g2d.drawImage(watermarkImage, x, y, x2, y2, x3, y3, x4, y4, null);
                //g2d.drawImage(watermarkImage, x, y, null);
            }
        }*/
        //Logger.debug("width="+width+",height="+height+",scaleWidth="+scaleWidth+",scaleHeight"+scaleHeight);
        var x = 10
        while (x < width) {
            var y = 10
            while (y < height) {
                g2d.drawImage(watermarkImage, x, y, null)
                y += 2 * scaleHeight
            }
            x += 2 * scaleWidth
        }

        ImageIO.write(sourceImage, imgFormat, destImageFile)
        g2d.dispose()

    }


    @Throws(IOException::class)
    fun addImageWatermark(sourceImage: BufferedImage, watermarkImage: BufferedImage,
                          alpha: Float): InputStream {
        // initializes necessary graphic properties
        val g2d = sourceImage.graphics as Graphics2D
        val alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)
        g2d.composite = alphaChannel

        // 平铺水印图片
        var x = 0
        while (x < sourceImage.width) {
            var y = 0
            while (y < sourceImage.height) {
                g2d.drawImage(watermarkImage, x, y, null)
                y += watermarkImage.height
            }
            x += watermarkImage.width
        }

        val outs = ByteArrayOutputStream()
        ImageIO.write(sourceImage, "png", outs)
        g2d.dispose()
        val ins = ByteArrayInputStream(outs.toByteArray())
        return ins
    }

    @Throws(IOException::class)
    fun ImageWatermark(filePath: String, watermarkFilePath: String,
                       alpha: Float): InputStream {
        val sourceImage = ImageIO.read(File(filePath))
        val watermarkImage = ImageIO.read(File(watermarkFilePath))
        return addImageWatermark(sourceImage, watermarkImage, alpha)

    }

    //    /**
    //     * Create a captche image
    //     */
    //    public static Captcha captcha(int width, int height) {
    //        return new Captcha(width, height);
    //    }
    //
    //    /**
    //     * Create a 150x50 captcha image
    //     */
    //    public static Captcha captcha() {
    //        return captcha(150, 50);
    //    }

    /**
     * A captcha image.
     */
    class ImageCaptcha : InputStream, TextProducer {

        var width: Int = 0
        var height: Int = 0
        private var answer: String? = null

        constructor(width: Int, height: Int) {
            this.width = width
            this.height = height
        }

        constructor() {
            this.width = DEFAULT_WIDTH
            this.height = DEFAULT_HEIGHT
        }

        fun SetAnswer(text: String) {
            this.answer = text
        }

        fun GetAnswer(): String {
            return this.answer ?: ""
        }

        private var bais: ByteArrayInputStream? = null

        @Throws(IOException::class)
        override fun read(): Int {
            check()
            return bais!!.read()
        }

        @Throws(IOException::class)
        override fun read(b: ByteArray): Int {
            check()
            return bais!!.read(b)
        }

        internal fun check() {
            try {
                if (StringUtils.isBlank(this.answer)) {
                    SetAnswer(RandomText())
                }
                if (bais == null) {
                    val builder = Captcha.Builder(width, height)

                    val wordRenderer: WordRenderer
                    if (OutlineFont) {
                        // 空心字体
                        wordRenderer = ColoredEdgesWordRenderer(DEFAULT_COLORS, DEFAULT_FONTS)
                    } else {
                        // 实心字体
                        wordRenderer = KWordRenderer(DEFAULT_COLORS,
                                DEFAULT_FONTS, TextMarginLeft, height - TextMarginBottom)
                    }

                    builder.addText(this, wordRenderer)

                    if (AddBorder) {
                        builder.addBorder()
                        //                        Logger.debug("ADD Border");
                    }
                    if (CurvedLine) {
                        builder.addNoise(CurvedLineNoiseProducer(Color.decode(CurvedLineColor),
                                CurvedLineWidth.toFloat()))
                        builder.addNoise(CurvedLineNoiseProducer(Color.decode(CurvedLineColor),
                                CurvedLineWidth.toFloat()))
                    }
                    if (BackgroundTransparent) {
                        val tbp = TransparentBackgroundProducer()
                        builder.addBackground(tbp)
                    }
                    if (BackgroundFlatColor) {
                        val fbp = FlatColorBackgroundProducer(Color.decode(FlatColor))
                        builder.addBackground(fbp)
                    }
                    if (BackgroundGradiated) {
                        val gbp = GradiatedBackgroundProducer()
                        gbp.setFromColor(Color.decode(GradiatedFromColor))
                        gbp.setToColor(Color.decode(GradiatedToColor))
                        builder.addBackground(gbp)
                    }
                    if (RippleGimpyRenderer) {
                        val rip = RippleGimpyRenderer()
                        builder.gimp(rip)
                    }
                    if (BlockGimpyRenderer) {
                        val blk = BlockGimpyRenderer(BlockGimpyBlockSize)
                        builder.gimp(blk)
                    }
                    if (DropShadowGimpyRenderer) {
                        val dsh = DropShadowGimpyRenderer(DropShadowGimpyRadius,
                                DropShadowGimpyOpacity)
                        builder.gimp(dsh)
                    }
                    val ca = builder.build()
                    val bi = ca.image
                    val baos = ByteArrayOutputStream()
                    ImageIO.write(bi, "png", baos)
                    bais = ByteArrayInputStream(baos.toByteArray())
                }
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }


        }

        override fun getText(): String {
            return this.answer ?: ""
        }

        companion object {

            internal val DEFAULT_WIDTH = Hub.configuration().getInt("captcha_Width", 150)!!
            internal val DEFAULT_HEIGHT = Hub.configuration().getInt("captcha_Height", 50)!!
            internal val TextLength = Hub.configuration().getInt("captcha_Text_Length", 4)!!
            internal val TextMarginBottom = Hub.configuration().getInt("captcha_Text_MarginBottom", 15)!!
            internal val TextMarginLeft = Hub.configuration().getInt("captcha_Text_MarginLeft", 10)!!
            internal val OutlineFont = ConfGetBool("captcha_Outline_Font", false)
            internal val BackgroundTransparent = ConfGetBool("captcha_Background_Transparent", true)
            internal val BackgroundGradiated = ConfGetBool("captcha_Background_Gradiated", false)
            internal val GradiatedFromColor = Hub.configuration().getString("captcha_Background_Gradiated_FromColor", "#EDEEF0")
            internal val GradiatedToColor = Hub.configuration().getString("captcha_Background_Gradiated_ToColor", "#C5D0E6")
            internal val BackgroundFlatColor = ConfGetBool("captcha_Background_FlatColor", false)
            internal val FlatColor = Hub.configuration().getString("captcha_Background_FlatColor_Color", "#EDEEF0")
            internal val RippleGimpyRenderer = ConfGetBool("captcha_GimpyRenderer_RippleGimpyRenderer", true)
            internal val BlockGimpyRenderer = ConfGetBool("captcha_GimpyRenderer_BlockGimpyRenderer", false)
            internal val BlockGimpyBlockSize = Hub.configuration().getInt("captcha_GimpyRenderer_BlockGimpyRenderer_BlockSize", 1)!!
            internal val DropShadowGimpyRenderer = ConfGetBool("captcha_DropShadowGimpyRenderer", false)
            internal val DropShadowGimpyRadius = Hub.configuration().getInt("captcha_DropShadowGimpyRenderer_Radius", 3)!!
            internal val DropShadowGimpyOpacity = Hub.configuration().getInt("captcha_DropShadowGimpyRenderer_Opacity", 75)!!
            internal val CurvedLine = ConfGetBool("captcha_Noise_CurvedLine", false)
            internal val CurvedLineWidth = Hub.configuration().getInt("captcha_Noise_CurvedLine_Width", 2)!!
            internal val CurvedLineColor = Hub.configuration().getString("captcha_Noise_CurvedLine_Color", "#2795EA")
            internal val AddBorder = ConfGetBool("captcha_AddBorder", false)

            private val CHAR_CODES = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789"

            private val DEFAULT_COLORS = ArrayList<Color>()
            private val DEFAULT_FONTS = ArrayList<Font>()

            init {
                val colors = Hub.configuration().getStringList("captcha_Font_Colors")
                if (colors == null) {
                    DEFAULT_COLORS.add(Color.BLUE)
                    DEFAULT_COLORS.add(Color.GREEN)
                    DEFAULT_COLORS.add(Color.RED)
                    DEFAULT_COLORS.add(Color.BLACK)
                } else {
                    for (color_name in colors) {
                        DEFAULT_COLORS.add(Color.decode(color_name))
                    }
                }

                val fonts = Hub.configuration().getStringList("captcha_Fonts")
                if (fonts == null) {
                    DEFAULT_FONTS.add(Font("Arial", Font.BOLD, 40))
                    DEFAULT_FONTS.add(Font("Courier", Font.BOLD, 40))
                    DEFAULT_FONTS.add(Font("Arial", Font.ITALIC, 40))
                    DEFAULT_FONTS.add(Font("Courier", Font.ITALIC, 40))
                } else {
                    for (font_name in fonts) {
                        DEFAULT_FONTS.add(Font.decode(font_name))
                    }
                }
            }

            var ContenType = "image/png"

            fun RandomText(): String {
                val charsArray = CHAR_CODES.toCharArray()
                val random = Random(System.currentTimeMillis())
                val sb = StringBuffer(TextLength)
                for (i in 0..TextLength - 1) {
                    sb.append(charsArray[random.nextInt(charsArray.size)])
                }
                return sb.toString()
            }

            fun ConfGetBool(key: String, defaultValue: Boolean): Boolean {
                val v = Hub.configuration().getString(key)
                if (StringUtils.isBlank(v)) {
                    return defaultValue
                }

                if (v.equals("TRUE", ignoreCase = true) || v.equals("YES", ignoreCase = true)) {
                    return true
                } else {
                    return false
                }
            }
        }
    }
}

/**
 * Resize an image

 * originalImage The image file
 * to            The destination file
 * w             The new width (or -1 mail_to proportionally resize)
 * h             The new height (or -1 mail_to proportionally resize)
 */

internal class KWordRenderer(private val _colors: List<Color>, private val _fonts: List<Font>, private val pos_x_: Int, private val pos_y_: Int) : WordRenderer {

    override fun render(word: String, image: BufferedImage) {
        val g = image.createGraphics()

        val hints = RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON)
        hints.add(RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY))
        g.setRenderingHints(hints)

        g.color = randomColor
        val frc = g.fontRenderContext
        var startPosX = pos_x_
        val wc = word.toCharArray()
        val generator = Random()
        for (element in wc) {
            val itchar = charArrayOf(element)
            val itFont = randomFont
            g.font = itFont

            val gv = itFont.createGlyphVector(frc, itchar)
            val charWitdth = gv.visualBounds.width

            g.drawChars(itchar, 0, itchar.size, startPosX, pos_y_)
            startPosX = startPosX + charWitdth.toInt()
        }
    }

    private val randomColor: Color
        get() = getRandomObject(_colors) as Color

    private val randomFont: Font
        get() = getRandomObject(_fonts) as Font

    private fun getRandomObject(objs: List<Any>): Any {
        if (objs.size == 1) {
            return objs[0]
        }

        val gen = SecureRandom()
        val i = gen.nextInt(objs.size)
        return objs[i]
    }

}
