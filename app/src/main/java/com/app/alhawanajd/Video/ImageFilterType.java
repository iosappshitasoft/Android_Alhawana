package com.app.alhawanajd.Video;

import android.content.Context;

import com.app.alhawanajd.Video.imagefilter.GPUImageBoxBlurFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageBrightnessFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageBulgeDistortionFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageCGAColorspaceFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageContrastFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageCrosshatchFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageExposureFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageGammaFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageGaussianBlurFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageHalftoneFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageHazeFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageHighlightShadowFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageHueFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageLuminanceFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageLuminanceThresholdFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageMonochromeFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageOpacityFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImagePosterizeFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageRGBFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageSaturationFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageSharpenFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageSolarizeFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageSwirlFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageToneCurveFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageVibranceFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageVignetteFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageWeakPixelInclusionFilter;
import com.app.alhawanajd.Video.imagefilter.GPUImageZoomBlurFilter;

import java.util.Arrays;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter;

public enum ImageFilterType {

    Normal,
    Brightness,
    Exposure,
    Contrast,
    Saturation,
    Gamma,
    Hue,
    Vibrance,
    HighlightShadow,
    Monochrome,
    Haze,
    Luminance,
    LuminanceThreshold,
    Sharpen,
    GaussianBlur,
    BoxBlur,
    ZoomBlur,
    Halftone,
    Crosshatch,
    Posterize,
    Swirl,

    BulgeDistortion,
    CGAColorspace,
    ImageLookUp,
    Opacity,
    RGB,
    Sepia,
    Solarize,
    Vignette,
    WeakPixelInclusion;

    public static List<ImageFilterType> gpuImageView() {
        return Arrays.asList(ImageFilterType.values());
    }


    public static GPUImageFilter gpuImageView(int pos, Context context) {

        switch (pos) {
            case 1:
                GPUImageBrightnessFilter gpuImageBrightnessFilter = new GPUImageBrightnessFilter();
                gpuImageBrightnessFilter.setBrightness(0.2f);
                return gpuImageBrightnessFilter;
            case 2:
                return new GPUImageExposureFilter();
            case 3:

                GPUImageContrastFilter gpuImageContrastFilter = new GPUImageContrastFilter();
                gpuImageContrastFilter.setContrast(2.5f);
                return gpuImageContrastFilter;
            case 4:
                return new GPUImageSaturationFilter();
            case 5:
                GPUImageGammaFilter glGammaFilter = new GPUImageGammaFilter();
                glGammaFilter.setGamma(2f);
                return glGammaFilter;
            case 6:
                return new GPUImageHueFilter();
            case 7:
                GPUImageVibranceFilter glVibranceFilter = new GPUImageVibranceFilter();
                glVibranceFilter.setVibrance(3f);
                return glVibranceFilter;
            /*case 8:
                GPUImageWhiteBalanceFilter glWhiteBalanceFilter = new GPUImageWhiteBalanceFilter();
                glWhiteBalanceFilter.setTemperature(2400f);
                glWhiteBalanceFilter.setTint(2f);
                return glWhiteBalanceFilter;*/
            case 8:
                return new GPUImageHighlightShadowFilter();
            case 9:
            case 26:
                return new GPUImageMonochromeFilter();
            case 10:
                GPUImageHazeFilter glHazeFilter = new GPUImageHazeFilter();
                glHazeFilter.setSlope(-0.5f);
                return glHazeFilter;
            case 11:
                return new GPUImageLuminanceFilter();
            case 12:
                return new GPUImageLuminanceThresholdFilter();
            case 13:
                GPUImageSharpenFilter glSharpenFilter = new GPUImageSharpenFilter();
                glSharpenFilter.setSharpness(4f);
                return glSharpenFilter;
            case 14:
                return new GPUImageGaussianBlurFilter();
            case 15:
                return new GPUImageBoxBlurFilter();
            case 16:
                return new GPUImageZoomBlurFilter();
            case 17:
                return new GPUImageHalftoneFilter();
            case 18:
                return new GPUImageCrosshatchFilter();
            case 19:
                return new GPUImagePosterizeFilter();
            case 20:
                return new GPUImageSwirlFilter();

            // Android
            case 21:
                return new GPUImageBulgeDistortionFilter();
            case 22:
                return new GPUImageCGAColorspaceFilter();
            /*case 24:
                return new GPUImageGrayScaleFilter();
            case 25:
                return new GPUImageInvertFilter();*/
            case 23:
                /*Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.search_white);
                return new GPUImageLookUpTableFilter(bitmap);*/
                GPUImageRGBFilter glRGBFilter2 = new GPUImageRGBFilter();
                glRGBFilter2.setRed(2f);
                return glRGBFilter2;
            case 24:
                return new GPUImageOpacityFilter();
            case 25:
                GPUImageRGBFilter glRGBFilter = new GPUImageRGBFilter();
                glRGBFilter.setRed(0f);
                return glRGBFilter;
            case 27:
                return new GPUImageSolarizeFilter();
            case 28:
                return new GPUImageVignetteFilter();
            case 29:
                return new GPUImageWeakPixelInclusionFilter();
            case 0:
            default:
                return new GPUImageToneCurveFilter();
            /*try {
                    //InputStream is = context.getAssets().open("acv/tone_cuver_sample.acv");
                } catch (IOException e) {
                    Log.e("FilterType", "Error");
                }
                return new GPUImageFilter();*/
        }
    }

