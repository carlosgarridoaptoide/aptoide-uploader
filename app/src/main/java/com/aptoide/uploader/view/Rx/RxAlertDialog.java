package com.aptoide.uploader.view.Rx;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.View;
import com.jakewharton.rxrelay2.PublishRelay;
import io.reactivex.Observable;

public class RxAlertDialog implements DialogInterface {

  private final AlertDialog dialog;
  private final DialogClick positiveClick;

  protected RxAlertDialog(AlertDialog dialog, DialogClick positiveClick) {
    this.dialog = dialog;
    this.positiveClick = positiveClick;
  }

  public void show() {
    dialog.show();
  }

  public boolean isShowing() {
    return dialog.isShowing();
  }

  @Override public void cancel() {
    dialog.cancel();
  }

  @Override public void dismiss() {
    dialog.dismiss();
  }

  public Observable<DialogInterface> positiveClicks() {
    if (positiveClick != null) {
      return positiveClick.clicks()
          .map(click -> this);
    }
    return Observable.empty();
  }

  public static class Builder {

    private final AlertDialog.Builder builder;

    private DialogClick positiveClick;
    private DialogClick negativeClick;

    public Builder(Context context) {
      this.builder = new AlertDialog.Builder(context);
    }

    public Builder setView(View view) {
      builder.setView(view);
      return this;
    }

    public Builder setTitle(@StringRes int titleId) {
      builder.setTitle(titleId);
      return this;
    }

    public Builder setMessage(@StringRes int messageId) {
      builder.setMessage(messageId);
      return this;
    }

    public Builder setPositiveButton(@StringRes int textId) {
      positiveClick = new DialogClick(DialogInterface.BUTTON_POSITIVE, PublishRelay.create());
      builder.setPositiveButton(textId, positiveClick);
      return this;
    }

    public Builder setNegativeButton(@StringRes int textId) {
      negativeClick = new DialogClick(DialogInterface.BUTTON_NEGATIVE, PublishRelay.create());
      builder.setNegativeButton(textId, negativeClick);
      return this;
    }

    public RxAlertDialog build() {
      final AlertDialog dialog = builder.create();
      return new RxAlertDialog(dialog, positiveClick);
    }
  }

  protected static class DialogClick implements DialogInterface.OnClickListener {

    private final int which;
    private final PublishRelay<String> subject;

    public DialogClick(int which, PublishRelay<String> subject) {
      this.which = which;
      this.subject = subject;
    }

    @Override public void onClick(DialogInterface dialog, int which) {
      if (this.which == which) {
        subject.accept("");
      }
    }

    public Observable<String> clicks() {
      return subject;
    }
  }
}
