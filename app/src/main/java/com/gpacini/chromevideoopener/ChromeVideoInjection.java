package com.gpacini.chromevideoopener;

import android.content.Context;
import android.content.Intent;
import android.content.res.XResources;
import android.net.Uri;
import android.view.ContextMenu;
import android.view.Menu;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by gavinpacini on 18/01/16.
 */
public class ChromeVideoInjection implements IXposedHookLoadPackage {

    private static final String FILTER_PACKAGE = "com.android.chrome";
    private static final String HOOK_CLASS = "org.chromium.chrome.browser.contextmenu.ChromeContextMenuPopulator";
    private static final String HOOK_METHOD_BUILD = "buildContextMenu";
    private static final String HOOK_METHOD_CLICK = "onItemSelected";
    private static final String HOOK_ARG_PARAMS = "org.chromium.chrome.browser.contextmenu.ContextMenuParams";
    private static final String HOOK_ARG_HELPER = "org.chromium.chrome.browser.contextmenu.ContextMenuHelper";

    private static final int RANDOM_ID = XResources.getFakeResId("R.id.contextmenu_open_video");

    private Context context;

    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals(FILTER_PACKAGE)) {
            return;
        }

        XposedHelpers.findAndHookMethod(HOOK_CLASS, lpparam.classLoader, HOOK_METHOD_BUILD, ContextMenu.class, Context.class, HOOK_ARG_PARAMS, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                ContextMenu menu = (ContextMenu) param.args[0];

                context = (Context) param.args[1];

                boolean shouldShow = (boolean) param.args[2].getClass().getMethod("isVideo").invoke(param.args[2], (Object[]) null);

                if (shouldShow) {
                    menu.add(Menu.NONE, RANDOM_ID, Menu.NONE, "Open video with...");
                }
            }
        });

        XposedHelpers.findAndHookMethod(HOOK_CLASS, lpparam.classLoader, HOOK_METHOD_CLICK, HOOK_ARG_HELPER, HOOK_ARG_PARAMS, int.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                int resId = (int) param.args[2];

                if(resId == RANDOM_ID){

                    String sourceUrl = (String) param.args[1].getClass().getMethod("getSrcUrl").invoke(param.args[1], (Object[]) null);

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl));
                    intent.setDataAndType(Uri.parse(sourceUrl), "video/*");
                    context.startActivity(intent);
                }
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
            }
        });
    }

}