    /*public static GlFilter createGlFilter(FilterType filterType, Context context) {
        switch (filterType) {
            case BILATERAL_BLUR:
                return new GlBilateralFilter();
            case BOX_BLUR:
                return new GlBoxBlurFilter();
            case BRIGHTNESS:
                GlBrightnessFilter glBrightnessFilter = new GlBrightnessFilter();
                glBrightnessFilter.setBrightness(0.2f);
                return glBrightnessFilter;
            case BULGE_DISTORTION:
                return new GlBulgeDistortionFilter();
            case CGA_COLORSPACE:
                return new GlCGAColorspaceFilter();
            case CONTRAST:
                GlContrastFilter glContrastFilter = new GlContrastFilter();
                glContrastFilter.setContrast(2.5f);
                return glContrastFilter;
            case CROSSHATCH:
                return new GlCrosshatchFilter();
            case EXPOSURE:
                return new GlExposureFilter();
            case FILTER_GROUP_SAMPLE:
                return new GlFilterGroup(new GlSepiaFilter(), new GlVignetteFilter());
            case GAMMA:
                GlGammaFilter glGammaFilter = new GlGammaFilter();
                glGammaFilter.setGamma(2f);
                return glGammaFilter;
            case GAUSSIAN_FILTER:
                return new GlGaussianBlurFilter();
            case GRAY_SCALE:
                return new GlGrayScaleFilter();
            case HALFTONE:
                return new GlHalftoneFilter();
            case HAZE:
                GlHazeFilter glHazeFilter = new GlHazeFilter();
                glHazeFilter.setSlope(-0.5f);
                return glHazeFilter;
            case HIGHLIGHT_SHADOW:
                return new GlHighlightShadowFilter();
            case HUE:
                return new GlHueFilter();
            case INVERT:
                return new GlInvertFilter();
            case LOOK_UP_TABLE_SAMPLE:
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.search_white);
                return new GlLookUpTableFilter(bitmap);
            case LUMINANCE:
                return new GlLuminanceFilter();
            case LUMINANCE_THRESHOLD:
                return new GlLuminanceThresholdFilter();
            case MONOCHROME:
                return new GlMonochromeFilter();
            case OPACITY:
                return new GlOpacityFilter();
            case PIXELATION:
                return new GlPixelationFilter();
            case POSTERIZE:
                return new GlPosterizeFilter();
            case RGB:
                GlRGBFilter glRGBFilter = new GlRGBFilter();
                glRGBFilter.setRed(0f);
                return glRGBFilter;
            case SATURATION:
                return new GlSaturationFilter();
            case SEPIA:
                return new GlSepiaFilter();
            case SHARP:
                GlSharpenFilter glSharpenFilter = new GlSharpenFilter();
                glSharpenFilter.setSharpness(4f);
                return glSharpenFilter;
            case SOLARIZE:
                return new GlSolarizeFilter();
            case SPHERE_REFRACTION:
                return new GlSphereRefractionFilter();
            case SWIRL:
                return new GlSwirlFilter();
            case TONE_CURVE_SAMPLE:
                try {
                    InputStream is = context.getAssets().open("acv/tone_cuver_sample.acv");
                    return new GlToneCurveFilter(is);
                } catch (IOException e) {
                    Log.e("FilterType", "Error");
                }
                return new GlFilter();
            case TONE:
                return new GlToneFilter();
            case VIBRANCE:
                GlVibranceFilter glVibranceFilter = new GlVibranceFilter();
                glVibranceFilter.setVibrance(3f);
                return glVibranceFilter;
            case VIGNETTE:
                return new GlVignetteFilter();
            case WATERMARK:
                return new GlWatermarkFilter(BitmapFactory.decodeResource(context.getResources(), R.drawable.search), GlWatermarkFilter.Position.RIGHT_BOTTOM);
            case WEAK_PIXEL:
                return new GlWeakPixelInclusionFilter();
            case WHITE_BALANCE:
                GlWhiteBalanceFilter glWhiteBalanceFilter = new GlWhiteBalanceFilter();
                glWhiteBalanceFilter.setTemperature(2400f);
                glWhiteBalanceFilter.setTint(2f);
                return glWhiteBalanceFilter;
            case ZOOM_BLUR:
                return new GlZoomBlurFilter();
            case DEFAULT:
            default:
                return new GlFilter();
        }
    }*/
}
