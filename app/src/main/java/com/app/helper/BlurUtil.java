package com.app.helper;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;


public class BlurUtil {
    public static Bitmap blur(Context context, Bitmap image, float radius) {
        if (radius < 0 || radius > 25) {
            throw new IllegalArgumentException("Blur radius must be between 0 and 25");
        }

        Bitmap outputBitmap = Bitmap.createBitmap(image);
        RenderScript rs = RenderScript.create(context);

        Allocation input = Allocation.createFromBitmap(rs, image);
        Allocation output = Allocation.createFromBitmap(rs, outputBitmap);

        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        blur.setInput(input);
        blur.setRadius(radius);
        blur.forEach(output);

        output.copyTo(outputBitmap);
        rs.destroy();

        return outputBitmap;
    }
}
/*

public class BlurUtil {

    public static Bitmap blur(Context context, Bitmap image, float radius) {
        // Create an output bitmap
        Bitmap outputBitmap = Bitmap.createBitmap(image);
        // Initialize RenderScript
        RenderScript renderScript = RenderScript.create(context);

        // Create input and output allocations
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        // Create the blur script
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        blurScript.setRadius(radius); // Radius ranges from 0 to 25
        blurScript.setInput(tmpIn);
        blurScript.forEach(tmpOut);

        // Copy the output to the output bitmap
        tmpOut.copyTo(outputBitmap);

        // Destroy the RenderScript
        renderScript.destroy();

        return outputBitmap;
    }
}
*/
