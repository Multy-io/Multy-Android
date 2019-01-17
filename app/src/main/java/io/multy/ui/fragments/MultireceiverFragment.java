package io.multy.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airbnb.lottie.LottieAnimationView;

import java.security.MessageDigest;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.multy.R;
import io.multy.storage.RealmManager;
import io.multy.ui.Hash2PicView;

public class MultireceiverFragment extends Fragment {

    @BindView(R.id.mr_icon)
    Hash2PicView mrIdIcon;
    @BindView(R.id.animation_view)
    LottieAnimationView wavesAnimation;

    private static final char[] HEX_CHARS = "0123456789ABCDEF".toCharArray();

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        ViewGroup convertView = (ViewGroup) inflater.inflate(R.layout.item_multi_receiver, container, false);
        ButterKnife.bind(this, convertView);

        return convertView;
    }

    public void updateMultireceiverView() {
        mrIdIcon.setAvatar(getMultireceiverAvatar());
        wavesAnimation.playAnimation();
    }

    private String getMultireceiverAvatar() {
        String userId = RealmManager.getSettingsDao().getUserId().getUserId();
        String userIdHex = md5(userId);

        return stringToHex(userIdHex.getBytes());
    }

    private String stringToHex(byte[] buf) {
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }

    public static final String md5(final String s) {
        final String MD5 = "MD5";
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance(MD5);
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuilder hexString = new StringBuilder();
            for (byte aMessageDigest : messageDigest) {
                String h = Integer.toHexString(0xFF & aMessageDigest);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (java.security.NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }
}
