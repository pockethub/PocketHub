package com.github.mobile.android.authenticator;


import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import com.github.mobile.android.R;
import com.google.inject.Inject;

import org.eclipse.egit.github.core.User;
import org.eclipse.egit.github.core.client.HttpClient;
import org.eclipse.egit.github.core.service.UserService;
import roboguice.activity.RoboAccountAuthenticatorActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;


import static android.accounts.AccountManager.KEY_BOOLEAN_RESULT;
import static android.text.TextUtils.isEmpty;
import static com.github.mobile.android.authenticator.Constants.GITHUB_ACCOUNT_TYPE;

public class GitHubAuthenticatorActivity extends RoboAccountAuthenticatorActivity {
    public static final String PARAM_CONFIRMCREDENTIALS = "confirmCredentials";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";

    private static final String TAG = "GHAuthenticatorActivity";

    private AccountManager mAccountManager;
    @InjectView(R.id.message) TextView mMessage;
    @InjectView(R.id.username_edit) EditText mUsernameEdit;
    @InjectView(R.id.password_edit) EditText mPasswordEdit;

	@Inject
	private HttpClient<?> client;

    private RoboAsyncTask<User> authenticationTask;
    private String mAuthtoken;
    private String mAuthtokenType;

    /**
     * If set we are just checking that the user knows their credentials; this
     * doesn't cause the user's password to be changed on the device.
     */
    private Boolean mConfirmCredentials = false;

    /** for posting authentication attempts back to UI thread */
    private final Handler mHandler = new Handler();


    private String mPassword;

    /** Was the original caller asking for an entirely new account? */
    protected boolean mRequestNewAccount = false;

    private String mUsername;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle icicle) {
        Log.i(TAG, "onCreate(" + icicle + ")");
        super.onCreate(icicle);
        mAccountManager = AccountManager.get(this);
        Log.i(TAG, "loading data from Intent");
        final Intent intent = getIntent();
        mUsername = intent.getStringExtra(PARAM_USERNAME);
        mAuthtokenType = intent.getStringExtra(PARAM_AUTHTOKEN_TYPE);
        mRequestNewAccount = mUsername == null;
        mConfirmCredentials = intent.getBooleanExtra(PARAM_CONFIRMCREDENTIALS, false);

        Log.i(TAG, "    request new: " + mRequestNewAccount);
        
        requestWindowFeature(Window.FEATURE_LEFT_ICON);
        setContentView(R.layout.login_activity);
        getWindow().setFeatureDrawableResource(Window.FEATURE_LEFT_ICON,
            android.R.drawable.ic_dialog_alert);

//        mUsernameEdit.setText(mUsername);
//        mMessage.setText(getMessage());
    }

    /*
     * {@inheritDoc}
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        final ProgressDialog dialog = new ProgressDialog(this);
        // dialog.setMessage(getText(R.string.ui_activity_authenticating));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.i(TAG, "dialog cancel has been invoked");
                if (authenticationTask != null) {
                    authenticationTask.cancel(true);
                    finish();
                }
            }
        });
        return dialog;
    }

    /**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication.
     *
     * Specified by android:onClick="handleLogin" in the layout xml
     */
    public void handleLogin(View view) {
        Log.d(TAG, "handleLogin hit on"+view);
        if (mRequestNewAccount) {
            mUsername = mUsernameEdit.getText().toString();
        }
        mPassword = mPasswordEdit.getText().toString();
        if (isEmpty(mUsername) || isEmpty(mPassword)) {
            mMessage.setText(getMessage());
        } else {
            showProgress();

            authenticationTask = new RoboAsyncTask<User>(this) {
                public User call() throws Exception {
                    client.setCredentials(mUsername, mPassword);

                    return new UserService(client).getUser();
                }

                @Override
                protected void onException(Exception e) throws RuntimeException {
                    mMessage.setText(e.getMessage());
                }

                public void onSuccess(User user) {
                    onAuthenticationResult(true);
                }

                @Override
                protected void onFinally() throws RuntimeException {
                    hideProgress();
                }
            };
            authenticationTask.execute();
        }
    }

    /**
     * Called when response is received from the server for confirm credentials
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller.
     *
     */
    protected void finishConfirmCredentials(boolean result) {
        Log.i(TAG, "finishConfirmCredentials()");
        final Account account = new Account(mUsername, GITHUB_ACCOUNT_TYPE);
        mAccountManager.setPassword(account, mPassword);

        final Intent intent = new Intent();
        intent.putExtra(KEY_BOOLEAN_RESULT, result);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    /**
     *
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. Also sets
     * the authToken in AccountManager for this account.
     *
     */

    protected void finishLogin() {
        Log.i(TAG, "finishLogin()");
        final Account account = new Account(mUsername, GITHUB_ACCOUNT_TYPE);

        if (mRequestNewAccount) {
            mAccountManager.addAccountExplicitly(account, mPassword, null);
            // Set contacts sync for this account.
            ContentResolver.setSyncAutomatically(account,
                    ContactsContract.AUTHORITY, true);
        } else {
            mAccountManager.setPassword(account, mPassword);
        }
        final Intent intent = new Intent();
        mAuthtoken = mPassword;
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, mUsername);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, GITHUB_ACCOUNT_TYPE);
        if (mAuthtokenType != null
            && mAuthtokenType.equals(Constants.AUTHTOKEN_TYPE)) {
            intent.putExtra(AccountManager.KEY_AUTHTOKEN, mAuthtoken);
        }
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

    protected void hideProgress() {
        dismissDialog(0);
    }
    protected void showProgress() {
        showDialog(0);
    }

    /**
     * Called when the authentication process completes (see attemptLogin()).
     */
    public void onAuthenticationResult(boolean result) {
        Log.i(TAG, "onAuthenticationResult(" + result + ")");
        if (result) {
            if (!mConfirmCredentials) {
                finishLogin();
            } else {
                finishConfirmCredentials(true);
            }
        } else {
            Log.e(TAG, "onAuthenticationResult: failed to authenticate");
            if (mRequestNewAccount) {
                // "Please enter a valid username/password.
//                mMessage
//                    .setText(getText(R.string.login_activity_loginfail_text_both));
				mMessage.setText("Please enter a valid username/password.");
            } else {
                // "Please enter a valid password." (Used when the
                // account is already in the database but the password
                // doesn't work.)
//                mMessage
//                    .setText(getText(R.string.login_activity_loginfail_text_pwonly));
				mMessage.setText("Please enter a valid password.");
            }
        }
    }

    /**
     * Returns the message to be displayed at the top of the login dialog box.
     */
    private CharSequence getMessage() {
//        getString(R.string.label);
//        if (isEmpty(mUsername)) {
//            // If no username, then we ask the user to log in using an
//            // appropriate service.
//            final CharSequence msg =
//                getText(R.string.login_activity_newaccount_text);
//            return msg;
//        }
//        if (isEmpty(mPassword)) {
//            // We have an account but no password
//            return getText(R.string.login_activity_loginfail_text_pwmissing);
//        }
        return null;
    }

}
