package io.multy.ui.fragments.dialogs;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.multy.R;

public class SeedDialogFragment extends DialogFragment {

    private int titleResId;
    private int messageResId;
    private View.OnClickListener listener;

    @BindView(R.id.text_title)
    TextView textViewTitle;

    @BindView(R.id.text_message)
    TextView textViewMessage;

    public static SeedDialogFragment newInstance(int titleResId, int messageResId, View.OnClickListener positiveListener) {
        SeedDialogFragment seedDialogFragment = new SeedDialogFragment();
        seedDialogFragment.setTitleResId(titleResId);
        seedDialogFragment.setMessageResId(messageResId);
        seedDialogFragment.setListener(positiveListener);
        return seedDialogFragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_blue, container, false);
        ButterKnife.bind(this, view);
        textViewMessage.setText(messageResId);
        textViewTitle.setText(titleResId);
        return view;
    }

    @OnClick(R.id.button_positive)
    public void onClickPositive() {
        listener.onClick(null);
        dismiss();
    }

    @OnClick(R.id.button_negative)
    public void onClickNegative() {
        dismiss();
    }

    public int getTitleResId() {
        return titleResId;
    }

    public void setTitleResId(int titleResId) {
        this.titleResId = titleResId;
    }

    public int getMessageResId() {
        return messageResId;
    }

    public void setMessageResId(int messageResId) {
        this.messageResId = messageResId;
    }

    public View.OnClickListener getListener() {
        return listener;
    }

    public void setListener(View.OnClickListener listener) {
        this.listener = listener;
    }
}
