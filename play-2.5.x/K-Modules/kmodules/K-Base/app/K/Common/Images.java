package K.Common;

/**
 * Created with IntelliJ IDEA.
 * User: kk
 * Date: 13-11-16
 * Time: 上午11:25
 * To change this template use File | Settings | File Templates.
 */


import jj.play.ns.nl.captcha.Captcha;
import jj.play.ns.nl.captcha.backgrounds.FlatColorBackgroundProducer;
import jj.play.ns.nl.captcha.backgrounds.GradiatedBackgroundProducer;
import jj.play.ns.nl.captcha.backgrounds.TransparentBackgroundProducer;
import jj.play.ns.nl.captcha.gimpy.BlockGimpyRenderer;
import jj.play.ns.nl.captcha.gimpy.DropShadowGimpyRenderer;
import jj.play.ns.nl.captcha.gimpy.RippleGimpyRenderer;
import jj.play.ns.nl.captcha.noise.CurvedLineNoiseProducer;
import jj.play.ns.nl.captcha.text.producer.TextProducer;
import jj.play.ns.nl.captcha.text.renderer.ColoredEdgesWordRenderer;
import jj.play.ns.nl.captcha.text.renderer.WordRenderer;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.lang3.StringUtils;
import play.Configuration;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Images utils
 */
public class Images {


    /**
     * Resize an image
     *
     * originalImage The image file
     * to            The destination file
     * w             The new width (or -1 mail_to proportionally resize)
     * h             The new height (or -1 mail_to proportionally resize)
     */
    public static void resize(File originalImage, File to, int w, int h) {
        resize(originalImage, to, w, h, false);
    }

