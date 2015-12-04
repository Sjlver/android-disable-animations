package ch.epfl.sweng.disableanimations;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.test.ApplicationTestCase;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DisableAnimationsTest extends ApplicationTestCase<Application> {
    public DisableAnimationsTest() {
        super(Application.class);
    }

    /**
     * This function sets up the emulator so that it is adapted for testing. In particular, we
     * disable animations.
     *
     * @see https://github.com/JakeWharton/u2020
     * @see https://gist.github.com/daj/7b48f1b8a92abf960e7b
     * @see https://code.google.com/p/android-test-kit/wiki/DisablingAnimations
     * @see https://gist.github.com/danielgomezrico/9371a79a7222a156ddad
     */
    public void testDisableAnimations() throws ClassNotFoundException, NoSuchMethodException,
            InvocationTargetException, IllegalAccessException {
        disableAnimations(getContext());
    }

    private void disableAnimations(Context context) throws ClassNotFoundException,
            NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        int permStatus = context.checkCallingOrSelfPermission(Manifest.permission.SET_ANIMATION_SCALE);
        if (permStatus == PackageManager.PERMISSION_GRANTED) {
            setSystemAnimationsScale(0.0f);
        } else {
            throw new AssertionError("SET_ANIMATION_SCALE permission is not granted.");
        }
    }

    private void setSystemAnimationsScale(float animationScale) throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<?> windowManagerStubClazz = Class.forName("android.view.IWindowManager$Stub");
        Method asInterface = windowManagerStubClazz.getDeclaredMethod("asInterface", IBinder.class);
        Class<?> serviceManagerClazz = Class.forName("android.os.ServiceManager");
        Method getService = serviceManagerClazz.getDeclaredMethod("getService", String.class);
        Class<?> windowManagerClazz = Class.forName("android.view.IWindowManager");
        Method setAnimationScales = windowManagerClazz.getDeclaredMethod("setAnimationScales", float[].class);
        Method getAnimationScales = windowManagerClazz.getDeclaredMethod("getAnimationScales");

        IBinder windowManagerBinder = (IBinder) getService.invoke(null, "window");
        Object windowManagerObj = asInterface.invoke(null, windowManagerBinder);
        float[] currentScales = (float[]) getAnimationScales.invoke(windowManagerObj);
        for (int i = 0; i < currentScales.length; i++) {
            currentScales[i] = animationScale;
        }
        Log.d("DisableAnimations", "Setting animation scales to " + animationScale);
        setAnimationScales.invoke(windowManagerObj, new Object[]{currentScales});
    }
}