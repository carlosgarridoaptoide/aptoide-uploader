package com.aptoide.uploader.account.view;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.aptoide.uploader.ActivityNavigator;
import com.aptoide.uploader.R;
import com.aptoide.uploader.UploaderApplication;
import com.aptoide.uploader.account.AptoideAccountManager;
import com.aptoide.uploader.view.android.FragmentView;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxrelay2.PublishRelay;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class LoginFragment extends FragmentView implements LoginView {

  private EditText passwordEditText;
  private EditText usernameEditText;
  private View progressContainer;
  private View fragmentContainer;
  private TextView loadingTextView;
  private AptoideAccountManager accountManager;
  private View loginButton;
  private View facebookLoginButton;
  private View signUpButton;

  public static LoginFragment newInstance() {
    return new LoginFragment();
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    accountManager =
        ((UploaderApplication) getContext().getApplicationContext()).getAccountManager();
  }

  @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    passwordEditText = view.findViewById(R.id.fragment_login_password_edit_text);
    usernameEditText = view.findViewById(R.id.fragment_login_username_edit_text);
    loginButton = view.findViewById(R.id.fragment_login_button);
    facebookLoginButton = view.findViewById(R.id.fragment_login_facebook_button);
    signUpButton = view.findViewById(R.id.fragment_login_sign_up);
    progressContainer = view.findViewById(R.id.fragment_login_progress_container);
    loadingTextView = view.findViewById(R.id.fragment_login_loading_text_view);
    fragmentContainer = view.findViewById(R.id.fragment_login_content);

    FacebookSdk.sdkInitialize(getContext().getApplicationContext());
    LoginManager loginManager = LoginManager.getInstance();
    CallbackManager callbackManager = CallbackManager.Factory.create();
    PublishRelay<FacebookLoginResult> facebookLoginSubject = PublishRelay.create();

    new LoginPresenter(this, accountManager,
        new LoginNavigator(getContext(), getFragmentManager(), loginManager, callbackManager,
            facebookLoginSubject, getActivity(), (ActivityNavigator) getActivity()),
        new CompositeDisposable(), AndroidSchedulers.mainThread()).present();
  }

  @Override public void onDestroyView() {
    passwordEditText = null;
    usernameEditText = null;
    loginButton = null;
    signUpButton = null;
    fragmentContainer = null;
    progressContainer = null;
    loadingTextView = null;
    super.onDestroyView();
  }

  @Nullable @Override
  public android.view.View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_login, container, false);
  }

  @Override public Observable<CredentialsViewModel> getLoginEvent() {
    return RxView.clicks(loginButton)
        .map(__ -> getViewModel());
  }

  @Override public Observable<?> getFacebookLoginEvent() {
    return RxView.clicks(facebookLoginButton)
        .map(__ -> getViewModel());
  }

  @Override public Observable<CredentialsViewModel> getOpenCreateAccountView() {
    return RxView.clicks(signUpButton)
        .map(__ -> getViewModel());
  }

  @Override public void showLoading(String username) {
    loadingTextView.setText(getString(R.string.logging_as).concat(" " + username));
    fragmentContainer.setVisibility(View.GONE);
    progressContainer.setVisibility(View.VISIBLE);
  }

  @Override public void showLoading() {
    loadingTextView.setText(getString(R.string.logging_in));
    fragmentContainer.setVisibility(View.GONE);
    progressContainer.setVisibility(View.VISIBLE);
  }

  @Override public void hideLoading() {
    progressContainer.setVisibility(View.GONE);
    fragmentContainer.setVisibility(View.VISIBLE);
  }

  @Override public void showCrendentialsError() {
    Toast.makeText(getContext(), R.string.loginFail, Toast.LENGTH_SHORT)
        .show();
  }

  @Override public void showNetworkError() {
    Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT)
        .show();
  }

  @NonNull private CredentialsViewModel getViewModel() {
    return new CredentialsViewModel(usernameEditText.getText()
        .toString(), passwordEditText.getText()
        .toString());
  }
}
