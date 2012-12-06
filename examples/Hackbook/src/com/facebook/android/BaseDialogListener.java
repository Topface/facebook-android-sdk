package com.facebook.topface;

import com.facebook.topface.Facebook.DialogListener;

/**
 * Skeleton base class for RequestListeners, providing default error handling.
 * Applications should handle these error conditions.
 */
public abstract class BaseDialogListener implements DialogListener {

    @Override
    public void onFacebookError(FacebookError e) {
        e.printStackTrace();
    }

    @Override
    public void onError(DialogError e) {
        e.printStackTrace();
    }

    @Override
    public void onCancel() {
    }

}