    /**
     * Resize an image
     *
     * originalImage The image file
     * to            The destination file
     * w             The new width (or -1 mail_to proportionally resize) or the maxWidth if keepRatio is true
     * h             The new height (or -1 mail_to proportionally resize) or the maxHeight if keepRatio is true
     * keepRatio     : if true, resize will keep the original image ratio and use w and h as max dimensions
     */
    public static void resize(File originalImage, File to, int w, int h, boolean keepRatio) {
        try {
            BufferedImage source = ImageIO.read(originalImage);
            int owidth = source.getWidth();
            int oheight = source.getHeight();
            double ratio = (double) owidth / oheight;

            int maxWidth = w;
            int maxHeight = h;

            if (w < 0 && h < 0) {
                w = owidth;
                h = oheight;
            }
            if (w < 0 && h > 0) {
                w = (int) (h * ratio);
            }
            if (w > 0 && h < 0) {
                h = (int) (w / ratio);
            }

            if (keepRatio) {
                h = (int) (w / ratio);
                if (h > maxHeight) {
                    h = maxHeight;
                    w = (int) (h * ratio);
                }
                if (w > maxWidth) {
                    w = maxWidth;
                    h = (int) (w / ratio);
                }
            }

            String mimeType = "image/jpeg";
            if (to.getName().endsWith(".png")) {
                mimeType = "image/png";
            }
            if (to.getName().endsWith(".gif")) {
                mimeType = "image/gif";
            }

            // out
            BufferedImage dest = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
            Image srcSized = source.getScaledInstance(w, h, Image.SCALE_SMOOTH);
            Graphics graphics = dest.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, w, h);
            graphics.drawImage(srcSized, 0, 0, null);
            ImageWriter writer = ImageIO.getImageWritersByMIMEType(mimeType).next();
            ImageWriteParam params = writer.getDefaultWriteParam();
            FileImageOutputStream toFs = new FileImageOutputStream(to);
            writer.setOutput(toFs);
            IIOImage image = new IIOImage(dest, null, null);
            writer.write(null, image, params);
            toFs.flush();
            toFs.close();
            writer.dispose();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Crop an image
     *
     * originalImage The image file
     * to            The destination file
     * x1            The new x origin
     * y1            The new y origin
     * x2            The new x end
     * y2            The new y end
     */
    public static void crop(File originalImage, File to, int x1, int y1, int x2, int y2) {
        try {
            BufferedImage source = ImageIO.read(originalImage);

            String mimeType = "image/jpeg";
            if (to.getName().endsWith(".png")) {
                mimeType = "image/png";
            }
            if (to.getName().endsWith(".gif")) {
                mimeType = "image/gif";
            }
            int width = x2 - x1;
            int height = y2 - y1;

            // out
            BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Image croppedImage = source.getSubimage(x1, y1, width, height);
            Graphics graphics = dest.getGraphics();
            graphics.setColor(Color.WHITE);
            graphics.fillRect(0, 0, width, height);
            graphics.drawImage(croppedImage, 0, 0, null);
            ImageWriter writer = ImageIO.getImageWritersByMIMEType(mimeType).next();
            ImageWriteParam params = writer.getDefaultWriteParam();
            writer.setOutput(new FileImageOutputStream(to));
            IIOImage image = new IIOImage(dest, null, null);
            writer.write(null, image, params);
            writer.dispose();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Embeds an image watermark over a source image to produce
     * a watermarked one.
     *
     * watermarkImageFile The image file used as the watermark.
     * sourceImageFile    The source image file.
     * destImageFile      The output image file.
     */
    public static void addImageWatermark(File watermarkImageFile,
                                         File sourceImageFile,
                                         File destImageFile,
                                         String imgFormat,
                                         float alpha) throws IOException {
        BufferedImage sourceImage = ImageIO.read(sourceImageFile);
        BufferedImage watermarkImage = ImageIO.read(watermarkImageFile);

        // initializes necessary graphic properties
        Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();
        AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g2d.setComposite(alphaChannel);
        int width = sourceImage.getWidth();
        int scaleWidth = watermarkImage.getWidth();
        int height = sourceImage.getHeight();
        int scaleHeight = watermarkImage.getHeight();
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
        for (int x = 10; x < width; x += 2 * scaleWidth) {
            for (int y = 10; y < height; y += 2 * scaleHeight) {
                g2d.drawImage(watermarkImage, x, y, null);
            }
        }

        ImageIO.write(sourceImage, imgFormat, destImageFile);
        g2d.dispose();

    }


    public static InputStream addImageWatermark(BufferedImage sourceImage, BufferedImage watermarkImage,
                                                float alpha) throws IOException {
        // initializes necessary graphic properties
        Graphics2D g2d = (Graphics2D) sourceImage.getGraphics();
        AlphaComposite alphaChannel = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
        g2d.setComposite(alphaChannel);

        // 平铺水印图片
        for (int x = 0; x < sourceImage.getWidth(); x += watermarkImage.getWidth()) {
            for (int y = 0; y < sourceImage.getHeight(); y += watermarkImage.getHeight()) {
                g2d.drawImage(watermarkImage, x, y, null);
            }
        }

        ByteArrayOutputStream outs = new ByteArrayOutputStream();
        ImageIO.write(sourceImage, "png", outs);
        g2d.dispose();
        ByteArrayInputStream ins = new ByteArrayInputStream(outs.toByteArray());
        return ins;
    }

    public static InputStream ImageWatermark(String filePath, String watermarkFilePath,
                                             float alpha) throws IOException {
        BufferedImage sourceImage = ImageIO.read(new File(filePath));
        BufferedImage watermarkImage = ImageIO.read(new File(watermarkFilePath));
        return addImageWatermark(sourceImage, watermarkImage, alpha);

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
    public static class ImageCaptcha extends InputStream implements TextProducer {

        static final int DEFAULT_WIDTH = Hub.configuration().getInt("captcha_Width", 150);
        static final int DEFAULT_HEIGHT = Hub.configuration().getInt("captcha_Height", 50);
        static final int TextLength = Hub.configuration().getInt("captcha_Text_Length", 4);
        static final int TextMarginBottom = Hub.configuration().getInt("captcha_Text_MarginBottom", 15);
        static final int TextMarginLeft = Hub.configuration().getInt("captcha_Text_MarginLeft", 10);
        static final boolean OutlineFont = ConfGetBool("captcha_Outline_Font", false);
        static final boolean BackgroundTransparent = ConfGetBool("captcha_Background_Transparent", true);
        static final boolean BackgroundGradiated = ConfGetBool("captcha_Background_Gradiated", false);
        static final String GradiatedFromColor = Hub.configuration().getString("captcha_Background_Gradiated_FromColor", "#EDEEF0");
        static final String GradiatedToColor = Hub.configuration().getString("captcha_Background_Gradiated_ToColor", "#C5D0E6");
        static final boolean BackgroundFlatColor = ConfGetBool("captcha_Background_FlatColor", false);
        static final String FlatColor = Hub.configuration().getString("captcha_Background_FlatColor_Color", "#EDEEF0");
        static final boolean RippleGimpyRenderer = ConfGetBool("captcha_GimpyRenderer_RippleGimpyRenderer", true);
        static final boolean BlockGimpyRenderer = ConfGetBool("captcha_GimpyRenderer_BlockGimpyRenderer", false);
        static final int BlockGimpyBlockSize = Hub.configuration().getInt("captcha_GimpyRenderer_BlockGimpyRenderer_BlockSize", 1);
        static final boolean DropShadowGimpyRenderer = ConfGetBool("captcha_DropShadowGimpyRenderer", false);
        static final int DropShadowGimpyRadius = Hub.configuration().getInt("captcha_DropShadowGimpyRenderer_Radius", 3);
        static final int DropShadowGimpyOpacity = Hub.configuration().getInt("captcha_DropShadowGimpyRenderer_Opacity", 75);
        static final boolean CurvedLine = ConfGetBool("captcha_Noise_CurvedLine", false);
        static final int CurvedLineWidth = Hub.configuration().getInt("captcha_Noise_CurvedLine_Width", 2);
        static final String CurvedLineColor = Hub.configuration().getString("captcha_Noise_CurvedLine_Color", "#2795EA");
        static final boolean AddBorder = ConfGetBool("captcha_AddBorder", false);

        private static final String CHAR_CODES = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";

        private static final List<Color> DEFAULT_COLORS = new ArrayList<Color>();
        private static final List<Font> DEFAULT_FONTS = new ArrayList<Font>();

        static {
            List<String> colors = Hub.configuration().getStringList("captcha_Font_Colors");
            if (colors == null) {
                DEFAULT_COLORS.add(Color.BLUE);
                DEFAULT_COLORS.add(Color.GREEN);
                DEFAULT_COLORS.add(Color.RED);
                DEFAULT_COLORS.add(Color.BLACK);
            } else {
                for (String color_name : colors) {
                    DEFAULT_COLORS.add(Color.decode(color_name));
                }
            }

            List<String> fonts = Hub.configuration().getStringList("captcha_Fonts");
            if (fonts == null) {
                DEFAULT_FONTS.add(new Font("Arial", Font.BOLD, 40));
                DEFAULT_FONTS.add(new Font("Courier", Font.BOLD, 40));
                DEFAULT_FONTS.add(new Font("Arial", Font.ITALIC, 40));
                DEFAULT_FONTS.add(new Font("Courier", Font.ITALIC, 40));
            } else {
                for (String font_name : fonts) {
                    DEFAULT_FONTS.add(Font.decode(font_name));
                }
            }
        }

        public static String ContenType = "image/png";

        public static String RandomText() {
            char[] charsArray = CHAR_CODES.toCharArray();
            Random random = new Random(System.currentTimeMillis());
            StringBuffer sb = new StringBuffer(TextLength);
            for (int i = 0; i < TextLength; i++) {
                sb.append(charsArray[random.nextInt(charsArray.length)]);
            }
            return sb.toString();
        }

        public static boolean ConfGetBool(String key, boolean defaultValue) {
            String v = Hub.configuration().getString(key);
            if (StringUtils.isBlank(v)) {
                return defaultValue;
            }

            if (v.equalsIgnoreCase("TRUE") || v.equalsIgnoreCase("YES")) {
                return true;
            } else {
                return false;
            }
        }

        public int width;
        public int height;
        private String answer;

        public ImageCaptcha(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public ImageCaptcha() {
            this.width = DEFAULT_WIDTH;
            this.height = DEFAULT_HEIGHT;
        }

        public void SetAnswer(String text) {
            this.answer = text;
        }

        public String GetAnswer() {
            return this.answer;
        }

        private ByteArrayInputStream bais = null;

        @Override
        public int read() throws IOException {
            check();
            return bais.read();
        }

        @Override
        public int read(byte[] b) throws IOException {
            check();
            return bais.read(b);
        }

        void check() {
            try {
                if (StringUtils.isBlank(this.answer)) {
                    SetAnswer(RandomText());
                }
                if (bais == null) {
                    Captcha.Builder builder = new Captcha.Builder(width, height);

                    WordRenderer wordRenderer;
                    if (OutlineFont) {
                        // 空心字体
                        wordRenderer = new ColoredEdgesWordRenderer(DEFAULT_COLORS, DEFAULT_FONTS);
                    } else {
                        // 实心字体
                        wordRenderer = new KWordRenderer(DEFAULT_COLORS,
                                DEFAULT_FONTS, TextMarginLeft, height - TextMarginBottom);
                    }

                    builder.addText(this, wordRenderer);

                    if (AddBorder) {
                        builder.addBorder();
//                        Logger.debug("ADD Border");
                    }
                    if (CurvedLine) {
                        builder.addNoise(new CurvedLineNoiseProducer(Color.decode(CurvedLineColor),
                                CurvedLineWidth));
                        builder.addNoise(new CurvedLineNoiseProducer(Color.decode(CurvedLineColor),
                                CurvedLineWidth));
                    }
                    if (BackgroundTransparent) {
                        TransparentBackgroundProducer tbp = new TransparentBackgroundProducer();
                        builder.addBackground(tbp);
                    }
                    if (BackgroundFlatColor) {
                        FlatColorBackgroundProducer fbp = new FlatColorBackgroundProducer(Color.decode(FlatColor));
                        builder.addBackground(fbp);
                    }
                    if (BackgroundGradiated) {
                        GradiatedBackgroundProducer gbp = new GradiatedBackgroundProducer();
                        gbp.setFromColor(Color.decode(GradiatedFromColor));
                        gbp.setToColor(Color.decode(GradiatedToColor));
                        builder.addBackground(gbp);
                    }
                    if (RippleGimpyRenderer) {
                        RippleGimpyRenderer rip = new RippleGimpyRenderer();
                        builder.gimp(rip);
                    }
                    if (BlockGimpyRenderer) {
                        BlockGimpyRenderer blk = new BlockGimpyRenderer(BlockGimpyBlockSize);
                        builder.gimp(blk);
                    }
                    if (DropShadowGimpyRenderer) {
                        DropShadowGimpyRenderer dsh = new DropShadowGimpyRenderer(DropShadowGimpyRadius,
                                DropShadowGimpyOpacity);
                        builder.gimp(dsh);
                    }
                    Captcha ca = builder.build();
                    BufferedImage bi = ca.getImage();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bi, "png", baos);
                    bais = new ByteArrayInputStream(baos.toByteArray());
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }


        }

        @Override
        public String getText() {
            return this.answer;
        }
    }
}

class KWordRenderer implements WordRenderer {

    private final List<Color> _colors;
    private final List<Font> _fonts;
    private final int pos_x_;
    private final int pos_y_;

    public KWordRenderer(List<Color> colors, List<Font> fonts, int x, int y) {
        _colors = colors;
        _fonts = fonts;
        pos_y_ = y;
        pos_x_ = x;

    }

    @Override
    public void render(String word, BufferedImage image) {
        Graphics2D g = image.createGraphics();

        RenderingHints hints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.add(new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY));
        g.setRenderingHints(hints);

        g.setColor(getRandomColor());
        FontRenderContext frc = g.getFontRenderContext();
        int startPosX = pos_x_;
        char[] wc = word.toCharArray();
        Random generator = new Random();
        for (char element : wc) {
            char[] itchar = new char[]{element};
            Font itFont = getRandomFont();
            g.setFont(itFont);

            GlyphVector gv = itFont.createGlyphVector(frc, itchar);
            double charWitdth = gv.getVisualBounds().getWidth();

            g.drawChars(itchar, 0, itchar.length, startPosX, pos_y_);
            startPosX = startPosX + (int) charWitdth;
        }
    }

    private Color getRandomColor() {
        return (Color) getRandomObject(_colors);
    }

    private Font getRandomFont() {
        return (Font) getRandomObject(_fonts);
    }

    private Object getRandomObject(List<? extends Object> objs) {
        if (objs.size() == 1) {
            return objs.get(0);
        }

        Random gen = new SecureRandom();
        int i = gen.nextInt(objs.size());
        return objs.get(i);
    }

}
